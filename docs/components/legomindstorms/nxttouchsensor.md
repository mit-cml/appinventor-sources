# NxtTouchSensor

A component that provides a high-level interface to a touch sensor on a LEGO MINDSTORMS NXT robot.

---

## Designer Properties

---

### BluetoothClient

|  Property Name  |   Editor Type   | Default Value |
| :-------------: | :-------------: | :-----------: |
| BluetoothClient | BluetoothClient |               |

### PressedEventEnabled

|    Property Name    | Editor Type | Default Value |
| :-----------------: | :---------: | :-----------: |
| PressedEventEnabled |   boolean   |     False     |

### ReleasedEventEnabled

|     Property Name    | Editor Type | Default Value |
| :------------------: | :---------: | :-----------: |
| ReleasedEventEnabled |   boolean   |     False     |

### SensorPort

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|   SensorPort  | lego_nxt_sensor_port |       1       |

## Events

---

### Pressed

<div block-type = "component_event" component-selector = "NxtTouchSensor" event-selector = "Pressed" event-params = "" id = "nxttouchsensor-pressed"></div>

Touch sensor has been pressed.

### Released

<div block-type = "component_event" component-selector = "NxtTouchSensor" event-selector = "Released" event-params = "" id = "nxttouchsensor-released"></div>

Touch sensor has been released.

## Methods

---

### IsPressed

<div block-type = "component_method" component-selector = "NxtTouchSensor" method-selector = "IsPressed" method-params = "" return-type = "boolean" id = "nxttouchsensor-ispressed"></div>

Return Type : boolean

Returns true if the touch sensor is pressed.

## Block Properties

---

### PressedEventEnabled

<div block-type = "component_set_get" component-selector = "NxtTouchSensor" property-selector = "PressedEventEnabled" property-type = "get" id = "get-nxttouchsensor-pressedeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtTouchSensor" property-selector = "PressedEventEnabled" property-type = "set" id = "set-nxttouchsensor-pressedeventenabled"></div>

Whether the Pressed event should fire when the touch sensor is pressed.

|      Param Name     | IO Type |
| :-----------------: | :-----: |
| PressedEventEnabled | boolean |

### ReleasedEventEnabled

<div block-type = "component_set_get" component-selector = "NxtTouchSensor" property-selector = "ReleasedEventEnabled" property-type = "get" id = "get-nxttouchsensor-releasedeventenabled"></div>

<div block-type = "component_set_get" component-selector = "NxtTouchSensor" property-selector = "ReleasedEventEnabled" property-type = "set" id = "set-nxttouchsensor-releasedeventenabled"></div>

Whether the Released event should fire when the touch sensor is released.

|      Param Name      | IO Type |
| :------------------: | :-----: |
| ReleasedEventEnabled | boolean |

## Component

---

### NxtTouchSensor

<div block-type = "component_component_block" component-selector = "NxtTouchSensor" id = "component-nxttouchsensor"></div>

Return Type : component

Component NxtTouchSensor

