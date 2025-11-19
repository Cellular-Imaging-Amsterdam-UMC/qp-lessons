# Lesson Plan: Spot Detection with Spotiflow

**Duration:** 20 Minutes
**Goal:** Detect DNA repair foci within nuclei using a pre-trained deep learning spot detector.

## Part 1: The Context (5 Minutes)
*   **Image:** `NucleiDNARepairFoci.ome.tif`.
    *   **Red Channel:** Nuclei.
    *   **Green Channel:** DNA Repair Foci (Spots).
*   **Goal:** Count the number of spots *per nucleus*.

## Part 2: Parent Detection (5 Minutes)
*   **Concept:** To count "spots per cell", we first need the cells.
*   **Action:** Run `01_detect_nuclei.groovy`.
*   **Result:** Yellow outlines around the red nuclei.

## Part 3: Spotiflow (10 Minutes)
*   **Concept:** Spotiflow is a DL model trained specifically for spot detection. It is more robust to noise than standard "Fast Peak Search".
*   **Action:** Run `02_spotiflow_foci.groovy`.
*   **Parameters:**
    *   **Model:** `general` (Pre-trained model).
    *   **Channel:** Green.
*   **Result:** Small point annotations appear on the green spots.
*   **Analysis:** Look at the measurement table. Each Nucleus now has a "Num Spots" or "Children" count.

## Part 4: Visualization
*   **Tip:** `View > Show child objects` to toggle the visibility of the spots.
