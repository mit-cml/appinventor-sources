---
layout: documentation
title: User Interface
---

[&laquo; Back to index](index.html)
# User Interface

Table of Contents:

* [Button](#Button)
* [CheckBox](#CheckBox)
* [DatePicker](#DatePicker)
* [Image](#Image)
* [Label](#Label)
* [ListPicker](#ListPicker)
* [ListView](#ListView)
* [Notifier](#Notifier)
* [PasswordTextBox](#PasswordTextBox)
* [Screen](#Screen)
* [Slider](#Slider)
* [Spinner](#Spinner)
* [Switch](#Switch)
* [TextBox](#TextBox)
* [TimePicker](#TimePicker)
* [WebViewer](#WebViewer)

## Button  {#Button}

### Properties  {#Button-Properties}

{:.properties}

{:id="Button.BackgroundColor" .color} *BackgroundColor*
: Returns the button's background color

{:id="Button.Enabled" .boolean} *Enabled*
: If set, user can tap Button to cause action.

{:id="Button.FontBold" .boolean} *FontBold*
: If set, Button text is displayed in bold.

{:id="Button.FontItalic" .boolean} *FontItalic*
: If set, Button text is displayed in italics.

{:id="Button.FontSize" .number} *FontSize*
: Point size for Button text.

{:id="Button.FontTypeface" .number .do} *FontTypeface*
: Font family for Button text.

{:id="Button.Height" .number .bo} *Height*
: Specifies the vertical height of the Button, measured in pixels.

{:id="Button.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the Button as a percentage of the height of the Screen.

{:id="Button.Image" .text} *Image*
: Image to display on button.

{:id="Button.Shape" .number .do} *Shape*
: Returns the style of the button.

{:id="Button.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown  for a Button that as an image as background.

{:id="Button.Text" .text} *Text*
: Text to display on Button.

{:id="Button.TextAlignment" .number .do} *TextAlignment*
: Left, center, or right.

{:id="Button.TextColor" .color} *TextColor*
: Color for button text.

{:id="Button.Visible" .boolean} *Visible*
: Returns true iff the Button is visible.

{:id="Button.Width" .number .bo} *Width*
: Specifies the horizontal width of the Button, measured in pixels.

{:id="Button.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the Button as a percentage of the width of the Screen.

### Events  {#Button-Events}

{:.events}

{:id="Button.Click"} Click()
: User tapped and released the button.

{:id="Button.GotFocus"} GotFocus()
: Indicates the cursor moved over the Button so it is now possible to click it.

{:id="Button.LongClick"} LongClick()
: User held the button down.

{:id="Button.LostFocus"} LostFocus()
: Indicates the cursor moved away from the Button so it is now no longer possible to click it.

{:id="Button.TouchDown"} TouchDown()
: Indicates that the Button was pressed down.

{:id="Button.TouchUp"} TouchUp()
: Indicates that the Button has been released.

## CheckBox  {#CheckBox}

### Properties  {#CheckBox-Properties}

{:.properties}

{:id="CheckBox.BackgroundColor" .color} *BackgroundColor*
: Returns the background color of the CheckBox as an alpha-red-green-blue
 integer.

{:id="CheckBox.Checked" .boolean} *Checked*
: Returns true if the checkbox is checked.

{:id="CheckBox.Enabled" .boolean} *Enabled*
: Returns true if the CheckBox is active and clickable.

{:id="CheckBox.FontBold" .boolean .do} *FontBold*
: Returns true if the text of the CheckBox should be bold.
 If bold has been requested, this property will return true, even if the
 font does not support bold.

{:id="CheckBox.FontItalic" .boolean .do} *FontItalic*
: Returns true if the text of the CheckBox should be italic.
 If italic has been requested, this property will return true, even if the
 font does not support italic.

{:id="CheckBox.FontSize" .number} *FontSize*
: Returns the text font size of the CheckBox, measured in sp(scale-independent pixels).

{:id="CheckBox.FontTypeface" .number .do} *FontTypeface*
: Returns the text font face of the CheckBox as default, serif, sans
 serif, or monospace.

{:id="CheckBox.Height" .number .bo} *Height*
: Specifies the vertical height of the CheckBox, measured in pixels.

{:id="CheckBox.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the CheckBox as a percentage of the height of the Screen.

{:id="CheckBox.Text" .text} *Text*
: Returns the text displayed by the CheckBox.

{:id="CheckBox.TextColor" .color} *TextColor*
: Returns the text color of the CheckBox as an alpha-red-green-blue
 integer.

{:id="CheckBox.Visible" .boolean} *Visible*
: Returns true iff the CheckBox is visible.

{:id="CheckBox.Width" .number .bo} *Width*
: Specifies the horizontal width of the CheckBox, measured in pixels.

{:id="CheckBox.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the CheckBox as a percentage of the width of the Screen.

### Events  {#CheckBox-Events}

{:.events}

{:id="CheckBox.Changed"} Changed()
: Default Changed event handler.

{:id="CheckBox.GotFocus"} GotFocus()
: Default GotFocus event handler.

{:id="CheckBox.LostFocus"} LostFocus()
: Default LostFocus event handler.

## DatePicker  {#DatePicker}

### Properties  {#DatePicker-Properties}

{:.properties}

{:id="DatePicker.BackgroundColor" .color} *BackgroundColor*
: Returns the button's background color

{:id="DatePicker.Day" .number .ro .bo} *Day*
: the Day of the month that was last picked using the DatePicker.

{:id="DatePicker.Enabled" .boolean} *Enabled*
: If set, user can tap DatePicker to cause action.

{:id="DatePicker.FontBold" .boolean} *FontBold*
: If set, DatePicker text is displayed in bold.

{:id="DatePicker.FontItalic" .boolean} *FontItalic*
: If set, DatePicker text is displayed in italics.

{:id="DatePicker.FontSize" .number} *FontSize*
: Point size for DatePicker text.

{:id="DatePicker.FontTypeface" .number .do} *FontTypeface*
: Font family for DatePicker text.

{:id="DatePicker.Height" .number .bo} *Height*
: Specifies the vertical height of the DatePicker, measured in pixels.

{:id="DatePicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the DatePicker as a percentage of the height of the Screen.

{:id="DatePicker.Image" .text} *Image*
: Image to display on button.

{:id="DatePicker.Instant" .InstantInTime .ro .bo} *Instant*
: the instant of the date that was last picked using the DatePicker.

{:id="DatePicker.Month" .number .ro .bo} *Month*
: the number of the Month that was last picked using the DatePicker. Note that months start in 1 = January, 12 = December.

{:id="DatePicker.MonthInText" .text .ro .bo} *MonthInText*
: Returns the name of the Month that was last picked using the DatePicker, in textual format.

{:id="DatePicker.Shape" .number .do} *Shape*
: Returns the style of the button.

{:id="DatePicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown  for a DatePicker that as an image as background.

{:id="DatePicker.Text" .text} *Text*
: Text to display on DatePicker.

{:id="DatePicker.TextAlignment" .number .do} *TextAlignment*
: Left, center, or right.

{:id="DatePicker.TextColor" .color} *TextColor*
: Color for button text.

{:id="DatePicker.Visible" .boolean} *Visible*
: Returns true iff the DatePicker is visible.

{:id="DatePicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the DatePicker, measured in pixels.

{:id="DatePicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the DatePicker as a percentage of the width of the Screen.

{:id="DatePicker.Year" .number .ro .bo} *Year*
: the Year that was last picked using the DatePicker

### Events  {#DatePicker-Events}

{:.events}

{:id="DatePicker.AfterDateSet"} AfterDateSet()
: Event that runs after the user chooses a Date in the dialog

{:id="DatePicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the DatePicker so it is now possible to click it.

{:id="DatePicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the DatePicker so it is now no longer possible to click it.

{:id="DatePicker.TouchDown"} TouchDown()
: Indicates that the DatePicker was pressed down.

{:id="DatePicker.TouchUp"} TouchUp()
: Indicates that the DatePicker has been released.

### Methods  {#DatePicker-Methods}

{:.methods}

{:id="DatePicker.LaunchPicker" class="method"} <i/> LaunchPicker()
: Launches the DatePicker popup.

{:id="DatePicker.SetDateToDisplay" class="method"} <i/> SetDateToDisplay(*year*{:.number},*month*{:.number},*day*{:.number})
: Allows the user to set the date to be displayed when the date picker opens.
Valid values for the month field are 1-12 and 1-31 for the day field.

{:id="DatePicker.SetDateToDisplayFromInstant" class="method"} <i/> SetDateToDisplayFromInstant(*instant*{:.InstantInTime})
: Allows the user to set the date from the instant to be displayed when the date picker opens.

## Image  {#Image}

### Properties  {#Image-Properties}

{:.properties}

{:id="Image.Animation" .text .wo .bo} *Animation*
: This is a limited form of animation that can attach a small number of motion types to images.  The allowable motions are ScrollRightSlow, ScrollRight, ScrollRightFast, ScrollLeftSlow, ScrollLeft, ScrollLeftFast, and Stop

{:id="Image.Height" .number .bo} *Height*
: Specifies the vertical height of the Image, measured in pixels.

{:id="Image.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the Image as a percentage of the height of the Screen.

{:id="Image.Picture" .text} *Picture*
: Returns the path of the image's picture.

{:id="Image.RotationAngle" .number} *RotationAngle*
: Specifies the angle at which the image picture appears rotated.

{:id="Image.ScalePictureToFit" .boolean .wo} *ScalePictureToFit*
: Specifies whether the image should be resized to match the size of the ImageView.

{:id="Image.Scaling" .number .bo} *Scaling*
: This property determines how the picture scales according to the Height or Width of the Image. Scale proportionally (0) preserves the picture aspect ratio. Scale to fit (1) matches the Image area, even if the aspect ratio changes.

{:id="Image.Visible" .boolean} *Visible*
: Returns true iff the Image is visible.

{:id="Image.Width" .number .bo} *Width*
: Specifies the horizontal width of the Image, measured in pixels.

{:id="Image.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the Image as a percentage of the width of the Screen.

## Label  {#Label}

### Properties  {#Label-Properties}

{:.properties}

{:id="Label.BackgroundColor" .color} *BackgroundColor*
: Returns the label's background color as an alpha-red-green-blue
 integer.

{:id="Label.FontBold" .boolean .do} *FontBold*
: Returns true if the label's text should be bold.
 If bold has been requested, this property will return true, even if the
 font does not support bold.

{:id="Label.FontItalic" .boolean .do} *FontItalic*
: Returns true if the label's text should be italic.
 If italic has been requested, this property will return true, even if the
 font does not support italic.

{:id="Label.FontSize" .number} *FontSize*
: Returns the label's text's font size, measured in sp(scale-independent pixels).

{:id="Label.FontTypeface" .number .do} *FontTypeface*
: Returns the label's text's font face as default, serif, sans
 serif, or monospace.

{:id="Label.HTMLContent" .text .ro .bo} *HTMLContent*
: Returns the content of the Label as HTML. This is only useful if the
 HTMLFormat property is true.

{:id="Label.HTMLFormat" .boolean .do} *HTMLFormat*
: If true, then this label will show html text else it will show plain text. Note: Not all HTML is supported.

{:id="Label.HasMargins" .boolean} *HasMargins*
: Reports whether or not the label appears with margins.  All four margins (left, right, top, bottom) are the same.  This property has no effect in the designer, where labels are always shown with margins.

{:id="Label.Height" .number .bo} *Height*
: Specifies the vertical height of the Label, measured in pixels.

{:id="Label.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the Label as a percentage of the height of the Screen.

{:id="Label.Text" .text} *Text*
: Returns the text displayed by the label.

{:id="Label.TextAlignment" .number .do} *TextAlignment*
: Returns the alignment of the label's text: center, normal
 (e.g., left-justified if text is written left to right), or
 opposite (e.g., right-justified if text is written left to right).

{:id="Label.TextColor" .color} *TextColor*
: Returns the label's text color as an alpha-red-green-blue
 integer.

{:id="Label.Visible" .boolean} *Visible*
: Returns true iff the Label is visible.

{:id="Label.Width" .number .bo} *Width*
: Specifies the horizontal width of the Label, measured in pixels.

{:id="Label.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the Label as a percentage of the width of the Screen.

## ListPicker  {#ListPicker}

### Properties  {#ListPicker-Properties}

{:.properties}

{:id="ListPicker.BackgroundColor" .color} *BackgroundColor*
: Returns the button's background color

{:id="ListPicker.Elements" .list .bo} *Elements*
: Elements property getter method

{:id="ListPicker.ElementsFromString" .text .wo} *ElementsFromString*
: ElementsFromString property setter method

{:id="ListPicker.Enabled" .boolean} *Enabled*
: If set, user can tap ListPicker to cause action.

{:id="ListPicker.FontBold" .boolean} *FontBold*
: If set, ListPicker text is displayed in bold.

{:id="ListPicker.FontItalic" .boolean} *FontItalic*
: If set, ListPicker text is displayed in italics.

{:id="ListPicker.FontSize" .number} *FontSize*
: Point size for ListPicker text.

{:id="ListPicker.FontTypeface" .number .do} *FontTypeface*
: Font family for ListPicker text.

{:id="ListPicker.Height" .number .bo} *Height*
: Specifies the vertical height of the ListPicker, measured in pixels.

{:id="ListPicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the ListPicker as a percentage of the height of the Screen.

{:id="ListPicker.Image" .text} *Image*
: Image to display on button.

{:id="ListPicker.ItemBackgroundColor" .color} *ItemBackgroundColor*
: The background color of the ListPicker items.

{:id="ListPicker.ItemTextColor" .color} *ItemTextColor*
: The text color of the ListPicker items.

{:id="ListPicker.Selection" .text} *Selection*
: The selected item.  When directly changed by the programmer, the SelectionIndex property is also changed to the first item in the ListPicker with the given value.  If the value does not appear, SelectionIndex will be set to 0.

{:id="ListPicker.SelectionIndex" .number .bo} *SelectionIndex*
: The index of the currently selected item, starting at 1.  If no item is selected, the value will be 0.  If an attempt is made to set this to a number less than 1 or greater than the number of items in the ListPicker, SelectionIndex will be set to 0, and Selection will be set to the empty text.

{:id="ListPicker.Shape" .number .do} *Shape*
: Returns the style of the button.

{:id="ListPicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown  for a ListPicker that as an image as background.

{:id="ListPicker.ShowFilterBar" .boolean} *ShowFilterBar*
: Returns current state of ShowFilterBar indicating if Search Filter Bar will be displayed on ListPicker or not

{:id="ListPicker.Text" .text} *Text*
: Text to display on ListPicker.

{:id="ListPicker.TextAlignment" .number .do} *TextAlignment*
: Left, center, or right.

{:id="ListPicker.TextColor" .color} *TextColor*
: Color for button text.

{:id="ListPicker.Title" .text} *Title*
: Optional title displayed at the top of the list of choices.

{:id="ListPicker.Visible" .boolean} *Visible*
: Returns true iff the ListPicker is visible.

{:id="ListPicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the ListPicker, measured in pixels.

{:id="ListPicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the ListPicker as a percentage of the width of the Screen.

### Events  {#ListPicker-Events}

{:.events}

{:id="ListPicker.AfterPicking"} AfterPicking()
: Event to be raised after the picker activity returns its
 result and the properties have been filled in.

{:id="ListPicker.BeforePicking"} BeforePicking()
: Event to raise when the ListPicker is clicked or the list is shown
 using the Open block.  This event occurs before the list of items is displayed, and 
 can be used to prepare the list before it is shown.

{:id="ListPicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the ListPicker so it is now possible to click it.

{:id="ListPicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the ListPicker so it is now no longer possible to click it.

{:id="ListPicker.TouchDown"} TouchDown()
: Indicates that the ListPicker was pressed down.

{:id="ListPicker.TouchUp"} TouchUp()
: Indicates that the ListPicker has been released.

### Methods  {#ListPicker-Methods}

{:.methods}

{:id="ListPicker.Open" class="method"} <i/> Open()
: Opens the picker, as though the user clicked on it.

## ListView  {#ListView}

### Properties  {#ListView-Properties}

{:.properties}

{:id="ListView.BackgroundColor" .color} *BackgroundColor*
: The color of the listview background.

{:id="ListView.Elements" .list .bo} *Elements*
: List of text elements to show in the ListView.  This willsignal an error if the elements are not text strings.

{:id="ListView.ElementsFromString" .text .wo} *ElementsFromString*
: The TextView elements specified as a string with the items separated by commas such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the list.

{:id="ListView.Height" .number .bo} *Height*
: Determines the height of the list on the view.

{:id="ListView.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the ListView as a percentage of the height of the Screen.

{:id="ListView.Selection" .text} *Selection*
: Returns the text last selected in the ListView.

{:id="ListView.SelectionColor" .color} *SelectionColor*
: The color of the item when it is selected.

{:id="ListView.SelectionIndex" .number .bo} *SelectionIndex*
: The index of the currently selected item, starting at 1.  If no item is selected, the value will be 0.  If an attempt is made to set this to a number less than 1 or greater than the number of items in the ListView, SelectionIndex will be set to 0, and Selection will be set to the empty text.

{:id="ListView.ShowFilterBar" .boolean} *ShowFilterBar*
: Sets visibility of ShowFilterBar. True will show the bar, False will hide it.

{:id="ListView.TextColor" .color} *TextColor*
: The text color of the listview items.

{:id="ListView.TextSize" .number} *TextSize*
: The text size of the listview items.

{:id="ListView.Visible" .boolean} *Visible*
: Returns true iff the ListView is visible.

{:id="ListView.Width" .number .bo} *Width*
: Determines the width of the list on the view.

{:id="ListView.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the ListView as a percentage of the width of the Screen.

### Events  {#ListView-Events}

{:.events}

{:id="ListView.AfterPicking"} AfterPicking()
: Simple event to be raised after the an element has been chosen in the list. The selected element is available in the Selection property.

## Notifier  {#Notifier}

### Properties  {#Notifier-Properties}

{:.properties}

{:id="Notifier.BackgroundColor" .color .wo} *BackgroundColor*
: Specifies the background color for alerts (not dialogs).

{:id="Notifier.NotifierLength" .number .do} *NotifierLength*
: Specifies the length of time that the alert is shown -- either "short" or "long".

{:id="Notifier.TextColor" .color} *TextColor*
: Specifies the text color for alerts (not dialogs).

### Events  {#Notifier-Events}

{:.events}

{:id="Notifier.AfterChoosing"} AfterChoosing(*choice*{:.text})
: Event after the user has made a selection for ShowChooseDialog.

{:id="Notifier.AfterTextInput"} AfterTextInput(*response*{:.text})
: Event raised after the user has responded to ShowTextDialog.

{:id="Notifier.ChoosingCanceled"} ChoosingCanceled()
: Event raised when the user canceled ShowChooseDialog.

{:id="Notifier.TextInputCanceled"} TextInputCanceled()
: Event raised when the user canceled ShowTextDialog.

### Methods  {#Notifier-Methods}

{:.methods}

{:id="Notifier.DismissProgressDialog" class="method"} <i/> DismissProgressDialog()
: Dismiss a previously displayed ProgressDialog box

{:id="Notifier.LogError" class="method"} <i/> LogError(*message*{:.text})
: Writes an error message to the Android system log. See the Google Android documentation for how to access the log.

{:id="Notifier.LogInfo" class="method"} <i/> LogInfo(*message*{:.text})
: Writes an information message to the Android log.

{:id="Notifier.LogWarning" class="method"} <i/> LogWarning(*message*{:.text})
: Writes a warning message to the Android log. See the Google Android documentation for how to access the log.

{:id="Notifier.ShowAlert" class="method"} <i/> ShowAlert(*notice*{:.text})
: Display a temporary notification

{:id="Notifier.ShowChooseDialog" class="method"} <i/> ShowChooseDialog(*message*{:.text},*title*{:.text},*button1Text*{:.text},*button2Text*{:.text},*cancelable*{:.boolean})
: Shows a dialog box with two buttons, from which the user can choose.  If cancelable is true there will be an additional CANCEL button. Pressing a button will raise the AfterChoosing event.  The "choice" parameter to AfterChoosing will be the text on the button that was pressed, or "Cancel" if the  CANCEL button was pressed.

{:id="Notifier.ShowMessageDialog" class="method"} <i/> ShowMessageDialog(*message*{:.text},*title*{:.text},*buttonText*{:.text})
: Display an alert dialog with a single button that dismisses the alert.

{:id="Notifier.ShowPasswordDialog" class="method"} <i/> ShowPasswordDialog(*message*{:.text},*title*{:.text},*cancelable*{:.boolean})
: Shows a dialog box where the user can enter password (input is masked), after which the AfterTextInput event will be raised.  If cancelable is true there will be an additional CANCEL button. Entering password will raise the AfterTextInput event.  The "response" parameter to AfterTextInput will be the entered password, or "Cancel" if CANCEL button was pressed.

{:id="Notifier.ShowProgressDialog" class="method"} <i/> ShowProgressDialog(*message*{:.text},*title*{:.text})
: Shows a dialog box with an optional title and message (use empty strings if they are not wanted). This dialog box contains a spinning artifact to indicate that the program is working. It cannot be canceled by the user but must be dismissed by the App Inventor Program by using the DismissProgressDialog block.

{:id="Notifier.ShowTextDialog" class="method"} <i/> ShowTextDialog(*message*{:.text},*title*{:.text},*cancelable*{:.boolean})
: Shows a dialog box where the user can enter text, after which the AfterTextInput event will be raised.  If cancelable is true there will be an additional CANCEL button. Entering text will raise the AfterTextInput event.  The "response" parameter to AfterTextInput will be the text that was entered, or "Cancel" if the CANCEL button was pressed.

## PasswordTextBox  {#PasswordTextBox}

### Properties  {#PasswordTextBox-Properties}

{:.properties}

{:id="PasswordTextBox.BackgroundColor" .color} *BackgroundColor*
: The background color of the input box.  You can choose a color by name in the Designer or in the Blocks Editor.  The default background color is 'default' (shaded 3-D look).

{:id="PasswordTextBox.Enabled" .boolean} *Enabled*
: Whether the user can enter text into the PasswordTextBox.  By default, this is true.

{:id="PasswordTextBox.FontBold" .boolean .do} *FontBold*
: Whether the font for the text should be bold.  By default, it is not.

{:id="PasswordTextBox.FontItalic" .boolean .do} *FontItalic*
: Whether the text should appear in italics.  By default, it does not.

{:id="PasswordTextBox.FontSize" .number} *FontSize*
: The font size for the text.  By default, it is 14.0 points.

{:id="PasswordTextBox.FontTypeface" .number .do} *FontTypeface*
: The font for the text.  The value can be changed in the Designer.

{:id="PasswordTextBox.Height" .number .bo} *Height*
: Specifies the vertical height of the PasswordTextBox, measured in pixels.

{:id="PasswordTextBox.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the PasswordTextBox as a percentage of the height of the Screen.

{:id="PasswordTextBox.Hint" .text} *Hint*
: Text that should appear faintly in the PasswordTextBox to provide a hint as to what the user should enter.  This can only be seen if the <code>Text</code> property is empty.

{:id="PasswordTextBox.PasswordVisible" .boolean .bo} *PasswordVisible*
: Visibility of password.

{:id="PasswordTextBox.Text" .text} *Text*
: Returns the textbox contents.

{:id="PasswordTextBox.TextAlignment" .number .do} *TextAlignment*
: Whether the text should be left justified, centered, or right justified.  By default, text is left justified.

{:id="PasswordTextBox.TextColor" .color} *TextColor*
: The color for the text.  You can choose a color by name in the Designer or in the Blocks Editor.  The default text color is black.

{:id="PasswordTextBox.Visible" .boolean} *Visible*
: Returns true iff the PasswordTextBox is visible.

{:id="PasswordTextBox.Width" .number .bo} *Width*
: Specifies the horizontal width of the PasswordTextBox, measured in pixels.

{:id="PasswordTextBox.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the PasswordTextBox as a percentage of the width of the Screen.

### Events  {#PasswordTextBox-Events}

{:.events}

{:id="PasswordTextBox.GotFocus"} GotFocus()
: Event raised when the PasswordTextBox is selected for input, such as by
 the user touching it.

{:id="PasswordTextBox.LostFocus"} LostFocus()
: Event raised when the PasswordTextBox is no longer selected for input, such
 as if the user touches a different text box.

### Methods  {#PasswordTextBox-Methods}

{:.methods}

{:id="PasswordTextBox.RequestFocus" class="method"} <i/> RequestFocus()
: Sets the PasswordTextBox active.

## Screen  {#Screen}

### Properties  {#Screen-Properties}

{:.properties}

{:id="Screen.AboutScreen" .text} *AboutScreen*
: Information about the screen.  It appears when "About this Application" is selected from the system menu. Use it to inform people about your app.  In multiple screen apps, each screen has its own AboutScreen info.

{:id="Screen.AccentColor" .color .do} *AccentColor*
: This is the accent color used for highlights and other user interface accents.

{:id="Screen.AlignHorizontal" .number} *AlignHorizontal*
: A number that encodes how contents of the screen are aligned  horizontally. The choices are: 1 = left aligned, 2 = horizontally centered,  3 = right aligned.

{:id="Screen.AlignVertical" .number} *AlignVertical*
: A number that encodes how the contents of the arrangement are aligned vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, 3 = aligned at the bottom. Vertical alignment has no effect if the screen is scrollable.

{:id="Screen.AppName" .text .wo .do} *AppName*
: This is the display name of the installed application in the phone.If the AppName is blank, it will be set to the name of the project when the project is built.

{:id="Screen.BackgroundColor" .color} *BackgroundColor*
: BackgroundColor property getter method.

{:id="Screen.BackgroundImage" .text} *BackgroundImage*
: The screen background image.

{:id="Screen.BlocksToolkit" .text .wo .do} *BlocksToolkit*
: A JSON string representing the subset for the screen

{:id="Screen.CloseScreenAnimation" .text} *CloseScreenAnimation*
: The animation for closing current screen and returning  to the previous screen. Valid options are default, fade, zoom, slidehorizontal, slidevertical, and none

{:id="Screen.Height" .number .ro .bo} *Height*
: Screen height (y-size).

{:id="Screen.Icon" .text .wo .do} *Icon*
: Specifies the name of the application icon.

{:id="Screen.OpenScreenAnimation" .text} *OpenScreenAnimation*
: The animation for switching to another screen. Valid options are default, fade, zoom, slidehorizontal, slidevertical, and none

{:id="Screen.PrimaryColor" .color .do} *PrimaryColor*
: This is the primary color used for Material UI elements, such as the ActionBar.

{:id="Screen.PrimaryColorDark" .color .do} *PrimaryColorDark*
: This is the primary color used for darker elements in Material UI.

{:id="Screen.ScreenOrientation" .text} *ScreenOrientation*
: The requested screen orientation, specified as a text value.  Commonly used values are landscape, portrait, sensor, user and unspecified.  See the Android developer documentation for ActivityInfo.Screen_Orientation for the complete list of possible settings.

{:id="Screen.Scrollable" .boolean} *Scrollable*
: When checked, there will be a vertical scrollbar on the screen, and the height of the application can exceed the physical height of the device. When unchecked, the application height is constrained to the height of the device.

{:id="Screen.ShowListsAsJson" .boolean .do} *ShowListsAsJson*
: If false, lists will be converted to strings using Lisp notation, i.e., as symbols separated by spaces, e.g., (a 1 b2 (c d). If true, lists will appear as in Json or Python, e.g.  ["a", 1, "b", 2, ["c", "d"]].  This property appears only in Screen 1, and the value for Screen 1 determines the behavior for all screens. The property defaults to "true" meaning that the App Inventor programmer must explicitly set it to "false" if Lisp syntax is desired. In older versions of App Inventor, this setting defaulted to false. Older projects should not have been affected by this default settings update.

{:id="Screen.ShowStatusBar" .boolean} *ShowStatusBar*
: The status bar is the topmost bar on the screen. This property reports whether the status bar is visible.

{:id="Screen.Sizing" .text .wo .do} *Sizing*
: If set to fixed,  screen layouts will be created for a single fixed-size screen and autoscaled. If set to responsive, screen layouts will use the actual resolution of the device.  See the documentation on responsive design in App Inventor for more information. This property appears on Screen1 only and controls the sizing for all screens in the app.

{:id="Screen.Theme" .text .wo .do} *Theme*
: Sets the theme used by the application.

{:id="Screen.Title" .text} *Title*
: The caption for the form, which apears in the title bar

{:id="Screen.TitleVisible" .boolean} *TitleVisible*
: The title bar is the top gray bar on the screen. This property reports whether the title bar is visible.

{:id="Screen.TutorialURL" .text .wo .do} *TutorialURL*
: A URL to use to populate the Tutorial Sidebar while editing a project. Used as a teaching aid.

{:id="Screen.VersionCode" .number .wo .do} *VersionCode*
: An integer value which must be incremented each time a new Android Application Package File (APK) is created for the Google Play Store.

{:id="Screen.VersionName" .text .wo .do} *VersionName*
: A string which can be changed to allow Google Play Store users to distinguish between different versions of the App.

{:id="Screen.Width" .number .ro .bo} *Width*
: Screen width (x-size).

### Events  {#Screen-Events}

{:.events}

{:id="Screen.BackPressed"} BackPressed()
: Device back button pressed.

{:id="Screen.ErrorOccurred"} ErrorOccurred(*component*{:.component},*functionName*{:.text},*errorNumber*{:.number},*message*{:.text})
: Event raised when an error occurs. Only some errors will raise this condition.  For those errors, the system will show a notification by default.  You can use this event handler to prescribe an error behavior different than the default.

{:id="Screen.Initialize"} Initialize()
: Screen starting

{:id="Screen.OtherScreenClosed"} OtherScreenClosed(*otherScreenName*{:.text},*result*{:.any})
: Event raised when another screen has closed and control has returned to this screen.

{:id="Screen.PermissionDenied"} PermissionDenied(*component*{:.component},*functionName*{:.text},*permissionName*{:.text})
: Event to handle when the app user has denied a needed permission.

{:id="Screen.PermissionGranted"} PermissionGranted(*permissionName*{:.text})
: Event to handle when the app user has granted a needed permission. This event is only run when permission is
 granted in response to the AskForPermission method.

{:id="Screen.ScreenOrientationChanged"} ScreenOrientationChanged()
: Screen orientation changed

### Methods  {#Screen-Methods}

{:.methods}

{:id="Screen.AskForPermission" class="method"} <i/> AskForPermission(*permissionName*{:.text})
: Ask the user to grant access to a dangerous permission.

{:id="Screen.HideKeyboard" class="method"} <i/> HideKeyboard()
: Hide the onscreen soft keyboard.

## Slider  {#Slider}

### Properties  {#Slider-Properties}

{:.properties}

{:id="Slider.ColorLeft" .color} *ColorLeft*
: The color of slider to the left of the thumb.

{:id="Slider.ColorRight" .color} *ColorRight*
: The color of slider to the left of the thumb.

{:id="Slider.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the Slider as a percentage of the height of the Screen.

{:id="Slider.MaxValue" .number} *MaxValue*
: Sets the maximum value of slider.  Changing the maximum value also resets Thumbposition to be halfway between the minimum and the (new) maximum. If the new maximum is less than the current minimum, then minimum and maximum will both be set to this value.  Setting MaxValue resets the thumb position to halfway between MinValue and MaxValue and signals the PositionChanged event.

{:id="Slider.MinValue" .number} *MinValue*
: Sets the minimum value of slider.  Changing the minimum value also resets Thumbposition to be halfway between the (new) minimum and the maximum. If the new minimum is greater than the current maximum, then minimum and maximum will both be set to this value.  Setting MinValue resets the thumb position to halfway between MinValue and MaxValue and signals the PositionChanged event.

{:id="Slider.ThumbEnabled" .boolean} *ThumbEnabled*
: Sets whether or not to display the slider thumb.

{:id="Slider.ThumbPosition" .number} *ThumbPosition*
: Sets the position of the slider thumb. If this value is greater than MaxValue, then it will be set to same value as MaxValue. If this value is less than MinValue, then it will be set to same value as MinValue.

{:id="Slider.Visible" .boolean} *Visible*
: Returns true iff the Slider is visible.

{:id="Slider.Width" .number .bo} *Width*
: Specifies the horizontal width of the Slider, measured in pixels.

{:id="Slider.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the Slider as a percentage of the width of the Screen.

### Events  {#Slider-Events}

{:.events}

{:id="Slider.PositionChanged"} PositionChanged(*thumbPosition*{:.number})
: Indicates that position of the slider thumb has changed.

## Spinner  {#Spinner}

### Properties  {#Spinner-Properties}

{:.properties}

{:id="Spinner.Elements" .list .bo} *Elements*
: returns a list of text elements to be picked from.

{:id="Spinner.ElementsFromString" .text .wo} *ElementsFromString*
: sets the Spinner list to the elements passed in the comma-separated string

{:id="Spinner.Height" .number .bo} *Height*
: Specifies the vertical height of the Spinner, measured in pixels.

{:id="Spinner.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the Spinner as a percentage of the height of the Screen.

{:id="Spinner.Prompt" .text} *Prompt*
: Text with the current title for the Spinner window

{:id="Spinner.Selection" .text} *Selection*
: Returns the current selected item in the spinner

{:id="Spinner.SelectionIndex" .number .bo} *SelectionIndex*
: The index of the currently selected item, starting at 1. If no item is selected, the value will be 0.

{:id="Spinner.Visible" .boolean} *Visible*
: Returns true iff the Spinner is visible.

{:id="Spinner.Width" .number .bo} *Width*
: Specifies the horizontal width of the Spinner, measured in pixels.

{:id="Spinner.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the Spinner as a percentage of the width of the Screen.

### Events  {#Spinner-Events}

{:.events}

{:id="Spinner.AfterSelecting"} AfterSelecting(*selection*{:.text})
: Event called after the user selects an item from the dropdown list.

### Methods  {#Spinner-Methods}

{:.methods}

{:id="Spinner.DisplayDropdown" class="method"} <i/> DisplayDropdown()
: displays the dropdown list for selection, same action as when the user clicks on the spinner.

## Switch  {#Switch}

### Properties  {#Switch-Properties}

{:.properties}

{:id="Switch.BackgroundColor" .color} *BackgroundColor*
: Returns the background color of the Switch as an alpha-red-green-blue
 integer.

{:id="Switch.Enabled" .boolean} *Enabled*
: Returns true if the Switch is active and clickable.

{:id="Switch.FontBold" .boolean .do} *FontBold*
: Returns true if the text of the Switch should be bold.
 If bold has been requested, this property will return true, even if the
 font does not support bold.

{:id="Switch.FontItalic" .boolean .do} *FontItalic*
: Returns true if the text of the Switch should be italic.
 If italic has been requested, this property will return true, even if the
 font does not support italic.

{:id="Switch.FontSize" .number} *FontSize*
: Returns the text font size of the Switch, measured in sp(scale-independent pixels).

{:id="Switch.FontTypeface" .number .do} *FontTypeface*
: Returns the text font face of the Switch as default, serif, sans
 serif, or monospace.

{:id="Switch.Height" .number .bo} *Height*
: Specifies the vertical height of the Switch, measured in pixels.

{:id="Switch.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the Switch as a percentage of the height of the Screen.

{:id="Switch.On" .boolean} *On*
: Returns true if the checkbox is checked.

{:id="Switch.Text" .text} *Text*
: Returns the text displayed by the Switch.

{:id="Switch.TextColor" .color} *TextColor*
: Returns the text color of the Switch as an alpha-red-green-blue
 integer.

{:id="Switch.ThumbColorActive" .color .do} *ThumbColorActive*
: Returns the switch's thumb color (button that toggles back and forth)
 when the switch is ON/Checked

{:id="Switch.ThumbColorInactive" .color} *ThumbColorInactive*
: Returns the switch's thumb color (button that toggles back and forth)
 when the switch is Off/Unchecked

{:id="Switch.TrackColorActive" .color} *TrackColorActive*
: Returns the switch's track color

{:id="Switch.TrackColorInactive" .color} *TrackColorInactive*
: Color of the toggle track when switched off

{:id="Switch.Visible" .boolean} *Visible*
: Returns true iff the Switch is visible.

{:id="Switch.Width" .number .bo} *Width*
: Specifies the horizontal width of the Switch, measured in pixels.

{:id="Switch.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the Switch as a percentage of the width of the Screen.

### Events  {#Switch-Events}

{:.events}

{:id="Switch.Changed"} Changed()
: Default Changed event handler.

{:id="Switch.GotFocus"} GotFocus()
: Default GotFocus event handler.

{:id="Switch.LostFocus"} LostFocus()
: Default LostFocus event handler.

## TextBox  {#TextBox}

### Properties  {#TextBox-Properties}

{:.properties}

{:id="TextBox.BackgroundColor" .color} *BackgroundColor*
: The background color of the input box.  You can choose a color by name in the Designer or in the Blocks Editor.  The default background color is 'default' (shaded 3-D look).

{:id="TextBox.Enabled" .boolean} *Enabled*
: Whether the user can enter text into the TextBox.  By default, this is true.

{:id="TextBox.FontBold" .boolean .do} *FontBold*
: Whether the font for the text should be bold.  By default, it is not.

{:id="TextBox.FontItalic" .boolean .do} *FontItalic*
: Whether the text should appear in italics.  By default, it does not.

{:id="TextBox.FontSize" .number} *FontSize*
: The font size for the text.  By default, it is 14.0 points.

{:id="TextBox.FontTypeface" .number .do} *FontTypeface*
: The font for the text.  The value can be changed in the Designer.

{:id="TextBox.Height" .number .bo} *Height*
: Specifies the vertical height of the TextBox, measured in pixels.

{:id="TextBox.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the TextBox as a percentage of the height of the Screen.

{:id="TextBox.Hint" .text} *Hint*
: Text that should appear faintly in the TextBox to provide a hint as to what the user should enter.  This can only be seen if the <code>Text</code> property is empty.

{:id="TextBox.MultiLine" .boolean} *MultiLine*
: If true, then this text box accepts multiple lines of input, which are entered using the return key.  For single line text boxes there is a Done key instead of a return key, and pressing Done hides the keyboard.  The app should call the HideKeyboard method to hide the keyboard for a mutiline text box.

{:id="TextBox.NumbersOnly" .boolean} *NumbersOnly*
: If true, then this text box accepts only numbers as keyboard input.  Numbers can include a decimal point and an optional leading minus sign.  This applies to keyboard input only.  Even if NumbersOnly is true, you can use [set Text to] to enter any text at all.

{:id="TextBox.ReadOnly" .boolean} *ReadOnly*
: Whether the TextBox is read-only. By default, this is true.

{:id="TextBox.Text" .text} *Text*
: Returns the textbox contents.

{:id="TextBox.TextAlignment" .number .do} *TextAlignment*
: Whether the text should be left justified, centered, or right justified.  By default, text is left justified.

{:id="TextBox.TextColor" .color} *TextColor*
: The color for the text.  You can choose a color by name in the Designer or in the Blocks Editor.  The default text color is black.

{:id="TextBox.Visible" .boolean} *Visible*
: Returns true iff the TextBox is visible.

{:id="TextBox.Width" .number .bo} *Width*
: Specifies the horizontal width of the TextBox, measured in pixels.

{:id="TextBox.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the TextBox as a percentage of the width of the Screen.

### Events  {#TextBox-Events}

{:.events}

{:id="TextBox.GotFocus"} GotFocus()
: Event raised when the TextBox is selected for input, such as by
 the user touching it.

{:id="TextBox.LostFocus"} LostFocus()
: Event raised when the TextBox is no longer selected for input, such
 as if the user touches a different text box.

### Methods  {#TextBox-Methods}

{:.methods}

{:id="TextBox.HideKeyboard" class="method"} <i/> HideKeyboard()
: Hide the keyboard.  Only multiline text boxes need this. Single line text boxes close the keyboard when the users presses the Done key.

{:id="TextBox.RequestFocus" class="method"} <i/> RequestFocus()
: Sets the TextBox active.

## TimePicker  {#TimePicker}

### Properties  {#TimePicker-Properties}

{:.properties}

{:id="TimePicker.BackgroundColor" .color} *BackgroundColor*
: Returns the button's background color

{:id="TimePicker.Enabled" .boolean} *Enabled*
: If set, user can tap TimePicker to cause action.

{:id="TimePicker.FontBold" .boolean} *FontBold*
: If set, TimePicker text is displayed in bold.

{:id="TimePicker.FontItalic" .boolean} *FontItalic*
: If set, TimePicker text is displayed in italics.

{:id="TimePicker.FontSize" .number} *FontSize*
: Point size for TimePicker text.

{:id="TimePicker.FontTypeface" .number .do} *FontTypeface*
: Font family for TimePicker text.

{:id="TimePicker.Height" .number .bo} *Height*
: Specifies the vertical height of the TimePicker, measured in pixels.

{:id="TimePicker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the TimePicker as a percentage of the height of the Screen.

{:id="TimePicker.Hour" .number .ro .bo} *Hour*
: The hour of the last time set using the time picker. The hour is in a 24 hour format. If the last time set was 11:53 pm, this property will return 23.

{:id="TimePicker.Image" .text} *Image*
: Image to display on button.

{:id="TimePicker.Instant" .InstantInTime .ro .bo} *Instant*
: The instant of the last time set using the time picker

{:id="TimePicker.Minute" .number .ro .bo} *Minute*
: The minute of the last time set using the time picker

{:id="TimePicker.Shape" .number .do} *Shape*
: Returns the style of the button.

{:id="TimePicker.ShowFeedback" .boolean} *ShowFeedback*
: Specifies if a visual feedback should be shown  for a TimePicker that as an image as background.

{:id="TimePicker.Text" .text} *Text*
: Text to display on TimePicker.

{:id="TimePicker.TextAlignment" .number .do} *TextAlignment*
: Left, center, or right.

{:id="TimePicker.TextColor" .color} *TextColor*
: Color for button text.

{:id="TimePicker.Visible" .boolean} *Visible*
: Returns true iff the TimePicker is visible.

{:id="TimePicker.Width" .number .bo} *Width*
: Specifies the horizontal width of the TimePicker, measured in pixels.

{:id="TimePicker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the TimePicker as a percentage of the width of the Screen.

### Events  {#TimePicker-Events}

{:.events}

{:id="TimePicker.AfterTimeSet"} AfterTimeSet()
: This event is run when a user has set the time in the popup dialog.

{:id="TimePicker.GotFocus"} GotFocus()
: Indicates the cursor moved over the TimePicker so it is now possible to click it.

{:id="TimePicker.LostFocus"} LostFocus()
: Indicates the cursor moved away from the TimePicker so it is now no longer possible to click it.

{:id="TimePicker.TouchDown"} TouchDown()
: Indicates that the TimePicker was pressed down.

{:id="TimePicker.TouchUp"} TouchUp()
: Indicates that the TimePicker has been released.

### Methods  {#TimePicker-Methods}

{:.methods}

{:id="TimePicker.LaunchPicker" class="method"} <i/> LaunchPicker()
: Launches the TimePicker popup.

{:id="TimePicker.SetTimeToDisplay" class="method"} <i/> SetTimeToDisplay(*hour*{:.number},*minute*{:.number})
: Set the time to be shown in the Time Picker popup. Current time is shown by default.

{:id="TimePicker.SetTimeToDisplayFromInstant" class="method"} <i/> SetTimeToDisplayFromInstant(*instant*{:.InstantInTime})
: Set the time from the instant to be shown in the Time Picker popup. Current time is shown by default.

## WebViewer  {#WebViewer}

### Properties  {#WebViewer-Properties}

{:.properties}

{:id="WebViewer.CurrentPageTitle" .text .ro .bo} *CurrentPageTitle*
: Title of the page currently viewed

{:id="WebViewer.CurrentUrl" .text .ro .bo} *CurrentUrl*
: URL of the page currently viewed.   This could be different from the Home URL if new pages were visited by following links.

{:id="WebViewer.FollowLinks" .boolean} *FollowLinks*
: Determines whether to follow links when they are tapped in the WebViewer.  If you follow links, you can use GoBack and GoForward to navigate the browser history.

{:id="WebViewer.Height" .number .bo} *Height*
: Specifies the vertical height of the WebViewer, measured in pixels.

{:id="WebViewer.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the WebViewer as a percentage of the height of the Screen.

{:id="WebViewer.HomeUrl" .text} *HomeUrl*
: URL of the page the WebViewer should initially open to.  Setting this will load the page.

{:id="WebViewer.IgnoreSslErrors" .boolean} *IgnoreSslErrors*
: Determine whether or not to ignore SSL errors. Set to true to ignore errors. Use this to accept self signed certificates from websites.

{:id="WebViewer.PromptforPermission" .boolean} *PromptforPermission*
: If True, then prompt the user of the WebView to give permission to access the geolocation API. If False, then assume permission is granted.

{:id="WebViewer.UsesLocation" .boolean .wo .do} *UsesLocation*
: Whether or not to give the application permission to use the Javascript geolocation API. This property is available only in the designer.

{:id="WebViewer.Visible" .boolean} *Visible*
: Returns true iff the WebViewer is visible.

{:id="WebViewer.WebViewString" .text .bo} *WebViewString*
: Gets the WebView's String, which is viewable through Javascript in the WebView as the window.AppInventor object

{:id="WebViewer.Width" .number .bo} *Width*
: Specifies the horizontal width of the WebViewer, measured in pixels.

{:id="WebViewer.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the WebViewer as a percentage of the width of the Screen.

### Events  {#WebViewer-Events}

{:.events}

{:id="WebViewer.PageLoaded"} PageLoaded(*url*{:.text})
: When a page is finished loading this event is run.

{:id="WebViewer.WebViewStringChange"} WebViewStringChange(*value*{:.text})
: When the JavaScript calls AppInventor.setWebViewString this event is run.

### Methods  {#WebViewer-Methods}

{:.methods}

{:id="WebViewer.CanGoBack" class="method returns boolean"} <i/> CanGoBack()
: Returns true if the WebViewer can go back in the history list.

{:id="WebViewer.CanGoForward" class="method returns boolean"} <i/> CanGoForward()
: Returns true if the WebViewer can go forward in the history list.

{:id="WebViewer.ClearCaches" class="method"} <i/> ClearCaches()
: Clear WebView caches.

{:id="WebViewer.ClearLocations" class="method"} <i/> ClearLocations()
: Clear stored location permissions.

{:id="WebViewer.GoBack" class="method"} <i/> GoBack()
: Go back to the previous page in the history list.  Does nothing if there is no previous page.

{:id="WebViewer.GoForward" class="method"} <i/> GoForward()
: Go forward to the next page in the history list.   Does nothing if there is no next page.

{:id="WebViewer.GoHome" class="method"} <i/> GoHome()
: Loads the home URL page.  This happens automatically when the home URL is changed.

{:id="WebViewer.GoToUrl" class="method"} <i/> GoToUrl(*url*{:.text})
: Load the page at the given URL.