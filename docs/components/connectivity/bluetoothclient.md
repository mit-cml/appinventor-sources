<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# BluetoothClient

Bluetooth client component

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

### DisconnectOnError

|   Property Name   | Editor Type | Default Value |
| :---------------: | :---------: | :-----------: |
| DisconnectOnError |   boolean   |     False     |

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

<div block-type = "component_event" component-selector = "BluetoothClient" event-selector = "BluetoothError" id = "bluetoothclient-bluetootherror"></div>

The BluetoothError event is no longer used. Please use the Screen.ErrorOccurred event instead.

|  Param Name  | IO Type |
| :----------: | :-----: |
| functionName |   text  |
|    message   |   text  |

## Methods

---

### BytesAvailableToReceive

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "BytesAvailableToReceive" id = "bluetoothclient-bytesavailabletoreceive"></div>

Return Type : number

Returns an estimate of the number of bytes that can be received without blocking

### Connect

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "Connect" id = "bluetoothclient-connect"></div>

Return Type : boolean

Connect to the Bluetooth device with the specified address and the Serial Port Profile (SPP). Returns true if the connection was successful.

| Param Name | Input Type |
| :--------: | :--------: |
|   address  |    text    |

### ConnectWithUUID

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ConnectWithUUID" id = "bluetoothclient-connectwithuuid"></div>

Return Type : boolean

Connect to the Bluetooth device with the specified address and UUID. Returns true if the connection was successful.

| Param Name | Input Type |
| :--------: | :--------: |
|   address  |    text    |
|    uuid    |    text    |

### Disconnect

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "Disconnect" id = "bluetoothclient-disconnect"></div>

Return Type : No Return Value

Disconnect from the connected Bluetooth device.

### IsDevicePaired

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "IsDevicePaired" id = "bluetoothclient-isdevicepaired"></div>

Return Type : boolean

Checks whether the Bluetooth device with the specified address is paired.

| Param Name | Input Type |
| :--------: | :--------: |
|   address  |    text    |

### ReceiveSigned1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveSigned1ByteNumber" id = "bluetoothclient-receivesigned1bytenumber"></div>

Return Type : number

Receive a signed 1-byte number from the connected Bluetooth device.

### ReceiveSigned2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveSigned2ByteNumber" id = "bluetoothclient-receivesigned2bytenumber"></div>

Return Type : number

Receive a signed 2-byte number from the connected Bluetooth device.

### ReceiveSigned4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveSigned4ByteNumber" id = "bluetoothclient-receivesigned4bytenumber"></div>

Return Type : number

Receive a signed 4-byte number from the connected Bluetooth device.

### ReceiveSignedBytes

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveSignedBytes" id = "bluetoothclient-receivesignedbytes"></div>

Return Type : list

Receive multiple signed byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### ReceiveText

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveText" id = "bluetoothclient-receivetext"></div>

Return Type : text

Receive text from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### ReceiveUnsigned1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveUnsigned1ByteNumber" id = "bluetoothclient-receiveunsigned1bytenumber"></div>

Return Type : number

Receive an unsigned 1-byte number from the connected Bluetooth device.

### ReceiveUnsigned2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveUnsigned2ByteNumber" id = "bluetoothclient-receiveunsigned2bytenumber"></div>

Return Type : number

Receive a unsigned 2-byte number from the connected Bluetooth device.

### ReceiveUnsigned4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveUnsigned4ByteNumber" id = "bluetoothclient-receiveunsigned4bytenumber"></div>

Return Type : number

Receive a unsigned 4-byte number from the connected Bluetooth device.

### ReceiveUnsignedBytes

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "ReceiveUnsignedBytes" id = "bluetoothclient-receiveunsignedbytes"></div>

Return Type : list

Receive multiple unsigned byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### Send1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "Send1ByteNumber" id = "bluetoothclient-send1bytenumber"></div>

Return Type : No Return Value

Send a 1-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### Send2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "Send2ByteNumber" id = "bluetoothclient-send2bytenumber"></div>

Return Type : No Return Value

Send a 2-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### Send4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "Send4ByteNumber" id = "bluetoothclient-send4bytenumber"></div>

Return Type : No Return Value

Send a 4-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### SendBytes

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "SendBytes" id = "bluetoothclient-sendbytes"></div>

Return Type : No Return Value

Send a list of byte values to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|    list    |    list    |

### SendText

<div block-type = "component_method" component-selector = "BluetoothClient" method-selector = "SendText" id = "bluetoothclient-sendtext"></div>

Return Type : No Return Value

Send text to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |

## Block Properties

---

### AddressesAndNames

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "AddressesAndNames" property-type = "get" id = "get-bluetoothclient-addressesandnames"></div>

The addresses and names of paired Bluetooth devices

|     Param Name    | IO Type |
| :---------------: | :-----: |
| AddressesAndNames |   list  |

### Available

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "Available" property-type = "get" id = "get-bluetoothclient-available"></div>

Whether Bluetooth is available on the device

| Param Name | IO Type |
| :--------: | :-----: |
|  Available | boolean |

### CharacterEncoding

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "CharacterEncoding" property-type = "get" id = "get-bluetoothclient-characterencoding"></div>

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "CharacterEncoding" property-type = "set" id = "set-bluetoothclient-characterencoding"></div>

Sets the character encoding to use when sending and receiving text. The default value is \`"UTF-8"

|     Param Name    | IO Type |
| :---------------: | :-----: |
| CharacterEncoding |   text  |

### DelimiterByte

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "DelimiterByte" property-type = "get" id = "get-bluetoothclient-delimiterbyte"></div>

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "DelimiterByte" property-type = "set" id = "set-bluetoothclient-delimiterbyte"></div>

Sets the delimiter byte to use when passing a negative number for the numberOfBytes parameter when calling ReceiveText, ReceiveSignedBytes, or ReceiveUnsignedBytes. Those functions will continue to read bytes until they encounter the value specified here. The default delimiter is 0, the null byte.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| DelimiterByte |  number |

### DisconnectOnError

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "DisconnectOnError" property-type = "get" id = "get-bluetoothclient-disconnectonerror"></div>

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "DisconnectOnError" property-type = "set" id = "set-bluetoothclient-disconnectonerror"></div>

Disconnects BluetoothClient automatically when an error occurs.

|     Param Name    | IO Type |
| :---------------: | :-----: |
| DisconnectOnError | boolean |

### Enabled

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "Enabled" property-type = "get" id = "get-bluetoothclient-enabled"></div>

Whether Bluetooth is enabled

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### HighByteFirst

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "HighByteFirst" property-type = "get" id = "get-bluetoothclient-highbytefirst"></div>

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "HighByteFirst" property-type = "set" id = "set-bluetoothclient-highbytefirst"></div>

Returns \`true

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HighByteFirst | boolean |

### IsConnected

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "IsConnected" property-type = "get" id = "get-bluetoothclient-isconnected"></div>

On devices with API level 14 (LEVEL\_ICE\_CREAM\_SANDWICH) or higher, this property returned is accurate. But on old devices with API level lower than 14, it may not return the current state of connection(e.g., it might be disconnected but you may not know until you attempt to read/write the socket.

|  Param Name | IO Type |
| :---------: | :-----: |
| IsConnected | boolean |

### Secure

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "Secure" property-type = "get" id = "get-bluetoothclient-secure"></div>

<div block-type = "component_set_get" component-selector = "BluetoothClient" property-selector = "Secure" property-type = "set" id = "set-bluetoothclient-secure"></div>

Whether to invoke SSP (Simple Secure Pairing), which is supported on devices with Bluetooth v2.1 or higher. When working with embedded Bluetooth devices, this property may need to be set to False. For Android 2.0-2.2, this property setting will be ignored.

| Param Name | IO Type |
| :--------: | :-----: |
|   Secure   | boolean |

## Component

---

### BluetoothClient

<div block-type = "component_component_block" component-selector = "BluetoothClient" id = "component-bluetoothclient"></div>

Return Type : component

Component BluetoothClient

