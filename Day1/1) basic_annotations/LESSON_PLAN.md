# Lesson Plan: QuPath Basics - Annotations & Measurements

**Duration:** 15-20 Minutes
**Goal:** Familiarize users with the QuPath interface, navigation, and manual annotation tools before moving to automated analysis.

## Part 1: Interface Overview (5 Minutes)
*   **The Viewer:**
    *   **Pan:** Right-click and drag.
    *   **Zoom:** Scroll wheel (or use the `+` / `-` keys).
*   **The Tabs:**
    *   **Project Tab:** Shows your file list.
    *   **Annotations Tab:** Lists all objects you draw.
    *   **Hierarchy Tab:** Shows parent/child relationships (e.g., cells inside a tissue region).

## Part 2: Annotation Tools (10 Minutes)
*   **Shapes:**
    *   Select the **Rectangle Tool** (`R`) and draw a box.
    *   Select the **Ellipse Tool** (`E`) and draw a circle.
    *   Select the **Polygon Tool** (`P`) and click points to trace a shape. Double-click to close it.
*   **The Brush Tool:**
    *   Select the **Brush Tool** (`B`).
    *   Paint a region.
    *   **Tip:** Hold `Alt` while painting to erase.
    *   **Tip:** Hold `Shift` while brushing to continue adding to (or subtracting from) the current region.
*   **The Wand Tool:**
    *   Select the **Wand Tool** (`W`).
    *   Click on a high-contrast area (like the dark purple epidermis in the skin image).
    *   Hold `Shift` to add to the current Wand selection if it already picked a region.
    *   Observe how it automatically selects the region based on color.

## Part 3: Measurements (5 Minutes)
*   **Immediate Feedback:**
    *   Select an annotation. Look at the bottom status bar to see the **Area** and **Perimeter**.
*   **The Measurement Table:**
    *   Go to `Measure > Show annotation measurements`.
    *   See the list of all your drawings and their sizes.
*   **Adding Data:**
    *   Go to `Measure > Add geometry measurements`.
    *   Select "Circularity" and "Solidity".
    *   Check the table again to see the new columns.

## Part 4: Saving (2 Minutes)
*   **Concept:** QuPath does not edit your original image file. It saves a separate `.qpdata` file.
*   **Action:** `File > Save` (or `Ctrl + S`).
