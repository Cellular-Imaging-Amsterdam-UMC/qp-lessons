/*************************************************************
 * Combined StarDist + Spotiflow – Nuclei & Foci Detection (QuPath 0.7.x)
 *
 * PURPOSE
 * - Run StarDist to detect nuclei as annotations within selected parent objects.
 * - Run Spotiflow to detect foci (spots) in a separate channel.
 * - Count foci per nucleus annotation.
 *
 * REQUIREMENTS
 * - QuPath 0.7.x + qupath-extension-stardist + qupath-extension-biop-spotiflow
 * - Environment variable STARDIST_LOCAL_MODELS_PATH must point to a folder
 *   containing StarDist .pb models.
 *
 * OUTPUT
 * - Creates annotation objects for nuclei (StarDist)
 * - Creates point detections for foci (Spotiflow)
 * - Adds measurements: "Foci: Count (nucleus)"
 *************************************************************/

import qupath.ext.stardist.StarDist2D
import qupath.lib.gui.dialogs.Dialogs as GuiDialogs
import qupath.lib.plugins.parameters.ParameterList
import qupath.lib.objects.PathObjects
import qupath.lib.objects.classes.PathClass
import qupath.lib.regions.RegionRequest
import qupath.lib.regions.ImagePlane
import qupath.lib.images.writers.TileExporter
import qupath.lib.roi.ROIs
import qupath.opencv.ops.ImageOps
import java.io.FilenameFilter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

// ---------------------------
// Toggle: show parameters dialog?
// ---------------------------
boolean showSettingsDialog = true

// ---------------------------
// Fixed model selection
// ---------------------------
def preferredModelName = 'dsb2018_heavy_augment.pb'

// ---------------------------
// Default parameters - StarDist
// ---------------------------
def    nucleusChannelDefault    = 'DAPI'
double downscaleDefault         = 1.0
double thresholdDefault         = 0.50
int    meanfiltersizeDefault    = 0
int    gaussianfiltersizeDefault = 0
boolean excludeOnBordersDefault = true
def    annotationClassName      = 'Nuclei'

// ---------------------------
// Default parameters - Spotiflow
// ---------------------------
def    fociChannelDefault       = 'GFP'
def    fociClassNameDefault     = 'Foci'
def    fociDeviceDefault        = 'auto'
def    spotiflowModelDefault    = 'general'
def    spotiflowModelChoices    = ['general', 'hybiss', 'synth_complex', 'fluo_live', 'synth_3d', 'smfish_3d']
def    spotiflowPeakModeDefault = 'fast'
def    spotiflowPeakModeChoices = ['fast', 'skimage']

def parseOptionalDouble = { String label, String value ->
    def trimmed = value?.trim()
    if (!trimmed)
        return null
    try {
        return Double.parseDouble(trimmed)
    } catch (NumberFormatException ex) {
        throw new IllegalArgumentException("Invalid ${label} value: '${value}'")
    }
}

// ---------------------------
// Server & calibration
// ---------------------------
def server = getCurrentServer()
if (server == null)
    throw new IllegalStateException("No active image/server found.")

def cal = server.getPixelCalibration()
if (cal == null)
    throw new IllegalStateException("No pixel calibration found.")

def imageData = getCurrentImageData()
if (imageData == null) {
    GuiDialogs.showErrorMessage("Script", "No image open.")
    return
}

// ---------------------------
// Resolve StarDist model from environment
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
// Ensure parent objects exist
// ---------------------------
def parents = getSelectedObjects()
if (parents.isEmpty()) {
    // Create full-image annotation as parent
    def roi = ROIs.createRectangleROI(0, 0, server.getWidth(), server.getHeight(), null)
    def whole = PathObjects.createAnnotationObject(roi)
    addObject(whole)
    parents = [whole]
    println "No selection found; using full image as parent."
}

// ---------------------------
// Get channel names for dropdowns
// ---------------------------
def getChannelNamesAndDefault = {
    def md = server.getMetadata()
    def channels = md?.getChannels()
    def names = (channels != null && !channels.isEmpty()) ?
        channels.collect { it?.getName() ?: "" } : []

    if (names.isEmpty()) {
        int sizeC = md?.getSizeC() ?: 1
        names = (0..<sizeC).collect { "Channel ${it+1}" }
    }

    int idxDapi = names.findIndexOf { (it ?: "").toLowerCase().contains("dapi") }
    def defName = (idxDapi >= 0) ? names[idxDapi] : names[0]
    return [names, defName]
}

def pickDefaultFociChannel = { List<String> names ->
    def idx = names.findIndexOf { 
        def lower = (it ?: "").toLowerCase()
        lower.contains("gfp") || lower.contains("fitc") || lower.contains("green") || lower.contains("foci")
    }
    return (idx >= 0) ? names[idx] : (names.size() > 1 ? names[1] : names[0])
}

// ---------------------------
// Acquire parameters
// ---------------------------
def nucleusChannel    = nucleusChannelDefault
double downscale      = downscaleDefault
double threshold      = thresholdDefault
int meanfiltersize    = meanfiltersizeDefault
int gaussianfiltersize = gaussianfiltersizeDefault
boolean excludeOnBorders = excludeOnBordersDefault
def fociChannel       = fociChannelDefault
def fociClassName     = fociClassNameDefault
def fociDevice        = fociDeviceDefault
def spotiflowModel    = spotiflowModelDefault
Double spotiflowProbThresh = null
String spotiflowPeakMode = spotiflowPeakModeDefault

if (showSettingsDialog) {
    def (channelNames, defaultNucleusChannel) = getChannelNamesAndDefault()
    def defaultFociChannel = pickDefaultFociChannel(channelNames)

    def params = new ParameterList()
        // StarDist parameters
        .addTitleParameter("─────── StarDist (Nuclei) ───────")
        .addChoiceParameter("nucleusChannel", "Nucleus channel", defaultNucleusChannel, channelNames)
        .addDoubleParameter("downscale", "Downscale (≥1)", downscaleDefault, "", 1.0, 10.0,
            "Scale factor; larger = faster but coarser")
        .addDoubleParameter("threshold", "Probability threshold [0..1]", thresholdDefault, "", 0.0, 1.0,
            "Detection confidence threshold")
        .addIntParameter("meanfiltersize", "Mean filter radius", meanfiltersizeDefault, "px", 0, 99,
            "Preprocessing smoothing; 0 = off")
        .addIntParameter("gaussianfiltersize", "Gaussian filter radius", gaussianfiltersizeDefault, "px", 0, 99,
            "Preprocessing Gaussian blur; 0 = off")
        .addBooleanParameter("excludeOnBorders", "Exclude detections on borders", excludeOnBordersDefault,
            "Remove objects touching image edges")
        // Spotiflow parameters
        .addTitleParameter("─────── Spotiflow (Foci) ───────")
        .addChoiceParameter("fociChannel", "Foci channel", defaultFociChannel, channelNames)
        .addStringParameter("fociClassName", "Foci class name", fociClassNameDefault,
            "Classification name for detected foci")
        .addChoiceParameter("fociDevice", "Spotiflow device", fociDeviceDefault, ["auto", "cuda", "cpu", "mps"])
        .addChoiceParameter("spotiflowModel", "Spotiflow model", spotiflowModelDefault, spotiflowModelChoices)
        .addStringParameter("spotiflowProbThresh", "Probability threshold", "",
            "Leave blank to use model defaults.")
        .addChoiceParameter("spotiflowPeakMode", "Peak detection", spotiflowPeakModeDefault, spotiflowPeakModeChoices)

    if (!GuiDialogs.showParameterDialog("StarDist + Spotiflow Parameters", params)) {
        println "Cancelled by user."
        return
    }

    nucleusChannel     = params.getChoiceParameterValue("nucleusChannel")?.toString() ?: defaultNucleusChannel
    downscale          = params.getDoubleParameterValue("downscale")
    threshold          = params.getDoubleParameterValue("threshold")
    meanfiltersize     = params.getIntParameterValue("meanfiltersize")
    gaussianfiltersize = params.getIntParameterValue("gaussianfiltersize")
    excludeOnBorders   = params.getBooleanParameterValue("excludeOnBorders")
    fociChannel        = params.getChoiceParameterValue("fociChannel")?.toString() ?: defaultFociChannel
    fociClassName      = params.getStringParameterValue("fociClassName") ?: fociClassNameDefault
    fociDevice         = params.getChoiceParameterValue("fociDevice")?.toString() ?: fociDeviceDefault
    spotiflowModel     = params.getChoiceParameterValue("spotiflowModel")?.toString() ?: spotiflowModelDefault
    spotiflowPeakMode  = params.getChoiceParameterValue("spotiflowPeakMode")?.toString() ?: spotiflowPeakModeDefault

    def probThreshText = params.getStringParameterValue("spotiflowProbThresh")

    try {
        spotiflowProbThresh = parseOptionalDouble("probability threshold", probThreshText)
    } catch (IllegalArgumentException parseErr) {
        GuiDialogs.showErrorMessage("Spotiflow", parseErr.getMessage())
        return
    }
}

// ---------------------------
// Validate parameters
// ---------------------------
if (downscale < 1)
    throw new IllegalArgumentException("Downscale must be >= 1")
if (threshold < 0 || threshold > 1)
    throw new IllegalArgumentException("Threshold must be within [0..1]")

// ---------------------------
// Helper: check if ROI touches image boundary
// ---------------------------
def touchingBoundary = { roi ->
    if (roi == null) return false
    def bx = roi.getBoundsX(), by = roi.getBoundsY()
    def bw = roi.getBoundsWidth(), bh = roi.getBoundsHeight()
    return bx <= 0 || by <= 0 || bx + bw >= server.getWidth() - 1 || by + bh >= server.getHeight() - 1
}

// Helpers for python-based Spotiflow prediction
def safeName = { String s -> (s ?: "image").replaceAll('[\\/:*?"<>|]', '_') }

def listTifs = { File dir ->
    (dir.listFiles({ d, n -> n.toLowerCase().endsWith('.tif') } as FilenameFilter)?.toList() ?: [])
}

def moveOrCopy = { File src, File dst ->
    if (!src.exists())
        throw new IOException("Source file missing: ${src}")
    if (src.absolutePath.equalsIgnoreCase(dst.absolutePath))
        return
    try {
        Files.move(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING)
    } catch (Throwable moveErr) {
        Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING)
        src.delete()
    }
}

def deleteRecursive
deleteRecursive = { File f ->
    if (f == null || !f.exists())
        return
    if (f.isDirectory())
        f.listFiles()?.each { deleteRecursive(it) }
    f.delete()
}

def getSpotiflowPythonExe = {
    def setupClass = Class.forName("qupath.ext.biop.spotiflow.SpotiflowSetup")
    def setup = setupClass.getMethod("getInstance").invoke(null)
    def pythonPath = setupClass.getMethod("getSpotiflowPythonPath").invoke(setup)?.toString()
    if (!pythonPath)
        throw new IllegalStateException("Spotiflow python path is not configured. Set it via Extensions > Spotiflow settings.")
    def exe = new File(pythonPath.trim())
    if (!exe.exists())
        throw new IllegalStateException("Configured Spotiflow python interpreter does not exist: ${exe.absolutePath}")
    return exe
}

def exportNucleiPatches = { File datasetDir, List nucleiObjs, String channelName ->
    def records = []
    nucleiObjs.eachWithIndex { nucleus, idx ->
        def roi = nucleus.getROI()
        if (roi == null)
            return
        def plane = roi.getImagePlane()
        int bx = Math.max(0, Math.floor(roi.getBoundsX()) as int)
        int by = Math.max(0, Math.floor(roi.getBoundsY()) as int)
        int bw = Math.max(1, Math.ceil(roi.getBoundsWidth()) as int)
        int bh = Math.max(1, Math.ceil(roi.getBoundsHeight()) as int)
        def region = RegionRequest.createInstance(server.getPath(), 1.0, bx, by, bw, bh,
                plane?.getZ() ?: 0, plane?.getT() ?: 0)

        def before = listTifs(datasetDir).collect { it.absolutePath } as Set
        new TileExporter(imageData)
                .region(region)
                .channels(channelName)
                .tileSize(bw, bh)
                .overlap(0)
                .includePartialTiles(true)
                .imageExtension(".tif")
                .writeTiles(datasetDir.absolutePath)

        def created = listTifs(datasetDir).findAll { !before.contains(it.absolutePath) }
        if (created.isEmpty()) {
            println "WARN: Failed to export patch for nucleus ${idx + 1}."
            return
        }
        def chosen = created.max { it.lastModified() }
        def baseName = String.format("nucleus_%04d_x%d_y%d_w%d_h%d", idx + 1, bx, by, bw, bh)
        def renamed = new File(datasetDir, baseName + ".tif")
        moveOrCopy(chosen, renamed)
        records << [annotation: nucleus, baseName: baseName, boundsX: bx as double, boundsY: by as double, plane: plane]
    }
    return records
}

def runSpotiflowPredictPython = { File pythonExe, File datasetDir, Map cfg ->
    def cmd = [pythonExe.absolutePath, "-m", "spotiflow.cli.predict", datasetDir.absolutePath,
               "--out-dir", datasetDir.absolutePath, "--device", (cfg?.device ?: "auto"), "--verbose"]
    def modelName = cfg?.model ?: spotiflowModelDefault
    cmd << "--pretrained-model"
    cmd << modelName

    def addArg = { String flag, def value ->
        if (value != null)
            cmd << flag << value.toString()
    }

    addArg("--probability-threshold", cfg?.probThresh)
    addArg("--peak-mode", cfg?.peakMode)

    println "Executing Spotiflow python command:\n${cmd.join(' ')}"
    def pb = new ProcessBuilder(cmd)
    pb.redirectErrorStream(true)
    pb.environment().put("PYTHONIOENCODING", "utf-8")
    pb.environment().put("PYTHONUTF8", "1")
    def process = pb.start()
    process.inputStream.withReader { reader -> reader.eachLine { println "SPOTIFLOW> ${it}" } }
    int exitCode = process.waitFor()
    if (exitCode != 0)
        throw new IllegalStateException("Spotiflow python process exited with code ${exitCode}")
}

def loadSpotiflowDetections = { List records, File datasetDir ->
    def detectionsByAnnotation = [:].withDefault { [] }
    records.each { rec ->
        def csvFile = new File(datasetDir, rec.baseName + ".csv")
        if (!csvFile.exists())
            return
        csvFile.withReader { reader ->
            int lineIdx = 0
            reader.eachLine { line ->
                lineIdx++
                def trimmed = line?.trim()
                if (!trimmed)
                    return
                if (lineIdx == 1 && trimmed.toLowerCase().contains("y"))
                    return
                def parts = trimmed.split(/[;,\s]+/)
                if (parts.length < 2)
                    return
                try {
                    double relY = Double.parseDouble(parts[0])
                    double relX = Double.parseDouble(parts[1])
                    double globalX = rec.boundsX + relX
                    double globalY = rec.boundsY + relY
                    detectionsByAnnotation[rec.annotation] << [x: globalX, y: globalY, plane: rec.plane]
                } catch (NumberFormatException ignore) {}
            }
        }
    }
    return detectionsByAnnotation
}

// ---------------------------
// Remove existing nuclei and foci to avoid duplicates
// ---------------------------
def existingNuclei = getAnnotationObjects().findAll { it.getPathClass()?.toString() == annotationClassName }
if (!existingNuclei.isEmpty()) {
    println "Removing ${existingNuclei.size()} existing nuclei annotations..."
    removeObjects(existingNuclei, true)
}
def existingFociPre = getDetectionObjects().findAll { it.getPathClass()?.toString() == fociClassNameDefault }
if (!existingFociPre.isEmpty()) {
    println "Removing ${existingFociPre.size()} existing foci detections..."
    removeObjects(existingFociPre, true)
}

// ---------------------------
// 1) Run StarDist
// ---------------------------
println "\n========== Running StarDist =========="
try {
    double umPerPx = cal.getAveragedPixelSizeMicrons()
    if (!(umPerPx > 0))
        throw new IllegalStateException("Invalid µm/px: ${umPerPx}")

    double targetUmPerPx   = umPerPx * downscale

    def builder = StarDist2D.builder(pathModel)
            .threshold(threshold)
            .channels(nucleusChannel)
            .normalizePercentiles(1, 99)
            .pixelSize(targetUmPerPx)
            .measureShape()
            .measureIntensity()
            .includeProbability(true)
            .classify(annotationClassName)
            .doLog()
            .createAnnotations()

    if (meanfiltersize > 0)
        builder.preprocess(ImageOps.Filters.mean(meanfiltersize))
    if (gaussianfiltersize > 0)
        builder.preprocess(ImageOps.Filters.gaussianBlur(gaussianfiltersize as double))

    def stardist = builder.build()
    stardist.detectObjects(imageData, parents)

    if (excludeOnBorders) {
        def rm = getAnnotationObjects().findAll {
            it.getPathClass()?.toString() == annotationClassName && touchingBoundary(it.getROI())
        }
        if (!rm.isEmpty()) {
            removeObjects(rm, true)
            println "Removed ${rm.size()} border annotations."
        }
    }
    def createdAnnotations = getAnnotationObjects().findAll { it.getPathClass()?.toString() == annotationClassName }
    println "StarDist complete: created ${createdAnnotations.size()} nucleus annotations."

} catch (Exception e) {
    GuiDialogs.showErrorMessage("StarDist", e.getMessage())
    println "Error during StarDist: ${e.getMessage()}"
    return
}

// ---------------------------
// 2) Run Spotiflow
// ---------------------------
def nucleiAnnotations = getAnnotationObjects().findAll { it.getPathClass()?.toString() == annotationClassName }
if (nucleiAnnotations.isEmpty()) {
    GuiDialogs.showErrorMessage("Spotiflow", "No nucleus annotations found; cannot run Spotiflow.")
    return
}
def spotiflowRunConfig = [
    device     : fociDevice,
    model      : spotiflowModel,
    probThresh : spotiflowProbThresh,
    peakMode   : spotiflowPeakMode
]
println "\n========== Preparing Spotiflow dataset =========="
def tempRoot = Files.createTempDirectory("spotiflow-predict").toFile()
def imgLabel = getProjectEntry()?.getImageName() ?: (server.getMetadata()?.getName() ?: "image")
def datasetDir = new File(tempRoot, safeName(imgLabel) + "_patches")
datasetDir.mkdirs()

def exportRecords = exportNucleiPatches(datasetDir, nucleiAnnotations, fociChannel)
if (exportRecords.isEmpty()) {
    deleteRecursive(tempRoot)
    GuiDialogs.showErrorMessage("Spotiflow", "Failed to export nucleus patches for Spotiflow.")
    return
}
println "Exported ${exportRecords.size()} nucleus patches for Spotiflow."

println "\n========== Running Spotiflow =========="
try {
    def pythonExe = getSpotiflowPythonExe()
    runSpotiflowPredictPython(pythonExe, datasetDir, spotiflowRunConfig)
    println "Spotiflow python command finished successfully."
} catch (Exception e) {
    deleteRecursive(tempRoot)
    GuiDialogs.showErrorMessage("Spotiflow", e.getMessage())
    println "Error during Spotiflow: ${e.getMessage()}"
    return
}

println "\n========== Importing detections =========="
def detectionsByAnnotation = loadSpotiflowDetections(exportRecords, datasetDir)
def fociPathClass = PathClass.fromString(fociClassName)
def existingFoci = getDetectionObjects().findAll { it.getPathClass()?.toString() == fociClassName }
if (!existingFoci.isEmpty())
    removeObjects(existingFoci, true)

int totalDetections = 0
detectionsByAnnotation.each { nucleus, points ->
    points.each { pt ->
        def plane = pt.plane ?: ImagePlane.getPlane(0, 0)
        def roiPoint = ROIs.createPointsROI(pt.x, pt.y, plane)
        def detection = PathObjects.createDetectionObject(roiPoint, fociPathClass)
        nucleus.addChildObject(detection)
        totalDetections++
    }
}

deleteRecursive(tempRoot)
println "Imported ${totalDetections} foci detections."
fireHierarchyUpdate()

// ---------------------------
// 3) Count foci per nucleus annotation
// ---------------------------
println "\n========== Counting Foci per Nucleus =========="

def foci = getDetectionObjects().findAll { it.getPathClass()?.toString() == fociClassName }
def MEAS_NUC  = "Foci: Count (nucleus)"

int touched = 0
nucleiAnnotations.each { obj ->
    def roi = obj.getROI()
    if (roi == null) return

    int countNuc = 0
    foci.each { sp ->
        def r = sp.getROI()
        if (r == null) return
        double cx = r.getCentroidX(), cy = r.getCentroidY()
        if (roi.contains(cx, cy))
            countNuc++
    }

    obj.getMeasurements().put(MEAS_NUC, (double)countNuc)
    touched++
}

fireHierarchyUpdate()

println "\n========== Done =========="
println "Added foci measurements to ${touched} nucleus annotations."
