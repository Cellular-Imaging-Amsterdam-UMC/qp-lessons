# QuPath Cell Classification Teaching Kit

## Overview
This module teaches **Object Classification**. Unlike pixel classification (painting regions), here we first detect individual objects (cells) and then teach the computer to sort them into categories based on their shape and intensity.

## Prerequisites
1.  **QuPath v0.6.0+**
2.  **Sample Image**: `resources/nuclei.ome.tif` (Local file).
    *   *Description:* A DAPI-stained image showing a mix of normal nuclei (dim, smooth) and apoptotic nuclei (bright, fragmented).

## Setup Instructions
1.  **Create Project**:
    *   Open QuPath.
    *   Use `File > New Project...` (or `File > Project > Create project`) to create or open an existing project folder (preferably an empty subfolder) so QuPath keeps the annotations, detections, and script results together.
    *   Drag and drop the `resources/nuclei.ome.tif` file into the QuPath window.
    *   Set image type to "Fluorescence" if prompted.
2.  **Install Scripts**:
    *   Drag and drop the `.groovy` files from the `scripts` folder onto the QuPath window to open a script.

## The Scripts
*   **`01_setup_cells.groovy`**: Defines the "Normal" and "Apoptotic" classes.
*   **`02_report_cells.groovy`**: Calculates the Apoptotic Index (percentage of dead cells).
