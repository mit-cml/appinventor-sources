// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.util.Log;

import android.view.View;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.MapType;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.ScaleUnits;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.LocationSensor.LocationSensorListener;

import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.FileWriteOperation;
import com.google.appinventor.components.runtime.util.GeoJSONUtil;
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapController;
import com.google.appinventor.components.runtime.util.MapFactory.MapEventListener;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import com.google.appinventor.components.runtime.util.ScopedFile;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.IOException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.BoundingBox;

/**
 * A two-dimensional container that renders map tiles in the background and allows for multiple
 * {@link Marker} elements to identify points on the map. Map tiles are supplied by OpenStreetMap
 * contributors and the the United States Geological Survey, or a custom basemap URL can be provided.
 *
 * The `Map` component provides three utilities for manipulating its boundaries with App Inventor.
 * First, a locking mechanism is provided to allow the map to be moved relative to other components
 * on the Screen. Second, when unlocked, the user can pan the `Map` to any location. At this new
 * location, the &quot;Set Initial Boundary&quot; button can be pressed to save the current `Map`
 * coordinates to its properties. Lastly, if the `Map` is moved to a different location, for example
 * to add {@link Marker}s off-screen, then the &quot;Reset Map to Initial Bounds&quot; button can
 * be used to re-center the `Map` at the starting location.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@SuppressWarnings("WeakerAccess")
@DesignerComponent(version = YaVersion.MAP_COMPONENT_VERSION,
  category = ComponentCategory.MAPS,
  androidMinSdk = 8,
  description = "<p>A two-dimensional container that renders map tiles in the background and " +
    "allows for multiple Marker elements to identify points on the map. Map tiles are supplied " +
    "by OpenStreetMap contributors and the United States Geological Survey, or a custom basemap URL can be provided.</p>" +
    "<p>The Map component provides three utilities for manipulating its boundaries within App " +
    "Inventor. First, a locking mechanism is provided to allow the map to be moved relative to " +
    "other components on the Screen. Second, when unlocked, the user can pan the Map to any " +
    "location. At this new location, the &quot;Set Initial Boundary&quot; button can be pressed " +
    "to save the current Map coordinates to its properties. Lastly, if the Map is moved to a " +
    "different location, for example to add Markers off-screen, then the &quot;Reset Map to " +
    "Initial Bounds&quot; button can be used to re-center the Map at the starting location.</p>",
    iconName = "images/map.png")
@SimpleObject
@UsesAssets(fileNames = "location.png, marker.svg")
@UsesPermissions({ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
@UsesLibraries(libraries = "osmdroid.aar, osmdroid.jar, androidsvg.jar, jts.jar")
public class Map extends MapFeatureContainerBase implements MapEventListener {
  private static final String TAG = Map.class.getSimpleName();

  private static final String ERROR_INVALID_NUMBER = "%s is not a valid number.";
  private static final String ERROR_LATITUDE_OUT_OF_BOUNDS = "Latitude %f is out of bounds.";
  private static final String ERROR_LONGITUDE_OUT_OF_BOUNDS = "Longitude %f is out of bounds.";

  /**
   * <p>Platform-specific map controller returned by {@link MapFactory}.</p>
   */
  private MapController mapController = null;

  private LocationSensor sensor = null;

  /**
   * Construct a new map to be rendered within the given container.
   *
   * @param container Component or form containing the map view
   */
  public Map(final ComponentContainer container) {
    super(container);
    Log.d(TAG, "Map.<init>");
    container.$add(this);
    Width(ComponentConstants.MAP_PREFERRED_WIDTH);
    Height(ComponentConstants.MAP_PREFERRED_HEIGHT);
    CenterFromString("42.359144, -71.093612");
    ZoomLevel(13);
    EnableZoom(true);
    EnablePan(true);
    MapTypeAbstract(MapType.Road);
    CustomUrl("https://tile.openstreetmap.org/{z}/{x}/{y}.png");
    ShowCompass(false);
    LocationSensor(new LocationSensor(container.$form(), false));
    ShowUser(false);
    ShowZoom(false);
    EnableRotation(false);
    ShowScale(false);
  }

  @Override
  public View getView() {
    if (mapController == null) {
      mapController = MapFactory.newMap(container.$form());
      mapController.addEventListener(this);
    }
    return mapController.getView();
  }

  /**
   * Set the initial center coordinate of the map. The value is specified as a
   * comma-separated pair of decimal latitude and longitude coordinates, for example,
   * `42.359144, -71.093612`.
   *
   *   In blocks code, it is recommended for performance reasons to use
   * {@link #PanTo(double, double, int)} with numerical latitude and longitude rather than convert
   * to the string representation for use with this property.
   *
   * @param center A comma-separated string containing the latitude and longitude of the map center.
   */
  @SuppressWarnings("squid:S00100")
  @DesignerProperty(defaultValue = "42.359144, -71.093612",
      editorType = PropertyTypeConstants.PROPERTY_TYPE_GEOGRAPHIC_POINT)
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "<p>Set the initial center coordinate of the map. The value is specified as " +
          "a comma-separated pair of decimal latitude and longitude coordinates, for example, " +
          "<code>42.359144, -71.093612</code>.</p><p>In blocks code, it is recommended for " +
          "performance reasons to use SetCenter with numerical latitude and longitude rather " +
          "than convert to the string representation for use with this property.</p>")
  public void CenterFromString(String center) {
    String[] parts = center.split(",");
    if (parts.length != 2) {
      Log.e(TAG, center + " is not a valid point.");
      InvalidPoint(center + " is not a valid point.");
      return;
    }
    double latitude;
    double longitude;
    try {
      latitude = Double.parseDouble(parts[0].trim());
    } catch (NumberFormatException e) {
      Log.e(TAG, String.format(ERROR_INVALID_NUMBER, parts[0]));
      InvalidPoint(String.format(ERROR_INVALID_NUMBER, parts[0]));
      return;
    }
    try {
      longitude = Double.parseDouble(parts[1].trim());
    } catch (NumberFormatException e) {
      Log.e(TAG, String.format(ERROR_INVALID_NUMBER, parts[1]));
      InvalidPoint(String.format(ERROR_INVALID_NUMBER, parts[1]));
      return;
    }
    if (latitude > 90.0 || latitude < -90.0) {
      InvalidPoint(String.format(ERROR_LATITUDE_OUT_OF_BOUNDS, latitude));
    } else if (longitude > 180.0 || longitude < -180.0) {
      InvalidPoint(String.format(ERROR_LONGITUDE_OUT_OF_BOUNDS, longitude));
    } else {
      Log.i(TAG, "Setting center to " + latitude + ", " + longitude);
      mapController.setCenter(latitude, longitude);
    }
  }

  /**
   * Gets the latitude of the center of the Map. To change the latitude, use the
   * {@link #PanTo(double, double, int)} method.
   *
   * @return Returns the latitude of the center of the map
   */
  @SuppressWarnings("squid:S00100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The latitude of the center of the map.")
  public double Latitude() {
    return mapController.getLatitude();
  }

  /**
   * Gets the longitude of the center of the Map. To change the longitude, use the
   * {@link #PanTo(double, double, int)} method.
   *
   * @return Returns the longitude of the center of the map
   */
  @SuppressWarnings("squid:S00100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The longitude of the center of the map.")
  public double Longitude() {
    return mapController.getLongitude();
  }

  /**
   * Specifies the zoom level of the map.
   * Valid values of ZoomLevel are dependent on the tile provider and the latitude and
   * longitude of the map. For example, zoom levels are more constrained over oceans than dense
   * city centers to conserve space for storing tiles, so valid values may be 1-7 over ocean and
   * 1-20 over cities. Tile providers may send warning or error tiles if the zoom level is too
   * great for the server to support.
   *
   * @param zoom New zoom level.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_MAP_ZOOM,
      defaultValue = "13")
  @SimpleProperty
  public void ZoomLevel(int zoom) {
    mapController.setZoom(zoom);
  }

  /**
   * Get the zoom level of the map.
   *
   * @suppressdoc
   * @return Returns the current zoom level of the map.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The zoom level of the map. Valid values of ZoomLevel are " +
          "dependent on the tile provider and the latitude and longitude of the map. For " +
          "example, zoom levels are more constrained over oceans than dense city centers to " +
          "conserve space for storing tiles, so valid values may be 1-7 over ocean and 1-18 " +
          "over cities. Tile providers may send warning or error tiles if the zoom level is too " +
          "great for the server to support.")
  public int ZoomLevel() {
    return mapController.getZoom();
  }

  /**
   * Set whether the user can zoom the map using touch gestures. This value does not affect
   * whether the user can zoom using the zoom controls provided by
   * <a href="#Map.ShowZoom">ShowZoom</a>.
   *
   * @param zoom If true, then the user can use pinch/expand gestures to change the map zoom level.
   */
  @SuppressWarnings("WeakerAccess")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty
  public void EnableZoom(boolean zoom) {
    mapController.setZoomEnabled(zoom);
  }

  /**
   * Enables or disables the two-finger pinch gesture to zoom the Map.
   *
   * @return Returns whether multitouch zoom gestures are enabled for the map.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "If this property is set to true, multitouch zoom gestures are allowed on " +
        "the map. Otherwise, the map zoom cannot be changed by the user except via the zoom " +
        "control buttons.")
  public boolean EnableZoom() {
    return mapController.isZoomEnabled();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.0")
  @SimpleProperty
  public void Rotation(float rotation) {
    mapController.setRotation(rotation);
  }

  /**
   * Specifies the rotation of the map in decimal degrees, if any.
   */
  @SimpleProperty (category = PropertyCategory.APPEARANCE, description = "Sets or gets the rotation of the map in decimal degrees if any")
  public float Rotation() {
    return mapController.getRotation();
  }

  /**
   * Set the type of map tile used for the base tile layer.
   * Valid values are:
   *
   *   1. Roads
   *   2. Aerial
   *   3. Terrain
   *   4. Custom
   *
   * @param type Integer identifying the tile set to use for the map's base layer.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_MAP_TYPE,
      defaultValue = "1")
  @SimpleProperty
  public void MapType(@Options(MapType.class) int type) {
    MapType mapType = MapType.fromUnderlyingValue(type);
    if (mapType != null) {
      MapTypeAbstract(mapType);
    }
  }

  /**
   * Sets or gets the tile layer used to draw the Map background. Defaults to Roads. Valid values
   * are:
   *
   *   1. Roads
   *   2. Aerial
   *   3. Terrain
   *   4. Custom
   *
   *   **Note:** Road layers are provided by OpenStreetMap and aerial and terrain layers are
   * provided by the U.S. Geological Survey.
   *
   * @return Returns an integer identifying the base tile layer in use by the map.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The type of tile layer to use as the base of the map. Valid values " +
          "are: 1 (Roads), 2 (Aerial), 3 (Terrain), 4 (Custom)")
  public @Options(MapType.class) int MapType() {
    return MapTypeAbstract().toUnderlyingValue();
  }

  /**
   * Sets the tile layer used to draw the Map background.
   */
  @SuppressWarnings("RegularMethodName")
  public MapType MapTypeAbstract() {
    return mapController.getMapTypeAbstract();
  }

  /**
   * Returns the current tile layer used to draw the Map background.
   */
  @SuppressWarnings("RegularMethodName")
  public void MapTypeAbstract(MapType type) {
    mapController.setMapTypeAbstract(type);
  }

  /**
   * @return Returns the custom URL of the base tile layer in use by the map.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_MAP_CUSTOMURL,
      defaultValue = "https://tile.openstreetmap.org/{z}/{x}/{y}.png")
  @SimpleProperty(category = PropertyCategory.ADVANCED,
      description = "The URL of the custom tile layer to use as the base of the map. Valid URLs " +
          "should include &#123;z}, &#123;x} and &#123;y} placeholders and any authentication required. </p></p>" + 
          "e.g. https://tile.openstreetmap.org/&#123;z}/&#123;x}/&#123;y}.png </p>" +
          "or https://example.com/geoserver/gwc/service/tms/1.0.0/workspace:layername&#64;EPSG:3857&#64;jpeg/&#123;z}/&#123;x}/&#123;y}.jpeg&#63;flipY=true&authkey=123")
  public String CustomUrl() {
    return mapController.getCustomUrl();
  }

  /**
   * Update the custom URL of the base tile layer in use by the map.
   * e.g. https://tile.openstreetmap.org/{z}/{x}/{y}.png
   * e.g. https://example.com/geoserver/gwc/service/tms/1.0.0/workspace:layername@EPSG:3857@jpeg/{z}/{x}/{y}.jpeg?flipY=true&authkey=123
   */
  @SimpleProperty(category = PropertyCategory.ADVANCED)
  public void CustomUrl(String url) {
    mapController.setCustomUrl(url);
  }

  /**
   * Show a compass on the map. If the device provides a digital compass, orientation changes will
   * be used to rotate the compass icon.
   *
   * @param compass True if the compass should be enabled, otherwise false.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void ShowCompass(boolean compass) {
    mapController.setCompassEnabled(compass);
  }

  /**
   * Specifies whether to a compass overlay on the Map. The compass will be rotated based on the
   * device's orientation if a digital compass is present in hardware.
   *
   * @return True if the compass is enabled, otherwise false.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Show a compass icon rotated based on user orientation.")
  public boolean ShowCompass() {
    return mapController.isCompassEnabled();
  }

  /**
   * Specifies whether to show zoom controls or not.
   *
   * @param zoom True if the controls should be shown, otherwise false.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void ShowZoom(boolean zoom) {
    mapController.setZoomControlEnabled(zoom);
  }

  /**
   * Get whether the zoom controls are displayed on the map.
   *
   * @suppressdoc
   * @return True if the controls should be shown, otherwise false.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Show zoom buttons on the map.")
  public boolean ShowZoom() {
    return mapController.isZoomControlEnabled();
  }

  /**
   * Shows or hides an icon indicating the user's current location on the {@link Map}. The
   * availability and accuracy of this feature will depend on whether the user has location
   * services enabled and which location providers are available.
   *
   * @param user True if the user's location should be shown, otherwise false.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void ShowUser(boolean user) {
    mapController.setShowUserEnabled(user);
  }

  /**
   * Get whether the user's location is shown on the map.
   *
   * @suppressdoc
   * @return True if the user's location is being shown on the map, otherwise false.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Show the user's location on the map.")
  public boolean ShowUser() {
    return mapController.isShowUserEnabled();
  }

  /**
   * Enable rotating the map based on the user's orientation.
   *
   * @param rotation True if the map should be rotated based on user orientation, otherwise false.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void EnableRotation(boolean rotation) {
    mapController.setRotationEnabled(rotation);
  }

  /**
   * Enables or disables the two-finger rotation gesture to rotate the Map.
   *
   * @return True if the map is drawn based on orientation, otherwise false.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "If set to true, the user can use multitouch gestures to rotate the map " +
          "around its current center.")
  public boolean EnableRotation() {
    return mapController.isRotationEnabled();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty
  public void EnablePan(boolean pan) {
    mapController.setPanEnabled(pan);
  }

  /**
   * Enables or disables the ability of the user to move the Map.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Enable two-finger panning of the Map")
  public boolean EnablePan() {
    return mapController.isPanEnabled();
  }

  @SimpleProperty
  public void BoundingBox(YailList boundingbox) {
    double latNorth = GeometryUtil.coerceToDouble(((YailList) boundingbox.get(1)).get(1));
    double longWest = GeometryUtil.coerceToDouble(((YailList)boundingbox.get(1)).get(2));
    double latSouth = GeometryUtil.coerceToDouble(((YailList)boundingbox.get(2)).get(1));
    double longEast = GeometryUtil.coerceToDouble(((YailList)boundingbox.get(2)).get(2));
    mapController.setBoundingBox(new BoundingBox(latNorth, longEast, latSouth, longWest));
  }

  /**
   * Sets or gets the current boundary for the map's drawn view. The value is a list of lists
   * containing the northwest and southeast coordinates of the current view in the form
   * ``((North West) (South East))``.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Bounding box for the map stored as [[North, West], [South, East]].")
  public YailList BoundingBox() {
    BoundingBox bbox = mapController.getBoundingBox();
    YailList northwest = YailList.makeList(new Double[] { bbox.getLatNorth(), bbox.getLonWest() });
    YailList southeast = YailList.makeList(new Double[] { bbox.getLatSouth(), bbox.getLonEast() });
    return YailList.makeList(new YailList[] { northwest, southeast });
  }

  @Override
  @SimpleProperty
  public YailList Features() {
    return super.Features();
  }

  /**
   * Uses the provided [`LocationSensor`](sensors.html#LocationSensor) for user location data
   * rather than the built-in location provider.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Uses the provided LocationSensor for user location data rather than the " +
          "built-in location provider.")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT + ":com.google.appinventor.components.runtime.LocationSensor")
  public void LocationSensor(LocationSensor sensor) {
    LocationSensorListener listener = mapController.getLocationListener();
    if (this.sensor != null) {
      this.sensor.removeListener(listener);
    }
    this.sensor = sensor;
    if (this.sensor != null) {
      this.sensor.addListener(listener);
    }
  }

  public LocationSensor LocationSensor() {
    return sensor;
  }

  /**
   * Shows or hides a scale overlay on the {@link Map}. The scale will change with the zoom level
   * and its units can be controlled by the {@link #ScaleUnits(int)} property.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty
  public void ShowScale(boolean show) {
    mapController.setScaleVisible(show);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Shows a scale reference on the map.")
  public boolean ShowScale() {
    return mapController.isScaleVisible();
  }

  /**
   * Specifies the units used for the scale overlay. 1 (the default) will give metric units
   * (km, m) whereas 2 will give imperial units (mi, ft).
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_MAP_UNIT_SYSTEM,
      defaultValue = "1")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ScaleUnits(@Options(ScaleUnits.class) int units) {
    // Make sure units is a valid ScaleUnits.
    ScaleUnits scaleUnits = ScaleUnits.fromUnderlyingValue(units);
    if (scaleUnits == null) {
      $form().dispatchErrorOccurredEvent(this, "ScaleUnits",
          ErrorMessages.ERROR_INVALID_UNIT_SYSTEM, units);
      return;
    }
    ScaleUnitsAbstract(scaleUnits);
  }

  @SimpleProperty
  public @Options(ScaleUnits.class) int ScaleUnits() {
    return ScaleUnitsAbstract().toUnderlyingValue();
  }

  /**
   * Returns the system of measurement used by the map.
   */
  @SuppressWarnings("RegularMethodName")
  public ScaleUnits ScaleUnitsAbstract() {
    return mapController.getScaleUnitsAbstract();
  }

  /**
   * Sets the system of measurement used by the map.
   */
  @SuppressWarnings("RegularMethodName")
  public void ScaleUnitsAbstract(ScaleUnits units) {
    mapController.setScaleUnitsAbstract(units);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns the user's latitude if ShowUser is enabled.")
  public double UserLatitude() {
    return sensor == null ? -999 : sensor.Latitude();
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns the user's longitude if ShowUser is enabled.")
  public double UserLongitude() {
    return sensor == null ? -999 : sensor.Longitude();
  }

  @SimpleFunction(description = "Pans the map center to the given latitude and longitude and " +
      "adjust the zoom level to the specified zoom.")
  public void PanTo(double latitude, double longitude, int zoom) {
    mapController.panTo(latitude, longitude, zoom, 1);
  }

  /**
   * Creates a new {@link Marker} on the `Map` at the specified `latitude` and `longitude`.
   *
   * @param latitude Latitude of the new Marker
   * @param longitude Longitude of the new Marker
   * @return a freshly created Marker instance
   */
  @SimpleFunction(description = "Create a new marker with default properties at the specified " +
      "latitude and longitude.")
  public Marker CreateMarker(double latitude, double longitude) {
    Marker marker = new Marker(this);
    marker.SetLocation(latitude, longitude);
    return marker;
  }

  /**
   * Saves the features on the `Map` as a GeoJSON file at the specified path.
   */
  @SimpleFunction(description = "Save the contents of the Map to the specified path.")
  public void Save(final String path) {
    final List<MapFeature> featuresToSave = new ArrayList<MapFeature>(features);
    // Needed for backward compatibility prior to nb188
    if (path.startsWith("/") || path.startsWith("file:/")) {
      final java.io.File target = path.startsWith("file:") ? new java.io.File(URI.create(path))
          : new java.io.File(path);
      AsynchUtil.runAsynchronously(new Runnable() {
        @Override
        public void run() {
          doSave(featuresToSave, target);
        }
      });
    } else {
      new FileWriteOperation($form(), this, "Save", path, $form().DefaultFileScope(), false, true) {
        @Override
        protected void processFile(ScopedFile file) {
          String uri = FileUtil.resolveFileName(form, file);
          java.io.File target = new java.io.File(URI.create(uri));
          doSave(featuresToSave, target);
        }
      }.run();
    }
  }

  private void doSave(List<MapFeature> featuresToSave, java.io.File target) {
    try {
      GeoJSONUtil.writeFeaturesAsGeoJSON(featuresToSave, target.getAbsolutePath());
    } catch (final IOException e) {
      $form().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          $form().dispatchErrorOccurredEvent(Map.this, "Save",
              ErrorMessages.ERROR_EXCEPTION_DURING_MAP_SAVE, e.getMessage());
        }
      });
    }
  }

  /**
   * The `Ready` event runs once the `Map` has been initialized and is ready for user interaction.
   */
  @SuppressWarnings({"WeakerAccess", "squid:S00100"})
  @SimpleEvent(description = "Map has been initialized and is ready for user interaction.")
  public void Ready() {
    EventDispatcher.dispatchEvent(this, "Ready");
  }

  /**
   * The `BoundsChange` event runs when the user changes the map bounds, either by zooming, panning,
   * or rotating the view.
   */
  @SimpleEvent(description = "User has changed the map bounds by panning or zooming the map.")
  public void BoundsChange() {
    EventDispatcher.dispatchEvent(this, "BoundsChange");
  }

  /**
   * The `ZoomChange` event runs when the user has changed the zoom level of the map, such as by
   * pinching or double-tapping to zoom.
   */
  @SimpleEvent(description = "User has changed the zoom level of the map.")
  public void ZoomChange() {
    EventDispatcher.dispatchEvent(this, "ZoomChange");
  }

  /**
   * The `InvalidPoint` event runs when the program encounters an invalid point while processing
   * geographical data. Points are considered invalid when the latitude or longitude for the point
   * is outside the acceptable range (`[-90, 90]` and `[-180, 180]`, respectively). The `message`
   * parameter will contain an explanation for the error.
   *
   * @param message
   */
  @SimpleEvent(description = "An invalid coordinate was supplied during a maps operation. The " +
      "message parameter will have more details about the issue.")
  public void InvalidPoint(String message) {
    EventDispatcher.dispatchEvent(this, "InvalidPoint", message);
  }

  /**
   * The `TapAtPoint` runs when the user taps at a point on the map. The tapped location will be
   * reported in map coordinates via the `latitude`{:.variable.block} and
   * `longitude`{:.variable.block} parameters.
   */
  @SimpleEvent(description = "The user tapped at a point on the map.")
  public void TapAtPoint(double latitude, double longitude) {
    EventDispatcher.dispatchEvent(this, "TapAtPoint", latitude, longitude);
  }

  /**
   * The `DoubleTapAtPoint` runs when the user double taps at a point on the map. The tapped
   * location will be reported in map coordinates via the `latitude`{:.variable.block} and
   * `longitude`{:.variable.block} parameters.
   */
  @SimpleEvent(description = "The user double-tapped at a point on the map. This event will be " +
      "followed by a ZoomChanged event if zooming gestures are enabled and the map is not at " +
      "the highest possible zoom level.")
  public void DoubleTapAtPoint(double latitude, double longitude) {
    EventDispatcher.dispatchEvent(this, "DoubleTapAtPoint", latitude, longitude);
  }

  /**
   * The `LongPressAtPoint` runs when the user long-presses at a point on the map without moving
   * their finger (which would trigger a drag). The location of the long-press will be reported in
   * map coordinates via the `latitude`{:.variable.block} and `longitude`{:.variable.block}
   * parameters.
   */
  @SimpleEvent(description = "The user long-pressed at a point on the map.")
  public void LongPressAtPoint(double latitude, double longitude) {
    EventDispatcher.dispatchEvent(this, "LongPressAtPoint", latitude, longitude);
  }

  public MapController getController() {
    return mapController;
  }

  // MapEventListener implementation
  @Override
  public void onReady(MapController map) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Map.this.Ready();
      }
    });
  }

  @Override
  public void onBoundsChanged() {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Map.this.BoundsChange();
      }
    });
  }

  @Override
  public void onZoom() {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Map.this.ZoomChange();
      }
    });
  }

  @Override
  public void onSingleTap(final double latitude, final double longitude) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Map.this.TapAtPoint(latitude, longitude);
      }
    });
  }

  @Override
  public void onDoubleTap(final double latitude, final double longitude) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Map.this.DoubleTapAtPoint(latitude, longitude);
      }
    });
  }

  @Override
  public void onLongPress(final double latitude, final double longitude) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Map.this.LongPressAtPoint(latitude, longitude);
      }
    });
  }

  @Override
  public void onFeatureClick(final MapFeature feature) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        feature.Click();
      }
    });
  }

  @Override
  public void onFeatureLongPress(final MapFeature feature) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        feature.LongClick();
      }
    });
  }

  @Override
  public void onFeatureStartDrag(final MapFeature feature) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        feature.StartDrag();
      }
    });
  }

  @Override
  public void onFeatureDrag(final MapFeature feature) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        feature.Drag();
      }
    });
  }

  @Override
  public void onFeatureStopDrag(final MapFeature feature) {
    container.$form().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        feature.StopDrag();
      }
    });
  }

  @Override
  public Map getMap() {
    return this;
  }


  // MapFeatureContainerBase optimizations
  @Override
  void addFeature(MapMarker marker) {
    features.add(marker);
    marker.setMap(this);
    mapController.addFeature(marker);
  }

  @Override
  void addFeature(MapLineString lineString) {
    features.add(lineString);
    lineString.setMap(this);
    mapController.addFeature(lineString);
  }

  @Override
  void addFeature(MapPolygon polygon) {
    features.add(polygon);
    polygon.setMap(this);
    mapController.addFeature(polygon);
  }

  @Override
  void addFeature(MapRectangle rectangle) {
    features.add(rectangle);
    rectangle.setMap(this);
    mapController.addFeature(rectangle);
  }

  @Override
  void addFeature(MapCircle circle) {
    features.add(circle);
    circle.setMap(this);
    mapController.addFeature(circle);
  }

  @Override
  public void removeFeature(MapFeature feature) {
    features.remove(feature);
    mapController.removeFeature(feature);
  }
}
