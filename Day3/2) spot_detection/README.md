# QuPath Spot Detection Teaching Kit (Spotiflow)

## Overview
This module teaches sub-cellular detection using **Spotiflow**, a deep learning method for detecting small, dense spots (like DNA repair foci, FISH signals, or RNA scopes).

## Technical Prerequisites

*   The [StarDist extension](https://github.com/qupath/qupath-extension-stardist) and the local `dsb2018_heavy_augment.pb` model are used to find nuclei.
*   The [Spotiflow extension](https://github.com/BIOP/qupath-extension-spotiflow) supplies the configured Python interpreter. This lesson uses Spotiflow 0.6.5 and the `general` pretrained model.
*   Use the course standalone launcher or follow the [Day 3 setup guide](../../README_CELLPOSE_SPOTIFLOW.md) for a separate installation.

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson) with `resources/NucleiDNARepairFoci.ome.tif`. Set the image type to **Fluorescence**.

Start with a small representative region so you can check the nucleus outlines and focus detections quickly.

[Start the lesson](LESSON.md).
