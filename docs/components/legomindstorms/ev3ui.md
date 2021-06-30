<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Ev3UI

A component that provides a high-level interface to a LEGO MINDSTORMS EV3 robot, with functions to draw graphs on EV3 screen.

---

## Designer Properties

---

### BluetoothClient

|  Property Name  |   Editor Type   | Default Value |
| :-------------: | :-------------: | :-----------: |
| BluetoothClient | BluetoothClient |               |

## Methods

---

### DrawCircle

<div block-type = "component_method" component-selector = "Ev3UI" method-selector = "DrawCircle" id = "ev3ui-drawcircle"></div>

Return Type : No Return Value

Draw a circle on the screen.

| Param Name | Input Type |
| :--------: | :--------: |
|    color   |   number   |
|      x     |   number   |
|      y     |   number   |
|   radius   |   number   |
|    fill    |   boolean  |

### DrawIcon

<div block-type = "component_method" component-selector = "Ev3UI" method-selector = "DrawIcon" id = "ev3ui-drawicon"></div>

Return Type : No Return Value

Draw a built-in icon on screen.

| Param Name | Input Type |
| :--------: | :--------: |
|    color   |   number   |
|      x     |   number   |
|      y     |   number   |
|    type    |   number   |
|     no     |   number   |

### DrawLine

<div block-type = "component_method" component-selector = "Ev3UI" method-selector = "DrawLine" id = "ev3ui-drawline"></div>

Return Type : No Return Value

Draw a line on the screen.

| Param Name | Input Type |
| :--------: | :--------: |
|    color   |   number   |
|     x1     |   number   |
|     y1     |   number   |
|     x2     |   number   |
|     y2     |   number   |

### DrawPoint

<div block-type = "component_method" component-selector = "Ev3UI" method-selector = "DrawPoint" id = "ev3ui-drawpoint"></div>

Return Type : No Return Value

Draw a point on the screen.

| Param Name | Input Type |
| :--------: | :--------: |
|    color   |   number   |
|      x     |   number   |
|      y     |   number   |

### DrawRect

<div block-type = "component_method" component-selector = "Ev3UI" method-selector = "DrawRect" id = "ev3ui-drawrect"></div>

Return Type : No Return Value

Draw a rectangle on the screen.

| Param Name | Input Type |
| :--------: | :--------: |
|    color   |   number   |
|      x     |   number   |
|      y     |   number   |
|    width   |   number   |
|   height   |   number   |
|    fill    |   boolean  |

### FillScreen

<div block-type = "component_method" component-selector = "Ev3UI" method-selector = "FillScreen" id = "ev3ui-fillscreen"></div>

Return Type : No Return Value

Fill the screen with a color.

| Param Name | Input Type |
| :--------: | :--------: |
|    color   |   number   |

## Block Properties

---

### BluetoothClient

<div block-type = "component_set_get" component-selector = "Ev3UI" property-selector = "BluetoothClient" property-type = "get" id = "get-ev3ui-bluetoothclient"></div>

<div block-type = "component_set_get" component-selector = "Ev3UI" property-selector = "BluetoothClient" property-type = "set" id = "set-ev3ui-bluetoothclient"></div>

The BluetoothClient component that should be used for communication.

|    Param Name   |  IO Type  |
| :-------------: | :-------: |
| BluetoothClient | component |

## Component

---

### Ev3UI

<div block-type = "component_component_block" component-selector = "Ev3UI" id = "component-ev3ui"></div>

Return Type : component

Component Ev3UI

