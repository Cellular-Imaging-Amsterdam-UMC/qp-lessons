// assignment1_magic_setup.groovy

// MAGIC SETUP SCRIPT
// ------------------
// This script prepares the QuPath environment for Assignment 1.
// It defines epidermal sublayers + dermis classes and assigns colors.

// Compatible with QuPath v0.7.0+

import javafx.application.Platform
import qupath.lib.objects.classes.PathClass
import qupath.lib.common.ColorTools
import qupath.lib.gui.dialogs.Dialogs as GuiDialogs

def classes = [
    "Stratum corneum": ColorTools.makeRGB(255, 255, 0),
    "EPTZ": ColorTools.makeRGB(255, 170, 0),
    "Stratum granulosum": ColorTools.makeRGB(255, 105, 180),
    "Stratum spinosum": ColorTools.makeRGB(0, 200, 255),
    "Stratum basale": ColorTools.makeRGB(0, 120, 255),
    "Dermis": ColorTools.makeRGB(0, 180, 0),
    "Whitespace": ColorTools.makeRGB(180, 180, 180)
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

removeAnnotations()

// 5. Feedback
print("Assignment 1 Setup Complete!")
print("Classes defined: " + classes.keySet())
GuiDialogs.showInfoNotification("Assignment 1 Setup", "Skin layer classes loaded and colors set!\nReady to classify skin.\nSlide: skin.ome.tif")
