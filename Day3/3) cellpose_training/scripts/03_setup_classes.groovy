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

def qupath = getQuPath()
def classList = qupath.getAvailablePathClasses()

def ensureClass = { String name, int color ->
    def pathClass = PathClass.fromString(name, color)
    def exists = classList.any { it.getName() == name }

    if (!exists) {
        Platform.runLater {
            classList.add(pathClass)
            println "Added class: $name"
        }
    } else {
        println "Class already exists: $name"
    }

    return pathClass
}

// Add Training (blue) and Validation (orange) classes
def trainingClass = ensureClass("Training", ColorTools.makeRGB(0, 0, 255))
def validationClass = ensureClass("Validation", ColorTools.makeRGB(255, 165, 0))

println ""
println "Next steps:"
println "1. Create rectangle annotations and classify them as 'Training' or 'Validation'"
println "2. Inside each rectangle, annotate ALL objects using the Brush or Wand tool"
println "3. Run 04_train_cellpose.groovy to train your model"
