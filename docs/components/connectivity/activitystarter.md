# ActivityStarter

A component that can launch an activity using the `StartActivity` method.

Activities that can be launched include:

*   starting other App Inventor for Android apps
*   starting the camera application
*   performing web search
*   opening a browser to a specified web page
*   opening the map application to a specified location

You can also launch activities that return text data. See the documentation on using the Activity Starter for examples.

---

## Designer Properties

---

### Action

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Action    |    string   |               |

### ActivityClass

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| ActivityClass |    string   |               |

### ActivityPackage

|  Property Name  | Editor Type | Default Value |
| :-------------: | :---------: | :-----------: |
| ActivityPackage |    string   |               |

### DataType

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    DataType   |    string   |               |

### DataUri

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    DataUri    |    string   |               |

### ExtraKey

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    ExtraKey   |    string   |               |

### ExtraValue

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ExtraValue  |    string   |               |

### ResultName

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ResultName  |    string   |               |

## Events

---

### ActivityCanceled

<div block-type = "component_event" component-selector = "ActivityStarter" event-selector = "ActivityCanceled" event-params = "" id = "activitystarter-activitycanceled"></div>

Event raised if this ActivityStarter returns because the activity was canceled.

### ActivityError

<div block-type = "component_event" component-selector = "ActivityStarter" event-selector = "ActivityError" event-params = "message" id = "activitystarter-activityerror"></div>

The ActivityError event is no longer used. Please use the Screen.ErrorOccurred event instead.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

### AfterActivity

<div block-type = "component_event" component-selector = "ActivityStarter" event-selector = "AfterActivity" event-params = "result" id = "activitystarter-afteractivity"></div>

Event raised after this ActivityStarter returns.

| Param Name | IO Type |
| :--------: | :-----: |
|   result   |   text  |

## Methods

---

### ResolveActivity

<div block-type = "component_method" component-selector = "ActivityStarter" method-selector = "ResolveActivity" method-params = "" return-type = "text" id = "activitystarter-resolveactivity"></div>

Return Type : text

Returns the name of the activity that corresponds to this ActivityStarter, or an empty string if no corresponding activity can be found.

### StartActivity

<div block-type = "component_method" component-selector = "ActivityStarter" method-selector = "StartActivity" method-params = "" return-type = "undefined" id = "activitystarter-startactivity"></div>

Return Type : No Return Value

Start the activity corresponding to this ActivityStarter.

## Block Properties

---

### Action

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "Action" property-type = "get" id = "get-activitystarter-action"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "Action" property-type = "set" id = "set-activitystarter-action"></div>

Returns the action that will be used to start the activity.

| Param Name | IO Type |
| :--------: | :-----: |
|   Action   |   text  |

### ActivityClass

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ActivityClass" property-type = "get" id = "get-activitystarter-activityclass"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ActivityClass" property-type = "set" id = "set-activitystarter-activityclass"></div>

Returns the class part of the specific component that will be started.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| ActivityClass |   text  |

### ActivityPackage

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ActivityPackage" property-type = "get" id = "get-activitystarter-activitypackage"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ActivityPackage" property-type = "set" id = "set-activitystarter-activitypackage"></div>

Returns the package part of the specific component that will be started.

|    Param Name   | IO Type |
| :-------------: | :-----: |
| ActivityPackage |   text  |

### DataType

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "DataType" property-type = "get" id = "get-activitystarter-datatype"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "DataType" property-type = "set" id = "set-activitystarter-datatype"></div>

Returns the MIME type to pass to the activity.

| Param Name | IO Type |
| :--------: | :-----: |
|  DataType  |   text  |

### DataUri

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "DataUri" property-type = "get" id = "get-activitystarter-datauri"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "DataUri" property-type = "set" id = "set-activitystarter-datauri"></div>

Returns the data URI that will be used to start the activity.

| Param Name | IO Type |
| :--------: | :-----: |
|   DataUri  |   text  |

### ExtraKey

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ExtraKey" property-type = "get" id = "get-activitystarter-extrakey"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ExtraKey" property-type = "set" id = "set-activitystarter-extrakey"></div>

Returns the extra key that will be passed to the activity. DEPRECATED: New code should use Extras property instead.

| Param Name | IO Type |
| :--------: | :-----: |
|  ExtraKey  |   text  |

### ExtraValue

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ExtraValue" property-type = "get" id = "get-activitystarter-extravalue"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ExtraValue" property-type = "set" id = "set-activitystarter-extravalue"></div>

Returns the extra value that will be passed to the activity. DEPRECATED: New code should use Extras property instead.

| Param Name | IO Type |
| :--------: | :-----: |
| ExtraValue |   text  |

### Extras

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "Extras" property-type = "get" id = "get-activitystarter-extras"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "Extras" property-type = "set" id = "set-activitystarter-extras"></div>

Specifies the list of key-value pairs that will be passed as extra data to the activity.

| Param Name | IO Type |
| :--------: | :-----: |
|   Extras   |   list  |

### Result

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "Result" property-type = "get" id = "get-activitystarter-result"></div>

Returns the result from the activity.

| Param Name | IO Type |
| :--------: | :-----: |
|   Result   |   text  |

### ResultName

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ResultName" property-type = "get" id = "get-activitystarter-resultname"></div>

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ResultName" property-type = "set" id = "set-activitystarter-resultname"></div>

Returns the name that will be used to retrieve a result from the activity.

| Param Name | IO Type |
| :--------: | :-----: |
| ResultName |   text  |

### ResultType

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ResultType" property-type = "get" id = "get-activitystarter-resulttype"></div>

Returns the MIME type from the activity.

| Param Name | IO Type |
| :--------: | :-----: |
| ResultType |   text  |

### ResultUri

<div block-type = "component_set_get" component-selector = "ActivityStarter" property-selector = "ResultUri" property-type = "get" id = "get-activitystarter-resulturi"></div>

Returns the URI from the activity.

| Param Name | IO Type |
| :--------: | :-----: |
|  ResultUri |   text  |

## Component

---

### ActivityStarter

<div block-type = "component_component_block" component-selector = "ActivityStarter" id = "component-activitystarter"></div>

Return Type : component

Component ActivityStarter

