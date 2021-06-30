<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# LightSensor

A sensor component that can measure the light level.

---

## Designer Properties

---

### Enabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Enabled    |   boolean   |      True     |

### RefreshTime

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|  RefreshTime  | non_negative_integer |      1000     |

## Events

---

### LightChanged

<div block-type = "component_event" component-selector = "LightSensor" event-selector = "LightChanged" id = "lightsensor-lightchanged"></div>

Called when a change is detected in the light level.

| Param Name | IO Type |
| :--------: | :-----: |
|     lux    |  number |

## Block Properties

---

### Available

<div block-type = "component_set_get" component-selector = "LightSensor" property-selector = "Available" property-type = "get" id = "get-lightsensor-available"></div>

Specifies whether or not the device has the hardware to support the LightSensor component.

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### AverageLux

<div block-type = "component_set_get" component-selector = "LightSensor" property-selector = "AverageLux" property-type = "get" id = "get-lightsensor-averagelux"></div>

The average of the 10 most recent light levels measured, in lux.

| Param Name | IO Type |
| :--------: | :-----: |
| AverageLux |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "LightSensor" property-selector = "Enabled" property-type = "get" id = "get-lightsensor-enabled"></div>

<div block-type = "component_set_get" component-selector = "LightSensor" property-selector = "Enabled" property-type = "set" id = "set-lightsensor-enabled"></div>

If enabled, then device will listen for changes.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### Lux

<div block-type = "component_set_get" component-selector = "LightSensor" property-selector = "Lux" property-type = "get" id = "get-lightsensor-lux"></div>

The most recent light level, in lux, if the sensor is available and enabled.

| Param Name | IO Type |
| :--------: | :-----: |
|     Lux    |  number |

### RefreshTime

<div block-type = "component_set_get" component-selector = "LightSensor" property-selector = "RefreshTime" property-type = "get" id = "get-lightsensor-refreshtime"></div>

<div block-type = "component_set_get" component-selector = "LightSensor" property-selector = "RefreshTime" property-type = "set" id = "set-lightsensor-refreshtime"></div>

The requested minimum time in milliseconds between changes in readings being reported. Android is not guaranteed to honor the request. Setting this property has no effect on pre-Gingerbread devices.

|  Param Name | IO Type |
| :---------: | :-----: |
| RefreshTime |  number |

## Component

---

### LightSensor

<div block-type = "component_component_block" component-selector = "LightSensor" id = "component-lightsensor"></div>

Return Type : component

Component LightSensor

