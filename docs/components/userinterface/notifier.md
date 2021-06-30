<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Notifier

The Notifier component displays alert dialogs, messages, and temporary alerts, and creates Android log entries through the following methods:

*   ShowMessageDialog: displays a message which the user must dismiss by pressing a button.
*   ShowChooseDialog: displays a message two buttons to let the user choose one of two responses, for example, yes or no, after which the AfterChoosing event is raised.
*   ShowTextDialog: lets the user enter text in response to the message, after which the AfterTextInput event is raised.
*   ShowPasswordDialog: lets the user enter password in response to the message, after which the AfterTextInput event is raised.
*   ShowAlert: displays a temporary alert that goes away by itself after a short time.
*   ShowProgressDialog: displays an alert with a loading spinner that cannot be dismissed by the user. It can only be dismissed by using the DismissProgressDialog block.
*   DismissProgressDialog: Dismisses the progress dialog displayed by ShowProgressDialog.
*   LogError: logs an error message to the Android log.
*   LogInfo: logs an info message to the Android log.
*   LogWarning: logs a warning message to the Android log.
*   The messages in the dialogs (but not the alert) can be formatted using the following HTML tags:<b>, <big>, <blockquote>, <br>, <cite>, <dfn>, <div>, <em>, <small>, <strong>, <sub>, <sup>, <tt>. <u>
*   You can also use the font tag to specify color, for example, <font color="blue">. Some of the available color names are aqua, black, blue, fuchsia, green, grey, lime, maroon, navy, olive, purple, red, silver, teal, white, and yellow

---

## Designer Properties

---

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &HFF444444  |

### NotifierLength

|  Property Name |  Editor Type | Default Value |
| :------------: | :----------: | :-----------: |
| NotifierLength | toast_length |       1       |

### TextColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   TextColor   |    color    |   &HFFFFFFFF  |

## Events

---

### AfterChoosing

<div block-type = "component_event" component-selector = "Notifier" event-selector = "AfterChoosing" id = "notifier-afterchoosing"></div>

Event after the user has made a selection for ShowChooseDialog.

| Param Name | IO Type |
| :--------: | :-----: |
|   choice   |   text  |

### AfterTextInput

<div block-type = "component_event" component-selector = "Notifier" event-selector = "AfterTextInput" id = "notifier-aftertextinput"></div>

Event raised after the user has responded to ShowTextDialog.

| Param Name | IO Type |
| :--------: | :-----: |
|  response  |   text  |

### ChoosingCanceled

<div block-type = "component_event" component-selector = "Notifier" event-selector = "ChoosingCanceled" id = "notifier-choosingcanceled"></div>

Event raised when the user canceled ShowChooseDialog.

### TextInputCanceled

<div block-type = "component_event" component-selector = "Notifier" event-selector = "TextInputCanceled" id = "notifier-textinputcanceled"></div>

Event raised when the user canceled ShowTextDialog.

## Methods

---

### DismissProgressDialog

<div block-type = "component_method" component-selector = "Notifier" method-selector = "DismissProgressDialog" id = "notifier-dismissprogressdialog"></div>

Return Type : No Return Value

Dismiss a previously displayed ProgressDialog box

### LogError

<div block-type = "component_method" component-selector = "Notifier" method-selector = "LogError" id = "notifier-logerror"></div>

Return Type : No Return Value

Writes an error message to the Android system log. See the Google Android documentation for how to access the log.

| Param Name | Input Type |
| :--------: | :--------: |
|   message  |    text    |

### LogInfo

<div block-type = "component_method" component-selector = "Notifier" method-selector = "LogInfo" id = "notifier-loginfo"></div>

Return Type : No Return Value

Writes an information message to the Android log.

| Param Name | Input Type |
| :--------: | :--------: |
|   message  |    text    |

### LogWarning

<div block-type = "component_method" component-selector = "Notifier" method-selector = "LogWarning" id = "notifier-logwarning"></div>

Return Type : No Return Value

Writes a warning message to the Android log. See the Google Android documentation for how to access the log.

| Param Name | Input Type |
| :--------: | :--------: |
|   message  |    text    |

### ShowAlert

<div block-type = "component_method" component-selector = "Notifier" method-selector = "ShowAlert" id = "notifier-showalert"></div>

Return Type : No Return Value

Display a temporary notification.

| Param Name | Input Type |
| :--------: | :--------: |
|   notice   |    text    |

### ShowChooseDialog

<div block-type = "component_method" component-selector = "Notifier" method-selector = "ShowChooseDialog" id = "notifier-showchoosedialog"></div>

Return Type : No Return Value

Shows a dialog box with two buttons, from which the user can choose. If cancelable is true there will be an additional CANCEL button. Pressing a button will raise the AfterChoosing event. The "choice" parameter to AfterChoosing will be the text on the button that was pressed, or "Cancel" if the CANCEL button was pressed.

|  Param Name | Input Type |
| :---------: | :--------: |
|   message   |    text    |
|    title    |    text    |
| button1Text |    text    |
| button2Text |    text    |
|  cancelable |   boolean  |

### ShowMessageDialog

<div block-type = "component_method" component-selector = "Notifier" method-selector = "ShowMessageDialog" id = "notifier-showmessagedialog"></div>

Return Type : No Return Value

Display an alert dialog with a single button that dismisses the alert.

| Param Name | Input Type |
| :--------: | :--------: |
|   message  |    text    |
|    title   |    text    |
| buttonText |    text    |

### ShowPasswordDialog

<div block-type = "component_method" component-selector = "Notifier" method-selector = "ShowPasswordDialog" id = "notifier-showpassworddialog"></div>

Return Type : No Return Value

Shows a dialog box where the user can enter password (input is masked), after which the AfterTextInput event will be raised. If cancelable is true there will be an additional CANCEL button. Entering password will raise the AfterTextInput event. The "response" parameter to AfterTextInput will be the entered password, or "Cancel" if CANCEL button was pressed.

| Param Name | Input Type |
| :--------: | :--------: |
|   message  |    text    |
|    title   |    text    |
| cancelable |   boolean  |

### ShowProgressDialog

<div block-type = "component_method" component-selector = "Notifier" method-selector = "ShowProgressDialog" id = "notifier-showprogressdialog"></div>

Return Type : No Return Value

Shows a dialog box with an optional title and message (use empty strings if they are not wanted). This dialog box contains a spinning artifact to indicate that the program is working. It cannot be canceled by the user but must be dismissed by the App Inventor Program by using the DismissProgressDialog block.

| Param Name | Input Type |
| :--------: | :--------: |
|   message  |    text    |
|    title   |    text    |

### ShowTextDialog

<div block-type = "component_method" component-selector = "Notifier" method-selector = "ShowTextDialog" id = "notifier-showtextdialog"></div>

Return Type : No Return Value

Shows a dialog box where the user can enter text, after which the AfterTextInput event will be raised. If cancelable is true there will be an additional CANCEL button. Entering text will raise the AfterTextInput event. The "response" parameter to AfterTextInput will be the text that was entered, or "Cancel" if the CANCEL button was pressed.

| Param Name | Input Type |
| :--------: | :--------: |
|   message  |    text    |
|    title   |    text    |
| cancelable |   boolean  |

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "Notifier" property-selector = "BackgroundColor" property-type = "set" id = "set-notifier-backgroundcolor"></div>

Specifies the background color for alerts (not dialogs).

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### TextColor

<div block-type = "component_set_get" component-selector = "Notifier" property-selector = "TextColor" property-type = "get" id = "get-notifier-textcolor"></div>

<div block-type = "component_set_get" component-selector = "Notifier" property-selector = "TextColor" property-type = "set" id = "set-notifier-textcolor"></div>

Specifies the text color for alerts (not dialogs).

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

## Component

---

### Notifier

<div block-type = "component_component_block" component-selector = "Notifier" id = "component-notifier"></div>

Return Type : component

Component Notifier

