# Setting up Cellpose and Spotiflow for QuPath

This guide explains how to set up a single Python environment that supports both **Cellpose** and **Spotiflow** for use with QuPath, and how to install the necessary QuPath extensions.

## Part 1: Python Environment Setup

We will create a Conda environment named `qupath-dl` that contains both libraries.

### Prerequisites
*   **Miniconda or Anaconda**: Ensure you have conda installed. [Download Miniconda](https://docs.conda.io/en/latest/miniconda.html).
*   **GPU Drivers (Optional but Recommended)**: If you have an NVIDIA GPU, ensure your drivers are up to date.

### Step-by-Step Installation

1.  **Open your terminal** (Anaconda Prompt on Windows).

2.  **Create a new environment** with Python 3.12:
    ```bash
    conda create -n qupath-dl python=3.10
    ```

3.  **Activate the environment**:
    ```bash
    conda activate qupath-dl
    ```

4.  **Install PyTorch**:
    *   *For GPU (CUDA 12.6 example - check [pytorch.org](https://pytorch.org/get-started/locally/) for your system):*
        ```bash
        pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu126
        ```
    *   *For CPU only:*
        ```bash
        pip install torch torchvision torchaudio
        ```

5.  **Install Cellpose**:
    ```bash
    pip install cellpose
    ```

6.  **Install Spotiflow**:
    ```bash
    pip install spotiflow
    ```

7.  **Verify Installation**:
    Run these commands in python to check if they import correctly:
    ```bash
    python -c "import cellpose; print('Cellpose version:', cellpose.__version__)"
    python -c "import spotiflow; print('Spotiflow installed')"
    ```

---

## Part 2: QuPath Extension Installation

You need to install the BIOP extensions to allow QuPath to talk to your Python environment.

### 1. Download the Extensions
*   **Cellpose Extension**: Go to the [BIOP Cellpose GitHub](https://github.com/BIOP/qupath-extension-cellpose/releases) and download the latest `.zip` archive (e.g., `qupath-extension-cellpose-x.x.x.zip`). Extract the archive and copy the entire folder contents into your QuPath `extensions` directory (default on Windows: `%APPDATA%\QuPath\extensions`). Do not copy only the `.jar` file.
*   **Spotiflow Extension**: Go to the [BIOP Spotiflow GitHub](https://github.com/BIOP/qupath-extension-spotiflow/releases) and download the latest `.jar` file.

### 2. Install in QuPath
1.  Open QuPath.
2.  Drag and drop the downloaded `.jar` files onto the QuPath window.
3.  Restart QuPath.

---

## Part 3: Configuring QuPath

Now you need to tell QuPath where your Python environment is.

1.  Open QuPath.
2.  Go to `Edit > Preferences`.
3.  **For Cellpose**:
    *   Find the **Cellpose** section.
    *   **Python Environment**: Select `Conda`.
    *   **Environment Name**: Enter `qupath-dl`.
    *   (Alternatively, point to the python executable path).
4.  **For Spotiflow**:
    *   Find the **Spotiflow** section.
    *   Set the environment to the same `qupath-dl` environment.

**Done!** You can now run the Deep Learning lessons.
