// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.List;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.LocationSensor;
import com.google.appinventor.components.runtime.Map;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.BoundingBox;

import android.os.Build;
import android.view.View;

/**
 * Utilities used by the Map component to provide backward compatibility
 * with pre-Froyo devices.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public final class MapFactory {
  /**
   * MapEventListener provides a high-level interface to respond programmatically to events on
   * the underlying MapView implementation independent of the library used.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapEventListener {
    // Map events
    /**
     * The Map is ready for user interaction.
     *
     * @param map The MapController for the underlying Map implementation.
     */
    void onReady(MapController map);

    /**
     * The Map's bounds were changed, for example due to a user-initiated zoom or pan event.
     * Users can check {@link Map#BoundingBox()} for the updated bounds.
     */
    void onBoundsChanged();

    /**
     * The Map's zoom level has changed, for example due to a user-initiated zoom event.
     * Users can check {@link Map#ZoomLevel()} for the updated zoom level.
     */
    void onZoom();

    /**
     * onSingleTap is raised when the user taps on the Map but no feature is present at the
     * location to handle the tap event.
     *
     * @param latitude Latitude of the tap event
     * @param longitude Longitude of the tap event
     */
    void onSingleTap(double latitude, double longitude);

    /**
     * onDoubleTap is raised when the user double-taps on the Map but no feature is present
     * at the location to handle the double-tap event.
     *
     * @param latitude Latitude of the double-tap event
     * @param longitude Longitude of the double-tap event
     */
    void onDoubleTap(double latitude, double longitude);

    /**
     * onLongPress is raised when the user long-presses on the Map without moving their
     * location significantly enough to trigger a panning gesture.
     *
     * @param latitude Latitude of the long-press event
     * @param longitude Longitude of the long-press event
     */
    void onLongPress(double latitude, double longitude);

    // Feature events
    /**
     * onFeatureClick is raised when the user single-taps on a {@link MapFeature}.
     *
     * @param feature The map feature clicked by the user.
     */
    void onFeatureClick(MapFeature feature);

    /**
     * onFeatureLongPress is raised when the user long-presses on a {@link MapFeature} without
     * moving their location significantly enough to trigger a dragging gesture.
     *
     * @param feature The map feature long-pressed by the user.
     */
    void onFeatureLongPress(MapFeature feature);

    /**
     * onFeatureStartDrag is raised when the user starts dragging a {@link MapFeature}.
     *
     * @param feature The map feature the user has started dragging.
     */
    void onFeatureStartDrag(MapFeature feature);

    /**
     * onFeatureDrag is raised as the user drags the map feature. Handlers for this event
     * should run quickly because they block the UI thread and will make the dragging
     * behavior appear sluggish.
     *
     * @param feature The map feature the user is dragging.
     */
    void onFeatureDrag(MapFeature feature);

    /**
     * onFeatureStopDrag is raised when the user completes a dragging motion by releasing
     * their touch from the screen.
     *
     * @param feature The map feature the user has released from dragging.
     */
    void onFeatureStopDrag(MapFeature feature);
  }

  /**
   * MapController provides a high-level abstraction for the platform-specific implementation used
   * for the Map implementation. See {@link NativeOpenStreetMapController} for an OpenStreetMap
   * based solution compatible with newer devices and {@link DummyMapController} for
   * a version that works on any platform using a WebView component.
   *
   * @author ewpatton
   */
  public interface MapController {
    /**
     * Get the underlying Android view used for the Map in the view hierarchy.
     *
     * @return The Map's View
     */
    View getView();

    /**
     * Get the latitude at the center of the Map's viewport.
     *
     * @return the latitude of the map center
     */
    double getLatitude();

    /**
     * Get the longitude at the center of the Map's viewport.
     *
     * @return the longitude of the map center
     */
    double getLongitude();

    /**
     * Set the center of the map to a specific coordinate.
     *
     * @param latitude  the latitude of the map center
     * @param longitude  the longitude of the map center
     */
    void setCenter(double latitude, double longitude);

    /**
     * Set the zoom level of the Map. Valid values range from 0-18, depending on the selected
     * tile layer.
     *
     * @param zoom  the new zoom level to set on the map
     * @throws IllegalArgumentException if the zoom level given is outside of the range of valid
     * values for the active tile layer.
     */
    void setZoom(int zoom);

    /**
     * Get the zoom level of the Map.
     *
     * @return the zoom level of the map
     */
    int getZoom();

    /**
     * Get the type of the map being used.
     *
     * @return the type of the map's active tile layer
     */
    MapType getMapType();

    /**
     * Set the type of the map being used.
     *
     * @param type the new map type for the map
     */
    void setMapType(MapType type);

    /**
     * Set whether the compass is displayed on the map.
     *
     * @param enable true if the compass overlay should be shown, otherwise false.
     */
    void setCompassEnabled(boolean enable);

    /**
     * Get whether the compass is displayed on the map.
     *
     * @return true if the compass overlay is enabled, otherwise false.
     */
    boolean isCompassEnabled();

    /**
     * Set whether zooming is enabled on the map.
     *
     * @param enable true if the zoom gestures (pinch/expand) should be enabled on the map,
     *               otherwise false.
     */
    void setZoomEnabled(boolean enable);

    /**
     * Get whether zooming is enabled on the map.
     *
     * @return true if the zoom gestures are enabled, otherwise false.
     */
    boolean isZoomEnabled();

    /**
     * Set whether zoom controls are enabled on the map.
     *
     * @param enable true if the zoom buttons should be shown on the map
     */
    void setZoomControlEnabled(boolean enable);

    /**
     * Get whether zoom controls are enabled on the map.
     *
     * @return true if zoom controls are enabled, otherwise false.
     */
    boolean isZoomControlEnabled();

    /**
     * Set whether the user's location is displayed on the map.
     *
     * @param enable true if the user location should be displayed on the map, otherwise false.
     */
    void setShowUserEnabled(boolean enable);

    /**
     * Get whether the user's location is displayed on the map. Note that this does not
     * necessarily mean that the indicator is visible (e.g., GPS fix has not been acquired).
     *
     * @return true if the user location overlay is enabled, otherwise false.
     */
    boolean isShowUserEnabled();

    /**
     * Set whether rotation gestures are enabled on the map.
     *
     * @param enable true if the map should allow rotation, false otherwise. Implementations that
     *               do not support rotation need not honor this flag.
     */
    void setRotationEnabled(boolean enable);

    /**
     * Get whether rotation gestures are enabled on the map.
     *
     * @return true if rotation is enabled, otherwise false.
     */
    boolean isRotationEnabled();

    /**
     * Set whether panning gestures are enabled on the map.
     *
     * @param enable true if the map should allow panning, false otherwise. Implementations that
     *               do not support panning need not honor this flag.
     */
    void setPanEnabled(boolean enable);

    /**
     * Get whether panning gestures are enabled on the map.
     *
     * @return true if panning is enabled, otherwise false.
     */
    boolean isPanEnabled();

    /**
     * Pan to the indicated latitude and longitude, zooming to the specified zoom level over a
     * set period of time.
     *
     * @param latitude Latitude of the new center for the map's viewport.
     * @param longitude Longitude of the new center for the map's viewport.
     * @param zoom Possibly new zoom level for the map's viewport.
     * @param seconds The amount of time to animate the viewport transition.
     */
    void panTo(double latitude, double longitude, int zoom, double seconds);

    /**
     * Add an event listener to the Map controller.
     *
     * @param listener an object that will receive map events
     */
    void addEventListener(MapEventListener listener);

    /**
     * Get the bounding box for the Map. If rotation is enabled, it is possible that the bounding
     * box will be larger than the phone's viewport.
     *
     * @return the bounding box of the map's viewport.
     */
    BoundingBox getBoundingBox();

    /**
     * Set the bounding box for the Map.
     *
     * @param bbox the new bounding box for the viewport. The box may be adjusted based on the
     *             device viewport and available zoom levels from the current map type.
     */
    void setBoundingBox(BoundingBox bbox);

    /**
     * Add a marker to the map.
     *
     * @param marker the marker to add to the map
     */
    void addFeature(MapMarker marker);

    /**
     * Add a linestring to the map.
     *
     * @param linestring the line string to add to the map
     */
    void addFeature(MapLineString linestring);

    /**
     * Add a polygon to the map.
     *
     * @param polygon the polygon to add to the map
     */
    void addFeature(MapPolygon polygon);

    /**
     * Add a circle to the map.
     *
     * @param circle the circle to add to the map
     */
    void addFeature(MapCircle circle);

    /**
     * Add a rectangle to the map.
     *
     * @param rectangle the rectangle to add to the map
     */
    void addFeature(MapRectangle rectangle);

    /**
     * Remove a map feature.
     *
     * @param feature the map feature to remove
     */
    void removeFeature(MapFeature feature);

    /**
     * Show a feature on the map. The feature must have been previously added
     * via one of the addFeature calls. Features will be shown by default if the
     * {@link com.google.appinventor.components.runtime.AndroidViewComponent#Visible()}
     * property is true.
     *
     * @see #hideFeature(MapFeature)
     * @param feature the feature to show
     * @throws IllegalArgumentException if the feature is not on the map.
     */
    void showFeature(MapFeature feature);

    /**
     * Hide a feature on the map. The feature must have been previously added
     * via one of the addFeature calls. Features will be hidden by default if
     * the
     * {@link com.google.appinventor.components.runtime.AndroidViewComponent#Visible()}
     * property is false.
     *
     * @see #showFeature(MapFeature)
     * @param feature the feature to hide
     */
    void hideFeature(MapFeature feature);

    /**
     * Gets whether the feature is visible or not.
     *
     * @param feature the feature to check
     * @return  true if the feature is visible, otherwise false. This may not guarantee that the
     * feature is drawn within the viewport. It may be offscreen.
     */
    boolean isFeatureVisible(MapFeature feature);

    /**
     * Show the infobox attached to a map feature. The feature must have been
     * previously added via one of the addFeature calls and must be shown on the
     * map. The infobox will also be shown as part of the default click action
     * on a map feature.
     *
     * @see #hideInfobox(MapFeature)
     * @param feature the feature with the infobox to show
     * @throws IllegalArgumentException if the feature is not on the map
     */
    void showInfobox(MapFeature feature);

    /**
     * Hide the infobox attached to a map feature. The feature must have been
     * previously added via one of the addFeature calls, the feature must be
     * shown on the map, and the infobox must be displayed for this call to have
     * any effect.
     *
     * @see #showInfobox(MapFeature)
     * @param feature the feature with the infobox to hide
     */
    void hideInfobox(MapFeature feature);

    /**
     * Gets whether the feature's infobox is visible. If the feature does not have an infobox,
     * this will always return false.
     *
     * @return  true if the infobox exists and is visible, otherwise false. The infobox may be
     * visible but drawn offscreen if it is not within the map bounds.
     */
    boolean isInfoboxVisible(MapFeature feature);

    /**
     * Update the position of the marker on the map.
     *
     * @param marker the marker that needs its position updated on the map
     */
    void updateFeaturePosition(MapMarker marker);

    /**
     * Update the position of a linestring on the map.
     *
     * @param linestring the line string that needs its position updated on the map
     */
    void updateFeaturePosition(MapLineString linestring);

    /**
     * Update the position of a polygon on the map.
     *
     * @param polygon the polygon that needs its position updated on the map
     */
    void updateFeaturePosition(MapPolygon polygon);

    /**
     * Update the position of a circle on the map.
     *
     * @param circle the circle that needs its position updated on the map
     */
    void updateFeaturePosition(MapCircle circle);

    /**
     * Update the position of a rectangle on the map.
     *
     * @param rectangle the rectangle that needs its position updated on the map
     */
    void updateFeaturePosition(MapRectangle rectangle);

    /**
     * Update the fill color of any map feature that has fill properties.
     *
     * @param feature the map feature that needs its fill color updated on the map
     */
    void updateFeatureFill(HasFill feature);

    /**
     * Update the stroke width and color of any map feature that has stroke
     * properties.
     *
     * @param feature the map feature that needs its stroke color and width updated on the map
     */
    void updateFeatureStroke(HasStroke feature);

    /**
     * Update the text content of a feature's infobox.
     *
     * @param feature the map feature that needs its infobox updated
     */
    void updateFeatureText(MapFeature feature);

    /**
     * Update whether a feature is draggable in the map.
     *
     * @param feature the map feature that needs its draggable state to be updated
     */
    void updateFeatureDraggable(MapFeature feature);

    /**
     * Update the image used for a marker.
     *
     * @param marker the marker that needs its image updated on the map
     */
    void updateFeatureImage(MapMarker marker);

    /**
     * Update the size of a marker.
     *
     * @param marker the marker that needs its size to be updated on the map
     */
    void updateFeatureSize(MapMarker marker);

    /**
     * Get the location sensor implementation used to link the LocationSensorListener interface
     * to the underlying implementation's location sensor mechanism.
     *
     * @return  a location sensor listener specific to the map instance
     */
    LocationSensor.LocationSensorListener getLocationListener();

    /**
     * Gets the number of overlays present on the map.
     *
     * @return  the number of overlays on the map
     */
    int getOverlayCount();

    /**
     * Sets the rotation of the map in degrees
     * @param Rotation in degrees
     */
    void setRotation(float Rotation);

    /**
     * Gets the rotation of the map in degrees
     * @return the rotation
     */
    float getRotation();

    /**
     * Sets whether or not the scale overlay is visible.
     * @param show True if the scale should be shown, otherwise false.
     */
    void setScaleVisible(boolean show);

    /**
     * Gets the visibility of the scale on the map. A true value does
     * not guarantee that the scale is visible to the user (i.e., if
     * the Map is not visible).
     * @returns true if the scale is enabled on the map, otherwise false.
     */
    boolean isScaleVisible();

    /**
     * Sets the units for the scale. Options are either "metric" or "imperial"
     * @param units the new units to show for the scale
     */
    void setScaleUnits(MapScaleUnits units);

    /**
     * Gets the units for the scale.
     * @return the units used for the scale overlay
     */
    MapScaleUnits getScaleUnits();
  }

  /**
   * MapFeature is the root class of all features that are placed on the map. It inherits from the
   * Component interface since all MapFeatures are naturally components. MapFeature defines the
   * properties that are generally required by any map feature that should be implemented for App
   * Inventor.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapFeature extends Component {
    // Properties

    /**
     * The type (class) of the map feature. Typically this should be getClass().getSimpleName().
     * @return string representing the type of the feature
     */
    String Type();

    /**
     * Gets whether the component should be visible on the screen.
     * @return true if the feature is visible, otherwise false.
     */
    boolean Visible();

    /**
     * Sets whether the component should be visible on the screen.
     * @param visible true if the component should be visible, otherwise false.
     */
    void Visible(boolean visible);

    /**
     * Sets whether the map feature is draggable on the map, assuming that the map implementation
     * supports dragging features.
     * @param draggable true if the feature should be draggable by the app user, otherwise false.
     */
    void Draggable(boolean draggable);

    /**
     * Gets whether the map feature is draggable on the map.
     * @return true if the feature should be draggable by the end user, otherwise false.
     */
    boolean Draggable();

    /**
     * Sets the title of the feature's infobox.
     * @param title the title of the feature's infobox
     */
    void Title(String title);

    /**
     * Gets the title of the feature's infobox.
     * @return the title of the feature's infobox.
     */
    String Title();

    /**
     * Sets the long text description in the infobox.
     * @param description the description of the feature as shown in the infobox.
     */
    void Description(String description);

    /**
     * Gets the long text description in the infobox.
     * @return the description of the feature as shown in the infobox.
     */
    String Description();

    /**
     * Sets whether the infobox should automatically show when the app user taps the feature
     * @param enable true if the infobox should automatically show when the feature is tapped by
     *               the app user, otherwise false.
     */
    void EnableInfobox(boolean enable);

    /**
     * Gets whether the infobox will automatically show when the user taps the feature
     * @return true if the infobox will show automatically, otherwise false.
     */
    boolean EnableInfobox();

    // Functions

    /**
     * Shows the infobox for the feature even if it is not visible. Otherwise, this method has no
     * effect. This method can be used to show the infobox even if {@link #EnableInfobox()} is
     * false.
     */
    void ShowInfobox();

    /**
     * Hides the feature's infobox if it is visible. Otherwise, this method has no effect.
     */
    void HideInfobox();

    // Events

    /**
     * Runs when the user taps on the feature.
     */
    void Click();

    /**
     * Runs when the user long-presses on the feature but does not trigger a drag. Note that this
     * event will only run if {@link #Draggable()} is false.
     */
    void LongClick();

    /**
     * Runs when the user begins a drag operation on the feature.
     */
    void StartDrag();

    /**
     * Runs continuously as the user performs a drag operation on the feature.
     */
    void Drag();

    /**
     * Runs when the user releases a feature at the end of a drag operation.
     */
    void StopDrag();

    // non-component methods

    /**
     * Makes the feature accept the given <code>visitor</code> object.
     * @param visitor a visitor that will operate on the feature.
     * @param arguments any additional arguments to pass to the visitor's visit function.
     * @param <T> the return type of the visitor's visit function.
     * @return the value returned by the visitor's visit function applied to the feature.
     */
    <T> T accept(MapFeatureVisitor<T> visitor, Object... arguments);

    /**
     * Gets the centroid of the feature.
     * @return the feature's centroid.
     */
    GeoPoint getCentroid();

    /**
     * Gets the geometry of the feature, used for calculating distance and bearing measures.
     * @return the feature's geometry
     */
    Geometry getGeometry();

    /**
     * Sets the {@link Map} containing the feature. This must remove the feature from its
     * previous map.
     * @param map the new map on which to draw the feature
     */
    void setMap(MapFeatureContainer map);

    /**
     * Removes the feature from its current {@link Map}.
     */
    void removeFromMap();
  }

  /**
   * MapFeatureVisitor is a realization of the Visitor design pattern for {@link MapFeature}. It
   * allows the features to differentiate themselves so that type-specific processing can occur
   * on the feature.
   * @param <T> the return type that the visit method will return. If nothing will be returned, use
   *           {@link Void} and return <code>null</code>.
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapFeatureVisitor<T> {
    /**
     * Visit the {@link MapMarker}.
     * @param marker the marker to visit
     * @param arguments any additional arguments passed to the
     *                  {@link MapFeature#accept(MapFeatureVisitor, Object...)} method.
     * @return type T, specific to the concrete implementation
     */
    T visit(MapMarker marker, Object... arguments);

    /**
     * Visit the {@link MapLineString}.
     * @param lineString the linestring to visit
     * @param arguments any additional arguments passed to the
     *                  {@link MapFeature#accept(MapFeatureVisitor, Object...)} method.
     * @return type T, specific to the concrete implementation
     */
    T visit(MapLineString lineString, Object... arguments);

    /**
     * Visit the {@link MapPolygon}.
     * @param polygon the polygon to visit
     * @param arguments any additional arguments passed to the
     *                  {@link MapFeature#accept(MapFeatureVisitor, Object...)} method.
     * @return type T, specific to the concrete implementation
     */
    T visit(MapPolygon polygon, Object... arguments);

    /**
     * Visit the {@link MapCircle}.
     * @param circle the circle to visit
     * @param arguments any additional arguments passed to the
     *                  {@link MapFeature#accept(MapFeatureVisitor, Object...)} method.
     * @return type T, specific to the concrete implementation
     */
    T visit(MapCircle circle, Object... arguments);

    /**
     * Visit the {@link MapRectangle}.
     * @param rectangle the rectangle to visit
     * @param arguments any additional arguments passed to the
     *                  {@link MapFeature#accept(MapFeatureVisitor, Object...)} method.
     * @return type T, specific to the concrete implementation
     */
    T visit(MapRectangle rectangle, Object... arguments);
  }

  /**
   * MapFeatureContainer defines the API for components that contain {@link MapFeature} objects.
   * MapFeatureContainer inherits from {@link ComponentContainer} because it contains components.
   * Any new components that are designed to contain {@link MapFeature} should implment this
   * interface.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapFeatureContainer extends ComponentContainer {

    // Properties

    /**
     * Sets the list of {@link MapFeature} managed by this container. This can be used to
     * dynamically update the container with new features.
     * @param features the list of features to be contained by the container. It is an unchecked
     *                 exception to pass a {@link YailList} containing anything other than
     *                 instances of {@link MapFeature}.
     */
    void Features(YailList features);

    /**
     * Gets the list of {@link MapFeature} contained within the MapFeatureContainer.
     * @return a list of {@link MapFeature}
     */
    YailList Features();

    // Events

    /**
     * Runs when the app user clicks on a child of the feature collection. Events bubble from the
     * most specific component (i.e., the feature) to the least specific (i.e., the map).
     * @param feature the clicked feature
     */
    void FeatureClick(MapFeature feature);

    /**
     * Runs when the app user long-pressed on a child of the feature collection. Events bubble from
     * the most specific component (i.e., the feature) to the least specific (i.e., the map).
     * @param feature the long-pressed feature
     */
    void FeatureLongClick(MapFeature feature);

    /**
     * Runs when the app user starts dragging a child of the feature collection. Events bubble from
     * the most specific component (i.e., the feature) to the least specific (i.e., the map).
     * @param feature the dragged feature
     */
    void FeatureStartDrag(MapFeature feature);

    /**
     * Runs when the app user continues dragging a child of the feature collection. Events bubble
     * from the most specific component (i.e., the feature) to the least specific (i.e., the map).
     * @param feature the dragged feature
     */
    void FeatureDrag(MapFeature feature);

    /**
     * Runs when the app user stop dragging a child of the feature collection. Events bubble from
     * the most specific component (i.e., the feature) to the least specific (i.e., the map).
     * @param feature the dragged feature
     */
    void FeatureStopDrag(MapFeature feature);

    // non-component methods

    /**
     * Gets the map containing the feature collection.
     * @return the feature collection's map
     */
    Map getMap();

    /**
     * Adds a feature to the feature collection.
     * @param feature the feature to add
     */
    void addFeature(MapFeature feature);

    /**
     * Removes a feature from the feature collection.
     * @param feature the feature to remove
     */
    void removeFeature(MapFeature feature);
  }

  /**
   * HasFill is a marker interface for features that draw with a fill.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface HasFill {
    /**
     * Sets the color used to paint the interior of the feature.
     * @param argb the fill paint color
     */
    void FillColor(int argb);

    /**
     * Gets the color used to paint the interior of the feature
     * @return the fill paint color
     */
    int FillColor();
  }

  /**
   * HasStroke is a marker interface for features that draw with a stroke.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface HasStroke {
    /**
     * Sets the color used to paint the outline of the feature
     * @param argb the outline paint color
     */
    void StrokeColor(int argb);

    /**
     * Gets the color used to paint the outline of the feature
     * @return the outline paint color
     */
    int StrokeColor();

    /**
     * Sets the width of the outline of the feature
     * @param width the outline width
     */
    void StrokeWidth(int width);

    /**
     * Gets the width of the outline of the feature
     * @return the outline width
     */
    int StrokeWidth();
  }

  /**
   * MapFeatureContainer defines the API for the
   * {@link com.google.appinventor.components.runtime.FeatureCollection} component.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapFeatureCollection extends MapFeatureContainer {
    // Properties

    /**
     * Gets whether the map feature collection is visible on the map.
     * @return true if the collection should be drawn, otherwise false.
     */
    boolean Visible();

    /**
     * Gets the list of features contained by the feature collection.
     * @return the list of features in the collection.
     */
    YailList Features();

    /**
     * Sets the source of the feature collection. This is a designer only property that is used
     * to support loading feature collections in the web editor.
     * @param source asset name or url from which to load the feature collection
     */
    void Source(String source);

    /**
     * Gets the source of the feature collection.
     * @return the last asset or url from which the feature collection was successfully read.
     */
    String Source();

    // Functions

    /**
     * Loads the contents from the given {@code url}. On success, the
     * {@link #GotFeatures(String, YailList)} event is run with a description of the features.
     * On error, {@link #LoadError(String, int, String)} is run with a description of the error.
     * At this time, the only supported format is GeoJSON.
     * @param url the URL from which to load a feature collection.
     */
    void LoadFromURL(String url);

    // Events

    /**
     * Runs when a call to {@link #LoadFromURL(String)} succeeds. {@code features} will contain a
     * list of feature descriptions that can be used with
     * {@link Map#FeatureFromDescription(YailList)} to construct new features.
     * @param url the url corresponding to the requested url in {@link #LoadFromURL(String)}
     * @param features the list of feature descriptions read from the resource at {@code url}
     */
    void GotFeatures(String url, YailList features);

    /**
     * Runs when an error occurs reading a feature collection from a URL.
     * @param url the URL from which a read was attempted.
     * @param code the HTTP status code reported by the server.
     * @param message a human-readable error message sent by the server.
     */
    void LoadError(String url, int code, String message);
  }

  /**
   * MapCircle defines the API for the {@link com.google.appinventor.components.runtime.Circle}
   * component.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapCircle extends MapFeature, HasFill, HasStroke {
    // Properties

    /**
     * Sets the radius of the circle, in meters.
     * @param radius the radius in meters.
     */
    void Radius(double radius);

    /**
     * Gets the radius of the circle, in meters.
     * @return the radius in meters.
     */
    double Radius();

    /**
     * Sets the latitude of the center of the circle, in decimal degrees, with positive values
     * representing latitudes north of the equator.
     * @param latitude the new latitude of the circle, in degrees.
     */
    void Latitude(double latitude);

    /**
     * Gets the latitude of the center of the circle, in decimal degrees, with positive values
     * representing latitudes north of the equator.
     * @return the latitude of the circle, in degrees.
     */
    double Latitude();

    /**
     * Sets the longitude of the center of the circle, in decimal degrees, with positive values
     * representing longitudes east of the prime meridian.
     * @param longitude the new longitude of the circle, in degrees.
     */
    void Longitude(double longitude);

    /**
     * Gets the longitude of the center of the circle, in decimal degrees, with positive values
     * representing longitudes east of the prime meridian.
     * @return the longitude of the circle, in degrees.
     */
    double Longitude();

    // Methods

    /**
     * Sets the location of the circle center. This is more efficient than separately setting
     * the latitude and longitude.
     * @param latitude the new latitude of the circle center, in decimal degrees.
     * @param longitude the new longitude of the circle center, in decimal degrees.
     */
    void SetLocation(double latitude, double longitude);

    // Updates from native view

    /**
     * Applies updates from the native view. No validation of the latitude, longitude is performed,
     * and the owning map view is not invalidated.
     *
     * @param latitude a latitude between -90 and 90
     * @param longitude a longitude between -180 and 180
     */
    void updateCenter(double latitude, double longitude);
  }

  /**
   * MapRectangle defines the API for the
   * {@link com.google.appinventor.components.runtime.Rectangle} compoennt.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapRectangle extends MapFeature, HasFill, HasStroke {
    // Properties

    /**
     * Sets the eastern boundary of the rectangle, in decimal degrees, with positive values
     * representing longitudes east of the prime meridian.
     * @param east the new east bound of the rectangle, in decimal degrees. Range: [-180, 180]
     */
    void EastLongitude(double east);

    /**
     * Gets the eastern boundary of the rectangle, in decimal degrees, with positive values
     * representing longitudes east of the prime meridian.
     * @return the east bound of the rectangle, in decimal degrees.
     */
    double EastLongitude();

    /**
     * Sets the northern boundary of the rectangle, in decimal degrees, with positive values
     * representing latitudes north of the equator.
     * @param north the new northern bound of the rectangle, in decimal degrees. Range: [-90, 90]
     */
    void NorthLatitude(double north);

    /**
     * Gets the northern boundary of the rectangle, in decimal degrees, with positive values
     * representing latitudes north of the equator.
     * @return the north bound of the rectangle, in decimal degrees.
     */
    double NorthLatitude();

    /**
     * Sets the southern boundary of the rectangle, in decimal degrees, with positive values
     * representing latitudes north of the equator.
     * @param south the new southern bound of the rectangle, in decimal degrees. Range: [-90, 90]
     */
    void SouthLatitude(double south);

    /**
     * Gets the southern boundary of the rectangle, in decimal degrees, with positive values
     * representing latitudes north of the equator.
     * @return the south bound of the rectangle, in decimal degrees.
     */
    double SouthLatitude();

    /**
     * Sets the western boundary of the rectangle, in decimal degrees, with positive values
     * representing longitudes east of the prime meridian.
     * @param west the new western bound of the rectangle, in decimal degrees. Range: [-180, 180]
     */
    void WestLongitude(double west);

    /**
     * Gets the western boundary of the rectangle, in decimal degrees, with positive values
     * representing longitudes east of the prime meridian.
     * @return the west bound of the rectangle, in decimal degrees.
     */
    double WestLongitude();

    /**
     * Gets the geographic center of the rectangle given the current boundary.
     * @return the center of the rectangle as a list in the form {@code (latitude, longitude)}.
     */
    YailList Center();

    /**
     * Gets the bounds of the rectangle as north-west, south-east pair in the form
     * {@code ((North, West), (South, East))}
     * @return a list containing the northwest and southeast corners of the rectangle
     */
    YailList Bounds();

    // Methods

    /**
     * Moves the rectangle so that its center is at the given latitude and longitude. The latitude
     * and longitude extents of the rectangle are designed to be kept equal at the center point to
     * the best of App Inventor's ability. For example, moving a rectangle to the north pole cannot
     * be represented in a web mercator projection and so will not be accurately reflected in the
     * map view.
     * @param latitude the latitude of the new rectangle center
     * @param longitude the longitude of the new rectangle center.
     */
    void SetCenter(double latitude, double longitude);

    // Updates from native view

    /**
     * Updates the rectangle from the native view. This method _should not_ attempt to update the
     * native view as this may result in an infinite loop since the update may call this
     * implementation.
     *
     * @param north  the north latitude of the rectangle bounds
     * @param west  the west longitude of the rectangle bounds
     * @param south  the south latitude of the rectangle bounds
     * @param east  the east longitude of the rectangle bounds
     */
    void updateBounds(double north, double west, double south, double east);
  }

  /**
   * MapMarker defines the API for the {@link com.google.appinventor.components.runtime.Marker}
   * component.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapMarker extends MapFeature, HasFill, HasStroke {
    // Properties

    /**
     * Sets the image used to render the marker to the given path. If path is the empty string, or
     * no asset is present at {@code path}, then the default marker icon will be used.
     * @param path a relative or absolute path, or a url, to an image asset to use for the marker.
     */
    @SuppressWarnings("squid:S00100")
    void ImageAsset(String path);

    /**
     * Gets the path of the image used to render the marker. If the default icon is used, the path
     * will be the empty string.
     * @return path of the marker's image, or the empty string.
     */
    @SuppressWarnings("squid:S00100")
    String ImageAsset();

    /**
     * Sets the latitude of the marker, in decimal degrees, with positive values representing
     * degrees north of the equator.
     * @param latitude the new latitude of the marker. Range: [-90, 90]
     */
    @SuppressWarnings("squid:S00100")
    void Latitude(double latitude);

    /**
     * Gets the latitude of the marker, in decimal degrees, with positive values representing
     * degrees north of the equator.
     * @return the latitude of the marker
     */
    @SuppressWarnings("squid:S00100")
    double Latitude();

    /**
     * Sets the longitude of the marker, in decimal degrees, with positive values representing
     * degrees east of the prime meridian.
     * @param longitude the new longitude of the marker. Range: [-180, 180]
     */
    @SuppressWarnings("squid:S00100")
    void Longitude(double longitude);

    /**
     * Gets the longitude of the marker, in decimal degrees, with positive values representing
     * degrees east of the prime meridian.
     * @return the longitude of the marker
     */
    @SuppressWarnings("squid:S00100")
    double Longitude();

    /**
     * Sets the horizontal position of the marker image relative to the marker location. Valid
     * values are 1 (left), 2 (right), and 3 (center).
     * @param horizontal the new horizontal alignment of the marker image relative to its point
     *                   location.
     */
    @SuppressWarnings("squid:S00100")
    void AnchorHorizontal(int horizontal);

    /**
     * Gets the horizontal position of the marker image relative to its location. The value will be
     * either 1, 2, or 3.
     * @return the horizontal image position relative to the marker location
     */
    @SuppressWarnings("squid:S00100")
    int AnchorHorizontal();

    /**
     * Sets the vertical position of the marker image relative to the marker location. Valid values
     * are 1 (top), 2 (center), and 3 (bottom).
     * @param vertical the new vertical alignment of the marker image relative to its point
     *                 location.
     */
    @SuppressWarnings("squid:S00100")
    void AnchorVertical(int vertical);

    /**
     * Gets the vertical position of the marker image relative to its location. The value will be
     * either 1, 2, or 3.
     * @return the vertical image position relative to the marker location
     */
    @SuppressWarnings("squid:S00100")
    int AnchorVertical();

    /**
     * Sets whether to show a shadow under the marker.
     * @param show show a shadow under the marker
     * @deprecated This isn't guaranteed to be supported on multiple platforms so it was deprecated
     * prior to official release. Some apps developed using earlier versions of Maps on test servers
     * may reference this property, but it may be removed in a future version of App Inventor.
     */
    @SuppressWarnings("squid:S00100")
    void ShowShadow(boolean show);

    /**
     * Gets whether the map should render a shadow under the marker.
     * @return true if the marker should have a shadow, otherwise false.
     * @deprecated See the deprecation message for {@link #ShowShadow(boolean)}.
     */
    @SuppressWarnings("squid:S00100")
    boolean ShowShadow();

    /**
     * Sets the width of the marker image, in pixels.
     * @param width the new width of the marker image
     */
    @SuppressWarnings("squid:S00100")
    void Width(int width);

    /**
     * Gets the width of the marker image, in pixels.
     * @return the marker image width
     */
    @SuppressWarnings("squid:S00100")
    int Width();

    /**
     * Sets the height of the marker image, in pixels.
     * @param height the new height of the marker image
     */
    @SuppressWarnings("squid:S00100")
    void Height(int height);

    /**
     * Gets the height of the marker image, in pixels.
     * @return the marker image height
     */
    @SuppressWarnings("squid:S00100")
    int Height();

    // Methods

    /**
     * Sets the location of the marker to the given {@code latitude} and {@code longitude}. This is
     * more efficient than separately setting the latitude and longitude using the corresponding
     * properties.
     * @param latitude the new latitude of the marker
     * @param longitude the new longitude of the marker
     */
    @SuppressWarnings("squid:S00100")
    void SetLocation(double latitude, double longitude);

    // convenience methods

    /**
     * Gets the location of the Marker.
     *
     * @return the (latitude, longitude) of the marker's position as a IGeoPoint.
     */
    IGeoPoint getLocation();

    /**
     * Updates the location of the marker without triggering any events or a redraw. This is used to update the App
     * Inventor object in response to a user action in the map (e.g., dragging).
     *
     * @param latitude New latitude of the Marker
     * @param longitude New longitude of the Marker
     */
    void updateLocation(double latitude, double longitude);
  }

  /**
   * MapLineString defines the API for the
   * {@link com.google.appinventor.components.runtime.LineString} component.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapLineString extends MapFeature, HasStroke {

    /**
     * Gets the list of points defining the LineString.
     * @return a list of points in (lat, long) format.
     */
    YailList Points();

    /**
     * Sets the list of points defining the LineString. The list must contain at least two points.
     * @param points the new coordinates for the LineString.
     */
    void Points(YailList points);

    // For convenient java manipulation

    /**
     * Gets the list of points in {@link GeoPoint} format.
     * @return the LineString's points
     */
    List<GeoPoint> getPoints();

    /**
     * Updates the points of the LineString without refreshing the map. This is intended to be
     * called to finalize a drag event when the visual representation is correct but the internal
     * representation in the component is not. NB: This should not invalidate the map view.
     * @param points the new points for the LineString.
     */
    void updatePoints(List<GeoPoint> points);
  }

  /**
   * MapPolygon defines the API for the {@link com.google.appinventor.components.runtime.Polygon}
   * component.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface MapPolygon extends MapFeature, HasFill, HasStroke {

    /**
     * Gets the list of points defining the polygon, in (lat, long) format. If the polygon is
     * actually a multipolygon, this will return a list of lists of points, with each element in
     * the outer list representing a portion of the multipolygon.
     * @return a list of the polygon's points.
     */
    YailList Points();

    /**
     * Sets the list of points defining the polygon. If the list is actually a list of lists, it
     * will convert the polygon into a multipolygon.
     * @param points a list of points defining the polygon.
     */
    void Points(YailList points);

    /**
     * Gets the list of points defining holes in the polygon, in (lat, long) format. If the polygon
     * is actually a multipolygon, this will return a list of lists of lists, with each element in
     * the outer list corresponding to one of the polygon parts defined in {@link #Points()}.
     * @return a list of the polygon's hole points.
     */
    YailList HolePoints();

    /**
     * Sets the list of points defining holes in the polygon. The arity of the lists of lists must
     * match the number of polygons defined (if a multipolygon) or otherwise be 0.
     * @param points a list of lists of lists defining the holes in each part of the multipolygon.
     */
    void HolePoints(YailList points);

    // For convenient java manipulation

    /**
     * Gets the points defining the (multi-)polygon in {@link GeoPoint} format.
     * @return a list of lists of GeoPoint. Note that this is consistent with the multipolygon
     * format and for single polygons the size of the list will be 1.
     */
    List<List<GeoPoint>> getPoints();

    /**
     * Gets the hole points for the (multi-)polygon, if any, in {@link GeoPoint} format.
     * @return a list of lists of lists of GeoPoint, where each element in the outermost list
     * corresponds to a polygon defined at the n-th position in {@link #getPoints()}.
     */
    List<List<List<GeoPoint>>> getHolePoints();

    /**
     * Updates the internal representation of the points defining the polygon. This is intended to
     * only be called to finalize a drag operation so that the internal representation is
     * consistent with the final visual location of the polygon at the end of the drag. NB: This
     * should not invalidate the map view.
     * @param points the ring list representing the new points for the (multi-)polyon.
     */
    void updatePoints(List<List<GeoPoint>> points);

    /**
     * Updates the internal representation of the hole points for the (multi-)polygon. This is
     * intended to only be called to finalize a drag operation so that the internal representation
     * is consistent with the final visual location of the polygon at the end of the drag. NB: This
     * should not invalidate the map view.
     * @param points the lists of ring lists representing the new hole locations for the
     *               (multi-)polygon.
     */
    void updateHolePoints(List<List<List<GeoPoint>>> points);
  }

  /**
   * MapFeatureType reserves keywords for the different feature types supported by App Inventor.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public static final class MapFeatureType {
    private MapFeatureType() {}

    /**
     * The Marker type.
     */
    public static final String TYPE_MARKER = "Marker";

    /**
     * The Circle type.
     */
    public static final String TYPE_CIRCLE = "Circle";

    /**
     * The Rectangle type.
     */
    public static final String TYPE_RECTANGLE = "Rectangle";

    // the following 6 types are defined by GeoJSON

    /**
     * The Point type. Implemented as {@link com.google.appinventor.components.runtime.Marker}.
     */
    public static final String TYPE_POINT = "Point";

    /**
     * The LineString type. Implemented as {@link com.google.appinventor.components.runtime.LineString}.
     */
    public static final String TYPE_LINESTRING = "LineString";

    /**
     * The Polygon type. Implemented as {@link com.google.appinventor.components.runtime.Polygon}.
     */
    public static final String TYPE_POLYGON = "Polygon";

    /**
     * The MultiPoint type. Reserved for future use.
     */
    @SuppressWarnings("unused")
    public static final String TYPE_MULTIPOINT = "MultiPoint";

    /**
     * The MultiLineString type. Reserved for future use.
     */
    @SuppressWarnings("unused")
    public static final String TYPE_MULTILINESTRING = "MultiLineString";

    /**
     * The MultiPolygon type. Implemented as {@link com.google.appinventor.components.runtime.Polygon}.
     */
    public static final String TYPE_MULTIPOLYGON = "MultiPolygon";
  }

  /**
   * MapType defines the supported map types for App Inventor.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public enum MapType {
    /**
     * Reserved. Makes the map types start from 1 to be consistent with App Inventor design
     * principles.
     */
    UNKNOWN,

    /**
     * Roads tile layer.
     */
    ROADS,

    /**
     * Aerial/satellite tile layer.
     */
    AERIAL,

    /**
     * Terrain tile layer.
     */
    TERRAIN
  }

  /**
   * MapScaleUnits defines the available unit systems for rendering the map scale overlay.
   */
  public enum MapScaleUnits {
    /**
     * Reserved. Makes the map scale units start from 1 to be consistent with App Inventor design
     * principles.
     */
    UNKNOWN,

    /**
     * Metric units (km, m)
     */
    METRIC,

    /**
     * Imperial units (mi, ft)
     */
    IMPERIAL
  }

  /**
   * Constructs a new map for the given form. On versions of Android that are too old for Maps
   * support, a {@link DummyMapController} is returned that throws
   * {@link UnsupportedOperationException} whenever it is used.
   *
   * App Inventor derivatives may opt to provide their own Maps implementation that uses a backend
   * other than OpenStreetMaps by providing another implementation here.
   *
   * @param form the form to use as a context.
   * @return a new {@link MapController} instance. the actual instance will be a function of the
   * device's build SDK.
   */
  public static MapController newMap(Form form) {
    if (Build.VERSION.SDK_INT < 8) {
      return new DummyMapController();
    } else {
      return new NativeOpenStreetMapController(form);
    }
  }
}
