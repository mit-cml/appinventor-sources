---
layout: documentation
title: Data Science
---

[&laquo; Back to index](index.html)
# Data Science

Table of Contents:

* [AnomalyDetection](#AnomalyDetection)
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
