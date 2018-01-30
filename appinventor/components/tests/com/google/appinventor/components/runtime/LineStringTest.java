// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Before;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class LineStringTest extends MapTestBase {

  private LineString line;

  @Before
  public void setUp() {
    super.setUp();
    line = new LineString(getMap());
  }

  @Test
  public void testPointsFromString() {
    line.PointsFromString("[[1, 1], [-1, -1]]");
    YailList points = line.Points();
    assertEquals(2, points.size());
    assertEquals(1.0, (Double)((YailList)points.get(1)).get(1), DEG_TOL);
    assertEquals(1.0, (Double)((YailList)points.get(1)).get(2), DEG_TOL);
    assertEquals(-1.0, (Double)((YailList)points.get(2)).get(1), DEG_TOL);
    assertEquals(-1.0, (Double)((YailList)points.get(2)).get(2), DEG_TOL);
  }

  @Test
  public void testPointsFromStringBadForm() {
    line.PointsFromString("foo");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_LINESTRING_PARSE_ERROR);
  }

  @Test
  public void testPointsFromStringNotArray() {
    line.PointsFromString("{}");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_LINESTRING_PARSE_ERROR);
  }

  @Test
  public void testPointsFromStringArrayNoData() {
    line.PointsFromString("[]");
    assertEquals(0, line.Points().size());
  }

  @Test
  public void testPointsFromStringOneCoordinate() {
    line.PointsFromString("[[0, 0]]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_LINESTRING_TOO_FEW_POINTS);
  }

  @Test
  public void testPointsFromStringBadCoordinate() {
    line.PointsFromString("[[0, 0], \"foo\"]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_EXPECTED_ARRAY_AT_INDEX);
  }

  @Test
  public void testPointsFromStringWrongArity() {
    line.PointsFromString("[[0], [0, 0]]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_LINESTRING_TOO_FEW_FIELDS);
  }

  @Test
  public void testPointsFromStringBadLatitudeNorth() {
    line.PointsFromString("[[9000, 0], [0, 0]]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LATITUDE_IN_POINT_AT_INDEX);
  }

  @Test
  public void testPointsFromStringBadLatitudeSouth() {
    line.PointsFromString("[[-9000, 0], [0, 0]]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LATITUDE_IN_POINT_AT_INDEX);
  }

  @Test
  public void testPointsFromStringBadLongitudeEast() {
    line.PointsFromString("[[0, 9000], [0, 0]]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LONGITUDE_IN_POINT_AT_INDEX);
  }

  @Test
  public void testPointsFromStringBadLongitudeWest() {
    line.PointsFromString("[[0, -9000], [0, 0]]");
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_LONGITUDE_IN_POINT_AT_INDEX);
  }

  @Test
  public void testLineStringIsLineString() {
    assertEquals("LineString", line.Type());
  }

  @Test
  public void testPointsTooFew() {
    line.Points(YailList.makeEmptyList());
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_LINESTRING_TOO_FEW_POINTS);
  }

  @Test
  public void testPointsInvalid() {
    line.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] { 0., 0. }),
        "foo"
    }));
    ShadowEventDispatcher.assertErrorOccurred(ErrorMessages.ERROR_INVALID_TYPE_AT_INDEX);
  }

  @Test
  public void testUpdatePoints() {
    line.updatePoints(Arrays.asList(new GeoPoint(1.0, 1.0), new GeoPoint(-1.0, -1.0)));
    YailList points = line.Points();
    assertEquals(2, points.size());
    assertEquals(1.0, ((YailList) points.get(1)).get(1));
    assertEquals(1.0, ((YailList) points.get(1)).get(2));
    assertEquals(-1.0, ((YailList) points.get(2)).get(1));
    assertEquals(-1.0, ((YailList) points.get(2)).get(2));
    assertEquals(new GeoPoint(0., 0.), line.getCentroid());
  }
}
