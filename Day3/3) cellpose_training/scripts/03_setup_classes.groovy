// *************************************************************
//  Setup Training & Validation Classes for Cellpose Training
//  
//  This script adds the required classes for Cellpose training:
//  - Training: regions containing ground truth for model training
//  - Validation: regions for testing during training (prevents overfitting)
//
//  Run this once before creating your ground truth annotations.
// *************************************************************

import javafx.application.Platform
import qupath.lib.objects.classes.PathClass
import qupath.lib.common.ColorTools

def classes = [
    "Training": ColorTools.makeRGB(0, 0, 255),
    "Validation": ColorTools.makeRGB(255, 165, 0)
]

// QuPath's class list is shared with the JavaFX UI. Read and update it on the same thread.
def qupath = getQuPath()
Platform.runLater {
    def classList = qupath.getAvailablePathClasses()
    classes.each { label, color ->
        if (!classList.any { it?.getName() == label }) {
            classList.add(PathClass.fromString(label, color))
            println "Added class: $label"
        }
    }
}

println ""
println "Next steps:"
println "1. Create rectangle annotations and classify them as 'Training' or 'Validation'"
println "2. Inside each rectangle, annotate ALL objects using the Brush or Wand tool"
println "3. Run 04_train_cellpose.groovy to train your model"
