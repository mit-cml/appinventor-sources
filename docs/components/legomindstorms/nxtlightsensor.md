<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# NxtLightSensor

A component that provides a high-level interface to a light sensor on a LEGO MINDSTORMS NXT robot.

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
| BottomOfRange | non_negative_integer |      256      |

### GenerateLight

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| GenerateLight |   boolean   |     False     |

### SensorPort

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|   SensorPort  | lego_nxt_sensor_port |       3       |

### TopOfRange

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|   TopOfRange  | non_negative_integer |      767      |

### WithinRangeEventEnabled

|      Property Name      | Editor Type | Default Value |
| :---------------------: | :---------: | :-----------: |
| WithinRangeEventEnabled |   boolean   |     False     |

## Events

---

### AboveRange

<div block-type = "component_event" component-selector = "NxtLightSensor" event-selector = "AboveRange" id = "nxtlightsensor-aboverange"></div>

Light level has gone above the range.

### BelowRange

<div block-type = "component_event" component-selector = "NxtLightSensor" event-selector = "BelowRange" id = "nxtlightsensor-belowrange"></div>

Light level has gone below the range.

### WithinRange

<div block-type = "component_event" component-selector = "NxtLightSensor" event-selector = "WithinRange" id = "nxtlightsensor-withinrange"></div>

Light level has gone within the range.

## Methods

---

### GetLightLevel

<div block-type = "component_method" component-selector = "NxtLightSensor" method-selector = "GetLightLevel" id = "nxtlightsensor-getlightlevel"></div>

Return Type : number

Returns the current light level as a value between 0 and 1023, or -1 if the light level can not be read.

## Block Properties

---

### AboveRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "AboveRangeEventEnabled" property-type = "get" id = "get-nxtlightsensor-aboverangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "AboveRangeEventEnabled" property-type = "set" id = "set-nxtlightsensor-aboverangeeventenabled"></div>

Whether the AboveRange event should fire when the light level goes above the TopOfRange.

|       Param Name       | IO Type |
| :--------------------: | :-----: |
| AboveRangeEventEnabled | boolean |

### BelowRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "BelowRangeEventEnabled" property-type = "get" id = "get-nxtlightsensor-belowrangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "BelowRangeEventEnabled" property-type = "set" id = "set-nxtlightsensor-belowrangeeventenabled"></div>

Whether the BelowRange event should fire when the light level goes below the BottomOfRange.

|       Param Name       | IO Type |
| :--------------------: | :-----: |
| BelowRangeEventEnabled | boolean |

### BottomOfRange

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "BottomOfRange" property-type = "get" id = "get-nxtlightsensor-bottomofrange"></div>

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "BottomOfRange" property-type = "set" id = "set-nxtlightsensor-bottomofrange"></div>

The bottom of the range used for the BelowRange, WithinRange, and AboveRange events.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| BottomOfRange |  number |

### GenerateLight

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "GenerateLight" property-type = "get" id = "get-nxtlightsensor-generatelight"></div>

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "GenerateLight" property-type = "set" id = "set-nxtlightsensor-generatelight"></div>

Whether the light sensor should generate light.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| GenerateLight | boolean |

### TopOfRange

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "TopOfRange" property-type = "get" id = "get-nxtlightsensor-topofrange"></div>

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "TopOfRange" property-type = "set" id = "set-nxtlightsensor-topofrange"></div>

The top of the range used for the BelowRange, WithinRange, and AboveRange events.

| Param Name | IO Type |
| :--------: | :-----: |
| TopOfRange |  number |

### WithinRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "WithinRangeEventEnabled" property-type = "get" id = "get-nxtlightsensor-withinrangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtLightSensor" property-selector = "WithinRangeEventEnabled" property-type = "set" id = "set-nxtlightsensor-withinrangeeventenabled"></div>

Whether the WithinRange event should fire when the light level goes between the BottomOfRange and the TopOfRange.

|        Param Name       | IO Type |
| :---------------------: | :-----: |
| WithinRangeEventEnabled | boolean |

## Component

---

### NxtLightSensor

<div block-type = "component_component_block" component-selector = "NxtLightSensor" id = "component-nxtlightsensor"></div>

Return Type : component

Component NxtLightSensor

