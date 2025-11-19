# QuPath Pixel Classification Teaching Kit

## Overview
This kit provides a structured lesson plan and "Magic Scripts" to teach pixel classification in QuPath. It is designed for a 30-60 minute interactive session.

## Prerequisites
1.  **QuPath v0.6.0+**: Download from [qupath.github.io](https://qupath.github.io/).
2.  **Sample Image**: This lesson uses the local file `resources/skin.ome.tif`.

## Setup Instructions
1.  **Create Project**:
    *   Open QuPath.
    *   Drag and drop an empty folder onto QuPath to create a project (or use `File > Project > Create project`).
    *   Drag the `resources/skin.ome.tif` file into the QuPath window to add it to the project.
2.  **Install Scripts**:
    *   Copy the `scripts` folder from this kit into your QuPath project folder.
    *   Alternatively, drag and drop the `.groovy` files onto the QuPath script editor (`Automate > Show script editor`).

## The "Magic" Scripts
*   **`01_magic_setup.groovy`**: Run this *before* the lesson starts (or at the very beginning). It sets up the classes (Epidermis, Dermis, Whitespace) with high-contrast colors and clears any old junk.
*   **`02_magic_report.groovy`**: Run this *after* the students have trained their classifier. It calculates the Tumor Percentage and displays it in a simple popup box, avoiding the complex data tables for beginners.
