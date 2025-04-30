// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Circle;
import com.google.appinventor.components.runtime.LineString;
import com.google.appinventor.components.runtime.MapFeatureBase;
import com.google.appinventor.components.runtime.MapFeatureBaseWithFill;
import com.google.appinventor.components.runtime.MapTestBase;
import com.google.appinventor.components.runtime.Marker;
import com.google.appinventor.components.runtime.MarkerTest;
import com.google.appinventor.components.runtime.Polygon;
import com.google.appinventor.components.runtime.Rectangle;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import gnu.lists.FString;
import gnu.lists.LList;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLog.LogItem;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import static com.google.appinventor.components.runtime.Component.COLOR_BLUE;
import static com.google.appinventor.components.runtime.Component.COLOR_GREEN;
import static com.google.appinventor.components.runtime.Component.COLOR_RED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GeoJSONUtilTest extends MapTestBase {

  private static final String LOG_TAG = GeoJSONUtilTest.class.getSimpleName();
  private static final String TEST_DESCRIPTION = "test description";
  private static final String TEST_TITLE = "test title";

  @Test
  public void testParseColorByName() {
    assertEquals(COLOR_BLUE, GeoJSONUtil.parseColor("blue"));
  }

  @Test
  public void testParseColorAsHex1() {
    assertEquals(COLOR_BLUE, GeoJSONUtil.parseColor("#0000FF"));
  }

  @Test
  public void testParseColorAsHex2() {
    assertEquals(COLOR_BLUE, GeoJSONUtil.parseColor("&HFF0000FF"));
  }

  @Test
  public void testParseColorAsHex3() {
    assertEquals(COLOR_RED, GeoJSONUtil.parseColor(""));
  }

  @Test
  public void testParseColorHex3() {
    assertEquals(COLOR_BLUE, GeoJSONUtil.parseColorHex("00F"));
  }

  @Test
  public void testParseColorHex6() {
    assertEquals(COLOR_BLUE, GeoJSONUtil.parseColorHex("0000FF"));
  }

  @Test
  public void testParseColorHex8() {
    assertEquals(COLOR_BLUE, GeoJSONUtil.parseColorHex("FF0000FF"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseColorThrows() {
    GeoJSONUtil.parseColorHex("");
    fail();
  }

  @Test
  public void testCharToHexNum() {
    assertEquals(0, GeoJSONUtil.charToHex('0'));
  }

  @Test
  public void testCharToHexAlpha() {
    assertEquals(10, GeoJSONUtil.charToHex('A'));
    assertEquals(10, GeoJSONUtil.charToHex('a'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCharToHexThrows() {
    GeoJSONUtil.charToHex('?');
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCharToHexThrows2() {
    GeoJSONUtil.charToHex(' ');
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCharToHexThrows3() {
    GeoJSONUtil.charToHex('~');
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProcessGeoJSONFeatureBadType() {
    GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "FooBar"));
    fail();
  }

  /**
   * Tests that processing of a feature with a `null` geometry yields
   * the expected behavior.
   *
   * Previously, the function being tested with throw an
   * IllegalArgumentException when `null` was encountered. However,
   * the GeoJSON spec allows for `null` geometries for the purpose of
   * including metadata properties, so we ended up throwing an error
   * for an allowed use.
   */
  @Test
  public void testProcessGeoJSONFeatureNoGeometry() {
    assertNull(GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature")));
  }

  public void testProcessGeoJSONWarnUnknownProperty() {
    ShadowLog.setupLogging();
    GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "FooBar", false,
            "geometry", alist("type", "Point", "coordinates", list(-71, 42))));

    // Check that the FooBar property triggers a warning message
    List<LogItem> logs = ShadowLog.getLogsForTag(LOG_TAG);
    assertEquals(1, logs.size());
    assertEquals("Unsupported field \"FooBar\" in JSON format", logs.get(0).msg);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProcessGeoJSONThrowsOnBadGeometryType() {
    GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "FooBar", "coordinates", list())));
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProcessGeoJSONThrowsOnMissingCoordinates() {
    GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "Point")));
    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProcessGeoJSONThrowsOnCoordinateArityMismatch() {
    GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "Point", "coordinates", list())));
    fail();
  }

  @Test
  public void testProcessGeoJSONFeatureMarker() {
    ShadowLog.setupLogging();
    Marker marker = (Marker) GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "Point", "coordinates", list(-71, 42)),
            "properties", getTestProperties()));
    assertNotNull(marker);
    assertEquals(42.0, marker.Latitude(), 0.0);
    assertEquals(-71.0, marker.Longitude(), 0.0);
    assertTestProperties((MapMarker) marker);
    assertLogTriggered();
  }

  @Test
  public void testProcessGeoJSONFeatureLineString() {
    ShadowLog.setupLogging();
    LineString lineString = (LineString) GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "LineString", "coordinates", list(list(-71, 42), list(-71, 41))),
            "properties", getTestProperties()));
    assertNotNull(lineString);
    assertEquals(new GeoPoint(42.0, -71.0), lineString.getPoints().get(0));
    assertEquals(new GeoPoint(41.0, -71.0), lineString.getPoints().get(1));
    assertTestProperties(lineString);
    assertLogTriggered();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProcessGeoJSONFeatureLineStringThrowsTooFewPoints() {
    GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "LineString", "coordinates", list(list(-71, 42)))));
    fail();
  }

  @Test
  public void testProcessGeoJSONFeaturePolygon() {
    ShadowLog.setupLogging();
    Polygon polygon = (Polygon) GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "Polygon", "coordinates", list(list(list(-71, 42), list(-70, 42), list(-70, 41), list(-71, 42)))),
            "properties", getTestProperties()));
    assertNotNull(polygon);
    assertEquals(new GeoPoint(42.0, -71.0), polygon.getPoints().get(0).get(0));
    assertEquals(new GeoPoint(42.0, -70.0), polygon.getPoints().get(0).get(1));
    assertEquals(new GeoPoint(41.0, -70.0), polygon.getPoints().get(0).get(2));
    assertEquals(new GeoPoint(42.0, -71.0), polygon.getPoints().get(0).get(3));
    assertTestProperties(polygon);
    assertLogTriggered();
  }

  @Test
  public void testProcessGeoJSONFeaturePolygonWithHoles() {
    ShadowLog.setupLogging();
    Polygon polygon = (Polygon) GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "Polygon", "coordinates", list(
                // Polygon
                list(list(-71, 42), list(-70, 42), list(-70, 41), list(-71, 42)),
                // Holes
                list(list(-70.75, 41.75), list(-70.25, 41.75), list(-70.25, 41.25), list(-70.75, 41.75))
            )),
            "properties", getTestProperties()));
    assertNotNull(polygon);
    assertEquals(new GeoPoint(42.0, -71.0), polygon.getPoints().get(0).get(0));
    assertEquals(new GeoPoint(42.0, -70.0), polygon.getPoints().get(0).get(1));
    assertEquals(new GeoPoint(41.0, -70.0), polygon.getPoints().get(0).get(2));
    assertEquals(new GeoPoint(42.0, -71.0), polygon.getPoints().get(0).get(3));
    assertEquals(new GeoPoint(41.75, -70.75), polygon.getHolePoints().get(0).get(0).get(0));
    assertEquals(new GeoPoint(41.75, -70.25), polygon.getHolePoints().get(0).get(0).get(1));
    assertEquals(new GeoPoint(41.25, -70.25), polygon.getHolePoints().get(0).get(0).get(2));
    assertEquals(new GeoPoint(41.75, -70.75), polygon.getHolePoints().get(0).get(0).get(3));
    assertTestProperties(polygon);
    assertLogTriggered();
  }

  @Test
  public void testProcessGeoJSONFeatureMultiPolygon() {
    ShadowLog.setupLogging();
    Polygon polygon = (Polygon) GeoJSONUtil.processGeoJSONFeature(LOG_TAG, getMap(),
        alist("type", "Feature",
            "geometry", alist("type", "MultiPolygon", "coordinates", list(list(list(list(-71, 42), list(-70, 42), list(-70, 41), list(-71, 42))))),
            "properties", getTestProperties()));
    assertNotNull(polygon);
    assertEquals(new GeoPoint(42.0, -71.0), polygon.getPoints().get(0).get(0));
    assertEquals(new GeoPoint(42.0, -70.0), polygon.getPoints().get(0).get(1));
    assertEquals(new GeoPoint(41.0, -70.0), polygon.getPoints().get(0).get(2));
    assertEquals(new GeoPoint(42.0, -71.0), polygon.getPoints().get(0).get(3));
    assertTestProperties(polygon);
    assertLogTriggered();
  }

  @Test
  public void testParseBooleanOrString() {
    assertTrue(GeoJSONUtil.parseBooleanOrString(true));
    assertFalse(GeoJSONUtil.parseBooleanOrString(false));
    assertTrue(GeoJSONUtil.parseBooleanOrString("true"));
    assertFalse(GeoJSONUtil.parseBooleanOrString("false"));
    assertFalse(GeoJSONUtil.parseBooleanOrString(""));
    assertTrue(GeoJSONUtil.parseBooleanOrString(new FString("true")));
    assertFalse(GeoJSONUtil.parseBooleanOrString(new FString("false")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseBooleanOrStringThrowsInvalidArgument() {
    GeoJSONUtil.parseBooleanOrString(new Object());
  }

  @Test
  public void testParseIntegerOrString() {
    assertEquals(0, GeoJSONUtil.parseIntegerOrString(0));
    assertEquals(1, GeoJSONUtil.parseIntegerOrString(1.0));
    assertEquals(0, GeoJSONUtil.parseIntegerOrString("0"));
    assertEquals(1, GeoJSONUtil.parseIntegerOrString(new FString("1")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseIntegerOrStringThrowsInvalidArgument() {
    GeoJSONUtil.parseIntegerOrString(new Object());
  }

  @Test
  public void testWriteFeaturesAsGeoJSON() throws IOException, JSONException {
    MarkerTest.createMarker(getMap(), 42.0, -71.0);
    defaultLineEW(new LineString(getMap()));
    defaultPolygon(new Polygon(getMap()));
    defaultMultipolygon(new Polygon(getMap()));
    defaultRectangle(new Rectangle(getMap()));
    defaultCircle(new Circle(getMap()));
    String contents = saveMapToString();
    JSONObject json = new JSONObject(contents);
    assertEquals(6, json.getJSONArray("features").length());
    // Consider inspecting the objects more deeply to ensure correctness in the future
  }

  @Test
  public void testWriteFeaturesAsGeoJSONNoFeatures() throws IOException, JSONException {
    String contents = saveMapToString();
    JSONObject json = new JSONObject(contents);
    assertEquals(0, json.getJSONArray("features").length());
  }

  @Test
  public void testSwapCoordinates() {
    YailList testList = list(list("first", "second"));
    GeoJSONUtil.swapCoordinates(testList);
    assertEquals(1, testList.size());
    YailList child = (YailList) testList.getObject(0);
    assertEquals("second", child.getString(0));
    assertEquals("first", child.getString(1));
  }

  @Test
  public void testSwapNestedCoordinates() {
    YailList coords = YailList.makeList(new YailList[] {
        YailList.makeList(new YailList[] {
            YailList.makeList(new Double[] {
                0.0,
                1.0
            }),
            YailList.makeList(new Double[] {
                -1.0,
                0.0
            })
        })
    });
    GeoJSONUtil.swapNestedCoordinates((LList) coords.getCdr());
    assertEquals(1, coords.size());
    assertEquals(2, ((YailList) coords.get(1)).size());
    YailList coord = (YailList)((YailList) coords.get(1)).get(1);
    assertEquals(1.0, (Double) coord.get(1), 0.0);
    assertEquals(0.0, (Double) coord.get(2), 0.0);
    coord = (YailList)((YailList) coords.get(1)).get(2);
    assertEquals(0.0, (Double) coord.get(1), 0.0);
    assertEquals(-1.0, (Double) coord.get(2), 0.0);
  }

  @Test
  public void evilTestConstructor() throws Exception {
    Constructor defaultConstructor = GeoJSONUtil.class.getDeclaredConstructor();
    defaultConstructor.setAccessible(true);
    assertNotNull(defaultConstructor.newInstance());
  }

  private static YailList list(Object... items) {
    return YailList.makeList(items);
  }

  private static YailList alist(Object... pairs) {
    if (pairs.length % 2 == 1) {
      throw new IllegalArgumentException("Expected an even number of items for alist");
    }
    List<YailList> alist = new LinkedList<YailList>();
    for (int i = 0; i < pairs.length; i += 2) {
      alist.add(list(pairs[i], pairs[i+1]));
    }
    return YailList.makeList(alist);
  }

  private static YailList getTestProperties() {
    return alist(
        "description", TEST_DESCRIPTION,
        "draggable", true,
        "fill", "blue",
        "fill-opacity", 0.4,
        "image", "",
        "infobox", false,
        "stroke", "green",
        "stroke-opacity", 0.7,
        "stroke-width", 3,
        "title", TEST_TITLE,
        "visible", true,
        "anchorHorizontal", 1,
        "anchorVertical", 3,
        "width", 32,
        "height", 52,
        "triggersLog", true);
  }

  private static void assertTestProperties(MapMarker feature) {
    assertEquals("", feature.ImageAsset());
    assertEquals(1, feature.AnchorHorizontal());
    assertEquals(3, feature.AnchorVertical());
    assertEquals(32, feature.Width());
    assertEquals(52, feature.Height());
    assertTestProperties((MapFeatureBaseWithFill) feature);
  }

  private static void assertTestProperties(MapFeatureBaseWithFill feature) {
    assertEquals(COLOR_BLUE, feature.FillColor() | 0xFF000000);
    assertEquals(0.4, feature.FillOpacity(), 1e-6);
    assertEquals(Math.round(0.4 * 255), feature.FillColor() >>> 24);
    assertTestProperties((MapFeatureBase) feature);
  }

  private static void assertTestProperties(MapFeatureBase feature) {
    assertEquals(COLOR_GREEN, feature.StrokeColor() | 0xFF000000);
    assertEquals(0.7, feature.StrokeOpacity(), 1e-6);
    assertEquals(Math.round(0.7 * 255), feature.StrokeColor() >>> 24);
    assertEquals(3, feature.StrokeWidth());
    assertTestProperties((MapFeature) feature);
  }

  private static void assertTestProperties(MapFeature feature) {
    assertEquals(TEST_DESCRIPTION, feature.Description());
    assertTrue(feature.Draggable());
    assertFalse(feature.EnableInfobox());
    assertEquals(TEST_TITLE, feature.Title());
    assertTrue(feature.Visible());
  }

  private static void assertLogTriggered() {
    // Check that the triggersLog property triggers a log message
    List<LogItem> logs = ShadowLog.getLogsForTag(LOG_TAG);
    assertEquals(1, logs.size());
    assertEquals("Ignoring GeoJSON property \"triggersLog\"", logs.get(0).msg);
  }

  private String saveMapToString() throws IOException {
    File temp;
    InputStream is = null;
    try {
      temp = File.createTempFile("test", "geojson");
      temp.deleteOnExit();
      getMap().Save(temp.getAbsolutePath());
      ShadowAsynchUtil.runAllPendingRunnables();
      is = new BufferedInputStream(new FileInputStream(temp));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int read = 0;
      while ((read = is.read(buffer)) > 0) {
        baos.write(buffer, 0, read);
      }
      return baos.toString();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch(IOException e1) {
          // Nothing we can do about this
        }
      }
    }
  }
}
