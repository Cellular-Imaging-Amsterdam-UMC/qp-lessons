# Lesson: Scripting Basics

**Before running this lesson, read `README.md` in this folder for the setup instructions.**

**Duration:** 20-30 Minutes
**Goal:** Demystify the Script Editor. Write your first Groovy scripts to read annotations and automate a repeatable task.

## Part 1: The Script Editor (5 Minutes)
*   **Open:** `Automate > Script editor`.
*   **Interface:**
    *   **Editor Area:** Where you type code.
    *   **Run:** Click **Run** to execute the script on the open image.
    *   **Output:** The bottom pane where `print()` messages appear.
*   **Language:** QuPath uses **Groovy** (similar to Java but easier).

## Part 2: Hello World (5 Minutes)
*   **Activity:** Type `print "Hello QuPath!"` and run it.
*   **Concept:** The computer follows your instructions line by line.

## Part 3: Accessing Objects (10 Minutes)
*   **Goal:** Count how many annotations are on the image.
*   **Code:**
    ```groovy
    def annotations = getAnnotationObjects()
    print "I found " + annotations.size() + " annotations."
    ```
*   **Activity:** Draw three rectangles and run the script. Draw two more and run it again. On an image with no earlier annotations, the counts should be 3 and 5.
*   **Concept:** `getAnnotationObjects()` is a "getter" that fetches the current state of the image.

## Part 4: Looping & Filtering (10 Minutes)
*   **Goal:** Find only the *large* annotations.
*   **Code:**
    ```groovy
    def annotations = getAnnotationObjects()
    for (annotation in annotations) {
        def area = annotation.getROI().getArea()
        if (area > 5000) {
            print "Found a big one! Area: " + area
        }
    }
    ```
*   `getROI()` returns the outline associated with an annotation. `getArea()` reports its area in square pixels (px²), even when the viewer shows calibrated units. Draw a small and a large rectangle and change the cutoff to see which lines appear in the log. The script reports matching annotations and leaves them on the image.
*   **Concept:**
    *   **Loop (`for`)**: "Do this for every item in the list."
    *   **Condition (`if`)**: "Only do this if..."

## Part 5: The Workflow Tab (Optional)
*   **Tip:** Show the "Workflow" tab in the analysis pane.
*   **Action:** Try a recordable command such as `Objects > Annotations > Create full image annotation`. Then use `Automate > Create command history script` to see the generated Groovy. The workflow history records supported commands, so hand-drawn shapes and some viewer actions may be absent.
