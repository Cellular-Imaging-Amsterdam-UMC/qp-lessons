// 03_filter_by_area.groovy

// Demonstrates Loops and If-Statements.

def annotations = getAnnotationObjects()

print "Checking " + annotations.size() + " annotations..."

// Loop through every annotation in the list
for (annotation in annotations) {
    
    // Get the area (in pixels, unless calibrated)
    def area = annotation.getROI().getArea()
    
    // Check if it is "large" (e.g., > 5000 pixels)
    if (area > 5000) {
        print "Found a large object! Area: " + area
    }
}

print "Done."
