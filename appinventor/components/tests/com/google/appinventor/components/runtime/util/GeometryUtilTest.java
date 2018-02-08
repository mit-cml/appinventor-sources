// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.DispatchableError;
import com.google.appinventor.components.runtime.errors.IterationError;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import gnu.lists.FString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.util.GeoPoint;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.appinventor.components.runtime.util.GeometryUtil.isMultiPolygon;
import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLatitude;
import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLongitude;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class GeometryUtilTest {

  /** Absolute tolerance allowed in degrees */
  private static final double TOLERANCE = 1.0E-7;

  /** Percent error allowed in a double computation. */
  private static final double P_TOLERANCE = 0.005;

  private static final double DEGTORAD = Math.PI / 180.0;

  @Test
  public void testCoerceToDoubleInt() {
    assertEquals(0.0, GeometryUtil.coerceToDouble(0), TOLERANCE);
  }

  @Test
  public void testCoerceToDoubleDouble() {
    assertEquals(0.0, GeometryUtil.coerceToDouble(0.0), TOLERANCE);
  }

  @Test
  public void testCoerceToDoubleFString() {
    assertEquals(0.0, GeometryUtil.coerceToDouble(new FString("0")), TOLERANCE);
  }

  @Test
  public void testCoerceToDoubleString() {
    assertEquals(0.0, GeometryUtil.coerceToDouble("0"), TOLERANCE);
  }

  @Test
  public void testCoerceToDoubleNaN() {
    assertTrue(Double.isNaN(GeometryUtil.coerceToDouble("foo")));
  }

  @Test
  public void testCoerceToPoint() {
    assertPointEquals(0.0, 0.0, GeometryUtil.coerceToPoint(0, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCoerceToPointBadLatitudeThrows() {
    GeometryUtil.coerceToPoint("foo", 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCoerceToPointBadLongitudeThrows() {
    GeometryUtil.coerceToPoint(0, "foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCoerceToPointBadLatitudeRangeThrows() {
    GeometryUtil.coerceToPoint("999", 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCoerceToPointBadLongitudeRangeThrows() {
    GeometryUtil.coerceToPoint(0, "999");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCoerceToPointBadLatitudeRangeThrows2() {
    GeometryUtil.coerceToPoint("-999", 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCoerceToPointBadLongitudeRangeThrows2() {
    GeometryUtil.coerceToPoint(0, "-999");
  }

  @Test
  public void testAsYailList() {
    YailList result = GeometryUtil.asYailList(new GeoPoint(1.0, -1.0));
    assertNotNull(result);
    assertEquals(1.0, result.get(1));
    assertEquals(-1.0, result.get(2));
  }

  @Test
  public void testPointsListToYailList() {
    YailList result = GeometryUtil.pointsListToYailList(Arrays.asList(
        new GeoPoint(0.0, 0.0), new GeoPoint(1.0, 1.0),
        new GeoPoint(2.0, 0.0)));
    assertNotNull(result);
    assertEquals(3, result.size());
  }

  @Test
  public void testDistanceBetween() {
  }

  @Test
  public void testGetMidpointLineString0() {
    GeoPoint result = GeometryUtil.getMidpoint(new ArrayList<GeoPoint>());
    assertNotNull(result);
    assertEquals(0.0, result.getLatitude(), TOLERANCE);
    assertEquals(0.0, result.getLongitude(), TOLERANCE);
  }

  @Test
  public void testGetMidpointLineString1() {
    GeoPoint point = new GeoPoint(1.0, 1.0);
    GeoPoint result = GeometryUtil.getMidpoint(Collections.singletonList(point));
    assertNotNull(result);
    assertEquals(point.getLatitude(), result.getLatitude(), TOLERANCE);
    assertEquals(point.getLongitude(), result.getLongitude(), TOLERANCE);
  }

  @Test
  public void testGetMidpointLineString2() {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    points.add(new GeoPoint(0.0, 0.0));
    points.add(new GeoPoint(0.0, 2.0));
    GeoPoint result = GeometryUtil.getMidpoint(points);
    assertNotNull(result);
    assertEquals(0.0, result.getLatitude(), TOLERANCE);
    assertEquals(1.0, result.getLongitude(), TOLERANCE);
  }

  @Test
  public void testGetMidpointLineStringPositive() {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    points.add(new GeoPoint(0.0, 0.0));
    points.add(new GeoPoint(0.0, 3.0));
    points.add(new GeoPoint(0.0, 4.0));
    GeoPoint result = GeometryUtil.getMidpoint(points);
    assertNotNull(result);
    assertEquals(0.0, result.getLatitude(), TOLERANCE);
    assertEquals(2.0, result.getLongitude(), TOLERANCE);
  }

  @Test
  public void testGetMidpointLineStringNegative() {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    points.add(new GeoPoint(0.0, 0.0));
    points.add(new GeoPoint(0.0, 1.0));
    points.add(new GeoPoint(0.0, 4.0));
    GeoPoint result = GeometryUtil.getMidpoint(points);
    assertNotNull(result);
    assertEquals(0.0, result.getLatitude(), TOLERANCE);
    assertEquals(2.0, result.getLongitude(), TOLERANCE);
  }

  @Test
  public void testGetMidpointLineStringExact() {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    points.add(new GeoPoint(0.0, 0.0));
    points.add(new GeoPoint(0.0, 2.0));
    points.add(new GeoPoint(0.0, 4.0));
    GeoPoint result = GeometryUtil.getMidpoint(points);
    assertNotNull(result);
    assertEquals(0.0, result.getLatitude(), TOLERANCE);
    assertEquals(2.0, result.getLongitude(), TOLERANCE);
  }

  @Test
  public void testGetMidpointLineStringEven() {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    points.add(new GeoPoint(0.0, 0.0));
    points.add(new GeoPoint(0.0, 2.0));
    points.add(new GeoPoint(0.0, 3.0));
    points.add(new GeoPoint(0.0, 4.0));
    GeoPoint result = GeometryUtil.getMidpoint(points);
    assertNotNull(result);
    assertEquals(0.0, result.getLatitude(), TOLERANCE);
    assertEquals(2.0, result.getLongitude(), TOLERANCE);
  }

  @Test
  public void testGetMidpointLineStringOdd() {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    points.add(new GeoPoint(0.0, 0.0));
    points.add(new GeoPoint(0.0, 1.0));
    points.add(new GeoPoint(0.0, 2.0));
    points.add(new GeoPoint(0.0, 4.0));
    GeoPoint result = GeometryUtil.getMidpoint(points);
    assertNotNull(result);
    assertEquals(0.0, result.getLatitude(), TOLERANCE);
    assertEquals(2.0, result.getLongitude(), TOLERANCE);
  }

  @Test
  public void testGetMidpointReversible() {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    points.add(new GeoPoint(0.0, 0.0));
    points.add(new GeoPoint(0.0, 1.0));
    points.add(new GeoPoint(0.0, 2.0));
    points.add(new GeoPoint(0.0, 4.0));
    GeoPoint result1 = GeometryUtil.getMidpoint(points);
    Collections.reverse(points);
    GeoPoint result2 = GeometryUtil.getMidpoint(points);
    assertEquals(result1.getLatitude(), result2.getLatitude(), TOLERANCE);
    assertEquals(result1.getLongitude(), result2.getLongitude(), TOLERANCE);
  }

  @Test
  public void testPolygonCentroid() {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    points.add(new GeoPoint(0.0038, -0.1884));
    points.add(new GeoPoint(0.2652, 0.1335));
    points.add(new GeoPoint(-0.1458, 0.1960));
    points.add(new GeoPoint(0.0038, -0.1884));
    GeoPoint result = GeometryUtil.getCentroid(Collections.singletonList(points), null);
    assertNotNull(result);
    // Answer computed with QGIS 2.18.9
    assertPointEquals(0.041, 0.047, result);
  }

  @Test
  public void testEdgeDistanceBetweenCircumscribedCircles() {
    MapCircle circle1 = PowerMock.createMock(MapCircle.class);
    MapCircle circle2 = PowerMock.createMock(MapCircle.class);
    expect(circle1.getCentroid()).andReturn(new GeoPoint(0.0, 0.0));
    expect(circle1.Radius()).andReturn(1.0);
    expect(circle2.getCentroid()).andReturn(new GeoPoint(0.0, 0.0));
    expect(circle2.Radius()).andReturn(2.0);
    PowerMock.replayAll();
    assertEquals(0.0, GeometryUtil.distanceBetweenEdges(circle1, circle2), TOLERANCE);
    PowerMock.verifyAll();
  }

  @Test
  public void testEdgeDistanceBetweenCircles() {
    MapCircle circle1 = PowerMock.createMock(MapCircle.class);
    MapCircle circle2 = PowerMock.createMock(MapCircle.class);
    expect(circle1.getCentroid()).andReturn(new GeoPoint(0.0, 0.0));
    expect(circle1.Radius()).andReturn(1.0 /* meters */);
    expect(circle2.getCentroid()).andReturn(new GeoPoint(0.0, 1.0));
    expect(circle2.Radius()).andReturn(1.0 /* meters */);
    PowerMock.replayAll();
    final double answer = GeometryUtil.EARTH_RADIUS * DEGTORAD - 2.0;
    assertEquals(answer, GeometryUtil.distanceBetweenEdges(circle1, circle2), answer * P_TOLERANCE);
    PowerMock.verifyAll();
  }

  @Test
  public void testIsValidLatitude() {
    assertTrue(isValidLatitude(0.0));
    assertTrue(isValidLatitude(-90.0));
    assertTrue(isValidLatitude(90.0));
    assertFalse(isValidLatitude(-90.0 - 1E-13));
    assertFalse(isValidLatitude(90.0 + 1E-13));
    assertFalse(isValidLatitude(Double.NaN));
    assertFalse(isValidLatitude(Double.POSITIVE_INFINITY));
    assertFalse(isValidLatitude(Double.NEGATIVE_INFINITY));
  }

  @Test
  public void testIsValidLongitude() {
    assertTrue(isValidLongitude(0.0));
    assertTrue(isValidLongitude(-180.0));
    assertTrue(isValidLongitude(180.0));
    assertFalse(isValidLongitude(-180.0 - 1E-13));
    assertFalse(isValidLongitude(180.0 + 1E-13));
    assertFalse(isValidLongitude(Double.NaN));
    assertFalse(isValidLongitude(Double.POSITIVE_INFINITY));
    assertFalse(isValidLongitude(Double.NEGATIVE_INFINITY));
  }

  @Test(expected = DispatchableError.class)
  public void testPointFromYailListTooFewDimensions() {
    GeometryUtil.pointFromYailList(YailList.makeEmptyList());
  }

  @Test(expected = DispatchableError.class)
  public void testPointFromYailListBadValues() {
    GeometryUtil.pointFromYailList(YailList.makeList(new Object[] { "foo", "bar" }));
  }

  @Test(expected = DispatchableError.class)
  public void testPointFromYailListOutOfBounds() {
    GeometryUtil.pointFromYailList(YailList.makeList(new Object[] { 9000., 9000. }));
  }

  @Test
  public void testPointFromYailList() {
    assertPointEquals(1.0, -1.0, GeometryUtil.pointFromYailList(YailList.makeList(Arrays.asList(1.0, -1.0))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateGeometryPolgonNull() {
    GeometryUtil.createGeometry(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateGeometryPolygonHolesWrongArity() {
    List<List<GeoPoint>> points = new ArrayList<List<GeoPoint>>();
    points.add(new ArrayList<GeoPoint>());
    points.add(new ArrayList<GeoPoint>());
    List<List<List<GeoPoint>>> holes = new ArrayList<List<List<GeoPoint>>>();
    holes.add(new ArrayList<List<GeoPoint>>());
    GeometryUtil.createGeometry(points, holes);
  }

  @Test
  public void testCreateGeometryPolygonWithHoles() {
    List<List<GeoPoint>> points = new ArrayList<List<GeoPoint>>();
    points.add(Arrays.asList(
        new GeoPoint(1.0, 1.0),
        new GeoPoint(1.0, 0.0),
        new GeoPoint(0.0, 0.0),
        new GeoPoint(0.0, 1.0)
    ));
    List<List<List<GeoPoint>>> holes = new ArrayList<List<List<GeoPoint>>>();
    holes.add(Collections.singletonList(Arrays.asList(
        new GeoPoint(0.5, 0.5),
        new GeoPoint(0.5, 0.0),
        new GeoPoint(0.0, 0.5)
    )));
    Geometry geo = GeometryUtil.createGeometry(points, holes);
    assertEquals(1, geo.getNumGeometries());
    assertEquals(9, geo.getNumPoints());
  }

  @Test
  public void testCreateGeometryMultipolygonWithHoles() {
    List<List<GeoPoint>> points = new ArrayList<List<GeoPoint>>();
    points.add(Arrays.asList(
        new GeoPoint(1.0, 1.0),
        new GeoPoint(1.0, 0.0),
        new GeoPoint(0.0, 0.0),
        new GeoPoint(0.0, 1.0)
    ));
    points.add(Arrays.asList(
        new GeoPoint(-1.0, -1.0),
        new GeoPoint(-1.0, 0.0),
        new GeoPoint(0.0, 0.0),
        new GeoPoint(0.0, -1.0)
    ));
    List<List<List<GeoPoint>>> holes = new ArrayList<List<List<GeoPoint>>>();
    holes.add(Collections.singletonList(Arrays.asList(
        new GeoPoint(0.5, 0.5),
        new GeoPoint(0.5, 0.0),
        new GeoPoint(0.0, 0.5)
    )));
    holes.add(new ArrayList<List<GeoPoint>>());
    Geometry geo = GeometryUtil.createGeometry(points, holes);
    assertEquals(2, geo.getNumGeometries());
    assertEquals(14, geo.getNumPoints());
  }

  @Test(expected = IterationError.class)
  public void testMultiPolygonHolesFromYailList() {
    GeometryUtil.multiPolygonHolesFromYailList(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] {
            YailList.makeList(new Object[] {
                9000, 9000
            })
        })
    }));
  }

  @Test
  public void testIsPolygonFailsOnDispatchableError() {
    assertFalse(GeometryUtil.isPolygon(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] {
            9000, 9000
        }),
        YailList.makeEmptyList(),
        YailList.makeEmptyList()
    })));
  }

  @Test
  public void testIsMultiPolygonNoData() {
    assertFalse(GeometryUtil.isMultiPolygon(YailList.makeEmptyList()));
  }

  @Test
  public void testTexas() {
    YailList points = YailList.makeList(Collections.singletonList(YailList.makeList(new Object[] {
        GeometryUtil.asYailList(new GeoPoint(25.97433779000005, -97.22591847699988)),
        GeometryUtil.asYailList(new GeoPoint(25.973665612, -97.23090873599989)),
        GeometryUtil.asYailList(new GeoPoint(25.97380364100013, -97.23508409199985))
    })));
    assertTrue(isMultiPolygon(points));
  }

  /**
   * Tests coverage of the "constructor".
   *
   * It would be great if JaCoCo/SonarQube understood that private constructors aren't callable
   * and therefore shouldn't count against code coverage. However, that isn't the case. To ensure
   * that when we introduce new code the private constructors don't appear, we use reflection to
   * access the private construct and simulate what the results of the code analysis ought to
   * report.
   *
   * @throws Exception if the constructor doesn't exist
   */
  @Test
  public void evilTestPrivateConstructor() throws Exception {
    Constructor<GeometryUtil> constructor = GeometryUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertNotNull(constructor.newInstance());
  }

  private void assertPointEquals(double expectedLat, double expectedLong, GeoPoint point) {
    assertNotNull(point);
    assertEquals(expectedLat, point.getLatitude(), expectedLat * P_TOLERANCE);
    assertEquals(expectedLong, point.getLongitude(), expectedLat * P_TOLERANCE);
  }
}
