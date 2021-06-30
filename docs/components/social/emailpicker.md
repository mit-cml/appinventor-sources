<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# EmailPicker

An EmailPicker is a kind of text box. If the user begins entering the name or email address of a contact, the phone will show a dropdown menu of choices that complete the entry. If there are many contacts, the dropdown can take several seconds to appear, and can show intermediate results while the matches are being computed.

The initial contents of the text box and the contents< after user entry is in the `Text` property. If the `Text` property is initially empty, the contents of the `Hint` property will be faintly shown in the text box as a hint to the user.

Other properties affect the appearance of the text box (`TextAlignment`, `BackgroundColor`, etc.) and whether it can be used (`Enabled`).

Text boxes like this are usually used with `Button` components, with the user clicking on the button when text entry is complete.

---

## Designer Properties

---

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &H00000000  |

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

### Hint

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|      Hint     |    string   |               |

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
|   TextColor   |    color    |   &H00000000  |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### GotFocus

<div block-type = "component_event" component-selector = "EmailPicker" event-selector = "GotFocus" id = "emailpicker-gotfocus"></div>

Event raised when the \`EmailPicker\` is selected for input, such as by the user touching it.

### LostFocus

<div block-type = "component_event" component-selector = "EmailPicker" event-selector = "LostFocus" id = "emailpicker-lostfocus"></div>

Event raised when the EmailPicker is no longer selected for input, such as if the user touches a different text box.

## Methods

---

### RequestFocus

<div block-type = "component_method" component-selector = "EmailPicker" method-selector = "RequestFocus" id = "emailpicker-requestfocus"></div>

Return Type : No Return Value

Sets the EmailPicker active.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "BackgroundColor" property-type = "get" id = "get-emailpicker-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "BackgroundColor" property-type = "set" id = "set-emailpicker-backgroundcolor"></div>

The background color of the input box. You can choose a color by name in the Designer or in the Blocks Editor. The default background color is 'default' (shaded 3-D look).

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Enabled" property-type = "get" id = "get-emailpicker-enabled"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Enabled" property-type = "set" id = "set-emailpicker-enabled"></div>

Whether the user can enter text into the EmailPicker. By default, this is true.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "FontSize" property-type = "get" id = "get-emailpicker-fontsize"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "FontSize" property-type = "set" id = "set-emailpicker-fontsize"></div>

The font size for the text. By default, it is 14.0 points.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Height" property-type = "get" id = "get-emailpicker-height"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Height" property-type = "set" id = "set-emailpicker-height"></div>

Specifies the vertical height of the EmailPicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "HeightPercent" property-type = "set" id = "set-emailpicker-heightpercent"></div>

Specifies the vertical height of the EmailPicker as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Hint

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Hint" property-type = "get" id = "get-emailpicker-hint"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Hint" property-type = "set" id = "set-emailpicker-hint"></div>

Text that should appear faintly in the EmailPicker to provide a hint as to what the user should enter. This can only be seen if the Text property is empty.

| Param Name | IO Type |
| :--------: | :-----: |
|    Hint    |   text  |

### Text

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Text" property-type = "get" id = "get-emailpicker-text"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Text" property-type = "set" id = "set-emailpicker-text"></div>

Returns the textbox contents.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "TextColor" property-type = "get" id = "get-emailpicker-textcolor"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "TextColor" property-type = "set" id = "set-emailpicker-textcolor"></div>

The color for the text. You can choose a color by name in the Designer or in the Blocks Editor. The default text color is black.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Visible" property-type = "get" id = "get-emailpicker-visible"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Visible" property-type = "set" id = "set-emailpicker-visible"></div>

Specifies whether the EmailPicker should be visible on the screen. Value is true if the EmailPicker is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Width" property-type = "get" id = "get-emailpicker-width"></div>

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "Width" property-type = "set" id = "set-emailpicker-width"></div>

Specifies the horizontal width of the EmailPicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "EmailPicker" property-selector = "WidthPercent" property-type = "set" id = "set-emailpicker-widthpercent"></div>

Specifies the horizontal width of the EmailPicker as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### EmailPicker

<div block-type = "component_component_block" component-selector = "EmailPicker" id = "component-emailpicker"></div>

Return Type : component

Component EmailPicker

