# QuPath Scripting Basics Teaching Kit

## Overview
This module introduces the **Script Editor** and the basics of the Groovy programming language used in QuPath. It bridges the gap between clicking buttons and automating workflows.

## Prerequisites
1.  **QuPath 0.7.0**
2.  **Sample Image**: Any image will do, but `resources/skin.ome.tif` is good for drawing practice.

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson) with `resources/skin.ome.tif`. Set the image type to **Brightfield (H&E)**.

An existing image from Lesson 1 also works. The counting scripts use whatever annotations are already on the current image.

[Start the lesson](LESSON.md).

## The Scripts
*   **`01_hello_world.groovy`**: Your first script. Prints a message to the log.
*   **`02_count_annotations.groovy`**: Accesses the image data to count how many objects you drew.
*   **`03_filter_by_area.groovy`**: Uses a loop to find and print details about large annotations only.
