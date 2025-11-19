/**
 * 01_magic_setup.groovy
 * 
 * MAGIC SETUP SCRIPT
 * ------------------
 * This script prepares the QuPath environment for the teaching session.
 * It defines the classes and assigns high-contrast colors.
 * 
 * Compatible with QuPath v0.4.0 - v0.6.0+
 */

import qupath.lib.gui.QuPathGUI
import qupath.lib.objects.classes.PathClassFactory
import javafx.scene.paint.Color

// 1. Define the classes we want to use
// We use getPathClass to ensure we are using the singleton instances
def epidermisClass = PathClassFactory.getPathClass("Epidermis")
def dermisClass = PathClassFactory.getPathClass("Dermis")
def whitespaceClass = PathClassFactory.getPathClass("Whitespace")
def ignoreClass = PathClassFactory.getPathClass("Ignore*") // Useful for artifacts

// 2. Create the list of classes
def classes = [epidermisClass, dermisClass, whitespaceClass, ignoreClass]

// 3. Set these classes to the current image
// This populates the "Annotations" tab class list
setPathClasses(classes)

// 4. Set specific colors for teaching clarity
// Epidermis = Red (High contrast)
// Dermis = Green (High contrast to red)
// Whitespace = Light Gray (Unobtrusive)
// Ignore = Yellow (Warning)
def pathClassTools = QuPathGUI.getInstance().getPathClassTools()

pathClassTools.setPathClassColor(epidermisClass, Color.rgb(255, 0, 0))      // Red
pathClassTools.setPathClassColor(dermisClass, Color.rgb(0, 200, 0))     // Green
pathClassTools.setPathClassColor(whitespaceClass, Color.rgb(200, 200, 200)) // Gray
pathClassTools.setPathClassColor(ignoreClass, Color.rgb(255, 255, 0))   // Yellow

// 5. Clean up any existing annotations to start fresh
// (Optional: Comment this out if you want to keep student work)
clearAnnotations()

// 6. Feedback
print("Magic Setup Complete!")
print("Classes defined: " + classes.collect { it.getName() })
Dialogs.showInfoNotification("Magic Setup", "Classes loaded and colors set!\nReady for training.")
