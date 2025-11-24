// 01_magic_setup.groovy

// MAGIC SETUP SCRIPT
// ------------------
// This script prepares the QuPath environment for the teaching session.
// It defines the classes and assigns high-contrast colors.

// Compatible with QuPath v0.4.0 - v0.6.0+

import javafx.application.Platform
import qupath.lib.objects.classes.PathClass
import qupath.lib.common.ColorTools

// 1. Define the classes we want to use
// We use PathClass.fromString to ensure we are using the singleton instances and setting colors
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

def epidermisClass = ensureClass("Epidermis", ColorTools.makeRGB(255, 0, 0))     // Red
def dermisClass = ensureClass("Dermis", ColorTools.makeRGB(0, 200, 0))           // Green
def whitespaceClass = ensureClass("Whitespace", ColorTools.makeRGB(200, 200, 200)) // Gray
def ignoreClass = ensureClass("Ignore*", ColorTools.makeRGB(255, 255, 0))        // Yellow

// 2. Create the list of classes
def classes = [epidermisClass, dermisClass, whitespaceClass, ignoreClass]

// 3. Classes are now registered in the session list

// 4. Clean up any existing annotations to start fresh
// (Optional: Comment this out if you want to keep student work)
removeAnnotations()

// 5. Feedback
print("Magic Setup Complete!")
print("Classes defined: " + classes.collect { it.getName() })
Dialogs.showInfoNotification("Magic Setup", "Classes loaded and colors set!\nReady for training.")
