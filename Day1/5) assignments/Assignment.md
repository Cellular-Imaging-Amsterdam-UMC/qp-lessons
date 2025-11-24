# Day 1 Assignment Session

**Purpose:** Apply the skills from the Day 1 lessons to two short assignments that mix manual annotations, thresholding, and positive cell detection.

## Assignment A – Manual vs Threshold Measurement
*   **Image:** `resources/BreastGlandularCells.ome.tif` (Image type H-DAB)
*   **Objective:** Estimate the total glandular area in the tissue using two approaches.
    1.  **Manual Annotations:** Draw regions around each gland and record the combined area from the measurement table.
    2.  **Threshold-Based Detection:** Use `Classify > Pixel classification > Create thresholder` to capture the same glands automatically and compare the area with the manual result.Be aware the DAB signal is calculated from the image with a value of max 1, so a good threshold value to start with is 0.25. Also remember to use object tools like "Fill Holes" if needed, after the create objects.
*   **Deliverable:** Report the area of the glands from both methods in a bar graph, note the difference, and describe which tool felt faster, reproducable or more accurate. And add screenshots.

## Assignment B – Automatic Positive Cell Detection
*   **Image:** `resources/PostiveNuclei.ome.tif` (Image type Fluorescence)
*   **Objective:** Run a positive cell detection workflow to quantify positive nuclei automatically.
    1.  Run `Analyze > Cell detection > Positive cell detection`. Or the normal 'Cell detection' followed by a 'Single Measurement Classifier'.
    2.  Tune the nucleus detection and positivity thresholds so the preview matches what you would count manually.
    3.  Record the positive percentage from the detection summary or measurement table.
*   **Deliverable:** Document the key parameters you used (pixel size, intensity channel, threshold) and the final positive fraction. And add screenshots.

> **Tip:** Treat this session as self-guided assignement. You can refer back to the earlier Day 1 lessons (basic annotations, thresholding, threshold classification, positive cell detection) if you need a walkthrough of any step.
