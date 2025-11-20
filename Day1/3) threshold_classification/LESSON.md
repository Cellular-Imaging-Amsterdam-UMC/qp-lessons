# Lesson Plan: Threshold Classification

**Before running this lesson, read `README.md` in this folder for the setup instructions.**

**Duration:** 15-20 Minutes
**Goal:** Teach how to segment objects based on pixel intensity using the "Create Thresholder" tool.

## Part 1: The Concept (5 Minutes)
*   **Question:** "How do we find the bright object in this dark image?"
*   **Concept:** Here the object is simply *brighter* than the background. We can use a simple cutoff value (Threshold) based on intensity, without needing complex machine learning.

## Part 2: The Setup (2 Minutes)
*   **Action:** Run `01_setup_threshold.groovy`.
*   **Explain:** "I've created a class called **Islet**. We want to find all pixels that belong to this class."

## Part 3: Interactive Thresholding (10 Minutes)
*   **Step 1: Open the Tool**
    *   Go to `Classify > Pixel classification > Create thresholder`.
*   **Step 2: Adjust Parameters**
    *   **Channel:** Ensure the Red channel (or the active fluorescent channel) is selected.
    *   **Threshold:** Move the slider. Observe how the yellow overlay covers the bright area.
    *   **Smoothing:** (Optional) Increase sigma to smooth the edges if the result is too noisy.
*   **Step 3: Create Objects**
    *   Once the overlay matches the islet shape, click **Create Objects**.
    *   Choose **Class: Islet**.
    *   Click **OK**.

## Part 4: The Result (3 Minutes)
*   **Action:** Run `02_report_threshold.groovy`.
*   **Outcome:** A popup box appears: *"Islet Area: X µm²"*.
*   **Discussion:** Thresholding is fast and accurate for high-contrast fluorescence, but fails if the background is uneven or the object has complex texture.
