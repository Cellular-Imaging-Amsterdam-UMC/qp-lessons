/*************************************************************
 * Foci Histogram Report – Display distribution of spots per nucleus (QuPath 0.7.x)
 *
 * PURPOSE
 * - Creates a histogram showing the distribution of foci counts per nucleus.
 * - Displays summary statistics (mean, median, min, max, std dev).
 *
 * REQUIREMENTS
 * - Nuclei annotations must exist with "Foci: Count" or "Foci: Count (nucleus)" measurements.
 * - Run this after 02_spotiflow_foci.groovy or 03_stardist_spotiflow_combined.groovy.
 *
 * OUTPUT
 * - Shows a histogram chart in a popup window.
 * - Prints summary statistics to the log.
 *************************************************************/

import qupath.lib.gui.dialogs.Dialogs as GuiDialogs
import qupath.lib.plugins.parameters.ParameterList
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.layout.VBox
import javafx.scene.control.Label
import javafx.stage.Stage
import javafx.geometry.Insets

// ---------------------------
// Default parameters
// ---------------------------
int binSizeDefault = 8

// ---------------------------
// Show parameters dialog
// ---------------------------
def params = new ParameterList()
    .addIntParameter("binSize", "Histogram bin size", binSizeDefault, "", 1, 50,
        "Number of foci counts to group into each histogram bin (1 = no binning)")

if (!GuiDialogs.showParameterDialog("Foci Histogram Settings", params)) {
    println "Report: cancelled by user."
    return
}

int binSize = params.getIntParameterValue("binSize")
if (binSize < 1) binSize = 1

println "Using bin size: ${binSize}"

// ---------------------------
// Get nuclei annotations with foci counts
// ---------------------------
def nuclei = getAnnotationObjects().findAll { 
    it.getPathClass()?.toString() == "Nuclei"
}

if (nuclei.isEmpty()) {
    GuiDialogs.showErrorMessage("Report",
        "No nuclei annotations found!\nPlease run the detection scripts first.")
    return
}

// Try to find foci count measurement (different scripts use different names)
def measurementName = null
def possibleNames = ["Foci: Count", "Foci: Count (nucleus)", "Num Foci", "Num Spots"]

for (name in possibleNames) {
    def hasIt = nuclei.any { it.getMeasurements().containsKey(name) }
    if (hasIt) {
        measurementName = name
        break
    }
}

if (measurementName == null) {
    GuiDialogs.showErrorMessage("Report",
        "No foci count measurements found!\nPlease run Spotiflow foci detection first.")
    return
}

println "Using measurement: ${measurementName}"

// ---------------------------
// Extract foci counts
// ---------------------------
def fociCounts = nuclei.collect { nucleus ->
    def val = nucleus.getMeasurements().get(measurementName)
    return (val != null) ? val.intValue() : 0
}

// ---------------------------
// Calculate statistics
// ---------------------------
int totalNuclei = fociCounts.size()
int totalFoci = fociCounts.sum() ?: 0
double meanFoci = totalFoci / (double) totalNuclei
int minFoci = fociCounts.min() ?: 0
int maxFoci = fociCounts.max() ?: 0

// Calculate median
def sorted = fociCounts.sort()
double medianFoci
if (totalNuclei % 2 == 0) {
    medianFoci = (sorted[totalNuclei/2 - 1] + sorted[totalNuclei/2]) / 2.0
} else {
    medianFoci = sorted[(totalNuclei - 1) / 2]
}

// Calculate standard deviation
double variance = fociCounts.collect { (it - meanFoci) ** 2 }.sum() / totalNuclei
double stdDev = Math.sqrt(variance)

// ---------------------------
// Build histogram data (bin the counts)
// ---------------------------
def histogram = [:].withDefault { 0 }
fociCounts.each { count ->
    // Calculate which bin this count falls into
    int binIndex = (count / binSize) as int
    histogram[binIndex]++
}

// Sort by bin index
def sortedBins = histogram.keySet().sort()

// Helper to format bin labels (returns proper String for JavaFX compatibility)
def formatBinLabel = { int binIndex ->
    int start = binIndex * binSize
    int end = start + binSize - 1
    if (binSize == 1) {
        return String.valueOf(start)
    } else {
        return String.valueOf(start) + "-" + String.valueOf(end)
    }
}

// ---------------------------
// Print statistics to log
// ---------------------------
println "\n========== Foci Distribution Report =========="
println "Total Nuclei: ${totalNuclei}"
println "Total Foci: ${totalFoci}"
println "Mean Foci/Nucleus: ${String.format('%.2f', meanFoci)}"
println "Median Foci/Nucleus: ${String.format('%.1f', medianFoci)}"
println "Min Foci: ${minFoci}"
println "Max Foci: ${maxFoci}"
println "Std Dev: ${String.format('%.2f', stdDev)}"
println "Bin Size: ${binSize}"
println "\nHistogram:"
sortedBins.each { binIndex ->
    def count = histogram[binIndex]
    def bar = "█" * Math.min(count, 50)
    def label = formatBinLabel(binIndex)
    println String.format("  %8s foci: %4d nuclei %s", label, count, bar)
}
println "=============================================="

// ---------------------------
// Create JavaFX histogram chart
// ---------------------------
Platform.runLater {
    try {
        def xAxis = new CategoryAxis()
        def xLabel = (binSize > 1) ? "Foci per Nucleus (bin size: " + binSize + ")" : "Foci per Nucleus"
        xAxis.setLabel(xLabel)
        
        def yAxis = new NumberAxis()
        yAxis.setLabel("Number of Nuclei")
        
        def chart = new BarChart<>(xAxis, yAxis)
        chart.setTitle("Distribution of Foci per Nucleus")
        chart.setLegendVisible(false)
        chart.setCategoryGap(1)
        chart.setBarGap(0)
        
        def series = new XYChart.Series<>()
        sortedBins.each { binIndex ->
            series.getData().add(new XYChart.Data<>(formatBinLabel(binIndex), histogram[binIndex]))
        }
        chart.getData().add(series)
        
        // Statistics label
        def statsText = String.format(
            "Nuclei: %d  |  Total Foci: %d  |  Mean: %.2f  |  Median: %.1f  |  Std Dev: %.2f  |  Range: %d - %d",
            totalNuclei, totalFoci, meanFoci, medianFoci, stdDev, minFoci, maxFoci
        )
        def statsLabel = new Label(statsText)
        statsLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;")
        
        def vbox = new VBox(10, chart, statsLabel)
        vbox.setPadding(new Insets(10))
        
        def scene = new Scene(vbox, 800, 500)
        
        def stage = new Stage()
        stage.setTitle("Foci Histogram Report")
        stage.setScene(scene)
        stage.show()
        
    } catch (Exception e) {
        println "Error creating histogram chart: ${e.getMessage()}"
        e.printStackTrace()
    }
}

// Also show a simple notification
def summaryMessage = String.format(
    "Nuclei analyzed: %d\nTotal foci: %d\n\nMean: %.2f foci/nucleus\nMedian: %.1f\nRange: %d - %d",
    totalNuclei, totalFoci, meanFoci, medianFoci, minFoci, maxFoci
)
GuiDialogs.showInfoNotification("Foci Analysis Complete", summaryMessage)
