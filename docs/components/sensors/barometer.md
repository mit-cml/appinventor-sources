# Barometer

A sensor component that can measure the ambient air pressure.

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

### AirPressureChanged

<div block-type = "component_event" component-selector = "Barometer" event-selector = "AirPressureChanged" event-params = "pressure" id = "barometer-airpressurechanged"></div>

Called when a change is detected in the air pressure (provided in hPa).

| Param Name | IO Type |
| :--------: | :-----: |
|  pressure  |  number |

## Block Properties

---

### AirPressure

<div block-type = "component_set_get" component-selector = "Barometer" property-selector = "AirPressure" property-type = "get" id = "get-barometer-airpressure"></div>

The air pressure in hPa (millibar), if the sensor is available and enabled.

|  Param Name | IO Type |
| :---------: | :-----: |
| AirPressure |  number |

### Available

<div block-type = "component_set_get" component-selector = "Barometer" property-selector = "Available" property-type = "get" id = "get-barometer-available"></div>

Specifies whether or not the device has the hardware to support the Barometer component.

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### Enabled

<div block-type = "component_set_get" component-selector = "Barometer" property-selector = "Enabled" property-type = "get" id = "get-barometer-enabled"></div>

<div block-type = "component_set_get" component-selector = "Barometer" property-selector = "Enabled" property-type = "set" id = "set-barometer-enabled"></div>

If enabled, then device will listen for changes.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### RefreshTime

<div block-type = "component_set_get" component-selector = "Barometer" property-selector = "RefreshTime" property-type = "get" id = "get-barometer-refreshtime"></div>

<div block-type = "component_set_get" component-selector = "Barometer" property-selector = "RefreshTime" property-type = "set" id = "set-barometer-refreshtime"></div>

The requested minimum time in milliseconds between changes in readings being reported. Android is not guaranteed to honor the request. Setting this property has no effect on pre-Gingerbread devices.

|  Param Name | IO Type |
| :---------: | :-----: |
| RefreshTime |  number |

## Component

---

### Barometer

<div block-type = "component_component_block" component-selector = "Barometer" id = "component-barometer"></div>

Return Type : component

Component Barometer

