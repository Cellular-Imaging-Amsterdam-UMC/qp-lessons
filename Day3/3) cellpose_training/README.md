# QuPath Cellpose Training Teaching Kit

## Overview
This module teaches how to **Train a Custom Cellpose Model**. Sometimes pre-trained models (cyto3, nuclei) fail on specific modalities like Phase Contrast. In this lesson, we will first test pre-trained models, then annotate ground truth and train a custom model to segment phase contrast cells.

## Technical Prerequisites

*   Use the course standalone launcher or follow the [Day 3 setup guide](../../README_CELLPOSE_SPOTIFLOW.md) for the Cellpose Python environment, the StarDist extension, and the local model files. A CUDA-capable GPU can shorten training. CPU training also works and takes longer.
*   This lesson uses the 30 JPEG files in `resources/PhaseContrast/`.

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson). Create a project containing only the 30 phase-contrast JPGs (`01.jpg`–`30.jpg`). Keep `21.jpg`–`30.jpg` for testing. The calibration script in the lesson updates all images in the current project.

[Start the lesson](LESSON.md).

## Optional annotated example

The [nuclei training example](../../resources/QP-ML-Train-Nuclei-Example/QP/project.qpproj) is a separate QuPath project with fluorescence images and example annotations. Open this project file in QuPath from your downloaded course folder. In QuPath 0.7, click **Apply changes** if the **Update URIs** dialog appears. QuPath proposes all 25 image locations automatically when the TIFF files are kept in the example folder directly above `QP`.

## The Scripts
*   **`01_calibrate_images.groovy`**: Sets pixel calibration for all images in the project.
*   **`02a_test_cellpose.groovy`**: Tests pre-trained Cellpose models on phase contrast images (demonstrates the problem).
*   **`02b_test_stardist.groovy`**: Tests pre-trained StarDist models on phase contrast images (demonstrates the problem).
*   **`03_setup_classes.groovy`**: Adds the Training and Validation region classes.
*   **`04_train_cellpose.groovy`**: Trains a custom Cellpose model using your annotations.
*   **`05_run_custom_cellpose_model.groovy`**: Runs your custom-trained model on new images.
