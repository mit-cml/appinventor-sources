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

### Properties  {#ContactPicker-Properties}

{:.properties}

{:id="ContactPicker.BackgroundColor" .color} *BackgroundColor*
: Returns the button's background color

{:id="ContactPicker.ContactName" .text .ro .bo} *ContactName*
: Name property getter method.

{:id="ContactPicker.ContactUri" .text .ro .bo} *ContactUri*
: URI that specifies the location of the contact on the device.

{:id="ContactPicker.EmailAddress" .text .ro .bo} *EmailAddress*
: EmailAddress property getter method.

{:id="ContactPicker.EmailAddressList" .list .ro .bo} *EmailAddressList*
: EmailAddressList property getter method.

{:id="ContactPicker.Enabled" .boolean} *Enabled*
: If set, user can tap ContactPicker to cause action.

{:id="ContactPicker.FontBold" .boolean} *FontBold*
: If set, ContactPicker text is displayed in bold.

{:id="ContactPicker.FontItalic" .boolean} *FontItalic*
: If set, ContactPicker text is displayed in italics.

{:id="ContactPicker.FontSize" .number} *FontSize*
: Point size for ContactPicker text.

{:id="ContactPicker.FontTypeface" .number .do} *FontTypeface*
: Font family for ContactPicker text.

{:id="ContactPicker.Height" .number .bo} *Height*
: Specifies the vertical height of the ContactPicker, measured in pixels.

{:id="ContactPicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the ContactPicker as a percentage of the height of the Screen.

{:id="ContactPicker.Image" .text} *Image*
: Image to display on button.

{:id="ContactPicker.PhoneNumber" .text .ro .bo} *PhoneNumber*
: PhoneNumber property getter method.

{:id="ContactPicker.PhoneNumberList" .list .ro .bo} *PhoneNumberList*
: PhoneNumberList property getter method.

{:id="ContactPicker.Picture" .text .ro .bo} *Picture*
: Picture URI for this contact, which can be
 used to retrieve the contact's photo and other fields.

{:id="ContactPicker.Shape" .number .do} *Shape*
: Returns the style of the button.

{:id="ContactPicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown  for a ContactPicker that as an image as background.

{:id="ContactPicker.Text" .text} *Text*
: Text to display on ContactPicker.

{:id="ContactPicker.TextAlignment" .number .do} *TextAlignment*
: Left, center, or right.

{:id="ContactPicker.TextColor" .color} *TextColor*
: Color for button text.

{:id="ContactPicker.Visible" .boolean} *Visible*
: Returns true iff the ContactPicker is visible.

{:id="ContactPicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the ContactPicker, measured in pixels.

{:id="ContactPicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the ContactPicker as a percentage of the width of the Screen.

### Events  {#ContactPicker-Events}

{:.events}

{:id="ContactPicker.AfterPicking"} AfterPicking()
: Event to be raised after the picker activity returns its
 result and the properties have been filled in.

{:id="ContactPicker.BeforePicking"} BeforePicking()
: Event to raise when the ContactPicker is clicked or the list is shown
 using the Open block.  This event occurs before the list of items is displayed, and 
 can be used to prepare the list before it is shown.

{:id="ContactPicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the ContactPicker so it is now possible to click it.

{:id="ContactPicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the ContactPicker so it is now no longer possible to click it.

{:id="ContactPicker.TouchDown"} TouchDown()
: Indicates that the ContactPicker was pressed down.

{:id="ContactPicker.TouchUp"} TouchUp()
: Indicates that the ContactPicker has been released.

### Methods  {#ContactPicker-Methods}

{:.methods}

{:id="ContactPicker.Open" class="method"} <i/> Open()
: Opens the picker, as though the user clicked on it.

{:id="ContactPicker.ViewContact" class="method"} <i/> ViewContact(*uri*{:.text})
: view a contact via its URI

## EmailPicker  {#EmailPicker}

### Properties  {#EmailPicker-Properties}

{:.properties}

{:id="EmailPicker.BackgroundColor" .color} *BackgroundColor*
: The background color of the input box.  You can choose a color by name in the Designer or in the Blocks Editor.  The default background color is 'default' (shaded 3-D look).

{:id="EmailPicker.Enabled" .boolean} *Enabled*
: Whether the user can enter text into the EmailPicker.  By default, this is true.

{:id="EmailPicker.FontBold" .boolean .do} *FontBold*
: Whether the font for the text should be bold.  By default, it is not.

{:id="EmailPicker.FontItalic" .boolean .do} *FontItalic*
: Whether the text should appear in italics.  By default, it does not.

{:id="EmailPicker.FontSize" .number} *FontSize*
: The font size for the text.  By default, it is 14.0 points.

{:id="EmailPicker.FontTypeface" .number .do} *FontTypeface*
: The font for the text.  The value can be changed in the Designer.

{:id="EmailPicker.Height" .number .bo} *Height*
: Specifies the vertical height of the EmailPicker, measured in pixels.

{:id="EmailPicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the EmailPicker as a percentage of the height of the Screen.

{:id="EmailPicker.Hint" .text} *Hint*
: Text that should appear faintly in the EmailPicker to provide a hint as to what the user should enter.  This can only be seen if the <code>Text</code> property is empty.

{:id="EmailPicker.Text" .text} *Text*
: Returns the textbox contents.

{:id="EmailPicker.TextAlignment" .number .do} *TextAlignment*
: Whether the text should be left justified, centered, or right justified.  By default, text is left justified.

{:id="EmailPicker.TextColor" .color} *TextColor*
: The color for the text.  You can choose a color by name in the Designer or in the Blocks Editor.  The default text color is black.

{:id="EmailPicker.Visible" .boolean} *Visible*
: Returns true iff the EmailPicker is visible.

{:id="EmailPicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the EmailPicker, measured in pixels.

{:id="EmailPicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the EmailPicker as a percentage of the width of the Screen.

### Events  {#EmailPicker-Events}

{:.events}

{:id="EmailPicker.GotFocus"} GotFocus()
: Event raised when this component is selected for input, such as by
 the user touching it.

{:id="EmailPicker.LostFocus"} LostFocus()
: Event raised when the EmailPicker is no longer selected for input, such
 as if the user touches a different text box.

### Methods  {#EmailPicker-Methods}

{:.methods}

{:id="EmailPicker.RequestFocus" class="method"} <i/> RequestFocus()
: Sets the EmailPicker active.

## PhoneCall  {#PhoneCall}

### Properties  {#PhoneCall-Properties}

{:.properties}

{:id="PhoneCall.PhoneNumber" .text} *PhoneNumber*
: PhoneNumber property getter method.

### Events  {#PhoneCall-Events}

{:.events}

{:id="PhoneCall.IncomingCallAnswered"} IncomingCallAnswered(*phoneNumber*{:.text})
: Event indicating that an incoming phone call is answered. phoneNumber is the incoming call phone number.

{:id="PhoneCall.PhoneCallEnded"} PhoneCallEnded(*status*{:.number},*phoneNumber*{:.text})
: Event indicating that a phone call has ended. If status is 1, incoming call is missed or rejected; if status is 2, incoming call is answered before hanging up; if status is 3, outgoing call is hung up. phoneNumber is the ended call phone number.

{:id="PhoneCall.PhoneCallStarted"} PhoneCallStarted(*status*{:.number},*phoneNumber*{:.text})
: Event indicating that a phonecall has started. If status is 1, incoming call is ringing; if status is 2, outgoing call is dialled. phoneNumber is the incoming/outgoing phone number.

### Methods  {#PhoneCall-Methods}

{:.methods}

{:id="PhoneCall.MakePhoneCall" class="method"} <i/> MakePhoneCall()
: Makes a phone call using the number in the PhoneNumber property.

{:id="PhoneCall.MakePhoneCallDirect" class="method"} <i/> MakePhoneCallDirect()
: Makes a phone call using the number in the PhoneNumber property.

## PhoneNumberPicker  {#PhoneNumberPicker}

### Properties  {#PhoneNumberPicker-Properties}

{:.properties}

{:id="PhoneNumberPicker.BackgroundColor" .color} *BackgroundColor*
: Returns the button's background color

{:id="PhoneNumberPicker.ContactName" .text .ro .bo} *ContactName*
: Name property getter method.

{:id="PhoneNumberPicker.ContactUri" .text .ro .bo} *ContactUri*
: URI that specifies the location of the contact on the device.

{:id="PhoneNumberPicker.EmailAddress" .text .ro .bo} *EmailAddress*
: EmailAddress property getter method.

{:id="PhoneNumberPicker.EmailAddressList" .list .ro .bo} *EmailAddressList*
: EmailAddressList property getter method.

{:id="PhoneNumberPicker.Enabled" .boolean} *Enabled*
: If set, user can tap ContactPicker to cause action.

{:id="PhoneNumberPicker.FontBold" .boolean} *FontBold*
: If set, ContactPicker text is displayed in bold.

{:id="PhoneNumberPicker.FontItalic" .boolean} *FontItalic*
: If set, ContactPicker text is displayed in italics.

{:id="PhoneNumberPicker.FontSize" .number} *FontSize*
: Point size for ContactPicker text.

{:id="PhoneNumberPicker.FontTypeface" .number .do} *FontTypeface*
: Font family for ContactPicker text.

{:id="PhoneNumberPicker.Height" .number .bo} *Height*
: Specifies the vertical height of the ContactPicker, measured in pixels.

{:id="PhoneNumberPicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the ContactPicker as a percentage of the height of the Screen.

{:id="PhoneNumberPicker.Image" .text} *Image*
: Image to display on button.

{:id="PhoneNumberPicker.PhoneNumber" .text .ro .bo} *PhoneNumber*
: PhoneNumber property getter method.

{:id="PhoneNumberPicker.PhoneNumberList" .list .ro .bo} *PhoneNumberList*
: PhoneNumberList property getter method.

{:id="PhoneNumberPicker.Picture" .text .ro .bo} *Picture*
: Picture URI for this contact, which can be
 used to retrieve the contact's photo and other fields.

{:id="PhoneNumberPicker.Shape" .number .do} *Shape*
: Returns the style of the button.

{:id="PhoneNumberPicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown  for a ContactPicker that as an image as background.

{:id="PhoneNumberPicker.Text" .text} *Text*
: Text to display on ContactPicker.

{:id="PhoneNumberPicker.TextAlignment" .number .do} *TextAlignment*
: Left, center, or right.

{:id="PhoneNumberPicker.TextColor" .color} *TextColor*
: Color for button text.

{:id="PhoneNumberPicker.Visible" .boolean} *Visible*
: Returns true iff the ContactPicker is visible.

{:id="PhoneNumberPicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the ContactPicker, measured in pixels.

{:id="PhoneNumberPicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the ContactPicker as a percentage of the width of the Screen.

### Events  {#PhoneNumberPicker-Events}

{:.events}

{:id="PhoneNumberPicker.AfterPicking"} AfterPicking()
: Event to be raised after the picker activity returns its
 result and the properties have been filled in.

{:id="PhoneNumberPicker.BeforePicking"} BeforePicking()
: Event to raise when the ContactPicker is clicked or the list is shown
 using the Open block.  This event occurs before the list of items is displayed, and 
 can be used to prepare the list before it is shown.

{:id="PhoneNumberPicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the ContactPicker so it is now possible to click it.

{:id="PhoneNumberPicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the ContactPicker so it is now no longer possible to click it.

{:id="PhoneNumberPicker.TouchDown"} TouchDown()
: Indicates that the ContactPicker was pressed down.

{:id="PhoneNumberPicker.TouchUp"} TouchUp()
: Indicates that the ContactPicker has been released.

### Methods  {#PhoneNumberPicker-Methods}

{:.methods}

{:id="PhoneNumberPicker.Open" class="method"} <i/> Open()
: Opens the picker, as though the user clicked on it.

{:id="PhoneNumberPicker.ViewContact" class="method"} <i/> ViewContact(*uri*{:.text})
: view a contact via its URI

## Sharing  {#Sharing}

### Methods  {#Sharing-Methods}

{:.methods}

{:id="Sharing.ShareFile" class="method"} <i/> ShareFile(*file*{:.text})
: Shares a file through any capable application installed on the phone by displaying a list of the available apps and allowing the user to choose one from the list. The selected app will open with the file inserted on it.

{:id="Sharing.ShareFileWithMessage" class="method"} <i/> ShareFileWithMessage(*file*{:.text},*message*{:.text})
: Shares both a file and a message through any capable application installed on the phone by displaying a list of available apps and allowing the user to  choose one from the list. The selected app will open with the file and message inserted on it.

{:id="Sharing.ShareMessage" class="method"} <i/> ShareMessage(*message*{:.text})
: Shares a message through any capable application installed on the phone by displaying a list of the available apps and allowing the user to choose one from the list. The selected app will open with the message inserted on it.

## Texting  {#Texting}

### Properties  {#Texting-Properties}

{:.properties}

{:id="Texting.GoogleVoiceEnabled" .boolean} *GoogleVoiceEnabled*
: If true, then SendMessage will attempt to send messages over Wifi using Google Voice.  This requires that the Google Voice app must be installed and set up on the phone or tablet, with a Google Voice account.  If GoogleVoiceEnabled is false, the device must have phone and texting service in order to send or receive messages with this component.

{:id="Texting.Message" .text} *Message*
: The message that will be sent when the SendMessage method is called.

{:id="Texting.PhoneNumber" .text} *PhoneNumber*
: Sets the phone number to send the text message to when the SendMessage function is called.

{:id="Texting.ReceivingEnabled" .number} *ReceivingEnabled*
: If set to 1 (OFF) no messages will be received.  If set to 2 (FOREGROUND) or3 (ALWAYS) the component will respond to messages if it is running. If the app is not running then the message will be discarded if set to 2 (FOREGROUND). If set to 3 (ALWAYS) and the app is not running the phone will show a notification.  Selecting the notification will bring up the app and signal the MessageReceived event.  Messages received when the app is dormant will be queued, and so several MessageReceived events might appear when the app awakens.  As an app developer, it would be a good idea to give your users control over this property, so they can make their phones ignore text messages when your app is installed.

### Events  {#Texting-Events}

{:.events}

{:id="Texting.MessageReceived"} MessageReceived(*number*{:.text},*messageText*{:.text})
: Event that's raised when a text message is received by the phone.

### Methods  {#Texting-Methods}

{:.methods}

{:id="Texting.SendMessage" class="method"} <i/> SendMessage()
: Send a text message

{:id="Texting.SendMessageDirect" class="method"} <i/> SendMessageDirect()
: Send a text message directly

## Twitter  {#Twitter}

### Properties  {#Twitter-Properties}

{:.properties}

{:id="Twitter.ConsumerKey" .text} *ConsumerKey*
: ConsumerKey property getter method.

{:id="Twitter.ConsumerSecret" .text} *ConsumerSecret*
: ConsumerSecret property getter method.

{:id="Twitter.DirectMessages" .list .ro .bo} *DirectMessages*
: This property contains a list of the most recent messages mentioning the logged-in user.  Initially, the list is empty.  To set it, the program must: <ol> <li> Call the <code>Authorize</code> method.</li> <li> Wait for the <code>Authorized</code> event.</li> <li> Call the <code>RequestDirectMessages</code> method.</li> <li> Wait for the <code>DirectMessagesReceived</code> event.</li></ol>
The value of this property will then be set to the list of direct messages retrieved (and maintain that value until any subsequent call to <code>RequestDirectMessages</code>).

{:id="Twitter.Followers" .list .ro .bo} *Followers*
: This property contains a list of the followers of the logged-in user.  Initially, the list is empty.  To set it, the program must: <ol> <li> Call the <code>Authorize</code> method.</li> <li> Wait for the <code>IsAuthorized</code> event.</li> <li> Call the <code>RequestFollowers</code> method.</li> <li> Wait for the <code>FollowersReceived</code> event.</li></ol>
The value of this property will then be set to the list of followers (and maintain its value until any subsequent call to <code>RequestFollowers</code>).

{:id="Twitter.FriendTimeline" .list .ro .bo} *FriendTimeline*
: This property contains the 20 most recent messages of users being followed.  Initially, the list is empty.  To set it, the program must: <ol> <li> Call the <code>Authorize</code> method.</li> <li> Wait for the <code>IsAuthorized</code> event.</li> <li> Specify users to follow with one or more calls to the <code>Follow</code> method.</li> <li> Call the <code>RequestFriendTimeline</code> method.</li> <li> Wait for the <code>FriendTimelineReceived</code> event.</li> </ol>
The value of this property will then be set to the list of messages (and maintain its value until any subsequent call to <code>RequestFriendTimeline</code>.

{:id="Twitter.Mentions" .list .ro .bo} *Mentions*
: This property contains a list of mentions of the logged-in user.  Initially, the list is empty.  To set it, the program must: <ol> <li> Call the <code>Authorize</code> method.</li> <li> Wait for the <code>IsAuthorized</code> event.</li> <li> Call the <code>RequestMentions</code> method.</li> <li> Wait for the <code>MentionsReceived</code> event.</li></ol>
The value of this property will then be set to the list of mentions (and will maintain its value until any subsequent calls to <code>RequestMentions</code>).

{:id="Twitter.SearchResults" .list .ro .bo} *SearchResults*
: This property, which is initially empty, is set to a list of search results after the program: <ol><li>Calls the <code>SearchTwitter</code> method.</li> <li>Waits for the <code>SearchSuccessful</code> event.</li></ol>
The value of the property will then be the same as the parameter to <code>SearchSuccessful</code>.  Note that it is not necessary to call the <code>Authorize</code> method before calling <code>SearchTwitter</code>.

{:id="Twitter.TwitPic_API_Key" .text .bo} *TwitPic_API_Key*
: TwitPicAPIkey property getter method.

{:id="Twitter.Username" .text .ro .bo} *Username*
: The user name of the authorized user. Empty if there is no authorized user.

### Events  {#Twitter-Events}

{:.events}

{:id="Twitter.DirectMessagesReceived"} DirectMessagesReceived(*messages*{:.list})
: This event is raised when the recent messages requested through <code>RequestDirectMessages</code> have been retrieved. A list of the messages can then be found in the <code>messages</code> parameter or the <code>Messages</code> property.

{:id="Twitter.FollowersReceived"} FollowersReceived(*followers2*{:.list})
: This event is raised when all of the followers of the logged-in user requested through <code>RequestFollowers</code> have been retrieved. A list of the followers can then be found in the <code>followers</code> parameter or the <code>Followers</code> property.

{:id="Twitter.FriendTimelineReceived"} FriendTimelineReceived(*timeline*{:.list})
: This event is raised when the messages requested through <code>RequestFriendTimeline</code> have been retrieved. The <code>timeline</code> parameter and the <code>Timeline</code> property will contain a list of lists, where each sub-list contains a status update of the form (username message)

{:id="Twitter.IsAuthorized"} IsAuthorized()
: This event is raised after the program calls <code>Authorize</code> if the authorization was successful.  It is also called after a call to <code>CheckAuthorized</code> if we already have a valid access token. After this event has been raised, any other method for this component can be called.

{:id="Twitter.MentionsReceived"} MentionsReceived(*mentions*{:.list})
: This event is raised when the mentions of the logged-in user requested through <code>RequestMentions</code> have been retrieved.  A list of the mentions can then be found in the <code>mentions</code> parameter or the <code>Mentions</code> property.

{:id="Twitter.SearchSuccessful"} SearchSuccessful(*searchResults*{:.list})
: This event is raised when the results of the search requested through <code>SearchSuccessful</code> have been retrieved. A list of the results can then be found in the <code>results</code> parameter or the <code>Results</code> property.

### Methods  {#Twitter-Methods}

{:.methods}

{:id="Twitter.Authorize" class="method"} <i/> Authorize()
: Redirects user to login to Twitter via the Web browser using the OAuth protocol if we don't already have authorization.

{:id="Twitter.CheckAuthorized" class="method"} <i/> CheckAuthorized()
: Checks whether we already have access, and if so, causes IsAuthorized event handler to be called.

{:id="Twitter.DeAuthorize" class="method"} <i/> DeAuthorize()
: Removes Twitter authorization from this running app instance

{:id="Twitter.DirectMessage" class="method"} <i/> DirectMessage(*user*{:.text},*message*{:.text})
: This sends a direct (private) message to the specified user.  The message will be trimmed if it exceeds 160characters. <p><u>Requirements</u>: This should only be called after the <code>IsAuthorized</code> event has been raised, indicating that the user has successfully logged in to Twitter.</p>

{:id="Twitter.Follow" class="method"} <i/> Follow(*user*{:.text})
: Starts following a user.

{:id="Twitter.RequestDirectMessages" class="method"} <i/> RequestDirectMessages()
: Requests the 20 most recent direct messages sent to the logged-in user.  When the messages have been retrieved, the system will raise the <code>DirectMessagesReceived</code> event and set the <code>DirectMessages</code> property to the list of messages.<p><u>Requirements</u>: This should only be called after the <code>IsAuthorized</code> event has been raised, indicating that the user has successfully logged in to Twitter.</p>

{:id="Twitter.RequestFollowers" class="method"} <i/> RequestFollowers()
: Gets who is following you.

{:id="Twitter.RequestFriendTimeline" class="method"} <i/> RequestFriendTimeline()
: Gets the most recent 20 messages in the user's timeline.

{:id="Twitter.RequestMentions" class="method"} <i/> RequestMentions()
: Requests the 20 most recent mentions of the logged-in user.  When the mentions have been retrieved, the system will raise the <code>MentionsReceived</code> event and set the <code>Mentions</code> property to the list of mentions.<p><u>Requirements</u>: This should only be called after the <code>IsAuthorized</code> event has been raised, indicating that the user has successfully logged in to Twitter.</p>

{:id="Twitter.SearchTwitter" class="method"} <i/> SearchTwitter(*query*{:.text})
: This searches Twitter for the given String query.<p><u>Requirements</u>: This should only be called after the <code>IsAuthorized</code> event has been raised, indicating that the user has successfully logged in to Twitter.</p>

{:id="Twitter.StopFollowing" class="method"} <i/> StopFollowing(*user*{:.text})
: Stops following a user.

{:id="Twitter.Tweet" class="method"} <i/> Tweet(*status*{:.text})
: This sends a tweet as the logged-in user with the specified Text, which will be trimmed if it exceeds 160 characters. <p><u>Requirements</u>: This should only be called after the <code>IsAuthorized</code> event has been raised, indicating that the user has successfully logged in to Twitter.</p>

{:id="Twitter.TweetWithImage" class="method"} <i/> TweetWithImage(*status*{:.text},*imagePath*{:.text})
: This sends a tweet as the logged-in user with the specified Text and a path to the image to be uploaded, which will be trimmed if it exceeds 160 characters. If an image is not found or invalid, only the text will be tweeted.<p><u>Requirements</u>: This should only be called after the <code>IsAuthorized</code> event has been raised, indicating that the user has successfully logged in to Twitter.</p>