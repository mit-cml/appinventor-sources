# Marker

An icon positioned at a point to indicate information on a map. Markers can be used to provide an info window, custom fill and stroke colors, and custom images to convey information to the user.

---

## Designer Properties

---

### AnchorHorizontal

|   Property Name  |      Editor Type     | Default Value |
| :--------------: | :------------------: | :-----------: |
| AnchorHorizontal | horizontal_alignment |       3       |

### AnchorVertical

|  Property Name |     Editor Type    | Default Value |
| :------------: | :----------------: | :-----------: |
| AnchorVertical | vertical_alignment |       3       |

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

### ImageAsset

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ImageAsset  |    asset    |               |

### Latitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Latitude   |   latitude  |       0       |

### Longitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   Longitude   |  longitude  |       0       |

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

<div block-type = "component_event" component-selector = "Marker" event-selector = "Click" event-params = "" id = "marker-click"></div>

The user clicked on the Marker.

### Drag

<div block-type = "component_event" component-selector = "Marker" event-selector = "Drag" event-params = "" id = "marker-drag"></div>

The user dragged the Marker.

### LongClick

<div block-type = "component_event" component-selector = "Marker" event-selector = "LongClick" event-params = "" id = "marker-longclick"></div>

The user long-pressed on the Marker. This event will only trigger if Draggable is false.

### StartDrag

<div block-type = "component_event" component-selector = "Marker" event-selector = "StartDrag" event-params = "" id = "marker-startdrag"></div>

The user started a drag operation.

### StopDrag

<div block-type = "component_event" component-selector = "Marker" event-selector = "StopDrag" event-params = "" id = "marker-stopdrag"></div>

The user stopped a drag operation.

## Methods

---

### BearingToFeature

<div block-type = "component_method" component-selector = "Marker" method-selector = "BearingToFeature" method-params = "mapFeature-centroids" return-type = "number" id = "marker-bearingtofeature"></div>

Return Type : number

Returns the bearing from the Marker to the given map feature, in degrees from due north. If the centroids parameter is true, the bearing will be to the center of the map feature. Otherwise, the bearing will be computed to the point in the feature nearest the Marker.

| Param Name | Input Type |
| :--------: | :--------: |
| mapFeature |  component |
|  centroids |   boolean  |

### BearingToPoint

<div block-type = "component_method" component-selector = "Marker" method-selector = "BearingToPoint" method-params = "latitude-longitude" return-type = "number" id = "marker-bearingtopoint"></div>

Return Type : number

Returns the bearing from the Marker to the given latitude and longitude, in degrees from due north.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |

### DistanceToFeature

<div block-type = "component_method" component-selector = "Marker" method-selector = "DistanceToFeature" method-params = "mapFeature-centroids" return-type = "number" id = "marker-distancetofeature"></div>

Return Type : number

Compute the distance, in meters, between two map features.

| Param Name | Input Type |
| :--------: | :--------: |
| mapFeature |  component |
|  centroids |   boolean  |

### DistanceToPoint

<div block-type = "component_method" component-selector = "Marker" method-selector = "DistanceToPoint" method-params = "latitude-longitude" return-type = "number" id = "marker-distancetopoint"></div>

Return Type : number

Compute the distance, in meters, between a Marker and a latitude, longitude point.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |

### HideInfobox

<div block-type = "component_method" component-selector = "Marker" method-selector = "HideInfobox" method-params = "" return-type = "undefined" id = "marker-hideinfobox"></div>

Return Type : No Return Value

Hide the infobox if it is shown. If the infobox is not visible this function has no effect.

### SetLocation

<div block-type = "component_method" component-selector = "Marker" method-selector = "SetLocation" method-params = "latitude-longitude" return-type = "undefined" id = "marker-setlocation"></div>

Return Type : No Return Value

Set the location of the marker.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |

### ShowInfobox

<div block-type = "component_method" component-selector = "Marker" method-selector = "ShowInfobox" method-params = "" return-type = "undefined" id = "marker-showinfobox"></div>

Return Type : No Return Value

Show the infobox for the Marker. This will show the infobox even if EnableInfobox is set to false.

## Block Properties

---

### AnchorHorizontal

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "AnchorHorizontal" property-type = "get" id = "get-marker-anchorhorizontal"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "AnchorHorizontal" property-type = "set" id = "set-marker-anchorhorizontal"></div>

The horizontal alignment property controls where the Marker's anchor is located relative to its width. The choices are: 1 = left aligned, 3 = horizontally centered, 2 = right aligned.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| AnchorHorizontal |  number |

### AnchorVertical

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "AnchorVertical" property-type = "get" id = "get-marker-anchorvertical"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "AnchorVertical" property-type = "set" id = "set-marker-anchorvertical"></div>

The vertical alignment property controls where the Marker's anchor is located relative to its height. The choices are: 1 = aligned at the top, 2 = vertically centered, 3 = aligned at the bottom.

|   Param Name   | IO Type |
| :------------: | :-----: |
| AnchorVertical |  number |

### Description

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Description" property-type = "get" id = "get-marker-description"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Description" property-type = "set" id = "set-marker-description"></div>

The description displayed in the info window that appears when the user clicks on the Marker.

|  Param Name | IO Type |
| :---------: | :-----: |
| Description |   text  |

### Draggable

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Draggable" property-type = "get" id = "get-marker-draggable"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Draggable" property-type = "set" id = "set-marker-draggable"></div>

The Draggable property is used to set whether or not the user can drag the Marker by long-pressing and then dragging the Marker to a new location.

| Param Name | IO Type |
| :--------: | :-----: |
|  Draggable | boolean |

### EnableInfobox

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "EnableInfobox" property-type = "get" id = "get-marker-enableinfobox"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "EnableInfobox" property-type = "set" id = "set-marker-enableinfobox"></div>

Enable or disable the infobox window display when the user taps the Marker.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| EnableInfobox | boolean |

### FillColor

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "FillColor" property-type = "get" id = "get-marker-fillcolor"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "FillColor" property-type = "set" id = "set-marker-fillcolor"></div>

The paint color used to fill in the Marker.

| Param Name | IO Type |
| :--------: | :-----: |
|  FillColor |  number |

### FillOpacity

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "FillOpacity" property-type = "get" id = "get-marker-fillopacity"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "FillOpacity" property-type = "set" id = "set-marker-fillopacity"></div>

The opacity of the interior of the map feature.

|  Param Name | IO Type |
| :---------: | :-----: |
| FillOpacity |  number |

### Height

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Height" property-type = "get" id = "get-marker-height"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Height" property-type = "set" id = "set-marker-height"></div>

Specifies the \`Marker\`'s vertical height, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "HeightPercent" property-type = "set" id = "set-marker-heightpercent"></div>

Specifies the \`Marker\`'s vertical height as a percentage of the \[\`Screen\`'s \`Height\`\](userinterface.html#Screen.Height).

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### ImageAsset

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "ImageAsset" property-type = "get" id = "get-marker-imageasset"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "ImageAsset" property-type = "set" id = "set-marker-imageasset"></div>

Specifies the image shown for the \`Marker\`. If set to the empty string "", then the default marker icon will be used.

| Param Name | IO Type |
| :--------: | :-----: |
| ImageAsset |   text  |

### Latitude

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Latitude" property-type = "get" id = "get-marker-latitude"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Latitude" property-type = "set" id = "set-marker-latitude"></div>

Sets or gets the latitude of the \`Marker\`, in degrees, with positive values representing north of the equator and negative values representing south of the equator. To update the \`Latitude\` and

| Param Name | IO Type |
| :--------: | :-----: |
|  Latitude  |  number |

### Longitude

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Longitude" property-type = "get" id = "get-marker-longitude"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Longitude" property-type = "set" id = "set-marker-longitude"></div>

Sets or gets the longitude of the \`Marker\`, in degrees, with positive values representing east of the prime meridian and negative values representing west of the prime meridian. To update the

| Param Name | IO Type |
| :--------: | :-----: |
|  Longitude |  number |

### StrokeColor

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "StrokeColor" property-type = "get" id = "get-marker-strokecolor"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "StrokeColor" property-type = "set" id = "set-marker-strokecolor"></div>

The paint color used to outline the Marker.

|  Param Name | IO Type |
| :---------: | :-----: |
| StrokeColor |  number |

### StrokeOpacity

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "StrokeOpacity" property-type = "get" id = "get-marker-strokeopacity"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "StrokeOpacity" property-type = "set" id = "set-marker-strokeopacity"></div>

The opacity of the stroke used to outline the map feature.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| StrokeOpacity |  number |

### StrokeWidth

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "StrokeWidth" property-type = "get" id = "get-marker-strokewidth"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "StrokeWidth" property-type = "set" id = "set-marker-strokewidth"></div>

The width of the stroke used to outline the Marker.

|  Param Name | IO Type |
| :---------: | :-----: |
| StrokeWidth |  number |

### Title

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Title" property-type = "get" id = "get-marker-title"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Title" property-type = "set" id = "set-marker-title"></div>

The title displayed in the info window that appears when the user clicks on the Marker.

| Param Name | IO Type |
| :--------: | :-----: |
|    Title   |   text  |

### Type

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Type" property-type = "get" id = "get-marker-type"></div>

Returns the type of the feature. For Markers, this returns MapFeature.Marker ("Marker").

| Param Name | IO Type |
| :--------: | :-----: |
|    Type    |   text  |

### Visible

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Visible" property-type = "get" id = "get-marker-visible"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Visible" property-type = "set" id = "set-marker-visible"></div>

Specifies whether the Marker should be visible on the screen. Value is true if the component is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Width" property-type = "get" id = "get-marker-width"></div>

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "Width" property-type = "set" id = "set-marker-width"></div>

Specifies the horizontal width of the \`Marker\`, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "Marker" property-selector = "WidthPercent" property-type = "set" id = "set-marker-widthpercent"></div>

Specifies the horizontal width of the \`Marker\` as a percentage of the \[\`Screen\`'s \`Width\`\](userinterface.html#Screen.Width).

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### Marker

<div block-type = "component_component_block" component-selector = "Marker" id = "component-marker"></div>

Return Type : component

Component Marker

