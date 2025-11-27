// assignment1_magic_setup.groovy

// MAGIC SETUP SCRIPT
// ------------------
// This script prepares the QuPath environment for Assignment 1.
// It defines epidermal sublayers + dermis classes and assigns colors.

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

def scClass   = ensureClass("Stratum corneum", ColorTools.makeRGB(255, 255, 0))          // Yellow
def eptzClass = ensureClass("EPTZ", ColorTools.makeRGB(255, 170, 0))                     // Orange
def sgClass   = ensureClass("Stratum granulosum", ColorTools.makeRGB(255, 105, 180))     // Pink
def ssClass   = ensureClass("Stratum spinosum", ColorTools.makeRGB(0, 200, 255))         // Cyan
def sbClass   = ensureClass("Stratum basale", ColorTools.makeRGB(0, 120, 255))           // Blue
def dermisClass = ensureClass("Dermis", ColorTools.makeRGB(0, 180, 0))                   // Green
def whitespaceClass = ensureClass("Whitespace", ColorTools.makeRGB(180, 180, 180))       // Gray

// 2. Create the list of classes
def classes = [scClass, eptzClass, sgClass, ssClass, sbClass, dermisClass, whitespaceClass]

// 3. Classes are now registered in the session list

// 4. Clean up any existing annotations to start fresh
// (Optional: Comment this out if you want to keep student work)
removeAnnotations()

// 5. Feedback
print("Assignment 1 Setup Complete!")
print("Classes defined: " + classes.collect { it.getName() })
Dialogs.showInfoNotification("Assignment 1 Setup", "Skin layer classes loaded and colors set!\nReady to classify skin.\nSlide: skin.ome.tif")
