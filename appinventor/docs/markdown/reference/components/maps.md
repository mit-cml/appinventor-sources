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
* [Polygon](#Polygon)
* [Rectangle](#Rectangle)

## Circle  {#Circle}

### Properties  {#Circle-Properties}

{:.properties}

{:id="Circle.Description" .text} *Description*
: The description displayed in the info window that appears when the user clicks on the Circle.

{:id="Circle.Draggable" .boolean} *Draggable*
: The Draggable property is used to set whether or not the user can drag the Circle by long-pressing and then dragging the Circle to a new location.

{:id="Circle.EnableInfobox" .boolean} *EnableInfobox*
: Enable or disable the infobox window display when the user taps the Circle.

{:id="Circle.FillColor" .color} *FillColor*
: The paint color used to fill in the Circle.

{:id="Circle.FillOpacity" .number} *FillOpacity*
: The opacity of the interior of the map feature.

{:id="Circle.Latitude" .number} *Latitude*
: The latitude of the center of the circle.

{:id="Circle.Longitude" .number} *Longitude*
: The longitude of the center of the circle.

{:id="Circle.Radius" .number} *Radius*
: The radius of the circle in meters.

{:id="Circle.StrokeColor" .color} *StrokeColor*
: The paint color used to outline the Circle.

{:id="Circle.StrokeOpacity" .number} *StrokeOpacity*
: The opacity of the stroke used to outline the map feature.

{:id="Circle.StrokeWidth" .number} *StrokeWidth*
: The width of the stroke used to outline the Circle.

{:id="Circle.Title" .text} *Title*
: The title displayed in the info window that appears when the user clicks on the Circle.

{:id="Circle.Type" .text .ro .bo} *Type*
: The type of the feature. For Circles, this returns the text "Circle".

{:id="Circle.Visible" .boolean} *Visible*
: Specifies whether the Circle should be visible on the screen. Value is true if the component is showing and false if hidden.

### Events  {#Circle-Events}

{:.events}

{:id="Circle.Click"} Click()
: The user clicked on the Circle.

{:id="Circle.Drag"} Drag()
: The user dragged the Circle.

{:id="Circle.LongClick"} LongClick()
: The user long-pressed on the Circle. This event will only trigger if Draggable is false.

{:id="Circle.StartDrag"} StartDrag()
: The user started a drag operation.

{:id="Circle.StopDrag"} StopDrag()
: The user stopped a drag operation.

### Methods  {#Circle-Methods}

{:.methods}

{:id="Circle.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Compute the distance, in meters, between two map features.

{:id="Circle.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number},*centroid*{:.boolean})
: Compute the distance, in meters, between a Circle and a latitude, longitude point.

{:id="Circle.HideInfobox" class="method"} <i/> HideInfobox()
: Hide the infobox if it is shown. If the infobox is not visible this function has no effect.

{:id="Circle.SetLocation" class="method"} <i/> SetLocation(*latitude*{:.number},*longitude*{:.number})
: Set the center of the Circle.

{:id="Circle.ShowInfobox" class="method"} <i/> ShowInfobox()
: Show the infobox for the Circle. This will show the infobox even if EnableInfobox is set to false.

## FeatureCollection  {#FeatureCollection}

### Properties  {#FeatureCollection-Properties}

{:.properties}

{:id="FeatureCollection.Features" .list .bo} *Features*
: The list of features placed on this FeatureCollection. This list also includes any features created by calls to FeatureFromDescription

{:id="FeatureCollection.FeaturesFromGeoJSON" .text .wo} *FeaturesFromGeoJSON*
: Loads a collection of features from the given string. If the string is not valid GeoJSON, the ErrorLoadingFeatureCollection error will be run with url = <string>.

{:id="FeatureCollection.Height" .number .bo} *Height*
: Specifies the vertical height of the FeatureCollection, measured in pixels.

{:id="FeatureCollection.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the FeatureCollection as a percentage of the height of the Screen.

{:id="FeatureCollection.Source" .text .ro} *Source*
: Gets or sets the source URL used to populate the feature collection. If the feature collection was not loaded from a URL, this will be the empty string.

{:id="FeatureCollection.Visible" .boolean} *Visible*
: Returns true iff the FeatureCollection is visible.

{:id="FeatureCollection.Width" .number .bo} *Width*
: Specifies the horizontal width of the FeatureCollection, measured in pixels.

{:id="FeatureCollection.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the FeatureCollection as a percentage of the width of the Screen.

### Events  {#FeatureCollection-Events}

{:.events}

{:id="FeatureCollection.FeatureClick"} FeatureClick(*feature*{:.component})
: The user clicked on a map feature.

{:id="FeatureCollection.FeatureDrag"} FeatureDrag(*feature*{:.component})
: The user dragged a map feature.

{:id="FeatureCollection.FeatureLongClick"} FeatureLongClick(*feature*{:.component})
: The user long-pressed on a map feature.

{:id="FeatureCollection.FeatureStartDrag"} FeatureStartDrag(*feature*{:.component})
: The user started dragging a map feature.

{:id="FeatureCollection.FeatureStopDrag"} FeatureStopDrag(*feature*{:.component})
: The user stopped dragging a map feature.

{:id="FeatureCollection.GotFeatures"} GotFeatures(*url*{:.text},*features*{:.list})
: A GeoJSON document was successfully read from url. The features specified in the document are provided as a list in features.

{:id="FeatureCollection.LoadError"} LoadError(*url*{:.text},*responseCode*{:.number},*errorMessage*{:.text})
: An error was encountered while processing a GeoJSON document at the given url. The responseCode parameter will contain an HTTP status code and the errorMessage parameter will contain a detailed error message.

### Methods  {#FeatureCollection-Methods}

{:.methods}

{:id="FeatureCollection.FeatureFromDescription" class="method returns any"} <i/> FeatureFromDescription(*description*{:.list})
: Convert a feature description into an App Inventor map feature. Currently the only
 supported conversion is from a GeoJSON point to Marker component. If the feature has
 properties, they will be mapped into App Inventor properties using the following mapping:

 * description becomes Description
 * draggable becomes Draggable
 * infobox becomes EnableInfobox
 * fill becomes FillColor
 * fill-opacity becomes FillOpacity
 * image becomes ImageAsset
 * stroke becomes StrokeColor
 * stroke-opacity becomes StrokeOpacity
 * stroke-width becomes StrokeWidth
 * title becomes Title
 * visible becomes Visible

{:id="FeatureCollection.LoadFromURL" class="method"} <i/> LoadFromURL(*url*{:.text})
: <p>Load a feature collection in <a href="https://en.wikipedia.org/wiki/GeoJSON">GeoJSON</a> format from the given url. On success, the event GotFeatures will be raised with the given url and a list of the features parsed from the GeoJSON as a list of (key, value) pairs. On failure, the LoadError event will be raised with any applicable HTTP response code and error message.</p>

## LineString  {#LineString}

### Properties  {#LineString-Properties}

{:.properties}

{:id="LineString.Description" .text} *Description*
: The description displayed in the info window that appears when the user clicks on the LineString.

{:id="LineString.Draggable" .boolean} *Draggable*
: The Draggable property is used to set whether or not the user can drag the LineString by long-pressing and then dragging the LineString to a new location.

{:id="LineString.EnableInfobox" .boolean} *EnableInfobox*
: Enable or disable the infobox window display when the user taps the LineString.

{:id="LineString.Points" .list .bo} *Points*
: A list of latitude and longitude pairs that represent the line segments of the polyline.

{:id="LineString.PointsFromString" .text .wo} *PointsFromString*
: Set the points of the LineString from a specially-coded character string of the form:
 [[longitude1, latitude1], [longitude2, latitude2], ...]
 Note the reversal of latitude and longitude versus how they are typically represented.

{:id="LineString.StrokeColor" .color} *StrokeColor*
: The paint color used to outline the LineString.

{:id="LineString.StrokeOpacity" .number} *StrokeOpacity*
: The opacity of the stroke used to outline the map feature.

{:id="LineString.StrokeWidth" .number} *StrokeWidth*
: The width of the stroke used to outline the LineString.

{:id="LineString.Title" .text} *Title*
: The title displayed in the info window that appears when the user clicks on the LineString.

{:id="LineString.Type" .text .ro .bo} *Type*
: The type of the map feature. For LineString, this returns the text "LineString".

{:id="LineString.Visible" .boolean} *Visible*
: Specifies whether the LineString should be visible on the screen. Value is true if the component is showing and false if hidden.

### Events  {#LineString-Events}

{:.events}

{:id="LineString.Click"} Click()
: The user clicked on the LineString.

{:id="LineString.Drag"} Drag()
: The user dragged the LineString.

{:id="LineString.LongClick"} LongClick()
: The user long-pressed on the LineString. This event will only trigger if Draggable is false.

{:id="LineString.StartDrag"} StartDrag()
: The user started a drag operation.

{:id="LineString.StopDrag"} StopDrag()
: The user stopped a drag operation.

### Methods  {#LineString-Methods}

{:.methods}

{:id="LineString.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Compute the distance, in meters, between two map features.

{:id="LineString.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number},*centroid*{:.boolean})
: Compute the distance, in meters, between a LineString and a latitude, longitude point.

{:id="LineString.HideInfobox" class="method"} <i/> HideInfobox()
: Hide the infobox if it is shown. If the infobox is not visible this function has no effect.

{:id="LineString.ShowInfobox" class="method"} <i/> ShowInfobox()
: Show the infobox for the LineString. This will show the infobox even if EnableInfobox is set to false.

## Map  {#Map}

### Properties  {#Map-Properties}

{:.properties}

{:id="Map.BoundingBox" .list .bo} *BoundingBox*
: Bounding box for the map stored as [[North, West], [South, East]].

{:id="Map.CenterFromString" .text .wo} *CenterFromString*
: <p>Set the initial center coordinate of the map. The value is specified as a comma-separated pair of decimal latitude and longitude coordinates, for example, <code>42.359144, -71.093612</code>.</p><p>In blocks code, it is recommended for performance reasons to use SetCenter with numerical latitude and longitude rather than convert to the string representation for use with this property.</p>

{:id="Map.EnablePan" .boolean} *EnablePan*
: Enable two-finger panning of the Map

{:id="Map.EnableRotation" .boolean} *EnableRotation*
: Enable rotating the map based on the user's orientation.

{:id="Map.EnableZoom" .boolean} *EnableZoom*
: Set whether the user can zoom the map using touch gestures. This value does not affect
 whether the user can zoom using the zoom controls provided by
 <a href="#Map.ShowZoom">ShowZoom</a>.

{:id="Map.Features" .list .bo} *Features*
: The list of features placed on this Map. This list also includes any features created by calls to FeatureFromDescription

{:id="Map.Height" .number .bo} *Height*
: Specifies the vertical height of the Map, measured in pixels.

{:id="Map.HeightPercent" .number .wo .bo} *HeightPercent*
: Specifies the vertical height of the Map as a percentage of the height of the Screen.

{:id="Map.Latitude" .number .ro .bo} *Latitude*
: The latitude of the center of the map.

{:id="Map.LocationSensor" .component .wo} *LocationSensor*
: Uses the provided LocationSensor for user location data rather than the built-in location provider.

{:id="Map.Longitude" .number .ro .bo} *Longitude*
: The longitude of the center of the map.

{:id="Map.MapType" .number} *MapType*
: Set the type of map tile used for the base tile layer.
 Valid values are:

  1. Roads
  2. Aerial
  3. Terrain

{:id="Map.Rotation" .number} *Rotation*
: Sets or gets the rotation of the map in decimal degrees if any

{:id="Map.ScaleUnits" .number} *ScaleUnits*
: 

{:id="Map.ShowCompass" .boolean} *ShowCompass*
: Show a compass on the map. If the device provides a digital compass, orientation changes will
 be used to rotate the compass icon.

{:id="Map.ShowScale" .boolean} *ShowScale*
: Shows a scale reference on the map.

{:id="Map.ShowUser" .boolean} *ShowUser*
: Show the user's location on the map.

{:id="Map.ShowZoom" .boolean} *ShowZoom*
: Show the zoom controls on the map.

{:id="Map.UserLatitude" .number .ro .bo} *UserLatitude*
: Returns the user's latitude if ShowUser is enabled.

{:id="Map.UserLongitude" .number .ro .bo} *UserLongitude*
: Returns the user's longitude if ShowUser is enabled.

{:id="Map.Visible" .boolean} *Visible*
: Returns true iff the Map is visible.

{:id="Map.Width" .number .bo} *Width*
: Specifies the horizontal width of the Map, measured in pixels.

{:id="Map.WidthPercent" .number .wo .bo} *WidthPercent*
: Specifies the horizontal width of the Map as a percentage of the width of the Screen.

{:id="Map.ZoomLevel" .number} *ZoomLevel*
: Set the zoom level of the map.
 Valid values of ZoomLevel are dependent on the tile provider and the latitude and
 longitude of the map. For example, zoom levels are more constrained over oceans than dense
 city centers to conserve space for storing tiles, so valid values may be 1-7 over ocean and
 1-18 over cities. Tile providers may send warning or error tiles if the zoom level is too
 great for the server to support.

### Events  {#Map-Events}

{:.events}

{:id="Map.BoundsChange"} BoundsChange()
: User has changed the map bounds by panning or zooming the map.

{:id="Map.DoubleTapAtPoint"} DoubleTapAtPoint(*latitude*{:.number},*longitude*{:.number})
: The user double-tapped at a point on the map. This event will be followed by a ZoomChanged event if zooming gestures are enabled and the map is not at the highest possible zoom level.

{:id="Map.FeatureClick"} FeatureClick(*feature*{:.component})
: The user clicked on a map feature.

{:id="Map.FeatureDrag"} FeatureDrag(*feature*{:.component})
: The user dragged a map feature.

{:id="Map.FeatureLongClick"} FeatureLongClick(*feature*{:.component})
: The user long-pressed on a map feature.

{:id="Map.FeatureStartDrag"} FeatureStartDrag(*feature*{:.component})
: The user started dragging a map feature.

{:id="Map.FeatureStopDrag"} FeatureStopDrag(*feature*{:.component})
: The user stopped dragging a map feature.

{:id="Map.GotFeatures"} GotFeatures(*url*{:.text},*features*{:.list})
: A GeoJSON document was successfully read from url. The features specified in the document are provided as a list in features.

{:id="Map.InvalidPoint"} InvalidPoint(*message*{:.text})
: An invalid coordinate was supplied during a maps operation. The message parameter will have more details about the issue.

{:id="Map.LoadError"} LoadError(*url*{:.text},*responseCode*{:.number},*errorMessage*{:.text})
: An error was encountered while processing a GeoJSON document at the given url. The responseCode parameter will contain an HTTP status code and the errorMessage parameter will contain a detailed error message.

{:id="Map.LongPressAtPoint"} LongPressAtPoint(*latitude*{:.number},*longitude*{:.number})
: The user long-pressed at a point on the map.

{:id="Map.Ready"} Ready()
: Map has been initialized and is ready for user interaction.

{:id="Map.TapAtPoint"} TapAtPoint(*latitude*{:.number},*longitude*{:.number})
: The user tapped at a point on the map.

{:id="Map.ZoomChange"} ZoomChange()
: User has changed the zoom level of the map.

### Methods  {#Map-Methods}

{:.methods}

{:id="Map.CreateMarker" class="method returns component"} <i/> CreateMarker(*latitude*{:.number},*longitude*{:.number})
: Create a new marker with default properties at the specified latitude and longitude.

{:id="Map.FeatureFromDescription" class="method returns any"} <i/> FeatureFromDescription(*description*{:.list})
: Convert a feature description into an App Inventor map feature. Currently the only
 supported conversion is from a GeoJSON point to Marker component. If the feature has
 properties, they will be mapped into App Inventor properties using the following mapping:

 * description becomes Description
 * draggable becomes Draggable
 * infobox becomes EnableInfobox
 * fill becomes FillColor
 * fill-opacity becomes FillOpacity
 * image becomes ImageAsset
 * stroke becomes StrokeColor
 * stroke-opacity becomes StrokeOpacity
 * stroke-width becomes StrokeWidth
 * title becomes Title
 * visible becomes Visible

{:id="Map.LoadFromURL" class="method"} <i/> LoadFromURL(*url*{:.text})
: <p>Load a feature collection in <a href="https://en.wikipedia.org/wiki/GeoJSON">GeoJSON</a> format from the given url. On success, the event GotFeatures will be raised with the given url and a list of the features parsed from the GeoJSON as a list of (key, value) pairs. On failure, the LoadError event will be raised with any applicable HTTP response code and error message.</p>

{:id="Map.PanTo" class="method"} <i/> PanTo(*latitude*{:.number},*longitude*{:.number},*zoom*{:.number})
: Pan the map center to the given latitude and longitude and adjust the zoom level to the specified zoom.

{:id="Map.Save" class="method"} <i/> Save(*path*{:.text})
: Save the contents of the Map to the specified path.

## Marker  {#Marker}

### Properties  {#Marker-Properties}

{:.properties}

{:id="Marker.AnchorHorizontal" .number} *AnchorHorizontal*
: The horizontal alignment property controls where the Marker's anchor is located relative to its width.

{:id="Marker.AnchorVertical" .number} *AnchorVertical*
: The vertical alignment property controls where the Marker's anchor is located relative to its height.

{:id="Marker.Description" .text} *Description*
: The description displayed in the info window that appears when the user clicks on the Marker.

{:id="Marker.Draggable" .boolean} *Draggable*
: The Draggable property is used to set whether or not the user can drag the Marker by long-pressing and then dragging the Marker to a new location.

{:id="Marker.EnableInfobox" .boolean} *EnableInfobox*
: Enable or disable the infobox window display when the user taps the Marker.

{:id="Marker.FillColor" .color} *FillColor*
: The paint color used to fill in the Marker.

{:id="Marker.FillOpacity" .number} *FillOpacity*
: The opacity of the interior of the map feature.

{:id="Marker.Height" .number .bo} *Height*
: Sets the height of the marker image, in pixels.

{:id="Marker.HeightPercent" .number .wo .bo} *HeightPercent*
: Sets the height of the marker image, as a percentage of the screen height.

{:id="Marker.ImageAsset" .text} *ImageAsset*
: The ImageAsset property is used to provide an alternative image for the Marker.

{:id="Marker.Latitude" .number} *Latitude*
: The latitude of the marker, in decimal degrees, with positive values representing
 degrees north of the equator.

{:id="Marker.Longitude" .number} *Longitude*
: The longitude of the marker, in decimal degrees, with positive values representing
 degrees east of the prime meridian.

{:id="Marker.StrokeColor" .color} *StrokeColor*
: The paint color used to outline the Marker.

{:id="Marker.StrokeOpacity" .number} *StrokeOpacity*
: The opacity of the stroke used to outline the map feature.

{:id="Marker.StrokeWidth" .number} *StrokeWidth*
: The width of the stroke used to outline the Marker.

{:id="Marker.Title" .text} *Title*
: The title displayed in the info window that appears when the user clicks on the Marker.

{:id="Marker.Type" .text .ro .bo} *Type*
: The type of the map feature. For Marker, this returns the text "Marker".

{:id="Marker.Visible" .boolean} *Visible*
: Specifies whether the Marker should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="Marker.Width" .number .bo} *Width*
: Sets the width of the marker image, in pixels.

{:id="Marker.WidthPercent" .number .wo .bo} *WidthPercent*
: Sets the width of the marker image, as a percentage of the screen width.

### Events  {#Marker-Events}

{:.events}

{:id="Marker.Click"} Click()
: The user clicked on the Marker.

{:id="Marker.Drag"} Drag()
: The user dragged the Marker.

{:id="Marker.LongClick"} LongClick()
: The user long-pressed on the Marker. This event will only trigger if Draggable is false.

{:id="Marker.StartDrag"} StartDrag()
: The user started a drag operation.

{:id="Marker.StopDrag"} StopDrag()
: The user stopped a drag operation.

### Methods  {#Marker-Methods}

{:.methods}

{:id="Marker.BearingToFeature" class="method returns number"} <i/> BearingToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Returns the bearing from the Marker to the given map feature, in degrees from due north. If the centroids parameter is true, the bearing will be to the center of the map feature. Otherwise, the bearing will be computed to the point in the feature nearest the Marker.

{:id="Marker.BearingToPoint" class="method returns number"} <i/> BearingToPoint(*latitude*{:.number},*longitude*{:.number})
: Returns the bearing from the Marker to the given latitude and longitude, in degrees from due north.

{:id="Marker.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Compute the distance, in meters, between two map features.

{:id="Marker.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number})
: Compute the distance, in meters, between a Marker and a latitude, longitude point.

{:id="Marker.HideInfobox" class="method"} <i/> HideInfobox()
: Hide the infobox if it is shown. If the infobox is not visible this function has no effect.

{:id="Marker.SetLocation" class="method"} <i/> SetLocation(*latitude*{:.number},*longitude*{:.number})
: Set the location of the marker.

{:id="Marker.ShowInfobox" class="method"} <i/> ShowInfobox()
: Show the infobox for the Marker. This will show the infobox even if EnableInfobox is set to false.

## Polygon  {#Polygon}

### Properties  {#Polygon-Properties}

{:.properties}

{:id="Polygon.Description" .text} *Description*
: The description displayed in the info window that appears when the user clicks on the Polygon.

{:id="Polygon.Draggable" .boolean} *Draggable*
: The Draggable property is used to set whether or not the user can drag the Polygon by long-pressing and then dragging the Polygon to a new location.

{:id="Polygon.EnableInfobox" .boolean} *EnableInfobox*
: Enable or disable the infobox window display when the user taps the Polygon.

{:id="Polygon.FillColor" .color} *FillColor*
: The paint color used to fill in the Polygon.

{:id="Polygon.FillOpacity" .number} *FillOpacity*
: The opacity of the interior of the map feature.

{:id="Polygon.HolePoints" .list .bo} *HolePoints*
: Gets or sets the sequence of points used to draw holes in the polygon.

{:id="Polygon.HolePointsFromString" .text .wo} *HolePointsFromString*
: Constructs holes in a polygon from a given list of coordinates per hole.

{:id="Polygon.Points" .list .bo} *Points*
: Gets or sets the sequence of points used to draw the polygon.

{:id="Polygon.PointsFromString" .text .wo} *PointsFromString*
: Constructs a polygon from the given list of coordinates.

{:id="Polygon.StrokeColor" .color} *StrokeColor*
: The paint color used to outline the Polygon.

{:id="Polygon.StrokeOpacity" .number} *StrokeOpacity*
: The opacity of the stroke used to outline the map feature.

{:id="Polygon.StrokeWidth" .number} *StrokeWidth*
: The width of the stroke used to outline the Polygon.

{:id="Polygon.Title" .text} *Title*
: The title displayed in the info window that appears when the user clicks on the Polygon.

{:id="Polygon.Type" .text .ro .bo} *Type*
: The type of the feature. For polygons, this returns the text "Polygon".

{:id="Polygon.Visible" .boolean} *Visible*
: Specifies whether the Polygon should be visible on the screen. Value is true if the component is showing and false if hidden.

### Events  {#Polygon-Events}

{:.events}

{:id="Polygon.Click"} Click()
: The user clicked on the Polygon.

{:id="Polygon.Drag"} Drag()
: The user dragged the Polygon.

{:id="Polygon.LongClick"} LongClick()
: The user long-pressed on the Polygon. This event will only trigger if Draggable is false.

{:id="Polygon.StartDrag"} StartDrag()
: The user started a drag operation.

{:id="Polygon.StopDrag"} StopDrag()
: The user stopped a drag operation.

### Methods  {#Polygon-Methods}

{:.methods}

{:id="Polygon.Centroid" class="method returns list"} <i/> Centroid()
: Returns the centroid of the Polygon as a (latitude, longitude) pair.

{:id="Polygon.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Compute the distance, in meters, between two map features.

{:id="Polygon.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number},*centroid*{:.boolean})
: Compute the distance, in meters, between a Polygon and a latitude, longitude point.

{:id="Polygon.HideInfobox" class="method"} <i/> HideInfobox()
: Hide the infobox if it is shown. If the infobox is not visible this function has no effect.

{:id="Polygon.ShowInfobox" class="method"} <i/> ShowInfobox()
: Show the infobox for the Polygon. This will show the infobox even if EnableInfobox is set to false.

## Rectangle  {#Rectangle}

### Properties  {#Rectangle-Properties}

{:.properties}

{:id="Rectangle.Description" .text} *Description*
: The description displayed in the info window that appears when the user clicks on the Rectangle.

{:id="Rectangle.Draggable" .boolean} *Draggable*
: The Draggable property is used to set whether or not the user can drag the Rectangle by long-pressing and then dragging the Rectangle to a new location.

{:id="Rectangle.EastLongitude" .number} *EastLongitude*
: The east edge of the rectangle, in decimal degrees east of the prime meridian.

{:id="Rectangle.EnableInfobox" .boolean} *EnableInfobox*
: Enable or disable the infobox window display when the user taps the Rectangle.

{:id="Rectangle.FillColor" .color} *FillColor*
: The paint color used to fill in the Rectangle.

{:id="Rectangle.FillOpacity" .number} *FillOpacity*
: The opacity of the interior of the map feature.

{:id="Rectangle.NorthLatitude" .number} *NorthLatitude*
: The north edge of the rectangle, in decimal degrees north of the equator.

{:id="Rectangle.SouthLatitude" .number} *SouthLatitude*
: The south edge of the rectangle, in decimal degrees north of the equator.

{:id="Rectangle.StrokeColor" .color} *StrokeColor*
: The paint color used to outline the Rectangle.

{:id="Rectangle.StrokeOpacity" .number} *StrokeOpacity*
: The opacity of the stroke used to outline the map feature.

{:id="Rectangle.StrokeWidth" .number} *StrokeWidth*
: The width of the stroke used to outline the Rectangle.

{:id="Rectangle.Title" .text} *Title*
: The title displayed in the info window that appears when the user clicks on the Rectangle.

{:id="Rectangle.Type" .text .ro .bo} *Type*
: The type of the feature. For rectangles, this returns the text "Rectangle".

{:id="Rectangle.Visible" .boolean} *Visible*
: Specifies whether the Rectangle should be visible on the screen. Value is true if the component is showing and false if hidden.

{:id="Rectangle.WestLongitude" .number} *WestLongitude*
: The west edge of the rectangle, in decimal degrees east of the equator.

### Events  {#Rectangle-Events}

{:.events}

{:id="Rectangle.Click"} Click()
: The user clicked on the Rectangle.

{:id="Rectangle.Drag"} Drag()
: The user dragged the Rectangle.

{:id="Rectangle.LongClick"} LongClick()
: The user long-pressed on the Rectangle. This event will only trigger if Draggable is false.

{:id="Rectangle.StartDrag"} StartDrag()
: The user started a drag operation.

{:id="Rectangle.StopDrag"} StopDrag()
: The user stopped a drag operation.

### Methods  {#Rectangle-Methods}

{:.methods}

{:id="Rectangle.Bounds" class="method returns list"} <i/> Bounds()
: Returns the bounding box of the Rectangle in the format ((North West) (South East)).

{:id="Rectangle.Center" class="method returns list"} <i/> Center()
: Returns the center of the Rectangle as a list of the form (Latitude Longitude).

{:id="Rectangle.DistanceToFeature" class="method returns number"} <i/> DistanceToFeature(*mapFeature*{:.component},*centroids*{:.boolean})
: Compute the distance, in meters, between two map features.

{:id="Rectangle.DistanceToPoint" class="method returns number"} <i/> DistanceToPoint(*latitude*{:.number},*longitude*{:.number},*centroid*{:.boolean})
: Compute the distance, in meters, between a Rectangle and a latitude, longitude point.

{:id="Rectangle.HideInfobox" class="method"} <i/> HideInfobox()
: Hide the infobox if it is shown. If the infobox is not visible this function has no effect.

{:id="Rectangle.SetCenter" class="method"} <i/> SetCenter(*latitude*{:.number},*longitude*{:.number})
: Moves the Rectangle so that it is centered on the given latitude and longitude while attempting to maintain the width and height of the Rectangle as measured from the center to the edges.

{:id="Rectangle.ShowInfobox" class="method"} <i/> ShowInfobox()
: Show the infobox for the Rectangle. This will show the infobox even if EnableInfobox is set to false.