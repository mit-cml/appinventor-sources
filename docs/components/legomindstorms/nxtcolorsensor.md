# NxtColorSensor

A component that provides a high-level interface to a color sensor on a LEGO MINDSTORMS NXT robot.

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

### ColorChangedEventEnabled

|       Property Name      | Editor Type | Default Value |
| :----------------------: | :---------: | :-----------: |
| ColorChangedEventEnabled |   boolean   |     False     |

### DetectColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  DetectColor  |   boolean   |      True     |

### GenerateColor

| Property Name |        Editor Type       | Default Value |
| :-----------: | :----------------------: | :-----------: |
| GenerateColor | lego_nxt_generated_color |   &H00FFFFFF  |

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

<div block-type = "component_event" component-selector = "NxtColorSensor" event-selector = "AboveRange" event-params = "" id = "nxtcolorsensor-aboverange"></div>

Light level has gone above the range. The AboveRange event will not occur if the DetectColor property is set to True or if the AboveRangeEventEnabled property is set to False.

### BelowRange

<div block-type = "component_event" component-selector = "NxtColorSensor" event-selector = "BelowRange" event-params = "" id = "nxtcolorsensor-belowrange"></div>

Light level has gone below the range. The BelowRange event will not occur if the DetectColor property is set to True or if the BelowRangeEventEnabled property is set to False.

### ColorChanged

<div block-type = "component_event" component-selector = "NxtColorSensor" event-selector = "ColorChanged" event-params = "color" id = "nxtcolorsensor-colorchanged"></div>

Detected color has changed. The ColorChanged event will not occur if the DetectColor property is set to False or if the ColorChangedEventEnabled property is set to False.

| Param Name | IO Type |
| :--------: | :-----: |
|    color   |  number |

### WithinRange

<div block-type = "component_event" component-selector = "NxtColorSensor" event-selector = "WithinRange" event-params = "" id = "nxtcolorsensor-withinrange"></div>

Light level has gone within the range. The WithinRange event will not occur if the DetectColor property is set to True or if the WithinRangeEventEnabled property is set to False.

## Methods

---

### GetColor

<div block-type = "component_method" component-selector = "NxtColorSensor" method-selector = "GetColor" method-params = "" return-type = "number" id = "nxtcolorsensor-getcolor"></div>

Return Type : number

Returns the current detected color, or the color None if the color can not be read or if the DetectColor property is set to False.

### GetLightLevel

<div block-type = "component_method" component-selector = "NxtColorSensor" method-selector = "GetLightLevel" method-params = "" return-type = "number" id = "nxtcolorsensor-getlightlevel"></div>

Return Type : number

Returns the current light level as a value between 0 and 1023, or -1 if the light level can not be read or if the DetectColor property is set to True.

## Block Properties

---

### AboveRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "AboveRangeEventEnabled" property-type = "get" id = "get-nxtcolorsensor-aboverangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "AboveRangeEventEnabled" property-type = "set" id = "set-nxtcolorsensor-aboverangeeventenabled"></div>

Whether the AboveRange event should fire when the DetectColor property is set to False and the light level goes above the TopOfRange.

|       Param Name       | IO Type |
| :--------------------: | :-----: |
| AboveRangeEventEnabled | boolean |

### BelowRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "BelowRangeEventEnabled" property-type = "get" id = "get-nxtcolorsensor-belowrangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "BelowRangeEventEnabled" property-type = "set" id = "set-nxtcolorsensor-belowrangeeventenabled"></div>

Whether the BelowRange event should fire when the DetectColor property is set to False and the light level goes below the BottomOfRange.

|       Param Name       | IO Type |
| :--------------------: | :-----: |
| BelowRangeEventEnabled | boolean |

### BottomOfRange

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "BottomOfRange" property-type = "get" id = "get-nxtcolorsensor-bottomofrange"></div>

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "BottomOfRange" property-type = "set" id = "set-nxtcolorsensor-bottomofrange"></div>

The bottom of the range used for the BelowRange, WithinRange, and AboveRange events.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| BottomOfRange |  number |

### ColorChangedEventEnabled

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "ColorChangedEventEnabled" property-type = "get" id = "get-nxtcolorsensor-colorchangedeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "ColorChangedEventEnabled" property-type = "set" id = "set-nxtcolorsensor-colorchangedeventenabled"></div>

Whether the ColorChanged event should fire when the DetectColor property is set to True and the detected color changes.

|        Param Name        | IO Type |
| :----------------------: | :-----: |
| ColorChangedEventEnabled | boolean |

### DetectColor

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "DetectColor" property-type = "get" id = "get-nxtcolorsensor-detectcolor"></div>

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "DetectColor" property-type = "set" id = "set-nxtcolorsensor-detectcolor"></div>

Whether the sensor should detect color or light. True indicates that the sensor should detect color; False indicates that the sensor should detect light. If the DetectColor property is set to True, the BelowRange, WithinRange, and AboveRange events will not occur and the sensor will not generate color. If the DetectColor property is set to False, the ColorChanged event will not occur.

|  Param Name | IO Type |
| :---------: | :-----: |
| DetectColor | boolean |

### GenerateColor

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "GenerateColor" property-type = "get" id = "get-nxtcolorsensor-generatecolor"></div>

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "GenerateColor" property-type = "set" id = "set-nxtcolorsensor-generatecolor"></div>

The color that should generated by the sensor. Only None, Red, Green, or Blue are valid values. The sensor will not generate color when the DetectColor property is set to True.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| GenerateColor |  number |

### TopOfRange

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "TopOfRange" property-type = "get" id = "get-nxtcolorsensor-topofrange"></div>

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "TopOfRange" property-type = "set" id = "set-nxtcolorsensor-topofrange"></div>

The top of the range used for the BelowRange, WithinRange, and AboveRange events.

| Param Name | IO Type |
| :--------: | :-----: |
| TopOfRange |  number |

### WithinRangeEventEnabled

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "WithinRangeEventEnabled" property-type = "get" id = "get-nxtcolorsensor-withinrangeeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtColorSensor" property-selector = "WithinRangeEventEnabled" property-type = "set" id = "set-nxtcolorsensor-withinrangeeventenabled"></div>

Whether the WithinRange event should fire when the DetectColor property is set to False and the light level goes between the BottomOfRange and the TopOfRange.

|        Param Name       | IO Type |
| :---------------------: | :-----: |
| WithinRangeEventEnabled | boolean |

## Component

---

### NxtColorSensor

<div block-type = "component_component_block" component-selector = "NxtColorSensor" id = "component-nxtcolorsensor"></div>

Return Type : component

Component NxtColorSensor

