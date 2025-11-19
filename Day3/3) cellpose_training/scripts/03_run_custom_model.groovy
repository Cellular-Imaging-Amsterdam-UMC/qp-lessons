/**
 * 03_run_custom_model.groovy
 * 
 * Runs the custom-trained Cellpose model.
 */

import qupath.ext.biop.cellpose.Cellpose2D

// 1. Setup builder with CUSTOM model
// We refer to the model by the name we gave it during training
def cellpose = Cellpose2D.builder("my_phase_model")
    .channels("Red", "Green", "Blue")
    .diameter(0) // 0 = Use the diameter estimated during training
    .build()

// 2. Run detection
def imageData = getCurrentImageData()
def pathObjects = cellpose.detectObjects(imageData)

addObjects(pathObjects)

print("Custom model detection complete.")
