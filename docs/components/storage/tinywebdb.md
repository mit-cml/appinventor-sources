<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# TinyWebDB

Non-visible component that communicates with a Web service to store and retrieve information.

---

## Designer Properties

---

### ServiceURL

| Property Name | Editor Type |             Default Value            |
| :-----------: | :---------: | :----------------------------------: |
|   ServiceURL  |    string   | http://tinywebdb.appinventor.mit.edu |

## Events

---

### GotValue

<div block-type = "component_event" component-selector = "TinyWebDB" event-selector = "GotValue" id = "tinywebdb-gotvalue"></div>

Indicates that a GetValue server request has succeeded.

|   Param Name   | IO Type |
| :------------: | :-----: |
|  tagFromWebDB  |   text  |
| valueFromWebDB |   any   |

### ValueStored

<div block-type = "component_event" component-selector = "TinyWebDB" event-selector = "ValueStored" id = "tinywebdb-valuestored"></div>

Event indicating that a StoreValue server request has succeeded.

### WebServiceError

<div block-type = "component_event" component-selector = "TinyWebDB" event-selector = "WebServiceError" id = "tinywebdb-webserviceerror"></div>

Indicates that the communication with the Web service signaled an error.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

## Methods

---

### GetValue

<div block-type = "component_method" component-selector = "TinyWebDB" method-selector = "GetValue" id = "tinywebdb-getvalue"></div>

Return Type : No Return Value

Sends a request to the Web service to get the value stored under the given tag. The Web service must decide what to return if there is no value stored under the tag. This component accepts whatever is returned.

| Param Name | Input Type |
| :--------: | :--------: |
|     tag    |    text    |

### StoreValue

<div block-type = "component_method" component-selector = "TinyWebDB" method-selector = "StoreValue" id = "tinywebdb-storevalue"></div>

Return Type : No Return Value

Asks the Web service to store the given value under the given tag

|  Param Name  | Input Type |
| :----------: | :--------: |
|      tag     |    text    |
| valueToStore |     any    |

## Block Properties

---

### ServiceURL

<div block-type = "component_set_get" component-selector = "TinyWebDB" property-selector = "ServiceURL" property-type = "get" id = "get-tinywebdb-serviceurl"></div>

<div block-type = "component_set_get" component-selector = "TinyWebDB" property-selector = "ServiceURL" property-type = "set" id = "set-tinywebdb-serviceurl"></div>

The URL of the web service database.

| Param Name | IO Type |
| :--------: | :-----: |
| ServiceURL |   text  |

## Component

---

### TinyWebDB

<div block-type = "component_component_block" component-selector = "TinyWebDB" id = "component-tinywebdb"></div>

Return Type : component

Component TinyWebDB

