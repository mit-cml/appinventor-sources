---
layout: documentation
title: Extension
---

[&laquo; Back to index](index.html)
# Extension

Table of Contents:

* [TeachableMachine](#TeachableMachine)

## TeachableMachine  {#TeachableMachine}

Component that classifies images using a user trained model from the image classification explorer.
 Based heavily on the Look Extension by kevinzhu



### Properties  {#TeachableMachine-Properties}

{:.properties}

{:id="TeachableMachine.InputMode" .text} *InputMode*
: Gets or sets the input mode for classification. Valid values are "Video" (the default) and "Image".

{:id="TeachableMachine.MinimumInterval" .number} *MinimumInterval*
: Property for MinimumInterval

{:id="TeachableMachine.ModelLabels" .list .ro .bo} *ModelLabels*
: Gets all of the labels from this model. Only valid after ClassifierReady is signaled.

{:id="TeachableMachine.Running" .boolean .ro .bo} *Running*
: Property for Running

{:id="TeachableMachine.WebViewer" .component .wo .do} *WebViewer*
: Property for WebViewer

### Events  {#TeachableMachine-Events}

{:.events}

{:id="TeachableMachine.ClassifierReady"} ClassifierReady()
: Event indicating that the classifier is ready.

{:id="TeachableMachine.Error"} Error(*errorCode*{:.number})
: Event indicating that an error has occurred.

{:id="TeachableMachine.GotClassification"} GotClassification(*result*{:.dictionary})
: Event indicating that classification has finished successfully. Result is of the form [[class1, confidence1], [class2, confidence2], ..., [class10, confidence10]].

### Methods  {#TeachableMachine-Methods}

{:.methods}

{:id="TeachableMachine.ClassifyImageData" class="method"} <i/> ClassifyImageData(*image*{:.text})
: Performs classification on the image at the given path and triggers the GotClassification event when classification is finished successfully.

{:id="TeachableMachine.ClassifyVideoData" class="method"} <i/> ClassifyVideoData()
: Performs classification on current video frame and triggers the GotClassification event when classification is finished successfully.

{:id="TeachableMachine.StartContinuousClassification" class="method"} <i/> StartContinuousClassification()
: Method for StartContinuousClassification

{:id="TeachableMachine.StopContinuousClassification" class="method"} <i/> StopContinuousClassification()
: Method for StopContinuousClassification

{:id="TeachableMachine.ToggleCameraFacingMode" class="method"} <i/> ToggleCameraFacingMode()
: Toggles between user-facing and environment-facing camera.
