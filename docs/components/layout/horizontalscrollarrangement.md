# HorizontalScrollArrangement

A formatting element in which to place components that should be displayed from left to right. If you wish to have components displayed one over another, use `VerticalArrangement` instead.

This version is scrollable.

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

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "AlignHorizontal" property-type = "get" id = "get-horizontalscrollarrangement-alignhorizontal"></div>

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "AlignHorizontal" property-type = "set" id = "set-horizontalscrollarrangement-alignhorizontal"></div>

A number that encodes how contents of the HorizontalScrollArrangement are aligned horizontally. The choices are: 1 = left aligned, 2 = right aligned, 3 = horizontally centered. Alignment has no effect if the arrangement's width is automatic.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| AlignHorizontal |  number |

### AlignVertical

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "AlignVertical" property-type = "get" id = "get-horizontalscrollarrangement-alignvertical"></div>

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "AlignVertical" property-type = "set" id = "set-horizontalscrollarrangement-alignvertical"></div>

A number that encodes how the contents of the HorizontalScrollArrangement are aligned vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, 3 = aligned at the bottom. Alignment has no effect if the arrangement's height is automatic.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| AlignVertical |  number |

### BackgroundColor

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "BackgroundColor" property-type = "get" id = "get-horizontalscrollarrangement-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "BackgroundColor" property-type = "set" id = "set-horizontalscrollarrangement-backgroundcolor"></div>

Returns the background color of the HorizontalScrollArrangement

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Height

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "Height" property-type = "get" id = "get-horizontalscrollarrangement-height"></div>

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "Height" property-type = "set" id = "set-horizontalscrollarrangement-height"></div>

Specifies the vertical height of the HorizontalScrollArrangement, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "HeightPercent" property-type = "set" id = "set-horizontalscrollarrangement-heightpercent"></div>

Specifies the vertical height of the HorizontalScrollArrangement as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Image

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "Image" property-type = "get" id = "get-horizontalscrollarrangement-image"></div>

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "Image" property-type = "set" id = "set-horizontalscrollarrangement-image"></div>

Returns the path of the background image of the \`HorizontalScrollArrangement\`.

| Param Name | IO Type |
| :--------: | :-----: |
|    Image   |   text  |

### Visible

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "Visible" property-type = "get" id = "get-horizontalscrollarrangement-visible"></div>

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "Visible" property-type = "set" id = "set-horizontalscrollarrangement-visible"></div>

Specifies whether the HorizontalScrollArrangement should be visible on the screen. Value is true if the HorizontalScrollArrangement is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "Width" property-type = "get" id = "get-horizontalscrollarrangement-width"></div>

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "Width" property-type = "set" id = "set-horizontalscrollarrangement-width"></div>

Specifies the horizontal width of the HorizontalScrollArrangement, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "HorizontalScrollArrangement" property-selector = "WidthPercent" property-type = "set" id = "set-horizontalscrollarrangement-widthpercent"></div>

Specifies the horizontal width of the HorizontalScrollArrangement as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### HorizontalScrollArrangement

<div block-type = "component_component_block" component-selector = "HorizontalScrollArrangement" id = "component-horizontalscrollarrangement"></div>

Return Type : component

Component HorizontalScrollArrangement

