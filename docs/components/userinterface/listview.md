<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# ListView

This is a visible component that displays a list of text elements.  
The list can be set using the ElementsFromString property or using the Elements block in the blocks editor.

---

## Designer Properties

---

### BackgroundColor

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| BackgroundColor |    color    |   &HFF000000  |

### ElementsFromString

|    Property Name   | Editor Type | Default Value |
| :----------------: | :---------: | :-----------: |
| ElementsFromString |   textArea  |               |

### Selection

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   Selection   |    string   |               |

### SelectionColor

|  Property Name | Editor Type | Default Value |
| :------------: | :---------: | :-----------: |
| SelectionColor |    color    |   &HFFCCCCCC  |

### ShowFilterBar

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| ShowFilterBar |   boolean   |     False     |

### TextColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   TextColor   |    color    |   &HFFFFFFFF  |

### TextSize

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|    TextSize   | non_negative_integer |       22      |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### AfterPicking

<div block-type = "component_event" component-selector = "ListView" event-selector = "AfterPicking" id = "listview-afterpicking"></div>

Simple event to be raised after the an element has been chosen in the list. The selected element is available in the Selection property.

## Block Properties

---

### BackgroundColor

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "BackgroundColor" property-type = "get" id = "get-listview-backgroundcolor"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "BackgroundColor" property-type = "set" id = "set-listview-backgroundcolor"></div>

The color of the listview background.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| BackgroundColor |  number |

### Elements

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Elements" property-type = "get" id = "get-listview-elements"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Elements" property-type = "set" id = "set-listview-elements"></div>

List of text elements to show in the ListView. This will signal an error if the elements are not text strings.

| Param Name | IO Type |
| :--------: | :-----: |
|  Elements  |   list  |

### ElementsFromString

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "ElementsFromString" property-type = "set" id = "set-listview-elementsfromstring"></div>

The TextView elements specified as a string with the items separated by commas such as: Cheese,Fruit,Bacon,Radish. Each word before the comma will be an element in the list.

|     Param Name     | IO Type |
| :----------------: | :-----: |
| ElementsFromString |   text  |

### Height

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Height" property-type = "get" id = "get-listview-height"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Height" property-type = "set" id = "set-listview-height"></div>

Determines the height of the list on the view.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "HeightPercent" property-type = "set" id = "set-listview-heightpercent"></div>

Specifies the vertical height of the ListView as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Selection

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Selection" property-type = "get" id = "get-listview-selection"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Selection" property-type = "set" id = "set-listview-selection"></div>

Returns the text last selected in the ListView.

| Param Name | IO Type |
| :--------: | :-----: |
|  Selection |   text  |

### SelectionColor

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "SelectionColor" property-type = "get" id = "get-listview-selectioncolor"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "SelectionColor" property-type = "set" id = "set-listview-selectioncolor"></div>

The color of the item when it is selected.

|   Param Name   | IO Type |
| :------------: | :-----: |
| SelectionColor |  number |

### SelectionIndex

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "SelectionIndex" property-type = "get" id = "get-listview-selectionindex"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "SelectionIndex" property-type = "set" id = "set-listview-selectionindex"></div>

The index of the currently selected item, starting at 1. If no item is selected, the value will be 0. If an attempt is made to set this to a number less than 1 or greater than the number of items in the ListView, SelectionIndex will be set to 0, and Selection will be set to the empty text.

|   Param Name   | IO Type |
| :------------: | :-----: |
| SelectionIndex |  number |

### ShowFilterBar

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "ShowFilterBar" property-type = "get" id = "get-listview-showfilterbar"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "ShowFilterBar" property-type = "set" id = "set-listview-showfilterbar"></div>

Sets visibility of ShowFilterBar. True will show the bar, False will hide it.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| ShowFilterBar | boolean |

### TextColor

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "TextColor" property-type = "get" id = "get-listview-textcolor"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "TextColor" property-type = "set" id = "set-listview-textcolor"></div>

The text color of the listview items.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextColor |  number |

### TextSize

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "TextSize" property-type = "get" id = "get-listview-textsize"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "TextSize" property-type = "set" id = "set-listview-textsize"></div>

The text size of the listview items.

| Param Name | IO Type |
| :--------: | :-----: |
|  TextSize  |  number |

### Visible

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Visible" property-type = "get" id = "get-listview-visible"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Visible" property-type = "set" id = "set-listview-visible"></div>

Specifies whether the ListView should be visible on the screen. Value is true if the ListView is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Width" property-type = "get" id = "get-listview-width"></div>

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "Width" property-type = "set" id = "set-listview-width"></div>

Determines the width of the list on the view.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "ListView" property-selector = "WidthPercent" property-type = "set" id = "set-listview-widthpercent"></div>

Specifies the horizontal width of the ListView as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### ListView

<div block-type = "component_component_block" component-selector = "ListView" id = "component-listview"></div>

Return Type : component

Component ListView

