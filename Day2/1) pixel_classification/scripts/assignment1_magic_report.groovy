// assignment1_magic_report.groovy

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

def classNames = [
    "Stratum corneum",
    "EPTZ",
    "Stratum granulosum",
    "Stratum spinosum",
    "Stratum basale",
    "Dermis"
]

def classAreas = classNames.collectEntries { [(it): 0.0] }
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
    def name = pathClass.getName()
    
    if (classAreas.containsKey(name)) {
        classAreas[name] += area
        totalArea += area
    }
}

if (totalArea == 0) {
    Dialogs.showWarningNotification("Assignment 1 Report", "Found objects, but none were skin layer classes.")
    return
}

def summary = classAreas.collect { name, area ->
    def pct = (area / totalArea) * 100
    return String.format("%s: %.2f mm² (%.1f%%)", name, area / 1_000_000, pct)
}.join("\n")

// 4. Show your magic result
def message = "Skin layer summary (Assignment 1)\nSlide: skin.ome.tif\n\n" + summary

Dialogs.showInfoNotification("Assignment 1 Report", message)
print(message)
