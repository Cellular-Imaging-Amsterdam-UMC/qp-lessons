# Lesson Plan: Positive Cell Detection

**Duration:** 15-20 Minutes
**Goal:** Teach how to detect cells and classify them as Positive/Negative based on intensity in a single step.

## Part 1: The Concept (5 Minutes)
*   **Scenario:** "We want to count how many cells in this islet are producing insulin (Positive) vs those that are not (Negative)."
*   **Concept:** Instead of training a machine learning classifier (like in Lesson 3), we can use a simple **Intensity Threshold**. If a cell is brighter than X, it's Positive.

## Part 2: The Setup (2 Minutes)
*   **Action:** Run `01_setup_positive.groovy`.
*   **Explain:** "I've created our buckets: **Positive** (Red) and **Negative** (Blue)."

## Part 3: Running the Tool (10 Minutes)
*   **Step 1: Open the Tool**
    *   `Analyze > Cell detection > Positive cell detection`.
*   **Step 2: Configure Detection**
    *   **Detection Image:** Choose the channel that shows the nuclei (if available) or the main structure.
    *   **Requested Pixel Size:** 0.5 µm (Adjust based on image resolution).
*   **Step 3: Configure Intensity Threshold**
    *   **Score Compartment:** Choose `Cell: Mean` or `Nucleus: Mean`.
    *   **Threshold 1+:** This is the cutoff.
    *   **Visual Check:** Look at the preview. Cells above the threshold turn **Red** (Positive). Cells below turn **Blue** (Negative).
    *   Adjust the threshold slider until it accurately separates the bright cells from the dim ones.
*   **Step 4: Run**
    *   Click **Run**.

## Part 4: The Result (3 Minutes)
*   **Action:** Run `02_report_positive.groovy`.
*   **Outcome:** A popup box appears: *"Positivity: 65.4%"*.
*   **Discussion:** This is the standard method for scoring biomarkers like Ki67, ER, PR, or Insulin where the difference is purely intensity-based.
