<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# FirebaseDB

Non-visible component that communicates with a Firebase to store and retrieve information.

---

## Designer Properties

---

### DefaultURL

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   DefaultURL  |    string   |               |

### DeveloperBucket

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| DeveloperBucket |    string   |               |

### FirebaseToken

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| FirebaseToken |    string   |               |

### FirebaseURL

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  FirebaseURL  |  FirbaseURL |    DEFAULT    |

### Persist

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Persist    |   boolean   |     False     |

### ProjectBucket

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| ProjectBucket |    string   |               |

## Events

---

### DataChanged

<div block-type = "component_event" component-selector = "FirebaseDB" event-selector = "DataChanged" id = "firebasedb-datachanged"></div>

Indicates that the data in the Firebase has changed. Launches an event with the tag and value that have been updated.

| Param Name | IO Type |
| :--------: | :-----: |
|     tag    |   text  |
|    value   |   any   |

### FirebaseError

<div block-type = "component_event" component-selector = "FirebaseDB" event-selector = "FirebaseError" id = "firebasedb-firebaseerror"></div>

Indicates that the communication with the Firebase signaled an error.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

### FirstRemoved

<div block-type = "component_event" component-selector = "FirebaseDB" event-selector = "FirstRemoved" id = "firebasedb-firstremoved"></div>

Event triggered by the "RemoveFirst" function. The argument "value" is the object that was the first in the list, and which is now removed.

| Param Name | IO Type |
| :--------: | :-----: |
|    value   |   any   |

### GotValue

<div block-type = "component_event" component-selector = "FirebaseDB" event-selector = "GotValue" id = "firebasedb-gotvalue"></div>

Indicates that a GetValue request has succeeded.

| Param Name | IO Type |
| :--------: | :-----: |
|     tag    |   text  |
|    value   |   any   |

### TagList

<div block-type = "component_event" component-selector = "FirebaseDB" event-selector = "TagList" id = "firebasedb-taglist"></div>

Event triggered when we have received the list of known tags. Used with the "GetTagList" Function.

| Param Name | IO Type |
| :--------: | :-----: |
|    value   |   list  |

## Methods

---

### AppendValue

<div block-type = "component_method" component-selector = "FirebaseDB" method-selector = "AppendValue" id = "firebasedb-appendvalue"></div>

Return Type : No Return Value

Append a value to the end of a list atomically. If two devices use this function simultaneously, both will be appended and no data lost.

| Param Name | Input Type |
| :--------: | :--------: |
|     tag    |    text    |
| valueToAdd |     any    |

### ClearTag

<div block-type = "component_method" component-selector = "FirebaseDB" method-selector = "ClearTag" id = "firebasedb-cleartag"></div>

Return Type : No Return Value

Remove the tag from Firebase

| Param Name | Input Type |
| :--------: | :--------: |
|     tag    |    text    |

### GetTagList

<div block-type = "component_method" component-selector = "FirebaseDB" method-selector = "GetTagList" id = "firebasedb-gettaglist"></div>

Return Type : No Return Value

Get the list of tags for this application. When complete a "TagList" event will be triggered with the list of known tags.

### GetValue

<div block-type = "component_method" component-selector = "FirebaseDB" method-selector = "GetValue" id = "firebasedb-getvalue"></div>

Return Type : No Return Value

GetValue asks Firebase to get the value stored under the given tag. It will pass valueIfTagNotThere to GotValue if there is no value stored under the tag.

|     Param Name     | Input Type |
| :----------------: | :--------: |
|         tag        |    text    |
| valueIfTagNotThere |     any    |

### RemoveFirst

<div block-type = "component_method" component-selector = "FirebaseDB" method-selector = "RemoveFirst" id = "firebasedb-removefirst"></div>

Return Type : No Return Value

Return the first element of a list and atomically remove it. If two devices use this function simultaneously, one will get the first element and the the other will get the second element, or an error if there is no available element. When the element is available, the "FirstRemoved" event will be triggered.

| Param Name | Input Type |
| :--------: | :--------: |
|     tag    |    text    |

### StoreValue

<div block-type = "component_method" component-selector = "FirebaseDB" method-selector = "StoreValue" id = "firebasedb-storevalue"></div>

Return Type : No Return Value

Asks Firebase to store the given value under the given tag.

|  Param Name  | Input Type |
| :----------: | :--------: |
|      tag     |    text    |
| valueToStore |     any    |

### Unauthenticate

<div block-type = "component_method" component-selector = "FirebaseDB" method-selector = "Unauthenticate" id = "firebasedb-unauthenticate"></div>

Return Type : No Return Value

If you are having difficulty with the Companion and you are switching between different Firebase accounts, you may need to use this function to clear internal Firebase caches. You can just use the "Do It" function on this block in the blocks editor. Note: You should not normally need to use this block as part of an application.

## Block Properties

---

### ProjectBucket

<div block-type = "component_set_get" component-selector = "FirebaseDB" property-selector = "ProjectBucket" property-type = "get" id = "get-firebasedb-projectbucket"></div>

<div block-type = "component_set_get" component-selector = "FirebaseDB" property-selector = "ProjectBucket" property-type = "set" id = "set-firebasedb-projectbucket"></div>

Sets the ProjectBucket for this FirebaseDB.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| ProjectBucket |   text  |

## Component

---

### FirebaseDB

<div block-type = "component_component_block" component-selector = "FirebaseDB" id = "component-firebasedb"></div>

Return Type : component

Component FirebaseDB

