/*************************************************************
 * Spotiflow Foci Detection – Run on existing nuclei annotations (QuPath 0.7.x)
 *
 * PURPOSE
 * - Detect foci (spots) inside already detected nuclei annotations using Spotiflow.
 * - Run this AFTER 01_detect_nuclei.groovy to detect foci within nuclei.
 * - Counts foci per nucleus and adds measurements.
 *
 * REQUIREMENTS
 * - QuPath 0.7.x + qupath-extension-biop-spotiflow
 * - Nuclei annotations must already exist (from script 01)
 *
 * OUTPUT
 * - Creates point detections for each detected focus
 * - Adds "Foci: Count" measurement to each nucleus annotation
 *************************************************************/

import qupath.lib.gui.dialogs.Dialogs as GuiDialogs
import qupath.lib.plugins.parameters.ParameterList
import qupath.lib.objects.PathObjects
import qupath.lib.objects.classes.PathClass
import qupath.lib.regions.RegionRequest
import qupath.lib.regions.ImagePlane
import qupath.lib.images.writers.TileExporter
import qupath.lib.roi.ROIs
import java.io.FilenameFilter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

// ---------------------------
// Toggle: show parameters dialog?
// ---------------------------
boolean showSettingsDialog = true

// ---------------------------
// Default parameters
// ---------------------------
def    fociChannelDefault    = 'GFP'      // Channel containing foci signal
def    fociClassNameDefault  = 'Foci'     // Class name for detected foci
def    fociDeviceDefault     = 'auto'     // Device flag for Spotiflow (auto|cuda|cpu|mps)
def    spotiflowModelDefault = 'general'
def    spotiflowModelChoices = ['general', 'hybiss', 'synth_complex', 'fluo_live', 'synth_3d', 'smfish_3d']
def    spotiflowPeakModeDefault = 'fast'
def    spotiflowPeakModeChoices = ['fast', 'skimage']

// ---------------------------
// Server & calibration
// ---------------------------
def server = getCurrentServer()
if (server == null)
    throw new IllegalStateException("No active image/server found.")

def imageData = getCurrentImageData()
if (imageData == null) {
    GuiDialogs.showErrorMessage("Spotiflow", "No image open.")
    return
}

// ---------------------------
// Helper utilities for Python-based Spotiflow detection
// ---------------------------
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
// Check for existing nuclei detections
// ---------------------------
def nuclei = getAnnotationObjects().findAll { 
    it.getPathClass()?.toString() == "Nuclei"
}

if (nuclei.isEmpty()) {
    GuiDialogs.showErrorMessage("Spotiflow",
        "No nuclei annotations found!\nPlease run 01_detect_nuclei.groovy first.")
    return
}

println "Found ${nuclei.size()} nuclei annotations to analyze."

// ---------------------------
// Get channel names for dropdown
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

// ---------------------------
// Acquire parameters
// ---------------------------
def fociChannel   = fociChannelDefault
def fociClassName = fociClassNameDefault
def fociDevice    = fociDeviceDefault
def spotiflowModel = spotiflowModelDefault
Double spotiflowProbThresh = null
String spotiflowPeakMode = spotiflowPeakModeDefault

if (showSettingsDialog) {
    def channelNames = getChannelNames()
    
    // Try to find a default channel (GFP, FITC, or second channel)
    def defaultFociChannel = channelNames.find { 
        def lower = (it ?: "").toLowerCase()
        lower.contains("gfp") || lower.contains("fitc") || lower.contains("green") || lower.contains("foci")
    } ?: (channelNames.size() > 1 ? channelNames[1] : channelNames[0])

    def deviceChoices = ["auto", "cuda", "cpu", "mps"]

    def params = new ParameterList()
        .addChoiceParameter("fociChannel", "Foci channel", defaultFociChannel, channelNames)
        .addStringParameter("fociClassName", "Foci class name", fociClassNameDefault,
            "Classification name for detected foci")
        .addChoiceParameter("fociDevice", "Spotiflow device", fociDeviceDefault, deviceChoices)
        .addTitleParameter("─────── Spotiflow Model & Options ───────")
        .addChoiceParameter("spotiflowModel", "Pretrained model", spotiflowModelDefault, spotiflowModelChoices)
        .addStringParameter("spotiflowProbThresh", "Probability threshold", "",
            "Leave blank to use the model-specific threshold.")
        .addChoiceParameter("spotiflowPeakMode", "Peak detection", spotiflowPeakModeDefault, spotiflowPeakModeChoices)

    if (!GuiDialogs.showParameterDialog("Spotiflow Foci Detection", params)) {
        println "Spotiflow: cancelled by user."
        return
    }

    fociChannel   = params.getChoiceParameterValue("fociChannel")?.toString() ?: defaultFociChannel
    fociClassName = params.getStringParameterValue("fociClassName") ?: fociClassNameDefault
    fociDevice    = params.getChoiceParameterValue("fociDevice")?.toString() ?: fociDeviceDefault
    spotiflowModel = params.getChoiceParameterValue("spotiflowModel")?.toString() ?: spotiflowModelDefault
    spotiflowPeakMode = params.getChoiceParameterValue("spotiflowPeakMode")?.toString() ?: spotiflowPeakModeDefault

    def probThreshText = params.getStringParameterValue("spotiflowProbThresh")

    try {
        spotiflowProbThresh = parseOptionalDouble("probability threshold", probThreshText)
    } catch (IllegalArgumentException parseErr) {
        GuiDialogs.showErrorMessage("Spotiflow", parseErr.getMessage())
        return
    }
}

def spotiflowRunConfig = [
    device     : fociDevice,
    model      : spotiflowModel,
    probThresh : spotiflowProbThresh,
    peakMode   : spotiflowPeakMode
]

// ---------------------------
// Run Spotiflow detection via python module
// ---------------------------
def tempRoot = Files.createTempDirectory("spotiflow-predict").toFile()
def imgLabel = getProjectEntry()?.getImageName() ?: (server.getMetadata()?.getName() ?: "image")
def datasetDir = new File(tempRoot, safeName(imgLabel) + "_patches")
datasetDir.mkdirs()

println "\n========== Preparing Spotiflow dataset =========="
def exportRecords = exportNucleiPatches(datasetDir, nuclei, fociChannel)
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
    println "Error during Spotiflow execution: ${e.getMessage()}"
    return
}

println "\n========== Importing detections =========="
def detectionsByAnnotation = loadSpotiflowDetections(exportRecords, datasetDir)
def fociPathClass = PathClass.fromString(fociClassName)

// Remove existing foci detections to avoid duplicates
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
// Count foci per nucleus
// ---------------------------
def foci = getDetectionObjects().findAll { it.getPathClass()?.toString() == fociClassName }
println "Detected ${foci.size()} foci."

def MEAS_FOCI = "Foci: Count"

int touched = 0
nuclei.each { nucleus ->
    def roiNucleus = nucleus.getROI()
    if (roiNucleus == null) return

    int countFoci = foci.count { spot ->
        def r = spot.getROI()
        if (r == null) return false
        double cx = r.getCentroidX(), cy = r.getCentroidY()
        return roiNucleus.contains(cx, cy)
    }

    nucleus.getMeasurements().put(MEAS_FOCI, (double)countFoci)
    touched++
}

println "Done. Added foci count measurements to ${touched} nuclei annotations."

