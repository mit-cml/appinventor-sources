# Button

Button with the ability to detect clicks. Many aspects of its appearance can be changed, as well as whether it is clickable (`Enabled`), can be changed in the Designer or in the Blocks Editor.

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

### Image

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Image     |    asset    |               |

### Shape

| Property Name |  Editor Type | Default Value |
| :-----------: | :----------: | :-----------: |
|     Shape     | button_shape |       0       |

### ShowFeedback

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  ShowFeedback |   boolean   |      True     |

### Text

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|      Text     |    string   |               |

### TextAlignment

| Property Name |  Editor Type  | Default Value |
| :-----------: | :-----------: | :-----------: |
| TextAlignment | textalignment |       1       |

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

### Click

<div block-type = "component_event" component-selector = "Button" event-selector = "Click" event-params = "" id = "button-click"></div>

User tapped and released the button.

### GotFocus

<div block-type = "component_event" component-selector = "Button" event-selector = "GotFocus" event-params = "" id = "button-gotfocus"></div>

Indicates the cursor moved over the Button so it is now possible to click it.

### LongClick

<div block-type = "component_event" component-selector = "Button" event-selector = "LongClick" event-params = "" id = "button-longclick"></div>

User held the button down.

### LostFocus

<div block-type = "component_event" component-selector = "Button" event-selector = "LostFocus" event-params = "" id = "button-lostfocus"></div>

Indicates the cursor moved away from the Button so it is now no longer possible to click it.

### TouchDown

<div block-type = "component_event" component-selector = "Button" event-selector = "TouchDown" event-params = "" id = "button-touchdown"></div>

Indicates that the Button was pressed down.

### TouchUp

<div block-type = "component_event" component-selector = "Button" event-selector = "TouchUp" event-params = "" id = "button-touchup"></div>

Indicates that the Button has been released.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "Button" property-selector = "BackgroundColor" property-type = "get" id = "get-button-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "BackgroundColor" property-type = "set" id = "set-button-backgroundcolor"></div>

Returns the button's background color

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Enabled" property-type = "get" id = "get-button-enabled"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Enabled" property-type = "set" id = "set-button-enabled"></div>

If set, user can tap Button to cause action.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontBold

<div block-type = "component_set_get" component-selector = "Button" property-selector = "FontBold" property-type = "get" id = "get-button-fontbold"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "FontBold" property-type = "set" id = "set-button-fontbold"></div>

If set, Button text is displayed in bold.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontBold  | boolean |

### FontItalic

<div block-type = "component_set_get" component-selector = "Button" property-selector = "FontItalic" property-type = "get" id = "get-button-fontitalic"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "FontItalic" property-type = "set" id = "set-button-fontitalic"></div>

If set, Button text is displayed in italics.

| Param Name | IO Type |
| :--------: | :-----: |
| FontItalic | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "Button" property-selector = "FontSize" property-type = "get" id = "get-button-fontsize"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "FontSize" property-type = "set" id = "set-button-fontsize"></div>

Point size for Button text.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Height" property-type = "get" id = "get-button-height"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Height" property-type = "set" id = "set-button-height"></div>

Specifies the vertical height of the Button, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "Button" property-selector = "HeightPercent" property-type = "set" id = "set-button-heightpercent"></div>

Specifies the vertical height of the Button as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Image

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Image" property-type = "get" id = "get-button-image"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Image" property-type = "set" id = "set-button-image"></div>

Image to display on button.

| Param Name | IO Type |
| :--------: | :-----: |
|    Image   |   text  |

### ShowFeedback

<div block-type = "component_set_get" component-selector = "Button" property-selector = "ShowFeedback" property-type = "get" id = "get-button-showfeedback"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "ShowFeedback" property-type = "set" id = "set-button-showfeedback"></div>

Specifies if a visual feedback should be shown for a Button that has an image as background.

|  Param Name  | IO Type |
| :----------: | :-----: |
| ShowFeedback | boolean |

### Text

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Text" property-type = "get" id = "get-button-text"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Text" property-type = "set" id = "set-button-text"></div>

Text to display on Button.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "Button" property-selector = "TextColor" property-type = "get" id = "get-button-textcolor"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "TextColor" property-type = "set" id = "set-button-textcolor"></div>

Color for button text.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Visible" property-type = "get" id = "get-button-visible"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Visible" property-type = "set" id = "set-button-visible"></div>

Specifies whether the Button should be visible on the screen. Value is true if the Button is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Width" property-type = "get" id = "get-button-width"></div>

<div block-type = "component_set_get" component-selector = "Button" property-selector = "Width" property-type = "set" id = "set-button-width"></div>

Specifies the horizontal width of the Button, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "Button" property-selector = "WidthPercent" property-type = "set" id = "set-button-widthpercent"></div>

Specifies the horizontal width of the Button as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### Button

<div block-type = "component_component_block" component-selector = "Button" id = "component-button"></div>

Return Type : component

Component Button

