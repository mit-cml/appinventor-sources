# PasswordTextBox

A box for entering passwords. This is the same as the ordinary `TextBox` component except this does not display the characters typed by the user.

The value of the text in the box can be found or set through the `Text` property. If blank, the `Hint` property, which appears as faint text in the box, can provide the user with guidance as to what to type.

Text boxes are usually used with the `Button` component, with the user clicking on the button when text entry is complete.

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

### NumbersOnly

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  NumbersOnly  |   boolean   |     False     |

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

<div block-type = "component_event" component-selector = "PasswordTextBox" event-selector = "GotFocus" event-params = "" id = "passwordtextbox-gotfocus"></div>

Event raised when the PasswordTextBox is selected for input, such as by the user touching it.

### LostFocus

<div block-type = "component_event" component-selector = "PasswordTextBox" event-selector = "LostFocus" event-params = "" id = "passwordtextbox-lostfocus"></div>

Event raised when the PasswordTextBox is no longer selected for input, such as if the user touches a different text box.

## Methods

---

### RequestFocus

<div block-type = "component_method" component-selector = "PasswordTextBox" method-selector = "RequestFocus" method-params = "" return-type = "undefined" id = "passwordtextbox-requestfocus"></div>

Return Type : No Return Value

Sets the PasswordTextBox active.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "BackgroundColor" property-type = "get" id = "get-passwordtextbox-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "BackgroundColor" property-type = "set" id = "set-passwordtextbox-backgroundcolor"></div>

The background color of the input box. You can choose a color by name in the Designer or in the Blocks Editor. The default background color is 'default' (shaded 3-D look).

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Enabled" property-type = "get" id = "get-passwordtextbox-enabled"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Enabled" property-type = "set" id = "set-passwordtextbox-enabled"></div>

Whether the user can enter text into the PasswordTextBox. By default, this is true.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "FontSize" property-type = "get" id = "get-passwordtextbox-fontsize"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "FontSize" property-type = "set" id = "set-passwordtextbox-fontsize"></div>

The font size for the text. By default, it is 14.0 points.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Height" property-type = "get" id = "get-passwordtextbox-height"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Height" property-type = "set" id = "set-passwordtextbox-height"></div>

Specifies the vertical height of the PasswordTextBox, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "HeightPercent" property-type = "set" id = "set-passwordtextbox-heightpercent"></div>

Specifies the vertical height of the PasswordTextBox as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Hint

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Hint" property-type = "get" id = "get-passwordtextbox-hint"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Hint" property-type = "set" id = "set-passwordtextbox-hint"></div>

Text that should appear faintly in the PasswordTextBox to provide a hint as to what the user should enter. This can only be seen if the Text property is empty.

| Param Name | IO Type |
| :--------: | :-----: |
|    Hint    |   text  |

### NumbersOnly

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "NumbersOnly" property-type = "get" id = "get-passwordtextbox-numbersonly"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "NumbersOnly" property-type = "set" id = "set-passwordtextbox-numbersonly"></div>

If true, then this password text box accepts only numbers as keyboard input. Numbers can include a decimal point and an optional leading minus sign. This applies to keyboard input only. Even if NumbersOnly is true, you can use \[set Text to\] to enter any text at all.

|  Param Name | IO Type |
| :---------: | :-----: |
| NumbersOnly | boolean |

### PasswordVisible

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "PasswordVisible" property-type = "get" id = "get-passwordtextbox-passwordvisible"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "PasswordVisible" property-type = "set" id = "set-passwordtextbox-passwordvisible"></div>

Visibility of password.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| PasswordVisible | boolean |

### Text

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Text" property-type = "get" id = "get-passwordtextbox-text"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Text" property-type = "set" id = "set-passwordtextbox-text"></div>

Returns the textbox contents.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "TextColor" property-type = "get" id = "get-passwordtextbox-textcolor"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "TextColor" property-type = "set" id = "set-passwordtextbox-textcolor"></div>

The color for the text. You can choose a color by name in the Designer or in the Blocks Editor. The default text color is black.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Visible" property-type = "get" id = "get-passwordtextbox-visible"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Visible" property-type = "set" id = "set-passwordtextbox-visible"></div>

Specifies whether the PasswordTextBox should be visible on the screen. Value is true if the PasswordTextBox is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Width" property-type = "get" id = "get-passwordtextbox-width"></div>

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "Width" property-type = "set" id = "set-passwordtextbox-width"></div>

Specifies the horizontal width of the PasswordTextBox, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "PasswordTextBox" property-selector = "WidthPercent" property-type = "set" id = "set-passwordtextbox-widthpercent"></div>

Specifies the horizontal width of the PasswordTextBox as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### PasswordTextBox

<div block-type = "component_component_block" component-selector = "PasswordTextBox" id = "component-passwordtextbox"></div>

Return Type : component

Component PasswordTextBox

