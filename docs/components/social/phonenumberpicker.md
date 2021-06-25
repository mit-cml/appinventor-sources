# PhoneNumberPicker

A button that, when clicked on, displays a list of the contacts' phone numbers to choose among. After the user has made a selection, the following properties will be set to information about the chosen contact:

*   `ContactName`: the contact's name
*   `PhoneNumber`: the contact's phone number
*   `EmailAddress`: the contact's email address
*   `Picture`: the name of the file containing the contact's image, which can be used as a `Picture` property value for the `Image` or `ImageSprite` component.

Other properties affect the appearance of the button (`TextAlignment`, `BackgroundColor`, etc.) and whether it can be clicked on (`Enabled`).

The PhoneNumberPicker component may not work on all Android devices. For example, on Android systems before system 3.0, the returned lists of phone numbers and email addresses will be empty.

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

<div block-type = "component_event" component-selector = "PhoneNumberPicker" event-selector = "AfterPicking" event-params = "" id = "phonenumberpicker-afterpicking"></div>

Event to be raised after the \`ContactPicker\` activity returns its result and the properties have been filled in.

### BeforePicking

<div block-type = "component_event" component-selector = "PhoneNumberPicker" event-selector = "BeforePicking" event-params = "" id = "phonenumberpicker-beforepicking"></div>

Event to raise when the \`ContactPicker\` is clicked or the picker is shown using the

### GotFocus

<div block-type = "component_event" component-selector = "PhoneNumberPicker" event-selector = "GotFocus" event-params = "" id = "phonenumberpicker-gotfocus"></div>

Indicates the cursor moved over the ContactPicker so it is now possible to click it.

### LostFocus

<div block-type = "component_event" component-selector = "PhoneNumberPicker" event-selector = "LostFocus" event-params = "" id = "phonenumberpicker-lostfocus"></div>

Indicates the cursor moved away from the ContactPicker so it is now no longer possible to click it.

### TouchDown

<div block-type = "component_event" component-selector = "PhoneNumberPicker" event-selector = "TouchDown" event-params = "" id = "phonenumberpicker-touchdown"></div>

Indicates that the ContactPicker was pressed down.

### TouchUp

<div block-type = "component_event" component-selector = "PhoneNumberPicker" event-selector = "TouchUp" event-params = "" id = "phonenumberpicker-touchup"></div>

Indicates that the ContactPicker has been released.

## Methods

---

### Open

<div block-type = "component_method" component-selector = "PhoneNumberPicker" method-selector = "Open" method-params = "" return-type = "undefined" id = "phonenumberpicker-open"></div>

Return Type : No Return Value

Opens the ContactPicker, as though the user clicked on it.

### ViewContact

<div block-type = "component_method" component-selector = "PhoneNumberPicker" method-selector = "ViewContact" method-params = "uri" return-type = "undefined" id = "phonenumberpicker-viewcontact"></div>

Return Type : No Return Value

view a contact via its URI

| Param Name | Input Type |
| :--------: | :--------: |
|     uri    |    text    |

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "BackgroundColor" property-type = "get" id = "get-phonenumberpicker-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "BackgroundColor" property-type = "set" id = "set-phonenumberpicker-backgroundcolor"></div>

Returns the button's background color

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### ContactName

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "ContactName" property-type = "get" id = "get-phonenumberpicker-contactname"></div>

Returns the full name of the selected contact, or the empty string if a name is unavailable.

|  Param Name | IO Type |
| :---------: | :-----: |
| ContactName |   text  |

### ContactUri

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "ContactUri" property-type = "get" id = "get-phonenumberpicker-contacturi"></div>

URI that specifies the location of the contact on the device.

| Param Name | IO Type |
| :--------: | :-----: |
| ContactUri |   text  |

### EmailAddress

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "EmailAddress" property-type = "get" id = "get-phonenumberpicker-emailaddress"></div>

Returns the primary email address of the selected contact, or the empty string if an email address is unavailable.

|  Param Name  | IO Type |
| :----------: | :-----: |
| EmailAddress |   text  |

### EmailAddressList

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "EmailAddressList" property-type = "get" id = "get-phonenumberpicker-emailaddresslist"></div>

Returns a list of email addresses associated with the selected contact.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| EmailAddressList |   list  |

### Enabled

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Enabled" property-type = "get" id = "get-phonenumberpicker-enabled"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Enabled" property-type = "set" id = "set-phonenumberpicker-enabled"></div>

If set, user can tap ContactPicker to cause action.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontBold

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "FontBold" property-type = "get" id = "get-phonenumberpicker-fontbold"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "FontBold" property-type = "set" id = "set-phonenumberpicker-fontbold"></div>

If set, ContactPicker text is displayed in bold.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontBold  | boolean |

### FontItalic

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "FontItalic" property-type = "get" id = "get-phonenumberpicker-fontitalic"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "FontItalic" property-type = "set" id = "set-phonenumberpicker-fontitalic"></div>

If set, ContactPicker text is displayed in italics.

| Param Name | IO Type |
| :--------: | :-----: |
| FontItalic | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "FontSize" property-type = "get" id = "get-phonenumberpicker-fontsize"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "FontSize" property-type = "set" id = "set-phonenumberpicker-fontsize"></div>

Point size for ContactPicker text.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Height" property-type = "get" id = "get-phonenumberpicker-height"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Height" property-type = "set" id = "set-phonenumberpicker-height"></div>

Specifies the vertical height of the ContactPicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "HeightPercent" property-type = "set" id = "set-phonenumberpicker-heightpercent"></div>

Specifies the vertical height of the ContactPicker as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Image

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Image" property-type = "get" id = "get-phonenumberpicker-image"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Image" property-type = "set" id = "set-phonenumberpicker-image"></div>

Image to display on button.

| Param Name | IO Type |
| :--------: | :-----: |
|    Image   |   text  |

### PhoneNumber

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "PhoneNumber" property-type = "get" id = "get-phonenumberpicker-phonenumber"></div>

Returns the primary phone number associated with the selected contact, or the empty string if no phone number is associated with the contact.

|  Param Name | IO Type |
| :---------: | :-----: |
| PhoneNumber |   text  |

### PhoneNumberList

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "PhoneNumberList" property-type = "get" id = "get-phonenumberpicker-phonenumberlist"></div>

Returns a list of phone numbers associated with the selected contact.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| PhoneNumberList |   list  |

### Picture

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Picture" property-type = "get" id = "get-phonenumberpicker-picture"></div>

Returns a picture URI for the selected contact, which can be used to retrieve the contact's photo and other fields.

| Param Name | IO Type |
| :--------: | :-----: |
|   Picture  |   text  |

### ShowFeedback

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "ShowFeedback" property-type = "get" id = "get-phonenumberpicker-showfeedback"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "ShowFeedback" property-type = "set" id = "set-phonenumberpicker-showfeedback"></div>

Specifies if a visual feedback should be shown for a ContactPicker that has an image as background.

|  Param Name  | IO Type |
| :----------: | :-----: |
| ShowFeedback | boolean |

### Text

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Text" property-type = "get" id = "get-phonenumberpicker-text"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Text" property-type = "set" id = "set-phonenumberpicker-text"></div>

Text to display on ContactPicker.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "TextColor" property-type = "get" id = "get-phonenumberpicker-textcolor"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "TextColor" property-type = "set" id = "set-phonenumberpicker-textcolor"></div>

Color for button text.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Visible

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Visible" property-type = "get" id = "get-phonenumberpicker-visible"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Visible" property-type = "set" id = "set-phonenumberpicker-visible"></div>

Specifies whether the ContactPicker should be visible on the screen. Value is true if the ContactPicker is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Width" property-type = "get" id = "get-phonenumberpicker-width"></div>

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "Width" property-type = "set" id = "set-phonenumberpicker-width"></div>

Specifies the horizontal width of the ContactPicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "PhoneNumberPicker" property-selector = "WidthPercent" property-type = "set" id = "set-phonenumberpicker-widthpercent"></div>

Specifies the horizontal width of the ContactPicker as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### PhoneNumberPicker

<div block-type = "component_component_block" component-selector = "PhoneNumberPicker" id = "component-phonenumberpicker"></div>

Return Type : component

Component PhoneNumberPicker

