// *************************************************************
//  Cellpose 2D – Detection within selected parent objects (QuPath 0.6.x)
// 
//  Features:
//  - Model dropdown from CELLPOSE_LOCAL_MODELS_PATH (filenames only)
//    Excludes: 'cpsam'/'cpsam.pb' and any files starting with 'size_' or '_-'.
//  - Channel dropdowns:
//     * Nuclei (required): defaults to DAPI if present, else first channel
//     * Cytoplasm (optional): 'None' (0) by default
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
int nucleiChannelDefaultIndex = 1
int cytoChannelDefaultIndex   = 0

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

def pickDefaultNucleiName = { List<String> names ->
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
int nucleiChanIndex = nucleiChannelDefaultIndex
int cytoChanIndex   = cytoChannelDefaultIndex

if (showSettingsDialog) {
    def channelNames = getChannelNames()
    def defaultNucName = pickDefaultNucleiName(channelNames)
    // Map names -> 1-based indices
    def nameToIndex = [:]
    channelNames.eachWithIndex { nm, i -> nameToIndex[nm] = i + 1 }

    // Cytoplasm choices: include a 'None' (0)
    def cytoChoices = ["None"] + channelNames
    def defaultCytoChoice = "None"

    def params = new ParameterList()
        // Models: show only filenames; resolve to File later
        .addChoiceParameter("modelName", "Cellpose model", defaultModelName, modelNames)
        // Channels
        .addChoiceParameter("nucChannelName", "Nuclei channel (chan)", defaultNucName, channelNames)
        .addChoiceParameter("cytoChannelName", "Cytoplasm channel (chan2)", defaultCytoChoice, cytoChoices)
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

    def nucName        = params.getChoiceParameterValue("nucChannelName")?.toString() ?: defaultNucName
    def cytoNameChoice = params.getChoiceParameterValue("cytoChannelName")?.toString() ?: defaultCytoChoice

    nucleiChanIndex    = nameToIndex.getOrDefault(nucName, nucleiChannelDefaultIndex)
    cytoChanIndex      = (cytoNameChoice == "None") ? 0 : nameToIndex.getOrDefault(cytoNameChoice, 0)

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
if (nucleiChanIndex < 1)  throw new IllegalArgumentException("Nuclei channel must be ≥ 1 (1-based).")
if (cytoChanIndex < 0)    throw new IllegalArgumentException("Cytoplasm channel must be ≥ 0 (0 = None).")

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
        .cellposeChannels(nucleiChanIndex, cytoChanIndex) // 1-based; 0 = none for chan2

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
    println "  Channels: chan=${nucleiChanIndex}, chan2=${cytoChanIndex}"
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
