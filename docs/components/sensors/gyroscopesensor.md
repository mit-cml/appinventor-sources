# GyroscopeSensor

Non-visible component that can measure angular velocity in three dimensions in units of degrees per second.

In order to function, the component must have its `Enabled` property set to True, and the device must have a gyroscope sensor.

---

## Designer Properties

---

### Enabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Enabled    |   boolean   |      True     |

## Events

---

### GyroscopeChanged

<div block-type = "component_event" component-selector = "GyroscopeSensor" event-selector = "GyroscopeChanged" event-params = "xAngularVelocity-yAngularVelocity-zAngularVelocity-timestamp" id = "gyroscopesensor-gyroscopechanged"></div>

Indicates that the gyroscope sensor data has changed. The timestamp parameter is the time in nanoseconds at which the event occurred.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| xAngularVelocity |  number |
| yAngularVelocity |  number |
| zAngularVelocity |  number |
|     timestamp    |  number |

## Block Properties

---

### Available

<div block-type = "component_set_get" component-selector = "GyroscopeSensor" property-selector = "Available" property-type = "get" id = "get-gyroscopesensor-available"></div>

Indicates whether a gyroscope sensor is available.

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### Enabled

<div block-type = "component_set_get" component-selector = "GyroscopeSensor" property-selector = "Enabled" property-type = "get" id = "get-gyroscopesensor-enabled"></div>

<div block-type = "component_set_get" component-selector = "GyroscopeSensor" property-selector = "Enabled" property-type = "set" id = "set-gyroscopesensor-enabled"></div>

Enabled property getter method.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### XAngularVelocity

<div block-type = "component_set_get" component-selector = "GyroscopeSensor" property-selector = "XAngularVelocity" property-type = "get" id = "get-gyroscopesensor-xangularvelocity"></div>

The angular velocity around the X axis, in degrees per second.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| XAngularVelocity |  number |

### YAngularVelocity

<div block-type = "component_set_get" component-selector = "GyroscopeSensor" property-selector = "YAngularVelocity" property-type = "get" id = "get-gyroscopesensor-yangularvelocity"></div>

The angular velocity around the Y axis, in degrees per second.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| YAngularVelocity |  number |

### ZAngularVelocity

<div block-type = "component_set_get" component-selector = "GyroscopeSensor" property-selector = "ZAngularVelocity" property-type = "get" id = "get-gyroscopesensor-zangularvelocity"></div>

The angular velocity around the Z axis, in degrees per second.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| ZAngularVelocity |  number |

## Component

---

### GyroscopeSensor

<div block-type = "component_component_block" component-selector = "GyroscopeSensor" id = "component-gyroscopesensor"></div>

Return Type : component

Component GyroscopeSensor

