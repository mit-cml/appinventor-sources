---
layout: documentation
title: LEGO® MINDSTORMS®
---

[&laquo; Back to index](index.html)
# LEGO® MINDSTORMS®

Table of Contents:

* [Ev3ColorSensor](#Ev3ColorSensor)
* [Ev3Commands](#Ev3Commands)
* [Ev3GyroSensor](#Ev3GyroSensor)
* [Ev3Motors](#Ev3Motors)
* [Ev3Sound](#Ev3Sound)
* [Ev3TouchSensor](#Ev3TouchSensor)
* [Ev3UI](#Ev3UI)
* [Ev3UltrasonicSensor](#Ev3UltrasonicSensor)
* [NxtColorSensor](#NxtColorSensor)
* [NxtDirectCommands](#NxtDirectCommands)
* [NxtDrive](#NxtDrive)
* [NxtLightSensor](#NxtLightSensor)
* [NxtSoundSensor](#NxtSoundSensor)
* [NxtTouchSensor](#NxtTouchSensor)
* [NxtUltrasonicSensor](#NxtUltrasonicSensor)

## Ev3ColorSensor  {#Ev3ColorSensor}

![EV3 component icon](images/legoMindstormsEv3.png)

 A component that provides a high-level interface to a color sensor on a
 LEGO MINDSTORMS EV3 robot.



### Properties  {#Ev3ColorSensor-Properties}

{:.properties}

{:id="Ev3ColorSensor.AboveRangeEventEnabled" .boolean} *AboveRangeEventEnabled*
: Specifies whether the AboveRange event should fire when the light level
 goes above the TopOfRange.

{:id="Ev3ColorSensor.BelowRangeEventEnabled" .boolean} *BelowRangeEventEnabled*
: Specifies whether the BelowRange event should fire when the light level
 goes below the BottomOfRange.

{:id="Ev3ColorSensor.BluetoothClient" .component} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer**.

{:id="Ev3ColorSensor.BottomOfRange" .number} *BottomOfRange*
: Specifies the bottom of the range used for the BelowRange, WithinRange,
 and AboveRange events.

{:id="Ev3ColorSensor.ColorChangedEventEnabled" .boolean} *ColorChangedEventEnabled*
: Specifies whether the ColorChanged event should fire when the DetectColor
 property is set to True and the detected color changes

{:id="Ev3ColorSensor.Mode" .text} *Mode*
: The current mode of the sensor. One of:
     Reflected: Senses the current light level including light reflected by the sensor.
     Ambient: Senses the current light level *not* including light reflected by the sensor.
     Color: Senses the color the sensor is pointing at.

{:id="Ev3ColorSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be set in the Designer.**

{:id="Ev3ColorSensor.TopOfRange" .number} *TopOfRange*
: Specifies the top of the range used for the BelowRange, WithinRange, and
 AboveRange events.

{:id="Ev3ColorSensor.WithinRangeEventEnabled" .boolean} *WithinRangeEventEnabled*
: Specifies whether the WithinRange event should fire when the light level
 goes between the BottomOfRange and the TopOfRange.

### Events  {#Ev3ColorSensor-Events}

{:.events}

{:id="Ev3ColorSensor.AboveRange"} AboveRange()
: Light level has gone above the range.

{:id="Ev3ColorSensor.BelowRange"} BelowRange()
: Light level has gone below the range.

{:id="Ev3ColorSensor.ColorChanged"} ColorChanged(*colorCode*{:.number},*colorName*{:.text})
: Called when the detected color has changed.

{:id="Ev3ColorSensor.WithinRange"} WithinRange()
: Light level has gone within the range.

### Methods  {#Ev3ColorSensor-Methods}

{:.methods}

{:id="Ev3ColorSensor.GetColorCode" class="method returns number"} <i/> GetColorCode()
: It returns the color code for the detected color.

{:id="Ev3ColorSensor.GetColorName" class="method returns text"} <i/> GetColorName()
: Returns the name of the detected color.

{:id="Ev3ColorSensor.GetLightLevel" class="method returns number"} <i/> GetLightLevel()
: It returns the light level in percentage.

## Ev3Commands  {#Ev3Commands}

![EV3 component icon](images/legoMindstormsEv3.png)

 A component that provides a low-level interface to a LEGO MINDSTORMS EV3
 robot, with functions to send system or direct commands to EV3 robots.



### Properties  {#Ev3Commands-Properties}

{:.properties}

{:id="Ev3Commands.BluetoothClient" .component} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer**.

### Events  {#Ev3Commands-Events}

{:.events}
None


### Methods  {#Ev3Commands-Methods}

{:.methods}

{:id="Ev3Commands.GetBatteryCurrent" class="method returns number"} <i/> GetBatteryCurrent()
: Get the battery current.

{:id="Ev3Commands.GetBatteryVoltage" class="method returns number"} <i/> GetBatteryVoltage()
: Get the battery voltage.

{:id="Ev3Commands.GetFirmwareBuild" class="method returns text"} <i/> GetFirmwareBuild()
: Get the firmware build on EV3.

{:id="Ev3Commands.GetFirmwareVersion" class="method returns text"} <i/> GetFirmwareVersion()
: Get the firmware version on EV3.

{:id="Ev3Commands.GetHardwareVersion" class="method returns text"} <i/> GetHardwareVersion()
: Get the hardware version of EV3.

{:id="Ev3Commands.GetOSBuild" class="method returns text"} <i/> GetOSBuild()
: Get the OS build on EV3.

{:id="Ev3Commands.GetOSVersion" class="method returns text"} <i/> GetOSVersion()
: Get the OS version on EV3.

{:id="Ev3Commands.KeepAlive" class="method"} <i/> KeepAlive(*minutes*{:.number})
: Keep the EV3 brick from shutdown for a period of time.

## Ev3GyroSensor  {#Ev3GyroSensor}

![EV3 component icon](images/legoMindstormsEv3.png)

 A component that provides a high-level interface to a gyro sensor on a LEGO
 MINDSTORMS EV3 robot.



### Properties  {#Ev3GyroSensor-Properties}

{:.properties}

{:id="Ev3GyroSensor.BluetoothClient" .component} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer**.

{:id="Ev3GyroSensor.Mode" .text} *Mode*
: Returns the mode of the sensor.

{:id="Ev3GyroSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be set in the Designer.**

{:id="Ev3GyroSensor.SensorValueChangedEventEnabled" .boolean} *SensorValueChangedEventEnabled*
: Returns whether the SensorValueChanged event should fire when the sensor value changed.

### Events  {#Ev3GyroSensor-Events}

{:.events}

{:id="Ev3GyroSensor.SensorValueChanged"} SensorValueChanged(*sensorValue*{:.number})
: Called then the sensor value changed.

### Methods  {#Ev3GyroSensor-Methods}

{:.methods}

{:id="Ev3GyroSensor.GetSensorValue" class="method returns number"} <i/> GetSensorValue()
: Returns the current angle or rotation speed based on current mode,
 or -1 if the value cannot be read from sensor.

## Ev3Motors  {#Ev3Motors}

![EV3 component icon](images/legoMindstormsEv3.png)

 A component that provides both high- and low-level interfaces to
 control the motors on LEGO MINDSTORMS EV3.



### Properties  {#Ev3Motors-Properties}

{:.properties}

{:id="Ev3Motors.BluetoothClient" .component} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer**.

{:id="Ev3Motors.EnableSpeedRegulation" .boolean} *EnableSpeedRegulation*
: Returns whether to keep motor rotation at constant speed.

{:id="Ev3Motors.MotorPorts" .text .do} *MotorPorts*
: Specifies the motor port.

{:id="Ev3Motors.ReverseDirection" .boolean} *ReverseDirection*
: Returns if the direction of the motors is reversed.

{:id="Ev3Motors.StopBeforeDisconnect" .boolean} *StopBeforeDisconnect*
: Specifies whether to stop the drive motors before disconnecting.

{:id="Ev3Motors.TachoCountChangedEventEnabled" .boolean} *TachoCountChangedEventEnabled*
: Returns whether the TachoCountChanged event should fire when the motor angle is increaing.

{:id="Ev3Motors.WheelDiameter" .number .do} *WheelDiameter*
: Returns the diameter of the wheels attached on motors.

### Events  {#Ev3Motors-Events}

{:.events}

{:id="Ev3Motors.TachoCountChanged"} TachoCountChanged(*tachoCount*{:.number})
: Called when the tacho count has changed.

### Methods  {#Ev3Motors-Methods}

{:.methods}

{:id="Ev3Motors.GetTachoCount" class="method returns number"} <i/> GetTachoCount()
: Get the current tacho count.

{:id="Ev3Motors.ResetTachoCount" class="method"} <i/> ResetTachoCount()
: Set the current tacho count to zero.

{:id="Ev3Motors.RotateInDistance" class="method"} <i/> RotateInDistance(*power*{:.number},*distance*{:.number},*useBrake*{:.boolean})
: Rotate the motors in a distance.

{:id="Ev3Motors.RotateInDuration" class="method"} <i/> RotateInDuration(*power*{:.number},*milliseconds*{:.number},*useBrake*{:.boolean})
: Rotate the motors in a period of time.

{:id="Ev3Motors.RotateInTachoCounts" class="method"} <i/> RotateInTachoCounts(*power*{:.number},*tachoCounts*{:.number},*useBrake*{:.boolean})
: Rotate the motors in a number of tacho counts.

{:id="Ev3Motors.RotateIndefinitely" class="method"} <i/> RotateIndefinitely(*power*{:.number})
: Start to rotate the motors.

{:id="Ev3Motors.RotateSyncInDistance" class="method"} <i/> RotateSyncInDistance(*power*{:.number},*distance*{:.number},*turnRatio*{:.number},*useBrake*{:.boolean})
: Rotate the motors at the same speed for a distance in cm.

{:id="Ev3Motors.RotateSyncInDuration" class="method"} <i/> RotateSyncInDuration(*power*{:.number},*milliseconds*{:.number},*turnRatio*{:.number},*useBrake*{:.boolean})
: Rotate the motors at the same speed in a period of time.

{:id="Ev3Motors.RotateSyncInTachoCounts" class="method"} <i/> RotateSyncInTachoCounts(*power*{:.number},*tachoCounts*{:.number},*turnRatio*{:.number},*useBrake*{:.boolean})
: Rotate the motors at the same speed in a number of tacho counts.

{:id="Ev3Motors.RotateSyncIndefinitely" class="method"} <i/> RotateSyncIndefinitely(*power*{:.number},*turnRatio*{:.number})
: Start to rotate the motors at the same speed.

{:id="Ev3Motors.Stop" class="method"} <i/> Stop(*useBrake*{:.boolean})
: Stop the motors of the robot.

{:id="Ev3Motors.ToggleDirection" class="method"} <i/> ToggleDirection()
: Toggle the direction of motors.

## Ev3Sound  {#Ev3Sound}

![EV3 component icon](images/legoMindstormsEv3.png)

 A component that provides a high-level interface to a LEGO MINDSTORMS EV3
 robot, which provides sound functionalities.



### Properties  {#Ev3Sound-Properties}

{:.properties}

{:id="Ev3Sound.BluetoothClient" .component} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer**.

### Events  {#Ev3Sound-Events}

{:.events}
None


### Methods  {#Ev3Sound-Methods}

{:.methods}

{:id="Ev3Sound.PlayTone" class="method"} <i/> PlayTone(*volume*{:.number},*frequency*{:.number},*milliseconds*{:.number})
: Make the robot play a tone.

{:id="Ev3Sound.StopSound" class="method"} <i/> StopSound()
: Stop any sound on the robot.

## Ev3TouchSensor  {#Ev3TouchSensor}

![EV3 component icon](images/legoMindstormsEv3.png)

 A component that provides a high-level interface to a touch sensor on a LEGO
 MINDSTORMS EV3 robot.



### Properties  {#Ev3TouchSensor-Properties}

{:.properties}

{:id="Ev3TouchSensor.BluetoothClient" .component} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer**.

{:id="Ev3TouchSensor.PressedEventEnabled" .boolean} *PressedEventEnabled*
: Returns whether the Pressed event should fire when the touch sensor is
 pressed.

{:id="Ev3TouchSensor.ReleasedEventEnabled" .boolean} *ReleasedEventEnabled*
: Specifies whether the Released event should fire when the touch sensor is
 released.

{:id="Ev3TouchSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be set in the Designer.**

### Events  {#Ev3TouchSensor-Events}

{:.events}

{:id="Ev3TouchSensor.Pressed"} Pressed()
: Called when the touch sensor is pressed.

{:id="Ev3TouchSensor.Released"} Released()
: Called when the touch sensor is pressed.

### Methods  {#Ev3TouchSensor-Methods}

{:.methods}

{:id="Ev3TouchSensor.IsPressed" class="method returns boolean"} <i/> IsPressed()
: Returns true if the touch sensor is pressed.

## Ev3UI  {#Ev3UI}

![EV3 component icon](images/legoMindstormsEv3.png)

 A component that provides a high-level interface to a LEGO MINDSTORMS EV3
 robot, which provides graphic functionalities.



### Properties  {#Ev3UI-Properties}

{:.properties}

{:id="Ev3UI.BluetoothClient" .component} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer**.

### Events  {#Ev3UI-Events}

{:.events}
None


### Methods  {#Ev3UI-Methods}

{:.methods}

{:id="Ev3UI.DrawCircle" class="method"} <i/> DrawCircle(*color*{:.number},*x*{:.number},*y*{:.number},*radius*{:.number},*fill*{:.boolean})
: Draw a circle on the screen.

{:id="Ev3UI.DrawIcon" class="method"} <i/> DrawIcon(*color*{:.number},*x*{:.number},*y*{:.number},*type*{:.number},*no*{:.number})
: Draw a built-in icon on screen.

{:id="Ev3UI.DrawLine" class="method"} <i/> DrawLine(*color*{:.number},*x1*{:.number},*y1*{:.number},*x2*{:.number},*y2*{:.number})
: Draw a line on the screen.

{:id="Ev3UI.DrawPoint" class="method"} <i/> DrawPoint(*color*{:.number},*x*{:.number},*y*{:.number})
: Draw a point on the screen.

{:id="Ev3UI.DrawRect" class="method"} <i/> DrawRect(*color*{:.number},*x*{:.number},*y*{:.number},*width*{:.number},*height*{:.number},*fill*{:.boolean})
: Draw a rectangle on the screen.

{:id="Ev3UI.FillScreen" class="method"} <i/> FillScreen(*color*{:.number})
: Fill the screen with a color.

## Ev3UltrasonicSensor  {#Ev3UltrasonicSensor}

![EV3 component icon](images/legoMindstormsEv3.png)

 A component that provides a high-level interface to an ultrasonic sensor on a LEGO
 MINDSTORMS EV3 robot.



### Properties  {#Ev3UltrasonicSensor-Properties}

{:.properties}

{:id="Ev3UltrasonicSensor.AboveRangeEventEnabled" .boolean} *AboveRangeEventEnabled*
: Specifies whether the AboveRange event should fire when the distance
 goes above the TopOfRange.

{:id="Ev3UltrasonicSensor.BelowRangeEventEnabled" .boolean} *BelowRangeEventEnabled*
: Specifies whether the BelowRange event should fire when the distance
 goes below the BottomOfRange.

{:id="Ev3UltrasonicSensor.BluetoothClient" .component} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer**.

{:id="Ev3UltrasonicSensor.BottomOfRange" .number} *BottomOfRange*
: Specifies the bottom of the range used for the BelowRange, WithinRange,
 and AboveRange events.

{:id="Ev3UltrasonicSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be set in the Designer.**

{:id="Ev3UltrasonicSensor.TopOfRange" .number} *TopOfRange*
: Specifies the top of the range used for the BelowRange, WithinRange, and
 AboveRange events.

{:id="Ev3UltrasonicSensor.Unit" .text} *Unit*
: Returns the unit of distance.

{:id="Ev3UltrasonicSensor.WithinRangeEventEnabled" .boolean} *WithinRangeEventEnabled*
: Specifies whether the WithinRange event should fire when the distance
 goes between the BottomOfRange and the TopOfRange.

### Events  {#Ev3UltrasonicSensor-Events}

{:.events}

{:id="Ev3UltrasonicSensor.AboveRange"} AboveRange()
: Called when the detected distance has gone above the range.

{:id="Ev3UltrasonicSensor.BelowRange"} BelowRange()
: Called when the detected distance has gone below the range.

{:id="Ev3UltrasonicSensor.WithinRange"} WithinRange()
: Called when the detected distance has gone within the range.

### Methods  {#Ev3UltrasonicSensor-Methods}

{:.methods}

{:id="Ev3UltrasonicSensor.GetDistance" class="method returns number"} <i/> GetDistance()
: Returns the current distance in centimeters as a value between 0 and 254, or -1 if the distance can not be read.

## NxtColorSensor  {#NxtColorSensor}

![NXT component icon](images/legoMindstormsNxt.png)

 A component that provides a high-level interface to a color sensor on a LEGO
 MINDSTORMS NXT robot.



### Properties  {#NxtColorSensor-Properties}

{:.properties}

{:id="NxtColorSensor.AboveRangeEventEnabled" .boolean} *AboveRangeEventEnabled*
: Specifies whether the AboveRange event should fire when the DetectColor
 property is set to False and the light level goes above the TopOfRange.

{:id="NxtColorSensor.BelowRangeEventEnabled" .boolean} *BelowRangeEventEnabled*
: Specifies whether the BelowRange event should fire when the DetectColor
 property is set to False and the light level goes below the BottomOfRange.

{:id="NxtColorSensor.BluetoothClient" .component .do} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer.**

{:id="NxtColorSensor.BottomOfRange" .number} *BottomOfRange*
: Specifies the bottom of the range used for the BelowRange, WithinRange,
 and AboveRange events.

{:id="NxtColorSensor.ColorChangedEventEnabled" .boolean} *ColorChangedEventEnabled*
: Specifies whether the ColorChanged event should fire when the DetectColor
 property is set to True and the detected color changes

{:id="NxtColorSensor.DetectColor" .boolean} *DetectColor*
: Specifies whether the sensor should detect color light. True indicates
 that the sensor should detect color; False indicates that the sensor
 should detect light.

{:id="NxtColorSensor.GenerateColor" .number} *GenerateColor*
: Specifies the color that should generated by the sensor.
 Only None, Red, Green, or Blue are valid values.
 The sensor will not generate color when the DetectColor property is set to
 True.

{:id="NxtColorSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be set in the Designer.**

{:id="NxtColorSensor.TopOfRange" .number} *TopOfRange*
: Specifies the top of the range used for the BelowRange, WithinRange, and
 AboveRange events.

{:id="NxtColorSensor.WithinRangeEventEnabled" .boolean} *WithinRangeEventEnabled*
: Specifies whether the WithinRange event should fire when the DetectColor
 property is set to False and the light level goes between the
 BottomOfRange and the TopOfRange.

### Events  {#NxtColorSensor-Events}

{:.events}

{:id="NxtColorSensor.AboveRange"} AboveRange()
: Light level has gone above the range. The AboveRange event will not occur if the DetectColor property is set to True or if the AboveRangeEventEnabled property is set to False.

{:id="NxtColorSensor.BelowRange"} BelowRange()
: Light level has gone below the range. The BelowRange event will not occur if the DetectColor property is set to True or if the BelowRangeEventEnabled property is set to False.

{:id="NxtColorSensor.ColorChanged"} ColorChanged(*color*{:.number})
: Detected color has changed. The ColorChanged event will not occur if the DetectColor property is set to False or if the ColorChangedEventEnabled property is set to False.

{:id="NxtColorSensor.WithinRange"} WithinRange()
: Light level has gone within the range. The WithinRange event will not occur if the DetectColor property is set to True or if the WithinRangeEventEnabled property is set to False.

### Methods  {#NxtColorSensor-Methods}

{:.methods}

{:id="NxtColorSensor.GetColor" class="method returns number"} <i/> GetColor()
: Returns the current detected color, or the color None if the color can not be read or if the DetectColor property is set to False.

{:id="NxtColorSensor.GetLightLevel" class="method returns number"} <i/> GetLightLevel()
: Returns the current light level as a value between 0 and 1023, or -1 if the light level can not be read or if the DetectColor property is set to True.

## NxtDirectCommands  {#NxtDirectCommands}

![NXT component icon](images/legoMindstormsNxt.png)

 A component that provides a low-level interface to a LEGO MINDSTORMS NXT
 robot, with functions to send NXT Direct Commands.



### Properties  {#NxtDirectCommands-Properties}

{:.properties}

{:id="NxtDirectCommands.BluetoothClient" .component .do} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer.**

### Events  {#NxtDirectCommands-Events}

{:.events}
None


### Methods  {#NxtDirectCommands-Methods}

{:.methods}

{:id="NxtDirectCommands.DeleteFile" class="method"} <i/> DeleteFile(*fileName*{:.text})
: Delete a file on the robot.

{:id="NxtDirectCommands.DownloadFile" class="method"} <i/> DownloadFile(*source*{:.text},*destination*{:.text})
: Download a file to the robot.

 <p/>See [`MediaUtil`'s `determineMediaSource`](#MediaUtil.determineMediaSource) for information about what
 a path can be.

{:id="NxtDirectCommands.GetBatteryLevel" class="method returns number"} <i/> GetBatteryLevel()
: Get the battery level for the robot. Returns the voltage in millivolts.

{:id="NxtDirectCommands.GetBrickName" class="method returns text"} <i/> GetBrickName()
: Get the brick name of the robot.

{:id="NxtDirectCommands.GetCurrentProgramName" class="method returns text"} <i/> GetCurrentProgramName()
: Get the name of currently running program on the robot.

{:id="NxtDirectCommands.GetFirmwareVersion" class="method returns list"} <i/> GetFirmwareVersion()
: Get the firmware and protocol version numbers for the robot as a list where the first element is the firmware version number and the second element is the protocol version number.

{:id="NxtDirectCommands.GetInputValues" class="method returns list"} <i/> GetInputValues(*sensorPortLetter*{:.text})
: Reads the values of an input sensor on the robot. Assumes sensor type has been configured via SetInputMode.

{:id="NxtDirectCommands.GetOutputState" class="method returns list"} <i/> GetOutputState(*motorPortLetter*{:.text})
: Reads the output state of a motor on the robot.

{:id="NxtDirectCommands.KeepAlive" class="method returns number"} <i/> KeepAlive()
: Keep Alive. Returns the current sleep time limit in milliseconds.

{:id="NxtDirectCommands.ListFiles" class="method returns list"} <i/> ListFiles(*wildcard*{:.text})
: Returns a list containing the names of matching files found on the robot.

{:id="NxtDirectCommands.LsGetStatus" class="method returns number"} <i/> LsGetStatus(*sensorPortLetter*{:.text})
: Returns the count of available bytes to read.

{:id="NxtDirectCommands.LsRead" class="method returns list"} <i/> LsRead(*sensorPortLetter*{:.text})
: Reads unsigned low speed data from an input sensor on the robot. Assumes sensor type has been configured via SetInputMode.

{:id="NxtDirectCommands.LsWrite" class="method"} <i/> LsWrite(*sensorPortLetter*{:.text},*list*{:.list},*rxDataLength*{:.number})
: Writes low speed data to an input sensor on the robot. Assumes sensor type has been configured via SetInputMode.

{:id="NxtDirectCommands.MessageRead" class="method returns text"} <i/> MessageRead(*mailbox*{:.number})
: Read a message from a mailbox (1-10) on the robot.

{:id="NxtDirectCommands.MessageWrite" class="method"} <i/> MessageWrite(*mailbox*{:.number},*message*{:.text})
: Write a message to a mailbox (1-10) on the robot.

{:id="NxtDirectCommands.PlaySoundFile" class="method"} <i/> PlaySoundFile(*fileName*{:.text})
: Play a sound file on the robot.

{:id="NxtDirectCommands.PlayTone" class="method"} <i/> PlayTone(*frequencyHz*{:.number},*durationMs*{:.number})
: Make the robot play a tone.

{:id="NxtDirectCommands.ResetInputScaledValue" class="method"} <i/> ResetInputScaledValue(*sensorPortLetter*{:.text})
: Reset the scaled value of an input sensor on the robot.

{:id="NxtDirectCommands.ResetMotorPosition" class="method"} <i/> ResetMotorPosition(*motorPortLetter*{:.text},*relative*{:.boolean})
: Reset motor position.

{:id="NxtDirectCommands.SetBrickName" class="method"} <i/> SetBrickName(*name*{:.text})
: Set the brick name of the robot.

{:id="NxtDirectCommands.SetInputMode" class="method"} <i/> SetInputMode(*sensorPortLetter*{:.text},*sensorType*{:.number},*sensorMode*{:.number})
: Configure an input sensor on the robot.

{:id="NxtDirectCommands.SetOutputState" class="method"} <i/> SetOutputState(*motorPortLetter*{:.text},*power*{:.number},*mode*{:.number},*regulationMode*{:.number},*turnRatio*{:.number},*runState*{:.number},*tachoLimit*{:.number})
: Sets the output state of a motor on the robot.

{:id="NxtDirectCommands.StartProgram" class="method"} <i/> StartProgram(*programName*{:.text})
: Start execution of a previously downloaded program on the robot.

{:id="NxtDirectCommands.StopProgram" class="method"} <i/> StopProgram()
: Stop execution of the currently running program on the robot.

{:id="NxtDirectCommands.StopSoundPlayback" class="method"} <i/> StopSoundPlayback()
: Stop sound playback.

## NxtDrive  {#NxtDrive}

![NXT component icon](images/legoMindstormsNxt.png)

 A component that provides a high-level interface to a LEGO MINDSTORMS NXT
 robot, with functions that can move and turn the robot.



### Properties  {#NxtDrive-Properties}

{:.properties}

{:id="NxtDrive.BluetoothClient" .component .do} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer.**

{:id="NxtDrive.DriveMotors" .text .do} *DriveMotors*
: Specifies the motor ports that are used for driving.

{:id="NxtDrive.StopBeforeDisconnect" .boolean} *StopBeforeDisconnect*
: Specifies whether to stop the drive motors before disconnecting.

{:id="NxtDrive.WheelDiameter" .number .do} *WheelDiameter*
: Returns the diameter of the wheels used for driving.

### Events  {#NxtDrive-Events}

{:.events}
None


### Methods  {#NxtDrive-Methods}

{:.methods}

{:id="NxtDrive.MoveBackward" class="method"} <i/> MoveBackward(*power*{:.number},*distance*{:.number})
: Move the robot backward the given distance, with the specified percentage of maximum power, by powering both drive motors backward.

{:id="NxtDrive.MoveBackwardIndefinitely" class="method"} <i/> MoveBackwardIndefinitely(*power*{:.number})
: Move the robot backward indefinitely, with the specified percentage of maximum power, by powering both drive motors backward.

{:id="NxtDrive.MoveForward" class="method"} <i/> MoveForward(*power*{:.number},*distance*{:.number})
: Move the robot forward the given distance, with the specified percentage of maximum power, by powering both drive motors forward.

{:id="NxtDrive.MoveForwardIndefinitely" class="method"} <i/> MoveForwardIndefinitely(*power*{:.number})
: Move the robot forward indefinitely, with the specified percentage of maximum power, by powering both drive motors forward.

{:id="NxtDrive.Stop" class="method"} <i/> Stop()
: Stop the drive motors of the robot.

{:id="NxtDrive.TurnClockwiseIndefinitely" class="method"} <i/> TurnClockwiseIndefinitely(*power*{:.number})
: Turn the robot clockwise indefinitely, with the specified percentage of maximum power, by powering the left drive motor forward and the right drive motor backward.

{:id="NxtDrive.TurnCounterClockwiseIndefinitely" class="method"} <i/> TurnCounterClockwiseIndefinitely(*power*{:.number})
: Turn the robot counterclockwise indefinitely, with the specified percentage of maximum power, by powering the right drive motor forward and the left drive motor backward.

## NxtLightSensor  {#NxtLightSensor}

![NXT component icon](images/legoMindstormsNxt.png)

 A component that provides a high-level interface to a light sensor on a LEGO
 MINDSTORMS NXT robot.



### Properties  {#NxtLightSensor-Properties}

{:.properties}

{:id="NxtLightSensor.AboveRangeEventEnabled" .boolean} *AboveRangeEventEnabled*
: Specifies whether the AboveRange event should fire when the light level
 goes above the TopOfRange.

{:id="NxtLightSensor.BelowRangeEventEnabled" .boolean} *BelowRangeEventEnabled*
: Specifies whether the BelowRange event should fire when the light level
 goes below the BottomOfRange.

{:id="NxtLightSensor.BluetoothClient" .component .do} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer.**

{:id="NxtLightSensor.BottomOfRange" .number} *BottomOfRange*
: Specifies the bottom of the range used for the BelowRange, WithinRange,
 and AboveRange events.

{:id="NxtLightSensor.GenerateLight" .boolean} *GenerateLight*
: Specifies whether the light sensor should generate light.

{:id="NxtLightSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be specified in the Designer.**

{:id="NxtLightSensor.TopOfRange" .number} *TopOfRange*
: Specifies the top of the range used for the BelowRange, WithinRange, and
 AboveRange events.

{:id="NxtLightSensor.WithinRangeEventEnabled" .boolean} *WithinRangeEventEnabled*
: Specifies whether the WithinRange event should fire when the light level
 goes between the BottomOfRange and the TopOfRange.

### Events  {#NxtLightSensor-Events}

{:.events}

{:id="NxtLightSensor.AboveRange"} AboveRange()
: Light level has gone above the range.

{:id="NxtLightSensor.BelowRange"} BelowRange()
: Light level has gone below the range.

{:id="NxtLightSensor.WithinRange"} WithinRange()
: Light level has gone within the range.

### Methods  {#NxtLightSensor-Methods}

{:.methods}

{:id="NxtLightSensor.GetLightLevel" class="method returns number"} <i/> GetLightLevel()
: Returns the current light level as a value between 0 and 1023, or -1 if the light level can not be read.

## NxtSoundSensor  {#NxtSoundSensor}

![NXT component icon](images/legoMindstormsNxt.png)

 A component that provides a high-level interface to a sound sensor on a LEGO
 MINDSTORMS NXT robot.



### Properties  {#NxtSoundSensor-Properties}

{:.properties}

{:id="NxtSoundSensor.AboveRangeEventEnabled" .boolean} *AboveRangeEventEnabled*
: Specifies whether the AboveRange event should fire when the sound level
 goes above the TopOfRange.

{:id="NxtSoundSensor.BelowRangeEventEnabled" .boolean} *BelowRangeEventEnabled*
: Specifies whether the BelowRange event should fire when the sound level
 goes below the BottomOfRange.

{:id="NxtSoundSensor.BluetoothClient" .component .do} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer.**

{:id="NxtSoundSensor.BottomOfRange" .number} *BottomOfRange*
: Specifies the bottom of the range used for the BelowRange, WithinRange,
 and AboveRange events.

{:id="NxtSoundSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be set in the Designer.**

{:id="NxtSoundSensor.TopOfRange" .number} *TopOfRange*
: Specifies the top of the range used for the BelowRange, WithinRange, and
 AboveRange events.

{:id="NxtSoundSensor.WithinRangeEventEnabled" .boolean} *WithinRangeEventEnabled*
: Specifies whether the WithinRange event should fire when the sound level
 goes between the BottomOfRange and the TopOfRange.

### Events  {#NxtSoundSensor-Events}

{:.events}

{:id="NxtSoundSensor.AboveRange"} AboveRange()
: Sound level has gone above the range.

{:id="NxtSoundSensor.BelowRange"} BelowRange()
: Sound level has gone below the range.

{:id="NxtSoundSensor.WithinRange"} WithinRange()
: Sound level has gone within the range.

### Methods  {#NxtSoundSensor-Methods}

{:.methods}

{:id="NxtSoundSensor.GetSoundLevel" class="method returns number"} <i/> GetSoundLevel()
: Returns the current sound level as a value between 0 and 1023, or -1 if the sound level can not be read.

## NxtTouchSensor  {#NxtTouchSensor}

![NXT component icon](images/legoMindstormsNxt.png)

 A component that provides a high-level interface to a touch sensor on a LEGO
 MINDSTORMS NXT robot.



### Properties  {#NxtTouchSensor-Properties}

{:.properties}

{:id="NxtTouchSensor.BluetoothClient" .component .do} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer.**

{:id="NxtTouchSensor.PressedEventEnabled" .boolean} *PressedEventEnabled*
: Specifies whether the Pressed event should fire when the touch sensor is
 pressed.

{:id="NxtTouchSensor.ReleasedEventEnabled" .boolean} *ReleasedEventEnabled*
: Specifies whether the Released event should fire when the touch sensor is
 released.

{:id="NxtTouchSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be set in the Designer.**

### Events  {#NxtTouchSensor-Events}

{:.events}

{:id="NxtTouchSensor.Pressed"} Pressed()
: Touch sensor has been pressed.

{:id="NxtTouchSensor.Released"} Released()
: Touch sensor has been released.

### Methods  {#NxtTouchSensor-Methods}

{:.methods}

{:id="NxtTouchSensor.IsPressed" class="method returns boolean"} <i/> IsPressed()
: Returns true if the touch sensor is pressed.

## NxtUltrasonicSensor  {#NxtUltrasonicSensor}

![NXT component icon](images/legoMindstormsNxt.png)

 A component that provides a high-level interface to an ultrasonic sensor on a LEGO
 MINDSTORMS NXT robot.



### Properties  {#NxtUltrasonicSensor-Properties}

{:.properties}

{:id="NxtUltrasonicSensor.AboveRangeEventEnabled" .boolean} *AboveRangeEventEnabled*
: Specifies whether the AboveRange event should fire when the distance
 goes above the TopOfRange.

{:id="NxtUltrasonicSensor.BelowRangeEventEnabled" .boolean} *BelowRangeEventEnabled*
: Specifies whether the BelowRange event should fire when the distance
 goes below the BottomOfRange.

{:id="NxtUltrasonicSensor.BluetoothClient" .component .do} *BluetoothClient*
: Specifies the BluetoothClient component that should be used for communication.
 **Must be set in the Designer.**

{:id="NxtUltrasonicSensor.BottomOfRange" .number} *BottomOfRange*
: Specifies the bottom of the range used for the BelowRange, WithinRange,
 and AboveRange events.

{:id="NxtUltrasonicSensor.SensorPort" .text .do} *SensorPort*
: Specifies the sensor port that the sensor is connected to.
 **Must be set in the Designer.**

{:id="NxtUltrasonicSensor.TopOfRange" .number} *TopOfRange*
: Specifies the top of the range used for the BelowRange, WithinRange, and
 AboveRange events.

{:id="NxtUltrasonicSensor.WithinRangeEventEnabled" .boolean} *WithinRangeEventEnabled*
: Specifies whether the WithinRange event should fire when the distance
 goes between the BottomOfRange and the TopOfRange.

### Events  {#NxtUltrasonicSensor-Events}

{:.events}

{:id="NxtUltrasonicSensor.AboveRange"} AboveRange()
: Distance has gone above the range.

{:id="NxtUltrasonicSensor.BelowRange"} BelowRange()
: Distance has gone below the range.

{:id="NxtUltrasonicSensor.WithinRange"} WithinRange()
: Distance has gone within the range.

### Methods  {#NxtUltrasonicSensor-Methods}

{:.methods}

{:id="NxtUltrasonicSensor.GetDistance" class="method returns number"} <i/> GetDistance()
: Returns the current distance in centimeters as a value between 0 and 254, or -1 if the distance can not be read.
