# CloudDB

Non-visible component that communicates with CloudDB server to store and retrieve information.

---

## Designer Properties

---

### DefaultRedisServer

|    Property Name   | Editor Type | Default Value |
| :----------------: | :---------: | :-----------: |
| DefaultRedisServer |    string   |               |

### ProjectID

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ProjectID   |    string   |               |

### RedisPort

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   RedisPort   |   integer   |      6381     |

### RedisServer

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  RedisServer  |    string   |    DEFAULT    |

### Token

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Token     |    string   |               |

### UseSSL

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     UseSSL    |   boolean   |      True     |

## Events

---

### CloudDBError

<div block-type = "component_event" component-selector = "CloudDB" event-selector = "CloudDBError" event-params = "message" id = "clouddb-clouddberror"></div>

Indicates that an error occurred while communicating with the CloudDB Redis server.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

### DataChanged

<div block-type = "component_event" component-selector = "CloudDB" event-selector = "DataChanged" event-params = "tag-value" id = "clouddb-datachanged"></div>

Indicates that the data in the CloudDB project has changed. Launches an event with the \`tag\`

| Param Name | IO Type |
| :--------: | :-----: |
|     tag    |   text  |
|    value   |   any   |

### FirstRemoved

<div block-type = "component_event" component-selector = "CloudDB" event-selector = "FirstRemoved" event-params = "value" id = "clouddb-firstremoved"></div>

Event triggered by the "RemoveFirstFromList" function. The argument "value" is the object that was the first in the list, and which is now removed.

| Param Name | IO Type |
| :--------: | :-----: |
|    value   |   any   |

### GotValue

<div block-type = "component_event" component-selector = "CloudDB" event-selector = "GotValue" event-params = "tag-value" id = "clouddb-gotvalue"></div>

Indicates that a

| Param Name | IO Type |
| :--------: | :-----: |
|     tag    |   text  |
|    value   |   any   |

### TagList

<div block-type = "component_event" component-selector = "CloudDB" event-selector = "TagList" event-params = "value" id = "clouddb-taglist"></div>

Event triggered when we have received the list of known tags. Used with the "GetTagList" Function.

| Param Name | IO Type |
| :--------: | :-----: |
|    value   |   list  |

### UpdateDone

<div block-type = "component_event" component-selector = "CloudDB" event-selector = "UpdateDone" event-params = "tag-operation" id = "clouddb-updatedone"></div>

Indicates that operations that store data to CloudDB have completed.

| Param Name | IO Type |
| :--------: | :-----: |
|     tag    |   text  |
|  operation |   text  |

## Methods

---

### AppendValueToList

<div block-type = "component_method" component-selector = "CloudDB" method-selector = "AppendValueToList" method-params = "tag-itemToAdd" return-type = "undefined" id = "clouddb-appendvaluetolist"></div>

Return Type : No Return Value

Append a value to the end of a list atomically. If two devices use this function simultaneously, both will be appended and no data lost.

| Param Name | Input Type |
| :--------: | :--------: |
|     tag    |    text    |
|  itemToAdd |     any    |

### ClearTag

<div block-type = "component_method" component-selector = "CloudDB" method-selector = "ClearTag" method-params = "tag" return-type = "undefined" id = "clouddb-cleartag"></div>

Return Type : No Return Value

Remove the tag from CloudDB.

| Param Name | Input Type |
| :--------: | :--------: |
|     tag    |    text    |

### CloudConnected

<div block-type = "component_method" component-selector = "CloudDB" method-selector = "CloudConnected" method-params = "" return-type = "boolean" id = "clouddb-cloudconnected"></div>

Return Type : boolean

returns True if we are on the network and will likely be able to connect to the CloudDB server.

### GetTagList

<div block-type = "component_method" component-selector = "CloudDB" method-selector = "GetTagList" method-params = "" return-type = "undefined" id = "clouddb-gettaglist"></div>

Return Type : No Return Value

Get the list of tags for this application. When complete a "TagList" event will be triggered with the list of known tags.

### GetValue

<div block-type = "component_method" component-selector = "CloudDB" method-selector = "GetValue" method-params = "tag-valueIfTagNotThere" return-type = "undefined" id = "clouddb-getvalue"></div>

Return Type : No Return Value

Get the Value for a tag, doesn't return the value but will cause a GotValue event to fire when the value is looked up.

|     Param Name     | Input Type |
| :----------------: | :--------: |
|         tag        |    text    |
| valueIfTagNotThere |     any    |

### RemoveFirstFromList

<div block-type = "component_method" component-selector = "CloudDB" method-selector = "RemoveFirstFromList" method-params = "tag" return-type = "undefined" id = "clouddb-removefirstfromlist"></div>

Return Type : No Return Value

Return the first element of a list and atomically remove it. If two devices use this function simultaneously, one will get the first element and the the other will get the second element, or an error if there is no available element. When the element is available, the "FirstRemoved" event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|     tag    |    text    |

### StoreValue

<div block-type = "component_method" component-selector = "CloudDB" method-selector = "StoreValue" method-params = "tag-valueToStore" return-type = "undefined" id = "clouddb-storevalue"></div>

Return Type : No Return Value

Store a value at a tag.

|  Param Name  | Input Type |
| :----------: | :--------: |
|      tag     |    text    |
| valueToStore |     any    |

## Block Properties

---

### ProjectID

<div block-type = "component_set_get" component-selector = "CloudDB" property-selector = "ProjectID" property-type = "get" id = "get-clouddb-projectid"></div>

Gets the ProjectID for this CloudDB project.

| Param Name | IO Type |
| :--------: | :-----: |
|  ProjectID |   text  |

### RedisPort

<div block-type = "component_set_get" component-selector = "CloudDB" property-selector = "RedisPort" property-type = "get" id = "get-clouddb-redisport"></div>

The Redis Server port to use. Defaults to 6381

| Param Name | IO Type |
| :--------: | :-----: |
|  RedisPort |  number |

### RedisServer

<div block-type = "component_set_get" component-selector = "CloudDB" property-selector = "RedisServer" property-type = "get" id = "get-clouddb-redisserver"></div>

The Redis Server to use to store data. A setting of "DEFAULT" means that the MIT server will be used.

|  Param Name | IO Type |
| :---------: | :-----: |
| RedisServer |   text  |

## Component

---

### CloudDB

<div block-type = "component_component_block" component-selector = "CloudDB" id = "component-clouddb"></div>

Return Type : component

Component CloudDB

