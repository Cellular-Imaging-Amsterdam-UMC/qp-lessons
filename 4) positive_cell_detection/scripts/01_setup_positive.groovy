/**
 * 01_setup_positive.groovy
 * 
 * SETUP SCRIPT
 * ------------
 * Prepares the classes for Positive Cell Detection.
 */

import qupath.lib.gui.QuPathGUI
import qupath.lib.objects.classes.PathClassFactory
import javafx.scene.paint.Color

// 1. Define the classes
// QuPath has built-in "Positive" and "Negative" classes usually derived from a base
def positiveClass = PathClassFactory.getPathClass("Positive")
def negativeClass = PathClassFactory.getPathClass("Negative")

// 2. Set the classes
setPathClasses([positiveClass, negativeClass])

// 3. Set Colors
// Positive = Red (Standard convention)
// Negative = Blue (Standard convention)
def pathClassTools = QuPathGUI.getInstance().getPathClassTools()
pathClassTools.setPathClassColor(positiveClass, Color.rgb(255, 0, 0))    // Red
pathClassTools.setPathClassColor(negativeClass, Color.rgb(0, 0, 255))    // Blue

// 4. Clean up
clearAnnotations()

Dialogs.showInfoNotification("Setup", "Classes 'Positive' & 'Negative' created.\nReady for detection.")
