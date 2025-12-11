// assignment2_magic_setup.groovy
// QuPath v0.6.0+ helper script for Assignment 2 (CellsNuclei example)
// Creates the Dividing / Non-dividing classes with vivid colors

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

def dividingClass = ensureClass("Dividing", ColorTools.makeRGB(0, 255, 0))          // Bright green

def nonDividingClass = ensureClass("Non-dividing", ColorTools.makeRGB(255, 0, 0))   // Red

def backgroundClass = ensureClass("Background", ColorTools.makeRGB(120, 120, 120))  // Gray optional

removeAnnotations()
removeDetections()

print "Assignment 2 Setup Complete!"
Dialogs.showInfoNotification(
        "Assignment 2 Setup",
        "Classes created: Dividing, Non-dividing, Background.\nReady to run Cell detection on CellsNuclei.ome.tif.")
