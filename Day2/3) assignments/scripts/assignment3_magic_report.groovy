// assignment2_magic_report.groovy
// Summarizes Dividing vs Non-dividing cell counts for Assignment 2

import qupath.lib.gui.dialogs.Dialogs

def detections = getDetectionObjects()
if (detections.isEmpty()) {
    Dialogs.showWarningNotification("Assignment 2 Report", "No detections found. Run Cell detection first.")
    return
}

def targetClasses = ["Dividing", "Non-dividing"]
def counts = targetClasses.collectEntries { [(it): 0] }

def total = 0
for (detection in detections) {
    def pathClass = detection.getPathClass()
    if (pathClass == null) continue
    def name = pathClass.getName()
    if (counts.containsKey(name)) {
        counts[name] = counts[name] + 1
        total++
    }
}

if (total == 0) {
    Dialogs.showWarningNotification("Assignment 2 Report", "Detections exist, but none are labeled as Dividing or Non-dividing.")
    return
}

def summary = counts.collect { name, value ->
    def pct = (value / total) * 100
    return String.format("%s: %d cells (%.1f%%)", name, value, pct)
}.join("\n")

def message = "CellsNuclei summary\nSlide: CellsNuclei.ome.tif\nTotal labeled cells: ${total}\n\n${summary}"
Dialogs.showInfoNotification("Assignment 2 Report", message)
print(message)
