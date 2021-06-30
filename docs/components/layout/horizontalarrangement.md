<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# HorizontalArrangement

A formatting element in which to place components that should be displayed from left to right. If you wish to have components displayed one over another, use `VerticalArrangement` instead.

---

## Designer Properties

---

### AlignHorizontal

|  Property Name  |      Editor Type     | Default Value |
| :-------------: | :------------------: | :-----------: |
| AlignHorizontal | horizontal_alignment |       1       |

### AlignVertical

| Property Name |     Editor Type    | Default Value |
| :-----------: | :----------------: | :-----------: |
| AlignVertical | vertical_alignment |       1       |

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &H00000000  |

### Image

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Image     |    asset    |               |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Block Properties

---

### AlignHorizontal

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "AlignHorizontal" property-type = "get" id = "get-horizontalarrangement-alignhorizontal"></div>

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "AlignHorizontal" property-type = "set" id = "set-horizontalarrangement-alignhorizontal"></div>

A number that encodes how contents of the HorizontalArrangement are aligned horizontally. The choices are: 1 = left aligned, 2 = right aligned, 3 = horizontally centered. Alignment has no effect if the arrangement's width is automatic.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| AlignHorizontal |  number |

### AlignVertical

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "AlignVertical" property-type = "get" id = "get-horizontalarrangement-alignvertical"></div>

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "AlignVertical" property-type = "set" id = "set-horizontalarrangement-alignvertical"></div>

A number that encodes how the contents of the HorizontalArrangement are aligned vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, 3 = aligned at the bottom. Alignment has no effect if the arrangement's height is automatic.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| AlignVertical |  number |

### BackgroundColor

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "BackgroundColor" property-type = "get" id = "get-horizontalarrangement-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "BackgroundColor" property-type = "set" id = "set-horizontalarrangement-backgroundcolor"></div>

Returns the background color of the HorizontalArrangement

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Height

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "Height" property-type = "get" id = "get-horizontalarrangement-height"></div>

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "Height" property-type = "set" id = "set-horizontalarrangement-height"></div>

Specifies the vertical height of the HorizontalArrangement, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "HeightPercent" property-type = "set" id = "set-horizontalarrangement-heightpercent"></div>

Specifies the vertical height of the HorizontalArrangement as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Image

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "Image" property-type = "get" id = "get-horizontalarrangement-image"></div>

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "Image" property-type = "set" id = "set-horizontalarrangement-image"></div>

Returns the path of the background image of the \`HorizontalArrangement\`.

| Param Name | IO Type |
| :--------: | :-----: |
|    Image   |   text  |

### Visible

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "Visible" property-type = "get" id = "get-horizontalarrangement-visible"></div>

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "Visible" property-type = "set" id = "set-horizontalarrangement-visible"></div>

Specifies whether the HorizontalArrangement should be visible on the screen. Value is true if the HorizontalArrangement is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "Width" property-type = "get" id = "get-horizontalarrangement-width"></div>

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "Width" property-type = "set" id = "set-horizontalarrangement-width"></div>

Specifies the horizontal width of the HorizontalArrangement, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "HorizontalArrangement" property-selector = "WidthPercent" property-type = "set" id = "set-horizontalarrangement-widthpercent"></div>

Specifies the horizontal width of the HorizontalArrangement as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### HorizontalArrangement

<div block-type = "component_component_block" component-selector = "HorizontalArrangement" id = "component-horizontalarrangement"></div>

Return Type : component

Component HorizontalArrangement

