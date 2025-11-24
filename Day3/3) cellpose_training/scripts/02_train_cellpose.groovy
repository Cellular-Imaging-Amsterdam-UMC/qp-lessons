// 02_train_cellpose.groovy

// Trains a custom Cellpose model using the annotations in the project.

import qupath.ext.biop.cellpose.Cellpose2D

// 1. Define training parameters
def cellpose = Cellpose2D.builder("cyto2") // Start from cyto2 weights (transfer learning)
    .channels("Red", "Green", "Blue") // Use RGB for JPEGs
    .epochs(100)           // Number of training rounds
    .learningRate(0.1)     // How fast it learns
    .batchSize(8)
    .modelName("my_phase_model") // Name of the new model
    //.valDirectory("Validation") // Optional: If you want to enforce specific validation objects, though usually handled by class names or random split
    .build()

// 2. Train
// The extension will look for annotations.
// It treats objects classified as "Validation" as the validation set.
// It treats objects classified as "Training" (or unclassified regions) as the training set.
def result = cellpose.train()

print("Training complete!")
print("Model saved at: " + result.getModelPath())
