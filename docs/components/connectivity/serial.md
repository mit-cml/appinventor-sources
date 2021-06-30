<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Serial

Serial component which can be used to connect to devices like Arduino

---

## Designer Properties

---

### BaudRate

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    BaudRate   |   integer   |      9600     |

### BufferSize

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   BufferSize  |   integer   |      256      |

## Methods

---

### CloseSerial

<div block-type = "component_method" component-selector = "Serial" method-selector = "CloseSerial" id = "serial-closeserial"></div>

Return Type : boolean

Closes serial connection. Returns true when closed.

### InitializeSerial

<div block-type = "component_method" component-selector = "Serial" method-selector = "InitializeSerial" id = "serial-initializeserial"></div>

Return Type : No Return Value

Initializes serial connection.

### OpenSerial

<div block-type = "component_method" component-selector = "Serial" method-selector = "OpenSerial" id = "serial-openserial"></div>

Return Type : boolean

Opens serial connection. Returns true when opened.

### PrintSerial

<div block-type = "component_method" component-selector = "Serial" method-selector = "PrintSerial" id = "serial-printserial"></div>

Return Type : No Return Value

Writes given data to serial, and appends a new line at the end.

| Param Name | Input Type |
| :--------: | :--------: |
|    data    |    text    |

### ReadSerial

<div block-type = "component_method" component-selector = "Serial" method-selector = "ReadSerial" id = "serial-readserial"></div>

Return Type : text

Reads data from serial.

### WriteSerial

<div block-type = "component_method" component-selector = "Serial" method-selector = "WriteSerial" id = "serial-writeserial"></div>

Return Type : No Return Value

Writes given data to serial.

| Param Name | Input Type |
| :--------: | :--------: |
|    data    |    text    |

## Block Properties

---

### BaudRate

<div block-type = "component_set_get" component-selector = "Serial" property-selector = "BaudRate" property-type = "get" id = "get-serial-baudrate"></div>

<div block-type = "component_set_get" component-selector = "Serial" property-selector = "BaudRate" property-type = "set" id = "set-serial-baudrate"></div>

Returns the current baud rate

| Param Name | IO Type |
| :--------: | :-----: |
|  BaudRate  |  number |

### BufferSize

<div block-type = "component_set_get" component-selector = "Serial" property-selector = "BufferSize" property-type = "get" id = "get-serial-buffersize"></div>

<div block-type = "component_set_get" component-selector = "Serial" property-selector = "BufferSize" property-type = "set" id = "set-serial-buffersize"></div>

Returns the buffer size in bytes

| Param Name | IO Type |
| :--------: | :-----: |
| BufferSize |  number |

### IsInitialized

<div block-type = "component_set_get" component-selector = "Serial" property-selector = "IsInitialized" property-type = "get" id = "get-serial-isinitialized"></div>

Returns true when the Serial has been initialized.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| IsInitialized | boolean |

### IsOpen

<div block-type = "component_set_get" component-selector = "Serial" property-selector = "IsOpen" property-type = "get" id = "get-serial-isopen"></div>

Returns true when the Serial connection is open.

| Param Name | IO Type |
| :--------: | :-----: |
|   IsOpen   | boolean |

## Component

---

### Serial

<div block-type = "component_component_block" component-selector = "Serial" id = "component-serial"></div>

Return Type : component

Component Serial

