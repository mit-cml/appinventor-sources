<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# NxtSoundSensor

A component that provides a high-level interface to a sound sensor on a LEGO MINDSTORMS NXT robot.

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

### SensorPort

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|   SensorPort  | lego_nxt_sensor_port |       2       |

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

<div block-type = "component_event" component-selector = "NxtSoundSensor" event-selector = "AboveRange" id = "nxtsoundsensor-aboverange"></div>

Sound level has gone above the range.

### BelowRange

<div block-type = "component_event" component-selector = "NxtSoundSensor" event-selector = "BelowRange" id = "nxtsoundsensor-belowrange"></div>

Sound level has gone below the range.

### WithinRange

<div block-type = "component_event" component-selector = "NxtSoundSensor" event-selector = "WithinRange" id = "nxtsoundsensor-withinrange"></div>

Sound level has gone within the range.

## Methods

---

### GetSoundLevel

<div block-type = "component_method" component-selector = "NxtSoundSensor" method-selector = "GetSoundLevel" id = "nxtsoundsensor-getsoundlevel"></div>

Return Type : number

Returns the current sound level as a value between 0 and 1023, or -1 if the sound level can not be read.

## Block Properties

---

### AboveRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "AboveRangeEventEnabled" property-type = "get" id = "get-nxtsoundsensor-aboverangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "AboveRangeEventEnabled" property-type = "set" id = "set-nxtsoundsensor-aboverangeeventenabled"></div>

Whether the AboveRange event should fire when the sound level goes above the TopOfRange.

|       Param Name       | IO Type |
| :--------------------: | :-----: |
| AboveRangeEventEnabled | boolean |

### BelowRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "BelowRangeEventEnabled" property-type = "get" id = "get-nxtsoundsensor-belowrangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "BelowRangeEventEnabled" property-type = "set" id = "set-nxtsoundsensor-belowrangeeventenabled"></div>

Whether the BelowRange event should fire when the sound level goes below the BottomOfRange.

|       Param Name       | IO Type |
| :--------------------: | :-----: |
| BelowRangeEventEnabled | boolean |

### BottomOfRange

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "BottomOfRange" property-type = "get" id = "get-nxtsoundsensor-bottomofrange"></div>

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "BottomOfRange" property-type = "set" id = "set-nxtsoundsensor-bottomofrange"></div>

The bottom of the range used for the BelowRange, WithinRange, and AboveRange events.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| BottomOfRange |  number |

### TopOfRange

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "TopOfRange" property-type = "get" id = "get-nxtsoundsensor-topofrange"></div>

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "TopOfRange" property-type = "set" id = "set-nxtsoundsensor-topofrange"></div>

The top of the range used for the BelowRange, WithinRange, and AboveRange events.

| Param Name | IO Type |
| :--------: | :-----: |
| TopOfRange |  number |

### WithinRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "WithinRangeEventEnabled" property-type = "get" id = "get-nxtsoundsensor-withinrangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtSoundSensor" property-selector = "WithinRangeEventEnabled" property-type = "set" id = "set-nxtsoundsensor-withinrangeeventenabled"></div>

Whether the WithinRange event should fire when the sound level goes between the BottomOfRange and the TopOfRange.

|        Param Name       | IO Type |
| :---------------------: | :-----: |
| WithinRangeEventEnabled | boolean |

## Component

---

### NxtSoundSensor

<div block-type = "component_component_block" component-selector = "NxtSoundSensor" id = "component-nxtsoundsensor"></div>

Return Type : component

Component NxtSoundSensor

