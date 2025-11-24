// 01_stardist_nuclei.groovy

// Runs StarDist 2D for nuclear detection.
// Requires: QuPath StarDist Extension & Configured Python Environment.

import qupath.ext.stardist.StarDist2D

// 1. Setup the StarDist builder
def stardist = StarDist2D.builder()
    .threshold(0.5)              // Probability (detection) threshold
    .normalizePercentiles(1, 99) // Normalize contrast
    .pixelSize(0.5)              // Resolution to run detection (microns)
    .build()

// 2. Run detection
// We detect on the current image data
def imageData = getCurrentImageData()
def pathObjects = stardist.detectObjects(imageData)

// 3. Add objects to hierarchy
addObjects(pathObjects)

print("StarDist detection complete: " + pathObjects.size() + " nuclei found.")
