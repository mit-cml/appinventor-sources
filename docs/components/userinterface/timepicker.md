# TimePicker

A button that, when clicked on, launches a popup dialog to allow the user to select a time.

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

### AfterTimeSet

<div block-type = "component_event" component-selector = "TimePicker" event-selector = "AfterTimeSet" event-params = "" id = "timepicker-aftertimeset"></div>

This event is run when a user has set the time in the popup dialog.

### GotFocus

<div block-type = "component_event" component-selector = "TimePicker" event-selector = "GotFocus" event-params = "" id = "timepicker-gotfocus"></div>

Indicates the cursor moved over the TimePicker so it is now possible to click it.

### LostFocus

<div block-type = "component_event" component-selector = "TimePicker" event-selector = "LostFocus" event-params = "" id = "timepicker-lostfocus"></div>

Indicates the cursor moved away from the TimePicker so it is now no longer possible to click it.

### TouchDown

<div block-type = "component_event" component-selector = "TimePicker" event-selector = "TouchDown" event-params = "" id = "timepicker-touchdown"></div>

Indicates that the TimePicker was pressed down.

### TouchUp

<div block-type = "component_event" component-selector = "TimePicker" event-selector = "TouchUp" event-params = "" id = "timepicker-touchup"></div>

Indicates that the TimePicker has been released.

## Methods

---

### LaunchPicker

<div block-type = "component_method" component-selector = "TimePicker" method-selector = "LaunchPicker" method-params = "" return-type = "undefined" id = "timepicker-launchpicker"></div>

Return Type : No Return Value

Launches the TimePicker dialog.

### SetTimeToDisplay

<div block-type = "component_method" component-selector = "TimePicker" method-selector = "SetTimeToDisplay" method-params = "hour-minute" return-type = "undefined" id = "timepicker-settimetodisplay"></div>

Return Type : No Return Value

Set the time to be shown in the Time Picker popup. Current time is shown by default.

| Param Name | Input Type |
| :--------: | :--------: |
|    hour    |   number   |
|   minute   |   number   |

### SetTimeToDisplayFromInstant

<div block-type = "component_method" component-selector = "TimePicker" method-selector = "SetTimeToDisplayFromInstant" method-params = "instant" return-type = "undefined" id = "timepicker-settimetodisplayfrominstant"></div>

Return Type : No Return Value

Set the time from the instant to be shown in the Time Picker dialog. Current time is shown by default.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "BackgroundColor" property-type = "get" id = "get-timepicker-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "BackgroundColor" property-type = "set" id = "set-timepicker-backgroundcolor"></div>

Returns the button's background color

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Enabled" property-type = "get" id = "get-timepicker-enabled"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Enabled" property-type = "set" id = "set-timepicker-enabled"></div>

If set, user can tap TimePicker to cause action.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontBold

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "FontBold" property-type = "get" id = "get-timepicker-fontbold"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "FontBold" property-type = "set" id = "set-timepicker-fontbold"></div>

If set, TimePicker text is displayed in bold.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontBold  | boolean |

### FontItalic

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "FontItalic" property-type = "get" id = "get-timepicker-fontitalic"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "FontItalic" property-type = "set" id = "set-timepicker-fontitalic"></div>

If set, TimePicker text is displayed in italics.

| Param Name | IO Type |
| :--------: | :-----: |
| FontItalic | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "FontSize" property-type = "get" id = "get-timepicker-fontsize"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "FontSize" property-type = "set" id = "set-timepicker-fontsize"></div>

Point size for TimePicker text.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Height" property-type = "get" id = "get-timepicker-height"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Height" property-type = "set" id = "set-timepicker-height"></div>

Specifies the vertical height of the TimePicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "HeightPercent" property-type = "set" id = "set-timepicker-heightpercent"></div>

Specifies the vertical height of the TimePicker as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Hour

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Hour" property-type = "get" id = "get-timepicker-hour"></div>

The hour of the last time set using the time picker. The hour is in a 24 hour format. If the last time set was 11:53 pm, this property will return 23.

| Param Name | IO Type |
| :--------: | :-----: |
|    Hour    |  number |

### Image

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Image" property-type = "get" id = "get-timepicker-image"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Image" property-type = "set" id = "set-timepicker-image"></div>

Image to display on button.

| Param Name | IO Type |
| :--------: | :-----: |
|    Image   |   text  |

### Instant

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Instant" property-type = "get" id = "get-timepicker-instant"></div>

The instant of the last time set using the time picker

| Param Name |    IO Type    |
| :--------: | :-----------: |
|   Instant  | InstantInTime |

### Minute

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Minute" property-type = "get" id = "get-timepicker-minute"></div>

The minute of the last time set using the time picker

| Param Name | IO Type |
| :--------: | :-----: |
|   Minute   |  number |

### ShowFeedback

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "ShowFeedback" property-type = "get" id = "get-timepicker-showfeedback"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "ShowFeedback" property-type = "set" id = "set-timepicker-showfeedback"></div>

Specifies if a visual feedback should be shown for a TimePicker that has an image as background.

|  Param Name  | IO Type |
| :----------: | :-----: |
| ShowFeedback | boolean |

### Text

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Text" property-type = "get" id = "get-timepicker-text"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Text" property-type = "set" id = "set-timepicker-text"></div>

Text to display on TimePicker.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "TextColor" property-type = "get" id = "get-timepicker-textcolor"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "TextColor" property-type = "set" id = "set-timepicker-textcolor"></div>

Color for button text.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Visible" property-type = "get" id = "get-timepicker-visible"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Visible" property-type = "set" id = "set-timepicker-visible"></div>

Specifies whether the TimePicker should be visible on the screen. Value is true if the TimePicker is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Width" property-type = "get" id = "get-timepicker-width"></div>

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "Width" property-type = "set" id = "set-timepicker-width"></div>

Specifies the horizontal width of the TimePicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "TimePicker" property-selector = "WidthPercent" property-type = "set" id = "set-timepicker-widthpercent"></div>

Specifies the horizontal width of the TimePicker as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### TimePicker

<div block-type = "component_component_block" component-selector = "TimePicker" id = "component-timepicker"></div>

Return Type : component

Component TimePicker

