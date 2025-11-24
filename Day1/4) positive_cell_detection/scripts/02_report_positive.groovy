// 02_report_positive.groovy

// REPORT SCRIPT
// -------------
// Calculates the percentage of positive cells.

import qupath.lib.gui.QuPathGUI

// Get all detection objects (cells)
def cells = getDetectionObjects()

if (cells.isEmpty()) {
    Dialogs.showErrorMessage("Report", "No cells found!\nDid you run 'Positive Cell Detection'?")
    return
}

def positiveCount = 0
def negativeCount = 0
def totalCount = 0

for (cell in cells) {
    def pathClass = cell.getPathClass()
    if (pathClass == null) continue
    
    // Check for "Positive" in the name (handles "Tumor: Positive" etc.)
    if (pathClass.getName().contains("Positive")) {
        positiveCount++
        totalCount++
    } else if (pathClass.getName().contains("Negative")) {
        negativeCount++
        totalCount++
    }
}

if (totalCount == 0) {
    Dialogs.showWarningNotification("Report", "Cells found, but none are classified as Positive/Negative.")
    return
}

def positivityIndex = (positiveCount / totalCount) * 100

def message = String.format(
    "Total Cells: %d\n\nPositive: %d\nNegative: %d\n\nPositivity: %.1f%%", 
    totalCount, positiveCount, negativeCount, positivityIndex
)

Dialogs.showInfoNotification("Positive Detection Results", message)
print(message)
