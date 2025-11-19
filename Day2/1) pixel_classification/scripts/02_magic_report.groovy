/**
 * 02_magic_report.groovy
 * 
 * MAGIC REPORT SCRIPT
 * -------------------
 * This script runs the pixel classification measurement and displays
 * a simplified result for teaching purposes.
 * 
 * Compatible with QuPath v0.4.0 - v0.6.0+
 */

import qupath.lib.gui.QuPathGUI
import qupath.lib.objects.classes.PathClass
import java.awt.image.BufferedImage

// 1. Check if we have a classifier
// In a real workflow, we might load a classifier here. 
// For teaching, we assume the student just trained one and it's active on screen.

def imageData = getCurrentServer().getMetadata()
def pixelSize = imageData.getPixelWidthMicrons()

// 2. Create Measurements
// This forces QuPath to calculate the area of each class currently classified
// We use "createAnnotationsFromPixelClassifier" logic but just for measurement
// Or simpler: just measure the current classification if it's live.

// However, the most robust way in a script is to ask the user to ensure the classifier is saved/active
// and then run the measurement command.

// Let's try to detect the active pixel classification results.
// If "Live Prediction" is on, we can't easily measure it without creating objects.
// So we will create objects first (Magic!).

def hierarchy = getCurrentHierarchy()

// Check if there are any annotations to classify within
if (hierarchy.getAnnotationObjects().isEmpty()) {
    // If no annotations, create one for the whole image
    createFullImageAnnotation(true)
}

// 3. Run the classification to create objects
// We assume the classifier is currently active or selected.
// If not, we prompt the user.
// Note: In v0.6.0 scripting, we often need to specify the classifier name.
// For this magic demo, we'll assume the user has "Live Prediction" on or has saved a classifier.

// SIMPLIFIED APPROACH for Teaching:
// We will just count the existing annotations if they converted them, 
// OR we will prompt them to "Create Objects" first.
// BUT, to make it "Magic", let's try to trigger the measurement on the current selection.

// Let's assume the user has run "Create Objects" as per the lesson plan.
// We will iterate through the objects and sum the areas.

def tumorArea = 0.0
def stromaArea = 0.0
def totalArea = 0.0

def annotations = getAnnotationObjects()

if (annotations.isEmpty()) {
    Dialogs.showErrorMessage("Magic Report", "No objects found!\n\nDid you remember to click 'Create Objects' in the pixel classifier?")
    return
}

for (annotation in annotations) {
    def pathClass = annotation.getPathClass()
    if (pathClass == null) continue
    
    def area = annotation.getROI().getArea() * pixelSize * pixelSize // microns squared
    
    if (pathClass.getName() == "Epidermis") {
        tumorArea += area
    } else if (pathClass.getName() == "Dermis") {
        stromaArea += area
    }
    
    // We only count tissue in the total, not whitespace
    if (pathClass.getName() == "Epidermis" || pathClass.getName() == "Dermis") {
        totalArea += area
    }
}

if (totalArea == 0) {
    Dialogs.showWarningNotification("Magic Report", "Found objects, but none were Epidermis or Dermis.")
    return
}

def tumorPercentage = (tumorArea / totalArea) * 100

// 4. Show the Magic Result
def message = String.format("Epidermis Area: %.2f mm²\nDermis Area: %.2f mm²\n\nEpidermis Percentage: %.1f%%", 
    tumorArea / 1000000, stromaArea / 1000000, tumorPercentage)

Dialogs.showInfoNotification("Magic Report Results", message)
print(message)
