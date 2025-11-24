// 01_setup_cells.groovy

// SETUP SCRIPT
// ------------
// Prepares the classes for the Cell Classification lesson.

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

def normalClass = ensureClass("Normal", ColorTools.makeRGB(100, 100, 255)) // Light Blue
def apoptoticClass = ensureClass("Apoptotic", ColorTools.makeRGB(255, 0, 0)) // Red
def ignoreClass = ensureClass("Ignore*", ColorTools.makeRGB(255, 255, 0)) // Yellow

// 2. Classes registered in session list

// 3. Clean up
removeAnnotations()

Dialogs.showInfoNotification("Setup", "Classes 'Normal', 'Apoptotic', and 'Ignore*' ready for cell detection.")
