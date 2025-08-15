---
layout: documentation
title: Maps
---

[&laquo; Back to index](index.html)
# Maps

Table of Contents:

* [Circle](#Circle)
* [FeatureCollection](#FeatureCollection)
* [LineString](#LineString)
* [Map](#Map)
* [Marker](#Marker)
* [Navigation](#Navigation)
* [Polygon](#Polygon)
* [Rectangle](#Rectangle)

## Circle  {#Circle}

The `Circle` component visualizes a circle of a given [`Radius`](#Circle.Radius), in meters,
 centered at a [`Latitude`](#Circle.Latitude) and [`Longitude`](#Circle.Longitude). The `Circle`'s appearance
 cnn be customized using properties such as [`FillColor`](#Circle.FillColor), [`StrokeColor`](#Circle.StrokeColor),
 and [`StrokeWidth`](#Circle.StrokeWidth).

 The `Circle` component can also be used to implement features such as geofencing, a mechanism
 where the user's presence within an area is used to trigger other behaviors. Using the
 [`DistanceToPoint`](#Circle.DistanceToPoint) method combined with the
 [`LocationSensor`](sensors.html#LocationSensor), you can determine whether a user's location is
 inside or outside of the `Circle`. You can use this feature to trigger additional actions.



### Properties  {#Circle-Properties}

{:.properties}

{:id="Circle.Description" .text} *Description*
: Sets or gets the description displayed in the info window. The info window appears when the
 user taps on the `Circle`.

{:id="Circle.Draggable" .boolean} *Draggable*
: Sets or gets whether or not the user can drag a map feature. This feature is accessed by
 long-pressing and then dragging the `Circle` to a new location.

{:id="Circle.EnableInfobox" .boolean} *EnableInfobox*
: Enables or disables the infobox window display when the user taps the `Circle`.

{:id="Circle.FillColor" .color} *FillColor*
: Sets or gets the color used to fill in the `Circle`.

{:id="Circle.FillOpacity" .number} *FillOpacity*
: Sets or gets the opacity of the color used to fill the `Circle`. A value of 0.0 will be
 completely invisible and a value of 1.0 will be completely opaque.

{:id="Circle.Latitude" .number} *Latitude*
: Sets or gets the latitude of the center of the circle, in degrees. Positive values represent
 north of the equator and negative values represent south of the equator. To update the
 latitude and longitude simultaneously, use the [`SetLocation`](#Circle.SetLocation) method.

{:id="Circle.Longitude" .number} *Longitude*
: Sets or gets the longitude of the center of the circle, in degrees. Positive values represent
 east of the prime meridian and negative values represent west of the prime meridian. To update
 the latitude and longitude simultaneously, use the [`SetLocation`](#Circle.SetLocation) method.

{:id="Circle.Radius" .number} *Radius*
: Sets or gets the radius of the circle, in meters.

{:id="Circle.StrokeColor" .color} *StrokeColor*
: Sets or gets the color used to outline the `Circle`.

{:id="Circle.StrokeOpacity" .number} *StrokeOpacity*
: Sets or gets the opacity of the outline of the `Circle`. A value of 0.0 will be invisible and
 a value of 1.0 will be opaque.

{:id="Circle.StrokeWidth" .number} *StrokeWidth*
: Sets or gets the width of the stroke used to outline the `Circle`.

{:id="Circle.Title" .text} *Title*
: Sets or gets the title displayed in the info window that appears when the user clicks on the
 map feature.

{:id="Circle.Type" .text .ro .bo} *Type*
: Returns the type of the feature. For Circles, this returns MapFeature.Circle ("Circle").

{:id="Circle.Visible" .boolean} *Visible*
: Specifies whether the `Circle` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `Circle` is showing and `false`{:.logic.block} if hidden.

### Events  {#Circle-Events}

{:.events}

{:id="Circle.Click"} Click()
: The `Click` event runs when the user taps on the `Circle`.

{:id="Circle.Drag"} Drag()
: The `Drag` event runs in response to position updates of the `Circle` as the user drags it.

{:id="Circle.LongClick"} LongClick()
: The `LongClick` event runs when the user presses and holds the `Circle` and then releases it.
 This event will only trigger if [`Draggable`](#Circle.Draggable) is `false`{:.logic.block} because it
 uses the same gesture as [`StartDrag`](#Circle.StartDrag).

{:id="Circle.StartDrag"} StartDrag()
: The `StartDrag` event runs when the user presses and holds the `Circle` and then proceeds to
 move their finger on the screen. It will be followed by the [`Drag`](#Circle.Drag) and
 [`StopDrag`](#Circle.StopDrag) events.

{:id="Circle.StopDrag"} StopDrag()
: The `StopDrag` event runs when the user releases the `Circle` at the end of a drag.

### Methods  {#Circle-Methods}

{:.methods}

{:id="Circle.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Computes the distance between the `Circle` and the given `mapFeature`. If `centroids` is
 `true`{:.logic.block}, the computation is done between the centroids of the two features.
 Otherwise, the distance will be computed between the two features based on the closest points.
 Further, when `centroids` is `false`{:.logic.block}, this method will return 0 if the `Circle`
 intersects or contains the `mapFeature`. If an error occurs, this method will return -1.

{:id="Circle.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number},*centroid*{:.boolean})
: Computes the distance between the `Circle` and the given `latitude` and `longitude`. If
 `centroids` is `true`{:.logic.block}, the distance is computed from the center of the `Circle`
 to the given point. Otherwise, the distance is computed from the closest point on the `Circle`
 to the given point. Further, this method will return 0 if `centroids` is `false`{:.logic.block}
 and the point is in the `Circle`. If an error occurs, -1 will be returned.

{:id="Circle.HideInfobox" class="method"} <i/> HideInfobox()
: Hides the `Circle`'s info box if it is visible. Otherwise, no action is taken.

{:id="Circle.SetLocation" class="method"} <i/> SetLocation(*latitude*{:.number},*longitude*{:.number})
: Moves the center of the `Circle` to the given `latitude` and `longitude`. This method is more
 efficient than setting [`Latitude`](#Circle.Latitude) and [`Longitude`](#Circle.Longitude) separately.

{:id="Circle.ShowInfobox" class="method"} <i/> ShowInfobox()
: Shows the info box for the `Circle` if it is not visible. Otherwise, this method has no effect.
 This method can be used to show the info box even if [`EnableInfobox`](#Circle.EnableInfobox) is false.

## FeatureCollection  {#FeatureCollection}

A `FeatureCollection` groups one or more map features together. Any events that occur on a
 feature in the collection will also trigger the corresponding event in the collection component.
 `FeatureCollection`s can be loaded from external resources to populate [`Map`](#Map)s with content.
 GeoJSON is the only format supported at this time.



### Properties  {#FeatureCollection-Properties}

{:.properties}

{:id="FeatureCollection.Features" .list .bo} *Features*
: Gets the list of features attached to the `FeatureCollection` (without regard to the value of the
 feature's `Visible`{:.getter.block} property). This list also includes any features created on
 the `FeatureCollection` by calls to [`FeatureFromDescription`](#FeatureCollection.FeatureFromDescription).

{:id="FeatureCollection.FeaturesFromGeoJSON" .text .wo} *FeaturesFromGeoJSON*
: Populates the feature collection from a string containing GeoJSON content. Given the size of
 such strings, it is recommended to load the feature collection from assets or the web using
 the [`Source`](#FeatureCollection.Source) property.

{:id="FeatureCollection.Height" .number .bo} *Height*
: Specifies the `FeatureCollection`'s vertical height, measured in pixels.

{:id="FeatureCollection.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `FeatureCollection`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="FeatureCollection.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="FeatureCollection.Source" .text .ro} *Source*
: Specifies the source URL used to populate the feature collection. If the feature collection was not loaded from a URL, this will be the empty string.

{:id="FeatureCollection.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="FeatureCollection.Visible" .boolean} *Visible*
: Specifies whether the `FeatureCollection` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `FeatureCollection` is showing and `false`{:.logic.block} if hidden.

{:id="FeatureCollection.Width" .number .bo} *Width*
: Specifies the horizontal width of the `FeatureCollection`, measured in pixels.

{:id="FeatureCollection.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `FeatureCollection` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#FeatureCollection-Events}

{:.events}

{:id="FeatureCollection.FeatureClick"} FeatureClick(*feature*{:.component})
: When a feature is clicked, the parent `FeatureCollection` will also receive a `FeatureClick` event.
 The `feature` parameter indicates which child feature was clicked. This event is run *after*
 the `Click` event on the corresponding feature and after the `when any ... Click` event if one
 is provided.

{:id="FeatureCollection.FeatureDrag"} FeatureDrag(*feature*{:.component})
: When the user drags a feature, the parent `FeatureCollection` will also receive a `FeatureDrag` event.
 The `feature` parameter indicates which child feature was dragged. This event is run *after*
 the `Drag` event on the corresponding feature and after the `when any ... Drag` event if one is
 provided.

{:id="FeatureCollection.FeatureLongClick"} FeatureLongClick(*feature*{:.component})
: When a feature is long-clicked, the parent `FeatureCollection` will also receive a `FeatureLongClick`
 event. The `feature` parameter indicates which child feature was long-clicked. This event is
 run *after* the `LongClick` event on the corresponding feature and after the
 `when any ... LongClick` event if one is provided.

{:id="FeatureCollection.FeatureStartDrag"} FeatureStartDrag(*feature*{:.component})
: When the user starts dragging a feature, the parent `FeatureCollection` will also receive a
 `FeatureStartDrag` event. The `feature` parameter indicates which child feature was dragged.
 This event is run *after* the `StartDrag` event on the corresponding feature and after the
 `when any ... StartDrag` event if one is provided.

{:id="FeatureCollection.FeatureStopDrag"} FeatureStopDrag(*feature*{:.component})
: When the user stops dragging a feature, the parent `FeatureCollection` will also receive a
 `FeatureStopDrag` event. The `feature` parameter indicates which child feature was dragged.
 This event is run *after* the `StopDrag` event on the corresponding feature and after the
 `when any ... StopDrag` event if one is provided.

{:id="FeatureCollection.GotFeatures"} GotFeatures(*url*{:.text},*features*{:.list})
: The `GotFeatures` event is run when when a feature collection is successfully read from the
 given `url`{:.variable.block}. The `features`{:.variable.block} parameter will be a list of
 feature descriptions that can be converted into components using the
 [`FeatureFromDescription`](#FeatureCollection.FeatureFromDescription) method.

{:id="FeatureCollection.LoadError"} LoadError(*url*{:.text},*responseCode*{:.number},*errorMessage*{:.text})
: The `LoadError` event is run when an error occurs while processing a feature collection
 document at the given `url`{:.variable.block}. The `responseCode`{:.variable.block} parameter
 will contain an HTTP status code and the `errorMessage`{:.variable.block} parameter will
 contain a detailed error message.

### Methods  {#FeatureCollection-Methods}

{:.methods}

{:id="FeatureCollection.FeatureFromDescription" class="method returns any"} <i/> FeatureFromDescription(*description*{:.list})
: Converts a feature description into an App Inventor map feature. Points are converted into
 [`Marker`](#Marker) components, LineStrings are converted into [`LineString`](#LineString) components, and
 Polygons (and MultiPolygons) are converted into [`Polygon`](#Polygon) components. If the feature has
 properties, they will be mapped into App Inventor properties using the following mapping:

   * description becomes `Description`
   * draggable becomes `Draggable`
   * infobox becomes `EnableInfobox`
   * fill becomes `FillColor`
   * fill-opacity becomes `FillOpacity`
   * image becomes `ImageAsset`
   * stroke becomes `StrokeColor`
   * stroke-opacity becomes `StrokeOpacity`
   * stroke-width becomes `StrokeWidth`
   * title becomes `Title`
   * visible becomes `Visible`

{:id="FeatureCollection.LoadFromURL" class="method"} <i/> LoadFromURL(*url*{:.text})
: Loads a feature collection in GeoJSON format from the given `url`. On success,
 the event [`GotFeatures`](#FeatureCollection.GotFeatures) will be raised with the given `url`
 and a list of `feature`s parsed from the GeoJSON as a list of (key, value) pairs. On failure,
 the [`LoadError`](#FeatureCollection.LoadError) event will be raised with any applicable HTTP
 response code and error message.

## LineString  {#LineString}

`LineString` is a component for drawing an open, continuous sequence of lines on a `Map`. To add
 new points to a `LineString` in the designer, drag the midpoint of any segment away from the
 line to introduce a new vertex. Move a vertex by clicking and dragging the vertex to a new
 location. Clicking on a vertex will delete the vertex, unless only two remain.



### Properties  {#LineString-Properties}

{:.properties}

{:id="LineString.Description" .text} *Description*
: Sets or gets the description displayed in the info window. The info window appears when the
 user taps on the `LineString`.

{:id="LineString.Draggable" .boolean} *Draggable*
: Sets or gets whether or not the user can drag a map feature. This feature is accessed by
 long-pressing and then dragging the `LineString` to a new location.

{:id="LineString.EnableInfobox" .boolean} *EnableInfobox*
: Enables or disables the infobox window display when the user taps the `LineString`.

{:id="LineString.Points" .list .bo} *Points*
: The list of points, as pairs of latitudes and longitudes, in the `LineString`.

{:id="LineString.PointsFromString" .text .wo} *PointsFromString*
: Set the points of the LineString from a specially-coded character string of the form:
 [[latitude1, longitude1], [latitude2, longitude2], ...]

{:id="LineString.StrokeColor" .color} *StrokeColor*
: Sets or gets the color used to outline the `LineString`.

{:id="LineString.StrokeOpacity" .number} *StrokeOpacity*
: Sets or gets the opacity of the outline of the `LineString`. A value of 0.0 will be invisible and
 a value of 1.0 will be opaque.

{:id="LineString.StrokeWidth" .number} *StrokeWidth*
: Sets or gets the width of the stroke used to outline the `LineString`.

{:id="LineString.Title" .text} *Title*
: Sets or gets the title displayed in the info window that appears when the user clicks on the
 map feature.

{:id="LineString.Type" .text .ro .bo} *Type*
: Returns the type of the map feature. For LineString, this returns MapFeature.LineString ("LineString").

{:id="LineString.Visible" .boolean} *Visible*
: Specifies whether the `LineString` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `LineString` is showing and `false`{:.logic.block} if hidden.

### Events  {#LineString-Events}

{:.events}

{:id="LineString.Click"} Click()
: The `Click` event runs when the user taps on the `LineString`.

{:id="LineString.Drag"} Drag()
: The `Drag` event runs in response to position updates of the `LineString` as the user drags it.

{:id="LineString.LongClick"} LongClick()
: The `LongClick` event runs when the user presses and holds the `LineString` and then releases it.
 This event will only trigger if [`Draggable`](#LineString.Draggable) is `false`{:.logic.block} because it
 uses the same gesture as [`StartDrag`](#LineString.StartDrag).

{:id="LineString.StartDrag"} StartDrag()
: The `StartDrag` event runs when the user presses and holds the `LineString` and then proceeds to
 move their finger on the screen. It will be followed by the [`Drag`](#LineString.Drag) and
 [`StopDrag`](#LineString.StopDrag) events.

{:id="LineString.StopDrag"} StopDrag()
: The `StopDrag` event runs when the user releases the `LineString` at the end of a drag.

### Methods  {#LineString-Methods}

{:.methods}

{:id="LineString.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Computes the distance between the `LineString` and the given `mapFeature`. If `centroids` is
 `true`{:.logic.block}, the computation is done between the centroids of the two features.
 Otherwise, the distance will be computed between the two features based on the closest points.
 Further, when `centroids` is `false`{:.logic.block}, this method will return 0 if the `LineString`
 intersects or contains the `mapFeature`. If an error occurs, this method will return -1.

{:id="LineString.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number},*centroid*{:.boolean})
: Computes the distance between the `LineString` and the given `latitude` and `longitude`. If
 `centroids` is `true`{:.logic.block}, the distance is computed from the center of the `LineString`
 to the given point. Otherwise, the distance is computed from the closest point on the `LineString`
 to the given point. Further, this method will return 0 if `centroids` is `false`{:.logic.block}
 and the point is in the `LineString`. If an error occurs, -1 will be returned.

{:id="LineString.HideInfobox" class="method"} <i/> HideInfobox()
: Hides the `LineString`'s info box if it is visible. Otherwise, no action is taken.

{:id="LineString.ShowInfobox" class="method"} <i/> ShowInfobox()
: Shows the info box for the `LineString` if it is not visible. Otherwise, this method has no effect.
 This method can be used to show the info box even if [`EnableInfobox`](#LineString.EnableInfobox) is false.

## Map  {#Map}

A two-dimensional container that renders map tiles in the background and allows for multiple
 [`Marker`](#Marker) elements to identify points on the map. Map tiles are supplied by OpenStreetMap
 contributors and the the United States Geological Survey, or a custom basemap URL can be provided.

 The `Map` component provides three utilities for manipulating its boundaries with App Inventor.
 First, a locking mechanism is provided to allow the map to be moved relative to other components
 on the Screen. Second, when unlocked, the user can pan the `Map` to any location. At this new
 location, the &quot;Set Initial Boundary&quot; button can be pressed to save the current `Map`
 coordinates to its properties. Lastly, if the `Map` is moved to a different location, for example
 to add [`Marker`](#Marker)s off-screen, then the &quot;Reset Map to Initial Bounds&quot; button can
 be used to re-center the `Map` at the starting location.



### Properties  {#Map-Properties}

{:.properties}

{:id="Map.BoundingBox" .list .bo} *BoundingBox*
: Sets or gets the current boundary for the map's drawn view. The value is a list of lists
 containing the northwest and southeast coordinates of the current view in the form
 ``((North West) (South East))``.

{:id="Map.CenterFromString" .text .wo} *CenterFromString*
: Set the initial center coordinate of the map. The value is specified as a
 comma-separated pair of decimal latitude and longitude coordinates, for example,
 `42.359144, -71.093612`.

   In blocks code, it is recommended for performance reasons to use
 [`PanTo`](#Map.PanTo) with numerical latitude and longitude rather than convert
 to the string representation for use with this property.

{:id="Map.CustomUrl" .text} *CustomUrl*
: Update the custom URL of the base tile layer in use by the map.
 e.g. https://tile.openstreetmap.org/{z}/{x}/{y}.png
 e.g. https://example.com/geoserver/gwc/service/tms/1.0.0/workspace:layername

{:id="Map.EnablePan" .boolean} *EnablePan*
: Enables or disables the ability of the user to move the Map.

{:id="Map.EnableRotation" .boolean} *EnableRotation*
: Enables or disables the two-finger rotation gesture to rotate the Map.

{:id="Map.EnableZoom" .boolean} *EnableZoom*
: Enables or disables the two-finger pinch gesture to zoom the Map.

{:id="Map.Features" .list .bo} *Features*
: Gets the list of features attached to the `Map` (without regard to the value of the
 feature's `Visible`{:.getter.block} property). This list also includes any features created on
 the `Map` by calls to [`FeatureFromDescription`](#Map.FeatureFromDescription).

{:id="Map.Height" .number .bo} *Height*
: Specifies the `Map`'s vertical height, measured in pixels.

{:id="Map.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `Map`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="Map.Latitude" .number .ro .bo} *Latitude*
: Gets the latitude of the center of the Map. To change the latitude, use the
 [`PanTo`](#Map.PanTo) method.

{:id="Map.Left" .number} *Left*
: Specifies the position of the Left edge of the component relative to an
 AbsoluteArrangement.

{:id="Map.LocationSensor" .component .wo} *LocationSensor*
: Uses the provided [`LocationSensor`](sensors.html#LocationSensor) for user location data
 rather than the built-in location provider.

{:id="Map.Longitude" .number .ro .bo} *Longitude*
: Gets the longitude of the center of the Map. To change the longitude, use the
 [`PanTo`](#Map.PanTo) method.

{:id="Map.MapType" .number} *MapType*
: Sets or gets the tile layer used to draw the Map background. Defaults to Roads. Valid values
 are:

   1. Roads
   2. Aerial
   3. Terrain
   4. Custom

   **Note:** Road layers are provided by OpenStreetMap and aerial and terrain layers are
 provided by the U.S. Geological Survey.

{:id="Map.Rotation" .number} *Rotation*
: Specifies the rotation of the map in decimal degrees, if any.

{:id="Map.ScaleUnits" .number} *ScaleUnits*
: Specifies the units used for the scale overlay. 1 (the default) will give metric units
 (km, m) whereas 2 will give imperial units (mi, ft).

{:id="Map.ShowCompass" .boolean} *ShowCompass*
: Specifies whether to a compass overlay on the Map. The compass will be rotated based on the
 device's orientation if a digital compass is present in hardware.

{:id="Map.ShowScale" .boolean} *ShowScale*
: Shows a scale reference on the map.

{:id="Map.ShowUser" .boolean} *ShowUser*
: Shows or hides an icon indicating the user's current location on the [`Map`](#Map). The
 availability and accuracy of this feature will depend on whether the user has location
 services enabled and which location providers are available.

{:id="Map.ShowZoom" .boolean} *ShowZoom*
: Specifies whether to show zoom controls or not.

{:id="Map.Top" .number} *Top*
: Specifies the position of the Top edge of the component relative to an
 AbsoluteArrangement.

{:id="Map.UserLatitude" .number .ro .bo} *UserLatitude*
: Returns the user's latitude if ShowUser is enabled.

{:id="Map.UserLongitude" .number .ro .bo} *UserLongitude*
: Returns the user's longitude if ShowUser is enabled.

{:id="Map.Visible" .boolean} *Visible*
: Specifies whether the `Map` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `Map` is showing and `false`{:.logic.block} if hidden.

{:id="Map.Width" .number .bo} *Width*
: Specifies the horizontal width of the `Map`, measured in pixels.

{:id="Map.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `Map` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

{:id="Map.ZoomLevel" .number} *ZoomLevel*
: Specifies the zoom level of the map.
 Valid values of ZoomLevel are dependent on the tile provider and the latitude and
 longitude of the map. For example, zoom levels are more constrained over oceans than dense
 city centers to conserve space for storing tiles, so valid values may be 1-7 over ocean and
 1-20 over cities. Tile providers may send warning or error tiles if the zoom level is too
 great for the server to support.

### Events  {#Map-Events}

{:.events}

{:id="Map.BoundsChange"} BoundsChange()
: The `BoundsChange` event runs when the user changes the map bounds, either by zooming, panning,
 or rotating the view.

{:id="Map.DoubleTapAtPoint"} DoubleTapAtPoint(*latitude*{:.number},*longitude*{:.number})
: The `DoubleTapAtPoint` runs when the user double taps at a point on the map. The tapped
 location will be reported in map coordinates via the `latitude`{:.variable.block} and
 `longitude`{:.variable.block} parameters.

{:id="Map.FeatureClick"} FeatureClick(*feature*{:.component})
: When a feature is clicked, the parent `Map` will also receive a `FeatureClick` event.
 The `feature` parameter indicates which child feature was clicked. This event is run *after*
 the `Click` event on the corresponding feature and after the `when any ... Click` event if one
 is provided.

{:id="Map.FeatureDrag"} FeatureDrag(*feature*{:.component})
: When the user drags a feature, the parent `Map` will also receive a `FeatureDrag` event.
 The `feature` parameter indicates which child feature was dragged. This event is run *after*
 the `Drag` event on the corresponding feature and after the `when any ... Drag` event if one is
 provided.

{:id="Map.FeatureLongClick"} FeatureLongClick(*feature*{:.component})
: When a feature is long-clicked, the parent `Map` will also receive a `FeatureLongClick`
 event. The `feature` parameter indicates which child feature was long-clicked. This event is
 run *after* the `LongClick` event on the corresponding feature and after the
 `when any ... LongClick` event if one is provided.

{:id="Map.FeatureStartDrag"} FeatureStartDrag(*feature*{:.component})
: When the user starts dragging a feature, the parent `Map` will also receive a
 `FeatureStartDrag` event. The `feature` parameter indicates which child feature was dragged.
 This event is run *after* the `StartDrag` event on the corresponding feature and after the
 `when any ... StartDrag` event if one is provided.

{:id="Map.FeatureStopDrag"} FeatureStopDrag(*feature*{:.component})
: When the user stops dragging a feature, the parent `Map` will also receive a
 `FeatureStopDrag` event. The `feature` parameter indicates which child feature was dragged.
 This event is run *after* the `StopDrag` event on the corresponding feature and after the
 `when any ... StopDrag` event if one is provided.

{:id="Map.GotFeatures"} GotFeatures(*url*{:.text},*features*{:.list})
: The `GotFeatures` event is run when a feature collection is successfully read from the
 given `url`{:.variable.block}. The `features`{:.variable.block} parameter will be a list of
 feature descriptions that can be converted into components using the
 [`FeatureFromDescription`](#Map.FeatureFromDescription) method.

{:id="Map.InvalidPoint"} InvalidPoint(*message*{:.text})
: The `InvalidPoint` event runs when the program encounters an invalid point while processing
 geographical data. Points are considered invalid when the latitude or longitude for the point
 is outside the acceptable range (`[-90, 90]` and `[-180, 180]`, respectively). The `message`
 parameter will contain an explanation for the error.

{:id="Map.LoadError"} LoadError(*url*{:.text},*responseCode*{:.number},*errorMessage*{:.text})
: The `LoadError` event is run when an error occurs while processing a feature collection
 document at the given `url`{:.variable.block}. The `responseCode`{:.variable.block} parameter
 will contain an HTTP status code and the `errorMessage`{:.variable.block} parameter will
 contain a detailed error message.

{:id="Map.LongPressAtPoint"} LongPressAtPoint(*latitude*{:.number},*longitude*{:.number})
: The `LongPressAtPoint` runs when the user long-presses at a point on the map without moving
 their finger (which would trigger a drag). The location of the long-press will be reported in
 map coordinates via the `latitude`{:.variable.block} and `longitude`{:.variable.block}
 parameters.

{:id="Map.Ready"} Ready()
: The `Ready` event runs once the `Map` has been initialized and is ready for user interaction.

{:id="Map.TapAtPoint"} TapAtPoint(*latitude*{:.number},*longitude*{:.number})
: The `TapAtPoint` runs when the user taps at a point on the map. The tapped location will be
 reported in map coordinates via the `latitude`{:.variable.block} and
 `longitude`{:.variable.block} parameters.

{:id="Map.ZoomChange"} ZoomChange()
: The `ZoomChange` event runs when the user has changed the zoom level of the map, such as by
 pinching or double-tapping to zoom.

### Methods  {#Map-Methods}

{:.methods}

{:id="Map.CreateMarker" class="method returns component"} <i/> CreateMarker(*latitude*{:.number},*longitude*{:.number})
: Creates a new [`Marker`](#Marker) on the `Map` at the specified `latitude` and `longitude`.

{:id="Map.FeatureFromDescription" class="method returns any"} <i/> FeatureFromDescription(*description*{:.list})
: Converts a feature description into an App Inventor map feature. Points are converted into
 [`Marker`](#Marker) components, LineStrings are converted into [`LineString`](#LineString) components, and
 Polygons (and MultiPolygons) are converted into [`Polygon`](#Polygon) components. If the feature has
 properties, they will be mapped into App Inventor properties using the following mapping:

   * description becomes `Description`
   * draggable becomes `Draggable`
   * infobox becomes `EnableInfobox`
   * fill becomes `FillColor`
   * fill-opacity becomes `FillOpacity`
   * image becomes `ImageAsset`
   * stroke becomes `StrokeColor`
   * stroke-opacity becomes `StrokeOpacity`
   * stroke-width becomes `StrokeWidth`
   * title becomes `Title`
   * visible becomes `Visible`

{:id="Map.LoadFromURL" class="method"} <i/> LoadFromURL(*url*{:.text})
: Loads a feature collection in GeoJSON format from the given `url`. On success,
 the event [`GotFeatures`](#Map.GotFeatures) will be raised with the given `url`
 and a list of `feature`s parsed from the GeoJSON as a list of (key, value) pairs. On failure,
 the [`LoadError`](#Map.LoadError) event will be raised with any applicable HTTP
 response code and error message.

{:id="Map.PanTo" class="method"} <i/> PanTo(*latitude*{:.number},*longitude*{:.number},*zoom*{:.number})
: Pans the map center to the given latitude and longitude and adjust the zoom level to the specified zoom.

{:id="Map.Save" class="method"} <i/> Save(*path*{:.text})
: Saves the features on the `Map` as a GeoJSON file at the specified path.

## Marker  {#Marker}

The `Marker` component indicates points on a [`Map`](#Map), such as buildings or other points of
 interest. `Marker`s can be customized in many ways, such as using custom images from the app's
 assets or by changing the `Marker` [`FillColor`](#Marker.FillColor). `Marker`s can also be created
 dynamically by calling the [`Map`'s `CreateMarker`](#Map.CreateMarker) method and configured using
 the ["Any Component"](../other/any-component-blocks.html) blocks.



### Properties  {#Marker-Properties}

{:.properties}

{:id="Marker.AnchorHorizontal" .number} *AnchorHorizontal*
: Sets or gets the horizontal offset of the `Marker` center relative to its image. Valid values
 are: `1` (Left), `2` (Right), or `3` (Center).

{:id="Marker.AnchorVertical" .number} *AnchorVertical*
: Sets or gets the vertical offset of the `Marker` center relative to its image. Valid values
 are: `1` (Top), `2` (Center), or `3` (Bottom).

{:id="Marker.Description" .text} *Description*
: Sets or gets the description displayed in the info window. The info window appears when the
 user taps on the `Marker`.

{:id="Marker.Draggable" .boolean} *Draggable*
: Sets or gets whether or not the user can drag a map feature. This feature is accessed by
 long-pressing and then dragging the `Marker` to a new location.

{:id="Marker.EnableInfobox" .boolean} *EnableInfobox*
: Enables or disables the infobox window display when the user taps the `Marker`.

{:id="Marker.FillColor" .color} *FillColor*
: Sets or gets the color used to fill in the `Marker`.

{:id="Marker.FillOpacity" .number} *FillOpacity*
: Sets or gets the opacity of the color used to fill the `Marker`. A value of 0.0 will be
 completely invisible and a value of 1.0 will be completely opaque.

{:id="Marker.Height" .number .bo} *Height*
: Specifies the `Marker`'s vertical height, measured in pixels.

{:id="Marker.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the `Marker`'s vertical height as a percentage
 of the [`Screen`'s `Height`](userinterface.html#Screen.Height).

{:id="Marker.ImageAsset" .text} *ImageAsset*
: Specifies the image shown for the `Marker`. If set to the empty string "", then the default
 marker icon will be used.

{:id="Marker.Latitude" .number} *Latitude*
: Sets or gets the latitude of the `Marker`, in degrees, with positive values representing
 north of the equator and negative values representing south of the equator. To update the
 `Latitude` and [`Longitude`](#Marker.Longitude) simultaneously, use the
 [`SetLocation`](#Marker.SetLocation) method.

{:id="Marker.Longitude" .number} *Longitude*
: Sets or gets the longitude of the `Marker`, in degrees, with positive values representing east
 of the prime meridian and negative values representing west of the prime meridian. To update
 the [`Latitude`](#Marker.Latitude) and `Longitude` simultaneously, use the
 [`SetLocation`](#Marker.SetLocation) method.

{:id="Marker.StrokeColor" .color} *StrokeColor*
: Sets or gets the color used to outline the `Marker`.

{:id="Marker.StrokeOpacity" .number} *StrokeOpacity*
: Sets or gets the opacity of the outline of the `Marker`. A value of 0.0 will be invisible and
 a value of 1.0 will be opaque.

{:id="Marker.StrokeWidth" .number} *StrokeWidth*
: Sets or gets the width of the stroke used to outline the `Marker`.

{:id="Marker.Title" .text} *Title*
: Sets or gets the title displayed in the info window that appears when the user clicks on the
 map feature.

{:id="Marker.Type" .text .ro .bo} *Type*
: Return the type of the map feature. For Marker, this returns the text "Marker".

{:id="Marker.Visible" .boolean} *Visible*
: Specifies whether the `Marker` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `Marker` is showing and `false`{:.logic.block} if hidden.

{:id="Marker.Width" .number .bo} *Width*
: Specifies the horizontal width of the `Marker`, measured in pixels.

{:id="Marker.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the `Marker` as a percentage
 of the [`Screen`'s `Width`](userinterface.html#Screen.Width).

### Events  {#Marker-Events}

{:.events}

{:id="Marker.Click"} Click()
: The `Click` event runs when the user taps on the `Marker`.

{:id="Marker.Drag"} Drag()
: The `Drag` event runs in response to position updates of the `Marker` as the user drags it.

{:id="Marker.LongClick"} LongClick()
: The `LongClick` event runs when the user presses and holds the `Marker` and then releases it.
 This event will only trigger if [`Draggable`](#Marker.Draggable) is `false`{:.logic.block} because it
 uses the same gesture as [`StartDrag`](#Marker.StartDrag).

{:id="Marker.StartDrag"} StartDrag()
: The `StartDrag` event runs when the user presses and holds the `Marker` and then proceeds to
 move their finger on the screen. It will be followed by the [`Drag`](#Marker.Drag) and
 [`StopDrag`](#Marker.StopDrag) events.

{:id="Marker.StopDrag"} StopDrag()
: The `StopDrag` event runs when the user releases the `Marker` at the end of a drag.

### Methods  {#Marker-Methods}

{:.methods}

{:id="Marker.BearingToFeature" class="method returns number"} <i/> BearingToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Returns the bearing from the `Marker` to the given map feature, in degrees from due north.
 If the `centroids` parameter is `true`{:.logic.block}, the bearing will be to the center of
 the map feature. Otherwise, the bearing will be computed to the point in the feature nearest
 the `Marker`.

{:id="Marker.BearingToPoint" class="method returns number"} <i/> BearingToPoint(*latitude*{:.number},*longitude*{:.number})
: Returns the bearing from the `Marker` to the given `latitude` and `longitude`, in degrees
 from due north.

{:id="Marker.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Computes the distance between the `Marker` and the given `mapFeature`. If `centroids` is
 `true`{:.logic.block}, the computation is done between the centroids of the two features.
 Otherwise, the distance will be computed between the two features based on the closest points.
 Further, when `centroids` is `false`{:.logic.block}, this method will return 0 if the `Marker`
 intersects or contains the `mapFeature`. If an error occurs, this method will return -1.

{:id="Marker.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number})
: Compute the distance, in meters, between a `Marker` and a `latitude`, `longitude` point.

{:id="Marker.HideInfobox" class="method"} <i/> HideInfobox()
: Hides the `Marker`'s info box if it is visible. Otherwise, no action is taken.

{:id="Marker.SetLocation" class="method"} <i/> SetLocation(*latitude*{:.number},*longitude*{:.number})
: Sets the location of the `Marker`.

{:id="Marker.ShowInfobox" class="method"} <i/> ShowInfobox()
: Shows the info box for the `Marker` if it is not visible. Otherwise, this method has no effect.
 This method can be used to show the info box even if [`EnableInfobox`](#Marker.EnableInfobox) is false.

## Navigation  {#Navigation}

The Navigation component generates directions between two locations using a service called
 [OpenRouteService](https://openrouteservice.org). You must provide a valid API key from that
 service in order for this component to work.



### Properties  {#Navigation-Properties}

{:.properties}

{:id="Navigation.ApiKey" .text .wo} *ApiKey*
: API Key for Open Route Service. Obtain an API key at
 [https://openrouteservice.org](https://openrouteservice.org).

{:id="Navigation.EndLatitude" .number} *EndLatitude*
: The latitude of the end location.

{:id="Navigation.EndLocation" .component .wo .bo} *EndLocation*
: Set the end location.

{:id="Navigation.EndLongitude" .number} *EndLongitude*
: The longitude of the end location.

{:id="Navigation.Language" .text} *Language*
: The language to use for textual directions. Default is "en" for English.

{:id="Navigation.ResponseContent" .dictionary .ro .bo} *ResponseContent*
: The raw response from the server. This can be used to access more details beyond what the
 [`GotDirections`](#Navigation.GotDirections) event provides.

{:id="Navigation.StartLatitude" .number} *StartLatitude*
: The latitude of the start location.

{:id="Navigation.StartLocation" .component .wo .bo} *StartLocation*
: Set the start location.

{:id="Navigation.StartLongitude" .number} *StartLongitude*
: The longitude of the start location.

{:id="Navigation.TransportationMethod" .text} *TransportationMethod*
: The transportation method used for determining the route. Valid options are:

  - `foot-walking`: Route based on walking paths
  - `driving-car`: Route based on vehicle paths
  - `cycling-regular`: Route based on bicycle paths
  - `wheelchair`: Route based on wheelchair accessible paths

### Events  {#Navigation-Events}

{:.events}

{:id="Navigation.GotDirections"} GotDirections(*directions*{:.list},*points*{:.list},*distance*{:.number},*duration*{:.number})
: Event indicating that a request has finished and has returned data. The following parameters
 are provided:

  - `directions`: A list of text directions, such as "Turn left at Massachusetts Avenue".
  - `points`: A list of (latitude, longitude) points that represent the path to take. This can
    be passed to [`LineString`'s `Points`](#LineString.Points) to draw the line on a [`Map`](#Map).
  - `distance`: Estimated distance for the route, in meters.
  - `duration`: Estimated duration for the route, in seconds.

### Methods  {#Navigation-Methods}

{:.methods}

{:id="Navigation.RequestDirections" class="method"} <i/> RequestDirections()
: Request directions from the routing service using the values of [`StartLatitude`](#Navigation.StartLatitude),
 [`StartLongitude`](#Navigation.StartLongitude), [`EndLatitude`](#Navigation.EndLatitude), and [`EndLongitude`](#Navigation.EndLongitude). On success,
 the [`GotDirections`](#Navigation.GotDirections) event block will run. If an
 error occurs, the error will be reported via the
 [`Screen's ErrorOccurred`](userinterface.html#Screen.ErrorOccurred) event.

## Polygon  {#Polygon}

`Polygon` encloses an arbitrary 2-dimensional area on a [`Map`](#Map). `Polygon`s can be used for
 drawing a perimeter, such as a campus, city, or country. `Polygon`s begin as basic triangles.
 New vertices can be created by dragging the midpoint of a polygon away from the edge. Clicking
 on a vertex will remove the vertex, but a minimum of 3 vertices must exist at all times.



### Properties  {#Polygon-Properties}

{:.properties}

{:id="Polygon.Description" .text} *Description*
: Sets or gets the description displayed in the info window. The info window appears when the
 user taps on the `Polygon`.

{:id="Polygon.Draggable" .boolean} *Draggable*
: Sets or gets whether or not the user can drag a map feature. This feature is accessed by
 long-pressing and then dragging the `Polygon` to a new location.

{:id="Polygon.EnableInfobox" .boolean} *EnableInfobox*
: Enables or disables the infobox window display when the user taps the `Polygon`.

{:id="Polygon.FillColor" .color} *FillColor*
: Sets or gets the color used to fill in the `Polygon`.

{:id="Polygon.FillOpacity" .number} *FillOpacity*
: Sets or gets the opacity of the color used to fill the `Polygon`. A value of 0.0 will be
 completely invisible and a value of 1.0 will be completely opaque.

{:id="Polygon.HolePoints" .list .bo} *HolePoints*
: Specifies the points of any holes in the `Polygon`. The `HolePoints` property is a list of
 lists, with each sublist containing `(latitude, longitude)` points representing a hole.

{:id="Polygon.HolePointsFromString" .text .wo} *HolePointsFromString*
: Specifies holes in a `Polygon`from a GeoJSON string. In contrast to
 [`HolePoints`](#Polygon.HolePoints), the longitude of each point comes before the latitude as
 stated in the GeoJSON specification.

{:id="Polygon.Points" .list .bo} *Points*
: Specifies the Points used for drawing the `Polygon`. The Points are specified as a list of
 lists containing latitude and longitude values, such as `[[lat1, long1], [lat2, long2], ...]`.

{:id="Polygon.PointsFromString" .text .wo} *PointsFromString*
: Specifies the points for the `Polygon` from a GeoJSON string. Unlike [`Points`](#Polygon.Points),
 this property expects that the longitude comes first in the point rather than the latitude.

{:id="Polygon.StrokeColor" .color} *StrokeColor*
: Sets or gets the color used to outline the `Polygon`.

{:id="Polygon.StrokeOpacity" .number} *StrokeOpacity*
: Sets or gets the opacity of the outline of the `Polygon`. A value of 0.0 will be invisible and
 a value of 1.0 will be opaque.

{:id="Polygon.StrokeWidth" .number} *StrokeWidth*
: Sets or gets the width of the stroke used to outline the `Polygon`.

{:id="Polygon.Title" .text} *Title*
: Sets or gets the title displayed in the info window that appears when the user clicks on the
 map feature.

{:id="Polygon.Type" .text .ro .bo} *Type*
: Returns the type of the feature. For polygons, this returns MapFeature.Polygon ("Polygon").

{:id="Polygon.Visible" .boolean} *Visible*
: Specifies whether the `Polygon` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `Polygon` is showing and `false`{:.logic.block} if hidden.

### Events  {#Polygon-Events}

{:.events}

{:id="Polygon.Click"} Click()
: The `Click` event runs when the user taps on the `Polygon`.

{:id="Polygon.Drag"} Drag()
: The `Drag` event runs in response to position updates of the `Polygon` as the user drags it.

{:id="Polygon.LongClick"} LongClick()
: The `LongClick` event runs when the user presses and holds the `Polygon` and then releases it.
 This event will only trigger if [`Draggable`](#Polygon.Draggable) is `false`{:.logic.block} because it
 uses the same gesture as [`StartDrag`](#Polygon.StartDrag).

{:id="Polygon.StartDrag"} StartDrag()
: The `StartDrag` event runs when the user presses and holds the `Polygon` and then proceeds to
 move their finger on the screen. It will be followed by the [`Drag`](#Polygon.Drag) and
 [`StopDrag`](#Polygon.StopDrag) events.

{:id="Polygon.StopDrag"} StopDrag()
: The `StopDrag` event runs when the user releases the `Polygon` at the end of a drag.

### Methods  {#Polygon-Methods}

{:.methods}

{:id="Polygon.Centroid" class="method returns list"} <i/> Centroid()
: Gets the centroid of the `Polygon` as a `(latitude, longitude)` pair.

{:id="Polygon.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Computes the distance between the `Polygon` and the given `mapFeature`. If `centroids` is
 `true`{:.logic.block}, the computation is done between the centroids of the two features.
 Otherwise, the distance will be computed between the two features based on the closest points.
 Further, when `centroids` is `false`{:.logic.block}, this method will return 0 if the `Polygon`
 intersects or contains the `mapFeature`. If an error occurs, this method will return -1.

{:id="Polygon.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number},*centroid*{:.boolean})
: Computes the distance between the `Polygon` and the given `latitude` and `longitude`. If
 `centroids` is `true`{:.logic.block}, the distance is computed from the center of the `Polygon`
 to the given point. Otherwise, the distance is computed from the closest point on the `Polygon`
 to the given point. Further, this method will return 0 if `centroids` is `false`{:.logic.block}
 and the point is in the `Polygon`. If an error occurs, -1 will be returned.

{:id="Polygon.HideInfobox" class="method"} <i/> HideInfobox()
: Hides the `Polygon`'s info box if it is visible. Otherwise, no action is taken.

{:id="Polygon.ShowInfobox" class="method"} <i/> ShowInfobox()
: Shows the info box for the `Polygon` if it is not visible. Otherwise, this method has no effect.
 This method can be used to show the info box even if [`EnableInfobox`](#Polygon.EnableInfobox) is false.

## Rectangle  {#Rectangle}

`Rectangle`s are polygons with fixed latitudes and longitudes for the north, south, east, and
 west boundaries. Moving a vertex of the `Rectangle` updates the appropriate edges accordingly.



### Properties  {#Rectangle-Properties}

{:.properties}

{:id="Rectangle.Description" .text} *Description*
: Sets or gets the description displayed in the info window. The info window appears when the
 user taps on the `Rectangle`.

{:id="Rectangle.Draggable" .boolean} *Draggable*
: Sets or gets whether or not the user can drag a map feature. This feature is accessed by
 long-pressing and then dragging the `Rectangle` to a new location.

{:id="Rectangle.EastLongitude" .number} *EastLongitude*
: Specifies the east-most edge of the `Rectangle`, in decimal degrees east of the prime meridian.

{:id="Rectangle.EnableInfobox" .boolean} *EnableInfobox*
: Enables or disables the infobox window display when the user taps the `Rectangle`.

{:id="Rectangle.FillColor" .color} *FillColor*
: Sets or gets the color used to fill in the `Rectangle`.

{:id="Rectangle.FillOpacity" .number} *FillOpacity*
: Sets or gets the opacity of the color used to fill the `Rectangle`. A value of 0.0 will be
 completely invisible and a value of 1.0 will be completely opaque.

{:id="Rectangle.NorthLatitude" .number} *NorthLatitude*
: Specifies the north-most edge of the `Rectangle`, in decimal degrees north of the equator.

{:id="Rectangle.SouthLatitude" .number} *SouthLatitude*
: Specifies the west-most edge of the `Rectangle`, in decimal degrees east of the prime meridian.

{:id="Rectangle.StrokeColor" .color} *StrokeColor*
: Sets or gets the color used to outline the `Rectangle`.

{:id="Rectangle.StrokeOpacity" .number} *StrokeOpacity*
: Sets or gets the opacity of the outline of the `Rectangle`. A value of 0.0 will be invisible and
 a value of 1.0 will be opaque.

{:id="Rectangle.StrokeWidth" .number} *StrokeWidth*
: Sets or gets the width of the stroke used to outline the `Rectangle`.

{:id="Rectangle.Title" .text} *Title*
: Sets or gets the title displayed in the info window that appears when the user clicks on the
 map feature.

{:id="Rectangle.Type" .text .ro .bo} *Type*
: Returns the type of the feature. For rectangles, this returns MapFeature.Rectangle ("Rectangle").

{:id="Rectangle.Visible" .boolean} *Visible*
: Specifies whether the `Rectangle` should be visible on the screen.  Value is `true`{:.logic.block}
 if the `Rectangle` is showing and `false`{:.logic.block} if hidden.

{:id="Rectangle.WestLongitude" .number} *WestLongitude*
: Specifies the south-most edge of the `Rectangle`, in decimal degrees south of the equator.

### Events  {#Rectangle-Events}

{:.events}

{:id="Rectangle.Click"} Click()
: The `Click` event runs when the user taps on the `Rectangle`.

{:id="Rectangle.Drag"} Drag()
: The `Drag` event runs in response to position updates of the `Rectangle` as the user drags it.

{:id="Rectangle.LongClick"} LongClick()
: The `LongClick` event runs when the user presses and holds the `Rectangle` and then releases it.
 This event will only trigger if [`Draggable`](#Rectangle.Draggable) is `false`{:.logic.block} because it
 uses the same gesture as [`StartDrag`](#Rectangle.StartDrag).

{:id="Rectangle.StartDrag"} StartDrag()
: The `StartDrag` event runs when the user presses and holds the `Rectangle` and then proceeds to
 move their finger on the screen. It will be followed by the [`Drag`](#Rectangle.Drag) and
 [`StopDrag`](#Rectangle.StopDrag) events.

{:id="Rectangle.StopDrag"} StopDrag()
: The `StopDrag` event runs when the user releases the `Rectangle` at the end of a drag.

### Methods  {#Rectangle-Methods}

{:.methods}

{:id="Rectangle.Bounds" class="method returns list"} <i/> Bounds()
: Returns the bounding box of the `Rectangle` in the format `((North West) (South East))`.

{:id="Rectangle.Center" class="method returns list"} <i/> Center()
: Returns the center of the `Rectangle` as a list of the form `(Latitude Longitude)`.

{:id="Rectangle.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Computes the distance between the `Rectangle` and the given `mapFeature`. If `centroids` is
 `true`{:.logic.block}, the computation is done between the centroids of the two features.
 Otherwise, the distance will be computed between the two features based on the closest points.
 Further, when `centroids` is `false`{:.logic.block}, this method will return 0 if the `Rectangle`
 intersects or contains the `mapFeature`. If an error occurs, this method will return -1.

{:id="Rectangle.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number},*centroid*{:.boolean})
: Computes the distance between the `Rectangle` and the given `latitude` and `longitude`. If
 `centroids` is `true`{:.logic.block}, the distance is computed from the center of the `Rectangle`
 to the given point. Otherwise, the distance is computed from the closest point on the `Rectangle`
 to the given point. Further, this method will return 0 if `centroids` is `false`{:.logic.block}
 and the point is in the `Rectangle`. If an error occurs, -1 will be returned.

{:id="Rectangle.HideInfobox" class="method"} <i/> HideInfobox()
: Hides the `Rectangle`'s info box if it is visible. Otherwise, no action is taken.

{:id="Rectangle.SetCenter" class="method"} <i/> SetCenter(*latitude*{:.number},*longitude*{:.number})
: Move the `Rectangle` to be centered on the given `latitude` and `longitude`, attempting to keep
 the width and height (in meters) as equal as possible adjusting for changes in latitude.

{:id="Rectangle.ShowInfobox" class="method"} <i/> ShowInfobox()
: Shows the info box for the `Rectangle` if it is not visible. Otherwise, this method has no effect.
 This method can be used to show the info box even if [`EnableInfobox`](#Rectangle.EnableInfobox) is false.
