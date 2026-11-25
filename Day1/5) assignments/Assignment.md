# Day 1 Assignment Session

**Purpose:** Apply the skills from the Day 1 lessons to two short assignments that mix manual annotations, thresholding, and positive cell detection.

Use the [course setup steps](../../README.md#before-each-lesson) to add the assignment images to a working project.

## Assignment A – Manual vs Threshold Measurement
*   **Image:** `resources/BreastGlandularCells.ome.tif` (Image type H-DAB)
*   **Objective:** Estimate the total glandular area in the tissue using two approaches.
    1.  **Manual Annotations:** Draw regions around each gland and record the combined area from the measurement table.
    2.  **Threshold-Based Detection:** Use `Classify > Pixel classification > Create thresholder` to threshold the **DAB OD** channel in the same analysis region. Try 0.25 as a starting value and adjust against the image. Use **Create objects** to make annotations. Decide whether a gland lumen should count toward area and apply that rule to both methods. If appropriate, use `Objects > Annotations > Fill holes` on the thresholded regions.
*   **Deliverable:** Plot the two area estimates with units, give their difference, and include screenshots of the outlines. Explain one reason for the difference and record the threshold so someone can repeat the comparison.

## Assignment B – Automatic Positive Cell Detection
*   **Image:** `resources/PostiveNuclei.ome.tif` (Image type Fluorescence)
*   **Objective:** Run a positive cell detection workflow to quantify positive nuclei automatically.
    1.  Run `Analyze > Cell detection > Positive cell detection`. Or the normal 'Cell detection' followed by a 'Single Measurement Classifier'.
    2.  Use `View > Brightness/Contrast` to inspect the two channels separately. Choose the channel that shows the nuclei for detection and the marker channel for scoring. In a small ROI, compare the detected outlines and positive labels with a manual count before applying the settings to the analysis region.
    3.  Record the positive percentage from the detection summary or measurement table.
*   **Deliverable:** Record the nuclear detection and marker channels, the detection and positivity thresholds, the positive and negative counts, and the final percentage. Include a screenshot that shows the detections.

## Bonus Assignment – Your Own Experiment

* **Image:** Pick any image from the main `resources/` folder.
* **Objective:** Design a mini-analysis that combines at least two tools from Day 1 (manual annotations, thresholding, positive cell detection, pixel classifiers, measurement table, etc.). Decide what biological question you want to answer (area fraction, number of positives, intensity comparison) and choose the workflow that best supports it.
    1. Briefly describe why you selected the image and what you plan to measure.
    2. Run the necessary steps (e.g., draw annotations, create a thresholder, detect cells + single measurement classifier, export measurements).
    3. Summarize the result in a short paragraph plus a screenshot or chart.
* **Deliverable:** Turn in a one-slide summary or short paragraph detailing the question, the tools you used, key parameter values, and the final measurement. Include at least one screenshot referencing the tools in action.

> **Tip:** Treat this session as self-guided exercise. You can refer back to the earlier Day 1 lessons (basic annotations, thresholding, threshold classification, positive cell detection) if you need a walkthrough of any step.
