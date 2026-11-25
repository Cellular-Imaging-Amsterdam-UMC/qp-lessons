# QuPath Positive Cell Detection Teaching Kit

## Overview
This module teaches the **Positive Cell Detection** workflow. This is a streamlined tool in QuPath that combines cell detection and intensity thresholding in a single step. It is commonly used for calculating a "Positivity Index" (e.g., Ki67, Insulin positivity).

## Prerequisites
1.  **QuPath 0.7.0**
2.  **Sample Image**: `resources/Pancreatic Islet Cells of the Mouse.tif` (Same as Lesson 3).

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson) with `resources/Pancreatic Islet Cells of the Mouse.tif`. Set the image type to **Fluorescence**.

Use a fresh image entry for this lesson so the islet annotations from the thresholding exercise are easy to keep separate.

[Start the lesson](LESSON.md). [Watch the instruction video](LESSON.md#instruction-video).

## The Scripts
*   **`01_setup_positive.groovy`**: Defines the "Positive" and "Negative" classes.
*   **`02_report_positive.groovy`**: Calculates the percentage of positive cells.
