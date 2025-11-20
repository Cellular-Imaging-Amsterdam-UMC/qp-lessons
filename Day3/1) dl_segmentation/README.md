# QuPath Deep Learning Segmentation Teaching Kit

## Overview
This module introduces **Deep Learning-based segmentation** using **StarDist** and **Cellpose**. These tools often outperform standard watershed detection, especially in crowded or noisy images.

## Technical Prerequisites (CRITICAL)
Before running this lesson, you **MUST** have the following installed and configured:
1.  **QuPath Extensions**:
    *   [QuPath StarDist Extension](https://github.com/qupath/qupath-extension-stardist)
    *   [QuPath Cellpose Extension](https://github.com/BIOP/qupath-extension-cellpose)
2.  **Python Environment**:
    *   A Python environment (conda or venv) with `stardist`, `cellpose`, and `pytorch` installed.
    *   QuPath must be pointed to this environment via `Edit > Preferences > Cellpose/StarDist`.

## Setup Instructions
1.  **Create Project**:
    *   Open QuPath.
    *   Create or open a project (`File > New Project...` or `File > Project > Open project`) pointed at an empty folder so QuPath saves all detection results and annotations in that workspace.
    *   Drag and drop `resources/nuclei.ome.tif` into the viewer.
    *   Set image type to "Fluorescence".
2.  **Install Scripts**:
    *   Drag and drop the `.groovy` files from the `scripts` folder onto the QuPath window to open a script.

## The Scripts
*   **`01_stardist_nuclei.groovy`**: Runs the StarDist 2D model to detect nuclei.
*   **`02_cellpose_nuclei.groovy`**: Runs Cellpose (using the 'cyto2' or 'nuclei' model) to segment cells.
