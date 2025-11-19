/**
 * 02_count_annotations.groovy
 * 
 * Demonstrates how to "get" data from QuPath.
 */

// 1. Get the list of all annotation objects
def annotations = getAnnotationObjects()

// 2. Count them
def count = annotations.size()

// 3. Print the result
print "Current number of annotations: " + count
