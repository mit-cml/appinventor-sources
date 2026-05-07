// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.text.TextUtils;
import android.util.Log;
import com.google.appinventor.components.runtime.LineString;
import com.google.appinventor.components.runtime.Marker;
import com.google.appinventor.components.runtime.Polygon;
import com.google.appinventor.components.runtime.util.MapFactory.HasFill;
import com.google.appinventor.components.runtime.util.MapFactory.HasStroke;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureContainer;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureType;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.common.annotations.VisibleForTesting;
import gnu.lists.FString;
import gnu.lists.LList;
import gnu.lists.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.appinventor.components.runtime.Component.*;

/**
 * Utility class to process data from GeoJSON.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public final class GeoJSONUtil {
  private static final java.util.Map<String, Integer> colors;
  private static final int ERROR_CODE_MALFORMED_GEOJSON = -3;
  private static final String ERROR_MALFORMED_GEOJSON = "Malformed GeoJSON response. Expected FeatureCollection as root element.";
  private static final String ERROR_UNKNOWN_TYPE = "Unrecognized/invalid type in JSON object";
  private static final String GEOJSON_COORDINATES = "coordinates";
  private static final String GEOJSON_FEATURE = "Feature";
  private static final String GEOJSON_FEATURECOLLECTION = "FeatureCollection";
  private static final String GEOJSON_FEATURES = "features";
  private static final String GEOJSON_GEOMETRY = "geometry";
  private static final String GEOJSON_GEOMETRYCOLLECTION = "GeometryCollection";
  private static final String GEOJSON_PROPERTIES = "properties";
  private static final String GEOJSON_TYPE = "type";
  private static final String PROPERTY_ANCHOR_HORIZONTAL = "anchorHorizontal";
  private static final String PROPERTY_ANCHOR_VERTICAL = "anchorVertical";
  private static final String PROPERTY_DESCRIPTION = "description";
  private static final String PROPERTY_DRAGGABLE = "draggable";
  private static final String PROPERTY_FILL = "fill";
  private static final String PROPERTY_FILL_OPACITY = "fill-opacity";
  private static final String PROPERTY_HEIGHT = "height";
  private static final String PROPERTY_IMAGE = "image";
  private static final String PROPERTY_INFOBOX = "infobox";
  private static final String PROPERTY_STROKE = "stroke";
  private static final String PROPERTY_STROKE_OPACITY = "stroke-opacity";
  private static final String PROPERTY_STROKE_WIDTH = "stroke-width";
  private static final String PROPERTY_TITLE = "title";
  private static final String PROPERTY_WIDTH = "width";
  private static final String PROPERTY_VISIBLE = "visible";
  private static final int KEY = 1;
  private static final int VALUE = 2;
  // Indexes in YailList of Lat, Long in GeoJSON, which are reversed from how they are usually written
  private static final int LATITUDE = 2;
  private static final int LONGITUDE = 1;
  private static final Map<String, PropertyApplication> SUPPORTED_PROPERTIES;

  private interface PropertyApplication {
    void apply(MapFeature feature, Object value);
  }

  static {
    colors = new HashMap<String, Integer>();
    colors.put("black", COLOR_BLACK);
    colors.put("blue", COLOR_BLUE);
    colors.put("cyan", COLOR_CYAN);
    colors.put("darkgray", COLOR_DKGRAY);
    colors.put("gray", COLOR_GRAY);
    colors.put("green", COLOR_GREEN);
    colors.put("lightgray", COLOR_LTGRAY);
    colors.put("magenta", COLOR_MAGENTA);
    colors.put("orange", COLOR_ORANGE);
    colors.put("pink", COLOR_PINK);
    colors.put("red", COLOR_RED);
    colors.put("white", COLOR_WHITE);
    colors.put("yellow", COLOR_YELLOW);

    SUPPORTED_PROPERTIES = new HashMap<String, PropertyApplication>();
    SUPPORTED_PROPERTIES.put(PROPERTY_ANCHOR_HORIZONTAL.toLowerCase(), new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof MapMarker) {
          ((MapMarker) feature).AnchorHorizontal(parseIntegerOrString(value));
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_ANCHOR_VERTICAL.toLowerCase(), new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof MapMarker) {
          ((MapMarker) feature).AnchorHorizontal();
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_DESCRIPTION, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        feature.Description(value.toString());
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_DRAGGABLE, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        feature.Draggable(parseBooleanOrString(value));
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_FILL, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof HasFill) {
          ((HasFill) feature).FillColor(value instanceof Number ? ((Number) value).intValue() :
              parseColor(value.toString()));
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_FILL_OPACITY, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof HasFill) {
          ((HasFill) feature).FillOpacity(parseFloatOrString(value));
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_HEIGHT, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof MapMarker) {
          ((MapMarker) feature).Height(parseIntegerOrString(value));
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_IMAGE, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof MapMarker) {
          ((MapMarker) feature).ImageAsset(value.toString());
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_INFOBOX, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        feature.EnableInfobox(parseBooleanOrString(value));
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_STROKE, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof HasStroke) {
          ((HasStroke) feature).StrokeColor(value instanceof Number ? ((Number) value).intValue() :
              parseColor(value.toString()));
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_STROKE_OPACITY, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof HasStroke) {
          ((HasStroke) feature).StrokeOpacity(parseFloatOrString(value));
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_STROKE_WIDTH, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof HasStroke) {
          ((HasStroke) feature).StrokeWidth(parseIntegerOrString(value));
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_TITLE, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        feature.Title(value.toString());
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_WIDTH, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        if (feature instanceof MapMarker) {
          ((MapMarker) feature).Width(parseIntegerOrString(value));
        }
      }
    });
    SUPPORTED_PROPERTIES.put(PROPERTY_VISIBLE, new PropertyApplication() {
      @Override
      public void apply(MapFeature feature, Object value) {
        feature.Visible(parseBooleanOrString(value));
      }
    });
  }

  private GeoJSONUtil() {}

  @VisibleForTesting
  static int parseColor(final String value) {
    String lcValue = value.toLowerCase();
    Integer result = colors.get(lcValue);
    if (result != null) {
      return result;
    } else if (lcValue.startsWith("#")) {
      return parseColorHex(lcValue.substring(1));
    } else if (lcValue.startsWith("&h")) {
      return parseColorHex(lcValue.substring(2));
    } else {
      return COLOR_RED;
    }
  }

  @VisibleForTesting
  static int parseColorHex(final String value) {
    int argb = 0;
    if (value.length() == 3) {
      // 4-bit RGB
      argb = 0xFF000000;
      int hex;
      for (int i = 0; i < value.length(); i++) {
        hex = charToHex(value.charAt(i));
        argb |= ((hex << 4) | hex) << (2-i)*8;
      }
    } else if (value.length() == 6) {
      // 8-bit RGB
      argb = 0xFF000000;
      int hex;
      for (int i = 0; i < 3; i++) {
        hex = charToHex(value.charAt(2*i)) << 4 | charToHex(value.charAt(2*i+1));
        argb |= hex << (2-i)*8;
      }
    } else if (value.length() == 8) {
      // 8-bit ARGB
      int hex;
      for (int i = 0; i < 4; i++) {
        hex = charToHex(value.charAt(2*i)) << 4 | charToHex(value.charAt(2*i+1));
        argb |= hex << (3-i)*8;
      }
    } else {
      throw new IllegalArgumentException();
    }
    return argb;
  }

  @VisibleForTesting
  static int charToHex(char c) {
    if ( '0' <= c && c <= '9' ) {
      return c - '0';
    } else if ( 'a' <= c && c <= 'f' ) {
      return c - 'a' + 10;
    } else if ( 'A' <= c && c <= 'F' ) {
      return c - 'A' + 10;
    } else {
      throw new IllegalArgumentException("Invalid hex character. Expected [0-9A-Fa-f].");
    }
  }

  public static MapFactory.MapFeature processGeoJSONFeature(final String logTag,
      final MapFactory.MapFeatureContainer container, final YailList descriptions) {
    String type = null;
    YailList geometry = null;
    YailList properties = null;
    for (Object o : (LList) descriptions.getCdr()) {
      YailList keyvalue = (YailList)o;
      String key = keyvalue.getString(0);
      Object value = keyvalue.getObject(1);
      if (GEOJSON_TYPE.equals(key)) {
        type = (String) value;
      } else if (GEOJSON_GEOMETRY.equals(key)) {
        geometry = (YailList) value;
      } else if (GEOJSON_PROPERTIES.equals(key)) {
        properties = (YailList) value;
      } else {
        Log.w(logTag, String.format("Unsupported field \"%s\" in JSON format", key));
      }
    }
    if (!GEOJSON_FEATURE.equals(type)) {
      throw new IllegalArgumentException(String.format("Unknown type \"%s\"", type));
    }
    if (geometry == null) {
      // While GeoJSON supports features with null geometries, App Inventor has no way to
      // represent them.
      return null;
    }
    MapFactory.MapFeature feature = processGeometry(logTag, container, geometry);
    if (properties != null) {
      processProperties(logTag, feature, properties);
    }
    return feature;
  }

  private static MapFactory.MapFeature processGeometry(final String logTag,
      final MapFactory.MapFeatureContainer container, final YailList geometry) {
    String type = null;
    YailList coordinates = null;
    for (Object o : (LList) geometry.getCdr()) {
      YailList keyvalue = (YailList)o;
      String key = keyvalue.getString(0);
      Object value = keyvalue.getObject(1);
      if (GEOJSON_TYPE.equals(key)) {
        type = (String) value;
      } else if (GEOJSON_COORDINATES.equals(key)) {
        coordinates = (YailList) value;
      } else {
        Log.w(logTag, String.format("Unsupported field \"%s\" in JSON format", key));
      }
    }
    if (coordinates == null) {
      throw new IllegalArgumentException("No coordinates found in GeoJSON Feature");
    }
    return processCoordinates(container, type, coordinates);
  }

  private static MapFeature processCoordinates(final MapFeatureContainer container,
      final String type, final YailList coordinates) {
    if (MapFactory.MapFeatureType.TYPE_POINT.equals(type)) {
      return markerFromGeoJSON(container, coordinates);
    } else if (MapFactory.MapFeatureType.TYPE_LINESTRING.equals(type)) {
      return lineStringFromGeoJSON(container, coordinates);
    } else if (MapFactory.MapFeatureType.TYPE_POLYGON.equals(type)) {
      return polygonFromGeoJSON(container, coordinates);
    } else if (MapFeatureType.TYPE_MULTIPOLYGON.equals(type)) {
      return multipolygonFromGeoJSON(container, coordinates);
    }
    throw new IllegalArgumentException();
  }

  private static MapFactory.MapMarker markerFromGeoJSON(final MapFeatureContainer container,
      final YailList coordinates) {
    if (coordinates.length() != 3) {  // One entry for list header and two for lat, long pair
      throw new IllegalArgumentException("Invalid coordinate supplied in GeoJSON");
    }
    Marker marker = new Marker(container);
    marker.Latitude(((Number) coordinates.get(LATITUDE)).doubleValue());
    marker.Longitude(((Number) coordinates.get(LONGITUDE)).doubleValue());
    return marker;
  }

  private static MapLineString lineStringFromGeoJSON(final MapFeatureContainer container,
      final YailList coordinates) {
    if (coordinates.size() < 2) {
      throw new IllegalArgumentException("Too few coordinates supplied in GeoJSON");
    }
    LineString lineString = new LineString(container);
    lineString.Points(swapCoordinates(coordinates));
    return lineString;
  }

  private static MapPolygon polygonFromGeoJSON(final MapFeatureContainer container,
      final YailList coordinates) {
    Polygon polygon = new Polygon(container);
    Iterator i = coordinates.iterator();
    i.next();
    polygon.Points(swapCoordinates((YailList) i.next()));
    if (i.hasNext()) {
      polygon.HolePoints(YailList.makeList(swapNestedCoordinates((LList) ((Pair)coordinates.getCdr()).getCdr())));
    }
    polygon.Initialize();
    return polygon;
  }

  private static MapPolygon multipolygonFromGeoJSON(final MapFeatureContainer container,
      final YailList coordinates) {
    Polygon polygon = new Polygon(container);
    List<YailList> points = new ArrayList<YailList>();
    List<YailList> holePoints = new ArrayList<YailList>();
    Iterator i = coordinates.iterator();
    i.next();
    while (i.hasNext()) {
      YailList list = (YailList) i.next();
      points.add(swapCoordinates((YailList) list.get(1)));
      holePoints.add(YailList.makeList(swapNestedCoordinates((LList) ((Pair) list.getCdr()).getCdr())));
    }
    polygon.Points(YailList.makeList(points));
    polygon.HolePoints(YailList.makeList(holePoints));
    polygon.Initialize();
    return polygon;
  }

  private static void processProperties(final String logTag, final MapFactory.MapFeature feature,
      final YailList properties) {
    for (Object o : properties) {
      if (o instanceof YailList) {
        YailList pair = (YailList) o;
        String key = pair.get(KEY).toString();
        PropertyApplication application = SUPPORTED_PROPERTIES.get(key.toLowerCase());
        if (application != null) {
          application.apply(feature, pair.get(VALUE));
        } else {
          Log.i(logTag, String.format("Ignoring GeoJSON property \"%s\"", key));
        }
      }
    }
  }

  @VisibleForTesting
  static boolean parseBooleanOrString(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    } else if (value instanceof String) {
      return !("false".equalsIgnoreCase((String) value) || ((String) value).length() == 0);
    } else if (value instanceof FString) {
      return parseBooleanOrString(value.toString());
    } else {
      throw new IllegalArgumentException();
    }
  }

  @VisibleForTesting
  static int parseIntegerOrString(Object value) {
    if (value instanceof Number) {
      return ((Number) value).intValue();
    } else if (value instanceof String) {
      return Integer.parseInt((String) value);
    } else if (value instanceof FString) {
      return Integer.parseInt(value.toString());
    } else {
      throw new IllegalArgumentException();
    }
  }

  @VisibleForTesting
  static float parseFloatOrString(Object value) {
    if (value instanceof Number) {
      return ((Number) value).floatValue();
    } else if (value instanceof String) {
      return Float.parseFloat((String) value);
    } else if (value instanceof FString) {
      return Float.parseFloat(value.toString());
    } else {
      throw new IllegalArgumentException();
    }
  }

  public static List<YailList> getGeoJSONFeatures(final String logTag, final String content) throws JSONException {
    JSONObject parsedData = new JSONObject(stripBOM(content));
    JSONArray features = parsedData.getJSONArray(GEOJSON_FEATURES);
    List<YailList> yailFeatures = new ArrayList<YailList>();
    for (int i = 0; i < features.length(); i++) {
      yailFeatures.add(jsonObjectToYail(logTag, features.getJSONObject(i)));
    }
    return yailFeatures;
  }

  public static String getGeoJSONType(final String content, final String geojsonType) throws JSONException {
    JSONObject parsedData = new JSONObject(stripBOM(content));
    String type = parsedData.optString(geojsonType);
    return type;
  }

  private static YailList jsonObjectToYail(final String logTag, final JSONObject object) throws JSONException {
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
        pairs.add(YailList.makeList(new Object[] { key, jsonArrayToYail(logTag, (JSONArray) value)}));
      } else if (value instanceof JSONObject) {
        pairs.add(YailList.makeList(new Object[] { key, jsonObjectToYail(logTag, (JSONObject) value)}));
      } else if (!JSONObject.NULL.equals(value)) {
        Log.wtf(logTag, ERROR_UNKNOWN_TYPE + ": " + value.getClass());
        throw new IllegalArgumentException(ERROR_UNKNOWN_TYPE);
      }
    }
    return YailList.makeList(pairs);
  }

  private static YailList jsonArrayToYail(final String logTag, final JSONArray array) throws JSONException {
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
        items.add(jsonArrayToYail(logTag, (JSONArray) value));
      } else if (value instanceof JSONObject) {
        items.add(jsonObjectToYail(logTag, (JSONObject) value));
      } else if (!JSONObject.NULL.equals(value)) {
        Log.wtf(logTag, ERROR_UNKNOWN_TYPE + ": " + value.getClass());
        throw new IllegalArgumentException(ERROR_UNKNOWN_TYPE);
      }
    }
    return YailList.makeList(items);
  }

  private static String stripBOM(String content) {
    if (content.charAt(0) == '\uFEFF') {
      return content.substring(1);
    } else {
      return content;
    }
  }

  private static final class FeatureWriter implements MapFactory.MapFeatureVisitor<Void> {

    private final PrintStream out;

    private FeatureWriter(PrintStream out) {
      this.out = out;
    }

    private void writeType(String type) {
      out.print("\"type\":\"");
      out.print(type);
      out.print("\"");
    }

    private void writeProperty(String property, Object value) {
      try {
        String result = JsonUtil.getJsonRepresentation(value);
        out.print(",\"");
        out.print(property);
        out.print("\":");
        out.print(result);
      } catch(JSONException e) {
        Log.w("GeoJSONUtil", "Unable to serialize the value of \"" + property + "\" as JSON", e);
      }
    }

    private void writeProperty(String property, String value) {
      if (value == null || TextUtils.isEmpty(value)) {
        // Suppress empty values
        return;
      }
      writeProperty(property, (Object) value);
    }

    private void writeColorProperty(String property, int color) {
      out.print(",\"");
      out.print(property);
      out.print("\":\"&H");
      String unpadded = Integer.toHexString(color);
      for (int i = 8; i > unpadded.length(); i--) {
        out.print("0");
      }
      out.print(unpadded);
      out.print("\"");
    }

    private void writePointGeometry(GeoPoint point) {
      out.print("\"geometry\":{\"type\":\"Point\",\"coordinates\":[");
      out.print(point.getLongitude());
      out.print(",");
      out.print(point.getLatitude());
      if (hasAltitude(point)) {
        out.print(",");
        out.print(point.getAltitude());
      }
      out.print("]}");
    }

    private void writePropertiesHeader(String runtimeType) {
      out.print(",\"properties\":{\"$Type\":\"" + runtimeType + "\"");
    }

    private void writeProperties(MapFeature feature) {
      writeProperty(PROPERTY_DESCRIPTION, feature.Description());
      writeProperty(PROPERTY_DRAGGABLE, feature.Draggable());
      writeProperty(PROPERTY_INFOBOX, feature.EnableInfobox());
      writeProperty(PROPERTY_TITLE, feature.Title());
      writeProperty(PROPERTY_VISIBLE, feature.Visible());
    }

    private void writeProperties(HasStroke feature) {
      writeColorProperty(PROPERTY_STROKE, feature.StrokeColor());
      writeProperty(PROPERTY_STROKE_OPACITY, feature.StrokeOpacity());
      writeProperty(PROPERTY_STROKE_WIDTH, feature.StrokeWidth());
    }

    private void writeProperties(HasFill feature) {
      writeColorProperty(PROPERTY_FILL, feature.FillColor());
      writeProperty(PROPERTY_FILL_OPACITY, feature.FillOpacity());
    }

    private void writePoints(List<GeoPoint> points) {
      boolean first = true;
      for (GeoPoint p : points) {
        if (!first) out.print(',');
        out.print("[");
        out.print(p.getLongitude());
        out.print(",");
        out.print(p.getLatitude());
        if (hasAltitude(p)) {
          out.print(",");
          out.print(p.getAltitude());
        }
        out.print("]");
        first = false;
      }
    }

    private void writeLineGeometry(MapLineString lineString) {
      out.print("\"geometry\":{\"type\":\"LineString\",\"coordinates\":[");
      writePoints(lineString.getPoints());
      out.print("]}");
    }

    private void writeMultipolygonGeometryNoHoles(MapPolygon polygon) {
      out.print("\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[");
      Iterator<List<GeoPoint>> pointIterator = polygon.getPoints().iterator();
      Iterator<List<List<GeoPoint>>> holePointIterator = polygon.getHolePoints().iterator();
      boolean first = true;
      while (pointIterator.hasNext()) {
        if (!first) out.print(",");
        out.print("[");
        writePoints(pointIterator.next());
        if (holePointIterator.hasNext()) {
          for (List<GeoPoint> holePoints : holePointIterator.next()) {
            out.print(",");
            writePoints(holePoints);
          }
        }
        out.print("]");
        first = false;
      }
      out.print("]}");
    }

    private void writePolygonGeometryNoHoles(MapPolygon polygon) {
      out.print("\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[");
      writePoints(polygon.getPoints().get(0));
      if (!polygon.getHolePoints().isEmpty()) {
        for (List<GeoPoint> points : polygon.getHolePoints().get(0)) {
          out.print(",");
          writePoints(points);
        }
      }
      out.print("]}");
    }

    private void writePolygonGeometry(MapPolygon polygon) {
      if (polygon.getPoints().size() > 1) {
        writeMultipolygonGeometryNoHoles(polygon);
      } else {
        writePolygonGeometryNoHoles(polygon);
      }
    }

    @Override
    public Void visit(MapFactory.MapMarker marker, Object... arguments) {
      out.print("{");
      writeType(GEOJSON_FEATURE);
      out.print(',');
      writePointGeometry(marker.getCentroid());
      writePropertiesHeader(marker.getClass().getName());
      writeProperties((MapFeature) marker);
      writeProperties((HasStroke) marker);
      writeProperties((HasFill) marker);
      writeProperty(PROPERTY_ANCHOR_HORIZONTAL, marker.AnchorHorizontal());
      writeProperty(PROPERTY_ANCHOR_VERTICAL, marker.AnchorVertical());
      writeProperty(PROPERTY_HEIGHT, marker.Height());
      writeProperty(PROPERTY_IMAGE, marker.ImageAsset());
      writeProperty(PROPERTY_WIDTH, marker.Width());
      out.print("}}");
      return null;
    }

    @Override
    public Void visit(MapFactory.MapLineString lineString, Object... arguments) {
      out.print("{");
      writeType(GEOJSON_FEATURE);
      out.print(',');
      writeLineGeometry(lineString);
      writePropertiesHeader(lineString.getClass().getName());
      writeProperties((MapFeature) lineString);
      writeProperties((HasStroke) lineString);
      out.print("}}");
      return null;
    }

    @Override
    public Void visit(MapFactory.MapPolygon polygon, Object... arguments) {
      out.print("{");
      writeType(GEOJSON_FEATURE);
      out.print(',');
      writePolygonGeometry(polygon);
      writePropertiesHeader(polygon.getClass().getName());
      writeProperties((MapFeature) polygon);
      writeProperties((HasStroke) polygon);
      writeProperties((HasFill) polygon);
      out.print("}}");
      return null;
    }

    @Override
    public Void visit(MapFactory.MapCircle circle, Object... arguments) {
      out.print("{");
      writeType(GEOJSON_FEATURE);
      out.print(',');
      writePointGeometry(circle.getCentroid());
      writePropertiesHeader(circle.getClass().getName());
      writeProperties((MapFeature) circle);
      writeProperties((HasStroke) circle);
      writeProperties((HasFill) circle);
      out.print("}}");
      return null;
    }

    @Override
    public Void visit(MapFactory.MapRectangle rectangle, Object... arguments) {
      out.print("{");
      writeType(GEOJSON_FEATURE);
      out.print(",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[");
      out.print("[" + rectangle.WestLongitude() + "," + rectangle.NorthLatitude() + "],");
      out.print("[" + rectangle.WestLongitude() + "," + rectangle.SouthLatitude() + "],");
      out.print("[" + rectangle.EastLongitude() + "," + rectangle.SouthLatitude() + "],");
      out.print("[" + rectangle.EastLongitude() + "," + rectangle.NorthLatitude() + "],");
      out.print("[" + rectangle.WestLongitude() + "," + rectangle.NorthLatitude() + "]]}");
      writePropertiesHeader(rectangle.getClass().getName());
      writeProperties((MapFeature) rectangle);
      writeProperties((HasStroke) rectangle);
      writeProperties((HasFill) rectangle);
      writeProperty("NorthLatitude", rectangle.NorthLatitude());
      writeProperty("WestLongitude", rectangle.WestLongitude());
      writeProperty("SouthLatitude", rectangle.SouthLatitude());
      writeProperty("EastLongitude", rectangle.EastLongitude());
      out.print("}}");
      return null;
    }

    // This is here because of an interaction between OSMDroid's (bad) default that not having an
    // altitude is the same as having 0 altitude and a rule in Sonar that doubles shouldn't be
    // directly compared. In this case, we actually want to know that the double value is exactly 0.
    // It would be better if OSMDroid used NaN for the altitude if it was undefined, but such is life.
    private static boolean hasAltitude(GeoPoint point) {
      return Double.compare(0.0, point.getAltitude()) != 0;
    }
  }

  public static void writeFeaturesAsGeoJSON(List<MapFactory.MapFeature> featuresToSave, String path) throws IOException {
    PrintStream out = null;
    try {
      out = new PrintStream(new FileOutputStream(path));
      FeatureWriter writer = new FeatureWriter(out);
      out.print("{\"type\": \"FeatureCollection\", \"features\":[");
      MapFeature feature;
      Iterator<MapFeature> it = featuresToSave.iterator();
      if (it.hasNext()) {
        feature = it.next();
        feature.accept(writer);
        while (it.hasNext()) {
          feature = it.next();
          out.print(',');
          feature.accept(writer);
        }
      }
      out.print("]}");
    } finally {
      IOUtils.closeQuietly("GeoJSONUtil", out);
    }
  }

  /**
   * App Inventor stores values latitude, longitude, so swap the coordinate order when reading
   * GeoJSON.
   * @param coordinates A YailList of coordinates.
   * @return coordinates, with its lat and long swapped.
   */
  public static YailList swapCoordinates(YailList coordinates) {
    Iterator i = coordinates.iterator();
    i.next();
    while (i.hasNext()) {
      YailList coordinate = (YailList) i.next();
      Object temp = coordinate.get(1);
      Pair p = (Pair) coordinate.getCdr();
      p.setCar(coordinate.get(2));
      p = (Pair) p.getCdr();
      p.setCar(temp);
    }
    return coordinates;
  }

  public static <E> List<List<E>> swapCoordinates2(List<List<E>> coordinates) {
    for (List<E> point : coordinates) {
      E temp = point.get(0);
      point.set(0, point.get(1));
      point.set(1, temp);
    }
    return coordinates;
  }

  public static LList swapNestedCoordinates(LList coordinates) {
    LList it = coordinates;
    while (!it.isEmpty()) {
      swapCoordinates((YailList) it.get(0));
      it = (LList) ((Pair) it).getCdr();
    }
    return coordinates;
  }
}
