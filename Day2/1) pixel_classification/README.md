# QuPath Pixel Classification Teaching Kit

## Overview
This kit provides a structured lesson plan and "Magic Scripts" to teach pixel classification in QuPath. It is designed for a 30-60 minute interactive session.

## Prerequisites
1.  **QuPath 0.7.0**: Download from [qupath.github.io](https://qupath.github.io/).
2.  **Sample Image**: This lesson uses the local file `resources/skin.ome.tif`.

## Setup Instructions

Follow the [course setup steps](../../README.md#before-each-lesson) with `resources/skin.ome.tif`. Set the image type to **Brightfield (H&E)**.

Run the setup script before drawing training strokes. If you make an optional training composite, run setup before creating its crop regions.

[Start the lesson](LESSON.md).

[Watch the instruction video](LESSON.md#instruction-video).

## The "Magic" Scripts
*   **`assignment1_magic_setup.groovy`**: Run this *before* the lesson starts (or at the very beginning). It sets up the skin-layer classes (Stratum corneum, EPTZ, Stratum granulosum, Stratum spinosum, Stratum basale, Dermis + Whitespace) with high-contrast colors and clears any old annotations.
*   **`assignment1_magic_report.groovy`**: Run this after the classifier has created prediction annotations on the reporting image. It summarizes the area and percentage for each tissue class, excluding Whitespace so they can compare layer proportions without digging through measurement tables.
