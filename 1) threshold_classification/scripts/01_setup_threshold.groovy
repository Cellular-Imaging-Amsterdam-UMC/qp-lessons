/**
 * 01_setup_threshold.groovy
 * 
 * SETUP SCRIPT
 * ------------
 * Prepares the classes for the Thresholding lesson.
 */

import qupath.lib.gui.QuPathGUI
import qupath.lib.objects.classes.PathClassFactory
import javafx.scene.paint.Color

// 1. Define the class
def isletClass = PathClassFactory.getPathClass("Islet")

// 2. Set the class
setPathClasses([isletClass])

// 3. Set Color (Yellow for visibility on Red background)
def pathClassTools = QuPathGUI.getInstance().getPathClassTools()
pathClassTools.setPathClassColor(isletClass, Color.rgb(255, 255, 0)) // Yellow

// 4. Clean up
clearAnnotations()

Dialogs.showInfoNotification("Setup", "Class 'Islet' created.\nReady for thresholding.")
