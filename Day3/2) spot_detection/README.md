# QuPath Spot Detection Teaching Kit (Spotiflow)

## Overview
This module teaches sub-cellular detection using **Spotiflow**, a deep learning method for detecting small, dense spots (like DNA repair foci, FISH signals, or RNA scopes).

## Technical Prerequisites (CRITICAL)
1.  **QuPath Spotiflow Extension**: [GitHub Link](https://github.com/weigertlab/qupath-extension-spotiflow)
2.  **Python Environment**: Must have `spotiflow` installed.

## Setup Instructions
1.  **Create Project**:
    *   Open QuPath.
    *   Drag and drop `resources/NucleiDNARepairFoci.ome.tif`.
    *   Set image type to "Fluorescence".
2.  **Install Scripts**:
    *   Drag the `.groovy` files from `scripts/` into the Script Editor.

## The Scripts
*   **`01_detect_nuclei.groovy`**: Detects the nuclei first (Parent objects).
*   **`02_spotiflow_foci.groovy`**: Runs Spotiflow to find green foci *inside* the nuclei.
