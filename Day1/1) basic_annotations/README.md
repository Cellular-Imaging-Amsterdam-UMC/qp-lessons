# QuPath Basics: Annotations & Measurements

## Overview
This introductory module covers the fundamental skills needed to use QuPath: navigating the interface, drawing manual annotations, and extracting basic measurements.

## Prerequisites
1.  **QuPath v0.6.0+**
2.  **Sample Image**: `resources/skin.ome.tif` (Local file).
    *   *Why this image?* It has distinct regions (Epidermis/Dermis) that are perfect for practicing with the Wand and Brush tools.

## Setup Instructions
1.  **Create Project**:
    *   Open QuPath.
    *   Use `File > New Project...` to create or open an existing project folder (usually an empty subfolder) so QuPath has a workspace to store annotations, scripts, and results before importing images.
    *   Drag and drop the `resources/skin.ome.tif` file into the QuPath window.
    *   (Optional) If asked, set the image type to "Brightfield (H&E)".
2.  **Install Scripts**:
    *   Drag and drop the `.groovy` files from the `scripts` folder onto the QuPath script editor.

## The Scripts
*   **`01_setup_basics.groovy`**: Clears the workspace to give you a fresh canvas.
*   **`02_report_basics.groovy`**: A simple helper that counts your annotations and calculates their total area, demonstrating how scripts can interact with your manual work.
