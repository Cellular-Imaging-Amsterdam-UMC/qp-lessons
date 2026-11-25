// assignment3_magic_setup.groovy
// QuPath v0.7.0+ helper script for Assignment 3 (CellsNuclei example)
// Creates the Dividing / Non-dividing classes with vivid colors

import javafx.application.Platform
import qupath.lib.objects.classes.PathClass
import qupath.lib.common.ColorTools
import qupath.lib.gui.dialogs.Dialogs as GuiDialogs

def classes = [
    "Dividing": ColorTools.makeRGB(0, 255, 0),
    "Non-dividing": ColorTools.makeRGB(255, 0, 0),
    "Background": ColorTools.makeRGB(120, 120, 120)
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
removeDetections()

print "Assignment 3 Setup Complete!"
GuiDialogs.showInfoNotification(
        "Assignment 3 Setup",
        "Classes created: Dividing, Non-dividing, Background.\nReady to run Cell detection on CellsNuclei.ome.tif.")
