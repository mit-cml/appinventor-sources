<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Ev3Sound

A component that provides a high-level interface to sound functionalities on LEGO MINDSTORMS EV3 robot.

---

## Designer Properties

---

### BluetoothClient

|  Property Name  |   Editor Type   | Default Value |
| :-------------: | :-------------: | :-----------: |
| BluetoothClient | BluetoothClient |               |

## Methods

---

### PlayTone

<div block-type = "component_method" component-selector = "Ev3Sound" method-selector = "PlayTone" id = "ev3sound-playtone"></div>

Return Type : No Return Value

Make the robot play a tone.

|  Param Name  | Input Type |
| :----------: | :--------: |
|    volume    |   number   |
|   frequency  |   number   |
| milliseconds |   number   |

### StopSound

<div block-type = "component_method" component-selector = "Ev3Sound" method-selector = "StopSound" id = "ev3sound-stopsound"></div>

Return Type : No Return Value

Stop any sound on the robot.

## Block Properties

---

### BluetoothClient

<div block-type = "component_set_get" component-selector = "Ev3Sound" property-selector = "BluetoothClient" property-type = "get" id = "get-ev3sound-bluetoothclient"></div>

<div block-type = "component_set_get" component-selector = "Ev3Sound" property-selector = "BluetoothClient" property-type = "set" id = "set-ev3sound-bluetoothclient"></div>

The BluetoothClient component that should be used for communication.

|    Param Name   |  IO Type  |
| :-------------: | :-------: |
| BluetoothClient | component |

## Component

---

### Ev3Sound

<div block-type = "component_component_block" component-selector = "Ev3Sound" id = "component-ev3sound"></div>

Return Type : component

Component Ev3Sound

