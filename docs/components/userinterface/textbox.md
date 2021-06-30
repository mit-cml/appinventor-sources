<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# TextBox

A box for the user to enter text. The initial or user-entered text value is in the `Text` property. If blank, the `Hint` property, which appears as faint text in the box, can provide the user with guidance as to what to type.

The `MultiLine` property determines if the text can havemore than one line. For a single line text box, the keyboard will closeautomatically when the user presses the Done key. To close the keyboard for multiline text boxes, the app should use the HideKeyboard method or rely on the user to press the Back key.

The `NumbersOnly` property restricts the keyboard to acceptnumeric input only.

Other properties affect the appearance of the text box (`TextAlignment`, `BackgroundColor`, etc.) and whether it can be used (`Enabled`).

Text boxes are usually used with the `Button` component, with the user clicking on the button when text entry is complete.

If the text entered by the user should not be displayed, use `PasswordTextBox` instead.

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

### MultiLine

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   MultiLine   |   boolean   |     False     |

### NumbersOnly

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  NumbersOnly  |   boolean   |     False     |

### ReadOnly

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    ReadOnly   |   boolean   |     False     |

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

<div block-type = "component_event" component-selector = "TextBox" event-selector = "GotFocus" id = "textbox-gotfocus"></div>

Event raised when the TextBox is selected for input, such as by the user touching it.

### LostFocus

<div block-type = "component_event" component-selector = "TextBox" event-selector = "LostFocus" id = "textbox-lostfocus"></div>

Event raised when the TextBox is no longer selected for input, such as if the user touches a different text box.

## Methods

---

### HideKeyboard

<div block-type = "component_method" component-selector = "TextBox" method-selector = "HideKeyboard" id = "textbox-hidekeyboard"></div>

Return Type : No Return Value

Hide the keyboard. Only multiline text boxes need this. Single line text boxes close the keyboard when the users presses the Done key.

### RequestFocus

<div block-type = "component_method" component-selector = "TextBox" method-selector = "RequestFocus" id = "textbox-requestfocus"></div>

Return Type : No Return Value

Sets the TextBox active.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "BackgroundColor" property-type = "get" id = "get-textbox-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "BackgroundColor" property-type = "set" id = "set-textbox-backgroundcolor"></div>

The background color of the input box. You can choose a color by name in the Designer or in the Blocks Editor. The default background color is 'default' (shaded 3-D look).

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Enabled" property-type = "get" id = "get-textbox-enabled"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Enabled" property-type = "set" id = "set-textbox-enabled"></div>

Whether the user can enter text into the TextBox. By default, this is true.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "FontSize" property-type = "get" id = "get-textbox-fontsize"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "FontSize" property-type = "set" id = "set-textbox-fontsize"></div>

The font size for the text. By default, it is 14.0 points.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Height" property-type = "get" id = "get-textbox-height"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Height" property-type = "set" id = "set-textbox-height"></div>

Specifies the vertical height of the TextBox, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "HeightPercent" property-type = "set" id = "set-textbox-heightpercent"></div>

Specifies the vertical height of the TextBox as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Hint

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Hint" property-type = "get" id = "get-textbox-hint"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Hint" property-type = "set" id = "set-textbox-hint"></div>

Text that should appear faintly in the TextBox to provide a hint as to what the user should enter. This can only be seen if the Text property is empty.

| Param Name | IO Type |
| :--------: | :-----: |
|    Hint    |   text  |

### MultiLine

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "MultiLine" property-type = "get" id = "get-textbox-multiline"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "MultiLine" property-type = "set" id = "set-textbox-multiline"></div>

If true, then this text box accepts multiple lines of input, which are entered using the return key. For single line text boxes there is a Done key instead of a return key, and pressing Done hides the keyboard. The app should call the HideKeyboard method to hide the keyboard for a mutiline text box.

| Param Name | IO Type |
| :--------: | :-----: |
|  MultiLine | boolean |

### NumbersOnly

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "NumbersOnly" property-type = "get" id = "get-textbox-numbersonly"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "NumbersOnly" property-type = "set" id = "set-textbox-numbersonly"></div>

If true, then this text box accepts only numbers as keyboard input. Numbers can include a decimal point and an optional leading minus sign. This applies to keyboard input only. Even if NumbersOnly is true, you can use \[set Text to\] to enter any text at all.

|  Param Name | IO Type |
| :---------: | :-----: |
| NumbersOnly | boolean |

### ReadOnly

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "ReadOnly" property-type = "get" id = "get-textbox-readonly"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "ReadOnly" property-type = "set" id = "set-textbox-readonly"></div>

Whether the TextBox is read-only. By default, this is true.

| Param Name | IO Type |
| :--------: | :-----: |
|  ReadOnly  | boolean |

### Text

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Text" property-type = "get" id = "get-textbox-text"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Text" property-type = "set" id = "set-textbox-text"></div>

Returns the textbox contents.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "TextColor" property-type = "get" id = "get-textbox-textcolor"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "TextColor" property-type = "set" id = "set-textbox-textcolor"></div>

The color for the text. You can choose a color by name in the Designer or in the Blocks Editor. The default text color is black.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Visible" property-type = "get" id = "get-textbox-visible"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Visible" property-type = "set" id = "set-textbox-visible"></div>

Specifies whether the TextBox should be visible on the screen. Value is true if the TextBox is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Width" property-type = "get" id = "get-textbox-width"></div>

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "Width" property-type = "set" id = "set-textbox-width"></div>

Specifies the horizontal width of the TextBox, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "TextBox" property-selector = "WidthPercent" property-type = "set" id = "set-textbox-widthpercent"></div>

Specifies the horizontal width of the TextBox as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### TextBox

<div block-type = "component_component_block" component-selector = "TextBox" id = "component-textbox"></div>

Return Type : component

Component TextBox

