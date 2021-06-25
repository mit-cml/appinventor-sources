# Image

Component for displaying images. The picture to display, and other aspects of the Image's appearance, can be specified in the Designer or in the Blocks Editor.

---

## Designer Properties

---

### AlternateText

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| AlternateText |    string   |               |

### Clickable

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   Clickable   |   boolean   |     False     |

### Picture

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Picture    |    asset    |               |

### RotationAngle

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| RotationAngle |    float    |      0.0      |

### ScalePictureToFit

|   Property Name   | Editor Type | Default Value |
| :---------------: | :---------: | :-----------: |
| ScalePictureToFit |   boolean   |     False     |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### Click

<div block-type = "component_event" component-selector = "Image" event-selector = "Click" event-params = "" id = "image-click"></div>

An event that occurs when an image is clicked.

## Block Properties

---

### AlternateText

<div block-type = "component_set_get" component-selector = "Image" property-selector = "AlternateText" property-type = "set" id = "set-image-alternatetext"></div>

A written description of what the image looks like.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| AlternateText |   text  |

### Animation

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Animation" property-type = "set" id = "set-image-animation"></div>

This is a limited form of animation that can attach a small number of motion types to images. The allowable motions are ScrollRightSlow, ScrollRight, ScrollRightFast, ScrollLeftSlow, ScrollLeft, ScrollLeftFast, and Stop

| Param Name | IO Type |
| :--------: | :-----: |
|  Animation |   text  |

### Clickable

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Clickable" property-type = "get" id = "get-image-clickable"></div>

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Clickable" property-type = "set" id = "set-image-clickable"></div>

Specifies whether the image should be clickable or not.

| Param Name | IO Type |
| :--------: | :-----: |
|  Clickable | boolean |

### Height

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Height" property-type = "get" id = "get-image-height"></div>

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Height" property-type = "set" id = "set-image-height"></div>

Specifies the vertical height of the Image, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "Image" property-selector = "HeightPercent" property-type = "set" id = "set-image-heightpercent"></div>

Specifies the vertical height of the Image as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Picture

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Picture" property-type = "get" id = "get-image-picture"></div>

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Picture" property-type = "set" id = "set-image-picture"></div>

Returns the path of the image's picture.

| Param Name | IO Type |
| :--------: | :-----: |
|   Picture  |   text  |

### RotationAngle

<div block-type = "component_set_get" component-selector = "Image" property-selector = "RotationAngle" property-type = "get" id = "get-image-rotationangle"></div>

<div block-type = "component_set_get" component-selector = "Image" property-selector = "RotationAngle" property-type = "set" id = "set-image-rotationangle"></div>

Specifies the angle, in degrees, at which the image picture appears rotated.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| RotationAngle |  number |

### ScalePictureToFit

<div block-type = "component_set_get" component-selector = "Image" property-selector = "ScalePictureToFit" property-type = "set" id = "set-image-scalepicturetofit"></div>

Specifies whether the image should be resized to match the size of the ImageView.

|     Param Name    | IO Type |
| :---------------: | :-----: |
| ScalePictureToFit | boolean |

### Scaling

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Scaling" property-type = "get" id = "get-image-scaling"></div>

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Scaling" property-type = "set" id = "set-image-scaling"></div>

This property determines how the picture scales according to the Height or Width of the Image. Scale proportionally (0) preserves the picture aspect ratio. Scale to fit (1) matches the Image area, even if the aspect ratio changes.

| Param Name | IO Type |
| :--------: | :-----: |
|   Scaling  |  number |

### Visible

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Visible" property-type = "get" id = "get-image-visible"></div>

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Visible" property-type = "set" id = "set-image-visible"></div>

Specifies whether the Image should be visible on the screen. Value is true if the Image is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Width" property-type = "get" id = "get-image-width"></div>

<div block-type = "component_set_get" component-selector = "Image" property-selector = "Width" property-type = "set" id = "set-image-width"></div>

Specifies the horizontal width of the Image, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "Image" property-selector = "WidthPercent" property-type = "set" id = "set-image-widthpercent"></div>

Specifies the horizontal width of the Image as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### Image

<div block-type = "component_component_block" component-selector = "Image" id = "component-image"></div>

Return Type : component

Component Image

