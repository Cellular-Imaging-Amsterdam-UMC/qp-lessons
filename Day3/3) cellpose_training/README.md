# QuPath Cellpose Training Teaching Kit

## Overview
This module teaches how to **Train a Custom Cellpose Model**. Sometimes pre-trained models (cyto2, nuclei) fail on specific modalities like Phase Contrast. In this lesson, we will annotate ground truth and train a model to segment phase contrast cells.

## Technical Prerequisites (CRITICAL)
1.  **QuPath Cellpose Extension**: Installed and configured.
2.  **GPU Support**: Training on a CPU is extremely slow. A CUDA-capable GPU is highly recommended.
3.  **Training Data**: The `resources/PhaseContrast/` folder contains 30 JPEG images.

## Setup Instructions
1.  **Create Project**:
    *   Open QuPath and use `File > New Project...` to create (or `File > Project > Open project` to reuse) a folder for this lesson; the project folder is where annotations, training data, and scripts are stored, so start here before importing images.
    *   Import all images from `resources/PhaseContrast/`.
2.  **Install Scripts**:
    *   Drag the `.groovy` files from `scripts/` into the Script Editor.

## The Workflow
1.  **Annotate**: Draw "Ground Truth" on a few images.
2.  **Train**: Run the training script to create a custom model.
3.  **Predict**: Use the new model on the remaining images.
