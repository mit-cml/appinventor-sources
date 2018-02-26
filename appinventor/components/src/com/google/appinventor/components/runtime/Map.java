// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.LocationSensor.LocationSensorListener;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.GeoJSONUtil;
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.YailList;
import org.osmdroid.util.BoundingBox;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapController;
import com.google.appinventor.components.runtime.util.MapFactory.MapEventListener;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;

import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A two-dimensional container that renders map tiles in the background and allows for multiple
 * Marker elements to identify points on the map. Map tiles are supplied by OpenStreetMap
 * contributors and the the United States Geological Survey.</p>
 * <p>The Map component provides three utilities for manipulating its boundaries with App Inventor.
 * First, a locking mechanism is provided to allow the map to be moved relative to other components
 * on the Screen. Second, when unlocked, the user can pan the Map to any location. At this new
 * location, the &quot;Set Initial Boundary&quot; button can be pressed to save the current Map
 * coordinates to its properties. Lastly, if the Map is moved to a different location, for example
 * to add Markers off-screen, then the &quot;Reset Map to Initial Bounds&quot; button can be used
 * to re-center the Map at the starting location.</p>
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@SuppressWarnings("WeakerAccess")
@DesignerComponent(version = YaVersion.MAP_COMPONENT_VERSION,
  category = ComponentCategory.MAPS,
  androidMinSdk = 8,
  description = "<p>A two-dimensional container that renders map tiles in the background and " +
    "allows for multiple Marker elements to identify points on the map. Map tiles are supplied " +
    "by OpenStreetMap contributors and the United States Geological Survey.</p>" +
    "<p>The Map component provides three utilities for manipulating its boundaries within App " +
    "Inventor. First, a locking mechanism is provided to allow the map to be moved relative to " +
    "other components on the Screen. Second, when unlocked, the user can pan the Map to any " +
    "location. At this new location, the &quot;Set Initial Boundary&quot; button can be pressed " +
    "to save the current Map coordinates to its properties. Lastly, if the Map is moved to a " +
    "different location, for example to add Markers off-screen, then the &quot;Reset Map to " +
    "Initial Bounds&quot; button can be used to re-center the Map at the starting location.</p>")
@SimpleObject
@UsesAssets(fileNames = "location.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET, " + "android.permission.ACCESS_FINE_LOCATION, "
  + "android.permission.ACCESS_COARSE_LOCATION, " + "android.permission.ACCESS_WIFI_STATE, "
  + "android.permission.ACCESS_NETWORK_STATE, " + "android.permission.WRITE_EXTERNAL_STORAGE, "
  + "android.permission.READ_EXTERNAL_STORAGE")
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
    MapType(1);
    ShowCompass(false);
    LocationSensor(new LocationSensor(container.$form(), false));
    ShowUser(false);
    ShowZoom(false);
    EnableRotation(false);
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
   * <p>Set the initial center coordinate of the map. The value is specified as a
   * comma-separated pair of decimal latitude and longitude coordinates, for example,
   * <code>42.359144, -71.093612</code>.</p>
   * <p>In blocks code, it is recommended for performance reasons to use SetCenter with numerical
   * latitude and longitude rather than convert to the string representation for use with this
   * property.</p>
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
   * <p>The latitude of the center of the map.</p>
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
   * <p>The longitude of the center of the map.</p>
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
   * <p>Set the zoom level of the map.</p>
   * <p>Valid values of ZoomLevel are dependent on the tile provider and the latitude and
   * longitude of the map. For example, zoom levels are more constrained over oceans than dense
   * city centers to conserve space for storing tiles, so valid values may be 1-7 over ocean and
   * 1-18 over cities. Tile providers may send warning or error tiles if the zoom level is too
   * great for the server to support.</p>
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
   * <p>Get the zoom level of the map.</p>
   *
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
   * <p>Set whether the user can zoom the map using touch gestures. This value does not affect
   * whether the user can zoom using the zoom controls from {@link #ShowZoom}.</p>
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
   * <p>Get whether the user can zoom the map using touch gestures.</p>
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

  /**
   * <p>Set the type of map tile used for the base tile layer. Valid values are:</p>
   * <ol>
   * <li>Roads</li>
   * <li>Aerial</li>
   * <li>Terrain</li>
   * </ol>
   *
   * @param type Integer identifying the tile set to use for the map's base layer.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_MAP_TYPE,
      defaultValue = "1")
  @SimpleProperty
  public void MapType(int type) {
    MapFactory.MapType newType = MapFactory.MapType.values()[type];
    mapController.setMapType(newType);
  }

  /**
   * <p>Get the type of the base tile layer used by the map. Valid values are:</p>
   * <ol>
   * <li>Roads</li>
   * <li>Aerial</li>
   * <li>Terrain</li>
   * </ol>
   *
   * @return Returns an integer identifying the base tile layer in use by the map.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The type of tile layer to use as the base of the map. Valid values " +
          "are: 1 (Roads), 2 (Aerial), 3 (Terrain)")
  public int MapType() {
    return mapController.getMapType().ordinal();
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
   * Get whether the compass overlay is enabled on the map.
   *
   * @return True if the compass is enabled, otherwise false.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Show a compass icon rotated based on user orientation.")
  public boolean ShowCompass() {
    return mapController.isCompassEnabled();
  }

  /**
   * Show the zoom controls on the map.
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
   * @return True if the controls should be shown, otherwise false.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Show zoom buttons on the map.")
  public boolean ShowZoom() {
    return mapController.isZoomControlEnabled();
  }

  /**
   * Show the user's location on the map.
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
   * Get whether the map is rotating based on the user orientation.
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

  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Bounding box for the map stored as [[North, West], [South, East]].")
  public YailList BoundingBox() {
    BoundingBox bbox = mapController.getBoundingBox();
    YailList northwest = YailList.makeList(new Double[] { bbox.getLatNorth(), bbox.getLonWest() });
    YailList southeast = YailList.makeList(new Double[] { bbox.getLatSouth(), bbox.getLonEast() });
    return YailList.makeList(new YailList[] { northwest, southeast });
  }

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

  @SimpleFunction(description = "Pan the map center to the given latitude and longitude and " +
      "adjust the zoom level to the specified zoom.")
  public void PanTo(double latitude, double longitude, int zoom) {
    mapController.panTo(latitude, longitude, zoom, 1);
  }

  @SimpleFunction(description = "Create a new marker with default properties at the specified " +
      "latitude and longitude.")
  public Marker CreateMarker(double latitude, double longitude) {
    Marker marker = new Marker(this);
    marker.SetLocation(latitude, longitude);
    return marker;
  }

  @SimpleFunction(description = "Save the contents of the Map to the specified path.")
  public void Save(final String path) {
    final List<MapFeature> featuresToSave = new ArrayList<MapFeature>(features);
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        try {
          GeoJSONUtil.writeFeaturesAsGeoJSON(featuresToSave, path);
        } catch(final IOException e) {
          final Form form = $form();
          form.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              form.dispatchErrorOccurredEvent(Map.this, "Save",
                  ErrorMessages.ERROR_EXCEPTION_DURING_MAP_SAVE, e.getMessage());
            }
          });
        }
      }
    });
  }

  @SuppressWarnings({"WeakerAccess", "squid:S00100"})
  @SimpleEvent(description = "Map has been initialized and is ready for user interaction.")
  public void Ready() {
    EventDispatcher.dispatchEvent(this, "Ready");
  }

  @SimpleEvent(description = "User has changed the map bounds by panning or zooming the map.")
  public void BoundsChange() {
    EventDispatcher.dispatchEvent(this, "BoundsChange");
  }

  @SimpleEvent(description = "User has changed the zoom level of the map.")
  public void ZoomChange() {
    EventDispatcher.dispatchEvent(this, "ZoomChange");
  }

  @SimpleEvent(description = "An invalid coordinate was supplied during a maps operation. The " +
      "message parameter will have more details about the issue.")
  public void InvalidPoint(String message) {
    EventDispatcher.dispatchEvent(this, "InvalidPoint", message);
  }

  @SimpleEvent(description = "The user tapped at a point on the map.")
  public void TapAtPoint(double latitude, double longitude) {
    EventDispatcher.dispatchEvent(this, "TapAtPoint", latitude, longitude);
  }

  @SimpleEvent(description = "The user double-tapped at a point on the map. This event will be " +
      "followed by a ZoomChanged event if zooming gestures are enabled and the map is not at " +
      "the highest possible zoom level.")
  public void DoubleTapAtPoint(double latitude, double longitude) {
    EventDispatcher.dispatchEvent(this, "DoubleTapAtPoint", latitude, longitude);
  }

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
