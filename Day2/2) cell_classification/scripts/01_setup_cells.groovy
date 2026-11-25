// 01_setup_cells.groovy

// SETUP SCRIPT
// ------------
// Prepares the classes for the Cell Classification lesson.

import javafx.application.Platform
import qupath.lib.objects.classes.PathClass
import qupath.lib.common.ColorTools
import qupath.lib.gui.dialogs.Dialogs as GuiDialogs

def classes = [
    "Normal": ColorTools.makeRGB(0, 255, 0),
    "Apoptotic": ColorTools.makeRGB(0, 0, 255)
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

GuiDialogs.showInfoNotification("Setup", "Classes 'Normal', 'Apoptotic', and 'Ignore*' ready for cell detection.")
