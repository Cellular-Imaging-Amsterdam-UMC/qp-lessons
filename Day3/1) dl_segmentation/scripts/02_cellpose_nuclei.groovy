/**
 * 02_cellpose_nuclei.groovy
 * 
 * Runs Cellpose for cell/nuclei detection.
 * Requires: QuPath Cellpose Extension & Configured Python Environment.
 */

import qupath.ext.biop.cellpose.Cellpose2D

// 1. Setup Cellpose builder
def cellpose = Cellpose2D.builder("cyto2") // Use 'cyto2' or 'nuclei' model
    .pixelSize(0.5)             // Resolution (microns)
    .channels("DAPI")           // Channel name to use
    .diameter(30)               // Approximate diameter in pixels (at the requested pixelSize)
    .build()

// 2. Run detection
def imageData = getCurrentImageData()
def pathObjects = cellpose.detectObjects(imageData)

// 3. Add objects to hierarchy
addObjects(pathObjects)

print("Cellpose detection complete: " + pathObjects.size() + " cells found.")
