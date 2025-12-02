// *************************************************************
//  Cellpose 2D – Detection within selected parent objects (QuPath 0.6.x)
// 
//  Features:
//  - Model dropdown from CELLPOSE_LOCAL_MODELS_PATH (filenames only)
//    Excludes: 'cpsam'/'cpsam.pb' and any files starting with 'size_' or '_-'.
//  - Channel dropdowns (following Cellpose CLI conventions):
//     * chan (required): primary channel to segment (0=GRAY, 1=RED, 2=GREEN, 3=BLUE)
//       This contains the cell/nuclei/bacteria signal depending on the model chosen.
//     * chan2 (optional): nuclear channel for cyto models only (0=NONE, 1=RED, 2=GREEN, 3=BLUE)
//  - Dynamic classification:
//     * "Nuclei" when cellexpansion == 0
//     * "Cells"  when cellexpansion  > 0
// - Parameters are remembered between runs
//************************************************************/

import qupath.ext.biop.cellpose.Cellpose2D
import qupath.lib.gui.dialogs.Dialogs
import qupath.lib.plugins.parameters.ParameterList
import qupath.opencv.ops.ImageOps
import qupath.lib.gui.QuPathGUI
import javafx.scene.control.ButtonType

// ---------------------------
// Toggle: show parameters dialog?
// ---------------------------
boolean showSettingsDialog = true

// ---------------------------
// Defaults (used when no dialog)
// ---------------------------
double diameterUmDefault         = 20.0
double thresholdDefault          = 0.0
double flowThresholdDefault      = 0.4
double cellexpansionDefault      = 0.0         // µm; 0 = nuclei only
int    tileSizeDefault           = 4096
boolean createAnnotationsDefault = false
int    meanfiltersizeDefault     = 0
int    gaussianfiltersizeDefault = 0
boolean excludeOnBordersDefault  = true

// Headless channel defaults (1-based; 0 = none for chan2)
// chan: primary segmentation channel (1-based index)
// chan2: optional nuclear channel for cyto models (0 = none)
int chanDefaultIndex  = 1
int chan2DefaultIndex = 0

// ---------------------------
// Resolve models from environment
// ---------------------------
def envDir = System.getenv('CELLPOSE_LOCAL_MODELS_PATH')
if (!envDir)
    throw new RuntimeException("Environment variable CELLPOSE_LOCAL_MODELS_PATH is not set.")
def envDirFile = new File(envDir)
if (!envDirFile.isDirectory())
    throw new RuntimeException("CELLPOSE_LOCAL_MODELS_PATH does not point to a valid folder: " + envDir)

// Accept any file, but exclude cpsam, size_*, and _-CellposeModels.txt
def modelFiles = envDirFile.listFiles()?.findAll { f ->
    if (!f.isFile()) return false
    def lower = f.name.toLowerCase()
    def excluded = (lower == 'cpsam') || 
                   (lower == 'cpsam.pb') || 
                   lower.startsWith('size_') ||
                   lower.startsWith('_-')
    return !excluded
} ?: []

if (modelFiles.isEmpty())
    throw new RuntimeException("No usable models found in ${envDir} (after excluding cpsam, size_*, and _-*).")

// Build name -> file map; show only names in dropdown
def modelNames = modelFiles.collect { it.name }.sort { it.toLowerCase() }
def nameToFile = modelFiles.collectEntries { [(it.name): it] }

// Prefer 'cyto3' as default if found, otherwise use first model
def defaultModelName = modelNames.find { it.toLowerCase() == 'cyto3' } ?: modelNames.first()

// ---------------------------
// Server & calibration
// ---------------------------
def server = getCurrentServer()
if (server == null)
    throw new IllegalStateException("No active image/server found.")
def cal = server.getPixelCalibration()
if (cal == null)
    throw new IllegalStateException("No pixel calibration found for the current server.")

// ---------------------------
// Check for selected parent objects early
// ---------------------------
def pathObjects = getSelectedObjects()
if (pathObjects == null || pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("Cellpose", "Please select at least one parent object (e.g., ROI or annotation).")
    return
}

// ---------------------------
// Helpers: channel names & defaults
// ---------------------------
def getChannelNames = {
    def md = server.getMetadata()
    def channels = md?.getChannels()
    def names = (channels != null && !channels.isEmpty()) ?
        channels.collect { it?.getName() ?: "" } : []
    if (names.isEmpty()) {
        int sizeC = md?.getSizeC() ?: 1
        names = (0..<sizeC).collect { "Channel ${it+1}" }
    }
    return names
}

def pickDefaultChanName = { List<String> names ->
    // Default to DAPI if present (common for nuclei models), otherwise first channel
    int idxDapi = names.findIndexOf { (it ?: "").toLowerCase().contains("dapi") }
    return (idxDapi >= 0) ? names[idxDapi] : names[0]
}

// ---------------------------
// Parameters (dialog or defaults)
// ---------------------------
String selectedModelName    = defaultModelName
double diameterUm           = diameterUmDefault
double threshold            = thresholdDefault
double flowThreshold        = flowThresholdDefault
double cellexpansion        = cellexpansionDefault
int    tileSize             = tileSizeDefault
boolean createAnnotations   = createAnnotationsDefault
int    meanfiltersize       = meanfiltersizeDefault
int    gaussianfiltersize   = gaussianfiltersizeDefault
boolean excludeOnBorders    = excludeOnBordersDefault

// Channels (1-based indices for Cellpose; 0 for none in chan2)
// chan: primary segmentation channel, chan2: optional nuclear channel (cyto models only)
int chanIndex  = chanDefaultIndex
int chan2Index = chan2DefaultIndex

if (showSettingsDialog) {
    def channelNames = getChannelNames()
    def defaultChanName = pickDefaultChanName(channelNames)
    // Map names -> 1-based indices
    def nameToIndex = [:]
    channelNames.eachWithIndex { nm, i -> nameToIndex[nm] = i + 1 }

    // chan2 choices: include a 'None' (0) - only used for cyto models
    def chan2Choices = ["None"] + channelNames
    def defaultChan2Choice = "None"

    def params = new ParameterList()
        // Models: show only filenames; resolve to File later
        .addChoiceParameter("modelName", "Cellpose model", defaultModelName, modelNames)
        // Channels (following Cellpose CLI: chan = primary, chan2 = nuclear for cyto models)
        .addChoiceParameter("chanName", "Primary channel (chan)", defaultChanName, channelNames)
        .addChoiceParameter("chan2Name", "Nuclear channel for cyto models (chan2)", defaultChan2Choice, chan2Choices)
        // Numeric params
        .addDoubleParameter("diameterUm", "Diameter", diameterUmDefault, "µm", 0.0, 1000.0,
            "Median object diameter in µm (0 = auto for some models)")
        .addDoubleParameter("threshold", "Cellprob threshold", thresholdDefault, "", -10.0, 10.0,
            "Cell Probability threshold (Cellpose default is 0.0)")
        .addDoubleParameter("flowThreshold", "Flow threshold", flowThresholdDefault, "", 0.0, 10.0,
            "Flow threshold (Cellpose default ~0.4)")
        .addDoubleParameter("cellexpansion", "Cell expansion", cellexpansionDefault, "µm", 0.0, 200.0,
            "0 = off; converted to px using image calibration")
        .addIntParameter("tileSize", "Tile size", tileSizeDefault, "px", 256, 8192,
            "Process tiles of this size (GPU: larger can be faster)")
        .addIntParameter("meanfiltersize", "Mean filter radius", meanfiltersizeDefault, "px", 0, 99,
            "0 = off (optional smoothing)")
        .addIntParameter("gaussianfiltersize", "Gaussian filter radius", gaussianfiltersizeDefault, "px", 0, 99,
            "0 = off (optional smoothing)")
        .addBooleanParameter("createAnnotations", "Create annotations (instead of detections)", createAnnotationsDefault,
            "If enabled, creates annotations; ignores cellExpansion")
        .addBooleanParameter("excludeOnBorders", "Exclude detections on borders", excludeOnBordersDefault,
            "If enabled, removes detections touching image borders")

    if (!Dialogs.showParameterDialog("Cellpose parameters", params)) {
        println "Cellpose: cancelled by user."
        return
    }

    selectedModelName  = params.getChoiceParameterValue("modelName")?.toString() ?: defaultModelName

    def chanNameChoice  = params.getChoiceParameterValue("chanName")?.toString() ?: defaultChanName
    def chan2NameChoice = params.getChoiceParameterValue("chan2Name")?.toString() ?: defaultChan2Choice

    chanIndex  = nameToIndex.getOrDefault(chanNameChoice, chanDefaultIndex)
    chan2Index = (chan2NameChoice == "None") ? 0 : nameToIndex.getOrDefault(chan2NameChoice, 0)

    diameterUm         = params.getDoubleParameterValue("diameterUm")
    threshold          = params.getDoubleParameterValue("threshold")
    flowThreshold      = params.getDoubleParameterValue("flowThreshold")
    cellexpansion      = params.getDoubleParameterValue("cellexpansion")
    tileSize           = params.getIntParameterValue("tileSize")
    meanfiltersize     = params.getIntParameterValue("meanfiltersize")
    gaussianfiltersize = params.getIntParameterValue("gaussianfiltersize")
    createAnnotations  = params.getBooleanParameterValue("createAnnotations")
    excludeOnBorders   = params.getBooleanParameterValue("excludeOnBorders")
}

// Resolve model path from selected filename
def modelFile = nameToFile[selectedModelName]
if (modelFile == null || !modelFile.isFile())
    throw new RuntimeException("Selected model not found on disk: " + selectedModelName)
def pathModel = modelFile.absolutePath

// ---------------------------
// Validate parameters
// ---------------------------
if (diameterUm < 0)       throw new IllegalArgumentException("Diameter must be ≥ 0 µm.")
if (tileSize < 256)       throw new IllegalArgumentException("Tile size must be ≥ 256 px.")
if (cellexpansion < 0)    throw new IllegalArgumentException("Cell expansion must be ≥ 0 µm.")
if (chanIndex < 1)        throw new IllegalArgumentException("Primary channel (chan) must be ≥ 1 (1-based).")
if (chan2Index < 0)       throw new IllegalArgumentException("Nuclear channel (chan2) must be ≥ 0 (0 = None).")

// ---------------------------
// Build & run Cellpose
// ---------------------------
try {
    double umPerPx = cal.getAveragedPixelSizeMicrons()
    if (Double.isNaN(umPerPx) || umPerPx <= 0)
        throw new IllegalStateException("Invalid pixel calibration (µm/px): ${umPerPx}")

    double diameterPx      = (diameterUm > 0) ? (diameterUm / umPerPx) : 0.0
    double cellExpansionPx = (cellexpansion > 0) ? (cellexpansion / umPerPx) : 0.0
    def classLabel = (cellexpansion > 0) ? "Cells" : "Nuclei"

    def builder = Cellpose2D
        .builder(pathModel)            // absolute path resolved from selected filename
        .pixelSize(umPerPx)
        .tileSize(tileSize)
        .cellprobThreshold(threshold)
        .flowThreshold(flowThreshold)
        .diameter(diameterPx)
        .cellExpansion(cellExpansionPx)     // 0 => nuclei-only shapes
        .cellConstrainScale(1.5)
        .measureShape()
        .measureIntensity()
        .classify(classLabel)
        .cellposeChannels(chanIndex, chan2Index) // chan: primary (1-based), chan2: nuclear for cyto models (0 = none)

    if (meanfiltersize > 0)
        builder.preprocess(ImageOps.Filters.mean(meanfiltersize))

    // Add Gaussian filter option
    if (gaussianfiltersize > 0)
        builder.preprocess(ImageOps.Filters.gaussianBlur(gaussianfiltersize as double))

    if (createAnnotations)
        builder.createAnnotations()

    def cellpose = builder.build()

    def imageData = getCurrentImageData()

    println "Running Cellpose detection..."
    println "  Model: ${selectedModelName}"
    println "  Channels: chan=${chanIndex} (primary), chan2=${chan2Index} (nuclear for cyto models)"
    println "  Diameter: ${diameterUm} µm  | Expansion: ${cellexpansion} µm  | Class: ${classLabel}"
    cellpose.detectObjects(imageData, pathObjects)
    println "Cellpose detection complete."

    // Remove border detections if requested
    if (excludeOnBorders) {
        def allDetections = getDetectionObjects()
        def toRemove = allDetections.findAll { detection ->
            def roi = detection.getROI()
            if (roi == null) return false
            
            def bounds = roi.getBoundsX() == 0 || 
                        roi.getBoundsY() == 0 || 
                        roi.getBoundsX() + roi.getBoundsWidth() >= server.getWidth() ||
                        roi.getBoundsY() + roi.getBoundsHeight() >= server.getHeight()
            return bounds
        }
        removeObjects(toRemove, true)
        println "Removed ${toRemove.size()} border detections. ${allDetections.size() - toRemove.size()} detections remaining."
    }

} catch (Exception e) {
    Dialogs.showErrorMessage("Cellpose", e.getMessage())
    println "Error during Cellpose execution: ${e.getMessage()}"
    println "Correct the settings or selection and try again."
}
