# Lesson: Deep Learning Segmentation

**Before running this lesson, read `README.md` in this folder for the setup instructions.**

**Duration:** 30 Minutes
**Goal:** Compare standard watershed cell detection with StarDist and Cellpose on the same nuclei.

## Part 1: The Problem (5 Minutes)
*   **Activity:** Draw and select a representative parent annotation on `nuclei.ome.tif`. Use the same region for all three methods. Run `Analyze > Cell detection > Cell detection` using **Channel 1**, with **Cell expansion** set to 0 for nuclei only.
*   **Observation:** Notice how it might split large nuclei or merge touching ones. It requires careful parameter tuning.
*   **Key Parameters to Tune:**
    *   **Requested pixel size (µm):** Controls resolution for detection. Smaller = finer detail but slower.
    *   **Background radius (µm):** Size of the local background estimation window. Should be larger than your largest nucleus.
    *   **Median filter radius (µm):** Smooths noise before detection. Increase for noisy images.
    *   **Sigma (µm):** Gaussian smoothing for gradient calculation. Larger values merge nearby edges; smaller values may over-segment.
    *   **Minimum/Maximum area (µm²):** Filters out objects outside this size range. Useful for removing debris or merged clusters.
    *   **Threshold:** Intensity threshold for nucleus detection. Lower = more sensitive; higher = more selective.
    *   **Split by shape:** Attempts to separate touching nuclei using shape-based watershed. Enable for clustered nuclei.
    *   **Cell expansion (µm):** Grows detected nuclei outward to estimate cell boundaries.
*   **The Challenge:** Getting good results requires balancing many interdependent parameters—what works for one image may fail on another.
*   **Alternative:** Pretrained models use shapes and image context learned from training data. Their performance depends on how closely the lesson image resembles that data.

## Part 2: StarDist (10 Minutes)
*   **Concept:** StarDist predicts outlines from radial distances drawn outward from an object centre. This works well for many compact nuclei and can be less suitable for highly irregular or fragmented objects.
*   **Action:** Save a screenshot and the detection count from Part 1. Remove the earlier detections or open an equivalent image entry for comparison. Select the same parent region and run `01_stardist_nuclei.groovy`.
*   **Key Parameters:**
    *   **Channel:** Select **Channel 1**, the only channel on this image.
    *   **Probability Threshold (0–1):** Controls detection confidence. Lower values detect more objects (including weak/uncertain ones); higher values are more selective. Start at 0.5 and adjust.
    *   **Downscale (≥1):** Reduces image resolution before detection. Higher = faster but coarser. Start at 1 to process the native resolution.
    *   **Cell expansion (µm):** Expands detected nuclei outward to approximate cell boundaries. Set to 0 for nuclei-only.
    *   **Mean filter radius (px):** Applies a mean (box) filter to smooth noise before detection. 0 = off.
    *   **Gaussian filter radius (px):** Applies a Gaussian blur for smoother noise reduction. 0 = off.
    *   **Exclude detections on borders:** Removes objects touching the image edge (recommended for quantification).
*   **Discussion:**
    *   Did it miss anything?
    *   Try adjusting the probability threshold—what happens at 0.3 vs 0.7?
*   **Exercise:** Enable preprocessing filters (mean or Gaussian) and observe how they affect segmentation quality on noisy images.

## Part 3: Cellpose (10 Minutes)
*   **Concept:** Cellpose is a generalist algorithm that works on a wide variety of shapes (not just stars). It's slower but often more robust for irregular shapes.
*   **Action:** Record the StarDist result. With the same analysis region selected and previous detections cleared or saved separately, run `02_cellpose.groovy` in your project.
*   **Key Parameters:**
    *   **Cellpose model:** Choose a pretrained model (`nucleitorch_0` for nuclei, or `cyto3` for a comparison).
    *   **Primary channel (chan):** The main channel to segment. This should contain the signal for whatever you're detecting (nuclei, cells, bacteria, etc.) depending on the model chosen.
    *   **Nuclear channel for cyto models (chan2):** (Optional) Only used with cyto models to help identify cell boundaries. Set to "None" for nuclei-only models or when not needed.
    *   **Diameter (µm):** Expected median diameter of objects. Critical for good results—measure a few objects first!
    *   **Cellprob threshold:** Controls mask confidence. Default is 0; negative values include more marginal detections.
    *   **Flow threshold:** Controls flow error tolerance for mask generation. The default is 0.4. Higher values accept masks with more flow error.
    *   **Cell expansion (µm):** Expands detected nuclei outward. 0 = nuclei only.
    *   **Tile size (px):** Larger tiles = faster processing but more GPU memory. Reduce if you run out of memory.
    *   **Mean filter radius (px):** Applies mean smoothing before detection. 0 = off.
    *   **Gaussian filter radius (px):** Applies Gaussian blur before detection. 0 = off.
    *   **Exclude detections on borders:** Removes objects touching the image edge.
*   **Comparison:** Compare the three screenshots on the same region. Count detections and inspect the same dim, touching, and fragmented nuclei in each result. Note which objects were missed, merged or split.
*   **Exercise:** Try adjusting the **Mean filter** or **Gaussian filter** radius to see how preprocessing affects segmentation. On noisy images, a small filter (e.g., 2–5 px) may reduce noise but can also blur nearby boundaries. Change one setting at a time.
