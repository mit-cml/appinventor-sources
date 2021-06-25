# ImagePicker

A special-purpose button. When the user taps an image picker, the device's image gallery appears, and the user can choose an image. After an image is picked, it is saved, and the `Selected` property will be the name of the file where the image is stored. In order to not fill up storage, a maximum of 10 images will be stored. Picking more images will delete previous images, in order from oldest to newest.

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

### AfterPicking

<div block-type = "component_event" component-selector = "ImagePicker" event-selector = "AfterPicking" event-params = "" id = "imagepicker-afterpicking"></div>

Event to be raised after the \`ImagePicker\` activity returns its result and the properties have been filled in.

### BeforePicking

<div block-type = "component_event" component-selector = "ImagePicker" event-selector = "BeforePicking" event-params = "" id = "imagepicker-beforepicking"></div>

Event to raise when the \`ImagePicker\` is clicked or the picker is shown using the

### GotFocus

<div block-type = "component_event" component-selector = "ImagePicker" event-selector = "GotFocus" event-params = "" id = "imagepicker-gotfocus"></div>

Indicates the cursor moved over the ImagePicker so it is now possible to click it.

### LostFocus

<div block-type = "component_event" component-selector = "ImagePicker" event-selector = "LostFocus" event-params = "" id = "imagepicker-lostfocus"></div>

Indicates the cursor moved away from the ImagePicker so it is now no longer possible to click it.

### TouchDown

<div block-type = "component_event" component-selector = "ImagePicker" event-selector = "TouchDown" event-params = "" id = "imagepicker-touchdown"></div>

Indicates that the ImagePicker was pressed down.

### TouchUp

<div block-type = "component_event" component-selector = "ImagePicker" event-selector = "TouchUp" event-params = "" id = "imagepicker-touchup"></div>

Indicates that the ImagePicker has been released.

## Methods

---

### Open

<div block-type = "component_method" component-selector = "ImagePicker" method-selector = "Open" method-params = "" return-type = "undefined" id = "imagepicker-open"></div>

Return Type : No Return Value

Opens the ImagePicker, as though the user clicked on it.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "BackgroundColor" property-type = "get" id = "get-imagepicker-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "BackgroundColor" property-type = "set" id = "set-imagepicker-backgroundcolor"></div>

Returns the button's background color

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Enabled" property-type = "get" id = "get-imagepicker-enabled"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Enabled" property-type = "set" id = "set-imagepicker-enabled"></div>

If set, user can tap ImagePicker to cause action.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontBold

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "FontBold" property-type = "get" id = "get-imagepicker-fontbold"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "FontBold" property-type = "set" id = "set-imagepicker-fontbold"></div>

If set, ImagePicker text is displayed in bold.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontBold  | boolean |

### FontItalic

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "FontItalic" property-type = "get" id = "get-imagepicker-fontitalic"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "FontItalic" property-type = "set" id = "set-imagepicker-fontitalic"></div>

If set, ImagePicker text is displayed in italics.

| Param Name | IO Type |
| :--------: | :-----: |
| FontItalic | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "FontSize" property-type = "get" id = "get-imagepicker-fontsize"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "FontSize" property-type = "set" id = "set-imagepicker-fontsize"></div>

Point size for ImagePicker text.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Height" property-type = "get" id = "get-imagepicker-height"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Height" property-type = "set" id = "set-imagepicker-height"></div>

Specifies the vertical height of the ImagePicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "HeightPercent" property-type = "set" id = "set-imagepicker-heightpercent"></div>

Specifies the vertical height of the ImagePicker as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Image

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Image" property-type = "get" id = "get-imagepicker-image"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Image" property-type = "set" id = "set-imagepicker-image"></div>

Image to display on button.

| Param Name | IO Type |
| :--------: | :-----: |
|    Image   |   text  |

### Selection

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Selection" property-type = "get" id = "get-imagepicker-selection"></div>

Path to the file containing the image that was selected.

| Param Name | IO Type |
| :--------: | :-----: |
|  Selection |   text  |

### ShowFeedback

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "ShowFeedback" property-type = "get" id = "get-imagepicker-showfeedback"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "ShowFeedback" property-type = "set" id = "set-imagepicker-showfeedback"></div>

Specifies if a visual feedback should be shown for a ImagePicker that has an image as background.

|  Param Name  | IO Type |
| :----------: | :-----: |
| ShowFeedback | boolean |

### Text

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Text" property-type = "get" id = "get-imagepicker-text"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Text" property-type = "set" id = "set-imagepicker-text"></div>

Text to display on ImagePicker.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "TextColor" property-type = "get" id = "get-imagepicker-textcolor"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "TextColor" property-type = "set" id = "set-imagepicker-textcolor"></div>

Color for button text.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Visible" property-type = "get" id = "get-imagepicker-visible"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Visible" property-type = "set" id = "set-imagepicker-visible"></div>

Specifies whether the ImagePicker should be visible on the screen. Value is true if the ImagePicker is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Width" property-type = "get" id = "get-imagepicker-width"></div>

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "Width" property-type = "set" id = "set-imagepicker-width"></div>

Specifies the horizontal width of the ImagePicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "ImagePicker" property-selector = "WidthPercent" property-type = "set" id = "set-imagepicker-widthpercent"></div>

Specifies the horizontal width of the ImagePicker as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### ImagePicker

<div block-type = "component_component_block" component-selector = "ImagePicker" id = "component-imagepicker"></div>

Return Type : component

Component ImagePicker

