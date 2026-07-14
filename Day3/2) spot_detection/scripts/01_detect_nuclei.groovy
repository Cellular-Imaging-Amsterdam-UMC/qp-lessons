/*************************************************************
 * StarDist 2D – Nuclei detection within selected parent objects (QuPath 0.7.x)
 *
 * PURPOSE
 * - Run StarDist 2D nucleus detection inside *selected* parent objects.
 * - Uses a local TensorFlow (.pb) model from the directory defined by
 *   the environment variable: STARDIST_LOCAL_MODELS_PATH.
 *
 * MODES
 * - Set 'showSettingsDialog' (below) to control how parameters are provided:
 *     false = run headless using the default user parameters in this script.
 *     true  = show a dialog to set parameters interactively (model path is NOT asked).
 *
 * REQUIREMENTS
 * - QuPath 0.7.x + qupath-extension-stardist.
 * - Environment variable STARDIST_LOCAL_MODELS_PATH must point to a folder
 *   containing StarDist .pb models.
 * - OpenCV extension enabled if you use the mean filter preprocessing step.
 *
 * OUTPUT
 * - Creates detection objects (nuclei and cells if cellexpansion > 0) OR annotation objects.
 * - Adds intensity & shape measurements + probability.
 * - Classifies created objects as:
 *     * "Nuclei" when cellexpansion == 0
 *     * "Cells"  when cellexpansion  > 0
 *************************************************************/

import qupath.ext.stardist.StarDist2D
import qupath.lib.gui.dialogs.Dialogs as GuiDialogs
import qupath.lib.plugins.parameters.ParameterList
import qupath.opencv.ops.ImageOps

// ---------------------------
// Toggle: show parameters dialog?
// ---------------------------
boolean showSettingsDialog = true

// ---------------------------
// Fixed model selection (NOT asked in dialog)
// ---------------------------
def preferredModelName = 'dsb2018_heavy_augment.pb' // Expected inside STARDIST_LOCAL_MODELS_PATH

// ---------------------------
// Default user parameters (used when showSettingsDialog=false;
// also used as initial values when the dialog is shown)
// ---------------------------
def    channelDefault           = 'DAPI' // Headless default; dialog uses dropdown populated from image
double downscaleDefault         = 1.0       // >=1; larger = faster & coarser
double thresholdDefault         = 0.50      // 0..1
int    meanfiltersizeDefault    = 2         // pixels; 0 = off
int    gaussianfiltersizeDefault = 0        // pixels; 0 = off
boolean excludeOnBordersDefault = true     // true = remove detections touching image borders

// ---------------------------
// Resolve model path from environment
// ---------------------------
def envDir = System.getenv('STARDIST_LOCAL_MODELS_PATH')
if (!envDir)
    throw new RuntimeException("Environment variable STARDIST_LOCAL_MODELS_PATH is not set.")

def envDirFile = new File(envDir)
if (!envDirFile.isDirectory())
    throw new RuntimeException("STARDIST_LOCAL_MODELS_PATH does not point to a valid folder: " + envDir)

def modelFile = new File(envDirFile, preferredModelName)
if (!modelFile.exists())
    throw new RuntimeException("Model file not found in STARDIST_LOCAL_MODELS_PATH: " + modelFile.absolutePath)

println "Using StarDist model: ${modelFile.absolutePath}"
def pathModel = modelFile.absolutePath

// ---------------------------
/** Helpers **/
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
    GuiDialogs.showErrorMessage("StarDist", "Please select at least one parent object (e.g., ROI or annotation).")
    return
}

/**
 * Gather channel names from current image; pick DAPI (if present) as default, otherwise first.
 * Returns [List<String> channelNames, String defaultName]
 */
def getChannelNamesAndDefault = {
    def md = server.getMetadata()
    def channels = md?.getChannels()
    def names = (channels != null && !channels.isEmpty()) ?
        channels.collect { it?.getName() ?: "" } : []

    if (names.isEmpty()) {
        // Fallback: generic names if metadata lacks channel objects
        int sizeC = md?.getSizeC() ?: 1
        names = (0..<sizeC).collect { "Channel ${it+1}" }
    }

    int idxDapi = names.findIndexOf { (it ?: "").toLowerCase().contains("dapi") }
    def defName = (idxDapi >= 0) ? names[idxDapi] : names[0]
    return [names, defName]
}

// Helper: parse channel string to int if possible, else keep as string (used for headless)
def parseChannel(Object c) {
    if (c == null) return 'Nucleus'
    def s = c.toString().trim()
    try {
        return Integer.parseInt(s)
    } catch (Exception ignore) {
        return s
    }
}

// ---------------------------
// Acquire parameters (dialog or defaults)
// ---------------------------
def channel            = channelDefault
double downscale       = downscaleDefault
double threshold       = thresholdDefault
int    meanfiltersize  = meanfiltersizeDefault
int    gaussianfiltersize = gaussianfiltersizeDefault
boolean excludeOnBorders = excludeOnBordersDefault

if (showSettingsDialog) {
    def (channelNames, defaultChannelName) = getChannelNamesAndDefault()

    def params = new ParameterList()
        // Dropdown (choice) with channel names from current image
        .addChoiceParameter("channel", "Channel", defaultChannelName, channelNames)
        // bounded overloads require a unit string before bounds
        .addDoubleParameter("downscale", "Downscale (>0)", downscaleDefault, "", 1, 100.0,
            "Scale factor for detection resolution; larger = faster/coarser")
        .addDoubleParameter("threshold", "Probability Threshold [0..1]", thresholdDefault, "", 0.0, 1.0,
            "Probability threshold for detections")
        .addIntParameter("meanfiltersize", "Mean filter radius", meanfiltersizeDefault, "px", 0, 99,
            "0 = off")
        .addIntParameter("gaussianfiltersize", "Gaussian filter radius", gaussianfiltersizeDefault, "px", 0, 99,
            "0 = off")
        .addBooleanParameter("excludeOnBorders", "Exclude detections on borders", excludeOnBordersDefault,
            "If enabled, removes detections touching image borders")

    if (!GuiDialogs.showParameterDialog("StarDist parameters", params)) {
        println "StarDist: cancelled by user."
        return
    }

    // For choice parameters, use getChoiceParameterValue; fall back to toString just in case
    def chosen = params.getChoiceParameterValue("channel")
    channel            = (chosen != null) ? chosen.toString() : defaultChannelName
    downscale          = params.getDoubleParameterValue("downscale")
    threshold          = params.getDoubleParameterValue("threshold")
    meanfiltersize     = params.getIntParameterValue("meanfiltersize")
    gaussianfiltersize = params.getIntParameterValue("gaussianfiltersize")
    excludeOnBorders   = params.getBooleanParameterValue("excludeOnBorders")
}

// ---------------------------
// Validate parameters
// ---------------------------
if (downscale < 1)
    throw new IllegalArgumentException("Parameter 'downscale' must be >= 1 (current value: ${downscale}).")
if (threshold < 0 || threshold > 1)
    throw new IllegalArgumentException("Parameter 'threshold' must be within [0..1] (current value: ${threshold}).")
if (meanfiltersize < 0)
    throw new IllegalArgumentException("Parameter 'meanfiltersize' must be ≥ 0 (current value: ${meanfiltersize}).")
if (gaussianfiltersize < 0)
    throw new IllegalArgumentException("Parameter 'gaussianfiltersize' must be ≥ 0 (current value: ${gaussianfiltersize}).")
def classLabel = "Nuclei"

// ---------------------------
// Remove existing nuclei annotations to avoid duplicates
// ---------------------------
def existingNuclei = getAnnotationObjects().findAll { it.getPathClass()?.toString() == classLabel }
if (!existingNuclei.isEmpty()) {
    println "Removing ${existingNuclei.size()} existing nuclei annotations..."
    removeObjects(existingNuclei, true)
}

// ---------------------------
// Build & run StarDist
// ---------------------------
try {
    double umPerPx = cal.getAveragedPixelSizeMicrons()
    if (Double.isNaN(umPerPx) || umPerPx <= 0)
        throw new IllegalStateException("Invalid pixel calibration (µm/px): ${umPerPx}")

    double targetUmPerPx   = umPerPx * downscale
    double cellExpansionPx = 0.0

    def builder = StarDist2D
        .builder(pathModel)
        .threshold(threshold)
        .channels(parseChannel(channel))   // accepts either int index or string name
        .normalizePercentiles(1, 99)
        .pixelSize(targetUmPerPx)
//        .cellConstrainScale(1.5)
        .measureShape()
        .measureIntensity()
        .includeProbability(true)
        .doLog()
        .classify(classLabel)
        .createAnnotations()
    // Optional advanced options:
        // .tileSize(1024)
        // .nThreads(4)
        // .simplify(1.0)
        // .ignoreCellOverlaps(false)
        // .constrainToParent(false)
    
    if (meanfiltersize > 0)
        builder.preprocess(ImageOps.Filters.mean(meanfiltersize))

    // Add Gaussian filter option
    if (gaussianfiltersize > 0)
        builder.preprocess(ImageOps.Filters.gaussianBlur(gaussianfiltersize as double))

    def stardist = builder.build()

    def imageData = getCurrentImageData()

    println "Running StarDist detection (class: ${classLabel}, channel: ${channel})..."
    stardist.detectObjects(imageData, pathObjects)
    println "StarDist detection complete."

    // Remove border detections if requested
    if (excludeOnBorders) {
        def annotationObjects = getAnnotationObjects().findAll { it.getPathClass()?.toString() == classLabel }
        def toRemove = annotationObjects.findAll { detection ->
            def roi = detection.getROI()
            if (roi == null) return false
            
            def bounds = roi.getBoundsX() == 0 || 
                        roi.getBoundsY() == 0 || 
                        roi.getBoundsX() + roi.getBoundsWidth() >= server.getWidth() ||
                        roi.getBoundsY() + roi.getBoundsHeight() >= server.getHeight()
            return bounds
        }
        removeObjects(toRemove, true)
        println "Removed ${toRemove.size()} border annotations."
    }

} catch (Exception e) {
    GuiDialogs.showErrorMessage("StarDist", e.getMessage())
    println "Error during StarDist execution: ${e.getMessage()}"
    println "Correct the settings or selection and try again."
}
