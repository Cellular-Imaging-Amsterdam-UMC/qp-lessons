/*************************************************************
 * Calibrate All Images in Project (QuPath 0.7.x)
 *
 * PURPOSE
 * - Sets the pixel size calibration for all images in the current project.
 * - Useful for images without embedded calibration (e.g., JPG files).
 *
 * REQUIREMENTS
 * - A QuPath project must be open with images.
 *
 * OUTPUT
 * - All images in the project will have their pixel size set to the specified value.
 *************************************************************/

import qupath.lib.gui.dialogs.Dialogs as GuiDialogs
import qupath.lib.plugins.parameters.ParameterList
import qupath.lib.images.servers.PixelCalibration

// ---------------------------
// Default parameters
// ---------------------------
double pixelSizeDefault = 0.2  // µm/pixel

// ---------------------------
// Check for open project
// ---------------------------
def project = getProject()
if (project == null) {
    GuiDialogs.showErrorMessage("Calibration",
        "No project is open!\nPlease open a QuPath project first.")
    return
}

def imageList = project.getImageList()
if (imageList.isEmpty()) {
    GuiDialogs.showErrorMessage("Calibration",
        "The project contains no images!")
    return
}

// ---------------------------
// Show parameters dialog
// ---------------------------
def params = new ParameterList()
    .addDoubleParameter("pixelSize", "Pixel size", pixelSizeDefault, "µm/pixel", 0.001, 10.0,
        "The pixel size in micrometers per pixel (isotropic, same for X and Y)")

if (!GuiDialogs.showParameterDialog("Set Image Calibration", params)) {
    println "Calibration: cancelled by user."
    return
}

double pixelSize = params.getDoubleParameterValue("pixelSize")
if (pixelSize <= 0) {
    GuiDialogs.showErrorMessage("Calibration", "Pixel size must be greater than 0!")
    return
}

println "Setting pixel size to ${pixelSize} µm/pixel for all images..."

// ---------------------------
// Calibrate all images
// ---------------------------
int calibratedCount = 0
int errorCount = 0

for (entry in imageList) {
    try {
        def imageData = entry.readImageData()
        def server = imageData.getServer()
        
        // Create new calibration with the specified pixel size
        def newCalibration = new PixelCalibration.Builder()
            .pixelSizeMicrons(pixelSize, pixelSize)  // X and Y pixel size
            .build()
        
        // Get current metadata and update with new calibration
        def metadata = server.getMetadata()
        def newMetadata = new qupath.lib.images.servers.ImageServerMetadata.Builder(metadata)
            .pixelSizeMicrons(pixelSize, pixelSize)
            .build()
        
        // Update the image data with new metadata
        imageData.updateServerMetadata(newMetadata)
        
        // Save the changes
        entry.saveImageData(imageData)
        
        println "  Calibrated: ${entry.getImageName()}"
        calibratedCount++
        
    } catch (Exception e) {
        println "  ERROR calibrating ${entry.getImageName()}: ${e.getMessage()}"
        errorCount++
    }
}

// ---------------------------
// Summary
// ---------------------------
println "\n========== Calibration Complete =========="
println "Images calibrated: ${calibratedCount}"
if (errorCount > 0) {
    println "Errors: ${errorCount}"
}
println "Pixel size: ${pixelSize} µm/pixel"
println "==========================================="

def message = "Calibrated ${calibratedCount} images to ${pixelSize} µm/pixel"
if (errorCount > 0) {
    message += "\n\n${errorCount} errors occurred (see log)"
}
GuiDialogs.showInfoNotification("Calibration Complete", message)

// Refresh current image if one is open
if (getCurrentImageData() != null) {
    println "\nNote: You may need to close and reopen the current image to see the updated calibration."
}
