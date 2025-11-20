# Lesson Plan: Deep Learning Segmentation

**Before running this lesson, read `README.md` in this folder for the setup instructions.**

**Duration:** 30 Minutes
**Goal:** Compare standard watershed detection with AI-based methods (StarDist & Cellpose).

## Part 1: The Problem (5 Minutes)
*   **Activity:** Run standard `Analyze > Cell detection > Cell detection` on `nuclei.ome.tif`.
*   **Observation:** Notice how it might split large nuclei or merge touching ones. It requires careful parameter tuning (Threshold, Sigma).
*   **Solution:** Deep Learning models "look" at the shape and context, often working out-of-the-box without tuning.

## Part 2: StarDist (10 Minutes)
*   **Concept:** StarDist predicts star-convex polygons. It's incredibly fast and accurate for roundish objects (nuclei).
*   **Action:** Run `01_stardist_nuclei.groovy`.
*   **Discussion:**
    *   Did it miss anything?
    *   Look at the "Probability" threshold in the script.

## Part 3: Cellpose (10 Minutes)
*   **Concept:** Cellpose is a generalist algorithm that works on a wide variety of shapes (not just stars). It's slower but often more robust for irregular shapes.
*   **Action:** Run `02_cellpose_nuclei.groovy`.
*   **Comparison:** Toggle between the StarDist and Cellpose detections. Which one handled the "Apoptotic" (fragmented) nuclei better?
