# Day 1 Assignment Session

**Purpose:** Apply the skills from the Day 1 lessons to two short assignments that mix manual annotations, thresholding, and positive cell detection.

## Assignment A – Manual vs Threshold Measurement
*   **Image:** `resources/BreastGlandularCells.ome.tif`
*   **Objective:** Estimate the total glandular area in the tissue using two approaches.
    1.  **Manual Annotations:** Draw regions around each gland and record the combined area from the measurement table.
    2.  **Threshold-Based Detection:** Use `Classify > Pixel classification > Create thresholder` (or a similar intensity-based tool) to capture the same glands automatically and compare the area with the manual result.
*   **Deliverable:** Report the area from both methods, note the difference, and describe which tool felt faster or more accurate.

## Assignment B – Automatic Positive Cell Detection
*   **Image:** `resources/PostiveNuclei.ome.tif`
*   **Objective:** Run a positive cell detection workflow to quantify positive nuclei automatically.
    1.  Run `Analyze > Cell detection > Positive cell detection`.
    2.  Tune the nucleus detection and positivity thresholds so the preview matches what you would count manually.
    3.  Record the positive percentage from the detection summary or measurement table.
*   **Deliverable:** Document the key parameters you used (pixel size, intensity channel, threshold) and the final positive fraction.

> **Tip:** Treat this session as self-guided practice. You can refer back to the earlier Day 1 lessons (basic annotations, thresholding, positive cell detection) if you need a walkthrough of any step.
