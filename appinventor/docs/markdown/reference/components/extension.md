---
layout: documentation
title: Extension
---

[&laquo; Back to index](index.html)
# Extension

Table of Contents:

* [PersonalImageClassifier](#PersonalImageClassifier)

## PersonalImageClassifier  {#PersonalImageClassifier}

Component that classifies images using a user trained model from the image classification explorer.
 Based heavily on the Look Extension by kevinzhu



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

{:id="PersonalImageClassifier.ClassifierReady"} ClassifierReady()
: Event indicating that the classifier is ready.

{:id="PersonalImageClassifier.Error"} Error(*errorCode*{:.number})
: Event indicating that an error has occurred.

{:id="PersonalImageClassifier.GotClassification"} GotClassification(*result*{:.dictionary})
: Event indicating that classification has finished successfully. Result is of the form [[class1, confidence1], [class2, confidence2], ..., [class10, confidence10]].

### Methods  {#PersonalImageClassifier-Methods}

{:.methods}

{:id="PersonalImageClassifier.ClassifyImageData" class="method"} <i/> ClassifyImageData(*image*{:.text})
: Performs classification on the image at the given path and triggers the GotClassification event when classification is finished successfully.

{:id="PersonalImageClassifier.ClassifyVideoData" class="method"} <i/> ClassifyVideoData()
: Performs classification on current video frame and triggers the GotClassification event when classification is finished successfully.

{:id="PersonalImageClassifier.StartContinuousClassification" class="method"} <i/> StartContinuousClassification()
: Method for StartContinuousClassification

{:id="PersonalImageClassifier.StopContinuousClassification" class="method"} <i/> StopContinuousClassification()
: Method for StopContinuousClassification

{:id="PersonalImageClassifier.ToggleCameraFacingMode" class="method"} <i/> ToggleCameraFacingMode()
: Toggles between user-facing and environment-facing camera.
