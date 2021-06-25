# Screen

Top-level component containing all other components in the program

---

## Designer Properties

---

### AboutScreen

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  AboutScreen  |   textArea  |               |

### AccentColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  AccentColor  |    color    |   &HFFFF4081  |

### ActionBar

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ActionBar   |   boolean   |     False     |

### AlignHorizontal

|  Property Name  |      Editor Type     | Default Value |
| :-------------: | :------------------: | :-----------: |
| AlignHorizontal | horizontal_alignment |       1       |

### AlignVertical

| Property Name |     Editor Type    | Default Value |
| :-----------: | :----------------: | :-----------: |
| AlignVertical | vertical_alignment |       1       |

### AppName

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    AppName    |    string   |               |

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &HFFFFFFFF  |

### BackgroundImage

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundImage |    asset    |               |

### BigDefaultText

|  Property Name | Editor Type | Default Value |
| :------------: | :---------: | :-----------: |
| BigDefaultText |   boolean   |     False     |

### BlocksToolkit

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| BlocksToolkit | subset_json |               |

### CloseScreenAnimation

|     Property Name    |    Editor Type   | Default Value |
| :------------------: | :--------------: | :-----------: |
| CloseScreenAnimation | screen_animation |    default    |

### HighContrast

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  HighContrast |   boolean   |     False     |

### Icon

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|      Icon     |    asset    |               |

### OpenScreenAnimation

|    Property Name    |    Editor Type   | Default Value |
| :-----------------: | :--------------: | :-----------: |
| OpenScreenAnimation | screen_animation |    default    |

### PrimaryColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  PrimaryColor |    color    |   &HFF3F51B5  |

### PrimaryColorDark

|   Property Name  | Editor Type | Default Value |
| :--------------: | :---------: | :-----------: |
| PrimaryColorDark |    color    |   &HFF303F9F  |

### ScreenOrientation

|   Property Name   |     Editor Type    | Default Value |
| :---------------: | :----------------: | :-----------: |
| ScreenOrientation | screen_orientation |  unspecified  |

### Scrollable

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   Scrollable  |   boolean   |     False     |

### ShowListsAsJson

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| ShowListsAsJson |   boolean   |      True     |

### ShowStatusBar

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| ShowStatusBar |   boolean   |      True     |

### Sizing

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Sizing    |    sizing   |   Responsive  |

### Theme

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Theme     |    theme    |    Classic    |

### Title

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Title     |    string   |               |

### TitleVisible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  TitleVisible |   boolean   |      True     |

### TutorialURL

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  TutorialURL  |    string   |               |

### VersionCode

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|  VersionCode  | non_negative_integer |       1       |

### VersionName

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  VersionName  |    string   |      1.0      |

## Events

---

### BackPressed

<div block-type = "component_event" component-selector = "Screen" event-selector = "BackPressed" event-params = "" id = "screen-backpressed"></div>

Device back button pressed.

### ErrorOccurred

<div block-type = "component_event" component-selector = "Screen" event-selector = "ErrorOccurred" event-params = "component-functionName-errorNumber-message" id = "screen-erroroccurred"></div>

Event raised when an error occurs. Only some errors will raise this condition. For those errors, the system will show a notification by default. You can use this event handler to prescribe an error behavior different than the default.

|  Param Name  |  IO Type  |
| :----------: | :-------: |
|   component  | component |
| functionName |    text   |
|  errorNumber |   number  |
|    message   |    text   |

### Initialize

<div block-type = "component_event" component-selector = "Screen" event-selector = "Initialize" event-params = "" id = "screen-initialize"></div>

The Initialize event is run when the Screen starts and is only run once per screen.

### OtherScreenClosed

<div block-type = "component_event" component-selector = "Screen" event-selector = "OtherScreenClosed" event-params = "otherScreenName-result" id = "screen-otherscreenclosed"></div>

Event raised when another screen has closed and control has returned to this screen.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| otherScreenName |   text  |
|      result     |   any   |

### PermissionDenied

<div block-type = "component_event" component-selector = "Screen" event-selector = "PermissionDenied" event-params = "component-functionName-permissionName" id = "screen-permissiondenied"></div>

Event to handle when the app user has denied a needed permission.

|   Param Name   |  IO Type  |
| :------------: | :-------: |
|    component   | component |
|  functionName  |    text   |
| permissionName |    text   |

### PermissionGranted

<div block-type = "component_event" component-selector = "Screen" event-selector = "PermissionGranted" event-params = "permissionName" id = "screen-permissiongranted"></div>

Event to handle when the app user has granted a needed permission. This event is only run when permission is granted in response to the AskForPermission method.

|   Param Name   | IO Type |
| :------------: | :-----: |
| permissionName |   text  |

### ScreenOrientationChanged

<div block-type = "component_event" component-selector = "Screen" event-selector = "ScreenOrientationChanged" event-params = "" id = "screen-screenorientationchanged"></div>

Screen orientation changed

## Methods

---

### AskForPermission

<div block-type = "component_method" component-selector = "Screen" method-selector = "AskForPermission" method-params = "permissionName" return-type = "undefined" id = "screen-askforpermission"></div>

Return Type : No Return Value

Ask the user to grant access to a dangerous permission.

|   Param Name   | Input Type |
| :------------: | :--------: |
| permissionName |    text    |

### HideKeyboard

<div block-type = "component_method" component-selector = "Screen" method-selector = "HideKeyboard" method-params = "" return-type = "undefined" id = "screen-hidekeyboard"></div>

Return Type : No Return Value

Hide the onscreen soft keyboard.

## Block Properties

---

### AboutScreen

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "AboutScreen" property-type = "get" id = "get-screen-aboutscreen"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "AboutScreen" property-type = "set" id = "set-screen-aboutscreen"></div>

Information about the screen. It appears when "About this Application" is selected from the system menu. Use it to inform people about your app. In multiple screen apps, each screen has its own AboutScreen info.

|  Param Name | IO Type |
| :---------: | :-----: |
| AboutScreen |   text  |

### AlignHorizontal

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "AlignHorizontal" property-type = "get" id = "get-screen-alignhorizontal"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "AlignHorizontal" property-type = "set" id = "set-screen-alignhorizontal"></div>

A number that encodes how contents of the screen are aligned horizontally. The choices are: 1 = left aligned, 3 = horizontally centered, 2 = right aligned.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| AlignHorizontal |  number |

### AlignVertical

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "AlignVertical" property-type = "get" id = "get-screen-alignvertical"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "AlignVertical" property-type = "set" id = "set-screen-alignvertical"></div>

A number that encodes how the contents of the arrangement are aligned vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, 3 = aligned at the bottom. Vertical alignment has no effect if the screen is scrollable.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| AlignVertical |  number |

### BackgroundColor

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "BackgroundColor" property-type = "get" id = "get-screen-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "BackgroundColor" property-type = "set" id = "set-screen-backgroundcolor"></div>

BackgroundColor property getter method.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### BackgroundImage

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "BackgroundImage" property-type = "get" id = "get-screen-backgroundimage"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "BackgroundImage" property-type = "set" id = "set-screen-backgroundimage"></div>

The screen background image.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundImage |   text  |

### BigDefaultText

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "BigDefaultText" property-type = "get" id = "get-screen-bigdefaulttext"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "BigDefaultText" property-type = "set" id = "set-screen-bigdefaulttext"></div>

When checked, we will use high contrast mode

|   Param Name   | IO Type |
| :------------: | :-----: |
| BigDefaultText | boolean |

### CloseScreenAnimation

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "CloseScreenAnimation" property-type = "get" id = "get-screen-closescreenanimation"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "CloseScreenAnimation" property-type = "set" id = "set-screen-closescreenanimation"></div>

The animation for closing current screen and returning to the previous screen. Valid options are default, fade, zoom, slidehorizontal, slidevertical, and none

|      Param Name      | IO Type |
| :------------------: | :-----: |
| CloseScreenAnimation |   text  |

### Height

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "Height" property-type = "get" id = "get-screen-height"></div>

Screen height (y-size).

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HighContrast

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "HighContrast" property-type = "get" id = "get-screen-highcontrast"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "HighContrast" property-type = "set" id = "set-screen-highcontrast"></div>

When checked, we will use high contrast mode

|  Param Name  | IO Type |
| :----------: | :-----: |
| HighContrast | boolean |

### OpenScreenAnimation

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "OpenScreenAnimation" property-type = "get" id = "get-screen-openscreenanimation"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "OpenScreenAnimation" property-type = "set" id = "set-screen-openscreenanimation"></div>

The animation for switching to another screen. Valid options are default, fade, zoom, slidehorizontal, slidevertical, and none

|      Param Name     | IO Type |
| :-----------------: | :-----: |
| OpenScreenAnimation |   text  |

### Platform

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "Platform" property-type = "get" id = "get-screen-platform"></div>

The platform the app is running on, for example "Android" or "iOS".

| Param Name | IO Type |
| :--------: | :-----: |
|  Platform  |   text  |

### PlatformVersion

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "PlatformVersion" property-type = "get" id = "get-screen-platformversion"></div>

The dotted version number of the platform, such as 4.2.2 or 10.0. This is platform specific and there is no guarantee that it has a particular format.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| PlatformVersion |   text  |

### ScreenOrientation

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "ScreenOrientation" property-type = "get" id = "get-screen-screenorientation"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "ScreenOrientation" property-type = "set" id = "set-screen-screenorientation"></div>

The requested screen orientation, specified as a text value. Commonly used values are landscape, portrait, sensor, user and unspecified. See the Android developer documentation for ActivityInfo.Screen\_Orientation for the complete list of possible settings.

|     Param Name    | IO Type |
| :---------------: | :-----: |
| ScreenOrientation |   text  |

### Scrollable

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "Scrollable" property-type = "get" id = "get-screen-scrollable"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "Scrollable" property-type = "set" id = "set-screen-scrollable"></div>

When checked, there will be a vertical scrollbar on the screen, and the height of the application can exceed the physical height of the device. When unchecked, the application height is constrained to the height of the device.

| Param Name | IO Type |
| :--------: | :-----: |
| Scrollable | boolean |

### ShowStatusBar

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "ShowStatusBar" property-type = "get" id = "get-screen-showstatusbar"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "ShowStatusBar" property-type = "set" id = "set-screen-showstatusbar"></div>

The status bar is the topmost bar on the screen. This property reports whether the status bar is visible.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| ShowStatusBar | boolean |

### Title

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "Title" property-type = "get" id = "get-screen-title"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "Title" property-type = "set" id = "set-screen-title"></div>

The caption for the form, which apears in the title bar

| Param Name | IO Type |
| :--------: | :-----: |
|    Title   |   text  |

### TitleVisible

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "TitleVisible" property-type = "get" id = "get-screen-titlevisible"></div>

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "TitleVisible" property-type = "set" id = "set-screen-titlevisible"></div>

The title bar is the top gray bar on the screen. This property reports whether the title bar is visible.

|  Param Name  | IO Type |
| :----------: | :-----: |
| TitleVisible | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Screen" property-selector = "Width" property-type = "get" id = "get-screen-width"></div>

Screen width (x-size).

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

## Component

---

### Screen

<div block-type = "component_component_block" component-selector = "Screen" id = "component-screen"></div>

Return Type : component

Component Screen

