// 02_report_cells.groovy

// REPORT SCRIPT
// -------------
// Counts the cells and calculates the Apoptotic Index.

import qupath.lib.gui.QuPathGUI

// Get all detection objects (cells)
def cells = getDetectionObjects()

if (cells.isEmpty()) {
    Dialogs.showErrorMessage("Report", "No cells found!\nDid you run 'Cell Detection'?")
    return
}

def normalCount = 0
def apoptoticCount = 0
def unclassifiedCount = 0

for (cell in cells) {
    def pathClass = cell.getPathClass()
    if (pathClass == null) {
        unclassifiedCount++
        continue
    }
    
    if (pathClass.getName() == "Normal") {
        normalCount++
    } else if (pathClass.getName() == "Apoptotic") {
        apoptoticCount++
    } else {
        unclassifiedCount++
    }
}

def totalClassified = normalCount + apoptoticCount

if (totalClassified == 0) {
    Dialogs.showWarningNotification("Report", "Cells found, but none are classified.\nDid you train the object classifier?")
    return
}

def apoptoticIndex = (apoptoticCount / totalClassified) * 100

def message = String.format(
    "Total Cells: %d\n\nNormal: %d\nApoptotic: %d\n\nApoptotic Index: %.1f%%", 
    cells.size(), normalCount, apoptoticCount, apoptoticIndex
)

Dialogs.showInfoNotification("Cell Analysis Results", message)
print(message)
