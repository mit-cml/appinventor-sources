<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# AccelerometerSensor

Non-visible component that can detect shaking and measure acceleration approximately in three dimensions using SI units (m/s^2^). The components are:

*   **xAccel**: 0 when the phone is at rest on a flat surface, positive when the phone is tilted to the right (i.e., its left side is raised), and negative when the phone is tilted to the left (i.e., its right size is raised).
*   **yAccel**: 0 when the phone is at rest on a flat surface, positive when its bottom is raised, and negative when its top is raised.
*   **zAccel**: Equal to -9.8 (earth's gravity in meters per second per second when the device is at rest parallel to the ground with the display facing up, 0 when perpendicular to the ground, and +9.8 when facing down. The value can also be affected by accelerating it with or against gravity.

---

## Designer Properties

---

### Enabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Enabled    |   boolean   |      True     |

### LegacyMode

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   LegacyMode  |   boolean   |     False     |

### MinimumInterval

|  Property Name  |      Editor Type     | Default Value |
| :-------------: | :------------------: | :-----------: |
| MinimumInterval | non_negative_integer |      400      |

### Sensitivity

| Property Name |        Editor Type        | Default Value |
| :-----------: | :-----------------------: | :-----------: |
|  Sensitivity  | accelerometer_sensitivity |       2       |

## Events

---

### AccelerationChanged

<div block-type = "component_event" component-selector = "AccelerometerSensor" event-selector = "AccelerationChanged" id = "accelerometersensor-accelerationchanged"></div>

Indicates the acceleration changed in the X, Y, and/or Z dimensions.

| Param Name | IO Type |
| :--------: | :-----: |
|   xAccel   |  number |
|   yAccel   |  number |
|   zAccel   |  number |

### Shaking

<div block-type = "component_event" component-selector = "AccelerometerSensor" event-selector = "Shaking" id = "accelerometersensor-shaking"></div>

Indicates the device started being shaken or continues to be shaken.

## Block Properties

---

### Available

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "Available" property-type = "get" id = "get-accelerometersensor-available"></div>

Returns whether the accelerometer is available on the device.

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### Enabled

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "Enabled" property-type = "get" id = "get-accelerometersensor-enabled"></div>

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "Enabled" property-type = "set" id = "set-accelerometersensor-enabled"></div>

If true, the sensor will generate events. Otherwise, no events are generated even if the device is accelerated or shaken.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### MinimumInterval

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "MinimumInterval" property-type = "get" id = "get-accelerometersensor-minimuminterval"></div>

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "MinimumInterval" property-type = "set" id = "set-accelerometersensor-minimuminterval"></div>

The minimum interval, in milliseconds, between phone shakes

|    Param Name   | IO Type |
| :-------------: | :-----: |
| MinimumInterval |  number |

### Sensitivity

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "Sensitivity" property-type = "get" id = "get-accelerometersensor-sensitivity"></div>

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "Sensitivity" property-type = "set" id = "set-accelerometersensor-sensitivity"></div>

A number that encodes how sensitive the accelerometer is. The choices are: 1 = weak, 2 = moderate, 3 = strong.

|  Param Name | IO Type |
| :---------: | :-----: |
| Sensitivity |  number |

### XAccel

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "XAccel" property-type = "get" id = "get-accelerometersensor-xaccel"></div>

Returns the acceleration in the X-dimension in SI units (m/s²). The sensor must be enabled to return meaningful values.

| Param Name | IO Type |
| :--------: | :-----: |
|   XAccel   |  number |

### YAccel

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "YAccel" property-type = "get" id = "get-accelerometersensor-yaccel"></div>

Returns the acceleration in the Y-dimension in SI units (m/s²). The sensor must be enabled to return meaningful values.

| Param Name | IO Type |
| :--------: | :-----: |
|   YAccel   |  number |

### ZAccel

<div block-type = "component_set_get" component-selector = "AccelerometerSensor" property-selector = "ZAccel" property-type = "get" id = "get-accelerometersensor-zaccel"></div>

Returns the acceleration in the Z-dimension in SI units (m/s²). The sensor must be enabled to return meaningful values.

| Param Name | IO Type |
| :--------: | :-----: |
|   ZAccel   |  number |

## Component

---

### AccelerometerSensor

<div block-type = "component_component_block" component-selector = "AccelerometerSensor" id = "component-accelerometersensor"></div>

Return Type : component

Component AccelerometerSensor

