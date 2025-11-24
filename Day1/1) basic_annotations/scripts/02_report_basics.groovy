// 02_report_basics.groovy

// REPORT SCRIPT
// -------------
// Counts the number of annotations drawn by the user and sums their area.
// This demonstrates that QuPath "knows" what you drew.

import qupath.lib.gui.QuPathGUI

// 1. Get the data
def annotations = getAnnotationObjects()
def count = annotations.size()

if (count == 0) {
    Dialogs.showErrorMessage("Report", "No annotations found!\nTry drawing some shapes first.")
    return
}

// 2. Calculate Total Area
def totalArea = 0.0
def imageData = getCurrentServer().getMetadata()
def pixelSizeX = imageData.getPixelWidthMicrons()
def pixelSizeY = imageData.getPixelHeightMicrons()
def isCalibrated = (pixelSizeX != 1.0 && !Double.isNaN(pixelSizeX))

for (annotation in annotations) {
    def roi = annotation.getROI()
    if (roi != null) {
        if (isCalibrated) {
            totalArea += roi.getArea() * pixelSizeX * pixelSizeY
        } else {
            totalArea += roi.getArea()
        }
    }
}

// 3. Report
def unit = isCalibrated ? "µm²" : "pixels"
// Convert to mm² if it's huge
def areaString = ""
if (isCalibrated && totalArea > 1000000) {
    areaString = String.format("%.2f mm²", totalArea / 1000000)
} else {
    areaString = String.format("%.2f %s", totalArea, unit)
}

def message = String.format("You drew %d annotations.\nTotal Area: %s", count, areaString)

Dialogs.showInfoNotification("Basic Measurement", message)
print(message)
