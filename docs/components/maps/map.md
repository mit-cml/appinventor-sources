<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# Map

A two-dimensional container that renders map tiles in the background and allows for multiple Marker elements to identify points on the map. Map tiles are supplied by OpenStreetMap contributors and the United States Geological Survey.

The Map component provides three utilities for manipulating its boundaries within App Inventor. First, a locking mechanism is provided to allow the map to be moved relative to other components on the Screen. Second, when unlocked, the user can pan the Map to any location. At this new location, the "Set Initial Boundary" button can be pressed to save the current Map coordinates to its properties. Lastly, if the Map is moved to a different location, for example to add Markers off-screen, then the "Reset Map to Initial Bounds" button can be used to re-center the Map at the starting location.

---

## Designer Properties

---

### CenterFromString

|   Property Name  |    Editor Type   |     Default Value     |
| :--------------: | :--------------: | :-------------------: |
| CenterFromString | geographic_point | 42.359144, -71.093612 |

### EnablePan

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   EnablePan   |   boolean   |      True     |

### EnableRotation

|  Property Name | Editor Type | Default Value |
| :------------: | :---------: | :-----------: |
| EnableRotation |   boolean   |     False     |

### EnableZoom

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   EnableZoom  |   boolean   |      True     |

### LocationSensor

|  Property Name |                             Editor Type                            | Default Value |
| :------------: | :----------------------------------------------------------------: | :-----------: |
| LocationSensor | component:com.google.appinventor.components.runtime.LocationSensor |               |

### MapType

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    MapType    |   map_type  |       1       |

### Rotation

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Rotation   |    float    |      0.0      |

### ScaleUnits

| Property Name |   Editor Type   | Default Value |
| :-----------: | :-------------: | :-----------: |
|   ScaleUnits  | map_unit_system |       1       |

### ShowCompass

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  ShowCompass  |   boolean   |     False     |

### ShowScale

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ShowScale   |   boolean   |     False     |

### ShowUser

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    ShowUser   |   boolean   |     False     |

### ShowZoom

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    ShowZoom   |   boolean   |     False     |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

### ZoomLevel

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|   ZoomLevel   |   map_zoom  |       13      |

## Events

---

### BoundsChange

<div block-type = "component_event" component-selector = "Map" event-selector = "BoundsChange" id = "map-boundschange"></div>

User has changed the map bounds by panning or zooming the map.

### DoubleTapAtPoint

<div block-type = "component_event" component-selector = "Map" event-selector = "DoubleTapAtPoint" id = "map-doubletapatpoint"></div>

The user double-tapped at a point on the map. This event will be followed by a ZoomChanged event if zooming gestures are enabled and the map is not at the highest possible zoom level.

| Param Name | IO Type |
| :--------: | :-----: |
|  latitude  |  number |
|  longitude |  number |

### FeatureClick

<div block-type = "component_event" component-selector = "Map" event-selector = "FeatureClick" id = "map-featureclick"></div>

The user clicked on a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### FeatureDrag

<div block-type = "component_event" component-selector = "Map" event-selector = "FeatureDrag" id = "map-featuredrag"></div>

The user dragged a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### FeatureLongClick

<div block-type = "component_event" component-selector = "Map" event-selector = "FeatureLongClick" id = "map-featurelongclick"></div>

The user long-pressed on a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### FeatureStartDrag

<div block-type = "component_event" component-selector = "Map" event-selector = "FeatureStartDrag" id = "map-featurestartdrag"></div>

The user started dragging a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### FeatureStopDrag

<div block-type = "component_event" component-selector = "Map" event-selector = "FeatureStopDrag" id = "map-featurestopdrag"></div>

The user stopped dragging a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### GotFeatures

<div block-type = "component_event" component-selector = "Map" event-selector = "GotFeatures" id = "map-gotfeatures"></div>

A GeoJSON document was successfully read from url. The features specified in the document are provided as a list in features.

| Param Name | IO Type |
| :--------: | :-----: |
|     url    |   text  |
|  features  |   list  |

### InvalidPoint

<div block-type = "component_event" component-selector = "Map" event-selector = "InvalidPoint" id = "map-invalidpoint"></div>

An invalid coordinate was supplied during a maps operation. The message parameter will have more details about the issue.

| Param Name | IO Type |
| :--------: | :-----: |
|   message  |   text  |

### LoadError

<div block-type = "component_event" component-selector = "Map" event-selector = "LoadError" id = "map-loaderror"></div>

An error was encountered while processing a GeoJSON document at the given url. The responseCode parameter will contain an HTTP status code and the errorMessage parameter will contain a detailed error message.

|  Param Name  | IO Type |
| :----------: | :-----: |
|      url     |   text  |
| responseCode |  number |
| errorMessage |   text  |

### LongPressAtPoint

<div block-type = "component_event" component-selector = "Map" event-selector = "LongPressAtPoint" id = "map-longpressatpoint"></div>

The user long-pressed at a point on the map.

| Param Name | IO Type |
| :--------: | :-----: |
|  latitude  |  number |
|  longitude |  number |

### Ready

<div block-type = "component_event" component-selector = "Map" event-selector = "Ready" id = "map-ready"></div>

Map has been initialized and is ready for user interaction.

### TapAtPoint

<div block-type = "component_event" component-selector = "Map" event-selector = "TapAtPoint" id = "map-tapatpoint"></div>

The user tapped at a point on the map.

| Param Name | IO Type |
| :--------: | :-----: |
|  latitude  |  number |
|  longitude |  number |

### ZoomChange

<div block-type = "component_event" component-selector = "Map" event-selector = "ZoomChange" id = "map-zoomchange"></div>

User has changed the zoom level of the map.

## Methods

---

### CreateMarker

<div block-type = "component_method" component-selector = "Map" method-selector = "CreateMarker" id = "map-createmarker"></div>

Return Type : component

Create a new marker with default properties at the specified latitude and longitude.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |

### FeatureFromDescription

<div block-type = "component_method" component-selector = "Map" method-selector = "FeatureFromDescription" id = "map-featurefromdescription"></div>

Return Type : any

Converts a feature description into an App Inventor map feature. Points are converted into

|  Param Name | Input Type |
| :---------: | :--------: |
| description |    list    |

### LoadFromURL

<div block-type = "component_method" component-selector = "Map" method-selector = "LoadFromURL" id = "map-loadfromurl"></div>

Return Type : No Return Value

Load a feature collection in [GeoJSON](https://en.wikipedia.org/wiki/GeoJSON) format from the given url. On success, the event GotFeatures will be raised with the given url and a list of the features parsed from the GeoJSON as a list of (key, value) pairs. On failure, the LoadError event will be raised with any applicable HTTP response code and error message.

| Param Name | Input Type |
| :--------: | :--------: |
|     url    |    text    |

### PanTo

<div block-type = "component_method" component-selector = "Map" method-selector = "PanTo" id = "map-panto"></div>

Return Type : No Return Value

Pans the map center to the given latitude and longitude and adjust the zoom level to the specified zoom.

| Param Name | Input Type |
| :--------: | :--------: |
|  latitude  |   number   |
|  longitude |   number   |
|    zoom    |   number   |

### Save

<div block-type = "component_method" component-selector = "Map" method-selector = "Save" id = "map-save"></div>

Return Type : No Return Value

Save the contents of the Map to the specified path.

| Param Name | Input Type |
| :--------: | :--------: |
|    path    |    text    |

## Block Properties

---

### BoundingBox

<div block-type = "component_set_get" component-selector = "Map" property-selector = "BoundingBox" property-type = "get" id = "get-map-boundingbox"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "BoundingBox" property-type = "set" id = "set-map-boundingbox"></div>

Bounding box for the map stored as \[\[North, West\], \[South, East\]\].

|  Param Name | IO Type |
| :---------: | :-----: |
| BoundingBox |   list  |

### CenterFromString

<div block-type = "component_set_get" component-selector = "Map" property-selector = "CenterFromString" property-type = "set" id = "set-map-centerfromstring"></div>

Set the initial center coordinate of the map. The value is specified as a comma-separated pair of decimal latitude and longitude coordinates, for example, `42.359144, -71.093612`.

In blocks code, it is recommended for performance reasons to use SetCenter with numerical latitude and longitude rather than convert to the string representation for use with this property.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| CenterFromString |   text  |

### EnablePan

<div block-type = "component_set_get" component-selector = "Map" property-selector = "EnablePan" property-type = "get" id = "get-map-enablepan"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "EnablePan" property-type = "set" id = "set-map-enablepan"></div>

Enable two-finger panning of the Map

| Param Name | IO Type |
| :--------: | :-----: |
|  EnablePan | boolean |

### EnableRotation

<div block-type = "component_set_get" component-selector = "Map" property-selector = "EnableRotation" property-type = "get" id = "get-map-enablerotation"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "EnableRotation" property-type = "set" id = "set-map-enablerotation"></div>

Enable rotating the map based on the user's orientation.

|   Param Name   | IO Type |
| :------------: | :-----: |
| EnableRotation | boolean |

### EnableZoom

<div block-type = "component_set_get" component-selector = "Map" property-selector = "EnableZoom" property-type = "get" id = "get-map-enablezoom"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "EnableZoom" property-type = "set" id = "set-map-enablezoom"></div>

Set whether the user can zoom the map using touch gestures. This value does not affect whether the user can zoom using the zoom controls provided by [ShowZoom](#Map.ShowZoom).

| Param Name | IO Type |
| :--------: | :-----: |
| EnableZoom | boolean |

### Features

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Features" property-type = "get" id = "get-map-features"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Features" property-type = "set" id = "set-map-features"></div>

The list of features placed on this Map. This list also includes any features created by calls to FeatureFromDescription

| Param Name | IO Type |
| :--------: | :-----: |
|  Features  |   list  |

### Height

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Height" property-type = "get" id = "get-map-height"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Height" property-type = "set" id = "set-map-height"></div>

Specifies the vertical height of the Map, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "Map" property-selector = "HeightPercent" property-type = "set" id = "set-map-heightpercent"></div>

Specifies the vertical height of the Map as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Latitude

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Latitude" property-type = "get" id = "get-map-latitude"></div>

The latitude of the center of the map.

| Param Name | IO Type |
| :--------: | :-----: |
|  Latitude  |  number |

### LocationSensor

<div block-type = "component_set_get" component-selector = "Map" property-selector = "LocationSensor" property-type = "set" id = "set-map-locationsensor"></div>

Uses the provided LocationSensor for user location data rather than the built-in location provider.

|   Param Name   |  IO Type  |
| :------------: | :-------: |
| LocationSensor | component |

### Longitude

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Longitude" property-type = "get" id = "get-map-longitude"></div>

The longitude of the center of the map.

| Param Name | IO Type |
| :--------: | :-----: |
|  Longitude |  number |

### MapType

<div block-type = "component_set_get" component-selector = "Map" property-selector = "MapType" property-type = "get" id = "get-map-maptype"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "MapType" property-type = "set" id = "set-map-maptype"></div>

Set the type of map tile used for the base tile layer. Valid values are: 1. Roads 2. Aerial 3. Terrain

| Param Name | IO Type |
| :--------: | :-----: |
|   MapType  |  number |

### Rotation

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Rotation" property-type = "get" id = "get-map-rotation"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Rotation" property-type = "set" id = "set-map-rotation"></div>

Sets or gets the rotation of the map in decimal degrees if any

| Param Name | IO Type |
| :--------: | :-----: |
|  Rotation  |  number |

### ScaleUnits

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ScaleUnits" property-type = "get" id = "get-map-scaleunits"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ScaleUnits" property-type = "set" id = "set-map-scaleunits"></div>

Specifies the units used for the scale overlay. 1 (the default) will give metric units (km, m) whereas 2 will give imperial units (mi, ft).

| Param Name | IO Type |
| :--------: | :-----: |
| ScaleUnits |  number |

### ShowCompass

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ShowCompass" property-type = "get" id = "get-map-showcompass"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ShowCompass" property-type = "set" id = "set-map-showcompass"></div>

Show a compass on the map. If the device provides a digital compass, orientation changes will be used to rotate the compass icon.

|  Param Name | IO Type |
| :---------: | :-----: |
| ShowCompass | boolean |

### ShowScale

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ShowScale" property-type = "get" id = "get-map-showscale"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ShowScale" property-type = "set" id = "set-map-showscale"></div>

Shows or hides a scale overlay on the

| Param Name | IO Type |
| :--------: | :-----: |
|  ShowScale | boolean |

### ShowUser

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ShowUser" property-type = "get" id = "get-map-showuser"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ShowUser" property-type = "set" id = "set-map-showuser"></div>

Shows or hides an icon indicating the user's current location on the

| Param Name | IO Type |
| :--------: | :-----: |
|  ShowUser  | boolean |

### ShowZoom

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ShowZoom" property-type = "get" id = "get-map-showzoom"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ShowZoom" property-type = "set" id = "set-map-showzoom"></div>

Shows or hides an icon indicating the user's current location on the

| Param Name | IO Type |
| :--------: | :-----: |
|  ShowZoom  | boolean |

### UserLatitude

<div block-type = "component_set_get" component-selector = "Map" property-selector = "UserLatitude" property-type = "get" id = "get-map-userlatitude"></div>

Returns the user's latitude if ShowUser is enabled.

|  Param Name  | IO Type |
| :----------: | :-----: |
| UserLatitude |  number |

### UserLongitude

<div block-type = "component_set_get" component-selector = "Map" property-selector = "UserLongitude" property-type = "get" id = "get-map-userlongitude"></div>

Returns the user's longitude if ShowUser is enabled.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| UserLongitude |  number |

### Visible

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Visible" property-type = "get" id = "get-map-visible"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Visible" property-type = "set" id = "set-map-visible"></div>

Specifies whether the Map should be visible on the screen. Value is true if the Map is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Width" property-type = "get" id = "get-map-width"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "Width" property-type = "set" id = "set-map-width"></div>

Specifies the horizontal width of the Map, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "Map" property-selector = "WidthPercent" property-type = "set" id = "set-map-widthpercent"></div>

Specifies the horizontal width of the Map as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

### ZoomLevel

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ZoomLevel" property-type = "get" id = "get-map-zoomlevel"></div>

<div block-type = "component_set_get" component-selector = "Map" property-selector = "ZoomLevel" property-type = "set" id = "set-map-zoomlevel"></div>

Specifies the zoom level of the map. Valid values of ZoomLevel are dependent on the tile provider and the latitude and longitude of the map. For example, zoom levels are more constrained over oceans than dense city centers to conserve space for storing tiles, so valid values may be 1-7 over ocean and 1-20 over cities. Tile providers may send warning or error tiles if the zoom level is too great for the server to support.

| Param Name | IO Type |
| :--------: | :-----: |
|  ZoomLevel |  number |

## Component

---

### Map

<div block-type = "component_component_block" component-selector = "Map" id = "component-map"></div>

Return Type : component

Component Map

