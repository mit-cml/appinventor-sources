# Ev3Motors

A component that provides both high- and low-level interfaces to a LEGO MINDSTORMS EV3 robot, with functions that can control the motors.

---

## Designer Properties

---

### BluetoothClient

|  Property Name  |   Editor Type   | Default Value |
| :-------------: | :-------------: | :-----------: |
| BluetoothClient | BluetoothClient |               |

### EnableSpeedRegulation

|     Property Name     | Editor Type | Default Value |
| :-------------------: | :---------: | :-----------: |
| EnableSpeedRegulation |   boolean   |      True     |

### MotorPorts

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   MotorPorts  |    string   |      ABC      |

### ReverseDirection

|   Property Name  | Editor Type | Default Value |
| :--------------: | :---------: | :-----------: |
| ReverseDirection |   boolean   |     False     |

### StopBeforeDisconnect

|     Property Name    | Editor Type | Default Value |
| :------------------: | :---------: | :-----------: |
| StopBeforeDisconnect |   boolean   |      True     |

### TachoCountChangedEventEnabled

|         Property Name         | Editor Type | Default Value |
| :---------------------------: | :---------: | :-----------: |
| TachoCountChangedEventEnabled |   boolean   |     False     |

### WheelDiameter

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| WheelDiameter |    float    |      4.32     |

## Events

---

### TachoCountChanged

<div block-type = "component_event" component-selector = "Ev3Motors" event-selector = "TachoCountChanged" event-params = "tachoCount" id = "ev3motors-tachocountchanged"></div>

Called when the tacho count has changed.

| Param Name | IO Type |
| :--------: | :-----: |
| tachoCount |  number |

## Methods

---

### GetTachoCount

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "GetTachoCount" method-params = "" return-type = "number" id = "ev3motors-gettachocount"></div>

Return Type : number

Get the current tacho count.

### ResetTachoCount

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "ResetTachoCount" method-params = "" return-type = "undefined" id = "ev3motors-resettachocount"></div>

Return Type : No Return Value

Set the current tacho count to zero.

### RotateInDistance

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "RotateInDistance" method-params = "power-distance-useBrake" return-type = "undefined" id = "ev3motors-rotateindistance"></div>

Return Type : No Return Value

Rotate the motors in a distance.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |
|  distance  |   number   |
|  useBrake  |   boolean  |

### RotateInDuration

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "RotateInDuration" method-params = "power-milliseconds-useBrake" return-type = "undefined" id = "ev3motors-rotateinduration"></div>

Return Type : No Return Value

Rotate the motors in a period of time.

|  Param Name  | Input Type |
| :----------: | :--------: |
|     power    |   number   |
| milliseconds |   number   |
|   useBrake   |   boolean  |

### RotateInTachoCounts

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "RotateInTachoCounts" method-params = "power-tachoCounts-useBrake" return-type = "undefined" id = "ev3motors-rotateintachocounts"></div>

Return Type : No Return Value

Rotate the motors in a number of tacho counts.

|  Param Name | Input Type |
| :---------: | :--------: |
|    power    |   number   |
| tachoCounts |   number   |
|   useBrake  |   boolean  |

### RotateIndefinitely

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "RotateIndefinitely" method-params = "power" return-type = "undefined" id = "ev3motors-rotateindefinitely"></div>

Return Type : No Return Value

Start to rotate the motors.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |

### RotateSyncInDistance

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "RotateSyncInDistance" method-params = "power-distance-turnRatio-useBrake" return-type = "undefined" id = "ev3motors-rotatesyncindistance"></div>

Return Type : No Return Value

Rotate the motors at the same speed for a distance in cm.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |
|  distance  |   number   |
|  turnRatio |   number   |
|  useBrake  |   boolean  |

### RotateSyncInDuration

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "RotateSyncInDuration" method-params = "power-milliseconds-turnRatio-useBrake" return-type = "undefined" id = "ev3motors-rotatesyncinduration"></div>

Return Type : No Return Value

Rotate the motors at the same speed in a period of time.

|  Param Name  | Input Type |
| :----------: | :--------: |
|     power    |   number   |
| milliseconds |   number   |
|   turnRatio  |   number   |
|   useBrake   |   boolean  |

### RotateSyncInTachoCounts

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "RotateSyncInTachoCounts" method-params = "power-tachoCounts-turnRatio-useBrake" return-type = "undefined" id = "ev3motors-rotatesyncintachocounts"></div>

Return Type : No Return Value

Rotate the motors at the same speed in a number of tacho counts.

|  Param Name | Input Type |
| :---------: | :--------: |
|    power    |   number   |
| tachoCounts |   number   |
|  turnRatio  |   number   |
|   useBrake  |   boolean  |

### RotateSyncIndefinitely

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "RotateSyncIndefinitely" method-params = "power-turnRatio" return-type = "undefined" id = "ev3motors-rotatesyncindefinitely"></div>

Return Type : No Return Value

Start to rotate the motors at the same speed.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |
|  turnRatio |   number   |

### Stop

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "Stop" method-params = "useBrake" return-type = "undefined" id = "ev3motors-stop"></div>

Return Type : No Return Value

Stop the motors of the robot.

| Param Name | Input Type |
| :--------: | :--------: |
|  useBrake  |   boolean  |

### ToggleDirection

<div block-type = "component_method" component-selector = "Ev3Motors" method-selector = "ToggleDirection" method-params = "" return-type = "undefined" id = "ev3motors-toggledirection"></div>

Return Type : No Return Value

Toggle the direction of motors.

## Block Properties

---

### BluetoothClient

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "BluetoothClient" property-type = "get" id = "get-ev3motors-bluetoothclient"></div>

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "BluetoothClient" property-type = "set" id = "set-ev3motors-bluetoothclient"></div>

The BluetoothClient component that should be used for communication.

|    Param Name   |  IO Type  |
| :-------------: | :-------: |
| BluetoothClient | component |

### EnableSpeedRegulation

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "EnableSpeedRegulation" property-type = "get" id = "get-ev3motors-enablespeedregulation"></div>

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "EnableSpeedRegulation" property-type = "set" id = "set-ev3motors-enablespeedregulation"></div>

Specifies whether to keep motor rotation at constant speed.

|       Param Name      | IO Type |
| :-------------------: | :-----: |
| EnableSpeedRegulation | boolean |

### ReverseDirection

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "ReverseDirection" property-type = "get" id = "get-ev3motors-reversedirection"></div>

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "ReverseDirection" property-type = "set" id = "set-ev3motors-reversedirection"></div>

Set whether the direction of motors is reversed or not.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| ReverseDirection | boolean |

### StopBeforeDisconnect

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "StopBeforeDisconnect" property-type = "get" id = "get-ev3motors-stopbeforedisconnect"></div>

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "StopBeforeDisconnect" property-type = "set" id = "set-ev3motors-stopbeforedisconnect"></div>

Whether to stop the motor before disconnecting.

|      Param Name      | IO Type |
| :------------------: | :-----: |
| StopBeforeDisconnect | boolean |

### TachoCountChangedEventEnabled

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "TachoCountChangedEventEnabled" property-type = "get" id = "get-ev3motors-tachocountchangedeventenabled"></div>

<div block-type = "component_set_get" component-selector = "Ev3Motors" property-selector = "TachoCountChangedEventEnabled" property-type = "set" id = "set-ev3motors-tachocountchangedeventenabled"></div>

Whether the TachoCountChanged event should fire when the angle is changed.

|           Param Name          | IO Type |
| :---------------------------: | :-----: |
| TachoCountChangedEventEnabled | boolean |

## Component

---

### Ev3Motors

<div block-type = "component_component_block" component-selector = "Ev3Motors" id = "component-ev3motors"></div>

Return Type : component

Component Ev3Motors

