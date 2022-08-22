---
layout: documentation
title: Social
---

[&laquo; Back to index](index.html)
# Social

Table of Contents:

* [ContactPicker](#ContactPicker)
* [EmailPicker](#EmailPicker)
* [PhoneCall](#PhoneCall)
* [PhoneNumberPicker](#PhoneNumberPicker)
* [Sharing](#Sharing)
* [Texting](#Texting)
* [Twitter](#Twitter)

## ContactPicker  {#ContactPicker}

A button that, when clicked on, displays a list of the contacts to choose among. After the user
 has made a selection, the following properties will be set to information about the chosen
 contact:

  - [`ContactName`](#ContactPicker.ContactName): the contact's name
  - [`EmailAddress`](#ContactPicker.EmailAddress): the contact's primary email address
  - [`EmailAddressList`](#ContactPicker.EmailAddressList): a list of the contact's email addresses
  - [`ContactUri`](#ContactPicker.ContactUri): the contact's URI on the device
  - [`PhoneNumber`](#ContactPicker.PhoneNumber): the contact's primary phone number (on Later Android Verisons)
  - [`PhoneNumberList`](#ContactPicker.PhoneNumberList): a list of the contact's phone numbers (on Later Android Versions)
  - [`Picture`](#ContactPicker.Picture): the name of the file containing the contact's image, which can be used as a Picture property value for the Image or ImageSprite component.

 Other properties affect the appearance of the button ([`TextAlignment`](#ContactPicker.TextAlignment),
 [`BackgroundColor`](#ContactPicker.BackgroundColor), etc.) and whether it can be clicked on ([`Enabled`](#ContactPicker.Enabled)).

 The `ContactPicker` component might not work on all phones. For example, on Android systems
 before system 3.0, it cannot pick phone numbers, and the list of email addresses will contain
 only one email.



### Properties  {#ContactPicker-Properties}

{:.properties}

{:id="ContactPicker.BackgroundColor" .color} *BackgroundColor*
: Specifies the `ContactPicker`'s background color as an alpha-red-green-blue
 integer.  If an [`Image`](#ContactPicker.Image) has been set, the color
 change will not be visible until the [`Image`](#ContactPicker.Image) is removed.

{:id="ContactPicker.BackgroundRepeatMode" .number .do} *BackgroundRepeatMode*
: Specifies the repeat mode for the button background image. This does not check that the argument is a legal    
 value.

{:id="ContactPicker.ContactName" .text .ro .bo} *ContactName*
: Returns the full name of the selected contact, or the empty string if a name is unavailable.

{:id="ContactPicker.ContactUri" .text .ro .bo} *ContactUri*
: Returns a URI that specifies the location of the contact on the device.

{:id="ContactPicker.EmailAddress" .text .ro .bo} *EmailAddress*
: Returns the primary email address of the selected contact, or the empty string if an email
 address is unavailable.

{:id="ContactPicker.EmailAddressList" .list .ro .bo} *EmailAddressList*
: Returns a list of email addresses associated with the selected contact.

{:id="ContactPicker.Enabled" .boolean} *Enabled*
: Specifies whether the `ContactPicker` should be active and clickable.

{:id="ContactPicker.FontBold" .boolean} *FontBold*
: Specifies whether the text of the `ContactPicker` should be bold.
 Some fonts do not support bold.

{:id="ContactPicker.FontItalic" .boolean} *FontItalic*
: Specifies whether the text of the `ContactPicker` should be italic.
 Some fonts do not support italic.

{:id="ContactPicker.FontSize" .number} *FontSize*
: Specifies the text font size of the `ContactPicker`, measured in sp(scale-independent pixels).

{:id="ContactPicker.FontTypeface" .number .do} *FontTypeface*
: Specifies the text font face of the `ContactPicker` as default, serif, sans
 serif, or monospace.

{:id="ContactPicker.Height" .number .bo} *Height*
: Specifies the `ContactPicker`'s vertical height, measured in pixels.

{:id="ContactPicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `ContactPicker`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="ContactPicker.Image" .text} *Image*
: Specifies the path of the `ContactPicker`'s image. If there is both an `Image` and a
 [`BackgroundColor`](#ContactPicker.BackgroundColor) specified, only the `Image` will be visible.

{:id="ContactPicker.PhoneNumber" .text .ro .bo} *PhoneNumber*
: Returns the primary phone number associated with the selected contact, or the empty string if
 no phone number is associated with the contact.

{:id="ContactPicker.PhoneNumberList" .list .ro .bo} *PhoneNumberList*
: Returns a list of phone numbers associated with the selected contact.

{:id="ContactPicker.Picture" .text .ro .bo} *Picture*
: Returns a picture URI for the selected contact, which can be
 used to retrieve the contact's photo and other fields.

{:id="ContactPicker.Shape" .number .do} *Shape*
: Specifies the shape of the `ContactPicker`. The valid values for this property are `0` (default),
 `1` (rounded), `2` (rectangle), and `3` (oval). The `Shape` will not be visible if an
 [`Image`](#ContactPicker.Image) is used.

{:id="ContactPicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown when a `ContactPicker` with an assigned
 [`Image`](#ContactPicker.Image) is pressed.

{:id="ContactPicker.Text" .text} *Text*
: Specifies the text displayed by the `ContactPicker`.

{:id="ContactPicker.TextAlignment" .number .do} *TextAlignment*
: Specifies the alignment of the `ContactPicker`'s text. Valid values are:
 `0` (normal; e.g., left-justified if text is written left to right),
 `1` (center), or
 `2` (opposite; e.g., right-justified if text is written left to right).

{:id="ContactPicker.TextColor" .color} *TextColor*
: Specifies the text color of the `ContactPicker` as an alpha-red-green-blue
 integer.

{:id="ContactPicker.Visible" .boolean} *Visible*
: Specifies whether the `ContactPicker` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `ContactPicker` is showing and `false`{:.logic.block} if hidden.

{:id="ContactPicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the `ContactPicker`, measured in pixels.

{:id="ContactPicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `ContactPicker` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#ContactPicker-Events}

{:.events}

{:id="ContactPicker.AfterPicking"} AfterPicking()
: Event to be raised after the `ContactPicker` activity returns its
 result and the properties have been filled in.

{:id="ContactPicker.BeforePicking"} BeforePicking()
: Event to raise when the `ContactPicker` is clicked or the picker is shown
 using the [`Open`](#ContactPicker.Open) method.  This event occurs before the picker is displayed, and
 can be used to prepare the picker before it is shown.

{:id="ContactPicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the `ContactPicker` so it is now possible
 to click it.

{:id="ContactPicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the `ContactPicker` so it is now no
 longer possible to click it.

{:id="ContactPicker.TouchDown"} TouchDown()
: Indicates that the `ContactPicker` was pressed down.

{:id="ContactPicker.TouchUp"} TouchUp()
: Indicates that the `ContactPicker` has been released.

### Methods  {#ContactPicker-Methods}

{:.methods}

{:id="ContactPicker.Open" class="method"} <i/> Open()
: Opens the `ContactPicker`, as though the user clicked on it.

{:id="ContactPicker.ViewContact" class="method"} <i/> ViewContact(*uri*{:.text})
: Opens the selected contact's entry in the device's default Contacts app.

## EmailPicker  {#EmailPicker}

An `EmailPicker` is a kind of text box. If the user begins entering the name or email address of
 a contact, the phone will show a dropdown menu of choices that complete the entry. If there are
 many contacts, the dropdown can take several seconds to appear, and can show intermediate
 results while the matches are being computed.

 The initial contents of the text box and the contents< after user entry is in the [`Text`](#EmailPicker.Text)
 property. If the [`Text`](#EmailPicker.Text) property is initially empty, the contents of the [`Hint`](#EmailPicker.Hint)
 property will be faintly shown in the text box as a hint to the user.

 Other properties affect the appearance of the text box ([`TextAlignment`](#EmailPicker.TextAlignment),
 [`BackgroundColor`](#EmailPicker.BackgroundColor), etc.) and whether it can be used ([`Enabled`](#EmailPicker.Enabled)).

 Text boxes like this are usually used with [`Button`](userinterface.html#Button) components, with
 the user clicking on the button when text entry is complete.



### Properties  {#EmailPicker-Properties}

{:.properties}

{:id="EmailPicker.BackgroundColor" .color} *BackgroundColor*
: The background color of the `EmailPicker``. You can choose a color by name in the Designer or in
 the Blocks Editor. The default background color is 'default' (shaded 3-D look).

{:id="EmailPicker.Enabled" .boolean} *Enabled*
: If set, user can enter text into the `EmailPicker`.

{:id="EmailPicker.FontBold" .boolean .do} *FontBold*
: Specifies whether the text of the `EmailPicker` should be bold.
 Some fonts do not support bold.

{:id="EmailPicker.FontItalic" .boolean .do} *FontItalic*
: Specifies whether the text of the `EmailPicker` should be italic.
 Some fonts do not support italic.

{:id="EmailPicker.FontSize" .number} *FontSize*
: Specifies the text font size of the `EmailPicker`, measured in sp(scale-independent pixels).

{:id="EmailPicker.FontTypeface" .number .do} *FontTypeface*
: The text font face of the `EmailPicker`. Valid values are `0` (default), `1` (serif), `2` (sans
 serif), or `3` (monospace).

{:id="EmailPicker.Height" .number .bo} *Height*
: Specifies the `EmailPicker`'s vertical height, measured in pixels.

{:id="EmailPicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `EmailPicker`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="EmailPicker.Hint" .text} *Hint*
: `EmailPicker` hint for the user.

{:id="EmailPicker.Text" .text} *Text*
: The text in the `EmailPicker`, which can be set by the programmer in the Designer or Blocks Editor,
 or it can be entered by the user (unless the [`Enabled`](#EmailPicker.Enabled) property is false).

{:id="EmailPicker.TextAlignment" .number .do} *TextAlignment*
: Specifies the alignment of the `EmailPicker`'s text. Valid values are:
 `0` (normal; e.g., left-justified if text is written left to right),
 `1` (center), or
 `2` (opposite; e.g., right-justified if text is written left to right).

{:id="EmailPicker.TextColor" .color} *TextColor*
: Specifies the text color of the `EmailPicker` as an alpha-red-green-blue
 integer.

{:id="EmailPicker.Visible" .boolean} *Visible*
: Specifies whether the `EmailPicker` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `EmailPicker` is showing and `false`{:.logic.block} if hidden.

{:id="EmailPicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the `EmailPicker`, measured in pixels.

{:id="EmailPicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `EmailPicker` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#EmailPicker-Events}

{:.events}

{:id="EmailPicker.GotFocus"} GotFocus()
: Event raised when the `EmailPicker` is selected for input, such as by
 the user touching it.

{:id="EmailPicker.LostFocus"} LostFocus()
: Event raised when the `EmailPicker` is no longer selected for input, such
 as if the user touches a different text box.

### Methods  {#EmailPicker-Methods}

{:.methods}

{:id="EmailPicker.RequestFocus" class="method"} <i/> RequestFocus()
: Request focus to current `EmailPicker`.

## PhoneCall  {#PhoneCall}

![PhoneCall component icon](images/phonecall.png)

 A non-visible component that makes a phone call to the number specified in the
 [`PhoneNumber`](#PhoneCall.PhoneNumber) property, which can be set either in the Designer or Blocks Editor.
 The component has a [`MakePhoneCall`](#PhoneCall.MakePhoneCall) method, enabling the program to launch a phone call.
 You may also use [`MakePhoneCallDirect`](#PhoneCall.MakePhoneCallDirect) to directly initiate a phone call without user
 interaction. However, apps using this block may require further review by Google if submitted
 to the Play Store so it is advised to use [`MakePhoneCall`](#PhoneCall.MakePhoneCall) instead.

 Often, this component is used with the [`ContactPicker`](#ContactPicker) component, which lets the user
 select a contact from the ones stored on the phone and sets the
 [`PhoneNumber`](#PhoneCall.PhoneNumber) property to [`ContactPicker`'s `PhoneNumber`](#ContactPicker.PhoneNumber) property.

 To directly specify the phone number (e.g., 650-555-1212), set the [`PhoneNumber`](#PhoneCall.PhoneNumber)
 property to a Text with the specified digits (e.g., "6505551212"). Dashes, dots, and parentheses
 may be included (e.g., "(650)-555-1212") but will be ignored; spaces may not be included.



### Properties  {#PhoneCall-Properties}

{:.properties}

{:id="PhoneCall.PhoneNumber" .text} *PhoneNumber*
: Specifies the phone number to call.

### Events  {#PhoneCall-Events}

{:.events}

{:id="PhoneCall.IncomingCallAnswered"} IncomingCallAnswered(*phoneNumber*{:.text})
: Event indicating that an incoming phone call is answered. `phoneNumber`{:.variable.block} is
 the incoming call phone number.

{:id="PhoneCall.PhoneCallEnded"} PhoneCallEnded(*status*{:.number},*phoneNumber*{:.text})
: Event indicating that a phone call has ended. The `status`{:.variable.block} can be any of:

   - `1`: Incoming call was missed or rejected
   - `2`: Incoming call was answered and hung up
   - `3`: Outgoing call was hung up.

{:id="PhoneCall.PhoneCallStarted"} PhoneCallStarted(*status*{:.number},*phoneNumber*{:.text})
: Event indicating that a phone call has started. The `status`{:.variable.block} can be any of:

   - `1`: Incoming call is ringing
   - `2`: Outgoing call is dialled

### Methods  {#PhoneCall-Methods}

{:.methods}

{:id="PhoneCall.MakePhoneCall" class="method"} <i/> MakePhoneCall()
: Launches the default dialer app set to start a phone call using the number in the
 [`PhoneNumber`](#PhoneCall.PhoneNumber) property.

{:id="PhoneCall.MakePhoneCallDirect" class="method"} <i/> MakePhoneCallDirect()
: Directly initiates a phone call using the number in the [`PhoneNumber`](#PhoneCall.PhoneNumber) property,
 bypassing user interaction to start the call. **Most apps should use
 [`MakePhoneCall`](#PhoneCall.MakePhoneCall) instead, which requires no permissions.**

## PhoneNumberPicker  {#PhoneNumberPicker}

A button that, when clicked on, displays a list of the contacts' phone numbers to choose among.
 After the user has made a selection, the following properties will be set to information about
 the chosen contact:

  - [`ContactName`](#PhoneNumberPicker.ContactName): the contact's name
  - [`PhoneNumber`](#PhoneNumberPicker.PhoneNumber): the contact's phone number
  - [`EmailAddress`](#PhoneNumberPicker.EmailAddress): the contact's email address
  - [`Picture`](#PhoneNumberPicker.Picture): the name of the file containing the contact's image, which can be used as a Picture property value for the Image or ImageSprite component.

 Other properties affect the appearance of the button ([`TextAlignment`](#PhoneNumberPicker.TextAlignment),
 [`BackgroundColor`](#PhoneNumberPicker.BackgroundColor), etc.) and whether it can be clicked on ([`Enabled`](#PhoneNumberPicker.Enabled)).

 The `PhoneNumberPicker` component may not work on all Android devices. For example, on Android
 systems before system 3.0, the returned lists of phone numbers and email addresses will be empty.



### Properties  {#PhoneNumberPicker-Properties}

{:.properties}

{:id="PhoneNumberPicker.BackgroundColor" .color} *BackgroundColor*
: Specifies the `PhoneNumberPicker`'s background color as an alpha-red-green-blue
 integer.  If an [`Image`](#PhoneNumberPicker.Image) has been set, the color
 change will not be visible until the [`Image`](#PhoneNumberPicker.Image) is removed.

{:id="PhoneNumberPicker.BackgroundRepeatMode" .number .do} *BackgroundRepeatMode*
: Specifies the repeat mode for the button background image. This does not check that the argument is a legal    
 value.

{:id="PhoneNumberPicker.ContactName" .text .ro .bo} *ContactName*
: Returns the full name of the selected contact, or the empty string if a name is unavailable.

{:id="PhoneNumberPicker.ContactUri" .text .ro .bo} *ContactUri*
: Returns a URI that specifies the location of the contact on the device.

{:id="PhoneNumberPicker.EmailAddress" .text .ro .bo} *EmailAddress*
: Returns the primary email address of the selected contact, or the empty string if an email
 address is unavailable.

{:id="PhoneNumberPicker.EmailAddressList" .list .ro .bo} *EmailAddressList*
: Returns a list of email addresses associated with the selected contact.

{:id="PhoneNumberPicker.Enabled" .boolean} *Enabled*
: Specifies whether the `PhoneNumberPicker` should be active and clickable.

{:id="PhoneNumberPicker.FontBold" .boolean} *FontBold*
: Specifies whether the text of the `PhoneNumberPicker` should be bold.
 Some fonts do not support bold.

{:id="PhoneNumberPicker.FontItalic" .boolean} *FontItalic*
: Specifies whether the text of the `PhoneNumberPicker` should be italic.
 Some fonts do not support italic.

{:id="PhoneNumberPicker.FontSize" .number} *FontSize*
: Specifies the text font size of the `PhoneNumberPicker`, measured in sp(scale-independent pixels).

{:id="PhoneNumberPicker.FontTypeface" .number .do} *FontTypeface*
: Specifies the text font face of the `PhoneNumberPicker` as default, serif, sans
 serif, or monospace.

{:id="PhoneNumberPicker.Height" .number .bo} *Height*
: Specifies the `PhoneNumberPicker`'s vertical height, measured in pixels.

{:id="PhoneNumberPicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `PhoneNumberPicker`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="PhoneNumberPicker.Image" .text} *Image*
: Specifies the path of the `PhoneNumberPicker`'s image. If there is both an `Image` and a
 [`BackgroundColor`](#PhoneNumberPicker.BackgroundColor) specified, only the `Image` will be visible.

{:id="PhoneNumberPicker.PhoneNumber" .text .ro .bo} *PhoneNumber*
: Returns the primary phone number associated with the selected contact, or the empty string if
 no phone number is associated with the contact.

{:id="PhoneNumberPicker.PhoneNumberList" .list .ro .bo} *PhoneNumberList*
: Returns a list of phone numbers associated with the selected contact.

{:id="PhoneNumberPicker.Picture" .text .ro .bo} *Picture*
: Returns a picture URI for the selected contact, which can be
 used to retrieve the contact's photo and other fields.

{:id="PhoneNumberPicker.Shape" .number .do} *Shape*
: Specifies the shape of the `PhoneNumberPicker`. The valid values for this property are `0` (default),
 `1` (rounded), `2` (rectangle), and `3` (oval). The `Shape` will not be visible if an
 [`Image`](#PhoneNumberPicker.Image) is used.

{:id="PhoneNumberPicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown when a `PhoneNumberPicker` with an assigned
 [`Image`](#PhoneNumberPicker.Image) is pressed.

{:id="PhoneNumberPicker.Text" .text} *Text*
: Specifies the text displayed by the `PhoneNumberPicker`.

{:id="PhoneNumberPicker.TextAlignment" .number .do} *TextAlignment*
: Specifies the alignment of the `PhoneNumberPicker`'s text. Valid values are:
 `0` (normal; e.g., left-justified if text is written left to right),
 `1` (center), or
 `2` (opposite; e.g., right-justified if text is written left to right).

{:id="PhoneNumberPicker.TextColor" .color} *TextColor*
: Specifies the text color of the `PhoneNumberPicker` as an alpha-red-green-blue
 integer.

{:id="PhoneNumberPicker.Visible" .boolean} *Visible*
: Specifies whether the `PhoneNumberPicker` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `PhoneNumberPicker` is showing and `false`{:.logic.block} if hidden.

{:id="PhoneNumberPicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the `PhoneNumberPicker`, measured in pixels.

{:id="PhoneNumberPicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `PhoneNumberPicker` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#PhoneNumberPicker-Events}

{:.events}

{:id="PhoneNumberPicker.AfterPicking"} AfterPicking()
: Event to be raised after the `PhoneNumberPicker` activity returns its
 result and the properties have been filled in.

{:id="PhoneNumberPicker.BeforePicking"} BeforePicking()
: Event to raise when the `PhoneNumberPicker` is clicked or the picker is shown
 using the [`Open`](#PhoneNumberPicker.Open) method.  This event occurs before the picker is displayed, and
 can be used to prepare the picker before it is shown.

{:id="PhoneNumberPicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the `PhoneNumberPicker` so it is now possible
 to click it.

{:id="PhoneNumberPicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the `PhoneNumberPicker` so it is now no
 longer possible to click it.

{:id="PhoneNumberPicker.TouchDown"} TouchDown()
: Indicates that the `PhoneNumberPicker` was pressed down.

{:id="PhoneNumberPicker.TouchUp"} TouchUp()
: Indicates that the `PhoneNumberPicker` has been released.

### Methods  {#PhoneNumberPicker-Methods}

{:.methods}

{:id="PhoneNumberPicker.Open" class="method"} <i/> Open()
: Opens the `PhoneNumberPicker`, as though the user clicked on it.

{:id="PhoneNumberPicker.ViewContact" class="method"} <i/> ViewContact(*uri*{:.text})
: Opens the selected contact's entry in the device's default Contacts app.

## Sharing  {#Sharing}

Sharing is a non-visible component that enables sharing files and/or messages between your app
 and other apps installed on a device. The component will display a list of the installed apps
 that can handle the information provided, and will allow the user to choose one to share the
 content with, for instance a mail app, a social network app, a texting app, and so on.

 The file path can be taken directly from other components such as the
 [`Camera`](media.html#Camera) or the [`ImagePicker`](media.html#ImagePicker), but can also be
 specified directly to read from storage. Be aware that different devices treat storage
 differently, so a few things to try if, for instance, you have a file called `arrow.gif` in the
 folder `Appinventor/assets`, would be:

 - `"file:///sdcard/Appinventor/assets/arrow.gif"`; or
 - `"/storage/Appinventor/assets/arrow.gif"`



### Properties  {#Sharing-Properties}

{:.properties}
None


### Events  {#Sharing-Events}

{:.events}
None


### Methods  {#Sharing-Methods}

{:.methods}

{:id="Sharing.ShareFile" class="method"} <i/> ShareFile(*file*{:.text})
: Shares a file through any capable application installed on the phone by displaying a list of the available apps and allowing the user to choose one from the list. The selected app will open with the file inserted on it.

{:id="Sharing.ShareFileWithMessage" class="method"} <i/> ShareFileWithMessage(*file*{:.text},*message*{:.text})
: Shares both a file and a message through any capable application installed on the phone by displaying a list of available apps and allowing the user to  choose one from the list. The selected app will open with the file and message inserted on it.

{:id="Sharing.ShareMessage" class="method"} <i/> ShareMessage(*message*{:.text})
: Shares a message through any capable application installed on the phone by displaying a list of the available apps and allowing the user to choose one from the list. The selected app will open with the message inserted on it.

## Texting  {#Texting}

![Texting component icon](images/texting.png)

 A component that will, when the [`SendMessage`](#Texting.SendMessage) method is called, launch the device's
 preferred texting app to send the text message specified in the [`SendMessage`](#Texting.SendMessage) property
 to the phone number specified in the [`PhoneNumber`](#Texting.PhoneNumber) property. You may also send text
 messages without user interaction by calling [`SendMessageDirect`](#Texting.SendMessageDirect) instead, but this
 adds dangerous permissions to your final app.

 If the [`ReceivingEnabled`](#Texting.ReceivingEnabled) property is set to 1 messages will not be received. If
 [`ReceivingEnabled`](#Texting.ReceivingEnabled) is set to 2 messages will be received only when the application is
 running. Finally if [`ReceivingEnabled`](#Texting.ReceivingEnabled) is set to 3, messages will be received when the
 application is running and when the application is not running they will be queued and a
 notification displayed to the user.

 When a message arrives, the [`MessageReceived`](#Texting.MessageReceived) event is raised and
 provides the sending number and message.

 An app that includes this component will receive messages even when it is in the background
 (i.e. when it's not visible on the screen) and, moreso, even if the app is not running, so long
 as it's installed on the phone. If the phone receives a text message when the app is not in the
 foreground, the phone will show a notification in the notification bar. Selecting the
 notification will bring up the app. As an app developer, you'll probably want to give your users
 the ability to control ReceivingEnabled so that they can make the phone ignore text messages.

 If the [`GoogleVoiceEnabled`](#Texting.GoogleVoiceEnabled) property is true, messages can be sent over Wifi using
 Google Voice. This option requires that the user have a Google Voice account and that the mobile
 Voice app is installed on the phone. The Google Voice option works only on phones that support
 Android 2.0 (Eclair) or higher. **Unfortunately, receiving no longer works in Google Voice due to
 changes introduced in Google Voice App.**

 To specify the phone number (e.g., 650-555-1212), set the PhoneNumber property to a Text string
 with the specified digits (e.g., 6505551212). Dashes, dots, and parentheses may be included
 (e.g., (650)-555-1212) but will be ignored; spaces may not be included.

 Another way for an app to specify a phone number would be to include a [`PhoneNumberPicker`](#PhoneNumberPicker)
 component, which lets the users select a phone numbers from the ones stored in the the phone's
 contacts.



### Properties  {#Texting-Properties}

{:.properties}

{:id="Texting.GoogleVoiceEnabled" .boolean} *GoogleVoiceEnabled*
: If this property is true, then SendMessage will attempt to send messages over
 WiFi, using Google voice.

{:id="Texting.Message" .text} *Message*
: The message that will be sent when the [`SendMessage`](#Texting.SendMessage) method is called.
 The maximum length of a standard SMS message is usually 170. It may be less for languages
 using diacritical marks.

{:id="Texting.PhoneNumber" .text} *PhoneNumber*
: The number that the message will be sent to when the SendMessage method is called.  The 
 number is a text string with the specified digits (e.g., 6505551212).  Dashes, dots, 
 and parentheses may be included (e.g., (650)-555-1212) but will be ignored; spaces
 should not be included.

{:id="Texting.ReceivingEnabled" .number} *ReceivingEnabled*
: If set to 1 (OFF) no messages will be received. If set to 2 (FOREGROUND) or 3 (ALWAYS) the
 component will respond to messages if it is running. In the case of 2 (FOREGROUND), messages
 received while the app is not running are discarded. In the case of 3 (ALWAYS), messages
 receive while the app is not running will show a notification. Selecting the notification
 will bring up the app and signal the [`MessageReceived`](#Texting.MessageReceived) event. Messages
 received when the app is dormant will be queued, and so several
 [`MessageReceived`](#Texting.MessageReceived) events might appear when the app awakens. As an app
 developer, it would be a good idea to give your users control over this property, so they can
 make their phones ignore text messages when your app is installed.

### Events  {#Texting-Events}

{:.events}

{:id="Texting.MessageReceived"} MessageReceived(*number*{:.text},*messageText*{:.text})
: Event that's raised when a text message is received by the phone. **Using this block will add
 [dangerous permissions](//developer.android.com/guide/topics/permissions/overview#dangerous_permissions)
 that will require additional approval if your app is submitted to the Google Play Store.**

### Methods  {#Texting-Methods}

{:.methods}

{:id="Texting.SendMessage" class="method"} <i/> SendMessage()
: Launch the phone's default text messaging app with the message and phone number prepopulated.

{:id="Texting.SendMessageDirect" class="method"} <i/> SendMessageDirect()
: Send a text message. **Using this block will add
 [dangerous permissions](https://developer.android.com/guide/topics/permissions/overview#dangerous_permissions)
 that will require additional approval if your app is submitted to the Google Play Store.**

## Twitter  {#Twitter}

A non-visible component that enables communication with [Twitter](https://twitter.com). Once a
 user has logged into their Twitter account (and the authorization has been confirmed successful
 by the [`IsAuthorized`](#Twitter.IsAuthorized) event), many more operations are available:

 - Searching Twitter for tweets or labels ([`SearchTwitter`](#Twitter.SearchTwitter))
 - Sending a Tweet ([`Tweet`](#Twitter.Tweet))
 - Sending a Tweet with an Image ([`TweetWithImage`](#Twitter.TweetWithImage))
 - Directing a message to a specific user ([`DirectMessage`](#Twitter.DirectMessage))
 - Receiving the most recent messages directed to the logged-in user ([`RequestDirectMessages`](#Twitter.RequestDirectMessages))
 - Following a specific user ([`Follow`](#Twitter.Follow))
 - Ceasing to follow a specific user ([`StopFollowing`](#Twitter.StopFollowing))
 - Getting a list of users following the logged-in user ([`RequestFollowers`](#Twitter.RequestFollowers))
 - Getting the most recent messages of users followed by the logged-in user ([`RequestFriendTimeline`](#Twitter.RequestFriendTimeline))
 - Getting the most recent mentions of the logged-in user ([`RequestMentions`](#Twitter.RequestMentions))

 You must obtain a Consumer Key and Consumer Secret for Twitter authorization specific to your
 app from http://twitter.com/oauth_clients/new



### Properties  {#Twitter-Properties}

{:.properties}

{:id="Twitter.ConsumerKey" .text} *ConsumerKey*
: The consumer key to be used when authorizing with Twitter via OAuth.

{:id="Twitter.ConsumerSecret" .text} *ConsumerSecret*
: The consumer secret to be used when authorizing with Twitter via OAuth.

{:id="Twitter.DirectMessages" .list .ro .bo} *DirectMessages*
: This property contains a list of the most recent messages mentioning the logged-in user.
 Initially, the list is empty. To set it, the program must:

   1. Call the [`Authorize`](#Twitter.Authorize) method.
   2. Wait for the [`IsAuthorized`](#Twitter.IsAuthorized) event.
   3. Call the [`RequestDirectMessages`](#Twitter.RequestDirectMessages) method.
   4, Wait for the [`DirectMessagesReceived`](#Twitter.DirectMessagesReceived) event.

   The value of this property will then be set to the list of direct messages retrieved (and
 maintain that value until any subsequent call to [`RequestDirectMessages`](#Twitter.RequestDirectMessages)).

{:id="Twitter.Followers" .list .ro .bo} *Followers*
: This property contains a list of the followers of the logged-in user. Initially, the list is
 empty. To set it, the program must:

   1. Call the [`Authorize`](#Twitter.Authorize) method.
   2. Wait for the [`IsAuthorized`](#Twitter.IsAuthorized) event.
   3. Call the [`RequestFollowers`](#Twitter.RequestFollowers) method.
   4. Wait for the [`FollowersReceived`](#Twitter.FollowersReceived) event.

   The value of this property will then be set to the list of followers (and maintain its value
 until any subsequent call to [`RequestFollowers`](#Twitter.RequestFollowers)).

{:id="Twitter.FriendTimeline" .list .ro .bo} *FriendTimeline*
: This property contains the 20 most recent messages of users being followed. Initially, the
 list is empty. To set it, the program must:
 
   1. Call the [`Authorize`](#Twitter.Authorize) method.
   2. Wait for the [`IsAuthorized`](#Twitter.IsAuthorized) event.
   3. Specify users to follow with one or more calls to the [`Follow`](#Twitter.Follow) method.
   4. Call the [`RequestFriendTimeline`](#Twitter.RequestFriendTimeline)  method.
   5. Wait for the [`FriendTimelineReceived`](#Twitter.FriendTimelineReceived) event.

   The value of this property will then be set to the list of messages (and maintain its value
 until any subsequent call to [`RequestFriendTimeline`](#Twitter.RequestFriendTimeline).

{:id="Twitter.Mentions" .list .ro .bo} *Mentions*
: This property contains a list of mentions of the logged-in user. Initially, the list is empty.
 To set it, the program must:
 
   1. Call the [`Authorize`](#Twitter.Authorize) method.
   2. Wait for the [`IsAuthorized`](#Twitter.IsAuthorized) event.
   3. Call the [`RequestMentions`](#Twitter.RequestMentions) method.
   4. Wait for the [`MentionsReceived`](#Twitter.MentionsReceived) event.
 
   The value of this property will then be set to the list of mentions (and will maintain its
 value until any subsequent calls to [`RequestMentions`](#Twitter.RequestMentions)).

{:id="Twitter.SearchResults" .list .ro .bo} *SearchResults*
: This property, which is initially empty, is set to a list of search results after the program:

   1. Calls the [`SearchTwitter`](#Twitter.SearchTwitter) method.
   2. Waits for the [`SearchSuccessful`](#Twitter.SearchSuccessful) event.

   The value of the property will then be the same as the parameter to
 [`SearchSuccessful`](#Twitter.SearchSuccessful). Note that it is not necessary to call the [`Authorize`](#Twitter.Authorize)
 method before calling [`SearchTwitter`](#Twitter.SearchTwitter).

{:id="Twitter.Username" .text .ro .bo} *Username*
: The user name of the authorized user. Empty if there is no authorized user.

### Events  {#Twitter-Events}

{:.events}

{:id="Twitter.DirectMessagesReceived"} DirectMessagesReceived(*messages*{:.list})
: This event is raised when the recent messages requested through
 [`RequestDirectMessages`](#Twitter.RequestDirectMessages) have been retrieved. A list of the messages can then be found
 in the `messages`{:.variable.block} parameter or the [`DirectMessages`](#Twitter.DirectMessages) property.

{:id="Twitter.FollowersReceived"} FollowersReceived(*followers2*{:.list})
: This event is raised when all of the followers of the logged-in user requested through
 [`RequestFollowers`](#Twitter.RequestFollowers) have been retrieved. A list of the followers can then be found in
 the `followers`{:.variable.block} parameter or the [`Followers`](#Twitter.Followers) property.

{:id="Twitter.FriendTimelineReceived"} FriendTimelineReceived(*timeline*{:.list})
: This event is raised when the messages requested through [`RequestFriendTimeline`](#Twitter.RequestFriendTimeline) have
 been retrieved. The `timeline`{:.variable.block} parameter and the [`FriendTimeline`](#Twitter.FriendTimeline)
 property will contain a list of lists, where each sub-list contains a status update of the
 form (username message).

{:id="Twitter.IsAuthorized"} IsAuthorized()
: This event is raised after the program calls [`Authorize`](#Twitter.Authorize) if the authorization was
 successful. It is also called after a call to [`CheckAuthorized`](#Twitter.CheckAuthorized) if we already have a
 valid access token. After this event has been raised, any other method for this component can
 be called.

{:id="Twitter.MentionsReceived"} MentionsReceived(*mentions*{:.list})
: This event is raised when the mentions of the logged-in user requested through
 [`RequestMentions`](#Twitter.RequestMentions) have been retrieved. A list of the mentions can then be found in
 the `mentions`{:.variable.block} parameter or the [`Mentions`](#Twitter.Mentions) property.

{:id="Twitter.SearchSuccessful"} SearchSuccessful(*searchResults*{:.list})
: This event is raised when the results of the search requested through
 [`SearchTwitter`](#Twitter.SearchTwitter) have been retrieved. A list of the results can then be found in
 the `results`{:.variable.block} parameter or the [`SearchResults`](#Twitter.SearchResults) property.

### Methods  {#Twitter-Methods}

{:.methods}

{:id="Twitter.Authorize" class="method"} <i/> Authorize()
: Redirects user to login to Twitter via the Web browser using the OAuth protocol if we don't already have authorization.

{:id="Twitter.CheckAuthorized" class="method"} <i/> CheckAuthorized()
: Check whether we already have access, and if so, causes the [`IsAuthorized`](#Twitter.IsAuthorized) event
 handler to be called.

{:id="Twitter.DeAuthorize" class="method"} <i/> DeAuthorize()
: Removes Twitter authorization from this running app instance.

{:id="Twitter.DirectMessage" class="method"} <i/> DirectMessage(*user*{:.text},*message*{:.text})
: This sends a direct (private) message to the specified user. The message will be trimmed if it
 exceeds 160 characters.

   __Requirements__: This should only be called after the [`IsAuthorized`](#Twitter.IsAuthorized) event has
 been raised, indicating that the user has successfully logged in to Twitter.

{:id="Twitter.Follow" class="method"} <i/> Follow(*user*{:.text})
: Starts following a user.

{:id="Twitter.RequestDirectMessages" class="method"} <i/> RequestDirectMessages()
: Requests the 20 most recent direct messages sent to the logged-in user. When the messages have
 been retrieved, the system will raise the [`DirectMessagesReceived`](#Twitter.DirectMessagesReceived) event and set
 the [`DirectMessages`](#Twitter.DirectMessages) property to the list of messages.

   __Requirements__: This should only be called after the [`IsAuthorized`](#Twitter.IsAuthorized) event has
 been raised, indicating that the user has successfully logged in to Twitter.

{:id="Twitter.RequestFollowers" class="method"} <i/> RequestFollowers()
: Gets who is following you.

{:id="Twitter.RequestFriendTimeline" class="method"} <i/> RequestFriendTimeline()
: Gets the most recent 20 messages in the user's timeline.

{:id="Twitter.RequestMentions" class="method"} <i/> RequestMentions()
: Requests the 20 most recent mentions of the logged-in user. When the mentions have been
 retrieved, the system will raise the [`MentionsReceived`](#Twitter.MentionsReceived) event and set the
 [`Mentions`](#Twitter.Mentions) property to the list of mentions.

   __Requirements__: This should only be called after the [`IsAuthorized`](#Twitter.IsAuthorized) event has
 been raised, indicating that the user has successfully logged in to Twitter.

{:id="Twitter.SearchTwitter" class="method"} <i/> SearchTwitter(*query*{:.text})
: This searches Twitter for the given String query.

   __Requirements__: This should only be called after the [`IsAuthorized`](#Twitter.IsAuthorized) event has
 been raised, indicating that the user has successfully logged in to Twitter.

{:id="Twitter.StopFollowing" class="method"} <i/> StopFollowing(*user*{:.text})
: Stops following a user.

{:id="Twitter.Tweet" class="method"} <i/> Tweet(*status*{:.text})
: This sends a tweet as the logged-in user with the specified Text, which will be trimmed if it
 exceeds 160 characters.

   __Requirements__: This should only be called after the [`IsAuthorized`](#Twitter.IsAuthorized) event has
 been raised, indicating that the user has successfully logged in to Twitter.

{:id="Twitter.TweetWithImage" class="method"} <i/> TweetWithImage(*status*{:.text},*imagePath*{:.text})
: This sends a tweet as the logged-in user with the specified Text and a path to the image to be
 uploaded, which will be trimmed if it exceeds 160 characters. If an image is not found or
 invalid, the update will not be sent.

   __Requirements__: This should only be called after the [`IsAuthorized`](#Twitter.IsAuthorized) event has
 been raised, indicating that the user has successfully logged in to Twitter.
