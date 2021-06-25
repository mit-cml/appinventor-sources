# Label

A Label displays a piece of text, which is specified through the `Text` property. Other properties, all of which can be set in the Designer or Blocks Editor, control the appearance and placement of the text.

---

## Designer Properties

---

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &H00FFFFFF  |

### FontBold

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    FontBold   |   boolean   |     False     |

### FontItalic

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   FontItalic  |   boolean   |     False     |

### FontSize

| Property Name |     Editor Type    | Default Value |
| :-----------: | :----------------: | :-----------: |
|    FontSize   | non_negative_float |      14.0     |

### FontTypeface

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  FontTypeface |   typeface  |    default    |

### HTMLFormat

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   HTMLFormat  |   boolean   |     False     |

### HasMargins

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   HasMargins  |   boolean   |      True     |

### Text

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|      Text     |   textArea  |               |

### TextAlignment

| Property Name |  Editor Type  | Default Value |
| :-----------: | :-----------: | :-----------: |
| TextAlignment | textalignment |       0       |

### TextColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   TextColor   |    color    |   &HFF000000  |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "Label" property-selector = "BackgroundColor" property-type = "get" id = "get-label-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "Label" property-selector = "BackgroundColor" property-type = "set" id = "set-label-backgroundcolor"></div>

Returns the label's background color as an alpha-red-green-blue integer.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### FontSize

<div block-type = "component_set_get" component-selector = "Label" property-selector = "FontSize" property-type = "get" id = "get-label-fontsize"></div>

<div block-type = "component_set_get" component-selector = "Label" property-selector = "FontSize" property-type = "set" id = "set-label-fontsize"></div>

Returns the label's text's font size, measured in sp(scale-independent pixels).

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### HTMLContent

<div block-type = "component_set_get" component-selector = "Label" property-selector = "HTMLContent" property-type = "get" id = "get-label-htmlcontent"></div>

Returns the content of the Label as HTML. This is only useful if the HTMLFormat property is true.

|  Param Name | IO Type |
| :---------: | :-----: |
| HTMLContent |   text  |

### HasMargins

<div block-type = "component_set_get" component-selector = "Label" property-selector = "HasMargins" property-type = "get" id = "get-label-hasmargins"></div>

<div block-type = "component_set_get" component-selector = "Label" property-selector = "HasMargins" property-type = "set" id = "set-label-hasmargins"></div>

Reports whether or not the label appears with margins. All four margins (left, right, top, bottom) are the same. This property has no effect in the designer, where labels are always shown with margins.

| Param Name | IO Type |
| :--------: | :-----: |
| HasMargins | boolean |

### Height

<div block-type = "component_set_get" component-selector = "Label" property-selector = "Height" property-type = "get" id = "get-label-height"></div>

<div block-type = "component_set_get" component-selector = "Label" property-selector = "Height" property-type = "set" id = "set-label-height"></div>

Specifies the vertical height of the Label, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "Label" property-selector = "HeightPercent" property-type = "set" id = "set-label-heightpercent"></div>

Specifies the vertical height of the Label as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Text

<div block-type = "component_set_get" component-selector = "Label" property-selector = "Text" property-type = "get" id = "get-label-text"></div>

<div block-type = "component_set_get" component-selector = "Label" property-selector = "Text" property-type = "set" id = "set-label-text"></div>

Returns the text displayed by the label.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "Label" property-selector = "TextColor" property-type = "get" id = "get-label-textcolor"></div>

<div block-type = "component_set_get" component-selector = "Label" property-selector = "TextColor" property-type = "set" id = "set-label-textcolor"></div>

Returns the label's text color as an alpha-red-green-blue integer.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "Label" property-selector = "Visible" property-type = "get" id = "get-label-visible"></div>

<div block-type = "component_set_get" component-selector = "Label" property-selector = "Visible" property-type = "set" id = "set-label-visible"></div>

Specifies whether the Label should be visible on the screen. Value is true if the Label is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Label" property-selector = "Width" property-type = "get" id = "get-label-width"></div>

<div block-type = "component_set_get" component-selector = "Label" property-selector = "Width" property-type = "set" id = "set-label-width"></div>

Specifies the horizontal width of the Label, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "Label" property-selector = "WidthPercent" property-type = "set" id = "set-label-widthpercent"></div>

Specifies the horizontal width of the Label as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### Label

<div block-type = "component_component_block" component-selector = "Label" id = "component-label"></div>

Return Type : component

Component Label

