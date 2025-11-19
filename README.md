"# QuPath Lessons

This repository contains a collection of teaching kits and lesson plans for learning **QuPath**, an open-source software for bioimage analysis. These lessons are designed to guide users through various image analysis workflows, from basic thresholding to advanced machine learning-based classification.

## How to Download

You can download the course materials by cloning this repository or downloading the ZIP file.

### Option 1: Clone via Git
Open your terminal or command prompt and run:
```bash
git clone https://github.com/Cellular-Imaging-Amsterdam-UMC/qp-lessons.git
```

### Option 2: Download ZIP
1. Click on the green **Code** button at the top of the repository page.
2. Select **Download ZIP**.
3. Extract the contents to a folder on your computer.

## Available Lessons

The repository is organized into sequential modules, each focusing on a specific QuPath feature:

### 1. Threshold Classification
*   **Folder**: `1) threshold_classification`
*   **Topic**: The simplest form of segmentation.
*   **What you'll learn**: How to segment objects based on pixel intensity alone. Ideal for fluorescence images or high-contrast structures.

### 2. Pixel Classification
*   **Folder**: `2) pixel_classification`
*   **Topic**: Machine learning for region segmentation.
*   **What you'll learn**: How to train a classifier to distinguish between different tissue types (e.g., Tumor vs. Stroma) by "painting" examples.

### 3. Cell Classification
*   **Folder**: `3) cell_classification`
*   **Topic**: Object-based classification.
*   **What you'll learn**: How to detect individual cells and then train a machine learning model to sort them into categories (e.g., Normal vs. Apoptotic) based on shape and intensity features.

### 4. Positive Cell Detection
*   **Folder**: `4) positive_cell_detection`
*   **Topic**: Streamlined positivity analysis.
*   **What you'll learn**: A combined workflow for detecting cells and classifying them as positive or negative in a single step (commonly used for Ki67 or other biomarkers).

## Prerequisites

*   **QuPath v0.6.0+**: Download from [qupath.github.io](https://qupath.github.io/).
*   **Sample Images**: The lessons use sample images located in the `resources/` folder." 
