/**
 * FAST Spectral compensation helper for large fluorescence tilescans.
 *
 * Optimized version with:
 * - Parallel tile processing (configurable thread count)
 * - JPEG-2000 lossy compression (similar to CZI ~12× compression)
 * - Pyramidal output for faster viewing
 * - Memory-efficient channel processing
 *
 * Uses QuPath's built-in OMEPyramidWriter.Builder for reliable output of large images.
 */

import ij.plugin.filter.GaussianBlur
import ij.plugin.filter.RankFilters
import ij.process.FloatProcessor
import qupath.lib.gui.QuPathGUI
import qupath.lib.gui.dialogs.Dialogs
import qupath.lib.images.servers.ImageServerMetadata
import qupath.lib.images.servers.ImageChannel
import qupath.lib.images.servers.TransformingImageServer
import qupath.lib.images.servers.CroppedImageServer
import qupath.lib.images.writers.ome.OMEPyramidWriter
import qupath.lib.plugins.parameters.ParameterList
import qupath.lib.regions.RegionRequest
import qupath.lib.regions.ImageRegion
import qupath.lib.roi.RectangleROI

import javafx.application.Platform

import java.awt.image.BufferedImage
import java.awt.image.WritableRaster

// Gather the image server and metadata
def server = getCurrentServer()
if (server == null) {
    Dialogs.showErrorMessage("Spectral Compensation", "Open an image before running this script.")
    return
}

def imageData = getCurrentImageData()
def metadata = server.getMetadata()
if (metadata == null || imageData == null) {
    Dialogs.showErrorMessage("Spectral Compensation", "Image metadata is unavailable.")
    return
}

// Check for selected rectangular annotation
def selectedObject = imageData.getHierarchy().getSelectionModel().getSelectedObject()
def selectedROI = null
def hasRectSelection = false
if (selectedObject != null && selectedObject.getROI() != null) {
    def roi = selectedObject.getROI()
    // Check if it's a rectangle (RectangleROI or any ROI that is essentially rectangular)
    if (roi.isArea() && isRectangularROI(roi)) {
        selectedROI = roi
        hasRectSelection = true
        println "Found selected rectangular annotation: ${roi.getBoundsWidth().intValue()} x ${roi.getBoundsHeight().intValue()} px"
    } else {
        println "Selected annotation is not rectangular - will offer full image option."
    }
}

// Derive channel names (fallback to generic names if metadata lacks explicit labels)
def channelNames = getChannelNames(metadata)
if (channelNames.isEmpty()) {
    Dialogs.showErrorMessage("Spectral Compensation", "No channels were found on the current image.")
    return
}

def defaultTarget = channelNames[0]
def defaultSource = (channelNames.size() > 1) ? channelNames[1] : channelNames[0]

def defaultOutputDir = getDefaultOutputDir()

// Estimate crosstalk matrix before showing dialog
println "Estimating spectral crosstalk between channels..."
def crosstalkMatrix = estimateCrosstalkMatrix(server, channelNames)
displayCrosstalkMatrix(crosstalkMatrix, channelNames)

// Save crosstalk matrix to CSV
def imageName = server.getMetadata()?.getName() ?: "image"
def safeImageName = safeName(imageName)
saveCrosstalkMatrixCSV(crosstalkMatrix, channelNames, defaultOutputDir, safeImageName)

// Find suggested compensation based on highest crosstalk
def suggestion = findHighestCrosstalk(crosstalkMatrix, channelNames)
if (suggestion != null) {
    defaultTarget = suggestion.target
    defaultSource = suggestion.source
    println "\nSuggested: Compensate '${defaultTarget}' for bleed-through from '${defaultSource}' (~${String.format('%.1f', suggestion.percentage)}%)"
}

// Estimate memory requirements for user info
long imageSizeBytes = (long) server.getWidth() * server.getHeight() * 4 * channelNames.size()
def imageSizeMB = imageSizeBytes / (1024 * 1024)
println String.format("\nImage size: %d x %d, %d channels (%.0f MB uncompressed)", 
    server.getWidth(), server.getHeight(), channelNames.size(), imageSizeMB)
println "FAST MODE: Parallel tile processing with JPEG-2000 compression"

// Compression options for dialog
def compressionOptions = ["JPEG-2000 Lossy", "JPEG-2000 Lossless", "LZW", "ZLIB", "Uncompressed"]
def defaultCompression = "JPEG-2000 Lossless"  // Use lossless by default for best quality

// Default thread count - limited to avoid BioFormats reader pool contention
// BioFormats typically has 4-8 readers in its pool; more threads cause blocking/interrupts
int defaultThreads = 4

// Region options based on selection
def regionOptions = ["Full image"]
def defaultRegion = "Full image"
if (hasRectSelection) {
    def w = selectedROI.getBoundsWidth().intValue()
    def h = selectedROI.getBoundsHeight().intValue()
    regionOptions = ["Selected rectangle (${w} x ${h} px)", "Full image"]
    defaultRegion = "Selected rectangle (${w} x ${h} px)"
}

// Capture user choices via ParameterList
boolean showDialog = true
if (showDialog) {
    def suggestedPercentage = suggestion?.percentage ?: 10.0
    def params = new ParameterList()
        .addChoiceParameter("targetChannel", "Target channel", defaultTarget, channelNames)
        .addChoiceParameter("sourceChannel", "Compensator channel", defaultSource, channelNames)
        .addDoubleParameter("percentage", "Subtraction amount", suggestedPercentage, "%", 0.0, 200.0,
                "Multiply the filtered compensator signal by this percent before subtracting.")
        .addDoubleParameter("medianRadius", "Median filter radius", 1.0, "px", 0.0, 30.0,
                "Set to 0 to skip the median filter.")
        .addDoubleParameter("gaussianSigma", "Gaussian sigma", 1.0, "px", 0.0, 30.0,
                "Set to 0 to skip Gaussian smoothing.")
        .addTitleParameter("────── Region ──────")
        .addChoiceParameter("region", "Process region", defaultRegion, regionOptions,
                "Select 'Full image' or use a rectangular annotation for faster testing.")
        .addTitleParameter("────── Performance ──────")
        .addIntParameter("tileSize", "Tile size", 512, "px",
                "Smaller tiles allow more parallelism. 512 is optimal for most cases.")
        .addIntParameter("numThreads", "Processing threads", defaultThreads, "",
                "Parallel threads (max 8). BioFormats reader pool limits concurrency.")
        .addTitleParameter("────── Output ──────")
        .addChoiceParameter("compression", "Compression", defaultCompression, compressionOptions,
                "JPEG-2000 Lossless gives good compression without quality loss.")
        .addBooleanParameter("createPyramid", "Create pyramid for fast viewing", true,
                "Generates multiple resolution levels for faster navigation in QuPath.")
        .addStringParameter("outputDir", "Output directory", defaultOutputDir.getAbsolutePath(),
                "Output OME-TIFF will be saved here; the directory will be created if missing.")

    if (!Dialogs.showParameterDialog("Spectral Compensation (FAST)", params)) {
        println "Spectral compensation cancelled by user."
        return
    }

    def targetChannel = params.getChoiceParameterValue("targetChannel")?.toString() ?: defaultTarget
    def sourceChannel = params.getChoiceParameterValue("sourceChannel")?.toString() ?: defaultSource
    double percentage = params.getDoubleParameterValue("percentage")
    double medianRadius = params.getDoubleParameterValue("medianRadius")
    double gaussianSigma = params.getDoubleParameterValue("gaussianSigma")
    def regionChoice = params.getChoiceParameterValue("region")?.toString() ?: "Full image"
    boolean useSelectedRegion = hasRectSelection && regionChoice.startsWith("Selected")
    int tileSize = params.getIntParameterValue("tileSize")
    int numThreads = params.getIntParameterValue("numThreads")
    def compressionChoice = params.getChoiceParameterValue("compression")?.toString() ?: defaultCompression
    boolean createPyramid = params.getBooleanParameterValue("createPyramid")
    def outputDirPath = params.getStringParameterValue("outputDir")?.trim()

    runSpectralCompensation(server, metadata, imageData, channelNames, targetChannel, sourceChannel,
            percentage, medianRadius, gaussianSigma, tileSize, numThreads, compressionChoice, createPyramid, outputDirPath,
            useSelectedRegion, selectedROI)
}

return

// --------------------
// Helper functions
// --------------------

/**
 * Check if an ROI is rectangular (simple bounds check).
 */
def isRectangularROI(roi) {
    if (roi == null) return false
    // Check if the ROI area matches its bounding box area (within 1% tolerance)
    def roiArea = roi.getArea()
    def boundsArea = roi.getBoundsWidth() * roi.getBoundsHeight()
    if (boundsArea <= 0) return false
    def ratio = roiArea / boundsArea
    return ratio > 0.99  // 99% of bounding box = essentially rectangular
}

def runSpectralCompensation(server, metadata, imageData, channelNames, targetChannel, sourceChannel,
        percentage, medianRadius, gaussianSigma, tileSize, numThreads, compressionChoice, createPyramid, outputDirPath,
        useSelectedRegion = false, selectedROI = null) {

    if (targetChannel == sourceChannel) {
        Dialogs.showErrorMessage("Spectral Compensation", "Target and compensator channel must differ.")
        return
    }

    if (!outputDirPath) {
        Dialogs.showErrorMessage("Spectral Compensation", "Please provide a valid output directory.")
        return
    }

    def outputDir = new File(outputDirPath)
    if (!outputDir.exists() && !outputDir.mkdirs()) {
        Dialogs.showErrorMessage("Spectral Compensation", "Unable to create output directory: " + outputDirPath)
        return
    }

    int targetIndex = channelNames.indexOf(targetChannel)
    int sourceIndex = channelNames.indexOf(sourceChannel)
    if (targetIndex < 0 || sourceIndex < 0) {
        Dialogs.showErrorMessage("Spectral Compensation", "Could not resolve the chosen channels.")
        return
    }

    def subtractFraction = (float) (percentage / 100.0)
    def safeTarget = safeName(targetChannel)
    def safeSource = safeName(sourceChannel)

    int numChannels = channelNames.size()

    // Map compression choice to enum
    def compressionType = mapCompression(compressionChoice)
    
    // Clamp parameters
    tileSize = Math.max(256, Math.min(2048, tileSize))
    // Limit threads to 8 max - BioFormats reader pool can't handle more concurrent requests
    numThreads = Math.max(1, Math.min(numThreads, 8))

    println "═══════════════════════════════════════════════════════════════"
    println "FAST Spectral Compensation"
    println "═══════════════════════════════════════════════════════════════"
    println "Compensating channel '${targetChannel}' by subtracting ${percentage}% of '${sourceChannel}'"
    println "Tile size: ${tileSize}px, Threads: ${numThreads}"
    println "Compression: ${compressionChoice}"
    println "Create pyramid: ${createPyramid}"
    
    // Determine the server to use (full image or cropped region)
    def workingServer = server
    def regionWidth = server.getWidth()
    def regionHeight = server.getHeight()
    
    if (useSelectedRegion && selectedROI != null) {
        int x = selectedROI.getBoundsX().intValue()
        int y = selectedROI.getBoundsY().intValue()
        int w = selectedROI.getBoundsWidth().intValue()
        int h = selectedROI.getBoundsHeight().intValue()
        
        // Create a cropped server for the selected region
        def region = ImageRegion.createInstance(x, y, w, h, 0, 0)
        workingServer = new CroppedImageServer(server, region)
        regionWidth = w
        regionHeight = h
        
        println "Processing SELECTED REGION: ${w} x ${h} px (at ${x}, ${y})"
    } else {
        println "Processing FULL IMAGE: ${regionWidth} x ${regionHeight} px"
    }
    println "═══════════════════════════════════════════════════════════════"

    // Setup output file with unique name (avoid overwriting)
    def regionSuffix = useSelectedRegion ? "_crop" : ""
    def baseName = String.format("comp_%s_%s%s", safeTarget, safeSource, regionSuffix)
    def stitchedFile = getUniqueFile(outputDir, baseName, ".ome.tif")

    try {
        println "Creating compensated image server..."
        
        // Create a wrapping server that applies compensation on-the-fly
        // Uses workingServer which may be cropped if a region was selected
        def compensatedServer = new SpectralCompensationServer(
            workingServer, targetIndex, sourceIndex, subtractFraction, medianRadius, gaussianSigma
        )
        
        println "Writing OME-TIFF with parallel processing..."
        println "Output: ${stitchedFile.absolutePath}"
        
        def startTime = System.currentTimeMillis()
        
        // Use OMEPyramidWriter.Builder for optimized output
        def builder = new OMEPyramidWriter.Builder(compensatedServer)
            .tileSize(tileSize)
            .parallelize(numThreads)
            .compression(compressionType)
            .bigTiff()  // Always use BigTIFF for large file support
        
        // Add pyramid levels if requested
        if (createPyramid) {
            // Calculate appropriate downsamples based on working region size
            def downsamples = calculateDownsamples(regionWidth, regionHeight)
            if (downsamples.size() > 1) {
                builder.downsamples(downsamples as double[])
                println "Pyramid levels: ${downsamples.collect { it as int }.join(', ')}×"
            }
        }
        
        // Build and write
        builder.build().writeSeries(stitchedFile.absolutePath)
        
        def elapsed = (System.currentTimeMillis() - startTime) / 1000.0
        
        compensatedServer.close()
        
        // Also close the cropped server if we created one
        if (useSelectedRegion && workingServer != server) {
            workingServer.close()
        }
        
        // Report file size
        def fileSizeMB = stitchedFile.length() / (1024.0 * 1024.0)
        def originalSizeMB = (long) regionWidth * regionHeight * 4 * numChannels / (1024.0 * 1024.0)
        def compressionRatio = originalSizeMB / fileSizeMB
        
        println "═══════════════════════════════════════════════════════════════"
        println String.format("Completed in %.1f seconds", elapsed)
        println String.format("File size: %.1f MB (%.1f× compression)", fileSizeMB, compressionRatio)
        println "═══════════════════════════════════════════════════════════════"

        // Try to open the result in QuPath
        def loaded = openImageInQuPath(stitchedFile)

        def message = String.format("Wrote %s (%.1f MB, %.1f× compression) in %.1f sec", 
            stitchedFile.name, fileSizeMB, compressionRatio, elapsed)
        if (loaded)
            message += ". Loaded into QuPath."
        else
            message += ". Open manually from: ${stitchedFile.absolutePath}"

        Dialogs.showInfoNotification("Spectral Compensation", message)

    } catch (Exception e) {
        def errorMsg = e.message ?: e.getClass().getName()
        println "ERROR: ${e.getClass().getName()}: ${errorMsg}"
        e.printStackTrace()
        Dialogs.showErrorMessage("Spectral Compensation", "Failed: ${errorMsg}")
    }
}

/**
 * Map user-friendly compression name to OMEPyramidWriter.CompressionType
 */
private OMEPyramidWriter.CompressionType mapCompression(String choice) {
    switch (choice) {
        case "JPEG-2000 Lossy":
            return OMEPyramidWriter.CompressionType.J2K_LOSSY
        case "JPEG-2000 Lossless":
            return OMEPyramidWriter.CompressionType.J2K
        case "LZW":
            return OMEPyramidWriter.CompressionType.LZW
        case "ZLIB":
            return OMEPyramidWriter.CompressionType.ZLIB
        case "Uncompressed":
            return OMEPyramidWriter.CompressionType.UNCOMPRESSED
        default:
            return OMEPyramidWriter.CompressionType.J2K_LOSSY
    }
}

/**
 * Calculate appropriate pyramid downsamples based on image dimensions.
 * Returns a list of downsamples starting from 1.
 */
private List<Double> calculateDownsamples(int width, int height) {
    def downsamples = [1.0d]
    int maxDim = Math.max(width, height)
    
    // Add levels until the smallest dimension is ~512 pixels or less
    int minSize = 512
    double ds = 2.0
    while (maxDim / ds > minSize && downsamples.size() < 6) {
        downsamples << ds
        ds *= 2.0
    }
    
    return downsamples
}

/**
 * Get a unique file path by appending (1), (2), etc. if file already exists.
 */
private File getUniqueFile(File dir, String baseName, String extension) {
    def file = new File(dir, baseName + extension)
    if (!file.exists()) {
        return file
    }
    
    // File exists, find a unique number
    int counter = 1
    while (true) {
        file = new File(dir, "${baseName}_(${counter})${extension}")
        if (!file.exists()) {
            return file
        }
        counter++
        if (counter > 999) {
            // Safety limit
            throw new RuntimeException("Too many files with same base name: ${baseName}")
        }
    }
}

/**
 * Custom ImageServer that wraps another server and applies spectral compensation
 * to the target channel on-the-fly when tiles are requested.
 * 
 * Optimized for parallel access - only modifies the target channel, leaves others untouched.
 */
class SpectralCompensationServer extends TransformingImageServer<BufferedImage> {
    
    private final int targetIndex
    private final int sourceIndex
    private final float subtractFraction
    private final double medianRadius
    private final double gaussianSigma
    
    SpectralCompensationServer(def wrappedServer, int targetIndex, int sourceIndex,
            float subtractFraction, double medianRadius, double gaussianSigma) {
        super(wrappedServer)
        this.targetIndex = targetIndex
        this.sourceIndex = sourceIndex
        this.subtractFraction = subtractFraction
        this.medianRadius = medianRadius
        this.gaussianSigma = gaussianSigma
    }
    
    @Override
    BufferedImage readRegion(RegionRequest request) throws IOException {
        // Read the original tile
        def img = getWrappedServer().readRegion(request)
        if (img == null) return null
        
        int w = img.getWidth()
        int h = img.getHeight()
        int area = w * h
        
        // Get the raster data
        WritableRaster raster = img.getRaster()
        
        // Only extract the channels we need (memory optimization)
        float[] targetPixels = new float[area]
        float[] sourcePixels = new float[area]
        
        raster.getSamples(0, 0, w, h, targetIndex, targetPixels)
        raster.getSamples(0, 0, w, h, sourceIndex, sourcePixels)
        
        // Apply filtering to source channel
        // Create local instances for thread safety
        def sourceProc = new FloatProcessor(w, h, sourcePixels.clone())
        if (medianRadius > 0) {
            new RankFilters().rank(sourceProc, medianRadius, RankFilters.MEDIAN)
        }
        if (gaussianSigma > 0) {
            new GaussianBlur().blurGaussian(sourceProc, gaussianSigma, gaussianSigma, 0.01)
        }
        
        // Subtract and clamp
        for (int i = 0; i < area; i++) {
            float value = targetPixels[i] - subtractFraction * sourceProc.getf(i)
            targetPixels[i] = value < 0f ? 0f : value
        }
        
        // Write only the modified target channel back
        raster.setSamples(0, 0, w, h, targetIndex, targetPixels)
        
        return img
    }
    
    @Override
    String getServerType() {
        return "Spectral Compensation Server (Fast)"
    }
    
    @Override
    protected String createID() {
        return getClass().getName() + ": " + getWrappedServer().getPath() + 
               " [target=" + targetIndex + ", source=" + sourceIndex + 
               ", fraction=" + subtractFraction + "]"
    }
    
    @Override
    protected qupath.lib.images.servers.ImageServerBuilder.ServerBuilder<BufferedImage> createServerBuilder() {
        // This server is transient and not meant to be serialized/rebuilt
        return null
    }
}

private def getChannelNames(imageMetadata) {
    def channels = imageMetadata?.getChannels()
    def names = (channels != null && !channels.isEmpty())
            ? channels.collect { it?.getName() ?: "" }
            : []
    if (names.isEmpty()) {
        int sizeC = imageMetadata?.getSizeC() ?: 1
        names = (0..<sizeC).collect { "Channel ${it + 1}" }
    }
    return names
}

private boolean openImageInQuPath(File stitchedFile) {
    try {
        def gui = QuPathGUI.getInstance()
        if (gui == null)
            return false

        // Must run on FX application thread
        def filePath = stitchedFile.absolutePath
        Platform.runLater {
            try {
                gui.openImage(gui.getViewer(), filePath, true, false)
            } catch (Exception e) {
                println "Failed to load image on FX thread: ${e.message}"
            }
        }
        return true
    } catch (Exception e) {
        println "Failed to load stitched image into QuPath: ${e.message}"
        return false
    }
}

private File getDefaultOutputDir() {
    def gui = QuPathGUI.getInstance()
    def project = gui?.getProject()
    def projectPath = project?.getPath()
    def parentDir = projectPath ? projectPath.getParent() : null
    if (parentDir != null) {
        return new File(parentDir.toFile(), "compensation")
    }
    return new File(System.getProperty("user.home"), "spectral_compensation_output")
}

private def safeName(String label) {
    return (label ?: "channel").replaceAll(/[^A-Za-z0-9]/, "_").replaceAll(/_+/, "_").replaceFirst(/^_/, "").replaceFirst(/_$/, "")
}

/**
 * Estimate crosstalk matrix by sampling tiles and computing normalized regression slopes.
 * Returns a 2D array where [target][source] = estimated % of source appearing in target.
 */
private double[][] estimateCrosstalkMatrix(server, List<String> channelNames) {
    int numChannels = channelNames.size()
    double[][] matrix = new double[numChannels][numChannels]
    
    int imageWidth = server.getWidth()
    int imageHeight = server.getHeight()
    
    // Sample up to 9 regions across the image
    int sampleSize = Math.min(512, Math.min(imageWidth, imageHeight))
    def samplePoints = []
    for (int yi = 0; yi < 3; yi++) {
        for (int xi = 0; xi < 3; xi++) {
            int x = (int)(imageWidth * (xi + 0.5) / 3.0 - sampleSize / 2)
            int y = (int)(imageHeight * (yi + 0.5) / 3.0 - sampleSize / 2)
            x = Math.max(0, Math.min(x, imageWidth - sampleSize))
            y = Math.max(0, Math.min(y, imageHeight - sampleSize))
            samplePoints << [x: x, y: y]
        }
    }
    
    // Collect pixel values from all samples
    def channelValues = (0..<numChannels).collect { [] as List<Float> }
    
    for (def pt : samplePoints) {
        def request = RegionRequest.createInstance(server.getPath(), 1.0, 
            pt.x, pt.y, sampleSize, sampleSize, 0, 0)
        def tile = server.readRegion(request)
        if (tile == null) continue
        
        def raster = tile.getData()
        int w = raster.getWidth()
        int h = raster.getHeight()
        int bands = raster.getNumBands()
        
        float[] flatPixels = raster.getPixels(0, 0, w, h, (float[]) null)
        int area = w * h
        
        // Sample every 4th pixel to reduce computation
        for (int idx = 0; idx < area; idx += 4) {
            int offset = idx * bands
            for (int c = 0; c < Math.min(bands, numChannels); c++) {
                channelValues[c] << flatPixels[offset + c]
            }
        }
        tile.flush()
    }
    
    // Compute statistics for each channel
    int n = channelValues[0].size()
    if (n < 100) {
        println "Warning: insufficient samples for crosstalk estimation"
        return matrix
    }
    
    // Calculate mean and standard deviation for each channel
    double[] means = new double[numChannels]
    double[] stds = new double[numChannels]
    double[] mins = new double[numChannels]
    
    for (int c = 0; c < numChannels; c++) {
        def vals = channelValues[c]
        double sum = 0
        double minVal = Double.MAX_VALUE
        for (int i = 0; i < n; i++) {
            sum += vals[i]
            if (vals[i] < minVal) minVal = vals[i]
        }
        means[c] = sum / n
        mins[c] = minVal
        
        double sumSq = 0
        for (int i = 0; i < n; i++) {
            double diff = vals[i] - means[c]
            sumSq += diff * diff
        }
        stds[c] = Math.sqrt(sumSq / n)
    }
    
    // Compute normalized crosstalk for each channel pair
    for (int target = 0; target < numChannels; target++) {
        for (int source = 0; source < numChannels; source++) {
            if (target == source) {
                matrix[target][source] = 0.0  // No self-crosstalk displayed
                continue
            }
            
            // Skip if source has no variation
            if (stds[source] < 1e-6) {
                matrix[target][source] = 0.0
                continue
            }
            
            // Compute Pearson correlation and regression slope
            def srcVals = channelValues[source]
            def tgtVals = channelValues[target]
            
            double sumXY = 0, sumX2 = 0, sumY2 = 0
            double sumX = 0, sumY = 0
            
            for (int i = 0; i < n; i++) {
                // Use background-subtracted values
                double x = srcVals[i] - mins[source]
                double y = tgtVals[i] - mins[target]
                sumX += x
                sumY += y
                sumXY += x * y
                sumX2 += x * x
                sumY2 += y * y
            }
            
            // Pearson correlation coefficient
            double denomCorr = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY))
            double correlation = 0
            if (denomCorr > 1e-10) {
                correlation = (n * sumXY - sumX * sumY) / denomCorr
            }
            
            // Only consider positive correlations (bleed-through)
            if (correlation <= 0) {
                matrix[target][source] = 0.0
                continue
            }
            
            // Compute regression slope on background-subtracted data
            double denomSlope = n * sumX2 - sumX * sumX
            double slope = 0
            if (Math.abs(denomSlope) > 1e-10) {
                slope = (n * sumXY - sumX * sumY) / denomSlope
            }
            
            // The crosstalk percentage is: slope * (mean_source / mean_target) * 100
            // This represents: what % of target signal comes from source bleed-through
            double meanSrcBg = sumX / n  // Background-subtracted mean
            double meanTgtBg = sumY / n
            
            double crosstalkPct = 0
            if (meanTgtBg > 1e-6 && slope > 0) {
                // Estimate: how much would target decrease if we subtract slope*source?
                // As percentage of target signal
                crosstalkPct = (slope * meanSrcBg / meanTgtBg) * 100.0
                
                // Weight by correlation to reduce noise from spurious correlations
                crosstalkPct *= correlation * correlation  // R-squared weighting
            }
            
            // Clamp to reasonable range
            matrix[target][source] = Math.max(0, Math.min(50, crosstalkPct))
        }
    }
    
    return matrix
}

/**
 * Display the crosstalk matrix in a readable format.
 */
private void displayCrosstalkMatrix(double[][] matrix, List<String> channelNames) {
    int n = channelNames.size()
    
    // Abbreviate channel names for display
    def shortNames = channelNames.collect { name ->
        if (name.length() > 12) {
            return name.substring(0, 10) + ".."
        }
        return name
    }
    
    println "\n╔══════════════════════════════════════════════════════════════════╗"
    println "║                  SPECTRAL CROSSTALK MATRIX (%)                   ║"
    println "║          Rows = Target channel, Columns = Source channel         ║"
    println "╠══════════════════════════════════════════════════════════════════╣"
    
    // Header row
    def header = String.format("║ %12s │", "Target\\Src")
    for (int c = 0; c < n; c++) {
        header += String.format(" %6s", shortNames[c].substring(0, Math.min(6, shortNames[c].length())))
    }
    println header + " ║"
    println "╟──────────────┼" + "───────" * n + "╢"
    
    // Data rows
    for (int t = 0; t < n; t++) {
        def row = String.format("║ %12s │", shortNames[t])
        for (int s = 0; s < n; s++) {
            double val = matrix[t][s]
            if (t == s) {
                row += "    -  "  // Self
            } else if (val < 0.5) {
                row += "    .  "  // Negligible
            } else {
                row += String.format(" %5.1f ", val)
            }
        }
        println row + " ║"
    }
    
    println "╚══════════════════════════════════════════════════════════════════╝"
    println "\nNote: Values show estimated % bleed-through (R²-weighted)."
    println "      High values indicate spectral overlap requiring compensation."
}

/**
 * Save the crosstalk matrix as a CSV file.
 */
private void saveCrosstalkMatrixCSV(double[][] matrix, List<String> channelNames, File outputDir, String imageName) {
    // Ensure output directory exists
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    
    def csvFile = new File(outputDir, "${imageName}_crosstalk.csv")
    
    def sb = new StringBuilder()
    
    // Header row
    sb.append("Target\\Source")
    for (String name : channelNames) {
        sb.append(",\"").append(name.replace("\"", "\"\"")).append("\"")
    }
    sb.append("\n")
    
    // Data rows
    int n = channelNames.size()
    for (int t = 0; t < n; t++) {
        sb.append("\"").append(channelNames[t].replace("\"", "\"\"")).append("\"")
        for (int s = 0; s < n; s++) {
            if (t == s) {
                sb.append(",")  // Empty for self
            } else {
                sb.append(",").append(String.format("%.2f", matrix[t][s]))
            }
        }
        sb.append("\n")
    }
    
    csvFile.text = sb.toString()
    println "Crosstalk matrix saved to: ${csvFile.absolutePath}"
}

/**
 * Find the highest off-diagonal crosstalk value and return suggested compensation.
 */
private Map findHighestCrosstalk(double[][] matrix, List<String> channelNames) {
    int n = channelNames.size()
    double maxVal = 0
    int maxTarget = -1
    int maxSource = -1
    
    for (int t = 0; t < n; t++) {
        for (int s = 0; s < n; s++) {
            if (t != s && matrix[t][s] > maxVal) {
                maxVal = matrix[t][s]
                maxTarget = t
                maxSource = s
            }
        }
    }
    
    if (maxTarget >= 0 && maxVal > 2.0) {  // Only suggest if crosstalk > 2%
        return [
            target: channelNames[maxTarget],
            source: channelNames[maxSource],
            percentage: maxVal
        ]
    }
    return null
}
