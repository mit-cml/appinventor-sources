<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# NxtDirectCommands

A component that provides a low-level interface to a LEGO MINDSTORMS NXT robot, with functions to send NXT Direct Commands.

---

## Designer Properties

---

### BluetoothClient

|  Property Name  |   Editor Type   | Default Value |
| :-------------: | :-------------: | :-----------: |
| BluetoothClient | BluetoothClient |               |

## Methods

---

### DeleteFile

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "DeleteFile" id = "nxtdirectcommands-deletefile"></div>

Return Type : No Return Value

Delete a file on the robot.

| Param Name | Input Type |
| :--------: | :--------: |
|  fileName  |    text    |

### DownloadFile

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "DownloadFile" id = "nxtdirectcommands-downloadfile"></div>

Return Type : No Return Value

Download a file to the robot.

|  Param Name | Input Type |
| :---------: | :--------: |
|    source   |    text    |
| destination |    text    |

### GetBatteryLevel

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "GetBatteryLevel" id = "nxtdirectcommands-getbatterylevel"></div>

Return Type : number

Get the battery level for the robot. Returns the voltage in millivolts.

### GetBrickName

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "GetBrickName" id = "nxtdirectcommands-getbrickname"></div>

Return Type : text

Get the brick name of the robot.

### GetCurrentProgramName

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "GetCurrentProgramName" id = "nxtdirectcommands-getcurrentprogramname"></div>

Return Type : text

Get the name of currently running program on the robot.

### GetFirmwareVersion

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "GetFirmwareVersion" id = "nxtdirectcommands-getfirmwareversion"></div>

Return Type : list

Get the firmware and protocol version numbers for the robot as a list where the first element is the firmware version number and the second element is the protocol version number.

### GetInputValues

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "GetInputValues" id = "nxtdirectcommands-getinputvalues"></div>

Return Type : list

Reads the values of an input sensor on the robot. Assumes sensor type has been configured via SetInputMode.

|    Param Name    | Input Type |
| :--------------: | :--------: |
| sensorPortLetter |    text    |

### GetOutputState

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "GetOutputState" id = "nxtdirectcommands-getoutputstate"></div>

Return Type : list

Reads the output state of a motor on the robot.

|    Param Name   | Input Type |
| :-------------: | :--------: |
| motorPortLetter |    text    |

### KeepAlive

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "KeepAlive" id = "nxtdirectcommands-keepalive"></div>

Return Type : number

Keep Alive. Returns the current sleep time limit in milliseconds.

### ListFiles

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "ListFiles" id = "nxtdirectcommands-listfiles"></div>

Return Type : list

Returns a list containing the names of matching files found on the robot.

| Param Name | Input Type |
| :--------: | :--------: |
|  wildcard  |    text    |

### LsGetStatus

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "LsGetStatus" id = "nxtdirectcommands-lsgetstatus"></div>

Return Type : number

Returns the count of available bytes to read.

|    Param Name    | Input Type |
| :--------------: | :--------: |
| sensorPortLetter |    text    |

### LsRead

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "LsRead" id = "nxtdirectcommands-lsread"></div>

Return Type : list

Reads unsigned low speed data from an input sensor on the robot. Assumes sensor type has been configured via SetInputMode.

|    Param Name    | Input Type |
| :--------------: | :--------: |
| sensorPortLetter |    text    |

### LsWrite

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "LsWrite" id = "nxtdirectcommands-lswrite"></div>

Return Type : No Return Value

Writes low speed data to an input sensor on the robot. Assumes sensor type has been configured via SetInputMode.

|    Param Name    | Input Type |
| :--------------: | :--------: |
| sensorPortLetter |    text    |
|       list       |    list    |
|   rxDataLength   |   number   |

### MessageRead

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "MessageRead" id = "nxtdirectcommands-messageread"></div>

Return Type : text

Read a message from a mailbox (1-10) on the robot.

| Param Name | Input Type |
| :--------: | :--------: |
|   mailbox  |   number   |

### MessageWrite

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "MessageWrite" id = "nxtdirectcommands-messagewrite"></div>

Return Type : No Return Value

Write a message to a mailbox (1-10) on the robot.

| Param Name | Input Type |
| :--------: | :--------: |
|   mailbox  |   number   |
|   message  |    text    |

### PlaySoundFile

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "PlaySoundFile" id = "nxtdirectcommands-playsoundfile"></div>

Return Type : No Return Value

Play a sound file on the robot.

| Param Name | Input Type |
| :--------: | :--------: |
|  fileName  |    text    |

### PlayTone

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "PlayTone" id = "nxtdirectcommands-playtone"></div>

Return Type : No Return Value

Make the robot play a tone.

|  Param Name | Input Type |
| :---------: | :--------: |
| frequencyHz |   number   |
|  durationMs |   number   |

### ResetInputScaledValue

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "ResetInputScaledValue" id = "nxtdirectcommands-resetinputscaledvalue"></div>

Return Type : No Return Value

Reset the scaled value of an input sensor on the robot.

|    Param Name    | Input Type |
| :--------------: | :--------: |
| sensorPortLetter |    text    |

### ResetMotorPosition

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "ResetMotorPosition" id = "nxtdirectcommands-resetmotorposition"></div>

Return Type : No Return Value

Reset motor position.

|    Param Name   | Input Type |
| :-------------: | :--------: |
| motorPortLetter |    text    |
|     relative    |   boolean  |

### SetBrickName

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "SetBrickName" id = "nxtdirectcommands-setbrickname"></div>

Return Type : No Return Value

Set the brick name of the robot.

| Param Name | Input Type |
| :--------: | :--------: |
|    name    |    text    |

### SetInputMode

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "SetInputMode" id = "nxtdirectcommands-setinputmode"></div>

Return Type : No Return Value

Configure an input sensor on the robot.

|    Param Name    | Input Type |
| :--------------: | :--------: |
| sensorPortLetter |    text    |
|    sensorType    |   number   |
|    sensorMode    |   number   |

### SetOutputState

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "SetOutputState" id = "nxtdirectcommands-setoutputstate"></div>

Return Type : No Return Value

Sets the output state of a motor on the robot.

|    Param Name   | Input Type |
| :-------------: | :--------: |
| motorPortLetter |    text    |
|      power      |   number   |
|       mode      |   number   |
|  regulationMode |   number   |
|    turnRatio    |   number   |
|     runState    |   number   |
|    tachoLimit   |   number   |

### StartProgram

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "StartProgram" id = "nxtdirectcommands-startprogram"></div>

Return Type : No Return Value

Start execution of a previously downloaded program on the robot.

|  Param Name | Input Type |
| :---------: | :--------: |
| programName |    text    |

### StopProgram

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "StopProgram" id = "nxtdirectcommands-stopprogram"></div>

Return Type : No Return Value

Stop execution of the currently running program on the robot.

### StopSoundPlayback

<div block-type = "component_method" component-selector = "NxtDirectCommands" method-selector = "StopSoundPlayback" id = "nxtdirectcommands-stopsoundplayback"></div>

Return Type : No Return Value

Stop sound playback.

## Block Properties

---

## Component

---

### NxtDirectCommands

<div block-type = "component_component_block" component-selector = "NxtDirectCommands" id = "component-nxtdirectcommands"></div>

Return Type : component

Component NxtDirectCommands

