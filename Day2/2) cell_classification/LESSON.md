# Lesson Guide: Cell Classification (Normal vs Apoptotic)

**Before you start, read `README.md` in this folder for the setup instructions.**

**Duration:** 20-30 Minutes
**Goal:** Practice the workflow of Cell Detection → Feature Extraction → Object Classification.

## Part 1: The Concept (5 Minutes)
*   **Observe the slide:**
    *   **Normal nuclei** look dimmer, larger, and smooth/oval.
    *   **Apoptotic nuclei** glow brightly because the chromatin is condensed; they appear smaller or fragmented.
*   **Strategy:** Simple thresholding would lump cells together. Instead you will:
    1.  **Detect** every nucleus.
    2.  **Measure** intensity and shape features.
    3.  **Train** a classifier to separate the two phenotypes.

## Part 2: The Setup (2 Minutes)
*   **Run** `01_setup_cells.groovy`.
*   **What you get:** Two classes—**Normal** (blue) and **Apoptotic** (red)—so you can start labeling immediately.

## Part 3: Cell Detection (10 Minutes)
*   **Step 1: Run detection**
    *   Choose `Analyze > Cell detection > Cell detection`.
*   **Step 2: Tune parameters**
    *   **Detection image:** Select the DAPI channel.
    *   **Threshold:** Adjust until both dim and bright nuclei are detected.
    *   **Cell expansion:** Set to 5.
    *   Click **Run**.
*   **Outcome:** Yellow outlines should cover every nucleus. They start as "Unclassified" objects that you'll label next.
*   **Note on object type:** Cell Detection creates the special **Cell** object type—essentially a paired nucleus ROI with an optional expanded cytoplasm ROI—so QuPath can store both nuclear and cellular measurements together (see the [object types overview](https://qupath.readthedocs.io/en/stable/docs/concepts/objects.html#types-of-object)).

<p align="center"><a href="screenshots/detectcells.png"><img src="detectcells/Hierarchy.png" width="60%" alt="Detect Cells"></a></p>

## Part 4: Training the Classifier (10 Minutes)
*   **Step 1: Annotate examples**
    *   Pick the **Normal** class, right-click a dim smooth nucleus, and choose `Set class` (or draw a tiny ROI around it).
    *   Switch to **Apoptotic**, right-click a bright condensed nucleus, and set its class.
    *   Aim for 5–10 examples per class before training.
*   **Step 2: Train**
    *   Open `Classify > Object classification > Train object classifier`.
    *   **Classifier:** Keep Random Trees.
    *   **Features:** Ensure "Nucleus: Mean" (intensity) and "Nucleus: Circularity" (shape) are enabled.
    *   Click **Live update** to see instant feedback.
*   **Step 3: Refine**
    *   Check the overlay. If an apoptotic nucleus shows up blue, right-click it and reset the class to **Apoptotic**.
    *   Continue correcting mistakes until the colors match what you see.

## Part 5: The Result (5 Minutes)
*   **Save** the classifier ("Apoptosis Finder" is a good name) so you can reuse it later.
*   **Run** `02_report_cells.groovy`.
*   **Read the popup:** QuPath reports something like *"Apoptotic Index: 12.5%"*—a reproducible biomarker instead of a guess.
