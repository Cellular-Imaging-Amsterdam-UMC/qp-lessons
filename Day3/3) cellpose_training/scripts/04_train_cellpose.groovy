// *************************************************************
//  Cellpose 2D Training – Train a custom model (QuPath 0.7.x)
// 
//  Features:
//  - Base model selection: train from scratch ("None") or transfer learn from existing model
//  - Channel selection following Cellpose CLI conventions:
//     * chan (required): primary channel to segment (1-based index)
//     * chan2 (optional): nuclear channel for cyto models (0 = none)
//  - Configurable training parameters: epochs, learning rate, batch size, diameter
//  - Automatic model saving to project folder
//
//  Requirements:
//  - Annotations classified as "Training" for training data
//  - Annotations classified as "Validation" for validation data (optional but recommended)
//************************************************************/

import qupath.ext.biop.cellpose.Cellpose2D
import qupath.lib.gui.dialogs.Dialogs as GuiDialogs
import qupath.lib.plugins.parameters.ParameterList
import qupath.lib.gui.QuPathGUI
import qupath.opencv.ops.ImageOps

// ---------------------------
// Toggle: show parameters dialog?
// ---------------------------
boolean showSettingsDialog = true

// ---------------------------
// Defaults (used when no dialog)
// ---------------------------
String  modelNameDefault      = "my_cellpose_model"
double  diameterUmDefault     = 10.0
int     epochsDefault         = 100
double  learningRateDefault   = 0.2
int     batchSizeDefault      = 8
int     minTrainMasksDefault  = 1
int     meanfiltersizeDefault     = 0
int     gaussianfiltersizeDefault = 0
boolean cleanTrainingDirDefault   = true
boolean saveFlowsDefault          = false

// Headless channel defaults (1-based; 0 = none for chan2)
// chan: primary segmentation channel (1-based index)
// chan2: optional nuclear channel for cyto models (0 = none)
int chanDefaultIndex  = 1
int chan2DefaultIndex = 0

// ---------------------------
// Resolve base models from environment (for transfer learning)
// ---------------------------
def envDir = System.getenv('CELLPOSE_LOCAL_MODELS_PATH')
def baseModelChoices = ["None (train from scratch)"]  // Always include option to train from scratch

if (envDir) {
    def envDirFile = new File(envDir)
    if (envDirFile.isDirectory()) {
        // Accept any file, but exclude cpsam, size_*, and _-*
        def modelFiles = envDirFile.listFiles()?.findAll { f ->
            if (!f.isFile()) return false
            def lower = f.name.toLowerCase()
            def excluded = (lower == 'cpsam') || 
                           (lower == 'cpsam.pb') || 
                           lower.startsWith('size_') ||
                           lower.startsWith('_-')
            return !excluded
        } ?: []
        
        // Add found models to choices
        baseModelChoices.addAll(modelFiles.collect { it.name }.sort { it.toLowerCase() })
    }
}

// Default to "None" (train from scratch)
def defaultBaseModel = baseModelChoices.first()

// ---------------------------
// Server & calibration
// ---------------------------
def server = getCurrentServer()
if (server == null)
    throw new IllegalStateException("No active image/server found.")
def cal = server.getPixelCalibration()
if (cal == null)
    throw new IllegalStateException("No pixel calibration found for the current server.")

// Get project path for model output
def project = QuPathGUI.getInstance().getProject()
if (project == null)
    throw new IllegalStateException("No project found. Please create or open a project first.")
def projectPath = project.getPath().getParent()

// Create models directory in project folder
def modelsDir = new File(projectPath.toFile(), "models")
if (!modelsDir.exists()) {
    modelsDir.mkdirs()
}

// ---------------------------
// Precheck: Validate Training & Validation regions
// ---------------------------
println "Performing pre-training validation..."

int minTrainingRegions   = 6
int minValidationRegions = 2

def projectImages = project.getImageList()
if (projectImages.isEmpty())
    throw new IllegalStateException("No images found in project.")

// Collect all Training and Validation regions across the project
def trainingRegions = []
def validationRegions = []

for (entry in projectImages) {
    def hierarchy = entry.readHierarchy()
    def allAnnotations = hierarchy.getAnnotationObjects()
    
    for (annotation in allAnnotations) {
        def pathClass = annotation.getPathClass()
        if (pathClass == null) continue
        
        def className = pathClass.getName()
        if (className == "Training") {
            trainingRegions << [entry: entry, annotation: annotation, hierarchy: hierarchy]
        } else if (className == "Validation") {
            validationRegions << [entry: entry, annotation: annotation, hierarchy: hierarchy]
        }
    }
}

println "  Found ${trainingRegions.size()} Training regions"
println "  Found ${validationRegions.size()} Validation regions"

// Check minimum counts
def errors = []

if (trainingRegions.size() < minTrainingRegions) {
    errors << "Not enough Training regions: found ${trainingRegions.size()}, need at least ${minTrainingRegions}"
}
if (validationRegions.size() < minValidationRegions) {
    errors << "Not enough Validation regions: found ${validationRegions.size()}, need at least ${minValidationRegions}"
}

// Check that all regions contain at least one child annotation
def allRegions = trainingRegions + validationRegions

for (regionInfo in allRegions) {
    def region = regionInfo.annotation
    def entry = regionInfo.entry
    def hierarchy = regionInfo.hierarchy
    
    // Get all annotations that are contained within this region
    def regionROI = region.getROI()
    def containedAnnotations = hierarchy.getAnnotationObjects().findAll { child ->
        if (child == region) return false  // Skip the region itself
        def childROI = child.getROI()
        if (childROI == null) return false
        // Check if child centroid is inside region
        def centroidX = childROI.getCentroidX()
        def centroidY = childROI.getCentroidY()
        return regionROI.contains(centroidX, centroidY)
    }
    
    def regionClass = region.getPathClass()?.getName() ?: "Unknown"
    def imageName = entry.getImageName()
    
    if (containedAnnotations.isEmpty()) {
        errors << "${regionClass} region in '${imageName}' contains no cell annotations"
    }
}

// Check that all regions are the same size
if (!allRegions.isEmpty()) {
    // Get first region's dimensions as reference
    def firstROI = allRegions[0].annotation.getROI()
    def refWidth = Math.round(firstROI.getBoundsWidth())
    def refHeight = Math.round(firstROI.getBoundsHeight())
    
    def sizeMismatches = []
    for (regionInfo in allRegions) {
        def roi = regionInfo.annotation.getROI()
        def width = Math.round(roi.getBoundsWidth())
        def height = Math.round(roi.getBoundsHeight())
        
        if (width != refWidth || height != refHeight) {
            def regionClass = regionInfo.annotation.getPathClass()?.getName() ?: "Unknown"
            def imageName = regionInfo.entry.getImageName()
            sizeMismatches << "${regionClass} in '${imageName}': ${width}×${height} px"
        }
    }
    
    if (!sizeMismatches.isEmpty()) {
        errors << "Region size mismatch! Expected ${refWidth}×${refHeight} px, but found:\n    - " + sizeMismatches.join("\n    - ")
    }
    
    println "  Reference region size: ${refWidth}×${refHeight} px"
}

// Show errors and abort if any
if (!errors.isEmpty()) {
    def errorMessage = "Pre-training validation failed:\n\n• " + errors.join("\n• ")
    GuiDialogs.showErrorMessage("Training Validation Failed", errorMessage)
    println "VALIDATION FAILED:\n" + errorMessage
    return
}

println "  ✓ All validation checks passed!"

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
    // Default to first channel for training
    return names[0]
}

// ---------------------------
// Parameters (dialog or defaults)
// ---------------------------
String selectedBaseModel = defaultBaseModel
String modelName         = modelNameDefault
double diameterUm        = diameterUmDefault
int    epochs            = epochsDefault
double learningRate      = learningRateDefault
int    batchSize         = batchSizeDefault
int    minTrainMasks     = minTrainMasksDefault
int    meanfiltersize       = meanfiltersizeDefault
int    gaussianfiltersize   = gaussianfiltersizeDefault
boolean cleanTrainingDir    = cleanTrainingDirDefault
boolean saveFlows           = saveFlowsDefault

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
        // Model naming
        .addStringParameter("modelName", "New model name", modelNameDefault,
            "Name for the trained model (saved in project/models folder)")
        // Base model for transfer learning
        .addChoiceParameter("baseModel", "Base model (transfer learning)", defaultBaseModel, baseModelChoices)
        // Channels (following Cellpose CLI: chan = primary, chan2 = nuclear for cyto models)
        .addChoiceParameter("chanName", "Primary channel (chan)", defaultChanName, channelNames)
        .addChoiceParameter("chan2Name", "Nuclear channel for cyto models (chan2)", defaultChan2Choice, chan2Choices)
        // Training parameters
        .addDoubleParameter("diameterUm", "Diameter", diameterUmDefault, "µm", 0.0, 1000.0,
            "Median object diameter in µm (important for training)")
        .addIntParameter("epochs", "Epochs", epochsDefault, "", 1, 2000,
            "Number of training epochs (more = longer training, potentially better results)")
        .addDoubleParameter("learningRate", "Learning rate", learningRateDefault, "", 0.001, 1.0,
            "Learning rate (default 0.2; lower = slower but more stable)")
        .addIntParameter("batchSize", "Batch size", batchSizeDefault, "", 1, 64,
            "Batch size (reduce if running out of GPU memory)")
        .addIntParameter("minTrainMasks", "Minimum training masks", minTrainMasksDefault, "", 1, 100,
            "Minimum number of masks required per training image")
        // Preprocessing options
        .addIntParameter("meanfiltersize", "Mean filter radius", meanfiltersizeDefault, "px", 0, 99,
            "0 = off (optional smoothing before training)")
        .addIntParameter("gaussianfiltersize", "Gaussian filter radius", gaussianfiltersizeDefault, "px", 0, 99,
            "0 = off (optional smoothing before training)")
        // Advanced options
        .addBooleanParameter("cleanTrainingDir", "Clean training directory", cleanTrainingDirDefault,
            "Delete and re-save image patches before training (recommended)")
        .addBooleanParameter("saveFlows", "Save flow fields", saveFlowsDefault,
            "Save flow fields during training (useful for debugging)")

    if (!GuiDialogs.showParameterDialog("Cellpose Training Parameters", params)) {
        println "Cellpose training: cancelled by user."
        return
    }

    selectedBaseModel = params.getChoiceParameterValue("baseModel")?.toString() ?: defaultBaseModel
    modelName         = params.getStringParameterValue("modelName") ?: modelNameDefault

    def chanNameChoice  = params.getChoiceParameterValue("chanName")?.toString() ?: defaultChanName
    def chan2NameChoice = params.getChoiceParameterValue("chan2Name")?.toString() ?: defaultChan2Choice

    chanIndex  = nameToIndex.getOrDefault(chanNameChoice, chanDefaultIndex)
    chan2Index = (chan2NameChoice == "None") ? 0 : nameToIndex.getOrDefault(chan2NameChoice, 0)

    diameterUm    = params.getDoubleParameterValue("diameterUm")
    epochs        = params.getIntParameterValue("epochs")
    learningRate  = params.getDoubleParameterValue("learningRate")
    batchSize     = params.getIntParameterValue("batchSize")
    minTrainMasks = params.getIntParameterValue("minTrainMasks")
    meanfiltersize     = params.getIntParameterValue("meanfiltersize")
    gaussianfiltersize = params.getIntParameterValue("gaussianfiltersize")
    cleanTrainingDir   = params.getBooleanParameterValue("cleanTrainingDir")
    saveFlows          = params.getBooleanParameterValue("saveFlows")
}

// Resolve base model: "None (train from scratch)" -> "None", otherwise use model name
def baseModelForBuilder = selectedBaseModel.startsWith("None") ? "None" : selectedBaseModel

// ---------------------------
// Validate parameters
// ---------------------------
if (modelName == null || modelName.trim().isEmpty())
    throw new IllegalArgumentException("Model name cannot be empty.")
if (diameterUm <= 0)
    throw new IllegalArgumentException("Diameter must be > 0 µm.")
if (epochs < 1)
    throw new IllegalArgumentException("Epochs must be ≥ 1.")
if (learningRate <= 0)
    throw new IllegalArgumentException("Learning rate must be > 0.")
if (batchSize < 1)
    throw new IllegalArgumentException("Batch size must be ≥ 1.")
if (chanIndex < 1)
    throw new IllegalArgumentException("Primary channel (chan) must be ≥ 1 (1-based).")
if (chan2Index < 0)
    throw new IllegalArgumentException("Nuclear channel (chan2) must be ≥ 0 (0 = None).")

// ---------------------------
// Determine final model filename (with numbering if exists)
// ---------------------------
def getUniqueModelName = { File dir, String baseName ->
    def candidate = new File(dir, baseName)
    if (!candidate.exists()) return baseName
    
    // Try numbered versions
    int counter = 1
    while (true) {
        def numberedName = "${baseName}_${counter}"
        candidate = new File(dir, numberedName)
        if (!candidate.exists()) return numberedName
        counter++
        if (counter > 1000) throw new RuntimeException("Too many model versions exist for: ${baseName}")
    }
}

def finalModelName = getUniqueModelName(modelsDir, modelName.trim())

// ---------------------------
// Build & run Cellpose training
// ---------------------------
try {
    double umPerPx = cal.getAveragedPixelSizeMicrons()
    if (Double.isNaN(umPerPx) || umPerPx <= 0)
        throw new IllegalStateException("Invalid pixel calibration (µm/px): ${umPerPx}")

    double diameterPx = diameterUm / umPerPx

    println "Starting Cellpose training..."
    println "  Model name: ${finalModelName}"
    println "  Base model: ${baseModelForBuilder}"
    println "  Channels: chan=${chanIndex} (primary), chan2=${chan2Index} (nuclear for cyto models)"
    println "  Diameter: ${diameterUm} µm (${diameterPx} px)"
    println "  Epochs: ${epochs}, Learning rate: ${learningRate}, Batch size: ${batchSize}"
    println "  Preprocessing: Mean filter=${meanfiltersize}px, Gaussian filter=${gaussianfiltersize}px"
    println "  Model will be saved to: ${modelsDir.absolutePath}"

    def builder = Cellpose2D.builder(baseModelForBuilder)
        .cellposeChannels(chanIndex, chan2Index)
        .pixelSize(umPerPx)
        .diameter(diameterPx)
        .epochs(epochs)
        .learningRate(learningRate)
        .batchSize(batchSize)
        .minTrainMasks(minTrainMasks)
        .modelDirectory(modelsDir)  // Save model to project/models folder

    // Add preprocessing if specified
    if (meanfiltersize > 0)
        builder.preprocess(ImageOps.Filters.mean(meanfiltersize))
    
    if (gaussianfiltersize > 0)
        builder.preprocess(ImageOps.Filters.gaussianBlur(gaussianfiltersize as double))

    // Clean training directory (recommended to avoid stale data)
    if (cleanTrainingDir)
        builder.cleanTrainingDir()

    // Save flow fields if requested
    if (saveFlows)
        builder.addParameter("save_flows")

    def cellpose = builder.build()

    // Train the model
    // The extension looks for annotations classified as "Training" and "Validation"
    def resultModel = cellpose.train()

    println "Training complete!"
    println "Model saved at: ${resultModel.getAbsolutePath()}"

    // Rename the model to the user-specified name
    def finalModelFile = new File(modelsDir, finalModelName)
    if (resultModel.exists() && resultModel.renameTo(finalModelFile)) {
        println "Model renamed to: ${finalModelFile.absolutePath}"
    } else if (resultModel.exists()) {
        // If rename fails, try copying
        finalModelFile.bytes = resultModel.bytes
        resultModel.delete()
        println "Model copied to: ${finalModelFile.absolutePath}"
    }

    // Show training results
    def results = cellpose.getTrainingResults()
    results.show("Training Results - ${finalModelName}")

    // Show QC results if available
    try {
        def qcResults = cellpose.getQCResults()
        if (qcResults != null && qcResults.size() > 0) {
            qcResults.show("QC Results - ${finalModelName}")
        }
    } catch (Exception qcEx) {
        println "QC results not available: ${qcEx.getMessage()}"
    }

    // Show training graph
    cellpose.showTrainingGraph()

    // Show success message
    GuiDialogs.showInfoNotification("Cellpose Training",
        "Training complete!\nModel saved as: ${finalModelName}\nLocation: ${modelsDir.absolutePath}")

} catch (Exception e) {
    GuiDialogs.showErrorMessage("Cellpose Training", e.getMessage())
    println "Error during Cellpose training: ${e.getMessage()}"
    e.printStackTrace()
}
