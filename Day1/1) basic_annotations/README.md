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
    *   Drag and drop the `.groovy` files from the `scripts` folder onto the QuPath window to open a script.

## Color Deconvolution & Channels
*   The `skin.ome.tif` image is a brightfield **H&E** stain, so QuPath automatically creates multiple display channels beyond the raw RGB view (e.g., Optical Density sum, Hematoxylin, Eosin, Residual, and individual Red/Green/Blue channels).
*   Use `Image > Brightness/Contrast` or the `Channels` sidebar to toggle between those deconvolved stains when working with the Wand/Brush tools, or to focus on a single stain before collecting intensity features.
*   When you calculate features later (`Analyze > Calculate features`), those extra channels give you more options for intensity measurements (Hem vs. Eosin, OD, etc.), which can highlight specific tissue components.

## The Scripts
*   **`01_setup_basics.groovy`**: Clears the workspace to give you a fresh canvas.
*   **`02_report_basics.groovy`**: A simple helper that counts your annotations and calculates their total area, demonstrating how scripts can interact with your manual work.
