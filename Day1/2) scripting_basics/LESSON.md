# Lesson Plan: Scripting Basics

**Before running this lesson, read `README.md` in this folder for the setup instructions.**

**Duration:** 20-30 Minutes
**Goal:** Demystify the Script Editor. Users will write their first lines of code to interact with QuPath objects, understanding that "everything you do in the GUI can be done in code."

## Part 1: The Script Editor (5 Minutes)
*   **Open:** `Automate > Show script editor`.
*   **Interface:**
    *   **Editor Area:** Where you type code.
    *   **Run Button:** `Ctrl + R` to execute.
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
*   **Activity:** Draw 3 rectangles. Run the script. Draw 2 more. Run it again.
*   **Concept:** `getAnnotationObjects()` is a "getter" that fetches the current state of the image.

## Part 4: Looping & Filtering (10 Minutes)
*   **Goal:** Find only the *large* annotations.
*   **Code:**
    ```groovy
    def annotations = getAnnotationObjects()
    for (annotation in annotations) {
        def area = annotation.getROI().getArea()
        if (area > 1000) {
            print "Found a big one! Area: " + area
        }
    }
    ```
*   **Concept:**
    *   **Loop (`for`)**: "Do this for every item in the list."
    *   **Condition (`if`)**: "Only do this if..."

## Part 5: The Workflow Tab (Optional)
*   **Tip:** Show the "Workflow" tab in the analysis pane.
*   **Action:** Click "Create script" to see how QuPath automatically generates code for commands you ran (like Cell Detection).
