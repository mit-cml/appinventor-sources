# CheckBox

Checkbox that raises an event when the user clicks on it. There are many properties affecting its appearance that can be set in the Designer or Blocks Editor.

---

## Designer Properties

---

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &H00FFFFFF  |

### Checked

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Checked    |   boolean   |     False     |

### Enabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Enabled    |   boolean   |      True     |

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

### Text

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|      Text     |    string   |               |

### TextColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   TextColor   |    color    |   &HFF000000  |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### Changed

<div block-type = "component_event" component-selector = "CheckBox" event-selector = "Changed" event-params = "" id = "checkbox-changed"></div>

User tapped and released the CheckBox.

### GotFocus

<div block-type = "component_event" component-selector = "CheckBox" event-selector = "GotFocus" event-params = "" id = "checkbox-gotfocus"></div>

CheckBox became the focused component.

### LostFocus

<div block-type = "component_event" component-selector = "CheckBox" event-selector = "LostFocus" event-params = "" id = "checkbox-lostfocus"></div>

CheckBox stopped being the focused component.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "BackgroundColor" property-type = "get" id = "get-checkbox-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "BackgroundColor" property-type = "set" id = "set-checkbox-backgroundcolor"></div>

The background color of the CheckBox as an alpha-red-green-blue integer.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Checked

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Checked" property-type = "get" id = "get-checkbox-checked"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Checked" property-type = "set" id = "set-checkbox-checked"></div>

True if the box is checked, false otherwise.

| Param Name | IO Type |
| :--------: | :-----: |
|   Checked  | boolean |

### Enabled

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Enabled" property-type = "get" id = "get-checkbox-enabled"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Enabled" property-type = "set" id = "set-checkbox-enabled"></div>

True if the CheckBox is active and clickable.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "FontSize" property-type = "get" id = "get-checkbox-fontsize"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "FontSize" property-type = "set" id = "set-checkbox-fontsize"></div>

Specifies the text font size of the CheckBox in scale-independent pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Height" property-type = "get" id = "get-checkbox-height"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Height" property-type = "set" id = "set-checkbox-height"></div>

Specifies the vertical height of the CheckBox, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "HeightPercent" property-type = "set" id = "set-checkbox-heightpercent"></div>

Specifies the vertical height of the CheckBox as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Text

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Text" property-type = "get" id = "get-checkbox-text"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Text" property-type = "set" id = "set-checkbox-text"></div>

Specifies the text displayed by the CheckBox.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "TextColor" property-type = "get" id = "get-checkbox-textcolor"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "TextColor" property-type = "set" id = "set-checkbox-textcolor"></div>

Specifies the text color of the CheckBox as an alpha-red-green-blue integer.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Visible" property-type = "get" id = "get-checkbox-visible"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Visible" property-type = "set" id = "set-checkbox-visible"></div>

Specifies whether the CheckBox should be visible on the screen. Value is true if the CheckBox is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Width" property-type = "get" id = "get-checkbox-width"></div>

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "Width" property-type = "set" id = "set-checkbox-width"></div>

Specifies the horizontal width of the CheckBox, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "CheckBox" property-selector = "WidthPercent" property-type = "set" id = "set-checkbox-widthpercent"></div>

Specifies the horizontal width of the CheckBox as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### CheckBox

<div block-type = "component_component_block" component-selector = "CheckBox" id = "component-checkbox"></div>

Return Type : component

Component CheckBox

