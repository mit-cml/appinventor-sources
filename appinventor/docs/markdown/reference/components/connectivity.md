---
layout: documentation
title: Connectivity
---

[&laquo; Back to index](index.html)
# Connectivity

Table of Contents:

* [ActivityStarter](#ActivityStarter)
* [BluetoothClient](#BluetoothClient)
* [BluetoothServer](#BluetoothServer)
* [Web](#Web)

## ActivityStarter  {#ActivityStarter}

### Properties  {#ActivityStarter-Properties}

{:.properties}

{:id="ActivityStarter.Action" .text} *Action*
: Returns the action that will be used to start the activity.

{:id="ActivityStarter.ActivityClass" .text} *ActivityClass*
: Returns the class part of the specific component that will be started.

{:id="ActivityStarter.ActivityPackage" .text} *ActivityPackage*
: Returns the package part of the specific component that will be started.

{:id="ActivityStarter.DataType" .text} *DataType*
: Returns the MIME type to pass to the activity.

{:id="ActivityStarter.DataUri" .text} *DataUri*
: Returns the data URI that will be used to start the activity.

{:id="ActivityStarter.ExtraKey" .text} *ExtraKey*
: Returns the extra key that will be passed to the activity.
DEPRECATED: New code should use Extras property instead.

{:id="ActivityStarter.ExtraValue" .text} *ExtraValue*
: Returns the extra value that will be passed to the activity.
DEPRECATED: New code should use Extras property instead.

{:id="ActivityStarter.Extras" .list .bo} *Extras*
: Specifies the list of key-value pairs that will be passed as extra data to the activity.

{:id="ActivityStarter.Result" .text .ro .bo} *Result*
: Returns the result from the activity.

{:id="ActivityStarter.ResultName" .text} *ResultName*
: Returns the name that will be used to retrieve a result from the activity.

{:id="ActivityStarter.ResultType" .text .ro .bo} *ResultType*
: Returns the MIME type from the activity.

{:id="ActivityStarter.ResultUri" .text .ro .bo} *ResultUri*
: Returns the URI from the activity.

### Events  {#ActivityStarter-Events}

{:.events}

{:id="ActivityStarter.ActivityCanceled"} ActivityCanceled()
: Event raised if this ActivityStarter returns because the activity was canceled.

{:id="ActivityStarter.AfterActivity"} AfterActivity(*result*{:.text})
: Event raised after this ActivityStarter returns.

### Methods  {#ActivityStarter-Methods}

{:.methods}

{:id="ActivityStarter.ResolveActivity" class="method returns text"} <i/> ResolveActivity()
: Returns the name of the activity that corresponds to this ActivityStarter, or an empty string if no corresponding activity can be found.

{:id="ActivityStarter.StartActivity" class="method"} <i/> StartActivity()
: Start the activity corresponding to this ActivityStarter.

## BluetoothClient  {#BluetoothClient}

### Properties  {#BluetoothClient-Properties}

{:.properties}

{:id="BluetoothClient.AddressesAndNames" .list .ro .bo} *AddressesAndNames*
: The addresses and names of paired Bluetooth devices

{:id="BluetoothClient.Available" .boolean .ro .bo} *Available*
: Whether Bluetooth is available on the device

{:id="BluetoothClient.CharacterEncoding" .text} *CharacterEncoding*
: Sets the character encoding to use when sending and receiving text.

{:id="BluetoothClient.DelimiterByte" .number} *DelimiterByte*
: Sets the delimiter byte to use when passing a negative number for the
 numberOfBytes parameter when calling ReceiveText, ReceiveSignedBytes, or
 ReceiveUnsignedBytes.

{:id="BluetoothClient.Enabled" .boolean .ro .bo} *Enabled*
: Whether Bluetooth is enabled

{:id="BluetoothClient.HighByteFirst" .boolean} *HighByteFirst*
: Returns true if numbers are sent and received with the most significant
 byte first.

{:id="BluetoothClient.IsConnected" .boolean .ro .bo} *IsConnected*
: Returns true if a connection to a Bluetooth device has been made.

{:id="BluetoothClient.Secure" .boolean} *Secure*
: Whether to invoke SSP (Simple Secure Pairing), which is supported on devices with Bluetooth v2.1 or higher. When working with embedded Bluetooth devices, this property may need to be set to False. For Android 2.0-2.2, this property setting will be ignored.

### Events  {#BluetoothClient-Events}

{:.events}

### Methods  {#BluetoothClient-Methods}

{:.methods}

{:id="BluetoothClient.BytesAvailableToReceive" class="method returns number"} <i/> BytesAvailableToReceive()
: Returns an estimate of the number of bytes that can be received without blocking

{:id="BluetoothClient.Connect" class="method returns boolean"} <i/> Connect(*address*{:.text})
: Connect to the Bluetooth device with the specified address and the Serial Port Profile (SPP). Returns true if the connection was successful.

{:id="BluetoothClient.ConnectWithUUID" class="method returns boolean"} <i/> ConnectWithUUID(*address*{:.text},*uuid*{:.text})
: Connect to the Bluetooth device with the specified address and UUID. Returns true if the connection was successful.

{:id="BluetoothClient.Disconnect" class="method"} <i/> Disconnect()
: Disconnect from the connected Bluetooth device.

{:id="BluetoothClient.IsDevicePaired" class="method returns boolean"} <i/> IsDevicePaired(*address*{:.text})
: Checks whether the Bluetooth device with the specified address is paired.

{:id="BluetoothClient.ReceiveSigned1ByteNumber" class="method returns number"} <i/> ReceiveSigned1ByteNumber()
: Receive a signed 1-byte number from the connected Bluetooth device.

{:id="BluetoothClient.ReceiveSigned2ByteNumber" class="method returns number"} <i/> ReceiveSigned2ByteNumber()
: Receive a signed 2-byte number from the connected Bluetooth device.

{:id="BluetoothClient.ReceiveSigned4ByteNumber" class="method returns number"} <i/> ReceiveSigned4ByteNumber()
: Receive a signed 4-byte number from the connected Bluetooth device.

{:id="BluetoothClient.ReceiveSignedBytes" class="method returns list"} <i/> ReceiveSignedBytes(*numberOfBytes*{:.number})
: Receive multiple signed byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

{:id="BluetoothClient.ReceiveText" class="method returns text"} <i/> ReceiveText(*numberOfBytes*{:.number})
: Receive text from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

{:id="BluetoothClient.ReceiveUnsigned1ByteNumber" class="method returns number"} <i/> ReceiveUnsigned1ByteNumber()
: Receive an unsigned 1-byte number from the connected Bluetooth device.

{:id="BluetoothClient.ReceiveUnsigned2ByteNumber" class="method returns number"} <i/> ReceiveUnsigned2ByteNumber()
: Receive a unsigned 2-byte number from the connected Bluetooth device.

{:id="BluetoothClient.ReceiveUnsigned4ByteNumber" class="method returns number"} <i/> ReceiveUnsigned4ByteNumber()
: Receive a unsigned 4-byte number from the connected Bluetooth device.

{:id="BluetoothClient.ReceiveUnsignedBytes" class="method returns list"} <i/> ReceiveUnsignedBytes(*numberOfBytes*{:.number})
: Receive multiple unsigned byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

{:id="BluetoothClient.Send1ByteNumber" class="method"} <i/> Send1ByteNumber(*number*{:.text})
: Send a 1-byte number to the connected Bluetooth device.

{:id="BluetoothClient.Send2ByteNumber" class="method"} <i/> Send2ByteNumber(*number*{:.text})
: Send a 2-byte number to the connected Bluetooth device.

{:id="BluetoothClient.Send4ByteNumber" class="method"} <i/> Send4ByteNumber(*number*{:.text})
: Send a 4-byte number to the connected Bluetooth device.

{:id="BluetoothClient.SendBytes" class="method"} <i/> SendBytes(*list*{:.list})
: Send a list of byte values to the connected Bluetooth device.

{:id="BluetoothClient.SendText" class="method"} <i/> SendText(*text*{:.text})
: Send text to the connected Bluetooth device.

## BluetoothServer  {#BluetoothServer}

### Properties  {#BluetoothServer-Properties}

{:.properties}

{:id="BluetoothServer.Available" .boolean .ro .bo} *Available*
: Whether Bluetooth is available on the device

{:id="BluetoothServer.CharacterEncoding" .text} *CharacterEncoding*
: Sets the character encoding to use when sending and receiving text.

{:id="BluetoothServer.DelimiterByte" .number} *DelimiterByte*
: Sets the delimiter byte to use when passing a negative number for the
 numberOfBytes parameter when calling ReceiveText, ReceiveSignedBytes, or
 ReceiveUnsignedBytes.

{:id="BluetoothServer.Enabled" .boolean .ro .bo} *Enabled*
: Whether Bluetooth is enabled

{:id="BluetoothServer.HighByteFirst" .boolean} *HighByteFirst*
: Returns true if numbers are sent and received with the most significant
 byte first.

{:id="BluetoothServer.IsAccepting" .boolean .ro .bo} *IsAccepting*
: Returns true if this BluetoothServer component is accepting an
 incoming connection.

{:id="BluetoothServer.IsConnected" .boolean .ro .bo} *IsConnected*
: Returns true if a connection to a Bluetooth device has been made.

{:id="BluetoothServer.Secure" .boolean} *Secure*
: Whether to invoke SSP (Simple Secure Pairing), which is supported on devices with Bluetooth v2.1 or higher. When working with embedded Bluetooth devices, this property may need to be set to False. For Android 2.0-2.2, this property setting will be ignored.

### Events  {#BluetoothServer-Events}

{:.events}

{:id="BluetoothServer.ConnectionAccepted"} ConnectionAccepted()
: Indicates that a bluetooth connection has been accepted.

### Methods  {#BluetoothServer-Methods}

{:.methods}

{:id="BluetoothServer.AcceptConnection" class="method"} <i/> AcceptConnection(*serviceName*{:.text})
: Accept an incoming connection with the Serial Port Profile (SPP).

{:id="BluetoothServer.AcceptConnectionWithUUID" class="method"} <i/> AcceptConnectionWithUUID(*serviceName*{:.text},*uuid*{:.text})
: Accept an incoming connection with a specific UUID.

{:id="BluetoothServer.BytesAvailableToReceive" class="method returns number"} <i/> BytesAvailableToReceive()
: Returns an estimate of the number of bytes that can be received without blocking

{:id="BluetoothServer.Disconnect" class="method"} <i/> Disconnect()
: Disconnect from the connected Bluetooth device.

{:id="BluetoothServer.ReceiveSigned1ByteNumber" class="method returns number"} <i/> ReceiveSigned1ByteNumber()
: Receive a signed 1-byte number from the connected Bluetooth device.

{:id="BluetoothServer.ReceiveSigned2ByteNumber" class="method returns number"} <i/> ReceiveSigned2ByteNumber()
: Receive a signed 2-byte number from the connected Bluetooth device.

{:id="BluetoothServer.ReceiveSigned4ByteNumber" class="method returns number"} <i/> ReceiveSigned4ByteNumber()
: Receive a signed 4-byte number from the connected Bluetooth device.

{:id="BluetoothServer.ReceiveSignedBytes" class="method returns list"} <i/> ReceiveSignedBytes(*numberOfBytes*{:.number})
: Receive multiple signed byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

{:id="BluetoothServer.ReceiveText" class="method returns text"} <i/> ReceiveText(*numberOfBytes*{:.number})
: Receive text from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

{:id="BluetoothServer.ReceiveUnsigned1ByteNumber" class="method returns number"} <i/> ReceiveUnsigned1ByteNumber()
: Receive an unsigned 1-byte number from the connected Bluetooth device.

{:id="BluetoothServer.ReceiveUnsigned2ByteNumber" class="method returns number"} <i/> ReceiveUnsigned2ByteNumber()
: Receive a unsigned 2-byte number from the connected Bluetooth device.

{:id="BluetoothServer.ReceiveUnsigned4ByteNumber" class="method returns number"} <i/> ReceiveUnsigned4ByteNumber()
: Receive a unsigned 4-byte number from the connected Bluetooth device.

{:id="BluetoothServer.ReceiveUnsignedBytes" class="method returns list"} <i/> ReceiveUnsignedBytes(*numberOfBytes*{:.number})
: Receive multiple unsigned byte values from the connected Bluetooth device. If numberOfBytes is less than 0, read until a delimiter byte value is received.

{:id="BluetoothServer.Send1ByteNumber" class="method"} <i/> Send1ByteNumber(*number*{:.text})
: Send a 1-byte number to the connected Bluetooth device.

{:id="BluetoothServer.Send2ByteNumber" class="method"} <i/> Send2ByteNumber(*number*{:.text})
: Send a 2-byte number to the connected Bluetooth device.

{:id="BluetoothServer.Send4ByteNumber" class="method"} <i/> Send4ByteNumber(*number*{:.text})
: Send a 4-byte number to the connected Bluetooth device.

{:id="BluetoothServer.SendBytes" class="method"} <i/> SendBytes(*list*{:.list})
: Send a list of byte values to the connected Bluetooth device.

{:id="BluetoothServer.SendText" class="method"} <i/> SendText(*text*{:.text})
: Send text to the connected Bluetooth device.

{:id="BluetoothServer.StopAccepting" class="method"} <i/> StopAccepting()
: Stop accepting an incoming connection.

## Web  {#Web}

### Properties  {#Web-Properties}

{:.properties}

{:id="Web.AllowCookies" .boolean} *AllowCookies*
: Whether the cookies from a response should be saved and used in subsequent requests. Cookies are only supported on Android version 2.3 or greater.

{:id="Web.RequestHeaders" .list .bo} *RequestHeaders*
: The request headers, as a list of two-element sublists. The first element of each sublist represents the request header field name. The second element of each sublist represents the request header field values, either a single value or a list containing multiple values.

{:id="Web.ResponseFileName" .text} *ResponseFileName*
: The name of the file where the response should be saved. If SaveResponse is true and ResponseFileName is empty, then a new file name will be generated.

{:id="Web.SaveResponse" .boolean} *SaveResponse*
: Whether the response should be saved in a file.

{:id="Web.Timeout" .number} *Timeout*
: The number of milliseconds that a web request will wait for a response before giving up. If set to 0, then there is no time limit on how long the request will wait.

{:id="Web.Url" .text} *Url*
: The URL for the web request.

### Events  {#Web-Events}

{:.events}

{:id="Web.GotFile"} GotFile(*url*{:.text},*responseCode*{:.number},*responseType*{:.text},*fileName*{:.text})
: Event indicating that a request has finished.

{:id="Web.GotText"} GotText(*url*{:.text},*responseCode*{:.number},*responseType*{:.text},*responseContent*{:.text})
: Event indicating that a request has finished.

{:id="Web.TimedOut"} TimedOut(*url*{:.text})
: Event indicating that a request has timed out.

### Methods  {#Web-Methods}

{:.methods}

{:id="Web.BuildRequestData" class="method returns text"} <i/> BuildRequestData(*list*{:.list})
: Converts a list of two-element sublists, representing name and value pairs, to a
 string formatted as application/x-www-form-urlencoded media type, suitable to pass to
 PostText.

{:id="Web.ClearCookies" class="method"} <i/> ClearCookies()
: Clears all cookies for this Web component.

{:id="Web.Delete" class="method"} <i/> Delete()
: Performs an HTTP DELETE request using the Url property and retrieves the
 response.<br>
 If the SaveResponse property is true, the response will be saved in a file
 and the GotFile event will be triggered. The ResponseFileName property
 can be used to specify the name of the file.<br>
 If the SaveResponse property is false, the GotText event will be
 triggered.

{:id="Web.Get" class="method"} <i/> Get()
: Performs an HTTP GET request using the Url property and retrieves the
 response.<br>
 If the SaveResponse property is true, the response will be saved in a file
 and the GotFile event will be triggered. The ResponseFileName property
 can be used to specify the name of the file.<br>
 If the SaveResponse property is false, the GotText event will be
 triggered.

{:id="Web.HtmlTextDecode" class="method returns text"} <i/> HtmlTextDecode(*htmlText*{:.text})
: Decodes the given HTML text value. HTML character entities such as &amp;amp;, &amp;lt;, &amp;gt;, &amp;apos;, and &amp;quot; are changed to &amp;, &lt;, &gt;, &#39;, and &quot;. Entities such as &amp;#xhhhh, and &amp;#nnnn are changed to the appropriate characters.

{:id="Web.JsonTextDecode" class="method returns any"} <i/> JsonTextDecode(*jsonText*{:.text})
: Decodes the given JSON encoded value to produce a corresponding AppInventor value.
 A JSON list [x, y, z] decodes to a list (x y z),  A JSON object with name A and value B,
 (denoted as A:B enclosed in curly braces) decodes to a list
 ((A B)), that is, a list containing the two-element list (A B).

{:id="Web.PostFile" class="method"} <i/> PostFile(*path*{:.text})
: Performs an HTTP POST request using the Url property and data from the specified file.<br>If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.<br>If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PostText" class="method"} <i/> PostText(*text*{:.text})
: Performs an HTTP POST request using the Url property and the specified text.<br>The characters of the text are encoded using UTF-8 encoding.<br>If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The responseFileName property can be used to specify the name of the file.<br>If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PostTextWithEncoding" class="method"} <i/> PostTextWithEncoding(*text*{:.text},*encoding*{:.text})
: Performs an HTTP POST request using the Url property and the specified text.<br>The characters of the text are encoded using the given encoding.<br>If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.<br>If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PutFile" class="method"} <i/> PutFile(*path*{:.text})
: Performs an HTTP PUT request using the Url property and data from the specified file.<br>If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.<br>If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PutText" class="method"} <i/> PutText(*text*{:.text})
: Performs an HTTP PUT request using the Url property and the specified text.<br>The characters of the text are encoded using UTF-8 encoding.<br>If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The responseFileName property can be used to specify the name of the file.<br>If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PutTextWithEncoding" class="method"} <i/> PutTextWithEncoding(*text*{:.text},*encoding*{:.text})
: Performs an HTTP PUT request using the Url property and the specified text.<br>The characters of the text are encoded using the given encoding.<br>If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.<br>If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.UriDecode" class="method returns text"} <i/> UriDecode(*text*{:.text})
: Decodes the encoded text value.

{:id="Web.UriEncode" class="method returns text"} <i/> UriEncode(*text*{:.text})
: Encodes the given text value so that it can be used in a URL.

{:id="Web.XMLTextDecode" class="method returns any"} <i/> XMLTextDecode(*XmlText*{:.text})
: Decodes the given XML string to produce a list structure.  See the App Inventor documentation on "Other topics, notes, and details" for information.