/**
 * 02_spotiflow_foci.groovy
 * 
 * Runs Spotiflow to detect spots inside the selected nuclei.
 * Requires: QuPath Spotiflow Extension.
 */

import qupath.ext.spotiflow.Spotiflow

// 1. Check selection
def selected = getSelectedObjects()
if (selected.isEmpty()) {
    Dialogs.showErrorMessage("Spotiflow", "Please select the Nuclei first!")
    return
}

// 2. Setup Spotiflow
def spotiflow = Spotiflow.builder()
    .model("general")   // Use the general pre-trained model
    .channel("Green")   // Detect spots in the Green channel
    .probThresh(0.5)    // Probability threshold
    .build()

// 3. Run detection
// This runs detection *within* the selected parent objects
spotiflow.detectObjects(getCurrentImageData(), selected)

print("Spotiflow detection complete.")
