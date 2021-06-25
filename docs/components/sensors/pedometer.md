# Pedometer

A Component that acts like a Pedometer. It senses motion via the Accelerometer and attempts to determine if a step has been taken. Using a configurable stride length, it can estimate the distance traveled as well.

---

## Designer Properties

---

### StopDetectionTimeout

|     Property Name    |      Editor Type     | Default Value |
| :------------------: | :------------------: | :-----------: |
| StopDetectionTimeout | non_negative_integer |      2000     |

### StrideLength

| Property Name |     Editor Type    | Default Value |
| :-----------: | :----------------: | :-----------: |
|  StrideLength | non_negative_float |      0.73     |

## Events

---

### SimpleStep

<div block-type = "component_event" component-selector = "Pedometer" event-selector = "SimpleStep" event-params = "simpleSteps-distance" id = "pedometer-simplestep"></div>

This event is run when a raw step is detected.

|  Param Name | IO Type |
| :---------: | :-----: |
| simpleSteps |  number |
|   distance  |  number |

### WalkStep

<div block-type = "component_event" component-selector = "Pedometer" event-selector = "WalkStep" event-params = "walkSteps-distance" id = "pedometer-walkstep"></div>

This event is run when a walking step is detected. A walking step is a step that appears to be involved in forward motion.

| Param Name | IO Type |
| :--------: | :-----: |
|  walkSteps |  number |
|  distance  |  number |

## Methods

---

### Reset

<div block-type = "component_method" component-selector = "Pedometer" method-selector = "Reset" method-params = "" return-type = "undefined" id = "pedometer-reset"></div>

Return Type : No Return Value

Resets the step counter, distance measure and time running.

### Save

<div block-type = "component_method" component-selector = "Pedometer" method-selector = "Save" method-params = "" return-type = "undefined" id = "pedometer-save"></div>

Return Type : No Return Value

Saves the pedometer state to the phone. Permits permits accumulation of steps and distance between invocations of an App that uses the pedometer. Different Apps will have their own saved state.

### Start

<div block-type = "component_method" component-selector = "Pedometer" method-selector = "Start" method-params = "" return-type = "undefined" id = "pedometer-start"></div>

Return Type : No Return Value

Start counting steps

### Stop

<div block-type = "component_method" component-selector = "Pedometer" method-selector = "Stop" method-params = "" return-type = "undefined" id = "pedometer-stop"></div>

Return Type : No Return Value

Stop counting steps

## Block Properties

---

### Distance

<div block-type = "component_set_get" component-selector = "Pedometer" property-selector = "Distance" property-type = "get" id = "get-pedometer-distance"></div>

The approximate distance traveled in meters.

| Param Name | IO Type |
| :--------: | :-----: |
|  Distance  |  number |

### ElapsedTime

<div block-type = "component_set_get" component-selector = "Pedometer" property-selector = "ElapsedTime" property-type = "get" id = "get-pedometer-elapsedtime"></div>

Time elapsed in milliseconds since the pedometer was started.

|  Param Name | IO Type |
| :---------: | :-----: |
| ElapsedTime |  number |

### SimpleSteps

<div block-type = "component_set_get" component-selector = "Pedometer" property-selector = "SimpleSteps" property-type = "get" id = "get-pedometer-simplesteps"></div>

The number of simple steps taken since the pedometer has started.

|  Param Name | IO Type |
| :---------: | :-----: |
| SimpleSteps |  number |

### StopDetectionTimeout

<div block-type = "component_set_get" component-selector = "Pedometer" property-selector = "StopDetectionTimeout" property-type = "get" id = "get-pedometer-stopdetectiontimeout"></div>

<div block-type = "component_set_get" component-selector = "Pedometer" property-selector = "StopDetectionTimeout" property-type = "set" id = "set-pedometer-stopdetectiontimeout"></div>

The duration in milliseconds of idleness (no steps detected) after which to go into a "stopped" state

|      Param Name      | IO Type |
| :------------------: | :-----: |
| StopDetectionTimeout |  number |

### StrideLength

<div block-type = "component_set_get" component-selector = "Pedometer" property-selector = "StrideLength" property-type = "get" id = "get-pedometer-stridelength"></div>

<div block-type = "component_set_get" component-selector = "Pedometer" property-selector = "StrideLength" property-type = "set" id = "set-pedometer-stridelength"></div>

Set the average stride length in meters.

|  Param Name  | IO Type |
| :----------: | :-----: |
| StrideLength |  number |

### WalkSteps

<div block-type = "component_set_get" component-selector = "Pedometer" property-selector = "WalkSteps" property-type = "get" id = "get-pedometer-walksteps"></div>

the number of walk steps taken since the pedometer has started.

| Param Name | IO Type |
| :--------: | :-----: |
|  WalkSteps |  number |

## Component

---

### Pedometer

<div block-type = "component_component_block" component-selector = "Pedometer" id = "component-pedometer"></div>

Return Type : component

Component Pedometer

