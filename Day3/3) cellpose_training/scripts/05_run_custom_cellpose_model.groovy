// *************************************************************
//  Run Custom Cellpose Model – Detection within selected parent objects (QuPath 0.6.x)
// 
//  Features:
//  - Model selection from project's models folder (created by training script)
//  - Channel dropdowns (following Cellpose CLI conventions):
//     * chan (required): primary channel to segment (1-based index)
//     * chan2 (optional): nuclear channel for cyto models only (0 = none)
//  - Dynamic classification based on cell expansion setting
//  - All detection parameters configurable via dialog
//************************************************************/

import qupath.ext.biop.cellpose.Cellpose2D
import qupath.lib.gui.dialogs.Dialogs
import qupath.lib.plugins.parameters.ParameterList
import qupath.opencv.ops.ImageOps
import qupath.lib.gui.QuPathGUI

// ---------------------------
// Toggle: show parameters dialog?
// ---------------------------
boolean showSettingsDialog = true

// ---------------------------
// Defaults (used when no dialog)
// ---------------------------
double diameterUmDefault         = 0.0          // 0 = use diameter from training
double thresholdDefault          = 0.0
double flowThresholdDefault      = 0.4
double cellexpansionDefault      = 0.0          // µm; 0 = nuclei only
int    tileSizeDefault           = 4096
boolean createAnnotationsDefault = false
int    meanfiltersizeDefault     = 0
int    gaussianfiltersizeDefault = 0
boolean excludeOnBordersDefault  = true

// Headless channel defaults (1-based; 0 = none for chan2)
int chanDefaultIndex  = 1
int chan2DefaultIndex = 0

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
// Get project path for models folder
// ---------------------------
def project = QuPathGUI.getInstance().getProject()
if (project == null)
    throw new IllegalStateException("No project found. Please create or open a project first.")
def projectPath = project.getPath().getParent()
def projectModelsDir = new File(projectPath.toFile(), "models")

// ---------------------------
// Collect available custom models (project models only)
// ---------------------------
def modelFiles = []

// Check project models folder only
if (projectModelsDir.exists() && projectModelsDir.isDirectory()) {
    def projectModels = projectModelsDir.listFiles()?.findAll { f ->
        f.isFile() && !f.name.startsWith(".")
    } ?: []
    modelFiles.addAll(projectModels)
}

if (modelFiles.isEmpty()) {
    Dialogs.showErrorMessage("Cellpose", 
        "No custom models found in project/models folder.\n\n" +
        "Please train a model first using 04_train_cellpose.groovy")
    return
}

// Build model choices (just filenames, no source indicator needed)
def modelChoices = modelFiles.collect { it.name }.sort { it.toLowerCase() }

def nameToFile = [:]
modelFiles.each { f ->
    nameToFile[f.name] = f
}

def defaultModelChoice = modelChoices.first()

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
    // Default to DAPI if present, otherwise first channel
    int idxDapi = names.findIndexOf { (it ?: "").toLowerCase().contains("dapi") }
    return (idxDapi >= 0) ? names[idxDapi] : names[0]
}

// ---------------------------
// Parameters (dialog or defaults)
// ---------------------------
String selectedModelChoice  = defaultModelChoice
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
        // Model selection
        .addChoiceParameter("modelChoice", "Cellpose model", defaultModelChoice, modelChoices)
        // Channels (following Cellpose CLI: chan = primary, chan2 = nuclear for cyto models)
        .addChoiceParameter("chanName", "Primary channel (chan)", defaultChanName, channelNames)
        .addChoiceParameter("chan2Name", "Nuclear channel for cyto models (chan2)", defaultChan2Choice, chan2Choices)
        // Numeric params
        .addDoubleParameter("diameterUm", "Diameter", diameterUmDefault, "µm", 0.0, 1000.0,
            "Median object diameter in µm (0 = use diameter from training)")
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

    if (!Dialogs.showParameterDialog("Run Custom Cellpose Model", params)) {
        println "Cellpose: cancelled by user."
        return
    }

    selectedModelChoice = params.getChoiceParameterValue("modelChoice")?.toString() ?: defaultModelChoice

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

// Resolve model path from selected choice
def modelFile = nameToFile[selectedModelChoice]
if (modelFile == null || !modelFile.isFile())
    throw new RuntimeException("Selected model not found on disk: " + selectedModelChoice)
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

    double diameterPx      = (diameterUm > 0) ? (diameterUm / umPerPx) : 0.0  // 0 = use training diameter
    double cellExpansionPx = (cellexpansion > 0) ? (cellexpansion / umPerPx) : 0.0
    def classLabel = (cellexpansion > 0) ? "Cells" : "Nuclei"

    def builder = Cellpose2D
        .builder(pathModel)
        .pixelSize(umPerPx)
        .tileSize(tileSize)
        .cellprobThreshold(threshold)
        .flowThreshold(flowThreshold)
        .diameter(diameterPx)
        .cellExpansion(cellExpansionPx)
        .cellConstrainScale(1.5)
        .measureShape()
        .measureIntensity()
        .classify(classLabel)
        .cellposeChannels(chanIndex, chan2Index)

    if (meanfiltersize > 0)
        builder.preprocess(ImageOps.Filters.mean(meanfiltersize))

    if (gaussianfiltersize > 0)
        builder.preprocess(ImageOps.Filters.gaussianBlur(gaussianfiltersize as double))

    if (createAnnotations)
        builder.createAnnotations()

    def cellpose = builder.build()

    def imageData = getCurrentImageData()

    // Get clean model name for display
    def displayModelName = selectedModelChoice
    
    println "Running custom Cellpose model..."
    println "  Model: ${displayModelName}"
    println "  Channels: chan=${chanIndex} (primary), chan2=${chan2Index} (nuclear for cyto models)"
    println "  Diameter: ${diameterUm > 0 ? diameterUm + ' µm' : 'from training'}  | Expansion: ${cellexpansion} µm  | Class: ${classLabel}"
    
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
