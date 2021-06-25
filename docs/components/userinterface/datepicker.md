# DatePicker

A button that, when clicked on, launches a popup dialog to allow the user to select a date.

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

### AfterDateSet

<div block-type = "component_event" component-selector = "DatePicker" event-selector = "AfterDateSet" event-params = "" id = "datepicker-afterdateset"></div>

Event that runs after the user chooses a Date in the dialog

### GotFocus

<div block-type = "component_event" component-selector = "DatePicker" event-selector = "GotFocus" event-params = "" id = "datepicker-gotfocus"></div>

Indicates the cursor moved over the DatePicker so it is now possible to click it.

### LostFocus

<div block-type = "component_event" component-selector = "DatePicker" event-selector = "LostFocus" event-params = "" id = "datepicker-lostfocus"></div>

Indicates the cursor moved away from the DatePicker so it is now no longer possible to click it.

### TouchDown

<div block-type = "component_event" component-selector = "DatePicker" event-selector = "TouchDown" event-params = "" id = "datepicker-touchdown"></div>

Indicates that the DatePicker was pressed down.

### TouchUp

<div block-type = "component_event" component-selector = "DatePicker" event-selector = "TouchUp" event-params = "" id = "datepicker-touchup"></div>

Indicates that the DatePicker has been released.

## Methods

---

### LaunchPicker

<div block-type = "component_method" component-selector = "DatePicker" method-selector = "LaunchPicker" method-params = "" return-type = "undefined" id = "datepicker-launchpicker"></div>

Return Type : No Return Value

Launches the DatePicker dialog.

### SetDateToDisplay

<div block-type = "component_method" component-selector = "DatePicker" method-selector = "SetDateToDisplay" method-params = "year-month-day" return-type = "undefined" id = "datepicker-setdatetodisplay"></div>

Return Type : No Return Value

Allows the user to set the date to be displayed when the date picker opens. Valid values for the month field are 1-12 and 1-31 for the day field.

| Param Name | Input Type |
| :--------: | :--------: |
|    year    |   number   |
|    month   |   number   |
|     day    |   number   |

### SetDateToDisplayFromInstant

<div block-type = "component_method" component-selector = "DatePicker" method-selector = "SetDateToDisplayFromInstant" method-params = "instant" return-type = "undefined" id = "datepicker-setdatetodisplayfrominstant"></div>

Return Type : No Return Value

Allows the user to set the date from the instant to be displayed when the date picker opens.

| Param Name |   Input Type  |
| :--------: | :-----------: |
|   instant  | InstantInTime |

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "BackgroundColor" property-type = "get" id = "get-datepicker-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "BackgroundColor" property-type = "set" id = "set-datepicker-backgroundcolor"></div>

Returns the button's background color

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Day

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Day" property-type = "get" id = "get-datepicker-day"></div>

the Day of the month that was last picked using the DatePicker.

| Param Name | IO Type |
| :--------: | :-----: |
|     Day    |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Enabled" property-type = "get" id = "get-datepicker-enabled"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Enabled" property-type = "set" id = "set-datepicker-enabled"></div>

If set, user can tap DatePicker to cause action.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontBold

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "FontBold" property-type = "get" id = "get-datepicker-fontbold"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "FontBold" property-type = "set" id = "set-datepicker-fontbold"></div>

If set, DatePicker text is displayed in bold.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontBold  | boolean |

### FontItalic

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "FontItalic" property-type = "get" id = "get-datepicker-fontitalic"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "FontItalic" property-type = "set" id = "set-datepicker-fontitalic"></div>

If set, DatePicker text is displayed in italics.

| Param Name | IO Type |
| :--------: | :-----: |
| FontItalic | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "FontSize" property-type = "get" id = "get-datepicker-fontsize"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "FontSize" property-type = "set" id = "set-datepicker-fontsize"></div>

Point size for DatePicker text.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Height" property-type = "get" id = "get-datepicker-height"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Height" property-type = "set" id = "set-datepicker-height"></div>

Specifies the vertical height of the DatePicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "HeightPercent" property-type = "set" id = "set-datepicker-heightpercent"></div>

Specifies the vertical height of the DatePicker as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Image

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Image" property-type = "get" id = "get-datepicker-image"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Image" property-type = "set" id = "set-datepicker-image"></div>

Image to display on button.

| Param Name | IO Type |
| :--------: | :-----: |
|    Image   |   text  |

### Instant

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Instant" property-type = "get" id = "get-datepicker-instant"></div>

the instant of the date that was last picked using the DatePicker.

| Param Name |    IO Type    |
| :--------: | :-----------: |
|   Instant  | InstantInTime |

### Month

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Month" property-type = "get" id = "get-datepicker-month"></div>

the number of the Month that was last picked using the DatePicker. Note that months start in 1 = January, 12 = December.

| Param Name | IO Type |
| :--------: | :-----: |
|    Month   |  number |

### MonthInText

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "MonthInText" property-type = "get" id = "get-datepicker-monthintext"></div>

Returns the name of the Month that was last picked using the DatePicker, in textual format.

|  Param Name | IO Type |
| :---------: | :-----: |
| MonthInText |   text  |

### ShowFeedback

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "ShowFeedback" property-type = "get" id = "get-datepicker-showfeedback"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "ShowFeedback" property-type = "set" id = "set-datepicker-showfeedback"></div>

Specifies if a visual feedback should be shown for a DatePicker that has an image as background.

|  Param Name  | IO Type |
| :----------: | :-----: |
| ShowFeedback | boolean |

### Text

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Text" property-type = "get" id = "get-datepicker-text"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Text" property-type = "set" id = "set-datepicker-text"></div>

Text to display on DatePicker.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "TextColor" property-type = "get" id = "get-datepicker-textcolor"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "TextColor" property-type = "set" id = "set-datepicker-textcolor"></div>

Color for button text.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Visible" property-type = "get" id = "get-datepicker-visible"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Visible" property-type = "set" id = "set-datepicker-visible"></div>

Specifies whether the DatePicker should be visible on the screen. Value is true if the DatePicker is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Width" property-type = "get" id = "get-datepicker-width"></div>

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Width" property-type = "set" id = "set-datepicker-width"></div>

Specifies the horizontal width of the DatePicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "WidthPercent" property-type = "set" id = "set-datepicker-widthpercent"></div>

Specifies the horizontal width of the DatePicker as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

### Year

<div block-type = "component_set_get" component-selector = "DatePicker" property-selector = "Year" property-type = "get" id = "get-datepicker-year"></div>

the Year that was last picked using the DatePicker

| Param Name | IO Type |
| :--------: | :-----: |
|    Year    |  number |

## Component

---

### DatePicker

<div block-type = "component_component_block" component-selector = "DatePicker" id = "component-datepicker"></div>

Return Type : component

Component DatePicker

