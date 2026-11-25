# Day 3 Assignment Session: Custom Deep Learning Training

**Purpose:** Apply the skills from the Day 3 Cellpose training lesson to explore different training scenarios and compare model performance.

## Assignment A – Apply a Pretrained Model to Answer a Biological Question

*   **Context:** In the DL Segmentation lesson, you learned how to use pretrained StarDist and Cellpose models for nucleus and cell detection. Now it's time to apply these skills independently to explore a biological question of your choice.

*   **Objective:** Select an image from the course resources, formulate a biological question, apply an appropriate pretrained deep learning model, and analyze the results.

### Step 1: Choose Your Image and Biological Question

Select one of the images below from the [shared course resources](../../resources/) and choose a question. For a treatment-effect question you also need comparable controls and biological replicates.

| Image | Suggested Biological Question |
|-------|------------------------------|
| `Cells-Irradiated.ome.tif` | How variable are nuclear size and shape within this irradiated sample? |
| `NucleiDNARepairFoci.ome.tif` | What is the distribution of nuclear sizes? Are there distinct populations? |
| `TrophoblastCellCulture.ome.tif` | How variable are cell sizes and what is the cell density in the selected region? |
| `CellsNuclei.ome.tif` | What percentage of cells show elongated vs. round nuclear morphology? |
| `PostiveNuclei.ome.tif` | Can we quantify nuclear intensity differences across the population? |
| `TumorsKidney.ome.tif` | How does nuclear density vary across different tumor regions? |

### Step 2: Set Up Your Project

1.  **Create a New Project:** Use **Create project…** in the **Project** tab
2.  **Add Your Image:** `File > Project > Add images` → select your chosen image
3.  **Create an Annotation:** Draw and select a representative region of interest (ROI). Aim for about 50–100 objects for this descriptive exercise. Check pixel calibration before reporting areas or densities.

### Step 3: Apply a Pretrained Model

1.  **Choose Your Model:**
    *   For **nuclei detection**: Use StarDist (`01_stardist_nuclei.groovy`) or Cellpose with the `nucleitorch_0` model (`02_cellpose.groovy`)
    *   For **cell detection**: Use `cyto3` when a cell-boundary or cytoplasmic signal is visible. Start with zero expansion when segmenting cell outlines directly.
    
2.  **Configure Parameters:**
    *   **Channel:** Select the appropriate channel (e.g., DAPI for nuclei)
    *   **Diameter (Cellpose):** Measure 3–5 typical objects with a line annotation and read length in µm from the annotation measurement table
    *   **Probability/Detection threshold:** Start with defaults, adjust if needed
    *   **Cell expansion:** Set to 0 to keep the model outlines. If you start from nuclei, an expansion of 2–5 µm gives an estimated cell region. Record any expansion you use.

3.  **Run Detection:** Run the script from the [Deep Learning Segmentation lesson](../1%29%20dl_segmentation/LESSON.md) on the selected annotation. Inspect a few outlines before measuring.

### Step 4: Perform Measurements and Analysis

1.  **View Measurements:** `Measure > Show detection measurements`
2.  **Key Metrics to Explore:**
    *   **Area (µm²):** Object size distribution
    *   **Circularity:** Shape regularity (1.0 = perfect circle)
    *   **Perimeter (µm):** Edge complexity
    *   **Mean intensity:** Signal strength per object
    *   **Solidity:** Compactness of shape
    
3.  **Generate Summary Statistics:**
    *   Go to `Measure > Show detection measurements`
    *   Use **Save** in the measurement table to export the values
    *   Note the **mean**, **min**, **max**, and **standard deviation** of key measurements

4.  **Visualize Results:**
    *   Go to `Measure > Show measurement maps` to color-code detections by any measurement
    *   Open the detection measurement table and use **Show plots** to view a histogram for the selected measurement

### Step 5: Document Your Findings

*   **Deliverables:**
    1.  **Screenshot** of your detection results (showing good segmentation)
    2.  **Table** with summary statistics for at least 3 measurements relevant to your question
    3.  **Brief interpretation** (3-5 sentences) answering your biological question based on the data
    4.  **Reflection:** Did the pretrained model work well? What parameters did you adjust and why?

*   **Example report with illustrative numbers:**

    > **Image:** `Cells-Irradiated.ome.tif`
    > 
    > **Biological Question:** How variable is nuclear morphology in this irradiated image?
    > 
    > **Model Used:** StarDist (dsb2018_heavy_augment.pb), Threshold: 0.5
    > 
    > **Results (n = 127 nuclei):**
    > | Measurement | Mean | Std Dev | Min | Max |
    > |-------------|------|---------|-----|-----|
    > | Area (µm²) | 142.3 | 45.2 | 38.1 | 312.5 |
    > | Circularity | 0.73 | 0.18 | 0.22 | 0.95 |
    > | Solidity | 0.89 | 0.08 | 0.51 | 0.98 |
    > 
    > **Interpretation:** The nuclear population shows high variability in circularity (0.22–0.95), with both round and irregular outlines. Review the low-solidity examples in the image to distinguish true irregularities from segmentation errors.

---

## Assignment B – Evaluate and Improve Your Trained Model

*   **Context:** In Lesson 3 (cellpose_training), you trained a custom Cellpose model on phase contrast images. Now it's time to systematically evaluate its performance and optionally improve it.
*   **Objective:** Evaluate your trained model on multiple unseen images and optionally fine-tune it by adding more annotations.

### Part 1: Systematic Evaluation

1.  **Open Your Lesson 3 Project:** Continue with the QuPath project from Lesson 3 (cellpose_training).
2.  **Select Test Images:** Choose **3–5 held-out images**, for example from `21.jpg`–`30.jpg` if you used the lesson split.
3.  **Run Your Model:** Define comparable test ROIs, inspect or label the target objects, then select each ROI and run `05_run_custom_cellpose_model.groovy`.
4.  **Evaluate Performance:** For each image, assess:
    *   **True positives (TP):** Detections that match individual reference objects. Match each object at most once.
    *   **False positives (FP):** Extra detections, including extra pieces when a single object is split.
    *   **False negatives (FN):** Reference objects left unmatched, including objects merged with a neighbour.
    *   **Boundary quality:** Note where outlines are too large, too small, or poorly aligned. Use a consistent rule on every ROI.

### Part 2: Document Results

*   **Deliverables:**
    1.  **Screenshots** of detection results on at least 3 test images
    2.  **Evaluation table:**
        | Image | Reference objects | TP | FP | FN | Boundary notes |
        |-------|----------------|----------|-----------------|--------|---------------|
        | 21.jpg | | | | | |
        | 22.jpg | | | | | |
        | 23.jpg | | | | | |
    3.  **Performance summary** (3-5 sentences): Where does your model perform well? Where does it struggle? Are there specific image regions or object types that cause problems?

### Part 3: Fine-Tuning (Optional)

If your model shows consistent errors, try improving it:

1.  **Identify Problem Areas:** Note where false positives/negatives occur most often.
2.  **Add More Training Data:** Create **2–4 additional Training regions** on non-test images showing similar examples. If you use the original test images to guide tuning, set aside fresh images for a final independent check.
3.  **Re-train:** Run `04_train_cellpose.groovy` with:
    *   Same model name (a new version will be created automatically)
    *   Same parameters as before
4.  **Compare Models:** Run both models on the same chosen regions, clearing the previous detections or saving each result separately.

*   **Optional Deliverables:**
    1.  **Side-by-side comparison** of original vs. improved model on the same image
    2.  **Brief notes** on what annotations you added and whether performance improved

---

## Bonus Assignment – Transfer Learning: Pretrained vs. From Scratch

*   **Context:** Cellpose offers the option to start training from a pretrained base model (transfer learning) or train from scratch. Transfer learning can dramatically reduce training time and improve results when the pretrained model is similar to your target objects.

*   **Objective:** Compare model performance when training **from scratch** vs. using **transfer learning** with a pretrained base model.

### Instructions

1.  **Use the Same Ground Truth:** Use the Training and Validation annotations you created in Assignment B (or the lesson).

2.  **Train Two Models:**
    
    | Model | Base Model | Epochs |
    |-------|------------|--------|
    | Model A | `None (train from scratch)` | 75 |
    | Model B | `cyto3` (if available in the base model list) | 75 |

3.  **Run Both Models:** Apply each trained model to the **same unseen test image** using `05_run_custom_cellpose_model.groovy`.

4.  **Compare Results:** Use the same regions, target definitions, and inference settings apart from the model file. Record any differences you intentionally introduce.

*   **Deliverables:**
    1.  **Side-by-side screenshots** showing detection results from both models on the same image region
    2.  **Comparison table:**
    
        | Metric | From Scratch | Transfer Learning |
        |--------|--------------|-------------------|
        | Training time (approx.) | | |
        | Visual quality (1-5) | | |
        | Missed objects (estimate) | | |
        | False positives (estimate) | | |
    
    3.  **Conclusion** (3-5 sentences): Which approach worked better for your specific object type? When might you choose one approach over the other?

> 💡 **Tip:** Transfer learning typically works best when your objects are similar to what the base model was trained on. If your objects are very different (e.g., phase contrast nucleoli vs. fluorescent cytoplasm), training from scratch may actually perform better!



