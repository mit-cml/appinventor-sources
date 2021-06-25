# Circle

Circle

---

## Designer Properties

---

### Description

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  Description  |     text    |               |

### Draggable

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   Draggable   |   boolean   |     False     |

### EnableInfobox

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| EnableInfobox |   boolean   |     False     |

### FillColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   FillColor   |    color    |   &HFFFF0000  |

### FillOpacity

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  FillOpacity  |    float    |      1.0      |

### Latitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Latitude   |   latitude  |       0       |

### Longitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   Longitude   |  longitude  |       0       |

### Radius

| Property Name |     Editor Type    | Default Value |
| :-----------: | :----------------: | :-----------: |
|     Radius    | non_negative_float |       0       |

### StrokeColor

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  StrokeColor  |    color    |   &HFF000000  |

### StrokeOpacity

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| StrokeOpacity |    float    |      1.0      |

### StrokeWidth

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  StrokeWidth  |   integer   |       1       |

### Title

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     Title     |     text    |               |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### Click

<div block-type = "component_event" component-selector = "Circle" event-selector = "Click" event-params = "" id = "circle-click"></div>

The user clicked on the Circle.

### Drag

<div block-type = "component_event" component-selector = "Circle" event-selector = "Drag" event-params = "" id = "circle-drag"></div>

The user dragged the Circle.

### LongClick

<div block-type = "component_event" component-selector = "Circle" event-selector = "LongClick" event-params = "" id = "circle-longclick"></div>

The user long-pressed on the Circle. This event will only trigger if Draggable is false.

### StartDrag

<div block-type = "component_event" component-selector = "Circle" event-selector = "StartDrag" event-params = "" id = "circle-startdrag"></div>

The user started a drag operation.

### StopDrag

<div block-type = "component_event" component-selector = "Circle" event-selector = "StopDrag" event-params = "" id = "circle-stopdrag"></div>

The user stopped a drag operation.

## Methods

---

### DistanceToFeature

<div block-type = "component_method" component-selector = "Circle" method-selector = "DistanceToFeature" method-params = "mapFeature-centroids" return-type = "number" id = "circle-distancetofeature"></div>

Return Type : number

Compute the distance, in meters, between two map features.

| Param Name | Input Type |
| :--------: | :--------: |
| mapFeature |  component |
|  centroids |   boolean  |

### DistanceToPoint

<div block-type = "component_method" component-selector = "Circle" method-selector = "DistanceToPoint" method-params = "latitude-longitude-centroid" return-type = "number" id = "circle-distancetopoint"></div>

Return Type : number

Compute the distance, in meters, between a Circle and a latitude, longitude point.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |
|  centroid  |   boolean  |

### HideInfobox

<div block-type = "component_method" component-selector = "Circle" method-selector = "HideInfobox" method-params = "" return-type = "undefined" id = "circle-hideinfobox"></div>

Return Type : No Return Value

Hide the infobox if it is shown. If the infobox is not visible this function has no effect.

### SetLocation

<div block-type = "component_method" component-selector = "Circle" method-selector = "SetLocation" method-params = "latitude-longitude" return-type = "undefined" id = "circle-setlocation"></div>

Return Type : No Return Value

Set the center of the Circle.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |

### ShowInfobox

<div block-type = "component_method" component-selector = "Circle" method-selector = "ShowInfobox" method-params = "" return-type = "undefined" id = "circle-showinfobox"></div>

Return Type : No Return Value

Show the infobox for the Circle. This will show the infobox even if EnableInfobox is set to false.

## Block Properties

---

### Description

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Description" property-type = "get" id = "get-circle-description"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Description" property-type = "set" id = "set-circle-description"></div>

The description displayed in the info window that appears when the user clicks on the Circle.

|  Param Name | IO Type |
| :---------: | :-----: |
| Description |   text  |

### Draggable

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Draggable" property-type = "get" id = "get-circle-draggable"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Draggable" property-type = "set" id = "set-circle-draggable"></div>

The Draggable property is used to set whether or not the user can drag the Circle by long-pressing and then dragging the Circle to a new location.

| Param Name | IO Type |
| :--------: | :-----: |
|  Draggable | boolean |

### EnableInfobox

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "EnableInfobox" property-type = "get" id = "get-circle-enableinfobox"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "EnableInfobox" property-type = "set" id = "set-circle-enableinfobox"></div>

Enable or disable the infobox window display when the user taps the Circle.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| EnableInfobox | boolean |

### FillColor

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "FillColor" property-type = "get" id = "get-circle-fillcolor"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "FillColor" property-type = "set" id = "set-circle-fillcolor"></div>

The paint color used to fill in the Circle.

| Param Name | IO Type |
| :--------: | :-----: |
|  FillColor |  number |

### FillOpacity

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "FillOpacity" property-type = "get" id = "get-circle-fillopacity"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "FillOpacity" property-type = "set" id = "set-circle-fillopacity"></div>

The opacity of the interior of the map feature.

|  Param Name | IO Type |
| :---------: | :-----: |
| FillOpacity |  number |

### Latitude

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Latitude" property-type = "get" id = "get-circle-latitude"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Latitude" property-type = "set" id = "set-circle-latitude"></div>

The latitude of the center of the circle.

| Param Name | IO Type |
| :--------: | :-----: |
|  Latitude  |  number |

### Longitude

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Longitude" property-type = "get" id = "get-circle-longitude"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Longitude" property-type = "set" id = "set-circle-longitude"></div>

The longitude of the center of the circle.

| Param Name | IO Type |
| :--------: | :-----: |
|  Longitude |  number |

### Radius

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Radius" property-type = "get" id = "get-circle-radius"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Radius" property-type = "set" id = "set-circle-radius"></div>

The radius of the circle in meters.

| Param Name | IO Type |
| :--------: | :-----: |
|   Radius   |  number |

### StrokeColor

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "StrokeColor" property-type = "get" id = "get-circle-strokecolor"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "StrokeColor" property-type = "set" id = "set-circle-strokecolor"></div>

The paint color used to outline the Circle.

|  Param Name | IO Type |
| :---------: | :-----: |
| StrokeColor |  number |

### StrokeOpacity

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "StrokeOpacity" property-type = "get" id = "get-circle-strokeopacity"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "StrokeOpacity" property-type = "set" id = "set-circle-strokeopacity"></div>

The opacity of the stroke used to outline the map feature.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| StrokeOpacity |  number |

### StrokeWidth

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "StrokeWidth" property-type = "get" id = "get-circle-strokewidth"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "StrokeWidth" property-type = "set" id = "set-circle-strokewidth"></div>

The width of the stroke used to outline the Circle.

|  Param Name | IO Type |
| :---------: | :-----: |
| StrokeWidth |  number |

### Title

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Title" property-type = "get" id = "get-circle-title"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Title" property-type = "set" id = "set-circle-title"></div>

The title displayed in the info window that appears when the user clicks on the Circle.

| Param Name | IO Type |
| :--------: | :-----: |
|    Title   |   text  |

### Type

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Type" property-type = "get" id = "get-circle-type"></div>

Returns the type of the feature. For Circles, this returns MapFeature.Circle ("Circle").

| Param Name | IO Type |
| :--------: | :-----: |
|    Type    |   text  |

### Visible

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Visible" property-type = "get" id = "get-circle-visible"></div>

<div block-type = "component_set_get" component-selector = "Circle" property-selector = "Visible" property-type = "set" id = "set-circle-visible"></div>

Specifies whether the Circle should be visible on the screen. Value is true if the component is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

## Component

---

### Circle

<div block-type = "component_component_block" component-selector = "Circle" id = "component-circle"></div>

Return Type : component

Component Circle

