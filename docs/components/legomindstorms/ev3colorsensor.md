<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Ev3ColorSensor

A component that provides a high-level interface to a color sensor on a LEGO MINDSTORMS EV3 robot.

---

## Designer Properties

---

### AboveRangeEventEnabled

|      Property Name     | Editor Type | Default Value |
| :--------------------: | :---------: | :-----------: |
| AboveRangeEventEnabled |   boolean   |     False     |

### BelowRangeEventEnabled

|      Property Name     | Editor Type | Default Value |
| :--------------------: | :---------: | :-----------: |
| BelowRangeEventEnabled |   boolean   |     False     |

### BluetoothClient

|  Property Name  |   Editor Type   | Default Value |
| :-------------: | :-------------: | :-----------: |
| BluetoothClient | BluetoothClient |               |

### BottomOfRange

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
| BottomOfRange | non_negative_integer |       30      |

### ColorChangedEventEnabled

|       Property Name      | Editor Type | Default Value |
| :----------------------: | :---------: | :-----------: |
| ColorChangedEventEnabled |   boolean   |     False     |

### Mode

| Property Name |         Editor Type        | Default Value |
| :-----------: | :------------------------: | :-----------: |
|      Mode     | lego_ev3_color_sensor_mode |   reflected   |

### SensorPort

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|   SensorPort  | lego_ev3_sensor_port |       1       |

### TopOfRange

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|   TopOfRange  | non_negative_integer |       60      |

### WithinRangeEventEnabled

|      Property Name      | Editor Type | Default Value |
| :---------------------: | :---------: | :-----------: |
| WithinRangeEventEnabled |   boolean   |     False     |

## Events

---

### AboveRange

<div block-type = "component_event" component-selector = "Ev3ColorSensor" event-selector = "AboveRange" id = "ev3colorsensor-aboverange"></div>

Light level has gone above the range.

### BelowRange

<div block-type = "component_event" component-selector = "Ev3ColorSensor" event-selector = "BelowRange" id = "ev3colorsensor-belowrange"></div>

Light level has gone below the range.

### ColorChanged

<div block-type = "component_event" component-selector = "Ev3ColorSensor" event-selector = "ColorChanged" id = "ev3colorsensor-colorchanged"></div>

Called when the detected color has changed. The ColorChanged event will occur if the Mode property is set to "color" and the ColorChangedEventEnabled property is set to True.

| Param Name | IO Type |
| :--------: | :-----: |
|  colorCode |  number |
|  colorName |   text  |

### WithinRange

<div block-type = "component_event" component-selector = "Ev3ColorSensor" event-selector = "WithinRange" id = "ev3colorsensor-withinrange"></div>

Light level has gone within the range.

## Methods

---

### GetColorCode

<div block-type = "component_method" component-selector = "Ev3ColorSensor" method-selector = "GetColorCode" id = "ev3colorsensor-getcolorcode"></div>

Return Type : number

It returns the color code from 0 to 7 corresponding to no color, black, blue, green, yellow, red, white and brown.

### GetColorName

<div block-type = "component_method" component-selector = "Ev3ColorSensor" method-selector = "GetColorName" id = "ev3colorsensor-getcolorname"></div>

Return Type : text

Return the color name in one of "No Color", "Black", "Blue", "Green", "Yellow", "Red", "White", "Brown".

### GetLightLevel

<div block-type = "component_method" component-selector = "Ev3ColorSensor" method-selector = "GetLightLevel" id = "ev3colorsensor-getlightlevel"></div>

Return Type : number

It returns the light level in percentage, or -1 when the light level cannot be read.

## Block Properties

---

### AboveRangeEventEnabled

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "AboveRangeEventEnabled" property-type = "get" id = "get-ev3colorsensor-aboverangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "AboveRangeEventEnabled" property-type = "set" id = "set-ev3colorsensor-aboverangeeventenabled"></div>

Whether the AboveRange event should fire when the light level goes above the TopOfRange.

|       Param Name       | IO Type |
| :--------------------: | :-----: |
| AboveRangeEventEnabled | boolean |

### BelowRangeEventEnabled

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "BelowRangeEventEnabled" property-type = "get" id = "get-ev3colorsensor-belowrangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "BelowRangeEventEnabled" property-type = "set" id = "set-ev3colorsensor-belowrangeeventenabled"></div>

Whether the BelowRange event should fire when the light level goes below the BottomOfRange.

|       Param Name       | IO Type |
| :--------------------: | :-----: |
| BelowRangeEventEnabled | boolean |

### BluetoothClient

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "BluetoothClient" property-type = "get" id = "get-ev3colorsensor-bluetoothclient"></div>

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "BluetoothClient" property-type = "set" id = "set-ev3colorsensor-bluetoothclient"></div>

The BluetoothClient component that should be used for communication.

|    Param Name   |  IO Type  |
| :-------------: | :-------: |
| BluetoothClient | component |

### BottomOfRange

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "BottomOfRange" property-type = "get" id = "get-ev3colorsensor-bottomofrange"></div>

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "BottomOfRange" property-type = "set" id = "set-ev3colorsensor-bottomofrange"></div>

The bottom of the range used for the BelowRange, WithinRange, and AboveRange events.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| BottomOfRange |  number |

### ColorChangedEventEnabled

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "ColorChangedEventEnabled" property-type = "get" id = "get-ev3colorsensor-colorchangedeventenabled"></div>

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "ColorChangedEventEnabled" property-type = "set" id = "set-ev3colorsensor-colorchangedeventenabled"></div>

Whether the ColorChanged event should fire when the Mode property is set to "color" and the detected color changes.

|        Param Name        | IO Type |
| :----------------------: | :-----: |
| ColorChangedEventEnabled | boolean |

### Mode

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "Mode" property-type = "get" id = "get-ev3colorsensor-mode"></div>

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "Mode" property-type = "set" id = "set-ev3colorsensor-mode"></div>

Specifies the mode of the sensor.

| Param Name | IO Type |
| :--------: | :-----: |
|    Mode    |   text  |

### TopOfRange

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "TopOfRange" property-type = "get" id = "get-ev3colorsensor-topofrange"></div>

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "TopOfRange" property-type = "set" id = "set-ev3colorsensor-topofrange"></div>

The top of the range used for the BelowRange, WithinRange, and AboveRange events.

| Param Name | IO Type |
| :--------: | :-----: |
| TopOfRange |  number |

### WithinRangeEventEnabled

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "WithinRangeEventEnabled" property-type = "get" id = "get-ev3colorsensor-withinrangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "Ev3ColorSensor" property-selector = "WithinRangeEventEnabled" property-type = "set" id = "set-ev3colorsensor-withinrangeeventenabled"></div>

Whether the WithinRange event should fire when the light level goes between the BottomOfRange and the TopOfRange.

|        Param Name       | IO Type |
| :---------------------: | :-----: |
| WithinRangeEventEnabled | boolean |

## Component

---

### Ev3ColorSensor

<div block-type = "component_component_block" component-selector = "Ev3ColorSensor" id = "component-ev3colorsensor"></div>

Return Type : component

Component Ev3ColorSensor

