# QuPath Thresholding Teaching Kit

## Overview
This module teaches the simplest form of segmentation: **Thresholding**. It is ideal for fluorescence images or high-contrast structures where intensity alone defines the object.

## Prerequisites
1.  **QuPath 0.7.0**
2.  **Sample Image**: `resources/Pancreatic Islet Cells of the Mouse.tif` (Local file).

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson) with `resources/Pancreatic Islet Cells of the Mouse.tif`. Set the image type to **Fluorescence**.

The setup script removes existing annotations on the open image. Run it before drawing an analysis region.

[Start the lesson](LESSON.md).

## The Scripts
*   **`01_setup_threshold.groovy`**: Defines the "Islet" class and prepares the workspace.
*   **`02_report_threshold.groovy`**: Calculates and displays the total area of the detected islet.
