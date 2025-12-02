# QuPath Cellpose Training Teaching Kit

## Overview
This module teaches how to **Train a Custom Cellpose Model**. Sometimes pre-trained models (cyto3, nuclei) fail on specific modalities like Phase Contrast. In this lesson, we will first test pre-trained models, then annotate ground truth and train a custom model to segment phase contrast cells.

## Technical Prerequisites (CRITICAL)
1.  **QuPath Cellpose Extension**: Installed and configured.
2.  **QuPath StarDist Extension**: Installed and configured (for comparison).
3.  **GPU Support**: Training on a CPU is slow. A CUDA-capable GPU is highly recommended.
4.  **Training Data**: The `resources/PhaseContrast/` folder contains 30 JPEG images of phase contrast cells.

## Setup Instructions
1.  **Create a New Project**:
    *   Open QuPath.
    *   Go to `File > Project > Create project`.
    *   Choose or create a new empty folder for this lesson.
    *   Click `Create`.
2.  **Add All 30 Images**:
    *   Go to `File > Project > Add images`.
    *   Navigate to the `resources/PhaseContrast/` folder.
    *   Select all 30 JPG images (`01.jpg` through `30.jpg`).
    *   Click `Import` to add them to the project.
3.  **Install Scripts**:
    *   Drag and drop the `.groovy` files from the `scripts` folder onto the QuPath window to open them.
4.  **Calibrate Images**:
    *   Run `01_calibrate_images.groovy` to set the pixel size for all images (default: 0.2 µm/pixel).
    *   This is required because JPG images don't have embedded calibration metadata.

## The Scripts
*   **`01_calibrate_images.groovy`**: Sets pixel calibration for all images in the project.
*   **`02a_test_cellpose.groovy`**: Tests pre-trained Cellpose models on phase contrast images (demonstrates the problem).
*   **`02b_test_stardist.groovy`**: Tests pre-trained StarDist models on phase contrast images (demonstrates the problem).
*   **`04_train_cellpose.groovy`**: Trains a custom Cellpose model using your annotations.
*   **`05_run_custom_cellpose_model.groovy`**: Runs your custom-trained model on new images.
