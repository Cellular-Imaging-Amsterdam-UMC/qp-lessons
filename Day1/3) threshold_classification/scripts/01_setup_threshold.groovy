// 01_setup_threshold.groovy

// SETUP SCRIPT
// ------------
// Prepares the classes for the Thresholding lesson.

import javafx.application.Platform
import qupath.lib.objects.classes.PathClass
import qupath.lib.common.ColorTools
import qupath.lib.gui.dialogs.Dialogs as GuiDialogs

def classes = [
    "Islet": ColorTools.makeRGB(255, 255, 0)
]

// QuPath's class list is shared with the JavaFX UI. Read and update it on the same thread.
def qupath = getQuPath()
Platform.runLater {
    def classList = qupath.getAvailablePathClasses()
    classes.each { label, color ->
        def existing = classList.find { it?.getName() == label }
        if (existing == null) {
            classList.add(PathClass.fromString(label, color))
            println "Added class: $label"
        } else if (existing.getColor() != color) {
            existing.setColor(color)
            println "Updated color for class: $label"
        }
    }
}

removeAnnotations()
GuiDialogs.showInfoNotification("Setup", "Class 'Islet' ready for thresholding.")
