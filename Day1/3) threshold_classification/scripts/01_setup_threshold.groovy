// 01_setup_threshold.groovy

// SETUP SCRIPT
// ------------
// Prepares the classes for the Thresholding lesson.

import javafx.application.Platform
import qupath.lib.objects.classes.PathClass
import qupath.lib.common.ColorTools

// 1. Define the class
def className = "Islet"
def classColor = ColorTools.makeRGB(255, 255, 0) // Yellow
def qupath = getQuPath()
def classList = qupath.getAvailablePathClasses()

def ensureClass = { String name, int color ->
	def desiredClass = PathClass.fromString(name, color)
	def existing = classList.find { it.getName() == name }

	if (existing == null) {
		Platform.runLater {
			classList.add(desiredClass)
			println "Added class: $name"
		}
		return desiredClass
	}

	if (existing.getColor() != color) {
		Platform.runLater {
			existing.setColor(color)
			println "Updated color for class: $name"
		}
	} else {
		println "Class already exists: $name"
	}

	return existing
}

def isletClass = ensureClass(className, classColor)

// 3. Clean up
removeAnnotations()

Dialogs.showInfoNotification("Setup", "Class '${className}' ready for thresholding.")
