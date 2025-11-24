// 01_setup_positive.groovy

// SETUP SCRIPT
// ------------
// Prepares the classes for Positive Cell Detection.

import javafx.application.Platform
import qupath.lib.objects.classes.PathClass
import qupath.lib.common.ColorTools

// 1. Define the classes
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

def positiveClass = ensureClass("Positive", ColorTools.makeRGB(255, 0, 0))
def negativeClass = ensureClass("Negative", ColorTools.makeRGB(0, 0, 255))

// 2. (Intentionally left as session-only classes)

// 3. Clean up
removeAnnotations()

Dialogs.showInfoNotification("Setup", "Classes 'Positive' & 'Negative' ready for detection.")
