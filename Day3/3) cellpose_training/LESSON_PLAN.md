# Lesson Plan: Training Cellpose on Phase Contrast

**Duration:** 45-60 Minutes
**Goal:** Go from raw images to a custom-trained Deep Learning model.

## Part 1: The Challenge (5 Minutes)
*   **Activity:** Open `01.jpg` (Phase Contrast).
*   **Try:** Run standard Cellpose (`cyto2`).
*   **Result:** It likely fails or produces hallucinations because it wasn't trained on this specific type of phase contrast data.

## Part 2: Creating Ground Truth (20 Minutes)
*   **Goal:** Create a high-quality dataset with separate Training and Validation sets.
*   **The Concept:**
    *   **Training Regions:** The model uses these to learn.
    *   **Validation Regions:** The model uses these to test itself during training (preventing overfitting).
*   **Requirements:**
    *   **Training Regions:** Create at least **16** rectangles.
    *   **Validation Regions:** Create at least **6** rectangles.
    *   **Distribution:** Spread these across the series of images (e.g., `01.jpg` to `20.jpg`). Don't put them all on one image.
*   **Action:**
    1.  **Define Classes:** Create three classes: **Cell**, **Training**, and **Validation**.
    2.  **Draw Region:** Open an image (e.g., `01.jpg`). Draw a **Rectangle** around a cluster of cells.
    3.  **Classify Region:** Set the rectangle's class to **Training** (or **Validation**).
    4.  **Annotate Cells:**
        *   **Crucial:** Inside this rectangle, you **MUST** annotate **EVERY** single cell.
        *   Use the **Wand** or **Brush** tool.
        *   If you miss a cell inside the rectangle, the AI will learn that "this cell is background" (False Negative), which confuses the model.
    5.  **Classify Cells:** Set all these cell annotations to class **Cell**.
    6.  **Repeat:** Move to the next image and repeat until you have **16 Training** and **6 Validation** rectangles total.

## Part 3: Training (15 Minutes)
*   **Action:** Run `02_train_cellpose.groovy`.
*   **What happens:**
    *   QuPath exports your images and annotations.
    *   Cellpose trains a new model to minimize the error between its guess and your drawing.
    *   The model file is saved in the project folder.

## Part 4: Inference (10 Minutes)
*   **Action:** Open `04.jpg` (an image the AI hasn't seen).
*   **Action:** Run `03_run_custom_model.groovy`.
*   **Result:** The model should now detect the phase contrast cells much better than the stock `cyto2` model.
