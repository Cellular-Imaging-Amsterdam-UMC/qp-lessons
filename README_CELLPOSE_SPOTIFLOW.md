# Setting up Cellpose and Spotiflow for QuPath 0.7

This guide creates one Python environment for the Cellpose and Spotiflow QuPath extensions. It intentionally pins **Cellpose 3.1.1.3**, the newest Cellpose 3 release before Cellpose-SAM was introduced in Cellpose 4. Do not replace the pin with an unversioned `pip install cellpose`, because that installs Cellpose-SAM 4.x.

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
   python -c "import cellpose, spotiflow, torch; assert cellpose.__version__ == '3.1.1.3'; print('Cellpose:', cellpose.__version__); print('Spotiflow:', spotiflow.__version__); print('CUDA available:', torch.cuda.is_available())"
   ```

If an existing environment contains Cellpose 4, recreating the environment is safer than downgrading it in place.

## Part 2: QuPath 0.7 extensions

Install QuPath 0.7-compatible releases of both BIOP extensions:

- [BIOP Cellpose extension releases](https://github.com/BIOP/qupath-extension-cellpose/releases)
- [BIOP Spotiflow extension releases](https://github.com/BIOP/qupath-extension-spotiflow/releases)

The QuPath 7 standalone distribution used for these lessons contains Cellpose extension 0.12.1 and Spotiflow extension 0.4.1. When installing Cellpose from its release archive, copy **all** supplied files—not only the JAR—into the QuPath extensions directory. On Windows, the default user extension location is `%APPDATA%\QuPath\extensions` unless a custom QuPath user path is configured.

Restart QuPath after installing or replacing extensions.

## Part 3: Configure QuPath

1. Open `Edit > Preferences` in QuPath 0.7.
2. In the Cellpose settings, set the Python executable to the environment's interpreter:

   ```text
   <conda-root>\envs\qupath-dl\python.exe
   ```

3. In the Spotiflow settings, select the same Python executable.
4. Leave the separate Cellpose-SAM Python path empty; these lessons use Cellpose 3.
5. Restart QuPath, then run a Cellpose and a Spotiflow lesson script to confirm the setup.

To locate the interpreter path from the activated environment, run:

```bash
python -c "import sys; print(sys.executable)"
```

You can now run the Day 3 deep-learning lessons with QuPath 0.7.
