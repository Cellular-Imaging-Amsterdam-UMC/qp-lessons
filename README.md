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

The repository is organized into daily modules:

### Day 1: Basics & Positivity
*   **0. Basic Annotations** (`Day1/0) basic_annotations`)
    *   **Topic**: Interface navigation and manual tools.
    *   **What you'll learn**: How to use the Wand, Brush, and Shape tools, and how to view basic measurements.
*   **1. Threshold Classification** (`Day1/1) threshold_classification`)
    *   **Topic**: The simplest form of segmentation.
    *   **What you'll learn**: How to segment objects based on pixel intensity alone. Ideal for fluorescence images or high-contrast structures.
*   **2. Positive Cell Detection** (`Day1/2) positive_cell_detection`)
    *   **Topic**: Streamlined positivity analysis.
    *   **What you'll learn**: A combined workflow for detecting cells and classifying them as positive or negative in a single step (commonly used for Ki67 or other biomarkers).

### Day 2: Machine Learning Classification
*   **1. Pixel Classification** (`Day2/1) pixel_classification`)
    *   **Topic**: Machine learning for region segmentation.
    *   **What you'll learn**: How to train a classifier to distinguish between different tissue types (e.g., Epidermis vs. Dermis) by "painting" examples.
*   **2. Cell Classification** (`Day2/2) cell_classification`)
    *   **Topic**: Object-based classification.
    *   **What you'll learn**: How to detect individual cells and then train a machine learning model to sort them into categories (e.g., Normal vs. Apoptotic) based on shape and intensity features.

### Day 3
*   *(Coming Soon)*

## Prerequisites

* **QuPath v0.6.0+**: Download from [qupath.github.io](https://qupath.github.io/).
* **Sample Images**: The lessons use sample images located in the `resources/` folder." 
