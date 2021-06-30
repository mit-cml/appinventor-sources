<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# ListPicker

A button that, when clicked on, displays a list of texts for the user to choose among. The texts can be specified through the Designer or Blocks Editor by setting the `ElementsFromString` property to their string-separated concatenation (for example, _choice 1, choice 2, choice 3_) or by setting the `Elements` property to a List in the Blocks editor.

Setting property ShowFilterBar to true, will make the list searchable. Other properties affect the appearance of the button (`TextAlignment`, `BackgroundColor`, etc.) and whether it can be clicked on (`Enabled`).

---

## Designer Properties

---

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &H00000000  |

### ElementsFromString

|    Property Name   | Editor Type | Default Value |
| :----------------: | :---------: | :-----------: |
| ElementsFromString |   textArea  |               |

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

### ItemBackgroundColor

|    Property Name    | Editor Type | Default Value |
| :-----------------: | :---------: | :-----------: |
| ItemBackgroundColor |    color    |   &HFF000000  |

### ItemTextColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| ItemTextColor |    color    |   &HFFFFFFFF  |

### Selection

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   Selection   |    string   |               |

### Shape

| Property Name |  Editor Type | Default Value |
| :-----------: | :----------: | :-----------: |
|     Shape     | button_shape |       0       |

### ShowFeedback

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  ShowFeedback |   boolean   |      True     |

### ShowFilterBar

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| ShowFilterBar |   boolean   |     False     |

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

### Title

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Title     |    string   |               |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### AfterPicking

<div block-type = "component_event" component-selector = "ListPicker" event-selector = "AfterPicking" id = "listpicker-afterpicking"></div>

Event to be raised after the \`ListPicker\` activity returns its result and the properties have been filled in.

### BeforePicking

<div block-type = "component_event" component-selector = "ListPicker" event-selector = "BeforePicking" id = "listpicker-beforepicking"></div>

Event to raise when the \`ListPicker\` is clicked or the picker is shown using the

### GotFocus

<div block-type = "component_event" component-selector = "ListPicker" event-selector = "GotFocus" id = "listpicker-gotfocus"></div>

Indicates the cursor moved over the ListPicker so it is now possible to click it.

### LostFocus

<div block-type = "component_event" component-selector = "ListPicker" event-selector = "LostFocus" id = "listpicker-lostfocus"></div>

Indicates the cursor moved away from the ListPicker so it is now no longer possible to click it.

### TouchDown

<div block-type = "component_event" component-selector = "ListPicker" event-selector = "TouchDown" id = "listpicker-touchdown"></div>

Indicates that the ListPicker was pressed down.

### TouchUp

<div block-type = "component_event" component-selector = "ListPicker" event-selector = "TouchUp" id = "listpicker-touchup"></div>

Indicates that the ListPicker has been released.

## Methods

---

### Open

<div block-type = "component_method" component-selector = "ListPicker" method-selector = "Open" id = "listpicker-open"></div>

Return Type : No Return Value

Opens the ListPicker, as though the user clicked on it.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "BackgroundColor" property-type = "get" id = "get-listpicker-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "BackgroundColor" property-type = "set" id = "set-listpicker-backgroundcolor"></div>

Returns the button's background color

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Elements

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Elements" property-type = "get" id = "get-listpicker-elements"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Elements" property-type = "set" id = "set-listpicker-elements"></div>

Specifies the list of choices to display.

| Param Name | IO Type |
| :--------: | :-----: |
|  Elements  |   list  |

### ElementsFromString

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ElementsFromString" property-type = "set" id = "set-listpicker-elementsfromstring"></div>

Set the list of choices from a string of comma-separated values.

|     Param Name     | IO Type |
| :----------------: | :-----: |
| ElementsFromString |   text  |

### Enabled

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Enabled" property-type = "get" id = "get-listpicker-enabled"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Enabled" property-type = "set" id = "set-listpicker-enabled"></div>

If set, user can tap ListPicker to cause action.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### FontBold

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "FontBold" property-type = "get" id = "get-listpicker-fontbold"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "FontBold" property-type = "set" id = "set-listpicker-fontbold"></div>

If set, ListPicker text is displayed in bold.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontBold  | boolean |

### FontItalic

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "FontItalic" property-type = "get" id = "get-listpicker-fontitalic"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "FontItalic" property-type = "set" id = "set-listpicker-fontitalic"></div>

If set, ListPicker text is displayed in italics.

| Param Name | IO Type |
| :--------: | :-----: |
| FontItalic | boolean |

### FontSize

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "FontSize" property-type = "get" id = "get-listpicker-fontsize"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "FontSize" property-type = "set" id = "set-listpicker-fontsize"></div>

Point size for ListPicker text.

| Param Name | IO Type |
| :--------: | :-----: |
|  FontSize  |  number |

### Height

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Height" property-type = "get" id = "get-listpicker-height"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Height" property-type = "set" id = "set-listpicker-height"></div>

Specifies the vertical height of the ListPicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "HeightPercent" property-type = "set" id = "set-listpicker-heightpercent"></div>

Specifies the vertical height of the ListPicker as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Image

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Image" property-type = "get" id = "get-listpicker-image"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Image" property-type = "set" id = "set-listpicker-image"></div>

Image to display on button.

| Param Name | IO Type |
| :--------: | :-----: |
|    Image   |   text  |

### ItemBackgroundColor

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ItemBackgroundColor" property-type = "get" id = "get-listpicker-itembackgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ItemBackgroundColor" property-type = "set" id = "set-listpicker-itembackgroundcolor"></div>

The background color of the ListPicker items.

|      Param Name     | IO Type |
| :-----------------: | :-----: |
| ItemBackgroundColor |  number |

### ItemTextColor

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ItemTextColor" property-type = "get" id = "get-listpicker-itemtextcolor"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ItemTextColor" property-type = "set" id = "set-listpicker-itemtextcolor"></div>

The text color of the ListPicker items.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| ItemTextColor |  number |

### Selection

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Selection" property-type = "get" id = "get-listpicker-selection"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Selection" property-type = "set" id = "set-listpicker-selection"></div>

The selected item. When directly changed by the programmer, the SelectionIndex property is also changed to the first item in the ListPicker with the given value. If the value does not appear, SelectionIndex will be set to 0.

| Param Name | IO Type |
| :--------: | :-----: |
|  Selection |   text  |

### SelectionIndex

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "SelectionIndex" property-type = "get" id = "get-listpicker-selectionindex"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "SelectionIndex" property-type = "set" id = "set-listpicker-selectionindex"></div>

The index of the currently selected item, starting at 1. If no item is selected, the value will be 0. If an attempt is made to set this to a number less than 1 or greater than the number of items in the ListPicker, SelectionIndex will be set to 0, and Selection will be set to the empty text.

|   Param Name   | IO Type |
| :------------: | :-----: |
| SelectionIndex |  number |

### ShowFeedback

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ShowFeedback" property-type = "get" id = "get-listpicker-showfeedback"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ShowFeedback" property-type = "set" id = "set-listpicker-showfeedback"></div>

Specifies if a visual feedback should be shown for a ListPicker that has an image as background.

|  Param Name  | IO Type |
| :----------: | :-----: |
| ShowFeedback | boolean |

### ShowFilterBar

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ShowFilterBar" property-type = "get" id = "get-listpicker-showfilterbar"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "ShowFilterBar" property-type = "set" id = "set-listpicker-showfilterbar"></div>

Returns current state of ShowFilterBar indicating if Search Filter Bar will be displayed on ListPicker or not

|   Param Name  | IO Type |
| :-----------: | :-----: |
| ShowFilterBar | boolean |

### Text

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Text" property-type = "get" id = "get-listpicker-text"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Text" property-type = "set" id = "set-listpicker-text"></div>

Text to display on ListPicker.

| Param Name | IO Type |
| :--------: | :-----: |
|    Text    |   text  |

### TextColor

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "TextColor" property-type = "get" id = "get-listpicker-textcolor"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "TextColor" property-type = "set" id = "set-listpicker-textcolor"></div>

Color for button text.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### Title

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Title" property-type = "get" id = "get-listpicker-title"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Title" property-type = "set" id = "set-listpicker-title"></div>

Optional title displayed at the top of the list of choices.

| Param Name | IO Type |
| :--------: | :-----: |
|    Title   |   text  |

### Visible

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Visible" property-type = "get" id = "get-listpicker-visible"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Visible" property-type = "set" id = "set-listpicker-visible"></div>

Specifies whether the ListPicker should be visible on the screen. Value is true if the ListPicker is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Width" property-type = "get" id = "get-listpicker-width"></div>

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "Width" property-type = "set" id = "set-listpicker-width"></div>

Specifies the horizontal width of the ListPicker, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "ListPicker" property-selector = "WidthPercent" property-type = "set" id = "set-listpicker-widthpercent"></div>

Specifies the horizontal width of the ListPicker as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### ListPicker

<div block-type = "component_component_block" component-selector = "ListPicker" id = "component-listpicker"></div>

Return Type : component

Component ListPicker

