# Setting up Cellpose and Spotiflow for QuPath 0.7

This guide creates one Python environment for the Cellpose and Spotiflow QuPath extensions. It intentionally pins **Cellpose 3.1.1.3**, the version used by these scripts and their Cellpose 3 model files.

## Course standalone installation

With the supplied Windows standalone installation, start `RunQuPath7.0.cmd`. It loads the configured interpreter and model folders. The following setup is for a separate QuPath installation.

## Part 1: Python environment

The commands below create a Conda environment named `qupath-dl` with Python 3.12.

### Prerequisites

- Miniconda, Anaconda, Mamba, or Micromamba.
- For GPU acceleration, a supported NVIDIA GPU with a current driver.

### Installation

1. Open an Anaconda Prompt or terminal.

2. Create and activate the environment:

   ```bash
   conda create -n qupath-dl python=3.12 pip
   conda activate qupath-dl
   ```

3. Install PyTorch. Choose exactly one of the following commands.

   NVIDIA GPU (CUDA 12.6 wheels, matching the QuPath 7 standalone setup):

   ```bash
   python -m pip install torch torchvision --index-url https://download.pytorch.org/whl/cu126
   ```

   CPU only:

   ```bash
   python -m pip install torch torchvision --index-url https://download.pytorch.org/whl/cpu
   ```

   If CUDA 12.6 is not suitable for your system, select the appropriate command from the [PyTorch installation guide](https://pytorch.org/get-started/locally/).

4. Install the Python backends used by the lessons:

   ```bash
   python -m pip install "cellpose[gui]==3.1.1.3" "spotiflow==0.6.5"
   ```

5. Verify the installation:

   ```bash
   python -c "from importlib.metadata import version; import cellpose, spotiflow, torch; assert version('cellpose') == '3.1.1.3'; print('Cellpose:', version('cellpose')); print('Spotiflow:', version('spotiflow')); print('CUDA available:', torch.cuda.is_available())"
   ```

If an existing environment contains Cellpose 4, recreating the environment is safer than downgrading it in place.

## Part 2: QuPath 0.7 extensions

Install a QuPath 0.7-compatible release of each extension:

- [QuPath StarDist releases](https://github.com/qupath/qupath-extension-stardist/releases)

- [BIOP Cellpose extension releases](https://github.com/BIOP/qupath-extension-cellpose/releases)
- [BIOP Spotiflow extension releases](https://github.com/BIOP/qupath-extension-spotiflow/releases)

The QuPath 7 standalone distribution used for these lessons contains Cellpose extension 0.12.1 and Spotiflow extension 0.4.1. When installing Cellpose from its release archive, copy the JAR and accompanying files, including `run-cellpose-qc.py`, into the QuPath extensions directory. The QuPath user directory is listed under `Edit > Preferences`. [QuPath extension setup](https://qupath.readthedocs.io/en/stable/docs/intro/extensions.html)

Restart QuPath after installing or replacing extensions.

## Part 3: Configure QuPath

1. Open `Edit > Preferences` in QuPath 0.7.
2. In the Cellpose settings, set the Python executable to the environment's interpreter:

   ```text
   <conda-root>\envs\qupath-dl\python.exe
   ```

3. In the Spotiflow settings, select the same Python executable.
4. Leave the separate Cellpose-SAM Python path empty; these lessons use Cellpose 3.
5. Close QuPath before configuring the local models below.

To locate the interpreter path from the activated environment, run:

```bash
python -c "import sys; print(sys.executable)"
```

## Part 4: Local models and first run

*   **StarDist:** Download `dsb2018_heavy_augment.pb` into a model folder and set `STARDIST_LOCAL_MODELS_PATH` to that folder. QuPath's StarDist extension runs this file through OpenCV. [StarDist model files](https://github.com/qupath/models/tree/main/stardist)
*   **Cellpose:** The course scripts list the files in `CELLPOSE_LOCAL_MODELS_PATH`. Set it to your Cellpose model folder. To populate that folder using Cellpose 3, activate the `qupath-dl` environment and run the following command after setting the variable:

    ```bash
    python -c "from cellpose.models import model_path; print(model_path('cyto3')); print(model_path('nuclei'))"
    ```

    The nuclear model file appears as `nucleitorch_0` in the course dialog.
*   **Spotiflow:** The Python backend normally fetches the `general` model on first use. For an offline installation, prepare the model cache beforehand and set `SPOTIFLOW_CACHE_DIR` to its root. [Spotiflow documentation](https://github.com/weigertlab/spotiflow)

Set the model variables in the environment from which you launch QuPath. On Windows, for example, use the following in PowerShell and launch `QuPath-0.7.0.exe` from the same session. Replace the example paths with your own folders.

```powershell
$env:STARDIST_LOCAL_MODELS_PATH = 'C:\models\stardist'
$env:CELLPOSE_LOCAL_MODELS_PATH = 'C:\models\cellpose'
```

Try StarDist and Cellpose on a small selected region in the [segmentation lesson](Day3/1%29%20dl_segmentation/LESSON.md). For Spotiflow, first create nucleus annotations as described in the [spot detection lesson](Day3/2%29%20spot_detection/LESSON.md). A missing-model message usually means QuPath was started before the environment variables were set, or a variable points to the wrong folder. A missing-Python message means the extension preferences need the interpreter path reported in Part 3.
