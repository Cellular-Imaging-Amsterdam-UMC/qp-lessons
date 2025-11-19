# Lesson Plan: Cell Classification (Normal vs Apoptotic)

**Duration:** 20-30 Minutes
**Goal:** Teach the workflow of Cell Detection -> Feature Extraction -> Object Classification.

## Part 1: The Concept (5 Minutes)
*   **Observation:** Look at the image.
    *   **Normal Nuclei:** Dimmer, larger, smooth oval shape.
    *   **Apoptotic Nuclei:** Very bright (condensed chromatin), smaller, fragmented.
*   **Strategy:** We can't just threshold because we want to count *individual cells*. We need to:
    1.  **Detect** every nucleus.
    2.  **Measure** their properties (Intensity, Circularity).
    3.  **Train** a classifier to tell them apart.

## Part 2: The Setup (2 Minutes)
*   **Action:** Run `01_setup_cells.groovy`.
*   **Explain:** "I've created our buckets: **Normal** (Blue) and **Apoptotic** (Red)."

## Part 3: Cell Detection (10 Minutes)
*   **Step 1: Run Detection**
    *   `Analyze > Cell detection > Cell detection`.
*   **Step 2: Tune Parameters**
    *   **Detection Image:** Select the DAPI channel.
    *   **Threshold:** Adjust until all nuclei (dim and bright) are detected.
    *   **Cell Expansion:** Set to 0 (we only care about the nucleus here).
    *   Click **Run**.
*   **Outcome:** The image is covered in yellow outlines. Every nucleus is found, but they are all "Unclassified".

## Part 4: Training the Classifier (10 Minutes)
*   **Step 1: Annotate Examples**
    *   Select the **Normal** class. Find a dim, smooth nucleus. Right-click it -> `Set class`. (Or draw a small box around it).
    *   Select the **Apoptotic** class. Find a bright, condensed nucleus. Right-click -> `Set class`.
    *   *Tip:* You need about 5-10 examples of each.
*   **Step 2: Train**
    *   `Classify > Object classification > Train object classifier`.
    *   **Classifier:** Random Trees.
    *   **Features:** Ensure "Nucleus: Mean" (Intensity) and "Nucleus: Circularity" (Shape) are selected.
    *   Click **Live Update**.
*   **Step 3: Refine**
    *   Watch the colors change. If a bright cell is blue (wrong), right-click it and set it to **Apoptotic**. The classifier learns instantly.

## Part 5: The Result (5 Minutes)
*   **Action:** Save the classifier (name it "Apoptosis Finder").
*   **Action:** Run `02_report_cells.groovy`.
*   **Outcome:** A popup box appears: *"Apoptotic Index: 12.5%"*.
