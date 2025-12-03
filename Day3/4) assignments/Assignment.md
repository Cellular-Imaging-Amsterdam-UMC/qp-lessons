# Day 3 Assignment Session: Custom Deep Learning Training

**Purpose:** Apply the skills from the Day 3 Cellpose training lesson to explore different training scenarios and compare model performance.

## Assignment A – Apply a Pretrained Model to Answer a Biological Question

*   **Context:** In the DL Segmentation lesson, you learned how to use pretrained StarDist and Cellpose models for nucleus and cell detection. Now it's time to apply these skills independently to explore a biological question of your choice.

*   **Objective:** Select an image from the course resources, formulate a biological question, apply an appropriate pretrained deep learning model, and analyze the results.

### Step 1: Choose Your Image and Biological Question

Select one of the following images from `resources/` and consider the suggested biological question (or create your own):

| Image | Suggested Biological Question |
|-------|------------------------------|
| `Cells-Irradiated.ome.tif` | How does irradiation affect nuclear morphology (size, circularity, fragmentation)? |
| `NucleiDNARepairFoci.ome.tif` | What is the distribution of nuclear sizes? Are there distinct populations? |
| `TrophoblastCellCulture.ome.tif` | How variable are cell sizes in this culture? What's the cell density? |
| `CellsNuclei.ome.tif` | What percentage of cells show elongated vs. round nuclear morphology? |
| `PostiveNuclei.ome.tif` | Can we quantify nuclear intensity differences across the population? |
| `TumorsKidney.ome.tif` | How does nuclear density vary across different tumor regions? |

### Step 2: Set Up Your Project

1.  **Create a New Project:** `File > Project > Create project`
2.  **Add Your Image:** `File > Project > Add images` → select your chosen image
3.  **Create an Annotation:** Draw a rectangular annotation around a region of interest (ROI) containing at least **50-100 objects** for meaningful statistics

### Step 3: Apply a Pretrained Model

1.  **Choose Your Model:**
    *   For **nuclei detection**: Use StarDist (`01_stardist_nuclei.groovy`) or Cellpose with `nuclei` model (`02_cellpose.groovy`)
    *   For **cell detection**: Use Cellpose with `cyto3` model and set appropriate cell expansion
    
2.  **Configure Parameters:**
    *   **Channel:** Select the appropriate channel (e.g., DAPI for nuclei)
    *   **Diameter (Cellpose):** Measure 3-5 typical objects first using `Measure > Show measurement tools`
    *   **Probability/Detection threshold:** Start with defaults, adjust if needed
    *   **Cell expansion:** Set to 0 for nuclei-only, or 2-5 µm for estimating cell boundaries

3.  **Run Detection:** Execute the script on your selected annotation

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
    *   Use `Table > Export as CSV` to save your data
    *   Note the **mean**, **min**, **max**, and **standard deviation** of key measurements

4.  **Visualize Results:**
    *   Go to `Measure > Show measurement maps` to color-code detections by any measurement
    *   Create a histogram: Select detections → `Measure > Show histograms`

### Step 5: Document Your Findings

*   **Deliverables:**
    1.  **Screenshot** of your detection results (showing good segmentation)
    2.  **Table** with summary statistics for at least 3 measurements relevant to your question
    3.  **Brief interpretation** (3-5 sentences) answering your biological question based on the data
    4.  **Reflection:** Did the pretrained model work well? What parameters did you adjust and why?

*   **Example Report Format:**

    > **Image:** `Cells-Irradiated.ome.tif`
    > 
    > **Biological Question:** How does irradiation affect nuclear morphology?
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
    > **Interpretation:** The nuclear population shows high variability in circularity (0.22–0.95), suggesting the presence of both normal round nuclei and fragmented/irregular nuclei potentially indicative of apoptosis or radiation damage. The low-solidity outliers (< 0.7) likely represent damaged or fragmenting nuclei.

---

## Assignment B – Train on Alternate Objects

*   **Context:** In Lesson 3 (cellpose_training), you trained a model on one type of object (either Nuclei or Nucleoli in the phase contrast images).
*   **Objective:** Train a new model on the **other** object type that you didn't use in the lesson.

    1.  **Create a New Project:** Start fresh with a new QuPath project using the same `resources/PhaseContrast/` images.
    2.  **Calibrate Images:** Run `01_calibrate_images.groovy` to set the pixel size.
    3.  **Create Ground Truth:** Create Training (at least 6) and Validation (at least 2) regions containing **dense annotations** of the alternate object type.
    4.  **Train the Model:** Run `04_train_cellpose.groovy` with:
        *   Base model: `None (train from scratch)`
        *   Epochs: **at least 75**
        *   Diameter: Set appropriately for the object size (Nucleoli are much smaller than Nuclei!)
    5.  **Validate:** Run `05_run_custom_cellpose_model.groovy` on an unseen image and evaluate the results.

*   **Deliverables:**
    1.  **Screenshot** of the detection result on an unseen image
    2.  **Model parameters table:**
        | Parameter | Value |
        |-----------|-------|
        | Base model | |
        | Epochs | |
        | Diameter (µm) | |
        | Training regions | |
        | Validation regions | |
    3.  **Comparison notes** (3-5 sentences): Compare the difficulty of annotating this object type vs. the one from the lesson. Consider:
        *   Was it easier or harder to identify object boundaries?
        *   Did object density or overlap cause challenges?
        *   How did object size affect annotation time?
    4.  **Performance reflection:** How well did your trained model perform? Any obvious errors or missed detections?

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
    | Model B | `cyto3` or `nuclei` (whichever is closest to your object) | 75 |

3.  **Run Both Models:** Apply each trained model to the **same unseen test image** using `05_run_custom_cellpose_model.groovy`.

4.  **Compare Results:** Evaluate both models on the same regions.

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



