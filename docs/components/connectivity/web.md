# Web

Non-visible component that provides functions for HTTP GET, POST, PUT, and DELETE requests.

---

## Designer Properties

---

### AllowCookies

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  AllowCookies |   boolean   |     false     |

### ResponseFileName

|   Property Name  | Editor Type | Default Value |
| :--------------: | :---------: | :-----------: |
| ResponseFileName |    string   |               |

### SaveResponse

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  SaveResponse |   boolean   |     false     |

### Timeout

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|    Timeout    | non_negative_integer |       0       |

### Url

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|      Url      |    string   |               |

## Events

---

### GotFile

<div block-type = "component_event" component-selector = "Web" event-selector = "GotFile" event-params = "url-responseCode-responseType-fileName" id = "web-gotfile"></div>

Event indicating that a request has finished.

|  Param Name  | IO Type |
| :----------: | :-----: |
|      url     |   text  |
| responseCode |  number |
| responseType |   text  |
|   fileName   |   text  |

### GotText

<div block-type = "component_event" component-selector = "Web" event-selector = "GotText" event-params = "url-responseCode-responseType-responseContent" id = "web-gottext"></div>

Event indicating that a request has finished.

|    Param Name   | IO Type |
| :-------------: | :-----: |
|       url       |   text  |
|   responseCode  |  number |
|   responseType  |   text  |
| responseContent |   text  |

### TimedOut

<div block-type = "component_event" component-selector = "Web" event-selector = "TimedOut" event-params = "url" id = "web-timedout"></div>

Event indicating that a request has timed out.

| Param Name | IO Type |
| :--------: | :-----: |
|     url    |   text  |

## Methods

---

### BuildRequestData

<div block-type = "component_method" component-selector = "Web" method-selector = "BuildRequestData" method-params = "list" return-type = "text" id = "web-buildrequestdata"></div>

Return Type : text

Converts a list of two-element sublists, representing name and value pairs, to a string formatted as application/x-www-form-urlencoded media type, suitable to pass to PostText.

| Param Name | Input Type |
| :--------: | :--------: |
|    list    |    list    |

### ClearCookies

<div block-type = "component_method" component-selector = "Web" method-selector = "ClearCookies" method-params = "" return-type = "undefined" id = "web-clearcookies"></div>

Return Type : No Return Value

Clears all cookies for this Web component.

### Delete

<div block-type = "component_method" component-selector = "Web" method-selector = "Delete" method-params = "" return-type = "undefined" id = "web-delete"></div>

Return Type : No Return Value

Performs an HTTP DELETE request using the Url property and retrieves the response. If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file. If the SaveResponse property is false, the GotText event will be triggered.

### Get

<div block-type = "component_method" component-selector = "Web" method-selector = "Get" method-params = "" return-type = "undefined" id = "web-get"></div>

Return Type : No Return Value

Performs an HTTP GET request using the Url property and retrieves the response. If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file. If the SaveResponse property is false, the GotText event will be triggered.

### HtmlTextDecode

<div block-type = "component_method" component-selector = "Web" method-selector = "HtmlTextDecode" method-params = "htmlText" return-type = "text" id = "web-htmltextdecode"></div>

Return Type : text

Decodes the given HTML text value. HTML character entities such as &amp;, &lt;, &gt;, &apos;, and &quot; are changed to &, <, >, ', and ". Entities such as &#xhhhh, and &#nnnn are changed to the appropriate characters.

| Param Name | Input Type |
| :--------: | :--------: |
|  htmlText  |    text    |

### JsonObjectEncode

<div block-type = "component_method" component-selector = "Web" method-selector = "JsonObjectEncode" method-params = "jsonObject" return-type = "text" id = "web-jsonobjectencode"></div>

Return Type : text

Returns the value of a built-in type (i.e., boolean, number, text, list, dictionary) in its JavaScript Object Notation representation. If the value cannot be represented as JSON, the Screen's ErrorOccurred event will be run, if any, and the Web component will return the empty string.

| Param Name | Input Type |
| :--------: | :--------: |
| jsonObject |     any    |

### JsonTextDecode

<div block-type = "component_method" component-selector = "Web" method-selector = "JsonTextDecode" method-params = "jsonText" return-type = "any" id = "web-jsontextdecode"></div>

Return Type : any

Decodes the given JSON encoded value to produce a corresponding AppInventor value. A JSON list \`\[x, y, z\]\` decodes to a list \`(x y z)\`, A JSON object with key A and value B, (denoted as \`

| Param Name | Input Type |
| :--------: | :--------: |
|  jsonText  |    text    |

### JsonTextDecodeWithDictionaries

<div block-type = "component_method" component-selector = "Web" method-selector = "JsonTextDecodeWithDictionaries" method-params = "jsonText" return-type = "any" id = "web-jsontextdecodewithdictionaries"></div>

Return Type : any

Decodes the given JSON encoded value to produce a corresponding App Inventor value. A JSON list \[x, y, z\] decodes to a list (x y z). A JSON Object with name A and value B, denoted as \\

| Param Name | Input Type |
| :--------: | :--------: |
|  jsonText  |    text    |

### PatchFile

<div block-type = "component_method" component-selector = "Web" method-selector = "PatchFile" method-params = "path" return-type = "undefined" id = "web-patchfile"></div>

Return Type : No Return Value

Performs an HTTP PATCH request using the Url property and data from the specified file.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    path    |    text    |

### PatchText

<div block-type = "component_method" component-selector = "Web" method-selector = "PatchText" method-params = "text" return-type = "undefined" id = "web-patchtext"></div>

Return Type : No Return Value

Performs an HTTP PATCH request using the Url property and the specified text.  
The characters of the text are encoded using UTF-8 encoding.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The responseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |

### PatchTextWithEncoding

<div block-type = "component_method" component-selector = "Web" method-selector = "PatchTextWithEncoding" method-params = "text-encoding" return-type = "undefined" id = "web-patchtextwithencoding"></div>

Return Type : No Return Value

Performs an HTTP PATCH request using the Url property and the specified text.  
The characters of the text are encoded using the given encoding.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |
|  encoding  |    text    |

### PostFile

<div block-type = "component_method" component-selector = "Web" method-selector = "PostFile" method-params = "path" return-type = "undefined" id = "web-postfile"></div>

Return Type : No Return Value

Performs an HTTP POST request using the Url property and data from the specified file.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    path    |    text    |

### PostText

<div block-type = "component_method" component-selector = "Web" method-selector = "PostText" method-params = "text" return-type = "undefined" id = "web-posttext"></div>

Return Type : No Return Value

Performs an HTTP POST request using the Url property and the specified text.  
The characters of the text are encoded using UTF-8 encoding.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The responseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |

### PostTextWithEncoding

<div block-type = "component_method" component-selector = "Web" method-selector = "PostTextWithEncoding" method-params = "text-encoding" return-type = "undefined" id = "web-posttextwithencoding"></div>

Return Type : No Return Value

Performs an HTTP POST request using the Url property and the specified text.  
The characters of the text are encoded using the given encoding.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |
|  encoding  |    text    |

### PutFile

<div block-type = "component_method" component-selector = "Web" method-selector = "PutFile" method-params = "path" return-type = "undefined" id = "web-putfile"></div>

Return Type : No Return Value

Performs an HTTP PUT request using the Url property and data from the specified file.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    path    |    text    |

### PutText

<div block-type = "component_method" component-selector = "Web" method-selector = "PutText" method-params = "text" return-type = "undefined" id = "web-puttext"></div>

Return Type : No Return Value

Performs an HTTP PUT request using the Url property and the specified text.  
The characters of the text are encoded using UTF-8 encoding.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The responseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |

### PutTextWithEncoding

<div block-type = "component_method" component-selector = "Web" method-selector = "PutTextWithEncoding" method-params = "text-encoding" return-type = "undefined" id = "web-puttextwithencoding"></div>

Return Type : No Return Value

Performs an HTTP PUT request using the Url property and the specified text.  
The characters of the text are encoded using the given encoding.  
If the SaveResponse property is true, the response will be saved in a file and the GotFile event will be triggered. The ResponseFileName property can be used to specify the name of the file.  
If the SaveResponse property is false, the GotText event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |
|  encoding  |    text    |

### UriDecode

<div block-type = "component_method" component-selector = "Web" method-selector = "UriDecode" method-params = "text" return-type = "text" id = "web-uridecode"></div>

Return Type : text

Decodes the encoded text value so that the values aren't URL encoded anymore.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |

### UriEncode

<div block-type = "component_method" component-selector = "Web" method-selector = "UriEncode" method-params = "text" return-type = "text" id = "web-uriencode"></div>

Return Type : text

Encodes the given text value so that it can be used in a URL.

| Param Name | Input Type |
| :--------: | :--------: |
|    text    |    text    |

### XMLTextDecode

<div block-type = "component_method" component-selector = "Web" method-selector = "XMLTextDecode" method-params = "XmlText" return-type = "any" id = "web-xmltextdecode"></div>

Return Type : any

Decodes the given XML string to produce a dictionary structure. See the App Inventor documentation on "Other topics, notes, and details" for information.

| Param Name | Input Type |
| :--------: | :--------: |
|   XmlText  |    text    |

### XMLTextDecodeAsDictionary

<div block-type = "component_method" component-selector = "Web" method-selector = "XMLTextDecodeAsDictionary" method-params = "XmlText" return-type = "any" id = "web-xmltextdecodeasdictionary"></div>

Return Type : any

Decodes the given XML into a set of nested dictionaries that capture the structure and data contained in the XML. See the help for more details.

| Param Name | Input Type |
| :--------: | :--------: |
|   XmlText  |    text    |

## Block Properties

---

### AllowCookies

<div block-type = "component_set_get" component-selector = "Web" property-selector = "AllowCookies" property-type = "get" id = "get-web-allowcookies"></div>

<div block-type = "component_set_get" component-selector = "Web" property-selector = "AllowCookies" property-type = "set" id = "set-web-allowcookies"></div>

Whether the cookies from a response should be saved and used in subsequent requests. Cookies are only supported on Android version 2.3 or greater.

|  Param Name  | IO Type |
| :----------: | :-----: |
| AllowCookies | boolean |

### RequestHeaders

<div block-type = "component_set_get" component-selector = "Web" property-selector = "RequestHeaders" property-type = "get" id = "get-web-requestheaders"></div>

<div block-type = "component_set_get" component-selector = "Web" property-selector = "RequestHeaders" property-type = "set" id = "set-web-requestheaders"></div>

The request headers, as a list of two-element sublists. The first element of each sublist represents the request header field name. The second element of each sublist represents the request header field values, either a single value or a list containing multiple values.

|   Param Name   | IO Type |
| :------------: | :-----: |
| RequestHeaders |   list  |

### ResponseFileName

<div block-type = "component_set_get" component-selector = "Web" property-selector = "ResponseFileName" property-type = "get" id = "get-web-responsefilename"></div>

<div block-type = "component_set_get" component-selector = "Web" property-selector = "ResponseFileName" property-type = "set" id = "set-web-responsefilename"></div>

The name of the file where the response should be saved. If SaveResponse is true and ResponseFileName is empty, then a new file name will be generated.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| ResponseFileName |   text  |

### SaveResponse

<div block-type = "component_set_get" component-selector = "Web" property-selector = "SaveResponse" property-type = "get" id = "get-web-saveresponse"></div>

<div block-type = "component_set_get" component-selector = "Web" property-selector = "SaveResponse" property-type = "set" id = "set-web-saveresponse"></div>

Whether the response should be saved in a file.

|  Param Name  | IO Type |
| :----------: | :-----: |
| SaveResponse | boolean |

### Timeout

<div block-type = "component_set_get" component-selector = "Web" property-selector = "Timeout" property-type = "get" id = "get-web-timeout"></div>

<div block-type = "component_set_get" component-selector = "Web" property-selector = "Timeout" property-type = "set" id = "set-web-timeout"></div>

The number of milliseconds that a web request will wait for a response before giving up. If set to 0, then there is no time limit on how long the request will wait.

| Param Name | IO Type |
| :--------: | :-----: |
|   Timeout  |  number |

### Url

<div block-type = "component_set_get" component-selector = "Web" property-selector = "Url" property-type = "get" id = "get-web-url"></div>

<div block-type = "component_set_get" component-selector = "Web" property-selector = "Url" property-type = "set" id = "set-web-url"></div>

The URL for the web request.

| Param Name | IO Type |
| :--------: | :-----: |
|     Url    |   text  |

## Component

---

### Web

<div block-type = "component_component_block" component-selector = "Web" id = "component-web"></div>

Return Type : component

Component Web

