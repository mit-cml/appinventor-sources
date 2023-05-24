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
* [Serial](#Serial)
* [Web](#Web)

## ActivityStarter  {#ActivityStarter}

A component that can launch an activity using the StartActivity method.

 Activities that can be launched include:

 * Starting another App Inventor for Android app. To do so, first find out the class of the other
   application by downloading the source code and using a file explorer or unzip utility to find
   a file named "youngandroidproject/project.properties". The first line of the file will start
   with "main=" and be followed by the class name; for example,
   `main=com.gmail.Bitdiddle.Ben.HelloPurr.Screen1`. (The first components indicate that it was
   created by Ben.Bitdiddle@gmail.com.) To make your `ActivityStarter` launch this application,
   set the following properties:
   * `ActivityPackage` to the class name, dropping the last component (for example,
     `com.gmail.Bitdiddle.Ben.HelloPurr`)
   * `ActivityClass` to the entire class name (for example,
     `com.gmail.Bitdiddle.Ben.HelloPurr.Screen1`)
 * Starting the camera application by setting the following properties:
   * `Action`: `android.intent.action.MAIN`
   * `ActivityPackage`: `com.android.camera`
   * `ActivityClass`: `com.android.camera.Camera`
 * Performing web search. Assuming the term you want to search for is "vampire" (feel free to substitute your own choice), set the properties to:
   * `Action`: `android.intent.action.WEB_SEARCH`
   * `ExtraKey`: `query`
   * `ExtraValue`: `vampire`
   * `ActivityPackage`: `com.google.android.providers.enhancedgooglesearch`
   * `ActivityClass`: `com.google.android.providers.enhancedgooglesearch.Launcher`
 * Opening a browser to a specified web page. Assuming the page you want to go to is "www.facebook.com" (feel free to substitute your own choice), set the properties to:
   * `Action`: `android.intent.action.VIEW`
   * `DataUri`: `http://www.facebook.com`



### Properties  {#ActivityStarter-Properties}

{:.properties}

{:id="ActivityStarter.Action" .text} *Action*
: Specifies the action that will be used to start the activity.

{:id="ActivityStarter.ActivityClass" .text} *ActivityClass*
: Specifies the class part of the specific component that will be started.

{:id="ActivityStarter.ActivityPackage" .text} *ActivityPackage*
: Specifies the package part of the specific component that will be started.

{:id="ActivityStarter.DataType" .text} *DataType*
: Specifies the MIME type to pass to the activity.

{:id="ActivityStarter.DataUri" .text} *DataUri*
: Specifies the data URI that will be used to start the activity.

{:id="ActivityStarter.ExtraKey" .text} *ExtraKey*
: Specifies the extra key that will be passed to the activity.
 Obsolete. Should use Extras instead

{:id="ActivityStarter.ExtraValue" .text} *ExtraValue*
: Specifies the extra value that will be passed to the activity.
 Obsolete. Should use Extras instead

{:id="ActivityStarter.Extras" .list .bo} *Extras*
: Returns the list of key-value pairs that will be passed as extra data to the activity.

{:id="ActivityStarter.Result" .text .ro .bo} *Result*
: Returns the result from the activity.

{:id="ActivityStarter.ResultName" .text} *ResultName*
: Specifies the name that will be used to retrieve a result from the
 activity.

{:id="ActivityStarter.ResultType" .text .ro .bo} *ResultType*
: Returns the MIME type from the activity.

{:id="ActivityStarter.ResultUri" .text .ro .bo} *ResultUri*
: Returns the URI from the activity.

### Events  {#ActivityStarter-Events}

{:.events}

{:id="ActivityStarter.ActivityCanceled"} ActivityCanceled()
: Event raised if this `ActivityStarter returns because the activity was canceled.

{:id="ActivityStarter.AfterActivity"} AfterActivity(*result*{:.text})
: Event raised after this `ActivityStarter` returns.

### Methods  {#ActivityStarter-Methods}

{:.methods}

{:id="ActivityStarter.ResolveActivity" class="method returns text"} <i/> ResolveActivity()
: Returns the name of the activity that corresponds to this `ActivityStarter`,
 or an empty string if no corresponding activity can be found.

{:id="ActivityStarter.StartActivity" class="method"} <i/> StartActivity()
: Start the activity corresponding to this `ActivityStarter`.

## BluetoothClient  {#BluetoothClient}

Use `BluetoothClient` to connect your device to other devices using Bluetooth. This component
 uses the Serial Port Profile (SPP) for communication. If you are interested in using Bluetooth
 low energy, please see the
 [BluetoothLE](http://iot.appinventor.mit.edu/#/bluetoothle/bluetoothleintro) extension.



### Properties  {#BluetoothClient-Properties}

{:.properties}

{:id="BluetoothClient.AddressesAndNames" .list .ro .bo} *AddressesAndNames*
: Returns the list of paired Bluetooth devices. Each element of the returned
 list is a String consisting of the device's address, a space, and the
 device's name. On Android 12 or later, if the permissions BLUETOOTH_CONNECT
 and BLUETOOTH_SCAN have not been granted to the app, the block will raise
 an error via the Screen's PermissionDenied event.

{:id="BluetoothClient.Available" .boolean .ro .bo} *Available*
: Returns `true`{:.logic.block} if Bluetooth is available on the device,
 `false`{:.logic.block} otherwise.

{:id="BluetoothClient.CharacterEncoding" .text} *CharacterEncoding*
: Returns the character encoding to use when sending and receiving text.

{:id="BluetoothClient.DelimiterByte" .number} *DelimiterByte*
: Returns the delimiter byte to use when passing a negative number for the
 numberOfBytes parameter when calling ReceiveText, ReceiveSignedBytes, or
 ReceiveUnsignedBytes.

{:id="BluetoothClient.DisconnectOnError" .boolean} *DisconnectOnError*
: Specifies whether BluetoothClient/BluetoothServer should be disconnected automatically when an error occurs.

{:id="BluetoothClient.Enabled" .boolean .ro .bo} *Enabled*
: Returns `true`{:.logic.block} if Bluetooth is enabled, `false`{:.logic.block} otherwise.

{:id="BluetoothClient.HighByteFirst" .boolean} *HighByteFirst*
: Specifies whether numbers are sent and received with the most significant
 byte first.

{:id="BluetoothClient.IsConnected" .boolean .ro .bo} *IsConnected*
: Returns `frue`{:.logic.block} if a connection to a Bluetooth device has been made.

{:id="BluetoothClient.NoLocationNeeded" .boolean .do} *NoLocationNeeded*
: On Android 12 and later, indicates that Bluetooth is not used to determine the user's location.

{:id="BluetoothClient.PollingRate" .number} *PollingRate*
: Returns the configured polling rate value of the Bluetooth Client.

{:id="BluetoothClient.Secure" .boolean} *Secure*
: Specifies whether a secure connection should be used.

### Events  {#BluetoothClient-Events}

{:.events}
None


### Methods  {#BluetoothClient-Methods}

{:.methods}

{:id="BluetoothClient.BytesAvailableToReceive" class="method returns number"} <i/> BytesAvailableToReceive()
: Returns number of bytes available from the input stream.

{:id="BluetoothClient.Connect" class="method returns boolean"} <i/> Connect(*address*{:.text})
: Connect to a Bluetooth device with the given address.

{:id="BluetoothClient.ConnectWithUUID" class="method returns boolean"} <i/> ConnectWithUUID(*address*{:.text},*uuid*{:.text})
: Connect to a Bluetooth device with the given address and a specific UUID.

{:id="BluetoothClient.Disconnect" class="method"} <i/> Disconnect()
: Disconnects from the connected Bluetooth device.

{:id="BluetoothClient.IsDevicePaired" class="method returns boolean"} <i/> IsDevicePaired(*address*{:.text})
: Checks whether the Bluetooth device with the given address is paired.

{:id="BluetoothClient.ReceiveSigned1ByteNumber" class="method returns number"} <i/> ReceiveSigned1ByteNumber()
: Reads a signed 1-byte number.

{:id="BluetoothClient.ReceiveSigned2ByteNumber" class="method returns number"} <i/> ReceiveSigned2ByteNumber()
: Reads a signed 2-byte number.

{:id="BluetoothClient.ReceiveSigned4ByteNumber" class="method returns number"} <i/> ReceiveSigned4ByteNumber()
: Reads a signed 4-byte number.

{:id="BluetoothClient.ReceiveSignedBytes" class="method returns list"} <i/> ReceiveSignedBytes(*numberOfBytes*{:.number})
: Reads a number of signed bytes from the input stream and returns them as
 a List.

   If numberOfBytes is negative, this method reads until a delimiter byte
 value is read. The delimiter byte value is included in the returned list.

{:id="BluetoothClient.ReceiveText" class="method returns text"} <i/> ReceiveText(*numberOfBytes*{:.number})
: Reads a number of bytes from the input stream and converts them to text.

   If numberOfBytes is negative, read until a delimiter byte value is read.

{:id="BluetoothClient.ReceiveUnsigned1ByteNumber" class="method returns number"} <i/> ReceiveUnsigned1ByteNumber()
: Reads an unsigned 1-byte number.

{:id="BluetoothClient.ReceiveUnsigned2ByteNumber" class="method returns number"} <i/> ReceiveUnsigned2ByteNumber()
: Reads an unsigned 2-byte number.

{:id="BluetoothClient.ReceiveUnsigned4ByteNumber" class="method returns number"} <i/> ReceiveUnsigned4ByteNumber()
: Reads an unsigned 4-byte number.

{:id="BluetoothClient.ReceiveUnsignedBytes" class="method returns list"} <i/> ReceiveUnsignedBytes(*numberOfBytes*{:.number})
: Reads a number of unsigned bytes from the input stream and returns them as
 a List.

   If numberOfBytes is negative, this method reads until a delimiter byte
 value is read. The delimiter byte value is included in the returned list.

{:id="BluetoothClient.Send1ByteNumber" class="method"} <i/> Send1ByteNumber(*number*{:.text})
: Decodes the given number String to an integer and writes it as one byte
 to the output stream.

   If the number could not be decoded to an integer, or the integer would not
 fit in one byte, then the Form's ErrorOccurred event is triggered and this
 method returns without writing any bytes to the output stream.

{:id="BluetoothClient.Send2ByteNumber" class="method"} <i/> Send2ByteNumber(*number*{:.text})
: Decodes the given number String to an integer and writes it as two bytes
 to the output stream.

   If the number could not be decoded to an integer, or the integer would not
 fit in two bytes, then the Form's ErrorOccurred event is triggered and this
 method returns without writing any bytes to the output stream.

{:id="BluetoothClient.Send4ByteNumber" class="method"} <i/> Send4ByteNumber(*number*{:.text})
: Decodes the given number String to an integer and writes it as four bytes
 to the output stream.

   If the number could not be decoded to an integer, or the integer would not
 fit in four bytes, then the Form's ErrorOccurred event is triggered and this
 method returns without writing any bytes to the output stream.

{:id="BluetoothClient.SendBytes" class="method"} <i/> SendBytes(*list*{:.list})
: Takes each element from the given list, converts it to a String, decodes
 the String to an integer, and writes it as one byte to the output stream.

   If an element could not be decoded to an integer, or the integer would not
 fit in one byte, then the Form's ErrorOccurred event is triggered and this
 method returns without writing any bytes to the output stream.

{:id="BluetoothClient.SendText" class="method"} <i/> SendText(*text*{:.text})
: Converts the given text to bytes and writes them to the output stream.

## BluetoothServer  {#BluetoothServer}

Use the `BluetoothServer` component to turn your device into a server that receive connections
 from other apps that use the `BluetoothClient` component.



### Properties  {#BluetoothServer-Properties}

{:.properties}

{:id="BluetoothServer.Available" .boolean .ro .bo} *Available*
: Returns `true`{:.logic.block} if Bluetooth is available on the device,
 `false`{:.logic.block} otherwise.

{:id="BluetoothServer.CharacterEncoding" .text} *CharacterEncoding*
: Returns the character encoding to use when sending and receiving text.

{:id="BluetoothServer.DelimiterByte" .number} *DelimiterByte*
: Returns the delimiter byte to use when passing a negative number for the
 numberOfBytes parameter when calling ReceiveText, ReceiveSignedBytes, or
 ReceiveUnsignedBytes.

{:id="BluetoothServer.Enabled" .boolean .ro .bo} *Enabled*
: Returns `true`{:.logic.block} if Bluetooth is enabled, `false`{:.logic.block} otherwise.

{:id="BluetoothServer.HighByteFirst" .boolean} *HighByteFirst*
: Specifies whether numbers are sent and received with the most significant
 byte first.

{:id="BluetoothServer.IsAccepting" .boolean .ro .bo} *IsAccepting*
: Returns true if this BluetoothServer component is accepting an
 incoming connection.

{:id="BluetoothServer.IsConnected" .boolean .ro .bo} *IsConnected*
: Returns `frue`{:.logic.block} if a connection to a Bluetooth device has been made.

{:id="BluetoothServer.Secure" .boolean} *Secure*
: Specifies whether a secure connection should be used.

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
: Returns number of bytes available from the input stream.

{:id="BluetoothServer.Disconnect" class="method"} <i/> Disconnect()
: Disconnects from the connected Bluetooth device.

{:id="BluetoothServer.ReceiveSigned1ByteNumber" class="method returns number"} <i/> ReceiveSigned1ByteNumber()
: Reads a signed 1-byte number.

{:id="BluetoothServer.ReceiveSigned2ByteNumber" class="method returns number"} <i/> ReceiveSigned2ByteNumber()
: Reads a signed 2-byte number.

{:id="BluetoothServer.ReceiveSigned4ByteNumber" class="method returns number"} <i/> ReceiveSigned4ByteNumber()
: Reads a signed 4-byte number.

{:id="BluetoothServer.ReceiveSignedBytes" class="method returns list"} <i/> ReceiveSignedBytes(*numberOfBytes*{:.number})
: Reads a number of signed bytes from the input stream and returns them as
 a List.

   If numberOfBytes is negative, this method reads until a delimiter byte
 value is read. The delimiter byte value is included in the returned list.

{:id="BluetoothServer.ReceiveText" class="method returns text"} <i/> ReceiveText(*numberOfBytes*{:.number})
: Reads a number of bytes from the input stream and converts them to text.

   If numberOfBytes is negative, read until a delimiter byte value is read.

{:id="BluetoothServer.ReceiveUnsigned1ByteNumber" class="method returns number"} <i/> ReceiveUnsigned1ByteNumber()
: Reads an unsigned 1-byte number.

{:id="BluetoothServer.ReceiveUnsigned2ByteNumber" class="method returns number"} <i/> ReceiveUnsigned2ByteNumber()
: Reads an unsigned 2-byte number.

{:id="BluetoothServer.ReceiveUnsigned4ByteNumber" class="method returns number"} <i/> ReceiveUnsigned4ByteNumber()
: Reads an unsigned 4-byte number.

{:id="BluetoothServer.ReceiveUnsignedBytes" class="method returns list"} <i/> ReceiveUnsignedBytes(*numberOfBytes*{:.number})
: Reads a number of unsigned bytes from the input stream and returns them as
 a List.

   If numberOfBytes is negative, this method reads until a delimiter byte
 value is read. The delimiter byte value is included in the returned list.

{:id="BluetoothServer.Send1ByteNumber" class="method"} <i/> Send1ByteNumber(*number*{:.text})
: Decodes the given number String to an integer and writes it as one byte
 to the output stream.

   If the number could not be decoded to an integer, or the integer would not
 fit in one byte, then the Form's ErrorOccurred event is triggered and this
 method returns without writing any bytes to the output stream.

{:id="BluetoothServer.Send2ByteNumber" class="method"} <i/> Send2ByteNumber(*number*{:.text})
: Decodes the given number String to an integer and writes it as two bytes
 to the output stream.

   If the number could not be decoded to an integer, or the integer would not
 fit in two bytes, then the Form's ErrorOccurred event is triggered and this
 method returns without writing any bytes to the output stream.

{:id="BluetoothServer.Send4ByteNumber" class="method"} <i/> Send4ByteNumber(*number*{:.text})
: Decodes the given number String to an integer and writes it as four bytes
 to the output stream.

   If the number could not be decoded to an integer, or the integer would not
 fit in four bytes, then the Form's ErrorOccurred event is triggered and this
 method returns without writing any bytes to the output stream.

{:id="BluetoothServer.SendBytes" class="method"} <i/> SendBytes(*list*{:.list})
: Takes each element from the given list, converts it to a String, decodes
 the String to an integer, and writes it as one byte to the output stream.

   If an element could not be decoded to an integer, or the integer would not
 fit in one byte, then the Form's ErrorOccurred event is triggered and this
 method returns without writing any bytes to the output stream.

{:id="BluetoothServer.SendText" class="method"} <i/> SendText(*text*{:.text})
: Converts the given text to bytes and writes them to the output stream.

{:id="BluetoothServer.StopAccepting" class="method"} <i/> StopAccepting()
: Stop accepting an incoming connection.

## Serial  {#Serial}

Component for Serial



### Properties  {#Serial-Properties}

{:.properties}

{:id="Serial.BaudRate" .number} *BaudRate*
: Returns the current baud rate

{:id="Serial.BufferSize" .number} *BufferSize*
: Returns the buffer size in bytes

{:id="Serial.IsInitialized" .boolean .ro .bo} *IsInitialized*
: Returns true when the Serial has been initialized.

{:id="Serial.IsOpen" .boolean .ro .bo} *IsOpen*
: Returns true when the Serial connection is open.

### Events  {#Serial-Events}

{:.events}
None


### Methods  {#Serial-Methods}

{:.methods}

{:id="Serial.CloseSerial" class="method returns boolean"} <i/> CloseSerial()
: Closes serial connection. Returns true when closed.

{:id="Serial.InitializeSerial" class="method"} <i/> InitializeSerial()
: Initializes serial connection.

{:id="Serial.OpenSerial" class="method returns boolean"} <i/> OpenSerial()
: Opens serial connection. Returns true when opened.

{:id="Serial.PrintSerial" class="method"} <i/> PrintSerial(*data*{:.text})
: Writes given data to serial, and appends a new line at the end.

{:id="Serial.ReadSerial" class="method returns text"} <i/> ReadSerial()
: Reads data from serial.

{:id="Serial.WriteSerial" class="method"} <i/> WriteSerial(*data*{:.text})
: Writes given data to serial.

## Web  {#Web}

Non-visible component that provides functions for HTTP GET, POST, PUT, and DELETE requests.



### Properties  {#Web-Properties}

{:.properties}

{:id="Web.AllowCookies" .boolean} *AllowCookies*
: Specifies whether cookies should be allowed

{:id="Web.RequestHeaders" .list .bo} *RequestHeaders*
: Sets the request headers.

{:id="Web.ResponseFileName" .text} *ResponseFileName*
: Specifies the name of the file where the response should be saved.
 If SaveResponse is true and ResponseFileName is empty, then a new file
 name will be generated.

{:id="Web.SaveResponse" .boolean} *SaveResponse*
: Specifies whether the response should be saved in a file.

{:id="Web.Timeout" .number} *Timeout*
: Returns the number of milliseconds that each request will wait for a response before they time out.
 If set to 0, then the request will wait for a response indefinitely.

{:id="Web.Url" .text} *Url*
: Specifies the URL.

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
 response.

   If the SaveResponse property is true, the response will be saved in a file
 and the GotFile event will be triggered. The ResponseFileName property
 can be used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be
 triggered.

{:id="Web.Get" class="method"} <i/> Get()
: Performs an HTTP GET request using the Url property and retrieves the
 response.

   If the SaveResponse property is true, the response will be saved in a file
 and the GotFile event will be triggered. The ResponseFileName property
 can be used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be
 triggered.

{:id="Web.HtmlTextDecode" class="method returns text"} <i/> HtmlTextDecode(*htmlText*{:.text})
: Decodes the given HTML text value.

   HTML Character Entities such as `&amp;`, `&lt;`, `&gt;`, `&apos;`, and `&quot;` are
 changed to `&`, `<`, `>`, `'`, and `"`.
 Entities such as `&#xhhhh;`, and `&#nnnn;` are changed to the appropriate characters.

{:id="Web.JsonObjectEncode" class="method returns text"} <i/> JsonObjectEncode(*jsonObject*{:.any})
: Returns the value of a built-in type (i.e., boolean, number, text, list, dictionary)
 in its JavaScript Object Notation representation. If the value cannot be
 represented as JSON, the Screen's ErrorOccurred event will be run, if any,
 and the Web component will return the empty string.

{:id="Web.JsonTextDecode" class="method returns any"} <i/> JsonTextDecode(*jsonText*{:.text})
: Decodes the given JSON encoded value to produce a corresponding AppInventor value.
 A JSON list `[x, y, z]` decodes to a list `(x y z)`,  A JSON object with key A and value B,
 (denoted as `{A:B}`) decodes to a list `((A B))`, that is, a list containing the two-element
 list `(A B)`.

   Use the method [JsonTextDecodeWithDictionaries](#Web.JsonTextDecodeWithDictionaries) if you
 would prefer to get back dictionary objects rather than lists-of-lists in the result.

{:id="Web.JsonTextDecodeWithDictionaries" class="method returns any"} <i/> JsonTextDecodeWithDictionaries(*jsonText*{:.text})
: Decodes the given JSON encoded value to produce a corresponding App Inventor value.
 A JSON list [x, y, z] decodes to a list (x y z). A JSON Object with name A and value B,
 denoted as \{a: b\} decodes to a dictionary with the key a and value b.

{:id="Web.PatchFile" class="method"} <i/> PatchFile(*path*{:.text})
: Performs an HTTP PATCH request using the Url property and data from the specified file.

   If the SaveResponse property is true, the response will be saved in a file
 and the GotFile event will be triggered. The ResponseFileName property can be
 used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PatchText" class="method"} <i/> PatchText(*text*{:.text})
: Performs an HTTP PATCH request using the Url property and the specified text.

   The characters of the text are encoded using UTF-8 encoding.

   If the SaveResponse property is true, the response will be saved in a
 file and the GotFile event will be triggered. The responseFileName property
 can be used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PatchTextWithEncoding" class="method"} <i/> PatchTextWithEncoding(*text*{:.text},*encoding*{:.text})
: Performs an HTTP PATCH request using the Url property and the specified text.

   The characters of the text are encoded using the given encoding.

   If the SaveResponse property is true, the response will be saved in a
 file and the GotFile event will be triggered. The ResponseFileName property
 can be used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PostFile" class="method"} <i/> PostFile(*path*{:.text})
: Performs an HTTP POST request using the Url property and data from the specified file.

   If the SaveResponse property is true, the response will be saved in a file
 and the GotFile event will be triggered. The ResponseFileName property can be
 used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PostText" class="method"} <i/> PostText(*text*{:.text})
: Performs an HTTP POST request using the Url property and the specified text.

   The characters of the text are encoded using UTF-8 encoding.

   If the SaveResponse property is true, the response will be saved in a
 file and the GotFile event will be triggered. The responseFileName property
 can be used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PostTextWithEncoding" class="method"} <i/> PostTextWithEncoding(*text*{:.text},*encoding*{:.text})
: Performs an HTTP POST request using the Url property and the specified text.

   The characters of the text are encoded using the given encoding.

   If the SaveResponse property is true, the response will be saved in a
 file and the GotFile event will be triggered. The ResponseFileName property
 can be used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PutFile" class="method"} <i/> PutFile(*path*{:.text})
: Performs an HTTP PUT request using the Url property and data from the specified file.

   If the SaveResponse property is true, the response will be saved in a file
 and the GotFile event will be triggered. The ResponseFileName property can be
 used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PutText" class="method"} <i/> PutText(*text*{:.text})
: Performs an HTTP PUT request using the Url property and the specified text.

   The characters of the text are encoded using UTF-8 encoding.

   If the SaveResponse property is true, the response will be saved in a
 file and the GotFile event will be triggered. The responseFileName property
 can be used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.PutTextWithEncoding" class="method"} <i/> PutTextWithEncoding(*text*{:.text},*encoding*{:.text})
: Performs an HTTP PUT request using the Url property and the specified text.

   The characters of the text are encoded using the given encoding.

   If the SaveResponse property is true, the response will be saved in a
 file and the GotFile event will be triggered. The ResponseFileName property
 can be used to specify the name of the file.

   If the SaveResponse property is false, the GotText event will be triggered.

{:id="Web.UriDecode" class="method returns text"} <i/> UriDecode(*text*{:.text})
: Decodes the encoded text value so that the values aren't URL encoded anymore.

{:id="Web.UriEncode" class="method returns text"} <i/> UriEncode(*text*{:.text})
: Encodes the given text value so that it can be used in a URL.

{:id="Web.XMLTextDecode" class="method returns any"} <i/> XMLTextDecode(*XmlText*{:.text})
: Decodes the given XML string to produce a list structure. `<tag>string</tag>` decodes to
 a list that contains a pair of tag and string.  More generally, if obj1, obj2, ...
 are tag-delimited XML strings, then `<tag>obj1 obj2 ...</tag>` decodes to a list
 that contains a pair whose first element is tag and whose second element is the
 list of the decoded obj's, ordered alphabetically by tags.

   Examples:
   * `<foo><123/foo>` decodes to a one-item list containing the pair `(foo 123)`
   * `<foo>1 2 3</foo>` decodes to a one-item list containing the pair `(foo "1 2 3")`
   * `<a><foo>1 2 3</foo><bar>456</bar></a>` decodes to a list containing the pair `(a X)`
     where X is a 2-item list that contains the pair `(bar 123)` and the pair `(foo "1 2 3")`.

   If the sequence of obj's mixes tag-delimited and non-tag-delimited items, then the
 non-tag-delimited items are pulled out of the sequence and wrapped with a "content" tag.
 For example, decoding `<a><bar>456</bar>many<foo>1 2 3</foo>apples<a></code>`
 is similar to above, except that the list X is a 3-item list that contains the additional pair
 whose first item is the string "content", and whose second item is the list (many, apples).
 This method signals an error and returns the empty list if the result is not well-formed XML.

{:id="Web.XMLTextDecodeAsDictionary" class="method returns any"} <i/> XMLTextDecodeAsDictionary(*XmlText*{:.text})
: Decodes the given XML string to produce a dictionary structure. The dictionary includes the
 special keys `$tag`, `$localName`, `$namespace`, `$namespaceUri`, `$attributes`, and `$content`,
 as well as a key for each unique tag for every node, which points to a list of elements of
 the same structure as described here.

   The `$tag` key is the full tag name, e.g., foo:bar. The `$localName` is the local portion of
 the name (everything after the colon `:` character). If a namespace is given (everything before
 the colon `:` character), it is provided in `$namespace` and the corresponding URI is given
 in `$namespaceUri`. The attributes are stored in a dictionary in `$attributes` and the
 child nodes are given as a list under `$content`.

   **More Information on Special Keys**

   Consider the following XML document:

   ```xml
     <ex:Book xmlns:ex="http://example.com/">
       <ex:title xml:lang="en">On the Origin of Species</ex:title>
       <ex:author>Charles Darwin</ex:author>
     </ex:Book>
   ```

   When parsed, the `$tag` key will be `"ex:Book"`, the `$localName` key will be `"Book"`, the
 `$namespace` key will be `"ex"`, `$namespaceUri` will be `"http://example.com/"`, the
 `$attributes` key will be a dictionary `{}` (xmlns is removed for the namespace), and the
 `$content` will be a list of two items representing the decoded `<ex:title>` and `<ex:author>`
 elements. The first item, which corresponds to the `<ex:title>` element, will have an
 `$attributes` key containing the dictionary `{"xml:lang": "en"}`. For each `name=value`
 attribute on an element, a key-value pair mapping `name` to `value` will exist in the
 `$attributes` dictionary. In addition to these special keys, there will also be `"ex:title"`
 and `"ex:author"` to allow lookups faster than having to traverse the `$content` list.
