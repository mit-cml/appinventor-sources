# NxtDrive

A component that provides a high-level interface to a LEGO MINDSTORMS NXT robot, with functions that can move and turn the robot.

---

## Designer Properties

---

### BluetoothClient

|  Property Name  |   Editor Type   | Default Value |
| :-------------: | :-------------: | :-----------: |
| BluetoothClient | BluetoothClient |               |

### DriveMotors

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  DriveMotors  |    string   |       CB      |

### StopBeforeDisconnect

|     Property Name    | Editor Type | Default Value |
| :------------------: | :---------: | :-----------: |
| StopBeforeDisconnect |   boolean   |      True     |

### WheelDiameter

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| WheelDiameter |    float    |      4.32     |

## Methods

---

### MoveBackward

<div block-type = "component_method" component-selector = "NxtDrive" method-selector = "MoveBackward" method-params = "power-distance" return-type = "undefined" id = "nxtdrive-movebackward"></div>

Return Type : No Return Value

Move the robot backward the given distance, with the specified percentage of maximum power, by powering both drive motors backward.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |
|  distance  |   number   |

### MoveBackwardIndefinitely

<div block-type = "component_method" component-selector = "NxtDrive" method-selector = "MoveBackwardIndefinitely" method-params = "power" return-type = "undefined" id = "nxtdrive-movebackwardindefinitely"></div>

Return Type : No Return Value

Move the robot backward indefinitely, with the specified percentage of maximum power, by powering both drive motors backward.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |

### MoveForward

<div block-type = "component_method" component-selector = "NxtDrive" method-selector = "MoveForward" method-params = "power-distance" return-type = "undefined" id = "nxtdrive-moveforward"></div>

Return Type : No Return Value

Move the robot forward the given distance, with the specified percentage of maximum power, by powering both drive motors forward.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |
|  distance  |   number   |

### MoveForwardIndefinitely

<div block-type = "component_method" component-selector = "NxtDrive" method-selector = "MoveForwardIndefinitely" method-params = "power" return-type = "undefined" id = "nxtdrive-moveforwardindefinitely"></div>

Return Type : No Return Value

Move the robot forward indefinitely, with the specified percentage of maximum power, by powering both drive motors forward.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |

### Stop

<div block-type = "component_method" component-selector = "NxtDrive" method-selector = "Stop" method-params = "" return-type = "undefined" id = "nxtdrive-stop"></div>

Return Type : No Return Value

Stop the drive motors of the robot.

### TurnClockwiseIndefinitely

<div block-type = "component_method" component-selector = "NxtDrive" method-selector = "TurnClockwiseIndefinitely" method-params = "power" return-type = "undefined" id = "nxtdrive-turnclockwiseindefinitely"></div>

Return Type : No Return Value

Turn the robot clockwise indefinitely, with the specified percentage of maximum power, by powering the left drive motor forward and the right drive motor backward.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |

### TurnCounterClockwiseIndefinitely

<div block-type = "component_method" component-selector = "NxtDrive" method-selector = "TurnCounterClockwiseIndefinitely" method-params = "power" return-type = "undefined" id = "nxtdrive-turncounterclockwiseindefinitely"></div>

Return Type : No Return Value

Turn the robot counterclockwise indefinitely, with the specified percentage of maximum power, by powering the right drive motor forward and the left drive motor backward.

| Param Name | Input Type |
| :--------: | :--------: |
|    power   |   number   |

## Block Properties

---

### StopBeforeDisconnect

<div block-type = "component_set_get" component-selector = "NxtDrive" property-selector = "StopBeforeDisconnect" property-type = "get" id = "get-nxtdrive-stopbeforedisconnect"></div>

<div block-type = "component_set_get" component-selector = "NxtDrive" property-selector = "StopBeforeDisconnect" property-type = "set" id = "set-nxtdrive-stopbeforedisconnect"></div>

Whether to stop the drive motors before disconnecting.

|      Param Name      | IO Type |
| :------------------: | :-----: |
| StopBeforeDisconnect | boolean |

## Component

---

### NxtDrive

<div block-type = "component_component_block" component-selector = "NxtDrive" id = "component-nxtdrive"></div>

Return Type : component

Component NxtDrive

