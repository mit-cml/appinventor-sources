<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# OrientationSensor

Non-visible component providing information about the device's physical orientation in three dimensions:

*   **Roll**: 0 degrees when the device is level, increases to 90 degrees as the device is tilted up on its left side, and decreases to -90 degrees when the device is tilted up on its right side.
*   **Pitch**: 0 degrees when the device is level, up to 90 degrees as the device is tilted so its top is pointing down, up to 180 degrees as it gets turned over. Similarly, as the device is tilted so its bottom points down, pitch decreases to -90 degrees, then further decreases to -180 degrees as it gets turned all the way over.
*   **Azimuth**: 0 degrees when the top of the device is pointing north, 90 degrees when it is pointing east, 180 degrees when it is pointing south, 270 degrees when it is pointing west, etc.

These measurements assume that the device itself is not moving.

---

## Designer Properties

---

### Enabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Enabled    |   boolean   |      True     |

## Events

---

### OrientationChanged

<div block-type = "component_event" component-selector = "OrientationSensor" event-selector = "OrientationChanged" id = "orientationsensor-orientationchanged"></div>

Called when the orientation has changed.

| Param Name | IO Type |
| :--------: | :-----: |
|   azimuth  |  number |
|    pitch   |  number |
|    roll    |  number |

## Block Properties

---

### Angle

<div block-type = "component_set_get" component-selector = "OrientationSensor" property-selector = "Angle" property-type = "get" id = "get-orientationsensor-angle"></div>

Returns an angle that tells the direction in which the device is tiled. That is, it tells the direction of the force that would be felt by a ball rolling on the surface of the device.

| Param Name | IO Type |
| :--------: | :-----: |
|    Angle   |  number |

### Available

<div block-type = "component_set_get" component-selector = "OrientationSensor" property-selector = "Available" property-type = "get" id = "get-orientationsensor-available"></div>

Indicates whether the orientation sensor is present on the device.

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### Azimuth

<div block-type = "component_set_get" component-selector = "OrientationSensor" property-selector = "Azimuth" property-type = "get" id = "get-orientationsensor-azimuth"></div>

Returns the azimuth angle of the device. To return meaningful values the sensor must be enabled.

| Param Name | IO Type |
| :--------: | :-----: |
|   Azimuth  |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "OrientationSensor" property-selector = "Enabled" property-type = "get" id = "get-orientationsensor-enabled"></div>

<div block-type = "component_set_get" component-selector = "OrientationSensor" property-selector = "Enabled" property-type = "set" id = "set-orientationsensor-enabled"></div>

Specifies whether the orientation sensor is enabled.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### Magnitude

<div block-type = "component_set_get" component-selector = "OrientationSensor" property-selector = "Magnitude" property-type = "get" id = "get-orientationsensor-magnitude"></div>

Returns a number between 0 and 1 indicating how much the device is tilted. It gives the magnitude of the force that would be felt by a ball rolling on the surface of the device. For the angle of tilt, use

| Param Name | IO Type |
| :--------: | :-----: |
|  Magnitude |  number |

### Pitch

<div block-type = "component_set_get" component-selector = "OrientationSensor" property-selector = "Pitch" property-type = "get" id = "get-orientationsensor-pitch"></div>

Returns the pitch angle of the device. To return meaningful values the sensor must be enabled.

| Param Name | IO Type |
| :--------: | :-----: |
|    Pitch   |  number |

### Roll

<div block-type = "component_set_get" component-selector = "OrientationSensor" property-selector = "Roll" property-type = "get" id = "get-orientationsensor-roll"></div>

Returns the roll angle of the device. To return meaningful values the sensor must be enabled.

| Param Name | IO Type |
| :--------: | :-----: |
|    Roll    |  number |

## Component

---

### OrientationSensor

<div block-type = "component_component_block" component-selector = "OrientationSensor" id = "component-orientationsensor"></div>

Return Type : component

Component OrientationSensor

