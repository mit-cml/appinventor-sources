---
layout: documentation
title: Extension
---

[&laquo; Back to index](index.html)
# Extension

Table of Contents:

* [PosenetExtension](#PosenetExtension)

## PosenetExtension  {#PosenetExtension}

### Properties  {#PosenetExtension-Properties}

{:.properties}

{:id="PosenetExtension.Enabled" .boolean} *Enabled*
: Enables or disables pose detection.

{:id="PosenetExtension.KeyPoints" .list .ro .bo} *KeyPoints*
: A list of points representing body parts that met the minimum part confidence threshold.

{:id="PosenetExtension.LeftAnkle" .list .ro .bo} *LeftAnkle*
: Position of the left ankle in the video frame as an (x, y) pair. If no left ankle is detected, it returns an empty list.

{:id="PosenetExtension.LeftEar" .list .ro .bo} *LeftEar*
: Position of the left ear in the video frame as an (x, y) pair. If no left ear is detected, it returns an empty list.

{:id="PosenetExtension.LeftElbow" .list .ro .bo} *LeftElbow*
: Position of the left elbow in the video frame as an (x, y) pair. If no left elbow is detected, it returns an empty list.

{:id="PosenetExtension.LeftEye" .list .ro .bo} *LeftEye*
: Position of the left eye in the video frame as an (x, y) pair. If no left eye is detected, it returns an empty list.

{:id="PosenetExtension.LeftHip" .list .ro .bo} *LeftHip*
: Position of the left hip in the video frame as an (x, y) pair. If no left hip is detected, it returns an empty list.

{:id="PosenetExtension.LeftKnee" .list .ro .bo} *LeftKnee*
: Position of the left knee in the video frame as an (x, y) pair. If no left knee is detected, it returns an empty list.

{:id="PosenetExtension.LeftShoulder" .list .ro .bo} *LeftShoulder*
: Position of the left shoulder in the video frame as an (x, y) pair. If no left shoulder is detected, it returns an empty list.

{:id="PosenetExtension.LeftWrist" .list .ro .bo} *LeftWrist*
: Position of the left wrist in the video frame as an (x, y) pair. If no left wrist is detected, it returns an empty list.

{:id="PosenetExtension.MinPartConfidence" .number} *MinPartConfidence*
: The minimum amount of confidence to detect a body part.

{:id="PosenetExtension.MinPoseConfidence" .number} *MinPoseConfidence*
: The minimum confidence required to detect a pose.

{:id="PosenetExtension.Nose" .list .ro .bo} *Nose*
: Position of the nose in the video frame as an (x, y) pair. If no nose is detected, it returns an empty list.

{:id="PosenetExtension.RightAnkle" .list .ro .bo} *RightAnkle*
: Position of the right ankle in the video frame as an (x, y) pair. If no right ankle is detected, it returns an empty list.

{:id="PosenetExtension.RightEar" .list .ro .bo} *RightEar*
: Position of the right ear in the video frame as an (x, y) pair. If no right ear is detected, it returns an empty list.

{:id="PosenetExtension.RightElbow" .list .ro .bo} *RightElbow*
: Position of the right elbow in the video frame as an (x, y) pair. If no right eblow is detected, it returns an empty list.

{:id="PosenetExtension.RightEye" .list .ro .bo} *RightEye*
: Position of the right eye in the video frame as an (x, y) pair. If no right eye is detected, it returns an empty list.

{:id="PosenetExtension.RightHip" .list .ro .bo} *RightHip*
: Position of the right hip in the video frame as an (x, y) pair. If no right hip is detected, it returns an empty list.

{:id="PosenetExtension.RightKnee" .list .ro .bo} *RightKnee*
: Position of the right knee in the video frame as an (x, y) pair. If no right knee is detected, it returns an empty list.

{:id="PosenetExtension.RightShoulder" .list .ro .bo} *RightShoulder*
: Position of the right shoulder in the video frame as an (x, y) pair. If no right shoulder is detected, it returns an empty list.

{:id="PosenetExtension.RightWrist" .list .ro .bo} *RightWrist*
: Position of the right wrist the video frame as an (x, y) pair. If no right wrist is detected, it returns an empty list.

{:id="PosenetExtension.Skeleton" .list .ro .bo} *Skeleton*
: A list of pairs of points representing connections between valid body parts.

{:id="PosenetExtension.UseCamera" .text} *UseCamera*
: Configures Posenet to use the front or back camera on the device.

{:id="PosenetExtension.WebViewer" .component .wo .do} *WebViewer*
: 

{:id="PosenetExtension.backgroundImage" .text .ro .bo} *backgroundImage*
: BackGround Image.

### Events  {#PosenetExtension-Events}

{:.events}

{:id="PosenetExtension.Error"} Error(*errorCode*{:.number},*errorMessage*{:.text})
: Event indicating that an error has occurred.

{:id="PosenetExtension.ModelReady"} ModelReady()
: Event indicating that the classifier is ready.

{:id="PosenetExtension.PoseUpdated"} PoseUpdated()
: Event indicating that classification has finished successfully. Result is of the form [[class1, confidence1], ...]