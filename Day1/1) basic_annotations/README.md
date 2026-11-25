# QuPath Basics: Annotations & Measurements

## Overview
This introductory module covers the fundamental skills needed to use QuPath: navigating the interface, drawing manual annotations, and extracting basic measurements.

## Prerequisites
1.  **QuPath 0.7.0**
2.  **Sample Image**: `resources/skin.ome.tif` (Local file).
    *   *Why this image?* It has distinct regions (Epidermis/Dermis) that are perfect for practicing with the Wand and Brush tools.

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson) with `resources/skin.ome.tif`. Set the image type to **Brightfield (H&E)**.

The setup script is optional. It clears all objects on the open image, so use it only when starting over.

[Start the lesson](LESSON.md). [Watch the instruction video](LESSON.md#instruction-video).

## Color Deconvolution & Channels
*   The `skin.ome.tif` image is a brightfield **H&E** stain, so QuPath automatically creates multiple display channels beyond the raw RGB view (e.g., Optical Density sum, Hematoxylin, Eosin, Residual, and individual Red/Green/Blue channels).
*   Use `View > Brightness/Contrast` to toggle between those deconvolved stains when working with the Wand/Brush tools, or to focus on a single stain before collecting intensity features.
*   When you calculate features later (`Analyze > Calculate features`), those extra channels give you more options for intensity measurements (hematoxylin, eosin, or optical density sum), which can highlight specific tissue components.

## The Scripts
*   **`01_setup_basics.groovy`**: Clears the workspace to give you a fresh canvas.
*   **`02_report_basics.groovy`**: A simple helper that counts your annotations and calculates their total area, demonstrating how scripts can interact with your manual work.
