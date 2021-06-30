<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Spinner

A spinner component that displays a pop-up with a list of elements. These elements can be set in the Designer or Blocks Editor by setting the`ElementsFromString` property to a string-separated concatenation (for example, _choice 1, choice 2, choice 3_) or by setting the `Elements` property to a List in the Blocks editor. Spinners are created with the first item already selected. So selecting it does not generate an After Picking event. Consequently it's useful to make the first Spinner item be a non-choice like "Select from below...".

---

## Designer Properties

---

### ElementsFromString

|    Property Name   | Editor Type | Default Value |
| :----------------: | :---------: | :-----------: |
| ElementsFromString |   textArea  |               |

### Prompt

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Prompt    |    string   |               |

### Selection

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   Selection   |    string   |               |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### AfterSelecting

<div block-type = "component_event" component-selector = "Spinner" event-selector = "AfterSelecting" id = "spinner-afterselecting"></div>

Event called after the user selects an item from the dropdown list.

| Param Name | IO Type |
| :--------: | :-----: |
|  selection |   text  |

## Methods

---

### DisplayDropdown

<div block-type = "component_method" component-selector = "Spinner" method-selector = "DisplayDropdown" id = "spinner-displaydropdown"></div>

Return Type : No Return Value

Displays the dropdown list for selection, same action as when the user clicks on the spinner.

## Block Properties

---

### Elements

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Elements" property-type = "get" id = "get-spinner-elements"></div>

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Elements" property-type = "set" id = "set-spinner-elements"></div>

returns a list of text elements to be picked from.

| Param Name | IO Type |
| :--------: | :-----: |
|  Elements  |   list  |

### ElementsFromString

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "ElementsFromString" property-type = "set" id = "set-spinner-elementsfromstring"></div>

Sets the Spinner list to the elements passed in the comma-separated string

|     Param Name     | IO Type |
| :----------------: | :-----: |
| ElementsFromString |   text  |

### Height

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Height" property-type = "get" id = "get-spinner-height"></div>

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Height" property-type = "set" id = "set-spinner-height"></div>

Specifies the vertical height of the Spinner, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "HeightPercent" property-type = "set" id = "set-spinner-heightpercent"></div>

Specifies the vertical height of the Spinner as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Prompt

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Prompt" property-type = "get" id = "get-spinner-prompt"></div>

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Prompt" property-type = "set" id = "set-spinner-prompt"></div>

Text with the current title for the Spinner window

| Param Name | IO Type |
| :--------: | :-----: |
|   Prompt   |   text  |

### Selection

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Selection" property-type = "get" id = "get-spinner-selection"></div>

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Selection" property-type = "set" id = "set-spinner-selection"></div>

Returns the current selected item in the spinner

| Param Name | IO Type |
| :--------: | :-----: |
|  Selection |   text  |

### SelectionIndex

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "SelectionIndex" property-type = "get" id = "get-spinner-selectionindex"></div>

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "SelectionIndex" property-type = "set" id = "set-spinner-selectionindex"></div>

The index of the currently selected item, starting at 1. If no item is selected, the value will be 0.

|   Param Name   | IO Type |
| :------------: | :-----: |
| SelectionIndex |  number |

### Visible

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Visible" property-type = "get" id = "get-spinner-visible"></div>

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Visible" property-type = "set" id = "set-spinner-visible"></div>

Specifies whether the Spinner should be visible on the screen. Value is true if the Spinner is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Width" property-type = "get" id = "get-spinner-width"></div>

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "Width" property-type = "set" id = "set-spinner-width"></div>

Specifies the horizontal width of the Spinner, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "Spinner" property-selector = "WidthPercent" property-type = "set" id = "set-spinner-widthpercent"></div>

Specifies the horizontal width of the Spinner as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### Spinner

<div block-type = "component_component_block" component-selector = "Spinner" id = "component-spinner"></div>

Return Type : component

Component Spinner

