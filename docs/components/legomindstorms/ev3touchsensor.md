# Ev3TouchSensor

A component that provides a high-level interface to a touch sensor on a LEGO MINDSTORMS EV3 robot.

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
|   SensorPort  | lego_ev3_sensor_port |       1       |

## Events

---

### Pressed

<div block-type = "component_event" component-selector = "Ev3TouchSensor" event-selector = "Pressed" event-params = "" id = "ev3touchsensor-pressed"></div>

Called when the touch sensor is pressed.

### Released

<div block-type = "component_event" component-selector = "Ev3TouchSensor" event-selector = "Released" event-params = "" id = "ev3touchsensor-released"></div>

Called when the touch sensor is pressed.

## Methods

---

### IsPressed

<div block-type = "component_method" component-selector = "Ev3TouchSensor" method-selector = "IsPressed" method-params = "" return-type = "boolean" id = "ev3touchsensor-ispressed"></div>

Return Type : boolean

Returns true if the touch sensor is pressed.

## Block Properties

---

### BluetoothClient

<div block-type = "component_set_get" component-selector = "Ev3TouchSensor" property-selector = "BluetoothClient" property-type = "get" id = "get-ev3touchsensor-bluetoothclient"></div>

<div block-type = "component_set_get" component-selector = "Ev3TouchSensor" property-selector = "BluetoothClient" property-type = "set" id = "set-ev3touchsensor-bluetoothclient"></div>

The BluetoothClient component that should be used for communication.

|    Param Name   |  IO Type  |
| :-------------: | :-------: |
| BluetoothClient | component |

### PressedEventEnabled

<div block-type = "component_set_get" component-selector = "Ev3TouchSensor" property-selector = "PressedEventEnabled" property-type = "get" id = "get-ev3touchsensor-pressedeventenabled"></div>

<div block-type = "component_set_get" component-selector = "Ev3TouchSensor" property-selector = "PressedEventEnabled" property-type = "set" id = "set-ev3touchsensor-pressedeventenabled"></div>

Specifies whether the Pressed event should fire when the touch sensor is pressed.

|      Param Name     | IO Type |
| :-----------------: | :-----: |
| PressedEventEnabled | boolean |

### ReleasedEventEnabled

<div block-type = "component_set_get" component-selector = "Ev3TouchSensor" property-selector = "ReleasedEventEnabled" property-type = "get" id = "get-ev3touchsensor-releasedeventenabled"></div>

<div block-type = "component_set_get" component-selector = "Ev3TouchSensor" property-selector = "ReleasedEventEnabled" property-type = "set" id = "set-ev3touchsensor-releasedeventenabled"></div>

Whether the Released event should fire when the touch sensor is released.

|      Param Name      | IO Type |
| :------------------: | :-----: |
| ReleasedEventEnabled | boolean |

## Component

---

### Ev3TouchSensor

<div block-type = "component_component_block" component-selector = "Ev3TouchSensor" id = "component-ev3touchsensor"></div>

Return Type : component

Component Ev3TouchSensor

