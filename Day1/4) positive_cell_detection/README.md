# QuPath Positive Cell Detection Teaching Kit

## Overview
This module teaches the **Positive Cell Detection** workflow. This is a streamlined tool in QuPath that combines cell detection and intensity thresholding in a single step. It is commonly used for calculating a "Positivity Index" (e.g., Ki67, Insulin positivity).

## Prerequisites
1.  **QuPath v0.6.0+**
2.  **Sample Image**: `resources/Pancreatic Islet Cells of the Mouse.tif` (Same as Lesson 1).

## Setup Instructions
1.  **Create Project**:
    *   Open QuPath.
    *   Use `File > New Project...` to create or open an existing project folder (ideally an empty subfolder) so QuPath tracks all your annotations and results.
    *   Drag and drop the `resources/Pancreatic Islet Cells of the Mouse.tif` file into the QuPath window.
    *   Set image type to "Fluorescence" if prompted.
2.  **Install Scripts**:
    *   Drag and drop the `.groovy` files from the `scripts` folder onto the QuPath window to open a script.

## The Scripts
*   **`01_setup_positive.groovy`**: Defines the "Positive" and "Negative" classes.
*   **`02_report_positive.groovy`**: Calculates the percentage of positive cells.
