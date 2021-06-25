# Rectangle

Rectangle

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

### EastLongitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| EastLongitude |    float    |       0       |

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

### NorthLatitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| NorthLatitude |    float    |       0       |

### SouthLatitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| SouthLatitude |    float    |       0       |

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

### WestLongitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| WestLongitude |    float    |       0       |

## Events

---

### Click

<div block-type = "component_event" component-selector = "Rectangle" event-selector = "Click" event-params = "" id = "rectangle-click"></div>

The user clicked on the Rectangle.

### Drag

<div block-type = "component_event" component-selector = "Rectangle" event-selector = "Drag" event-params = "" id = "rectangle-drag"></div>

The user dragged the Rectangle.

### LongClick

<div block-type = "component_event" component-selector = "Rectangle" event-selector = "LongClick" event-params = "" id = "rectangle-longclick"></div>

The user long-pressed on the Rectangle. This event will only trigger if Draggable is false.

### StartDrag

<div block-type = "component_event" component-selector = "Rectangle" event-selector = "StartDrag" event-params = "" id = "rectangle-startdrag"></div>

The user started a drag operation.

### StopDrag

<div block-type = "component_event" component-selector = "Rectangle" event-selector = "StopDrag" event-params = "" id = "rectangle-stopdrag"></div>

The user stopped a drag operation.

## Methods

---

### Bounds

<div block-type = "component_method" component-selector = "Rectangle" method-selector = "Bounds" method-params = "" return-type = "list" id = "rectangle-bounds"></div>

Return Type : list

Returns the bounding box of the Rectangle in the format ((North West) (South East)).

### Center

<div block-type = "component_method" component-selector = "Rectangle" method-selector = "Center" method-params = "" return-type = "list" id = "rectangle-center"></div>

Return Type : list

Returns the center of the Rectangle as a list of the form (Latitude Longitude).

### DistanceToFeature

<div block-type = "component_method" component-selector = "Rectangle" method-selector = "DistanceToFeature" method-params = "mapFeature-centroids" return-type = "number" id = "rectangle-distancetofeature"></div>

Return Type : number

Compute the distance, in meters, between two map features.

| Param Name | Input Type |
| :--------: | :--------: |
| mapFeature |  component |
|  centroids |   boolean  |

### DistanceToPoint

<div block-type = "component_method" component-selector = "Rectangle" method-selector = "DistanceToPoint" method-params = "latitude-longitude-centroid" return-type = "number" id = "rectangle-distancetopoint"></div>

Return Type : number

Compute the distance, in meters, between a Rectangle and a latitude, longitude point.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |
|  centroid  |   boolean  |

### HideInfobox

<div block-type = "component_method" component-selector = "Rectangle" method-selector = "HideInfobox" method-params = "" return-type = "undefined" id = "rectangle-hideinfobox"></div>

Return Type : No Return Value

Hide the infobox if it is shown. If the infobox is not visible this function has no effect.

### SetCenter

<div block-type = "component_method" component-selector = "Rectangle" method-selector = "SetCenter" method-params = "latitude-longitude" return-type = "undefined" id = "rectangle-setcenter"></div>

Return Type : No Return Value

Moves the Rectangle so that it is centered on the given latitude and longitude while attempting to maintain the width and height of the Rectangle as measured from the center to the edges.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |

### ShowInfobox

<div block-type = "component_method" component-selector = "Rectangle" method-selector = "ShowInfobox" method-params = "" return-type = "undefined" id = "rectangle-showinfobox"></div>

Return Type : No Return Value

Show the infobox for the Rectangle. This will show the infobox even if EnableInfobox is set to false.

## Block Properties

---

### Description

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Description" property-type = "get" id = "get-rectangle-description"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Description" property-type = "set" id = "set-rectangle-description"></div>

The description displayed in the info window that appears when the user clicks on the Rectangle.

|  Param Name | IO Type |
| :---------: | :-----: |
| Description |   text  |

### Draggable

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Draggable" property-type = "get" id = "get-rectangle-draggable"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Draggable" property-type = "set" id = "set-rectangle-draggable"></div>

The Draggable property is used to set whether or not the user can drag the Rectangle by long-pressing and then dragging the Rectangle to a new location.

| Param Name | IO Type |
| :--------: | :-----: |
|  Draggable | boolean |

### EastLongitude

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "EastLongitude" property-type = "get" id = "get-rectangle-eastlongitude"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "EastLongitude" property-type = "set" id = "set-rectangle-eastlongitude"></div>

The east edge of the rectangle, in decimal degrees east of the prime meridian.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| EastLongitude |  number |

### EnableInfobox

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "EnableInfobox" property-type = "get" id = "get-rectangle-enableinfobox"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "EnableInfobox" property-type = "set" id = "set-rectangle-enableinfobox"></div>

Enable or disable the infobox window display when the user taps the Rectangle.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| EnableInfobox | boolean |

### FillColor

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "FillColor" property-type = "get" id = "get-rectangle-fillcolor"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "FillColor" property-type = "set" id = "set-rectangle-fillcolor"></div>

The paint color used to fill in the Rectangle.

| Param Name | IO Type |
| :--------: | :-----: |
|  FillColor |  number |

### FillOpacity

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "FillOpacity" property-type = "get" id = "get-rectangle-fillopacity"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "FillOpacity" property-type = "set" id = "set-rectangle-fillopacity"></div>

The opacity of the interior of the map feature.

|  Param Name | IO Type |
| :---------: | :-----: |
| FillOpacity |  number |

### NorthLatitude

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "NorthLatitude" property-type = "get" id = "get-rectangle-northlatitude"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "NorthLatitude" property-type = "set" id = "set-rectangle-northlatitude"></div>

The north edge of the rectangle, in decimal degrees north of the equator.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| NorthLatitude |  number |

### SouthLatitude

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "SouthLatitude" property-type = "get" id = "get-rectangle-southlatitude"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "SouthLatitude" property-type = "set" id = "set-rectangle-southlatitude"></div>

The south edge of the rectangle, in decimal degrees north of the equator.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| SouthLatitude |  number |

### StrokeColor

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "StrokeColor" property-type = "get" id = "get-rectangle-strokecolor"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "StrokeColor" property-type = "set" id = "set-rectangle-strokecolor"></div>

The paint color used to outline the Rectangle.

|  Param Name | IO Type |
| :---------: | :-----: |
| StrokeColor |  number |

### StrokeOpacity

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "StrokeOpacity" property-type = "get" id = "get-rectangle-strokeopacity"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "StrokeOpacity" property-type = "set" id = "set-rectangle-strokeopacity"></div>

The opacity of the stroke used to outline the map feature.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| StrokeOpacity |  number |

### StrokeWidth

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "StrokeWidth" property-type = "get" id = "get-rectangle-strokewidth"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "StrokeWidth" property-type = "set" id = "set-rectangle-strokewidth"></div>

The width of the stroke used to outline the Rectangle.

|  Param Name | IO Type |
| :---------: | :-----: |
| StrokeWidth |  number |

### Title

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Title" property-type = "get" id = "get-rectangle-title"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Title" property-type = "set" id = "set-rectangle-title"></div>

The title displayed in the info window that appears when the user clicks on the Rectangle.

| Param Name | IO Type |
| :--------: | :-----: |
|    Title   |   text  |

### Type

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Type" property-type = "get" id = "get-rectangle-type"></div>

Returns the type of the feature. For rectangles, this returns MapFeature.Rectangle ("Rectangle").

| Param Name | IO Type |
| :--------: | :-----: |
|    Type    |   text  |

### Visible

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Visible" property-type = "get" id = "get-rectangle-visible"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "Visible" property-type = "set" id = "set-rectangle-visible"></div>

Specifies whether the Rectangle should be visible on the screen. Value is true if the component is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### WestLongitude

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "WestLongitude" property-type = "get" id = "get-rectangle-westlongitude"></div>

<div block-type = "component_set_get" component-selector = "Rectangle" property-selector = "WestLongitude" property-type = "set" id = "set-rectangle-westlongitude"></div>

The west edge of the rectangle, in decimal degrees east of the equator.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| WestLongitude |  number |

## Component

---

### Rectangle

<div block-type = "component_component_block" component-selector = "Rectangle" id = "component-rectangle"></div>

Return Type : component

Component Rectangle

