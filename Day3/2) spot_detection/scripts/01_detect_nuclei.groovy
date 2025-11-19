/**
 * 01_detect_nuclei.groovy
 * 
 * Uses StarDist (or standard detection) to find nuclei.
 * These will serve as the "Parent" objects for spot detection.
 */

import qupath.ext.stardist.StarDist2D

// Clear old objects
clearAllObjects()

// Run StarDist on the Red channel (Nuclei)
def stardist = StarDist2D.builder()
    .threshold(0.5)
    .channels("Red") // Adjust channel name if needed
    .pixelSize(0.5)
    .build()

def imageData = getCurrentImageData()
def nuclei = stardist.detectObjects(imageData)

addObjects(nuclei)
selectObjects(nuclei)

print("Nuclei detected: " + nuclei.size())
