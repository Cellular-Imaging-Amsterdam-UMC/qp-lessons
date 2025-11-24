// 01_setup_basics.groovy

// SETUP SCRIPT
// ------------
// Clears any existing annotations to provide a clean slate for the lesson.

import qupath.lib.gui.QuPathGUI

// 1. Clean up
// Removes all objects (Annotations and Detections)
clearAllObjects()

// 2. Reset View (Optional)
// Resets the viewer to fit the image
// getCurrentViewer().resetView()

Dialogs.showInfoNotification("Setup", "Workspace cleared!\nReady to practice annotations.")
