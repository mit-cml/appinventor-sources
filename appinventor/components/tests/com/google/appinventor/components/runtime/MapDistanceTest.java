// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.YailList;
import org.junit.Ignore;
import org.junit.Test;

import static com.google.appinventor.components.runtime.util.GeometryUtil.ONE_DEG_IN_METERS;
import static org.junit.Assert.assertEquals;

public class MapDistanceTest extends MapTestBase {

  @Test
  public void testDistanceMarkerToPoint() {
    Marker marker = new Marker(getMap());
    assertEquals(ONE_DEG_IN_METERS, marker.DistanceToPoint(0.0, 1.0), M_TOL);
  }

  @Test
  public void testDistanceMarkerToMarker() {
    Marker marker = new Marker(getMap());
    Marker marker2 = new Marker(getMap());
    marker2.SetLocation(0.0, 1.0);
    assertEquals(ONE_DEG_IN_METERS, marker.DistanceToFeature(marker2, false), M_TOL);
  }

  @Test
  public void testDistanceMarkerToLineStringOnLine() {
    Marker marker = new Marker(getMap());
    LineString line = MapTestBase.makeLineString(getMap(), 1.0, 0.0, -1.0, 0.0);
    assertDistance(marker, line, 0.0, 0.0);
  }

  @Test
  public void testDistanceMarkerToLineStringOffLinePerpendicular() {
    Marker marker = new Marker(getMap());
    LineString line = MapTestBase.makeLineString(getMap(), 1.0, 1.0, -1.0, 1.0);
    assertDistance(marker, line, ONE_DEG_IN_METERS, ONE_DEG_IN_METERS);
  }

  @Test
  public void testDistanceMarkerToLineStringOffLineParallel() {
    Marker marker = new Marker(getMap());
    LineString line = MapTestBase.makeLineString(getMap(), 0.0, 1.0, 0.0, 3.0);
    assertDistance(marker, line, 2 * ONE_DEG_IN_METERS, ONE_DEG_IN_METERS);
  }

  @Test
  public void testDistanceMarkerToPolygonInside() {
    Marker marker = new Marker(getMap());
    Polygon polygon = makePolygon(getMap(), 1.0, -1.0, -1.0, 1.0);
    assertDistance(marker, polygon, 0.0, 0.0);
  }

  @Test
  public void testDistanceMarkerToPolygonOnEdge() {
    Marker marker = new Marker(getMap());
    Polygon polygon = makePolygon(getMap(), 1.0, 0.0, -1.0, 2.0);
    assertDistance(marker, polygon, ONE_DEG_IN_METERS, 0.0);
  }

  @Test
  public void testDistanceMarkerToPolygonOutside() {
    Marker marker = new Marker(getMap());
    Polygon polygon = makePolygon(getMap(), 1.0, 1.0, -1.0, 3.0);
    assertDistance(marker, polygon, 2 * ONE_DEG_IN_METERS, ONE_DEG_IN_METERS);
  }

  @Test
  public void testDistanceMarkerToRectangleInside() {
    Marker marker = new Marker(getMap());
    Rectangle rect = makeRectangle(getMap(), 1.0, -1.0, -1.0, 1.0);
    assertDistance(marker, rect, 0.0, 0.0);
  }

  @Test
  public void testDistanceMarkerToRectangleOnEdge() {
    Marker marker = new Marker(getMap());
    Rectangle rect = makeRectangle(getMap(), 1.0, 0.0, -1.0, 2.0);
    assertDistance(marker, rect, ONE_DEG_IN_METERS, 0.0);
  }

  @Test
  public void testDistanceMarkerToRectangleOutside() {
    Marker marker = new Marker(getMap());
    Rectangle rect = makeRectangle(getMap(), 1.0, 1.0, -1.0, 3.0);
    assertDistance(marker, rect, 2 * ONE_DEG_IN_METERS, ONE_DEG_IN_METERS);
  }

  @Test
  public void testDistanceMarkerToCircleInside() {
    Marker marker = new Marker(getMap());
    Circle circle = makeCircle(getMap(), 0.0, 0.0, ONE_DEG_IN_METERS);
    assertDistance(marker, circle, 0.0, 0.0);
  }

  @Test
  public void testDistanceMarkerToCircleOnEdge() {
    Marker marker = new Marker(getMap());
    Circle circle = makeCircle(getMap(), 0.0, 1.0, ONE_DEG_IN_METERS);
    assertDistance(marker, circle, ONE_DEG_IN_METERS, 0.0);
  }

  @Test
  public void testDistanceMarkerToCircleOutside() {
    Marker marker = new Marker(getMap());
    Circle circle = makeCircle(getMap(), 0.0, 2.0, ONE_DEG_IN_METERS);
    assertDistance(marker, circle, 2 * ONE_DEG_IN_METERS, ONE_DEG_IN_METERS);
  }

  @Test
  public void testDistanceLineStringToMidpoint() {
    LineString line = defaultLineNS(new LineString(getMap()));
    assertEquals(0.0, line.DistanceToPoint(0.0, 0.0, true), M_TOL);
    assertEquals(0.0, line.DistanceToPoint(0.0, 0.0, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToPointOnLine() {
    LineString line = defaultLineEW(new LineString(getMap()));
    assertEquals(0.5 * ONE_DEG_IN_METERS, line.DistanceToPoint(0.0, 0.5, true), M_TOL);
    assertEquals(0.0, line.DistanceToPoint(0.0, 0.5, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToPointOffLine() {
    LineString line = defaultLineNS(new LineString(getMap()));
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToPoint(0.0, 1.0, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToPoint(0.0, 1.0, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToMarker() {
    LineString line = defaultLineEW(new LineString(getMap()));
    Marker marker = MarkerTest.createMarker(getMap(), 0.0, 1.0);
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToFeature(marker, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(marker, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToLineStringCollinear() {
    LineString line = defaultLineEW(new LineString(getMap()));
    LineString line2 = defaultLineEW(new LineString(getMap()));
    assertEquals(0.0, line.DistanceToFeature(line2, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(line2, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToLineStringParallel() {
    LineString line = defaultLineNS(new LineString(getMap()));
    LineString line2 = makeLineString(getMap(), 1.0, 1.0, -1.0, 1.0);
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToFeature(line2, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToFeature(line2, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToLineStringBisect() {
    LineString line = defaultLineNS(new LineString(getMap()));
    LineString line2 = new LineString(getMap());
    defaultLineEW(line2);
    assertEquals(0.0, line.DistanceToFeature(line2, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(line2, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToPolygonInside() {
    LineString line = defaultLineNS(new LineString(getMap()));
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    assertEquals(0.0, line.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(polygon, false), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(line, true), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(line, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToPolygonCrossing() {
    LineString line = makeLineString(getMap(), 2.0, 0.0, -2.0, 0.0);
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    assertEquals(0.0, line.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(polygon, false), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(line, true), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(line, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToPolygonEdge() {
    LineString line = makeLineString(getMap(), 1.0, 1.0, -1.0, 1.0);
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(polygon, false), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, polygon.DistanceToFeature(line, true), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(line, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToPolygonOutside() {
    LineString line = makeLineString(getMap(), 1.0, 2.0, -1.0, 2.0);
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    assertEquals(2 * ONE_DEG_IN_METERS, line.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToFeature(polygon, false), M_TOL);
    assertEquals(2 * ONE_DEG_IN_METERS, polygon.DistanceToFeature(line, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, polygon.DistanceToFeature(line, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToCircleInside() {
    LineString line = defaultLineNS(new LineString(getMap()));
    Circle circle = defaultCircle(new Circle(getMap()));
    assertEquals(0.0, line.DistanceToFeature(circle, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(circle, false), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(line, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(line, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToCircleCrossing() {
    LineString line = makeLineString(getMap(), 2.0, 0.0, -2.0, 0.0);
    Circle circle = defaultCircle(new Circle(getMap()));
    assertEquals(0.0, line.DistanceToFeature(circle, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(circle, false), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(line, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(line, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToCircleTangent() {
    LineString line = makeLineString(getMap(), 1.0, 1.0, -1.0, 1.0);
    Circle circle = defaultCircle(new Circle(getMap()));
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToFeature(circle, true), M_TOL);
    assertEquals(0.0, line.DistanceToFeature(circle, false), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, circle.DistanceToFeature(line, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(line, false), M_TOL);
  }

  @Test
  public void testDistanceLineStringToCircleOutside() {
    LineString line = makeLineString(getMap(), 1.0, 2.0, -1.0, 2.0);
    Circle circle = defaultCircle(new Circle(getMap()));
    assertEquals(2 * ONE_DEG_IN_METERS, line.DistanceToFeature(circle, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, line.DistanceToFeature(circle, false), M_TOL);
    assertEquals(2 * ONE_DEG_IN_METERS, circle.DistanceToFeature(line, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, circle.DistanceToFeature(line, false), M_TOL);
  }

  @Test
  public void testDistancePolygonToPoint() {
    Polygon polygon = new Polygon(getMap());
    polygon.PointsFromString("[[-1, -1], [1, -1], [1, 1], [-1, 1]]");
    assertEquals(0.0, polygon.DistanceToPoint(0.0, 0.0, false), M_TOL);
    assertEquals(0.0, polygon.DistanceToPoint(0.0, 0.0, true), M_TOL);
  }

  @Test
  public void testDistancePolygonToPolygon() {
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    assertEquals(0.0, polygon.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(polygon, false), M_TOL);
  }

  @Test
  public void testDistancePolygonToCircleInside() {
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    Circle circle = defaultCircle(new Circle(getMap()));
    assertDistance(polygon, circle, 0.0, 0.0);
  }

  @Test
  public void testDistancePolygonToCircleCircumscribed() {
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    Circle circle = makeCircle(getMap(), 0.0, 0.0, Math.sqrt(2) * ONE_DEG_IN_METERS);
    assertDistance(polygon, circle, 0.0, 0.0);
  }

  @Test
  public void testDistancePolygonToCircleInsideDifferentCenter() {
    Polygon polygon = makePolygon(getMap(), 1.0, -2.0, -1.0, 2.0);
    Circle circle = makeCircle(getMap(), 0.0, 1.0, ONE_DEG_IN_METERS);
    assertDistance(polygon, circle, ONE_DEG_IN_METERS, 0.0);
  }

  @Test
  public void testDistancePolygonToCircleOverlap() {
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    Circle circle = makeCircle(getMap(), 0.0, 1.0, ONE_DEG_IN_METERS);
    assertDistance(polygon, circle, ONE_DEG_IN_METERS, 0.0);
  }

  @Test
  public void testDistancePolygonToCircleTangent() {
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    Circle circle = makeCircle(getMap(), 0.0, 2.0, ONE_DEG_IN_METERS);
    assertDistance(polygon, circle, 2 * ONE_DEG_IN_METERS, 0.0);
  }

  @Test
  public void testDistancePolygonToCircleOutside() {
    Polygon polygon = defaultPolygon(new Polygon(getMap()));
    Circle circle = makeCircle(getMap(), 0.0, 3.0, ONE_DEG_IN_METERS);
    assertDistance(polygon, circle, 3 * ONE_DEG_IN_METERS, ONE_DEG_IN_METERS);
  }

  /**
   * Tests the rectangle's distance to point computation.
   */
  @Test
  public void testDistanceRectToPoint() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    assertEquals(0.0, rect.DistanceToPoint(0.0, 0.0, true), M_TOL);
    // A point in or on the rectangle should yield 0.0 when centroids = false
    assertEquals(0.0, rect.DistanceToPoint(0.0, 1.0, false), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, rect.DistanceToPoint(0.0, 1.0, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, rect.DistanceToPoint(0.0, 2.0, false), M_TOL);
  }

  /**
   * Tests that the distance from a rectangle to a marker at its center is 0.0 meters.
   */
  @Test
  public void testDistanceRectToMarkerInside() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Marker marker = new Marker(getMap());
    marker.SetLocation(0.0, 0.0);
    assertEquals(0.0, rect.DistanceToFeature(marker, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(marker, false), M_TOL);
  }

  /**
   * Tests that the distance between the rectangle and a marker on its edge is half the length of
   * the rectangle in meters when centroids is true, and 0 when centroids is false.
   */
  @Test
  public void testDistanceRectToMarkerEdge() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Marker marker = new Marker(getMap());
    marker.SetLocation(0.0, 1.0);
    assertEquals(ONE_DEG_IN_METERS, rect.DistanceToFeature(marker, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(marker, false), M_TOL);
  }

  /**
   * Tests that the distance is either two degrees (centroids = true) or one degree
   * (centroids = false) if the marker is 1 degree longitude east of the rectangle.
   */
  @Test
  public void testDistanceRectToMarkerOutside() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Marker marker = new Marker(getMap());
    marker.SetLocation(0.0, 2.0);
    assertEquals(2 * ONE_DEG_IN_METERS, rect.DistanceToFeature(marker, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, rect.DistanceToFeature(marker, false), M_TOL);
  }

  /**
   * Tests that the distance to a line string contained within the rectangle and whose midpoint is
   * the center of the rectangle will be 0.0 meters from the rectangle regardless of the
   * centroid property.
   */
  @Test
  public void testDistanceRectToLineStringInside() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    LineString line = makeLineString(getMap(), 0, -0.5, 0, 0.5);
    assertDistance(rect, line, 0.0, 0.0);
  }

  @Test
  public void testDistanceRectToLineStringCrosses() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    LineString line = makeLineString(getMap(), 0, -2, 0, 2);
    assertDistance(rect, line, 0.0, 0.0);
  }

  @Test
  public void testDistanceRectToLineStringEdge() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    LineString line = makeLineString(getMap(), 0.5, 1, -0.5, 1);
    assertDistance(rect, line, ONE_DEG_IN_METERS, 0.0);
  }

  @Test
  public void testDistanceRectToLineStringOutside() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    LineString line = makeLineString(getMap(), 1, 2, -1, 2);
    assertDistance(rect, line, 2 * ONE_DEG_IN_METERS, ONE_DEG_IN_METERS);
  }

  @Test
  public void testDistanceRectToPolygonInside() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Polygon polygon = new Polygon(getMap());
    polygon.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] {  0.5,  0.5 }),
        YailList.makeList(new Object[] {  0.5, -0.5 }),
        YailList.makeList(new Object[] { -0.5, -0.5 }),
        YailList.makeList(new Object[] { -0.5,  0.5 })
    }));
    assertEquals(0.0, rect.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(polygon, false), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(rect, false), M_TOL);
  }

  @Test
  public void testDistanceRectToPolygonIntersect() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Polygon polygon = new Polygon(getMap());
    polygon.Points(YailList.makeList(new Object[] {
        YailList.makeList(new Object[] {  1.0, 0.5 }),
        YailList.makeList(new Object[] {  1.0, 1.5 }),
        YailList.makeList(new Object[] { -1.0, 1.5 }),
        YailList.makeList(new Object[] { -1.0, 0.5 })
    }));
    assertEquals(ONE_DEG_IN_METERS, rect.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(polygon, false), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, polygon.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(rect, false), M_TOL);
  }

  @Test
  public void testDistanceRectToPolygonTangent() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Polygon polygon = makePolygon(getMap(), 1.0, 1.0, -1.0, 3.0);
    assertEquals(2 * ONE_DEG_IN_METERS, rect.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(polygon, false), M_TOL);
    assertEquals(2 * ONE_DEG_IN_METERS, polygon.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, polygon.DistanceToFeature(rect, false), M_TOL);
  }

  @Test
  public void testDistanceRectToPolygonOutside() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Polygon polygon = makePolygon(getMap(), 1.0, 2.0, -1.0, 4.0);
    assertEquals(3 * ONE_DEG_IN_METERS, rect.DistanceToFeature(polygon, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, rect.DistanceToFeature(polygon, false), M_TOL);
    assertEquals(3 * ONE_DEG_IN_METERS, polygon.DistanceToFeature(rect, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, polygon.DistanceToFeature(rect, false), M_TOL);
  }

  @Test
  public void testDistanceRectToRect() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    assertEquals(0.0, rect.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(rect, false), M_TOL);
  }

  /**
   * Tests that the distance between a rectangle and a circle inscribed within is 0 meters,
   * regardless of the centroids parameter.
   */
  @Test
  public void testDistanceRectToCircleInscribed() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Circle circle = new Circle(getMap());
    circle.SetLocation(0.0, 0.0);
    circle.Radius(ONE_DEG_IN_METERS);
    assertEquals(0.0, rect.DistanceToFeature(circle, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(circle, false), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(rect, false), M_TOL);
  }

  /**
   * Tests
   */
  @Test
  public void testDistanceRectToCircleCircumscribed() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Circle circle = makeCircle(getMap(), 0.0, 0.0, Math.sqrt(2) * ONE_DEG_IN_METERS);
    assertEquals(0.0, rect.DistanceToFeature(circle, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(circle, false), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(rect, false), M_TOL);
  }

  @Test
  public void testDistanceRectToCircleIntersect() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Circle circle = makeCircle(getMap(), 0.0, 1.0, ONE_DEG_IN_METERS);
    assertEquals(ONE_DEG_IN_METERS, rect.DistanceToFeature(circle, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(circle, false), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, circle.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(rect, false), M_TOL);
  }

  @Test
  public void testDistanceRectToCircleTouch() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Circle circle = makeCircle(getMap(), 0.0, 2.0, ONE_DEG_IN_METERS);
    assertEquals(2 * ONE_DEG_IN_METERS, rect.DistanceToFeature(circle, true), M_TOL);
    assertEquals(0.0, rect.DistanceToFeature(circle, false), M_TOL);
    assertEquals(2 * ONE_DEG_IN_METERS, circle.DistanceToFeature(rect, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(rect, false), M_TOL);
  }

  @Test
  public void testDistanceRectToCircleOutside() {
    Rectangle rect = defaultRectangle(new Rectangle(getMap()));
    Circle circle = makeCircle(getMap(), 0.0, 3.0, ONE_DEG_IN_METERS);
    assertEquals(3 * ONE_DEG_IN_METERS, rect.DistanceToFeature(circle, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, rect.DistanceToFeature(circle, false), M_TOL);
    assertEquals(3 * ONE_DEG_IN_METERS, circle.DistanceToFeature(rect, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, circle.DistanceToFeature(rect, false), M_TOL);
  }

  @Test
  public void testDistanceCircleToPointInside() {
    Circle circle = defaultCircle(new Circle(getMap()));
    assertEquals(0.0, circle.DistanceToPoint(0.0, 0.0, true), M_TOL);
    assertEquals(0.0, circle.DistanceToPoint(0.0, 0.0, false), M_TOL);
  }

  @Test
  public void testDistanceCircleToPointEdge() {
    Circle circle = defaultCircle(new Circle(getMap()));
    assertEquals(1 * ONE_DEG_IN_METERS, circle.DistanceToPoint(0.0, 1.0, true), M_TOL);
    assertEquals(0.0, circle.DistanceToPoint(0.0, 1.0, false), M_TOL);
  }

  @Test
  public void testDistanceCircleToPointOutside() {
    Circle circle = defaultCircle(new Circle(getMap()));
    assertEquals(2 * ONE_DEG_IN_METERS, circle.DistanceToPoint(0.0, 2.0, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, circle.DistanceToPoint(0.0, 2.0, false), M_TOL);
  }

  @Test
  public void testDistanceCircleToCircleConcentric() {
    Circle circle = defaultCircle(new Circle(getMap()));
    Circle circle2 = new Circle(getMap());
    circle2.Radius(2 * ONE_DEG_IN_METERS);
    assertEquals(0.0, circle.DistanceToFeature(circle2, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(circle2, false), M_TOL);
  }

  @Test
  public void testDistanceCircleToCircleInside() {
    Circle circle = defaultCircle(new Circle(getMap()));
    Circle circle2 = new Circle(getMap());
    circle2.SetLocation(0.0, 0.5);
    circle2.Radius(0.5 * ONE_DEG_IN_METERS);
    assertEquals(0.5 * ONE_DEG_IN_METERS, circle.DistanceToFeature(circle2, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(circle2, false), M_TOL);
  }

  @Test
  public void testDistanceCircleToCircleIntersecting() {
    Circle circle = defaultCircle(new Circle(getMap()));
    Circle circle2 = new Circle(getMap());
    circle2.SetLocation(0.0, 2.0);
    circle2.Radius(2.0 * ONE_DEG_IN_METERS);
    assertEquals(2.0 * ONE_DEG_IN_METERS, circle.DistanceToFeature(circle2, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(circle2, false), M_TOL);
  }

  @Test
  public void testDistanceCircleToCircleTangent() {
    Circle circle = defaultCircle(new Circle(getMap()));
    Circle circle2 = new Circle(getMap());
    circle2.SetLocation(0.0, 2.0);
    circle2.Radius(ONE_DEG_IN_METERS);
    assertEquals(2.0 * ONE_DEG_IN_METERS, circle.DistanceToFeature(circle2, true), M_TOL);
    assertEquals(0.0, circle.DistanceToFeature(circle2, false), M_TOL);
  }

  @Test
  public void testDistanceCircleToCircleOutside() {
    Circle circle = defaultCircle(new Circle(getMap()));
    Circle circle2 = new Circle(getMap());
    circle2.SetLocation(0.0, 3.0);
    circle2.Radius(ONE_DEG_IN_METERS);
    assertEquals(3.0 * ONE_DEG_IN_METERS, circle.DistanceToFeature(circle2, true), M_TOL);
    assertEquals(ONE_DEG_IN_METERS, circle.DistanceToFeature(circle2, false), M_TOL);
  }

  @Test
  public void testBearingToPoint() {
    Marker marker = new Marker(getMap());
    // due north
    assertEquals(0.0, marker.BearingToPoint(1.0, 0.0), DEG_TOL);
    // due east
    assertEquals(90.0, marker.BearingToPoint(0.0, 1.0), DEG_TOL);
    // due south
    assertEquals(180.0, marker.BearingToPoint(-1.0, 0.0), DEG_TOL);
    // due west
    assertEquals(270.0, marker.BearingToPoint(0.0, -1.0), DEG_TOL);
  }

  @Test
  public void testBearingToMarker() {
    Marker marker = new Marker(getMap());
    Marker marker2 = new Marker(getMap());
    // due north
    marker2.SetLocation(1.0, 0.0);
    assertEquals(0.0, marker.BearingToFeature(marker2, true), DEG_TOL);
    // due east
    marker2.SetLocation(0.0, 1.0);
    assertEquals(90.0, marker.BearingToFeature(marker2, true), DEG_TOL);
    // due south
    marker2.SetLocation(-1.0, 0.0);
    assertEquals(180.0, marker.BearingToFeature(marker2, true), DEG_TOL);
    // due west
    marker2.SetLocation(0.0, -1.0);
    assertEquals(270.0, marker.BearingToFeature(marker2, true), DEG_TOL);
  }

  @Test
  public void testBearingToLineString() {
    Marker marker = new Marker(getMap());
    LineString line;
    // due north
    line = MapTestBase.makeLineString(getMap(), 1.0, -1.0, 1.0, 1.0);
    assertEquals(0.0, marker.BearingToFeature(line, true), DEG_TOL);
    assertEquals(0.0, marker.BearingToFeature(line, false), DEG_TOL);
    // due east
    line = MapTestBase.makeLineString(getMap(), 1.0, 1.0, -1.0, 1.0);
    assertEquals(90.0, marker.BearingToFeature(line, true), DEG_TOL);
    assertEquals(90.0, marker.BearingToFeature(line, false), DEG_TOL);
    // due south
    line = MapTestBase.makeLineString(getMap(), -1.0, -1.0, -1.0, 1.0);
    assertEquals(180.0, marker.BearingToFeature(line, true), DEG_TOL);
    assertEquals(180.0, marker.BearingToFeature(line, false), DEG_TOL);
    // due west
    line = MapTestBase.makeLineString(getMap(), 1.0, -1.0, -1.0, -1.0);
    assertEquals(270.0, marker.BearingToFeature(line, true), DEG_TOL);
    assertEquals(270.0, marker.BearingToFeature(line, false), DEG_TOL);
  }

  @Test
  public void testBearingToPolygon() {
    Marker marker = new Marker(getMap());
    Polygon polygon;
    // due north
    polygon = MapTestBase.makePolygon(getMap(), 2.0, -1.0, 1.0, 1.0);
    assertEquals(0.0, marker.BearingToFeature(polygon, true), DEG_TOL);
    assertEquals(0.0, marker.BearingToFeature(polygon, false), DEG_TOL);
    // due east
    polygon = MapTestBase.makePolygon(getMap(), 1.0, 1.0, -1.0, 2.0);
    assertEquals(90.0, marker.BearingToFeature(polygon, true), DEG_TOL);
    assertEquals(90.0, marker.BearingToFeature(polygon, false), DEG_TOL);
    // due south
    polygon = MapTestBase.makePolygon(getMap(), -1.0, -1.0, -2.0, 1.0);
    assertEquals(180.0, marker.BearingToFeature(polygon, true), DEG_TOL);
    assertEquals(180.0, marker.BearingToFeature(polygon, false), DEG_TOL);
    // due west
    polygon = MapTestBase.makePolygon(getMap(), 1.0, -2.0, -1.0, -1.0);
    assertEquals(270.0, marker.BearingToFeature(polygon, true), DEG_TOL);
    assertEquals(270.0, marker.BearingToFeature(polygon, false), DEG_TOL);
  }

  @Test
  public void testBearingToRectangle() {
    Marker marker = new Marker(getMap());
    Rectangle rect;
    // due north
    rect = MapTestBase.makeRectangle(getMap(), 2.0, -1.0, 1.0, 1.0);
    assertEquals(0.0, marker.BearingToFeature(rect, true), DEG_TOL);
    assertEquals(0.0, marker.BearingToFeature(rect, false), DEG_TOL);
    // due east
    rect = MapTestBase.makeRectangle(getMap(), 1.0, 1.0, -1.0, 2.0);
    assertEquals(90.0, marker.BearingToFeature(rect, true), DEG_TOL);
    assertEquals(90.0, marker.BearingToFeature(rect, false), DEG_TOL);
    // due south
    rect = MapTestBase.makeRectangle(getMap(), -1.0, -1.0, -2.0, 1.0);
    assertEquals(180.0, marker.BearingToFeature(rect, true), DEG_TOL);
    assertEquals(180.0, marker.BearingToFeature(rect, false), DEG_TOL);
    // due west
    rect = MapTestBase.makeRectangle(getMap(), 1.0, -2.0, -1.0, -1.0);
    assertEquals(270.0, marker.BearingToFeature(rect, true), DEG_TOL);
    assertEquals(270.0, marker.BearingToFeature(rect, false), DEG_TOL);
  }

  @Test
  public void testBearingToCircle() {
    Marker marker = new Marker(getMap());
    Circle circle;
    // due north
    circle = MapTestBase.makeCircle(getMap(), 1.0, 0.0, 1.0);
    assertEquals(0.0, marker.BearingToFeature(circle, true), DEG_TOL);
    assertEquals(0.0, marker.BearingToFeature(circle, false), DEG_TOL);
    // due east
    circle = MapTestBase.makeCircle(getMap(), 0.0, 1.0, 1.0);
    assertEquals(90.0, marker.BearingToFeature(circle, true), DEG_TOL);
    assertEquals(90.0, marker.BearingToFeature(circle, false), DEG_TOL);
    // due south
    circle = MapTestBase.makeCircle(getMap(), -1.0, 0.0, 1.0);
    assertEquals(180.0, marker.BearingToFeature(circle, true), DEG_TOL);
    assertEquals(180.0, marker.BearingToFeature(circle, false), DEG_TOL);
    // due west
    circle = MapTestBase.makeCircle(getMap(), 0.0, -1.0, 1.0);
    assertEquals(270.0, marker.BearingToFeature(circle, true), DEG_TOL);
    assertEquals(270.0, marker.BearingToFeature(circle, false), DEG_TOL);
  }

  @Test
  public void testDistanceToNull() {
    Marker marker = new Marker(getMap());
    assertEquals(-1.0, marker.DistanceToFeature(null, true), 0.0);
  }

  @Test
  public void testBearingToNull() {
    Marker marker = new Marker(getMap());
    assertEquals(-1.0, marker.BearingToFeature(null, true), 0.0);
  }

  public void assertDistance(MapFeatureBase a, MapFeatureBase b, double dCentroid, double dEdge) {
    assertEquals(dCentroid, a.DistanceToFeature(b, true), M_TOL);
    assertEquals(dCentroid, b.DistanceToFeature(a, true), M_TOL);
    assertEquals(dEdge, a.DistanceToFeature(b, false), M_TOL);
    assertEquals(dEdge, b.DistanceToFeature(a, false), M_TOL);
  }

}
