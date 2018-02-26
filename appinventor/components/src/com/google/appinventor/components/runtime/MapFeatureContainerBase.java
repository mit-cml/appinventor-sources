// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.util.Log;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.YailList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.appinventor.components.runtime.util.GeoJSONUtil.processGeoJSONFeature;

@SimpleObject
public abstract class MapFeatureContainerBase extends AndroidViewComponent implements MapFactory.MapFeatureContainer {
  private static final String TAG = MapFeatureContainerBase.class.getSimpleName();

  private static final int ERROR_CODE_MALFORMED_URL = -1;
  private static final int ERROR_CODE_IO_EXCEPTION = -2;
  private static final int ERROR_CODE_MALFORMED_GEOJSON = -3;
  private static final int ERROR_CODE_UNKNOWN_TYPE = -4;
  private static final String ERROR_MALFORMED_URL = "The URL is malformed";
  private static final String ERROR_IO_EXCEPTION = "Unable to download content from URL";
  private static final String ERROR_MALFORMED_GEOJSON = "Malformed GeoJSON response. Expected FeatureCollection as root element.";
  private static final String ERROR_UNKNOWN_TYPE = "Unrecognized/invalid type in JSON object";
  private static final String GEOJSON_TYPE = "type";
  private static final String GEOJSON_FEATURECOLLECTION = "FeatureCollection";
  private static final String GEOJSON_GEOMETRYCOLLECTION = "GeometryCollection";
  private static final String GEOJSON_FEATURES = "features";

  /**
   * <p>List of {@link MapFactory.MapFeature features} associated with this map, including those that are
   * invisible.</p>
   */
  protected List<MapFeature> features = new CopyOnWriteArrayList<MapFeature>();

  private final MapFactory.MapFeatureVisitor<Void> featureAdder = new MapFactory.MapFeatureVisitor<Void>() {
    @Override
    public Void visit(MapFactory.MapMarker marker, Object... arguments) {
      addFeature(marker);
      return null;
    }

    @Override
    public Void visit(MapFactory.MapLineString lineString, Object... arguments) {
      addFeature(lineString);
      return null;
    }

    @Override
    public Void visit(MapFactory.MapPolygon polygon, Object... arguments) {
      addFeature(polygon);
      return null;
    }

    @Override
    public Void visit(MapFactory.MapCircle circle, Object... arguments) {
      addFeature(circle);
      return null;
    }

    @Override
    public Void visit(MapFactory.MapRectangle rectangle, Object... arguments) {
      addFeature(rectangle);
      return null;
    }
  };

  @SuppressWarnings("WeakerAccess")
  protected MapFeatureContainerBase(ComponentContainer container) {
    super(container);
  }

  /**
   *
   *
   * @param features A YailList of {#Marker Markers}
   */
  @SimpleProperty
  public void Features(YailList features) {
    for (MapFactory.MapFeature feature : this.features) {
      feature.removeFromMap();
    }
    this.features.clear();
    ListIterator<?> it = features.listIterator(1);
    while (it.hasNext()) {
      Object o = it.next();
      if (o instanceof MapFactory.MapFeature) {
        this.addFeature((MapFactory.MapFeature) o);
      }
    }
    getMap().getView().invalidate();
  }

  /**
   * Get the list of features attached to this map. This list includes features with Visible set to false.
   *
   * @return A YailList of map features, e.g., Marker, LineString
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The list of features placed on this map. This list also includes any " +
          "features created by calls to FeatureFromDescription")
  public YailList Features() {
    return YailList.makeList(features);
  }

  @SimpleEvent(description = "The user clicked on a map feature.")
  public void FeatureClick(MapFactory.MapFeature feature) {
    EventDispatcher.dispatchEvent(this, "FeatureClick", feature);
    if (getMap() != this) {
      getMap().FeatureClick(feature);
    }
  }

  @SimpleEvent(description = "The user long-pressed on a map feature.")
  public void FeatureLongClick(MapFactory.MapFeature feature) {
    EventDispatcher.dispatchEvent(this, "FeatureLongClick", feature);
    if (getMap() != this) {
      getMap().FeatureLongClick(feature);
    }
  }

  @SimpleEvent(description = "The user started dragging a map feature.")
  public void FeatureStartDrag(MapFactory.MapFeature feature) {
    EventDispatcher.dispatchEvent(this, "FeatureStartDrag", feature);
    if (getMap() != this) {
      getMap().FeatureStartDrag(feature);
    }
  }

  @SimpleEvent(description = "The user dragged a map feature.")
  public void FeatureDrag(MapFactory.MapFeature feature) {
    EventDispatcher.dispatchEvent(this, "FeatureDrag", feature);
    if (getMap() != this) {
      getMap().FeatureDrag(feature);
    }
  }

  @SimpleEvent(description = "The user stopped dragging a map feature.")
  public void FeatureStopDrag(MapFactory.MapFeature feature) {
    EventDispatcher.dispatchEvent(this, "FeatureStopDrag", feature);
    if (getMap() != this) {
      getMap().FeatureStopDrag(feature);
    }
  }

  /**
   * Load a feature collection in GeoJSON
   * format from the given url. On success, the event GotFeatures will be raised with the given url
   * and a list of features parsed from the GeoJSON as a list of (key, value) pairs. On failure,
   * the LoadError event will be raised with any applicable HTTP response code and error
   * message.
   *
   * @param url The URL from which to read a GeoJSON-encoded feature collection
   */
  @SimpleFunction(description = "<p>Load a feature collection in " +
      "<a href=\"https://en.wikipedia.org/wiki/GeoJSON\">GeoJSON</a> format from the given " +
      "url. On success, the event GotFeatures will be raised with the given url and a list of " +
      "the features parsed from the GeoJSON as a list of (key, value) pairs. On failure, the " +
      "LoadError event will be raised with any applicable HTTP response code and error " +
      "message.</p>")
  public void LoadFromURL(final String url) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        performGet(url);
      }
    });
  }

  /**
   * Convert a feature description into an App Inventor map feature. Currently the only
   * supported conversion is from a GeoJSON point to Marker component. If the feature has
   * properties, they will be mapped into App Inventor properties using the following mapping:
   *
   * description becomes Description;
   * draggable becomes Draggable;
   * infobox becomes EnableInfobox;
   * fill becomes FillColor;
   * image becomes ImageAsset;
   * stroke becomes StrokeColor;
   * stroke-width becomes StrokeWidth;
   * title becomes Title;
   * visible becomes Visible
   *
   * @param description The description of a map feature, as a list of key-value pairs.
   * @return A new component representing the feature, or a string indicating an error.
   */
  @SimpleFunction
  public Object FeatureFromDescription(YailList description) {
    try {
      return processGeoJSONFeature(TAG, this, description);
    } catch(IllegalArgumentException e) {
      $form().dispatchErrorOccurredEvent(this, "FeatureFromDescription",
          ERROR_CODE_MALFORMED_GEOJSON, e.getMessage());
      return e.getMessage();
    }
  }

  @SimpleEvent(description = "A GeoJSON document was successfully read from url. The features " +
      "specified in the document are provided as a list in features.")
  public void GotFeatures(String url, YailList features) {
    EventDispatcher.dispatchEvent(this, "GotFeatures", url, features);
  }

  @SimpleEvent(description = "An error was encountered while processing a GeoJSON document at " +
      "the given url. The responseCode parameter will contain an HTTP status code and the " +
      "errorMessage parameter will contain a detailed error message.")
  public void LoadError(String url, int responseCode, String errorMessage) {
    EventDispatcher.dispatchEvent(this, "LoadError", url, responseCode, errorMessage);
  }

  @Override
  public Activity $context() {
    return container.$context();
  }

  @Override
  public Form $form() {
    return container.$form();
  }

  @Override
  public void $add(AndroidViewComponent component) {
    throw new UnsupportedOperationException("Map.$add() called");
  }

  @Override
  public void setChildWidth(AndroidViewComponent component, int width) {
    throw new UnsupportedOperationException("Map.setChildWidth called");
  }

  @Override
  public void setChildHeight(AndroidViewComponent component, int height) {
    throw new UnsupportedOperationException("Map.setChildHeight called");
  }

  public void removeFeature(MapFactory.MapFeature feature) {
    features.remove(feature);
    getMap().removeFeature(feature);
  }

  void addFeature(MapFactory.MapMarker marker) {
    features.add(marker);
    getMap().addFeature(marker);
  }

  void addFeature(MapFactory.MapLineString polyline) {
    features.add(polyline);
    getMap().addFeature(polyline);
  }

  void addFeature(MapFactory.MapPolygon polygon) {
    features.add(polygon);
    getMap().addFeature(polygon);
  }

  void addFeature(MapFactory.MapCircle circle) {
    features.add(circle);
    getMap().addFeature(circle);
  }

  void addFeature(MapFactory.MapRectangle rectangle) {
    features.add(rectangle);
    getMap().addFeature(rectangle);
  }

  @Override
  public void addFeature(MapFactory.MapFeature feature) {
    feature.accept(featureAdder);
  }

  private void performGet(final String url) {
    try {
      String jsonContent = loadUrl(url);
      if (jsonContent == null) {
        return;
      }
      processGeoJSON(url, jsonContent);
    } catch(Exception e) {
      Log.e(TAG, "Exception retreiving GeoJSON", e);
      $form().dispatchErrorOccurredEvent(this, "LoadFromURL", ERROR_CODE_UNKNOWN_TYPE,
          e.toString());
    }
  }

  private String loadUrl(final String url) {
    try {
      URLConnection connection = new URL(url).openConnection();
      connection.connect();
      if (connection instanceof HttpURLConnection) {
        HttpURLConnection conn = (HttpURLConnection) connection;
        final int responseCode = conn.getResponseCode();
        final String responseMessage = conn.getResponseMessage();
        if (responseCode != 200) {
          $form().runOnUiThread(new Runnable() {
            public void run() {
              MapFeatureContainerBase.this.LoadError(url, responseCode, responseMessage);
            }
          });
          conn.disconnect();
          return null;
        }
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
            "UTF-8"));
      StringBuilder content = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line);
        content.append("\n");
      }
      reader.close();
      return content.toString();
    } catch(MalformedURLException e) {
      $form().runOnUiThread(new Runnable() {
        public void run() {
          MapFeatureContainerBase.this.LoadError(url, ERROR_CODE_MALFORMED_URL,
              ERROR_MALFORMED_URL);
        }
      });
    } catch (IOException e) {
      $form().runOnUiThread(new Runnable() {
        public void run() {
          MapFeatureContainerBase.this.LoadError(url, ERROR_CODE_IO_EXCEPTION,
              ERROR_IO_EXCEPTION);
        }
      });
    }
    return null;
  }

  @SuppressWarnings("WeakerAccess")
  protected void processGeoJSON(final String url, final String content) throws JSONException {
    JSONObject parsedData = new JSONObject(content);
    String type = parsedData.optString(GEOJSON_TYPE);
    if (!GEOJSON_FEATURECOLLECTION.equals(type) && !GEOJSON_GEOMETRYCOLLECTION.equals(type)) {
      $form().runOnUiThread(new Runnable() {
        public void run() {
          MapFeatureContainerBase.this.LoadError(url, ERROR_CODE_MALFORMED_GEOJSON,
              ERROR_MALFORMED_GEOJSON);
        }
      });
      return;
    }
    JSONArray features = parsedData.getJSONArray(GEOJSON_FEATURES);
    final List<YailList> yailFeatures = new ArrayList<YailList>();
    for (int i = 0; i < features.length(); i++) {
      yailFeatures.add(jsonObjectToYail(features.getJSONObject(i)));
    }
    $form().runOnUiThread(new Runnable() {
      public void run() {
        MapFeatureContainerBase.this.GotFeatures(url, YailList.makeList(yailFeatures));
      }
    });
  }

  private YailList jsonObjectToYail(JSONObject object) throws JSONException {
    List<YailList> pairs = new ArrayList<YailList>();
    @SuppressWarnings("unchecked")  // json only allows String keys
        Iterator<String> j = object.keys();
    while (j.hasNext()) {
      String key = j.next();
      Object value = object.get(key);
      if (value instanceof Boolean ||
          value instanceof Integer ||
          value instanceof Long ||
          value instanceof Double ||
          value instanceof String) {
        pairs.add(YailList.makeList(new Object[] { key, value }));
      } else if (value instanceof JSONArray) {
        pairs.add(YailList.makeList(new Object[] { key, jsonArrayToYail((JSONArray) value)}));
      } else if (value instanceof JSONObject) {
        pairs.add(YailList.makeList(new Object[] { key, jsonObjectToYail((JSONObject) value)}));
      } else {
        Log.wtf(TAG, ERROR_UNKNOWN_TYPE);
        throw new IllegalArgumentException(ERROR_UNKNOWN_TYPE);
      }
    }
    return YailList.makeList(pairs);
  }

  private YailList jsonArrayToYail(JSONArray array) throws JSONException {
    List<Object> items = new ArrayList<Object>();
    for (int i = 0; i < array.length(); i++) {
      Object value = array.get(i);
      if (value instanceof Boolean ||
          value instanceof Integer ||
          value instanceof Long ||
          value instanceof Double ||
          value instanceof String) {
        items.add(value);
      } else if (value instanceof JSONArray) {
        items.add(jsonArrayToYail((JSONArray) value));
      } else if (value instanceof JSONObject) {
        items.add(jsonObjectToYail((JSONObject) value));
      } else {
        Log.wtf(TAG, ERROR_UNKNOWN_TYPE);
        throw new IllegalArgumentException(ERROR_UNKNOWN_TYPE);
      }
    }
    return YailList.makeList(items);
  }

}
