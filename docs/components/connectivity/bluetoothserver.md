<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# BluetoothServer

Bluetooth server component

---

## Designer Properties

---

### CharacterEncoding

|   Property Name   | Editor Type | Default Value |
| :---------------: | :---------: | :-----------: |
| CharacterEncoding |    string   |     UTF-8     |

### DelimiterByte

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
| DelimiterByte | non_negative_integer |       0       |

### HighByteFirst

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| HighByteFirst |   boolean   |     False     |

### Secure

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Secure    |   boolean   |      True     |

## Events

---

### BluetoothError

<div block-type = "component_event" component-selector = "BluetoothServer" event-selector = "BluetoothError" id = "bluetoothserver-bluetootherror"></div>

The BluetoothError event is no longer used. Please use the Screen.ErrorOccurred event instead.

|  Param Name  | IO Type |
| :----------: | :-----: |
| functionName |   text  |
|    message   |   text  |

### ConnectionAccepted

<div block-type = "component_event" component-selector = "BluetoothServer" event-selector = "ConnectionAccepted" id = "bluetoothserver-connectionaccepted"></div>

Indicates that a bluetooth connection has been accepted.

## Methods

---

### AcceptConnection

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "AcceptConnection" id = "bluetoothserver-acceptconnection"></div>

Return Type : No Return Value

Accept an incoming connection with the Serial Port Profile (SPP).

|  Param Name | Input Type |
| :---------: | :--------: |
| serviceName |    text    |

### AcceptConnectionWithUUID

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "AcceptConnectionWithUUID" id = "bluetoothserver-acceptconnectionwithuuid"></div>

Return Type : No Return Value

Accept an incoming connection with a specific UUID.

|  Param Name | Input Type |
| :---------: | :--------: |
| serviceName |    text    |
|     uuid    |    text    |

### BytesAvailableToReceive

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "BytesAvailableToReceive" id = "bluetoothserver-bytesavailabletoreceive"></div>

Return Type : number

Returns an estimate of the number of bytes that can be received without blocking

### Disconnect

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "Disconnect" id = "bluetoothserver-disconnect"></div>

Return Type : No Return Value

Disconnect from the connected Bluetooth device.

### ReceiveSigned1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveSigned1ByteNumber" id = "bluetoothserver-receivesigned1bytenumber"></div>

Return Type : number

Receive a signed 1-byte number from the connected Bluetooth device.

### ReceiveSigned2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveSigned2ByteNumber" id = "bluetoothserver-receivesigned2bytenumber"></div>

Return Type : number

Receive a signed 2-byte number from the connected Bluetooth device.

### ReceiveSigned4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveSigned4ByteNumber" id = "bluetoothserver-receivesigned4bytenumber"></div>

Return Type : number

Receive a signed 4-byte number from the connected Bluetooth device.

### ReceiveSignedBytes

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveSignedBytes" id = "bluetoothserver-receivesignedbytes"></div>

Return Type : list

Receive multiple signed byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### ReceiveText

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveText" id = "bluetoothserver-receivetext"></div>

Return Type : text

Receive text from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### ReceiveUnsigned1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveUnsigned1ByteNumber" id = "bluetoothserver-receiveunsigned1bytenumber"></div>

Return Type : number

Receive an unsigned 1-byte number from the connected Bluetooth device.

### ReceiveUnsigned2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveUnsigned2ByteNumber" id = "bluetoothserver-receiveunsigned2bytenumber"></div>

Return Type : number

Receive a unsigned 2-byte number from the connected Bluetooth device.

### ReceiveUnsigned4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveUnsigned4ByteNumber" id = "bluetoothserver-receiveunsigned4bytenumber"></div>

Return Type : number

Receive a unsigned 4-byte number from the connected Bluetooth device.

### ReceiveUnsignedBytes

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveUnsignedBytes" id = "bluetoothserver-receiveunsignedbytes"></div>

Return Type : list

Receive multiple unsigned byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### Send1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "Send1ByteNumber" id = "bluetoothserver-send1bytenumber"></div>

Return Type : No Return Value

Send a 1-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### Send2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "Send2ByteNumber" id = "bluetoothserver-send2bytenumber"></div>

Return Type : No Return Value

Send a 2-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### Send4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "Send4ByteNumber" id = "bluetoothserver-send4bytenumber"></div>

Return Type : No Return Value

Send a 4-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### SendBytes

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "SendBytes" id = "bluetoothserver-sendbytes"></div>

Return Type : No Return Value

Send a list of byte values to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|    list    |    list    |

### SendText

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "SendText" id = "bluetoothserver-sendtext"></div>

Return Type : No Return Value

Send text to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |

### StopAccepting

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "StopAccepting" id = "bluetoothserver-stopaccepting"></div>

Return Type : No Return Value

Stop accepting an incoming connection.

## Block Properties

---

### Available

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "Available" property-type = "get" id = "get-bluetoothserver-available"></div>

Whether Bluetooth is available on the device

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### CharacterEncoding

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "CharacterEncoding" property-type = "get" id = "get-bluetoothserver-characterencoding"></div>

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "CharacterEncoding" property-type = "set" id = "set-bluetoothserver-characterencoding"></div>

Sets the character encoding to use when sending and receiving text. The default value is \`"UTF-8"

|     Param Name    | IO Type |
| :---------------: | :-----: |
| CharacterEncoding |   text  |

### DelimiterByte

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "DelimiterByte" property-type = "get" id = "get-bluetoothserver-delimiterbyte"></div>

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "DelimiterByte" property-type = "set" id = "set-bluetoothserver-delimiterbyte"></div>

Sets the delimiter byte to use when passing a negative number for the numberOfBytes parameter when calling ReceiveText, ReceiveSignedBytes, or ReceiveUnsignedBytes. Those functions will continue to read bytes until they encounter the value specified here. The default delimiter is 0, the null byte.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| DelimiterByte |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "Enabled" property-type = "get" id = "get-bluetoothserver-enabled"></div>

Whether Bluetooth is enabled

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### HighByteFirst

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "HighByteFirst" property-type = "get" id = "get-bluetoothserver-highbytefirst"></div>

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "HighByteFirst" property-type = "set" id = "set-bluetoothserver-highbytefirst"></div>

Returns \`true

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HighByteFirst | boolean |

### IsAccepting

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "IsAccepting" property-type = "get" id = "get-bluetoothserver-isaccepting"></div>

Returns true if this BluetoothServer component is accepting an incoming connection.

|  Param Name | IO Type |
| :---------: | :-----: |
| IsAccepting | boolean |

### IsConnected

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "IsConnected" property-type = "get" id = "get-bluetoothserver-isconnected"></div>

On devices with API level 14 (LEVEL\_ICE\_CREAM\_SANDWICH) or higher, this property returned is accurate. But on old devices with API level lower than 14, it may not return the current state of connection(e.g., it might be disconnected but you may not know until you attempt to read/write the socket.

|  Param Name | IO Type |
| :---------: | :-----: |
| IsConnected | boolean |

### Secure

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "Secure" property-type = "get" id = "get-bluetoothserver-secure"></div>

<div block-type = "component_set_get" component-selector = "BluetoothServer" property-selector = "Secure" property-type = "set" id = "set-bluetoothserver-secure"></div>

Whether to invoke SSP (Simple Secure Pairing), which is supported on devices with Bluetooth v2.1 or higher. When working with embedded Bluetooth devices, this property may need to be set to False. For Android 2.0-2.2, this property setting will be ignored.

| Param Name | IO Type |
| :--------: | :-----: |
|   Secure   | boolean |

## Component

---

### BluetoothServer

<div block-type = "component_component_block" component-selector = "BluetoothServer" id = "component-bluetoothserver"></div>

Return Type : component

Component BluetoothServer

