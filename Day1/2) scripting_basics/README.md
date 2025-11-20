# QuPath Scripting Basics Teaching Kit

## Overview
This module introduces the **Script Editor** and the basics of the Groovy programming language used in QuPath. It bridges the gap between clicking buttons and automating workflows.

## Prerequisites
1.  **QuPath v0.6.0+**
2.  **Sample Image**: Any image will do, but `resources/skin.ome.tif` is good for drawing practice.

## Setup Instructions
1.  **Create Project**:
	*   Open QuPath.
	*   Use `File > New Project...` to create or open an existing project folder (preferably an empty subfolder) so QuPath can store annotations and scripts for this lesson.
	*   Drag and drop the `resources/skin.ome.tif` file into the QuPath window so it lives inside your new project (any image works if you already have one in the project).
2.  **Open Editor**: Go to `Automate > Show script editor`.

## The Scripts
*   **`01_hello_world.groovy`**: Your first script. Prints a message to the log.
*   **`02_count_annotations.groovy`**: Accesses the image data to count how many objects you drew.
*   **`03_filter_by_area.groovy`**: Uses a loop to find and print details about large annotations only.
