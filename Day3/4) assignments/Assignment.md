# Day 3 Assignment Session: Custom Deep Learning Training

**Purpose:** Apply the skills from the Day 3 Cellpose training lesson to explore different training scenarios and compare model performance.

## Assignment A – Train on Alternate Objects

*   **Context:** In Lesson 3 (cellpose_training), you trained a model on one type of object (either Nuclei or Nucleoli in the phase contrast images).
*   **Objective:** Train a new model on the **other** object type that you didn't use in the lesson.

    1.  **Create a New Project:** Start fresh with a new QuPath project using the same `resources/PhaseContrast/` images.
    2.  **Calibrate Images:** Run `01_calibrate_images.groovy` to set the pixel size.
    3.  **Create Ground Truth:** Create Training (at least 6) and Validation (at least 2) regions containing **dense annotations** of the alternate object type.
    4.  **Train the Model:** Run `04_train_cellpose.groovy` with:
        *   Base model: `None (train from scratch)`
        *   Epochs: **50**
        *   Diameter: Set appropriately for the object size (Nucleoli are much smaller than Nuclei!)
    5.  **Validate:** Run `05_run_custom_cellpose_model.groovy` on an unseen image and evaluate the results.

*   **Deliverable:** Screenshot of the detection result, the model parameters you used, and a short note comparing the difficulty of annotating this object type vs. the one from the lesson.

## Assignment B – Transfer Learning vs Training from Scratch

*   **Objective:** Compare training from scratch to transfer learning from a pretrained model.

    > ⚠️ **Warning:** Transfer learning from pretrained Cellpose models (like `cyto3` or `nuclei`) on phase contrast data may produce poor or no results, since these models were trained on very different image types. This is expected behavior!

    1.  **Use the Same Ground Truth:** Re-use the Training and Validation annotations from Assignment A (or from Lesson 3).
    2.  **Train with Transfer Learning:** Run `04_train_cellpose.groovy` with:
        *   Base model: Choose a pretrained model (e.g., `cyto3` or `nuclei`)
        *   Epochs: **50** (same as Assignment A for fair comparison)
        *   Same diameter and channel settings as before
    3.  **Compare Results:** Run `05_run_custom_cellpose_model.groovy` with both models on the same test image.

*   **Deliverable:** Side-by-side screenshots comparing detection results from:
    1.  Your "from scratch" model (Assignment A)
    2.  Your transfer learning model
    
    Include a short paragraph explaining which performed better and why you think that is.

## Assignment C – Effect of Training Epochs

*   **Objective:** Investigate how the number of training epochs affects model quality.

    1.  **Train with Fewer Epochs:** Using the same ground truth, train a model with only **50 epochs** (if you used 100 in the lesson, or vice versa).
    2.  **Compare to Original:** Compare detection results between models trained with different epoch counts.
    3.  **Observe Training Curve:** Look at the training graph that appears after training—does the loss plateau, or is it still decreasing?

*   **Deliverable:** 
    *   Screenshots of the training graphs from both runs
    *   Detection results comparison
    *   A short note on whether more epochs improved results, and if there's a point of diminishing returns

## Bonus Assignment – Your Own Deep Learning Experiment

*   **Images:** Use the images from `resources/PhaseContrast/` (from Lesson 3) or `resources/Nuclei/` (40 fluorescence images in this assignment folder).
*   **Objective:** Design a custom Cellpose training workflow for a challenging segmentation task.

    > 💡 **Note:** The `resources/Nuclei/` images require calibration. Use the `01_calibrate_images.groovy` script from Lesson 3 (`Day3/3) cellpose_training/scripts/`) with a pixel size of **0.69 µm/pixel**.

    1.  Identify what objects you want to segment and why pretrained models might struggle.
    2.  Calibrate images if needed (PhaseContrast: 0.2 µm/pixel, Nuclei: 0.69 µm/pixel).
    3.  Create appropriate Training and Validation regions with dense annotations.
    4.  Experiment with different training parameters (epochs, learning rate, diameter, preprocessing filters).
    5.  Document what worked and what didn't.

*   **Deliverable:** A one-slide or short paragraph summary including:
    *   The image and objects you chose
    *   Training parameters that worked best
    *   Before/after comparison (pretrained vs custom model)
    *   Key insights about training custom models

> **Tip:** Training deep learning models requires patience! If your first model doesn't work well, check your annotations (are they dense enough?), diameter setting (does it match your objects?), and training curve (did it converge?)..
