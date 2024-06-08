---
layout: documentation
title: Data Science
---

[&laquo; Back to index](index.html)
# Data Science

Table of Contents:

* [AnomalyDetection](#AnomalyDetection)
* [PersonalImageClassifier](#PersonalImageClassifier)
* [Regression](#Regression)

## AnomalyDetection  {#AnomalyDetection}

A data science component to apply different anomaly detection models.
 The component only needs a data source to apply the model on.

 The anomaly detection models only return a list of anomalies.
 ChartData2D component is needed to highlight the anomalies on a chart



### Properties  {#AnomalyDetection-Properties}

{:.properties}
None


### Events  {#AnomalyDetection-Events}

{:.events}
None


### Methods  {#AnomalyDetection-Methods}

{:.methods}

{:id="AnomalyDetection.CleanData" class="method returns list"} <i/> CleanData(*anomaly*{:.list},*xList*{:.list},*yList*{:.list})
: Given a single anomaly: [(anomaly index, anomaly value)]

 1. Iterate over the xList and delete value at anomaly index
 2. Iterate over the yList and delete the value at anomaly index with the same value as anomaly
    value
 3. combine the xList and yList after modification in a list of x and y pairs

 We assume x and y lists are the same size and are ordered.

{:id="AnomalyDetection.DetectAnomalies" class="method returns list"} <i/> DetectAnomalies(*dataList*{:.list},*threshold*{:.number})
: Calculates the mean and standard deviation of the data, and then checks each data point's
 Z-score against the threshold. If a data point's Z-score is greater than the threshold,
 the data point is labeled as anomaly.

{:id="AnomalyDetection.DetectAnomaliesInChartData" class="method returns list"} <i/> DetectAnomaliesInChartData(*chartData*{:.component},*threshold*{:.number})
: Detects anomalies in the given chart data object by comparing Y values to the threshold based
 on their standard deviation to the mean.

## PersonalImageClassifier  {#PersonalImageClassifier}

Component for PersonalImageClassifier



### Properties  {#PersonalImageClassifier-Properties}

{:.properties}

{:id="PersonalImageClassifier.InputMode" .text} *InputMode*
: Gets or sets the input mode for classification. Valid values are "Video" (the default) and "Image".

{:id="PersonalImageClassifier.MinimumInterval" .number} *MinimumInterval*
: Property for MinimumInterval

{:id="PersonalImageClassifier.Model" .text .wo .do} *Model*
: Property for Model

{:id="PersonalImageClassifier.ModelLabels" .list .ro .bo} *ModelLabels*
: Gets all of the labels from this model. Only valid after ClassifierReady is signaled.

{:id="PersonalImageClassifier.Running" .boolean .ro .bo} *Running*
: Property for Running

{:id="PersonalImageClassifier.WebViewer" .component .wo .do} *WebViewer*
: Property for WebViewer

### Events  {#PersonalImageClassifier-Events}

{:.events}
None


### Methods  {#PersonalImageClassifier-Methods}

{:.methods}

{:id="PersonalImageClassifier.ClassifyImageData" class="method"} <i/> ClassifyImageData(*image*{:.text})
: Performs classification on the image at the given path and triggers the GotClassification event when classification is finished successfully.

{:id="PersonalImageClassifier.ClassifyVideoData" class="method"} <i/> ClassifyVideoData()
: Performs classification on current video frame and triggers the GotClassification event when classification is finished successfully.

{:id="PersonalImageClassifier.StartContinuousClassification" class="method"} <i/> StartContinuousClassification()
: Starts continuous video classification if the input mode is set to video and the classification is not already running.

{:id="PersonalImageClassifier.StopContinuousClassification" class="method"} <i/> StopContinuousClassification()
: Stop continuous video classification if the input mode is set to video and the classification is running.

{:id="PersonalImageClassifier.ToggleCameraFacingMode" class="method"} <i/> ToggleCameraFacingMode()
: Toggles between user-facing and environment-facing camera.

## Regression  {#Regression}

A data science component to apply different regression models.
 The component only requires a data source to apply the model on.

   The component is only responsible for the statistical calculations and
 provides the following properties for line of best fit:
 "slope", "Yintercept", "correlation coefficient", and "predictions"

   To draw the line of best fit use the drawing block in ChartData2D component



### Properties  {#Regression-Properties}

{:.properties}
None


### Events  {#Regression-Events}

{:.events}
None


### Methods  {#Regression-Methods}

{:.methods}

{:id="Regression.CalculateLineOfBestFitValue" class="method returns any"} <i/> CalculateLineOfBestFitValue(*xList*{:.list},*yList*{:.list},*value*{:.text})
: Returns one of the Line of Best Fit values.
 A value could be "slope", "Yintercept", "correlation coefficient", "predictions" or a
 dictionary when AllValues is specified.
