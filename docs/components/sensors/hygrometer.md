# Hygrometer

A sensor component that can measure the relative ambient air humidity. Most Android devices do not have this sensor.

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

### HumidityChanged

<div block-type = "component_event" component-selector = "Hygrometer" event-selector = "HumidityChanged" event-params = "humidity" id = "hygrometer-humiditychanged"></div>

Called when a change is detected in the ambient air humidity (expressed as a percentage).

| Param Name | IO Type |
| :--------: | :-----: |
|  humidity  |  number |

## Block Properties

---

### Available

<div block-type = "component_set_get" component-selector = "Hygrometer" property-selector = "Available" property-type = "get" id = "get-hygrometer-available"></div>

Specifies whether or not the device has the hardware to support the Hygrometer component.

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### Enabled

<div block-type = "component_set_get" component-selector = "Hygrometer" property-selector = "Enabled" property-type = "get" id = "get-hygrometer-enabled"></div>

<div block-type = "component_set_get" component-selector = "Hygrometer" property-selector = "Enabled" property-type = "set" id = "set-hygrometer-enabled"></div>

If enabled, then device will listen for changes.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### Humidity

<div block-type = "component_set_get" component-selector = "Hygrometer" property-selector = "Humidity" property-type = "get" id = "get-hygrometer-humidity"></div>

The relative ambient humidity as a percentage, if the sensor is available and enabled.

| Param Name | IO Type |
| :--------: | :-----: |
|  Humidity  |  number |

### RefreshTime

<div block-type = "component_set_get" component-selector = "Hygrometer" property-selector = "RefreshTime" property-type = "get" id = "get-hygrometer-refreshtime"></div>

<div block-type = "component_set_get" component-selector = "Hygrometer" property-selector = "RefreshTime" property-type = "set" id = "set-hygrometer-refreshtime"></div>

The requested minimum time in milliseconds between changes in readings being reported. Android is not guaranteed to honor the request. Setting this property has no effect on pre-Gingerbread devices.

|  Param Name | IO Type |
| :---------: | :-----: |
| RefreshTime |  number |

## Component

---

### Hygrometer

<div block-type = "component_component_block" component-selector = "Hygrometer" id = "component-hygrometer"></div>

Return Type : component

Component Hygrometer

