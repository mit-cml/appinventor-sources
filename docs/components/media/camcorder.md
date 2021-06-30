<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Camcorder

A component to record a video using the device's camcorder.After the video is recorded, the name of the file on the phone containing the clip is available as an argument to the AfterRecording event. The file name can be used, for example, to set the source property of a VideoPlayer component.

---

## Events

---

### AfterRecording

<div block-type = "component_event" component-selector = "Camcorder" event-selector = "AfterRecording" id = "camcorder-afterrecording"></div>

Indicates that a video was recorded with the camera and provides the path to the stored video.

| Param Name | IO Type |
| :--------: | :-----: |
|    clip    |   text  |

## Methods

---

### RecordVideo

<div block-type = "component_method" component-selector = "Camcorder" method-selector = "RecordVideo" id = "camcorder-recordvideo"></div>

Return Type : No Return Value

Records a video, then raises the

## Component

---

### Camcorder

<div block-type = "component_component_block" component-selector = "Camcorder" id = "component-camcorder"></div>

Return Type : component

Component Camcorder

