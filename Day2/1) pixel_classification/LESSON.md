# Lesson Plan: Pixel Classification in QuPath

**Before running this lesson, read `README.md` in this folder for the setup instructions.**

**Duration:** 30-60 Minutes
**Goal:** Teach the concept of "Training by Example" using Random Trees pixel classification.

## Part 1: The "Why" (5 Minutes)
*   **Concept:** Why can't we just select the purple stuff?
*   **Activity:**
    1.  Open `skin.ome.tif`.
    2.  Zoom into the dark purple edge (Epidermis).
    3.  Ask students to describe the color (Purple).
    4.  Ask them to describe the Dermis (Pink).
    5.  **Conclusion:** Color alone is not enough. We need texture. We need a "Robot Eye" that looks at patterns, not just pixels.

## Part 2: The Setup (2 Minutes)
*   **Action:** Run the `01_magic_setup.groovy` script.
*   **Explain:** "I've just created our buckets: Epidermis (Red), Dermis (Green), and Whitespace (Gray). We need to teach the computer what goes in each bucket."

## Part 3: Interactive Training (15 Minutes)
*   **Step 1: Annotate**
    *   Select the **Epidermis** class.
    *   Use the **Brush Tool** to paint a few small areas of the purple outer layer.
    *   Select **Dermis** and paint some pink connective tissue.
    *   Select **Whitespace** and paint the empty glass background.
*   **Step 2: Train**
    *   Go to `Classify > Pixel classification > Train pixel classifier`.
    *   **Crucial Step:** Press **Live Prediction**.
*   **Step 3: Refine (The "Human-in-the-Loop")**
    *   Look for errors (e.g., "The computer thinks this ink mark is a tumor").
    *   **Fix it:** Select the correct class (Whitespace) and paint over the mistake.
    *   Watch the classifier update instantly.

## Part 4: Under the Hood (10 Minutes)
*   **Concept:** How does it work?
*   **Activity:** In the classifier dialog:
    *   Change **Resolution** to "Low". Observe how it gets "blobby" and fast.
    *   Change **Resolution** to "High". Observe how it gets noisy and slow.
    *   Explain **Features**: The computer isn't just seeing color; it's applying filters (Gaussian blur, Edge detection) to understand texture.

## Part 5: The Result (5 Minutes)
*   **Action:** Save the classifier (name it "My First Classifier").
*   **Action:** Run the `02_magic_report.groovy` script.
*   **Outcome:** A popup box appears: *"Epidermis Percentage: 15.2%"*.
*   **Discussion:** This number is a quantitative biomarker. It's reproducible and objective, unlike a human estimate like "looks like about 10%."
