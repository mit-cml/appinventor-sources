# Texting

A component that will, when the `SendMessage` method is called, send the text message specified in the `Message` property to the phone number specified in the `PhoneNumber` property.

If the `ReceivingEnabled` property is set to 1 messages will **not** be received. If `ReceivingEnabled` is set to 2 messages will be received only when the application is running. Finally if `ReceivingEnabled` is set to 3, messages will be received when the application is running **and** when the application is not running they will be queued and a notification displayed to the user.

When a message arrives, the `MessageReceived` event is raised and provides the sending number and message.

An app that includes this component will receive messages even when it is in the background (i.e. when it's not visible on the screen) and, moreso, even if the app is not running, so long as it's installed on the phone. If the phone receives a text message when the app is not in the foreground, the phone will show a notification in the notification bar. Selecting the notification will bring up the app. As an app developer, you'll probably want to give your users the ability to control ReceivingEnabled so that they can make the phone ignore text messages.

If the GoogleVoiceEnabled property is true, messages can be sent over Wifi using Google Voice. This option requires that the user have a Google Voice account and that the mobile Voice app is installed on the phone. The Google Voice option works only on phones that support Android 2.0 (Eclair) or higher.

To specify the phone number (e.g., 650-555-1212), set the `PhoneNumber` property to a Text string with the specified digits (e.g., 6505551212). Dashes, dots, and parentheses may be included (e.g., (650)-555-1212) but will be ignored; spaces may not be included.

Another way for an app to specify a phone number would be to include a `PhoneNumberPicker` component, which lets the users select a phone numbers from the ones stored in the the phone's contacts.

---

## Designer Properties

---

### GoogleVoiceEnabled

|    Property Name   | Editor Type | Default Value |
| :----------------: | :---------: | :-----------: |
| GoogleVoiceEnabled |   boolean   |     False     |

### Message

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Message    |    string   |               |

### PhoneNumber

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  PhoneNumber  |    string   |               |

### ReceivingEnabled

|   Property Name  |   Editor Type  | Default Value |
| :--------------: | :------------: | :-----------: |
| ReceivingEnabled | text_receiving |       1       |

## Events

---

### MessageReceived

<div block-type = "component_event" component-selector = "Texting" event-selector = "MessageReceived" event-params = "number-messageText" id = "texting-messagereceived"></div>

Event that's raised when a text message is received by the phone. \*\*Using this block will add \[dangerous permissions\](//developer.android.com/guide/topics/permissions/overview#dangerous\_permissions) that will require additional approval if your app is submitted to the Google Play Store.\*\*

|  Param Name | IO Type |
| :---------: | :-----: |
|    number   |   text  |
| messageText |   text  |

## Methods

---

### SendMessage

<div block-type = "component_method" component-selector = "Texting" method-selector = "SendMessage" method-params = "" return-type = "undefined" id = "texting-sendmessage"></div>

Return Type : No Return Value

Launch the phone's default text messaging app with the message and phone number prepopulated.

### SendMessageDirect

<div block-type = "component_method" component-selector = "Texting" method-selector = "SendMessageDirect" method-params = "" return-type = "undefined" id = "texting-sendmessagedirect"></div>

Return Type : No Return Value

Send a text message. \*\*Using this block will add \[dangerous permissions\](https://developer.android.com/guide/topics/permissions/overview#dangerous\_permissions) that will require additional approval if your app is submitted to the Google Play Store.\*\*

## Block Properties

---

### GoogleVoiceEnabled

<div block-type = "component_set_get" component-selector = "Texting" property-selector = "GoogleVoiceEnabled" property-type = "get" id = "get-texting-googlevoiceenabled"></div>

<div block-type = "component_set_get" component-selector = "Texting" property-selector = "GoogleVoiceEnabled" property-type = "set" id = "set-texting-googlevoiceenabled"></div>

If true, then SendMessage will attempt to send messages over Wifi using Google Voice. This requires that the Google Voice app must be installed and set up on the phone or tablet, with a Google Voice account. If GoogleVoiceEnabled is false, the device must have phone and texting service in order to send or receive messages with this component.

|     Param Name     | IO Type |
| :----------------: | :-----: |
| GoogleVoiceEnabled | boolean |

### Message

<div block-type = "component_set_get" component-selector = "Texting" property-selector = "Message" property-type = "get" id = "get-texting-message"></div>

<div block-type = "component_set_get" component-selector = "Texting" property-selector = "Message" property-type = "set" id = "set-texting-message"></div>

The message that will be sent when the SendMessage method is called.

| Param Name | IO Type |
| :--------: | :-----: |
|   Message  |   text  |

### PhoneNumber

<div block-type = "component_set_get" component-selector = "Texting" property-selector = "PhoneNumber" property-type = "get" id = "get-texting-phonenumber"></div>

<div block-type = "component_set_get" component-selector = "Texting" property-selector = "PhoneNumber" property-type = "set" id = "set-texting-phonenumber"></div>

Sets the phone number to send the text message to when the SendMessage function is called.

|  Param Name | IO Type |
| :---------: | :-----: |
| PhoneNumber |   text  |

### ReceivingEnabled

<div block-type = "component_set_get" component-selector = "Texting" property-selector = "ReceivingEnabled" property-type = "get" id = "get-texting-receivingenabled"></div>

<div block-type = "component_set_get" component-selector = "Texting" property-selector = "ReceivingEnabled" property-type = "set" id = "set-texting-receivingenabled"></div>

If set to 1 (OFF) no messages will be received. If set to 2 (FOREGROUND) or3 (ALWAYS) the component will respond to messages if it is running. If the app is not running then the message will be discarded if set to 2 (FOREGROUND). If set to 3 (ALWAYS) and the app is not running the phone will show a notification. Selecting the notification will bring up the app and signal the MessageReceived event. Messages received when the app is dormant will be queued, and so several MessageReceived events might appear when the app awakens. As an app developer, it would be a good idea to give your users control over this property, so they can make their phones ignore text messages when your app is installed.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| ReceivingEnabled |  number |

## Component

---

### Texting

<div block-type = "component_component_block" component-selector = "Texting" id = "component-texting"></div>

Return Type : component

Component Texting

