# QuPath Lessons

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
*   **1. Basic Annotations** (`Day1/1) basic_annotations`)
    *   **Topic**: Interface navigation and manual tools.
    *   **What you'll learn**: How to use the Wand, Brush, and Shape tools, and how to view basic measurements.
*   **2. Scripting Basics** (`Day1/2) scripting_basics`)
    *   **Topic**: Introduction to Groovy scripting.
    *   **What you'll learn**: How to write simple scripts to access objects, loop through data, and filter results. Demystifying the "Code" behind the GUI.
*   **3. Threshold Classification** (`Day1/3) threshold_classification`)
    *   **Topic**: The simplest form of segmentation.
    *   **What you'll learn**: How to segment objects based on pixel intensity alone. Ideal for fluorescence images or high-contrast structures.
*   **4. Positive Cell Detection** (`Day1/4) positive_cell_detection`)
    *   **Topic**: Streamlined positivity analysis.
    *   **What you'll learn**: A combined workflow for detecting cells and classifying them as positive or negative in a single step (commonly used for Ki67 or other biomarkers).
*   **5. Assignments** (`Day1/5) assignments`)
    *   **Topic**: Applied practice.
    *   **What you'll learn**: Measure glandular areas manually vs. thresholding and run an automatic positive cell detection workflow to reinforce Day 1 concepts.

### Day 2: Machine Learning Classification
*   **1. Pixel Classification** (`Day2/1) pixel_classification`)
    *   **Topic**: Machine learning for region segmentation.
    *   **What you'll learn**: How to train a classifier to distinguish between different tissue types (e.g., Epidermis vs. Dermis) by "painting" examples.
*   **2. Cell Classification** (`Day2/2) cell_classification`)
    *   **Topic**: Object-based classification.
    *   **What you'll learn**: How to detect individual cells and then train a machine learning model to sort them into categories (e.g., Normal vs. Apoptotic) based on shape and intensity features.

### Day 3: Advanced Deep Learning
*   **1. DL Segmentation** (`Day3/1) dl_segmentation`)
    *   **Topic**: StarDist & Cellpose.
    *   **What you'll learn**: Replacing standard watershed with AI models for robust nuclei/cell detection.
*   **2. Spot Detection** (`Day3/2) spot_detection`)
    *   **Topic**: Spotiflow.
    *   **What you'll learn**: Detecting sub-cellular signals (DNA repair foci) using deep learning.
*   **3. Cellpose Training** (`Day3/3) cellpose_training`)
    *   **Topic**: Custom Model Training.
    *   **What you'll learn**: How to annotate ground truth and train a custom Cellpose model for difficult images (Phase Contrast).

## Prerequisites

*   **QuPath 0.7.0**: The course was tested with this release. Download from [qupath.github.io](https://qupath.github.io/).
*   **Extensions**: For Day 3, you need the StarDist, Cellpose, and Spotiflow extensions installed.
*   **Python Environment**: Cellpose and Spotiflow use a configured Python environment. The StarDist scripts run in QuPath with a local model file. See [README_CELLPOSE_SPOTIFLOW.md](README_CELLPOSE_SPOTIFLOW.md) for setup instructions.
*   **Sample Images**: The lessons use sample images located in the `resources/` folder, and each lesson keeps a local `resources/` subdirectory with the files referenced in that module for easy access. Shared assignment images are in the repository-level `resources/` folder.

## Before each lesson

1. In the **Project** tab, click **Create project…** and choose an empty working folder. Use a separate project for each exercise where you want to keep separate results.
2. Click **Add images…** or drag the sample image into QuPath. Double-click its entry in the **Project** tab to open it. Set the image type specified by the lesson in the **Image** tab if needed.
3. When the lesson says to run a script, open **Automate > Script editor**, then **File > Open…** in the editor and choose the named `.groovy` file. Click **Run** in the editor. Most scripts act on the current image, so check the image and selection first. Run setup scripts that clear objects before drawing or loading work you want to keep.
4. Use **File > Save** (`Ctrl+S` on Windows) to save the current image's annotations, detections, and measurements in the project. QuPath links to the original image files, so keep them in place. Save scripts in the Script Editor and classifiers in their respective dialogs.

If you are using the course's Windows standalone installation, start it with `RunQuPath7.0.cmd` so the Day 3 scripts can find the configured models. For a separate installation, see the [Day 3 setup guide](README_CELLPOSE_SPOTIFLOW.md).

## Lesson Table of Contents
*   **Day 1**
    *   [Basic Annotations](Day1/1%29%20basic_annotations/README.md)
    *   [Scripting Basics](Day1/2%29%20scripting_basics/README.md)
    *   [Threshold Classification](Day1/3%29%20threshold_classification/README.md)
    *   [Positive Cell Detection](Day1/4%29%20positive_cell_detection/README.md)
    *   [Assignments](Day1/5%29%20assignments/Assignment.md)
*   **Day 2**
    *   [Pixel Classification](Day2/1%29%20pixel_classification/README.md)
    *   [Cell Classification](Day2/2%29%20cell_classification/README.md)
    *   [Assignments](Day2/3%29%20assignments/Assignment.md)
*   **Day 3**
    *   [DL Segmentation](Day3/1%29%20dl_segmentation/README.md)
    *   [Spot Detection](Day3/2%29%20spot_detection/README.md)
    *   [Cellpose Training](Day3/3%29%20cellpose_training/README.md)
    *   [Assignments](Day3/4%29%20assignments/Assignment.md)

Open the lesson README for the sample image and setup. Its **Start the lesson** link opens the exercises.

For detailed help and tutorials, visit the [official QuPath documentation](https://qupath.readthedocs.io/en/stable/index.html).
