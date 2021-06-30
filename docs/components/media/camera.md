<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Camera

A component to take a picture using the device's camera. After the picture is taken, the name of the file on the phone containing the picture is available as an argument to the AfterPicture event. The file name can be used, for example, to set the Picture property of an Image component.

---

## Events

---

### AfterPicture

<div block-type = "component_event" component-selector = "Camera" event-selector = "AfterPicture" id = "camera-afterpicture"></div>

Called after the picture is taken. The text argument \`image\` is the path that can be used to locate the image on the phone.

| Param Name | IO Type |
| :--------: | :-----: |
|    image   |   text  |

## Methods

---

### TakePicture

<div block-type = "component_method" component-selector = "Camera" method-selector = "TakePicture" id = "camera-takepicture"></div>

Return Type : No Return Value

Takes a picture, then raises the

## Block Properties

---

## Component

---

### Camera

<div block-type = "component_component_block" component-selector = "Camera" id = "component-camera"></div>

Return Type : component

Component Camera

