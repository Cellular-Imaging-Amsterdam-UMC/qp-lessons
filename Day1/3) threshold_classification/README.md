# QuPath Thresholding Teaching Kit

## Overview
This module teaches the simplest form of segmentation: **Thresholding**. It is ideal for fluorescence images or high-contrast structures where intensity alone defines the object.

## Prerequisites
1.  **QuPath v0.6.0+**
2.  **Sample Image**: `resources/Pancreatic Islet Cells of the Mouse.tif` (Local file).

## Setup Instructions
1.  **Create Project**:
    *   Open QuPath.
    *   Use `File > New Project...` to create or open an existing project folder (ideally an empty subfolder) so QuPath saves your annotations and scripts there.
    *   Drag and drop the `resources/Pancreatic Islet Cells of the Mouse.tif` file into the QuPath window.
    *   (Optional) If asked, set the image type to "Fluorescence".
2.  **Install Scripts**:
    *   Drag and drop the `.groovy` files from the `scripts` folder onto the QuPath window to open a script.

## The Scripts
*   **`01_setup_threshold.groovy`**: Defines the "Islet" class and prepares the workspace.
*   **`02_report_threshold.groovy`**: Calculates and displays the total area of the detected islet.
