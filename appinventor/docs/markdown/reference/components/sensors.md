---
layout: documentation
title: Sensors
---

[&laquo; Back to index](index.html)
# Sensors

Table of Contents:

* [AccelerometerSensor](#AccelerometerSensor)
* [BarcodeScanner](#BarcodeScanner)
* [Barometer](#Barometer)
* [Clock](#Clock)
* [GyroscopeSensor](#GyroscopeSensor)
* [Hygrometer](#Hygrometer)
* [LightSensor](#LightSensor)
* [LocationSensor](#LocationSensor)
* [NearField](#NearField)
* [OrientationSensor](#OrientationSensor)
* [Pedometer](#Pedometer)
* [ProximitySensor](#ProximitySensor)
* [Thermometer](#Thermometer)

## AccelerometerSensor  {#AccelerometerSensor}

### Properties  {#AccelerometerSensor-Properties}

{:.properties}

{:id="AccelerometerSensor.Available" .boolean .ro .bo} *Available*
: Available property getter method (read-only property).

{:id="AccelerometerSensor.Enabled" .boolean} *Enabled*
: If true, the sensor will generate events.  Otherwise, no events
 are generated even if the device is accelerated or shaken.

{:id="AccelerometerSensor.LegacyMode" .boolean .wo .do} *LegacyMode*
: Prior to the release that added this property the AccelerometerSensor component passed through sensor values directly as received from the Android system. However these values do not compensate for tablets that default to Landscape mode, requiring the MIT App Inventor programmer to compensate. However compensating would result in incorrect results in Portrait mode devices such as phones. We now detect Landscape mode tablets and perform the compensation. However if your project is already compensating for the change, you will now get incorrect results. Although our preferred solution is for you to update your project, you can also just set this property to “true” and our compensation code will be deactivated. Note: We recommend that you update your project as we may remove this property in a future release.

{:id="AccelerometerSensor.MinimumInterval" .number} *MinimumInterval*
: The minimum interval, in milliseconds, between phone shakes

{:id="AccelerometerSensor.Sensitivity" .number} *Sensitivity*
: A number that encodes how sensitive the accelerometer is. The choices are: 1 = weak, 2 = moderate,  3 = strong.

{:id="AccelerometerSensor.XAccel" .number .ro .bo} *XAccel*
: Returns the acceleration in the X-dimension in SI units (m/s^2).
 The sensor must be enabled to return meaningful values.

{:id="AccelerometerSensor.YAccel" .number .ro .bo} *YAccel*
: Returns the acceleration in the Y-dimension in SI units (m/s^2).
 The sensor must be enabled to return meaningful values.

{:id="AccelerometerSensor.ZAccel" .number .ro .bo} *ZAccel*
: Returns the acceleration in the Z-dimension in SI units (m/s^2).
 The sensor must be enabled to return meaningful values.

### Events  {#AccelerometerSensor-Events}

{:.events}

{:id="AccelerometerSensor.AccelerationChanged"} AccelerationChanged(*xAccel*{:.number},*yAccel*{:.number},*zAccel*{:.number})
: Indicates the acceleration changed in the X, Y, and/or Z dimensions.

{:id="AccelerometerSensor.Shaking"} Shaking()
: Indicates the device started being shaken or continues to be shaken.

## BarcodeScanner  {#BarcodeScanner}

### Properties  {#BarcodeScanner-Properties}

{:.properties}

{:id="BarcodeScanner.Result" .text .ro .bo} *Result*
: Text result of the previous scan.

{:id="BarcodeScanner.UseExternalScanner" .boolean} *UseExternalScanner*
: If true App Inventor will look for and use an external scanning program such as "Bar Code Scanner."

### Events  {#BarcodeScanner-Events}

{:.events}

{:id="BarcodeScanner.AfterScan"} AfterScan(*result*{:.text})
: Indicates that the scanner has read a (text) result and provides the result

### Methods  {#BarcodeScanner-Methods}

{:.methods}

{:id="BarcodeScanner.DoScan" class="method"} <i/> DoScan()
: Begins a barcode scan, using the camera. When the scan is complete, the AfterScan event will be raised.

## Barometer  {#Barometer}

### Properties  {#Barometer-Properties}

{:.properties}

{:id="Barometer.AirPressure" .number .ro .bo} *AirPressure*
: The air pressure in hPa (millibar), if the sensor is available and enabled.

{:id="Barometer.Available" .boolean .ro .bo} *Available*
: Specifies whether or not the device has the hardware to support the Barometer component.

{:id="Barometer.Enabled" .boolean} *Enabled*
: If enabled, then device will listen for changes.

{:id="Barometer.RefreshTime" .number} *RefreshTime*
: The requested minimum time in milliseconds between changes in readings being reported. Android is not guaranteed to honor the request. Setting this property has no effect on pre-Gingerbread devices.

### Events  {#Barometer-Events}

{:.events}

{:id="Barometer.AirPressureChanged"} AirPressureChanged(*pressure*{:.number})
: Called when a change is detected in the air pressure (provided in hPa).

## Clock  {#Clock}

### Properties  {#Clock-Properties}

{:.properties}

{:id="Clock.TimerAlwaysFires" .boolean} *TimerAlwaysFires*
: Will fire even when application is not showing on the screen if true

{:id="Clock.TimerEnabled" .boolean} *TimerEnabled*
: Fires timer if true

{:id="Clock.TimerInterval" .number} *TimerInterval*
: Interval between timer events in ms

### Events  {#Clock-Events}

{:.events}

{:id="Clock.Timer"} Timer()
: Timer has gone off.

### Methods  {#Clock-Methods}

{:.methods}

{:id="Clock.AddDays" class="method returns InstantInTime"} <i/> AddDays(*instant*{:.InstantInTime},*quantity*{:.number})
: An instant in time some days after the argument

{:id="Clock.AddDuration" class="method returns InstantInTime"} <i/> AddDuration(*instant*{:.InstantInTime},*quantity*{:.number})
: An instant in time some duration after the argument

{:id="Clock.AddHours" class="method returns InstantInTime"} <i/> AddHours(*instant*{:.InstantInTime},*quantity*{:.number})
: An instant in time some hours after the argument

{:id="Clock.AddMinutes" class="method returns InstantInTime"} <i/> AddMinutes(*instant*{:.InstantInTime},*quantity*{:.number})
: An instant in time some minutes after the argument

{:id="Clock.AddMonths" class="method returns InstantInTime"} <i/> AddMonths(*instant*{:.InstantInTime},*quantity*{:.number})
: An instant in time some months after the argument

{:id="Clock.AddSeconds" class="method returns InstantInTime"} <i/> AddSeconds(*instant*{:.InstantInTime},*quantity*{:.number})
: An instant in time some seconds after the argument

{:id="Clock.AddWeeks" class="method returns InstantInTime"} <i/> AddWeeks(*instant*{:.InstantInTime},*quantity*{:.number})
: An instant in time some weeks after the argument

{:id="Clock.AddYears" class="method returns InstantInTime"} <i/> AddYears(*instant*{:.InstantInTime},*quantity*{:.number})
: An instant in time some years after the argument

{:id="Clock.DayOfMonth" class="method returns number"} <i/> DayOfMonth(*instant*{:.InstantInTime})
: The day of the month

{:id="Clock.Duration" class="method returns number"} <i/> Duration(*start*{:.InstantInTime},*end*{:.InstantInTime})
: Milliseconds elapsed between instants

{:id="Clock.DurationToDays" class="method returns number"} <i/> DurationToDays(*duration*{:.number})
: convert duration to days

{:id="Clock.DurationToHours" class="method returns number"} <i/> DurationToHours(*duration*{:.number})
: convert duration to hours

{:id="Clock.DurationToMinutes" class="method returns number"} <i/> DurationToMinutes(*duration*{:.number})
: convert duration to minutes

{:id="Clock.DurationToSeconds" class="method returns number"} <i/> DurationToSeconds(*duration*{:.number})
: convert duration to seconds

{:id="Clock.DurationToWeeks" class="method returns number"} <i/> DurationToWeeks(*duration*{:.number})
: convert duration to weeks

{:id="Clock.FormatDate" class="method returns text"} <i/> FormatDate(*instant*{:.InstantInTime},*pattern*{:.text})
: Text representing the date of an instant in the specified pattern

{:id="Clock.FormatDateTime" class="method returns text"} <i/> FormatDateTime(*instant*{:.InstantInTime},*pattern*{:.text})
: Text representing the date and time of an instant in the specified pattern

{:id="Clock.FormatTime" class="method returns text"} <i/> FormatTime(*instant*{:.InstantInTime})
: Text representing the time of an instant

{:id="Clock.GetMillis" class="method returns number"} <i/> GetMillis(*instant*{:.InstantInTime})
: The instant in time measured as milliseconds since 1970.

{:id="Clock.Hour" class="method returns number"} <i/> Hour(*instant*{:.InstantInTime})
: The hour of the day

{:id="Clock.MakeDate" class="method returns InstantInTime"} <i/> MakeDate(*year*{:.number},*month*{:.number},*day*{:.number})
: Allows the user to set the clock to be a date value.
Valid values for the month field are 1-12 and 1-31 for the day field.

{:id="Clock.MakeInstant" class="method returns InstantInTime"} <i/> MakeInstant(*from*{:.text})
: An instant in time specified by MM/dd/YYYY hh:mm:ss or MM/dd/YYYY or hh:mm

{:id="Clock.MakeInstantFromMillis" class="method returns InstantInTime"} <i/> MakeInstantFromMillis(*millis*{:.number})
: An instant in time specified by the milliseconds since 1970.

{:id="Clock.MakeInstantFromParts" class="method returns InstantInTime"} <i/> MakeInstantFromParts(*year*{:.number},*month*{:.number},*day*{:.number},*hour*{:.number},*minute*{:.number},*second*{:.number})
: Allows the user to set the date and time to be displayed when the clock opens.
Valid values for the month field are 1-12 and 1-31 for the day field.

{:id="Clock.MakeTime" class="method returns InstantInTime"} <i/> MakeTime(*hour*{:.number},*minute*{:.number},*second*{:.number})
: Allows the user to set the time of the clock - Valid format is hh:mm:ss

{:id="Clock.Minute" class="method returns number"} <i/> Minute(*instant*{:.InstantInTime})
: The minute of the hour

{:id="Clock.Month" class="method returns number"} <i/> Month(*instant*{:.InstantInTime})
: The month of the year represented as a number from 1 to 12)

{:id="Clock.MonthName" class="method returns text"} <i/> MonthName(*instant*{:.InstantInTime})
: The name of the month

{:id="Clock.Now" class="method returns InstantInTime"} <i/> Now()
: The current instant in time read from phone's clock

{:id="Clock.Second" class="method returns number"} <i/> Second(*instant*{:.InstantInTime})
: The second of the minute

{:id="Clock.SystemTime" class="method returns number"} <i/> SystemTime()
: The phone's internal time

{:id="Clock.Weekday" class="method returns number"} <i/> Weekday(*instant*{:.InstantInTime})
: The day of the week represented as a number from 1 (Sunday) to 7 (Saturday)

{:id="Clock.WeekdayName" class="method returns text"} <i/> WeekdayName(*instant*{:.InstantInTime})
: The name of the day of the week

{:id="Clock.Year" class="method returns number"} <i/> Year(*instant*{:.InstantInTime})
: The year

## GyroscopeSensor  {#GyroscopeSensor}

### Properties  {#GyroscopeSensor-Properties}

{:.properties}

{:id="GyroscopeSensor.Available" .boolean .ro .bo} *Available*
: Indicates whether a gyroscope sensor is available.

{:id="GyroscopeSensor.Enabled" .boolean} *Enabled*
: Enabled property getter method.

{:id="GyroscopeSensor.XAngularVelocity" .number .ro .bo} *XAngularVelocity*
: The angular velocity around the X axis, in degrees per second.

{:id="GyroscopeSensor.YAngularVelocity" .number .ro .bo} *YAngularVelocity*
: The angular velocity around the Y axis, in degrees per second.

{:id="GyroscopeSensor.ZAngularVelocity" .number .ro .bo} *ZAngularVelocity*
: The angular velocity around the Z axis, in degrees per second.

### Events  {#GyroscopeSensor-Events}

{:.events}

{:id="GyroscopeSensor.GyroscopeChanged"} GyroscopeChanged(*xAngularVelocity*{:.number},*yAngularVelocity*{:.number},*zAngularVelocity*{:.number},*timestamp*{:.number})
: Indicates that the gyroscope sensor data has changed. The timestamp parameter is the time in nanoseconds at which the event occurred.

## Hygrometer  {#Hygrometer}

### Properties  {#Hygrometer-Properties}

{:.properties}

{:id="Hygrometer.Available" .boolean .ro .bo} *Available*
: Specifies whether or not the device has the hardware to support the Hygrometer component.

{:id="Hygrometer.Enabled" .boolean} *Enabled*
: If enabled, then device will listen for changes.

{:id="Hygrometer.Humidity" .number .ro .bo} *Humidity*
: The relative ambient humidity as a percentage, if the sensor is available and enabled.

{:id="Hygrometer.RefreshTime" .number} *RefreshTime*
: The requested minimum time in milliseconds between changes in readings being reported. Android is not guaranteed to honor the request. Setting this property has no effect on pre-Gingerbread devices.

### Events  {#Hygrometer-Events}

{:.events}

{:id="Hygrometer.HumidityChanged"} HumidityChanged(*humidity*{:.number})
: Called when a change is detected in the ambient air humidity (expressed as a percentage).

## LightSensor  {#LightSensor}

### Properties  {#LightSensor-Properties}

{:.properties}

{:id="LightSensor.Available" .boolean .ro .bo} *Available*
: Specifies whether or not the device has the hardware to support the LightSensor component.

{:id="LightSensor.AverageLux" .number .ro .bo} *AverageLux*
: The average of the 10 most recent light levels measured, in lux.

{:id="LightSensor.Enabled" .boolean} *Enabled*
: If enabled, then device will listen for changes.

{:id="LightSensor.Lux" .number .ro .bo} *Lux*
: The most recent light level, in lux, if the sensor is available and enabled.

{:id="LightSensor.RefreshTime" .number} *RefreshTime*
: The requested minimum time in milliseconds between changes in readings being reported. Android is not guaranteed to honor the request. Setting this property has no effect on pre-Gingerbread devices.

### Events  {#LightSensor-Events}

{:.events}

{:id="LightSensor.LightChanged"} LightChanged(*lux*{:.number})
: Called when a change is detected in the light level.

## LocationSensor  {#LocationSensor}

### Properties  {#LocationSensor-Properties}

{:.properties}

{:id="LocationSensor.Accuracy" .number .ro .bo} *Accuracy*
: The most recent measure of accuracy, in meters.  If no value is available,
 0 will be returned.

{:id="LocationSensor.Altitude" .number .ro .bo} *Altitude*
: The most recently available altitude value, in meters.  If no value is
 available, 0 will be returned.

{:id="LocationSensor.AvailableProviders" .list .ro .bo} *AvailableProviders*
: 

{:id="LocationSensor.CurrentAddress" .text .ro .bo} *CurrentAddress*
: Provides a textual representation of the current address or
 "No address available".

{:id="LocationSensor.DistanceInterval" .number} *DistanceInterval*
: Determines the minimum distance interval, in meters, that the sensor will try to use for sending out location updates. For example, if this is set to 5, then the sensor will fire a LocationChanged event only after 5 meters have been traversed. However, the sensor does not guarantee that an update will be received at exactly the distance interval. It may take more than 5 meters to fire an event, for instance.

{:id="LocationSensor.Enabled" .boolean} *Enabled*
: Indicates whether the user has specified that the sensor should
 listen for location changes and raise the corresponding events.

{:id="LocationSensor.HasAccuracy" .boolean .ro .bo} *HasAccuracy*
: Indicates whether information about location accuracy is available.

{:id="LocationSensor.HasAltitude" .boolean .ro .bo} *HasAltitude*
: Indicates whether altitude information is available.

{:id="LocationSensor.HasLongitudeLatitude" .boolean .ro .bo} *HasLongitudeLatitude*
: Indicates whether longitude and latitude information is available.  (It is
 always the case that either both or neither are.)

{:id="LocationSensor.Latitude" .number .ro .bo} *Latitude*
: The most recently available latitude value.  If no value is available,
 0 will be returned.

{:id="LocationSensor.Longitude" .number .ro .bo} *Longitude*
: The most recent available longitude value.  If no value is available,
 0 will be returned.

{:id="LocationSensor.ProviderLocked" .boolean .bo} *ProviderLocked*
: Indicates whether the sensor should allow the developer to
 manually change the provider (GPS, GSM, Wifi, etc.)
 from which location updates are received.

{:id="LocationSensor.ProviderName" .text .bo} *ProviderName*
: Indicates the source of the location information.  If there is no provider, the
 string "NO PROVIDER" is returned.  This is useful primarily for debugging.

{:id="LocationSensor.TimeInterval" .number} *TimeInterval*
: Determines the minimum time interval, in milliseconds, that the sensor will try to use for sending out location updates. However, location updates will only be received when the location of the phone actually changes, and use of the specified time interval is not guaranteed. For example, if 1000 is used as the time interval, location updates will never be fired sooner than 1000ms, but they may be fired anytime after.

### Events  {#LocationSensor-Events}

{:.events}

{:id="LocationSensor.LocationChanged"} LocationChanged(*latitude*{:.number},*longitude*{:.number},*altitude*{:.number},*speed*{:.number})
: Indicates that a new location has been detected.

{:id="LocationSensor.StatusChanged"} StatusChanged(*provider*{:.text},*status*{:.text})
: Indicates that the status of the location provider service has changed, such as when a
 provider is lost or a new provider starts being used.

### Methods  {#LocationSensor-Methods}

{:.methods}

{:id="LocationSensor.LatitudeFromAddress" class="method returns number"} <i/> LatitudeFromAddress(*locationName*{:.text})
: Derives latitude of given address

{:id="LocationSensor.LongitudeFromAddress" class="method returns number"} <i/> LongitudeFromAddress(*locationName*{:.text})
: Derives longitude of given address

## NearField  {#NearField}

### Properties  {#NearField-Properties}

{:.properties}

{:id="NearField.LastMessage" .text .ro .bo} *LastMessage*
: Returns the content of the most recently received tag.

{:id="NearField.ReadMode" .boolean} *ReadMode*
: Returns true if in read mode, false if in write mode.

{:id="NearField.TextToWrite" .text .bo} *TextToWrite*
: Returns the content of the text that's going to be written to the tag.

{:id="NearField.WriteType" .number .ro .bo} *WriteType*
: 

### Events  {#NearField-Events}

{:.events}

{:id="NearField.TagRead"} TagRead(*message*{:.text})
: Indicates that a new tag has been detected.
 Currently this is only a plain text tag, as specified in the
 manifest.  See Compiler.java.

{:id="NearField.TagWritten"} TagWritten()
: Event for TagWritten

## OrientationSensor  {#OrientationSensor}

### Properties  {#OrientationSensor-Properties}

{:.properties}

{:id="OrientationSensor.Angle" .number .ro .bo} *Angle*
: <p>Angle property getter method (read-only property).  Specifically, this
 provides the angle in which the orientation sensor is tilted, treating

{:id="OrientationSensor.Available" .boolean .ro .bo} *Available*
: Available property getter method (read-only property).

{:id="OrientationSensor.Azimuth" .number .ro .bo} *Azimuth*
: Azimuth property getter method (read-only property).

 <p>To return meaningful values the sensor must be enabled.</p>

{:id="OrientationSensor.Enabled" .boolean} *Enabled*
: Enabled property getter method.

{:id="OrientationSensor.Magnitude" .number .ro .bo} *Magnitude*
: Magnitude property getter method (read-only property).  Specifically, this
 returns a number between 0 and 1, indicating how much the device
 is tilted.  For the angle of tilt, use

{:id="OrientationSensor.Pitch" .number .ro .bo} *Pitch*
: Pitch property getter method (read-only property).

 <p>To return meaningful values the sensor must be enabled.</p>

{:id="OrientationSensor.Roll" .number .ro .bo} *Roll*
: Roll property getter method (read-only property).

 <p>To return meaningful values the sensor must be enabled.</p>

### Events  {#OrientationSensor-Events}

{:.events}

{:id="OrientationSensor.OrientationChanged"} OrientationChanged(*azimuth*{:.number},*pitch*{:.number},*roll*{:.number})
: Default OrientationChanged event handler.

 <p>This event is signalled when the device's orientation has changed.  It
 reports the new values of azimuth, pich, and roll, and it also sets the Azimuth, Pitch,
 and roll properties.</p>
 <p>Azimuth is the compass heading in degrees, pitch indicates how the device
 is tilted from top to bottom, and roll indicates how much the device is tilted from
 side to side.</p>

## Pedometer  {#Pedometer}

### Properties  {#Pedometer-Properties}

{:.properties}

{:id="Pedometer.CalibrateStrideLength" .boolean .bo} *CalibrateStrideLength*
: 

{:id="Pedometer.Distance" .number .ro .bo} *Distance*
: The approximate distance traveled in meters.

{:id="Pedometer.ElapsedTime" .number .ro .bo} *ElapsedTime*
: Time elapsed in milliseconds since the pedometer was started.

{:id="Pedometer.Moving" .boolean .ro .bo} *Moving*
: 

{:id="Pedometer.SimpleSteps" .number .ro .bo} *SimpleSteps*
: The number of simple steps taken since the pedometer has started.

{:id="Pedometer.StopDetectionTimeout" .number} *StopDetectionTimeout*
: The duration in milliseconds of idleness (no steps detected) after which to go into a "stopped" state

{:id="Pedometer.StrideLength" .number} *StrideLength*
: Set the average stride length in meters.

{:id="Pedometer.UseGPS" .boolean .wo .bo} *UseGPS*
: 

{:id="Pedometer.WalkSteps" .number .ro .bo} *WalkSteps*
: the number of walk steps taken since the pedometer has started.

### Events  {#Pedometer-Events}

{:.events}

{:id="Pedometer.CalibrationFailed"} CalibrationFailed()
: Event for CalibrationFailed

{:id="Pedometer.GPSAvailable"} GPSAvailable()
: Event for GPSAvailable

{:id="Pedometer.GPSLost"} GPSLost()
: Event for GPSLost

{:id="Pedometer.SimpleStep"} SimpleStep(*simpleSteps*{:.number},*distance*{:.number})
: This event is run when a raw step is detected

{:id="Pedometer.StartedMoving"} StartedMoving()
: Event for StartedMoving

{:id="Pedometer.StoppedMoving"} StoppedMoving()
: Event for StoppedMoving

{:id="Pedometer.WalkStep"} WalkStep(*walkSteps*{:.number},*distance*{:.number})
: This event is run when a walking step is detected. A walking step is a step that appears to be involved in forward motion.

### Methods  {#Pedometer-Methods}

{:.methods}

{:id="Pedometer.Pause" class="method"} <i/> Pause()
: Pause counting of steps and distance.

{:id="Pedometer.Reset" class="method"} <i/> Reset()
: Resets the step counter, distance measure and time running.

{:id="Pedometer.Resume" class="method"} <i/> Resume()
: Resumes counting, synonym of Start.

{:id="Pedometer.Save" class="method"} <i/> Save()
: Saves the pedometer state to the phone. Permits permits accumulation of steps and distance between invocations of an App that uses the pedometer. Different Apps will have their own saved state.

{:id="Pedometer.Start" class="method"} <i/> Start()
: Start counting steps

{:id="Pedometer.Stop" class="method"} <i/> Stop()
: Stop counting steps

## ProximitySensor  {#ProximitySensor}

### Properties  {#ProximitySensor-Properties}

{:.properties}

{:id="ProximitySensor.Available" .boolean .ro .bo} *Available*
: Reports whether or not the device has a proximity sensor

{:id="ProximitySensor.Distance" .number .ro .bo} *Distance*
: Returns the distance from the object to the device

{:id="ProximitySensor.Enabled" .boolean} *Enabled*
: If true, the sensor will generate events.  Otherwise, no events
 are generated .

{:id="ProximitySensor.KeepRunningWhenOnPause" .boolean} *KeepRunningWhenOnPause*
: Returns value of keepRunningWhenOnPause

{:id="ProximitySensor.MaximumRange" .number .ro .bo} *MaximumRange*
: Reports the Maximum Range of the device's ProximitySensor

### Events  {#ProximitySensor-Events}

{:.events}

{:id="ProximitySensor.ProximityChanged"} ProximityChanged(*distance*{:.number})
: Triggered when distance (in cm) of the object to the device changes.

## Thermometer  {#Thermometer}

### Properties  {#Thermometer-Properties}

{:.properties}

{:id="Thermometer.Available" .boolean .ro .bo} *Available*
: Specifies whether or not the device has the hardware to support the Thermometer component.

{:id="Thermometer.Enabled" .boolean} *Enabled*
: If enabled, then device will listen for changes.

{:id="Thermometer.RefreshTime" .number} *RefreshTime*
: The requested minimum time in milliseconds between changes in readings being reported. Android is not guaranteed to honor the request. Setting this property has no effect on pre-Gingerbread devices.

{:id="Thermometer.Temperature" .number .ro .bo} *Temperature*
: The temperature in degrees Celsius, if the sensor is available and enabled

### Events  {#Thermometer-Events}

{:.events}

{:id="Thermometer.TemperatureChanged"} TemperatureChanged(*temperature*{:.number})
: Called when a change is detected in the temperature (in degrees Celsius).