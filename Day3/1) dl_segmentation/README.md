# QuPath Deep Learning Segmentation Teaching Kit

## Overview
This module introduces **Deep Learning-based segmentation** using **StarDist** and **Cellpose**. These tools often outperform standard watershed detection, especially in crowded or noisy images.

## Technical Prerequisites

*   Install the [StarDist extension](https://github.com/qupath/qupath-extension-stardist) and [Cellpose extension](https://github.com/BIOP/qupath-extension-cellpose).
*   StarDist uses the local `dsb2018_heavy_augment.pb` model in QuPath. Cellpose uses a configured Python environment and a local model such as `nucleitorch_0` or `cyto3`.
*   Start the course standalone installation with `RunQuPath7.0.cmd`, or follow the [Day 3 setup guide](../../README_CELLPOSE_SPOTIFLOW.md) to configure your own installation.

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson) with `resources/nuclei.ome.tif`. Set the image type to **Fluorescence**.

Use a project for Cellpose. Keep the same parent annotation for each segmentation so the results cover the same area.

[Start the lesson](LESSON.md).

## The Scripts
*   **`01_stardist_nuclei.groovy`**: Runs the StarDist 2D model to detect nuclei.
*   **`02_cellpose.groovy`**: Runs Cellpose (using models like `cyto3` or `nucleitorch_0`) to segment cells or nuclei. Supports the Cellpose channel convention: `chan` for the primary segmentation channel and `chan2` as an optional nuclear channel for cyto models.
