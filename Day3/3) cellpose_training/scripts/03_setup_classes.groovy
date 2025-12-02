// *************************************************************
//  Setup Training & Validation Classes for Cellpose Training
//  
//  This script adds the required classes for Cellpose training:
//  - Training: regions containing ground truth for model training
//  - Validation: regions for testing during training (prevents overfitting)
//
//  Run this once before creating your ground truth annotations.
// *************************************************************

def pathClasses = [
    "Training",
    "Validation"
]

// Add classes if they don't already exist
def existingClasses = getQuPath().getAvailablePathClasses()
def existingNames = existingClasses.collect { it.getName() }

int added = 0
for (className in pathClasses) {
    if (!existingNames.contains(className)) {
        def newClass = getPathClass(className)
        added++
        println "Added class: ${className}"
    } else {
        println "Class already exists: ${className}"
    }
}

println "Done! ${added} new class(es) added."
println ""
println "Next steps:"
println "1. Create rectangle annotations and classify them as 'Training' or 'Validation'"
println "2. Inside each rectangle, annotate ALL cells using the Brush or Wand tool"
println "3. Run 04_train_cellpose.groovy to train your model"
