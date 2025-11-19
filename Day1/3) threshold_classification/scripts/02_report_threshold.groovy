/**
 * 02_report_threshold.groovy
 * 
 * REPORT SCRIPT
 * -------------
 * Measures the area of the Islet class.
 */

import qupath.lib.gui.QuPathGUI

def imageData = getCurrentServer().getMetadata()
def pixelSizeX = imageData.getPixelWidthMicrons()
def pixelSizeY = imageData.getPixelHeightMicrons()

// Check if pixel size is valid (not 1.0 which usually means uncalibrated)
// If uncalibrated, we report in pixels
def isCalibrated = (pixelSizeX != 1.0 && !Double.isNaN(pixelSizeX))

def annotations = getAnnotationObjects()
def isletArea = 0.0

for (annotation in annotations) {
    def pathClass = annotation.getPathClass()
    if (pathClass != null && pathClass.getName() == "Islet") {
        def roi = annotation.getROI()
        if (roi != null) {
            if (isCalibrated) {
                isletArea += roi.getArea() * pixelSizeX * pixelSizeY
            } else {
                isletArea += roi.getArea()
            }
        }
    }
}

if (isletArea == 0) {
    Dialogs.showErrorMessage("Report", "No 'Islet' objects found!\nDid you click 'Create Objects' in the thresholder?")
    return
}

def unit = isCalibrated ? "µm²" : "pixels"
def message = String.format("Total Islet Area: %.2f %s", isletArea, unit)

Dialogs.showInfoNotification("Islet Measurement", message)
print(message)
