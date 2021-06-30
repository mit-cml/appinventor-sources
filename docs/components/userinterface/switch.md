<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Switch

Toggle switch that raises an event when the user clicks on it. There are many properties affecting its appearance that can be set in the Designer or Blocks Editor.

---

## Designer Properties

---

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &H00FFFFFF  |

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

### On

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|       On      |   boolean   |     False     |

### Text

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|      Text     |    string   |               |

### TextColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   TextColor   |    color    |   &HFF000000  |

### ThumbColorActive

|   Property Name  | Editor Type | Default Value |
| :--------------: | :---------: | :-----------: |
| ThumbColorActive |    color    |   &HFFFFFFFF  |

### ThumbColorInactive

|    Property Name   | Editor Type | Default Value |
| :----------------: | :---------: | :-----------: |
| ThumbColorInactive |    color    |   &HFFCCCCCC  |

### TrackColorActive

|   Property Name  | Editor Type | Default Value |
| :--------------: | :---------: | :-----------: |
| TrackColorActive |    color    |   &HFF00FF00  |

### TrackColorInactive

|    Property Name   | Editor Type | Default Value |
| :----------------: | :---------: | :-----------: |
| TrackColorInactive |    color    |   &HFF444444  |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### Changed

<div block-type = "component_event" component-selector = "Switch" event-selector = "Changed" id = "switch-changed"></div>

User change the state of the \`Switch\` from On to Off or back.

### GotFocus

<div block-type = "component_event" component-selector = "Switch" event-selector = "GotFocus" id = "switch-gotfocus"></div>

Switch became the focused component.

### LostFocus

<div block-type = "component_event" component-selector = "Switch" event-selector = "LostFocus" id = "switch-lostfocus"></div>

Switch stopped being the focused component.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "BackgroundColor" property-type = "get" id = "get-switch-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "BackgroundColor" property-type = "set" id = "set-switch-backgroundcolor"></div>

The background color of the Switch as an alpha-red-green-blue integer.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Enabled" property-type = "get" id = "get-switch-enabled"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Enabled" property-type = "set" id = "set-switch-enabled"></div>

True if the Switch is active and clickable.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "FontSize" property-type = "get" id = "get-switch-fontsize"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "FontSize" property-type = "set" id = "set-switch-fontsize"></div>

Specifies the text font size of the Switch in scale-independent pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Height" property-type = "get" id = "get-switch-height"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Height" property-type = "set" id = "set-switch-height"></div>

Specifies the vertical height of the Switch, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "HeightPercent" property-type = "set" id = "set-switch-heightpercent"></div>

Specifies the vertical height of the Switch as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### On

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "On" property-type = "get" id = "get-switch-on"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "On" property-type = "set" id = "set-switch-on"></div>

Returns true if the \`Switch\` is on.

| Param Name | IO Type |
| :--------: | :-----: |
|     On     | boolean |

### Text

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Text" property-type = "get" id = "get-switch-text"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Text" property-type = "set" id = "set-switch-text"></div>

Specifies the text displayed by the Switch.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "TextColor" property-type = "get" id = "get-switch-textcolor"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "TextColor" property-type = "set" id = "set-switch-textcolor"></div>

Specifies the text color of the Switch as an alpha-red-green-blue integer.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### ThumbColorActive

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "ThumbColorActive" property-type = "get" id = "get-switch-thumbcoloractive"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "ThumbColorActive" property-type = "set" id = "set-switch-thumbcoloractive"></div>

Returns the \`Switch\`'s thumb color (button that toggles back and forth) when the switch is ON/Checked

|    Param Name    | IO Type |
| :--------------: | :-----: |
| ThumbColorActive |  number |

### ThumbColorInactive

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "ThumbColorInactive" property-type = "get" id = "get-switch-thumbcolorinactive"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "ThumbColorInactive" property-type = "set" id = "set-switch-thumbcolorinactive"></div>

Returns the \`Switch\`'s thumb color (button that toggles back and forth) when the switch is Off/Unchecked

|     Param Name     | IO Type |
| :----------------: | :-----: |
| ThumbColorInactive |  number |

### TrackColorActive

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "TrackColorActive" property-type = "get" id = "get-switch-trackcoloractive"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "TrackColorActive" property-type = "set" id = "set-switch-trackcoloractive"></div>

Returns the \`Switch\`'s track color

|    Param Name    | IO Type |
| :--------------: | :-----: |
| TrackColorActive |  number |

### TrackColorInactive

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "TrackColorInactive" property-type = "get" id = "get-switch-trackcolorinactive"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "TrackColorInactive" property-type = "set" id = "set-switch-trackcolorinactive"></div>

Color of the toggle track when switched off

|     Param Name     | IO Type |
| :----------------: | :-----: |
| TrackColorInactive |  number |

### Visible

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Visible" property-type = "get" id = "get-switch-visible"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Visible" property-type = "set" id = "set-switch-visible"></div>

Specifies whether the Switch should be visible on the screen. Value is true if the Switch is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Width" property-type = "get" id = "get-switch-width"></div>

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "Width" property-type = "set" id = "set-switch-width"></div>

Specifies the horizontal width of the Switch, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "Switch" property-selector = "WidthPercent" property-type = "set" id = "set-switch-widthpercent"></div>

Specifies the horizontal width of the Switch as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### Switch

<div block-type = "component_component_block" component-selector = "Switch" id = "component-switch"></div>

Return Type : component

Component Switch

