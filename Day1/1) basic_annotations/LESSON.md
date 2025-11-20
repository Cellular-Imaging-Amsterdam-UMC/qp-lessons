# Lesson Plan: QuPath Basics - Annotations & Measurements

**Before running this lesson, read `README.md` in this folder for the setup instructions.**

**Duration:** 15-20 Minutes
**Goal:** Familiarize users with the QuPath interface, navigation, and manual annotation tools before moving to automated analysis.

## Part 1: Getting Oriented (2 Minutes)
*   **Image information & pixel size:**
    *   Open `Image > Show image information` to check dimensions, channel counts, and the physical pixel size before drawing annotations.
    *   Hover over tissue to watch the status bar update coordinates, pixel size, and channel intensities.
*   **Channels & display tweaks:**
    *   Use `Image > Brightness/Contrast` (SHIFT-C) to toggle stains, adjust gamma, and mute unwanted channels so the brush and wand tools focus on the structures you care about.
    *   Disabling noisy channels (e.g., residual) often makes automatic selection tools behave more reliably.
*   **Navigation shortcuts:**
    *   `Z` / `Shift+Z` zoom in and out incrementally; `F` fits the image to the window.
    *   `Home`/`End` jump to the first/last image in the project—useful when reviewing a small cohort.
## Part 2: Interface Overview (5 Minutes)
*   **The Viewer:**
    *   **Pan:** Right-click and drag.
    *   **Zoom:** Scroll wheel (or use the `+` / `-` keys).
*   **The Tabs:**
    *   **Project Tab:** Shows your file list.
    *   **Annotations Tab:** Lists all objects you draw.
    *   **Hierarchy Tab:** Shows parent/child relationships 
    *   Remember the hierarchy is a tree: annotations can contain  other annotations. Use `Hierarchy > Update hierarchy` after adding or deleting objects so the tree refreshes

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

## Part 3: Working with Annotations (5 Minutes)
*   **Quick annotation actions:**
    *   Use `Objects > Create full image annotation` or `Annotations > Create full-screen annotation` (depending on your layout) to grab the entire field of view as a reference region.
    *   Try `Objects > Merge` after drawing overlapping shapes so you keep a single combined annotation.
    *   Use `Objects > Invert selection` when you want to focus on everything except what is currently highlighted.
    *   Expand or contract an annotation with `Objects > Expand` / `Objects > Contract` so you can fine-tune boundaries without redrawing.
    *   `Objects > Fill holes` is handy when you draw a ring-shaped annotation and want to ensure the interior is included.
    *   If a region is missing a tiny sliver, use `Objects > Trim` to remove stray edges or `Objects > Grow` to cover adjacent tissue.
    *   Use `Objects > Copy` + `Objects > Paste` to duplicate a region before experimenting, then `Objects > Rename` to keep track of variant copies.
    *   Select `Objects > Delete` to remove mistakes and clean up, or `Objects > Export > Annotations` to share your drawings with colleagues.
    *   Remember you can find these commands under the `Objects` or `Annotations` menus and watch how selections react as you add, subtract, and merge regions.

## Part 4: Measurements (5 Minutes)
*   **Immediate Feedback:**
    *   Select an annotation. Look at the bottom status bar to see the **Area** and **Perimeter**.
*   **The Measurement Table:**
    *   Go to `Measure > Show annotation measurements`.
    *   Each row represents one region or object; the default columns include the annotation name, area, perimeter, and basic intensity metrics. You can change which columns appear via `Measure > Edit measurement table columns` once you start collecting more features.
    *   **Adding Data:**
    *   Go to `Analyze > Calculate features`.
    *   Check "Add intensity features" to capture statistics such as mean, median, standard deviation, and min/max for the selected channel(s) or deconvolved stains. Enable "Add shape features" to compute geometry descriptors like circularity, solidity, and Feret diameters—these complement the basic area/perimeter values.
    *   When the dialog opens, the **Preferred pixel size** field controls how finely intensity is sampled: leave it empty to use the native pixel size or enter a larger value (e.g., 2–4 µm) for faster, coarser measurements.
    *   Reopen `Measure > Show annotation measurements` after the calculation finishes to see the new columns added to each row.
*   **Exporting Results:**
    *   Use `Measure > Export > Annotations` to save the drawn objects as a QuPath annotation file, or `Measure > Export > Measurement table` to write the current table to CSV/TSV so you can inspect it in Excel or R.
    *   If you need to export measurements from multiple images at once, open the Measurement Exporter (`Automate > Measurement exporter` or use the toolbar icon) to batch-select folders, specify the columns you want, and choose a destination file. You can include linked metadata (e.g., image name, annotations counts) so downstream analyses remain traceable.

## Part 5: Saving (2 Minutes)
*   **Concept:** QuPath does not edit your original image file. It saves a separate `.qpdata` file inside the current project, and it only prompts you to save (or actually saves) when you close the project or switch to another image.
*   **Action:** `File > Save` (or `Ctrl + S`) frequently while working on an image to keep the current annotations, detections, and scripts backed up—saving is only available once a project is open, so make sure you created one.
