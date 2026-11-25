# QuPath Cell Classification Teaching Kit

## Overview
This module teaches **Object Classification**. Unlike pixel classification (painting regions), here we first detect individual objects (cells) and then teach the computer to sort them into categories based on their shape and intensity.

## Prerequisites
1.  **QuPath 0.7.0**
2.  **Sample Image**: `resources/nuclei.ome.tif` (Local file).
    *   *Description:* A DAPI-stained image showing a mix of normal nuclei (dim, smooth) and apoptotic nuclei (bright, fragmented).

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson) with `resources/nuclei.ome.tif`. Set the image type to **Fluorescence**.

The nucleus signal is in the single channel, labelled **Channel 1** in QuPath.

[Start the lesson](LESSON.md).

## The Scripts
*   **`01_setup_cells.groovy`**: Defines the "Normal" and "Apoptotic" classes.
*   **`02_report_cells.groovy`**: Calculates the percentage of labelled detections classified as Apoptotic.
