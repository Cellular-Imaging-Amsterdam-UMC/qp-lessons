/**
 * 01_setup_cells.groovy
 * 
 * SETUP SCRIPT
 * ------------
 * Prepares the classes for the Cell Classification lesson.
 */

import qupath.lib.gui.QuPathGUI
import qupath.lib.objects.classes.PathClassFactory
import javafx.scene.paint.Color

// 1. Define the classes
def normalClass = PathClassFactory.getPathClass("Normal")
def apoptoticClass = PathClassFactory.getPathClass("Apoptotic")
def ignoreClass = PathClassFactory.getPathClass("Ignore*")

// 2. Set the classes
setPathClasses([normalClass, apoptoticClass, ignoreClass])

// 3. Set Colors
// Normal = Blue (Standard DAPI look)
// Apoptotic = Red (Danger/Death)
def pathClassTools = QuPathGUI.getInstance().getPathClassTools()
pathClassTools.setPathClassColor(normalClass, Color.rgb(100, 100, 255)) // Light Blue
pathClassTools.setPathClassColor(apoptoticClass, Color.rgb(255, 0, 0))  // Red
pathClassTools.setPathClassColor(ignoreClass, Color.rgb(255, 255, 0))   // Yellow

// 4. Clean up
clearAnnotations()

Dialogs.showInfoNotification("Setup", "Classes 'Normal' & 'Apoptotic' created.\nReady for cell detection.")
