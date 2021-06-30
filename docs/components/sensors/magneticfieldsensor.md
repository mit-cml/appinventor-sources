<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# MagneticFieldSensor

Non-visible component that measures the ambient geomagnetic field for all three physical axes (x, y, z) in Tesla https://en.wikipedia.org/wiki/Tesla\_(unit).

---

## Designer Properties

---

### Enabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Enabled    |   boolean   |      True     |

## Events

---

### MagneticChanged

<div block-type = "component_event" component-selector = "MagneticFieldSensor" event-selector = "MagneticChanged" id = "magneticfieldsensor-magneticchanged"></div>

Triggers when magnetic field has changed, setting the new values in parameters.

|    Param Name    | IO Type |
| :--------------: | :-----: |
|     xStrength    |  number |
|     yStrength    |  number |
|     zStrength    |  number |
| absoluteStrength |  number |

## Block Properties

---

### AbsoluteStrength

<div block-type = "component_set_get" component-selector = "MagneticFieldSensor" property-selector = "AbsoluteStrength" property-type = "get" id = "get-magneticfieldsensor-absolutestrength"></div>

Indicates the absolute strength of the field.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| AbsoluteStrength |  number |

### Available

<div block-type = "component_set_get" component-selector = "MagneticFieldSensor" property-selector = "Available" property-type = "get" id = "get-magneticfieldsensor-available"></div>

Indicates that there is a magnetic field sensor in the device and it is available.

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### Enabled

<div block-type = "component_set_get" component-selector = "MagneticFieldSensor" property-selector = "Enabled" property-type = "get" id = "get-magneticfieldsensor-enabled"></div>

<div block-type = "component_set_get" component-selector = "MagneticFieldSensor" property-selector = "Enabled" property-type = "set" id = "set-magneticfieldsensor-enabled"></div>

Indicates whether or not the magnetic field sensor is enabled and working.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### MaximumRange

<div block-type = "component_set_get" component-selector = "MagneticFieldSensor" property-selector = "MaximumRange" property-type = "get" id = "get-magneticfieldsensor-maximumrange"></div>

Indicates the maximum range the magnetic sensor can reach.

|  Param Name  | IO Type |
| :----------: | :-----: |
| MaximumRange |  number |

### XStrength

<div block-type = "component_set_get" component-selector = "MagneticFieldSensor" property-selector = "XStrength" property-type = "get" id = "get-magneticfieldsensor-xstrength"></div>

Indicates the field's strength in the X-axis.

| Param Name | IO Type |
| :--------: | :-----: |
|  XStrength |  number |

### YStrength

<div block-type = "component_set_get" component-selector = "MagneticFieldSensor" property-selector = "YStrength" property-type = "get" id = "get-magneticfieldsensor-ystrength"></div>

Indicates the field's strength in the Y-axis.

| Param Name | IO Type |
| :--------: | :-----: |
|  YStrength |  number |

### ZStrength

<div block-type = "component_set_get" component-selector = "MagneticFieldSensor" property-selector = "ZStrength" property-type = "get" id = "get-magneticfieldsensor-zstrength"></div>

Indicates the field's strength in the Z-axis.

| Param Name | IO Type |
| :--------: | :-----: |
|  ZStrength |  number |

## Component

---

### MagneticFieldSensor

<div block-type = "component_component_block" component-selector = "MagneticFieldSensor" id = "component-magneticfieldsensor"></div>

Return Type : component

Component MagneticFieldSensor

