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

<div block-type = "component_event" component-selector = "BluetoothServer" event-selector = "BluetoothError" event-params = "functionName-message" id = "bluetoothserver-bluetootherror"></div>

The BluetoothError event is no longer used. Please use the Screen.ErrorOccurred event instead.

|  Param Name  | IO Type |
| :----------: | :-----: |
| functionName |   text  |
|    message   |   text  |

### ConnectionAccepted

<div block-type = "component_event" component-selector = "BluetoothServer" event-selector = "ConnectionAccepted" event-params = "" id = "bluetoothserver-connectionaccepted"></div>

Indicates that a bluetooth connection has been accepted.

## Methods

---

### AcceptConnection

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "AcceptConnection" method-params = "serviceName" return-type = "undefined" id = "bluetoothserver-acceptconnection"></div>

Return Type : No Return Value

Accept an incoming connection with the Serial Port Profile (SPP).

|  Param Name | Input Type |
| :---------: | :--------: |
| serviceName |    text    |

### AcceptConnectionWithUUID

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "AcceptConnectionWithUUID" method-params = "serviceName-uuid" return-type = "undefined" id = "bluetoothserver-acceptconnectionwithuuid"></div>

Return Type : No Return Value

Accept an incoming connection with a specific UUID.

|  Param Name | Input Type |
| :---------: | :--------: |
| serviceName |    text    |
|     uuid    |    text    |

### BytesAvailableToReceive

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "BytesAvailableToReceive" method-params = "" return-type = "number" id = "bluetoothserver-bytesavailabletoreceive"></div>

Return Type : number

Returns an estimate of the number of bytes that can be received without blocking

### Disconnect

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "Disconnect" method-params = "" return-type = "undefined" id = "bluetoothserver-disconnect"></div>

Return Type : No Return Value

Disconnect from the connected Bluetooth device.

### ReceiveSigned1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveSigned1ByteNumber" method-params = "" return-type = "number" id = "bluetoothserver-receivesigned1bytenumber"></div>

Return Type : number

Receive a signed 1-byte number from the connected Bluetooth device.

### ReceiveSigned2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveSigned2ByteNumber" method-params = "" return-type = "number" id = "bluetoothserver-receivesigned2bytenumber"></div>

Return Type : number

Receive a signed 2-byte number from the connected Bluetooth device.

### ReceiveSigned4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveSigned4ByteNumber" method-params = "" return-type = "number" id = "bluetoothserver-receivesigned4bytenumber"></div>

Return Type : number

Receive a signed 4-byte number from the connected Bluetooth device.

### ReceiveSignedBytes

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveSignedBytes" method-params = "numberOfBytes" return-type = "list" id = "bluetoothserver-receivesignedbytes"></div>

Return Type : list

Receive multiple signed byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### ReceiveText

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveText" method-params = "numberOfBytes" return-type = "text" id = "bluetoothserver-receivetext"></div>

Return Type : text

Receive text from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### ReceiveUnsigned1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveUnsigned1ByteNumber" method-params = "" return-type = "number" id = "bluetoothserver-receiveunsigned1bytenumber"></div>

Return Type : number

Receive an unsigned 1-byte number from the connected Bluetooth device.

### ReceiveUnsigned2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveUnsigned2ByteNumber" method-params = "" return-type = "number" id = "bluetoothserver-receiveunsigned2bytenumber"></div>

Return Type : number

Receive a unsigned 2-byte number from the connected Bluetooth device.

### ReceiveUnsigned4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveUnsigned4ByteNumber" method-params = "" return-type = "number" id = "bluetoothserver-receiveunsigned4bytenumber"></div>

Return Type : number

Receive a unsigned 4-byte number from the connected Bluetooth device.

### ReceiveUnsignedBytes

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "ReceiveUnsignedBytes" method-params = "numberOfBytes" return-type = "list" id = "bluetoothserver-receiveunsignedbytes"></div>

Return Type : list

Receive multiple unsigned byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

|   Param Name  | Input Type |
| :-----------: | :--------: |
| numberOfBytes |   number   |

### Send1ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "Send1ByteNumber" method-params = "number" return-type = "undefined" id = "bluetoothserver-send1bytenumber"></div>

Return Type : No Return Value

Send a 1-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### Send2ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "Send2ByteNumber" method-params = "number" return-type = "undefined" id = "bluetoothserver-send2bytenumber"></div>

Return Type : No Return Value

Send a 2-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### Send4ByteNumber

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "Send4ByteNumber" method-params = "number" return-type = "undefined" id = "bluetoothserver-send4bytenumber"></div>

Return Type : No Return Value

Send a 4-byte number to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|   number   |    text    |

### SendBytes

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "SendBytes" method-params = "list" return-type = "undefined" id = "bluetoothserver-sendbytes"></div>

Return Type : No Return Value

Send a list of byte values to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|    list    |    list    |

### SendText

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "SendText" method-params = "text" return-type = "undefined" id = "bluetoothserver-sendtext"></div>

Return Type : No Return Value

Send text to the connected Bluetooth device.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |

### StopAccepting

<div block-type = "component_method" component-selector = "BluetoothServer" method-selector = "StopAccepting" method-params = "" return-type = "undefined" id = "bluetoothserver-stopaccepting"></div>

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

