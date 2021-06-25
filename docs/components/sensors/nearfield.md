# NearField

Non-visible component to provide NFC capabilities. For now this component supports the reading and writing of text tags only (if supported by the device)

In order to read and write text tags, the component must have its `ReadMode` property set to True or False respectively.

**Note:** This component will only work on Screen1 of any App Inventor app.

---

## Designer Properties

---

### ReadMode

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    ReadMode   |   boolean   |      True     |

## Events

---

### TagRead

<div block-type = "component_event" component-selector = "NearField" event-selector = "TagRead" event-params = "message" id = "nearfield-tagread"></div>

Indicates that a new tag has been detected. Currently this is only a plain text tag, as specified in the manifest.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

### TagWritten

<div block-type = "component_event" component-selector = "NearField" event-selector = "TagWritten" event-params = "" id = "nearfield-tagwritten"></div>

Indicates that a tag has come into range of the NFC sensor and has been written.

## Block Properties

---

### LastMessage

<div block-type = "component_set_get" component-selector = "NearField" property-selector = "LastMessage" property-type = "get" id = "get-nearfield-lastmessage"></div>

Returns the content of the most recently received tag.

|  Param Name | IO Type |
| :---------: | :-----: |
| LastMessage |   text  |

### ReadMode

<div block-type = "component_set_get" component-selector = "NearField" property-selector = "ReadMode" property-type = "get" id = "get-nearfield-readmode"></div>

<div block-type = "component_set_get" component-selector = "NearField" property-selector = "ReadMode" property-type = "set" id = "set-nearfield-readmode"></div>

Returns true if in read mode, false if in write mode.

| Param Name | IO Type |
| :--------: | :-----: |
|  ReadMode  | boolean |

### TextToWrite

<div block-type = "component_set_get" component-selector = "NearField" property-selector = "TextToWrite" property-type = "get" id = "get-nearfield-texttowrite"></div>

<div block-type = "component_set_get" component-selector = "NearField" property-selector = "TextToWrite" property-type = "set" id = "set-nearfield-texttowrite"></div>

Returns the content of the text that's going to be written to the tag.

|  Param Name | IO Type |
| :---------: | :-----: |
| TextToWrite |   text  |

### WriteType

<div block-type = "component_set_get" component-selector = "NearField" property-selector = "WriteType" property-type = "get" id = "get-nearfield-writetype"></div>

Returns the write type for the NFC component. For this version of the component, it is always \`1\`.

| Param Name | IO Type |
| :--------: | :-----: |
|  WriteType |  number |

## Component

---

### NearField

<div block-type = "component_component_block" component-selector = "NearField" id = "component-nearfield"></div>

Return Type : component

Component NearField

