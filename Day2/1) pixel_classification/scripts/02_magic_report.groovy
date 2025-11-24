// 02_magic_report.groovy

// MAGIC REPORT SCRIPT
// -------------------
// This script grabs the areas from your current pixel classification
// and shows a short summary so you can focus on the interpretation.

// Compatible with QuPath v0.4.0 - v0.6.0+

import qupath.lib.gui.QuPathGUI
import qupath.lib.objects.classes.PathClass
import java.awt.image.BufferedImage

// 1. Check if you have an active classifier
// Normally you would load one, but here we assume you just trained it
// and the prediction overlay is visible on screen.

def imageData = getCurrentServer().getMetadata()
def pixelSize = imageData.getPixelWidthMicrons()

// 2. Create measurements
// QuPath needs geometric objects to calculate class areas.
// If you only have Live Prediction on, we first ensure there's an annotation
// so Create Objects can run and produce measurements.

def hierarchy = getCurrentHierarchy()

// Check if you already have annotations to classify within
if (hierarchy.getAnnotationObjects().isEmpty()) {
    // If not, draw a full-image annotation so Create Objects can work
    createFullImageAnnotation(true)
}

// 3. Measure your classified regions
// At this point you should have run "Create Objects" from the pixel classifier.
// We loop through those objects and sum the epidermis/dermis areas.

def epidermisArea = 0.0
def dermisArea = 0.0
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
        epidermisArea += area
    } else if (pathClass.getName() == "Dermis") {
        dermisArea += area
    }
    
    // Only include tissue in the total, ignore whitespace
    if (pathClass.getName() == "Epidermis" || pathClass.getName() == "Dermis") {
        totalArea += area
    }
}

if (totalArea == 0) {
    Dialogs.showWarningNotification("Magic Report", "Found objects, but none were Epidermis or Dermis.")
    return
}

def epidermisPercentage = (epidermisArea / totalArea) * 100

// 4. Show your magic result
def message = String.format("Epidermis Area: %.2f mm²\nDermis Area: %.2f mm²\n\nEpidermis Percentage: %.1f%%", 
    epidermisArea / 1000000, dermisArea / 1000000, epidermisPercentage)

Dialogs.showInfoNotification("Magic Report Results", message)
print(message)
