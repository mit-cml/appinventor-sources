// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.DispatchableError;
import com.google.appinventor.components.runtime.errors.IterationError;
import org.json.JSONArray;
import org.json.JSONException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.osmdroid.api.IGeoPoint;

import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * GeometryUtil provides computational utilities to support centroid, distance, and area
 * calculations used for Map and related components.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
@SuppressWarnings("WeakerAccess")
public final class GeometryUtil {

  public static final double EARTH_RADIUS = 6378137.0;
  public static final double ONE_DEG_IN_METERS = EARTH_RADIUS * Math.PI / 180.0;
  public static final int WEB_MERCATOR_SRID = 4326;
  private static final GeometryFactory FACTORY =
      new GeometryFactory(new PrecisionModel(), WEB_MERCATOR_SRID);

  private GeometryUtil() {
  }

  public static double coerceToDouble(Object o) {
    if (o instanceof Number) {
      return ((Number) o).doubleValue();
    } else  {
      try {
        return Double.parseDouble(o.toString());
      } catch(NumberFormatException e) {
        return Double.NaN;
      }
    }
  }

  public static GeoPoint coerceToPoint(Object lat, Object lng) {
    double latitude = coerceToDouble(lat);
    double longitude = coerceToDouble(lng);
    if (Double.isNaN(latitude)) {
      throw new IllegalArgumentException("Latitude must be a numeric.");
    } else if (Double.isNaN(longitude)) {
      throw new IllegalArgumentException("Longitude must be a numeric.");
    } else if (latitude < -90.0 || latitude > 90.0) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90.");
    } else if (longitude < -180.0 || longitude > 180.0) {
      throw new IllegalArgumentException("Longitude must be between -180 and 180.");
    } else {
      return new GeoPoint(latitude, longitude);
    }
  }

  public static YailList asYailList(IGeoPoint point) {
    return YailList.makeList(new Object[] { point.getLatitude(), point.getLongitude() });
  }

  public static YailList pointsListToYailList(List<? extends IGeoPoint> points) {
    List<YailList> entries = new ArrayList<YailList>();
    for (IGeoPoint point : points) {
      entries.add(asYailList(point));
    }
    return YailList.makeList(entries);
  }

  public static GeoPoint pointFromYailList(YailList point) {
    if (point.length() < 3) {
      throw new DispatchableError(ErrorMessages.ERROR_INVALID_NUMBER_OF_VALUES_IN_POINT,
          2, point.length() - 1);
    }
    try {
      return GeometryUtil.coerceToPoint(point.get(1), point.get(2));
    } catch(IllegalArgumentException e) {
      throw new DispatchableError(ErrorMessages.ERROR_INVALID_POINT, point.get(1), point.get(2));
    }
  }

  public static List<GeoPoint> pointsFromYailList(YailList points) {
    List<GeoPoint> newPoints = new ArrayList<GeoPoint>();
    Iterator<?> it = points.iterator();
    int i = 1;
    it.next();  // skip *list* at index 0
    while (it.hasNext()) {
      try {
        newPoints.add(pointFromYailList(TypeUtil.castNotNull(it.next(), YailList.class, "list")));
        i++;
      } catch(DispatchableError e) {
        throw IterationError.fromError(i, e);
      }
    }
    return newPoints;
  }

  public static Geometry createGeometry(GeoPoint point) {
    return FACTORY.createPoint(geoPointToCoordinate(point));
  }

  public static Geometry createGeometry(List<GeoPoint> line) {
    return FACTORY.createLineString(pointsToCoordinates(line));
  }

  public static Geometry createGeometry(double north, double east, double south, double west) {
    return FACTORY.createPolygon(new Coordinate[]{
       new Coordinate(east, north),
       new Coordinate(east, south),
       new Coordinate(west, south),
       new Coordinate(west, north),
       new Coordinate(east, north)
    });
  }

  public static Geometry createGeometry(List<List<GeoPoint>> points, List<List<List<GeoPoint>>> holes) {
    if (points == null) {
      throw new IllegalArgumentException("points must not be null.");
    }
    if (holes != null && !holes.isEmpty() && holes.size() != points.size()) {
      throw new IllegalArgumentException("holes must either be null or the same length as points.");
    }
    Polygon[] polygons = new Polygon[points.size()];
    int i = 0;
    if (holes == null || holes.isEmpty()) {
      for (List<GeoPoint> ring : points) {
        polygons[i++] = ringToPolygon(ring);
      }
    } else {
      Iterator<List<GeoPoint>> ip = points.iterator();
      Iterator<List<List<GeoPoint>>> jp = holes.iterator();
      while (ip.hasNext()) {
        polygons[i++] = ringToPolygon(ip.next(), jp.next());
      }
    }
    return polygons.length == 1 ? polygons[0] : FACTORY.createMultiPolygon(polygons);
  }

  public static GeoPoint getMidpoint(List<GeoPoint> points) {
    if (points.isEmpty()) {
      // no points?
      return new GeoPoint(0.0, 0.0);
    } else if (points.size() == 1) {
      return new GeoPoint(points.get(0));
    }
    LineString ls = FACTORY.createLineString(pointsToCoordinates(points));
    return jtsPointToGeoPoint(ls.getCentroid());
  }

  /**
   * Computes the centroid of a multipolygon. Note that the centroid may not be contained in the
   * multipolygon.
   *
   * @param points A list of lists of points composing the multipolygon.
   * @param holes  A list of lists of lists of points indicating holes in the multipolygon. The
   *               <i>i</i>th list of lists of points is a list of polygons indicating holes in the
   *               <code>i</code>th polygon in <code>points</code>.
   * @return a GeoPoint indicating the centroid of the multipolygon described by
   *         <code>points</code> and <code>holes</code>.
   * @throws IllegalArgumentException if <code>points</code> is null or <code>holes</code> is not
   *                                  null and it does not have the same number of elements as
   *                                  <code>points</code>.
   */
  public static GeoPoint getCentroid(List<List<GeoPoint>> points, List<List<List<GeoPoint>>> holes) {
    return jtsPointToGeoPoint(createGeometry(points, holes).getCentroid());
  }

  public static Polygon ringToPolygon(List<GeoPoint> ring) {
    return FACTORY.createPolygon(geoPointsToLinearRing(ring));
  }

  public static Coordinate[] pointsToCoordinates(List<GeoPoint> points) {
    boolean closed = points.get(0).equals(points.get(points.size() - 1));
    Coordinate[] coordinates = new Coordinate[points.size() + (closed ? 0 : 1)];
    int i = 0;
    for (GeoPoint p : points) {
      coordinates[i++] = geoPointToCoordinate(p);
    }
    if (!closed) {
      coordinates[i] = coordinates[0];
    }
    return coordinates;
  }

  public static LinearRing geoPointsToLinearRing(List<GeoPoint> points) {
    return FACTORY.createLinearRing(pointsToCoordinates(points));
  }

  public static Polygon ringToPolygon(List<GeoPoint> ring, List<List<GeoPoint>> holes) {
    LinearRing shell = geoPointsToLinearRing(ring);
    LinearRing[] holeRings = new LinearRing[holes.size()];
    int i = 0;
    for (List<GeoPoint> h : holes) {
      holeRings[i++] = geoPointsToLinearRing(h);
    }
    return FACTORY.createPolygon(shell, holeRings);
  }

  public static GeoPoint jtsPointToGeoPoint(Point p) {
    return new GeoPoint(p.getY(), p.getX());
  }

  public static Coordinate geoPointToCoordinate(GeoPoint p) {
    return new Coordinate(p.getLongitude(), p.getLatitude());
  }

  public static double distanceBetween(IGeoPoint a, IGeoPoint b) {
    double lat1 = Math.toRadians(a.getLatitude());
    double lng1 = Math.toRadians(a.getLongitude());
    double lat2 = Math.toRadians(b.getLatitude());
    double lng2 = Math.toRadians(b.getLongitude());
    double dLat = lat2 - lat1;
    double dLng = lng2 - lng1;
    double cordlen = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLng / 2), 2);
    double angle = 2 * Math.atan2(Math.sqrt(cordlen), Math.sqrt(1 - cordlen));
    return EARTH_RADIUS * angle;
  }

  public static double distanceBetween(MapMarker marker, GeoPoint point) {
    return distanceBetween(marker.getLocation(), point);
  }

  public static double distanceBetween(MapMarker marker1, MapMarker marker2) {
    return distanceBetween(marker1.getLocation(), marker2.getLocation());
  }

  public static double distanceBetweenEdges(MapMarker marker, MapLineString lineString) {
    return ONE_DEG_IN_METERS * marker.getGeometry().distance(lineString.getGeometry());
  }

  public static double distanceBetweenEdges(MapMarker marker, MapPolygon polygon) {
    return ONE_DEG_IN_METERS * marker.getGeometry().distance(polygon.getGeometry());
  }

  public static double distanceBetweenEdges(MapMarker marker, MapCircle circle) {
    double d = marker.getCentroid().distanceTo(circle.getCentroid()) - circle.Radius();
    return d < 0 ? 0 : d;
  }

  public static double distanceBetweenEdges(MapMarker marker, MapRectangle rectangle) {
    return ONE_DEG_IN_METERS * marker.getGeometry().distance(rectangle.getGeometry());
  }

  public static double distanceBetweenEdges(MapLineString lineString, GeoPoint point) {
    return ONE_DEG_IN_METERS * lineString.getGeometry().distance(createGeometry(point));
  }

  public static double distanceBetweenEdges(MapLineString lineString1, MapLineString lineString2) {
    return ONE_DEG_IN_METERS * lineString1.getGeometry().distance(lineString2.getGeometry());
  }

  public static double distanceBetweenEdges(MapLineString lineString, MapPolygon polygon) {
    return ONE_DEG_IN_METERS * lineString.getGeometry().distance(polygon.getGeometry());
  }

  public static double distanceBetweenEdges(MapLineString lineString, MapCircle circle) {
    double d = (ONE_DEG_IN_METERS * lineString.getGeometry().distance(createGeometry(circle.getCentroid()))) - circle.Radius();
    return d < 0 ? 0 : d;
  }

  public static double distanceBetweenEdges(MapLineString lineString, MapRectangle rectangle) {
    return ONE_DEG_IN_METERS * lineString.getGeometry().distance(rectangle.getGeometry());
  }

  public static double distanceBetweenEdges(MapPolygon polygon, GeoPoint point) {
    return ONE_DEG_IN_METERS * polygon.getGeometry().distance(createGeometry(point));
  }

  public static double distanceBetweenEdges(MapPolygon polygon1, MapPolygon polygon2) {
    return ONE_DEG_IN_METERS * polygon1.getGeometry().distance(polygon2.getGeometry());
  }

  public static double distanceBetweenEdges(MapPolygon polygon, MapCircle circle) {
    double d = (ONE_DEG_IN_METERS * polygon.getGeometry().distance(createGeometry(circle.getCentroid()))) - circle.Radius();
    return d < 0 ? 0 : d;
  }

  public static double distanceBetweenEdges(MapPolygon polygon, MapRectangle rectangle) {
    return ONE_DEG_IN_METERS * polygon.getGeometry().distance(rectangle.getGeometry());
  }

  public static double distanceBetweenEdges(MapCircle circle, GeoPoint point) {
    double d = distanceBetween(circle.getCentroid(), point) - circle.Radius();
    return d < 0 ? 0 : d;
  }

  public static double distanceBetweenEdges(MapCircle circle1, MapCircle circle2) {
    double d = distanceBetween(circle1.getCentroid(), circle2.getCentroid()) - circle1.Radius() - circle2.Radius();
    return d < 0 ? 0 : d;
  }

  public static double distanceBetweenEdges(MapCircle circle, MapRectangle rectangle) {
    double d = (ONE_DEG_IN_METERS * rectangle.getGeometry().distance(createGeometry(circle.getCentroid()))) - circle.Radius();
    return d < 0 ? 0 : d;
  }

  public static double distanceBetweenEdges(MapRectangle rectangle, GeoPoint point) {
    return ONE_DEG_IN_METERS * rectangle.getGeometry().distance(createGeometry(point));
  }

  public static double distanceBetweenEdges(MapRectangle rectangle1, MapRectangle rectangle2) {
    return ONE_DEG_IN_METERS * rectangle1.getGeometry().distance(rectangle2.getGeometry());
  }

  public static double distanceBetweenCentroids(MapMarker marker, MapLineString lineString) {
    return distanceBetween(marker.getCentroid(), lineString.getCentroid());
  }

  public static double distanceBetweenCentroids(MapMarker marker, MapPolygon polygon) {
    return distanceBetween(marker.getCentroid(), polygon.getCentroid());
  }

  public static double distanceBetweenCentroids(MapMarker marker, MapCircle circle) {
    return distanceBetween(marker.getCentroid(), circle.getCentroid());
  }

  public static double distanceBetweenCentroids(MapMarker marker, MapRectangle rectangle) {
    return distanceBetween(marker.getCentroid(), rectangle.getCentroid());
  }

  public static double distanceBetweenCentroids(MapLineString lineString, GeoPoint point) {
    return distanceBetween(lineString.getCentroid(), point);
  }

  public static double distanceBetweenCentroids(MapLineString lineString1, MapLineString lineString2) {
    return distanceBetween(lineString1.getCentroid(), lineString2.getCentroid());
  }

  public static double distanceBetweenCentroids(MapLineString lineString, MapPolygon polygon) {
    return distanceBetween(lineString.getCentroid(), polygon.getCentroid());
  }

  public static double distanceBetweenCentroids(MapLineString lineString, MapCircle circle) {
    return distanceBetween(lineString.getCentroid(), circle.getCentroid());
  }

  public static double distanceBetweenCentroids(MapLineString lineString, MapRectangle rectangle) {
    return distanceBetween(lineString.getCentroid(), rectangle.getCentroid());
  }

  public static double distanceBetweenCentroids(MapPolygon polygon, GeoPoint point) {
    return distanceBetween(polygon.getCentroid(), point);
  }

  public static double distanceBetweenCentroids(MapPolygon polygon1, MapPolygon polygon2) {
    return distanceBetween(polygon1.getCentroid(), polygon2.getCentroid());
  }

  public static double distanceBetweenCentroids(MapPolygon polygon, MapCircle circle) {
    return distanceBetween(polygon.getCentroid(), circle.getCentroid());
  }

  public static double distanceBetweenCentroids(MapPolygon polygon, MapRectangle rectangle) {
    return distanceBetween(polygon.getCentroid(), rectangle.getCentroid());
  }

  public static double distanceBetweenCentroids(MapCircle circle, GeoPoint point) {
    return distanceBetween(circle.getCentroid(), point);
  }

  public static double distanceBetweenCentroids(MapCircle circle1, MapCircle circle2) {
    return distanceBetween(circle1.getCentroid(), circle2.getCentroid());
  }

  public static double distanceBetweenCentroids(MapCircle circle, MapRectangle rectangle) {
    return distanceBetween(circle.getCentroid(), rectangle.getCentroid());
  }

  public static double distanceBetweenCentroids(MapRectangle rectangle, GeoPoint point) {
    return distanceBetween(rectangle.getCentroid(), point);
  }

  public static double distanceBetweenCentroids(MapRectangle rectangle1, MapRectangle rectangle2) {
    return distanceBetween(rectangle1.getCentroid(), rectangle2.getCentroid());
  }

  public static double bearingTo(MapMarker from, MapMarker to) {
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  public static double bearingToEdge(MapMarker from, MapLineString to) {
    // TODO(ewpatton): Switch to using distance to nearest edge from centroid
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  public static double bearingToEdge(MapMarker from, MapPolygon to) {
    // TODO(ewpatton): Switch to using distance to nearest edge from centroid
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  public static double bearingToEdge(MapMarker from, MapRectangle to) {
    // TODO(ewpatton): Switch to using distance to nearest edge from centroid
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  public static double bearingToEdge(MapMarker from, MapCircle to) {
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  public static double bearingToCentroid(MapMarker from, MapLineString to) {
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  public static double bearingToCentroid(MapMarker from, MapPolygon to) {
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  public static double bearingToCentroid(MapMarker from, MapRectangle to) {
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  public static double bearingToCentroid(MapMarker from, MapCircle to) {
    return from.getCentroid().bearingTo(to.getCentroid());
  }

  /**
   * Tests whether the given double value is a valid latitude.
   *
   * @param latitude a value to test
   * @return  true if the latitude is in [-90, 90], otherwise false.
   */
  public static boolean isValidLatitude(double latitude) {
    return -90.0 <= latitude && latitude <= 90.0;
  }

  /**
   * Tests whether the given double value is a valid longitude.
   *
   * @param longitude a value to test
   * @return  true if the longitude is in [-180, 180], otherwise false.
   */
  public static boolean isValidLongitude(double longitude) {
    return -180.0 <= longitude && longitude <= 180.0;
  }

  public static List<GeoPoint> polygonToList(JSONArray array) throws JSONException {
    List<GeoPoint> points = new ArrayList<GeoPoint>(array.length());
    if (array.length() < 3) {
      throw new DispatchableError(ErrorMessages.ERROR_POLYGON_PARSE_ERROR,
          "Too few points in Polygon, expected 3.");
    }
    for (int i = 0; i < array.length(); i++) {
      JSONArray point = array.getJSONArray(i);
      if (point.length() < 2) {
        throw new JSONException("Invalid number of dimensions in polygon, expected 2.");
      }
      if (point.length() == 2) {
        points.add(new GeoPoint(point.getDouble(0), point.getDouble(1)));
      } else {
        points.add(new GeoPoint(point.getDouble(0), point.getDouble(1), point.getDouble(2)));
      }
    }
    return points;
  }

  public static List<List<GeoPoint>> multiPolygonToList(JSONArray array) throws JSONException {
    List<List<GeoPoint>> result = new ArrayList<List<GeoPoint>>();
    if (array.length() == 0) {
      return result;
    } else if (array.getJSONArray(0).optJSONArray(0) == null) {
      result.add(polygonToList(array));
    } else {
      for (int i = 0; i < array.length(); i++) {
        result.add(polygonToList(array.getJSONArray(i)));
      }
    }
    return result;
  }

  public static YailList multiPolygonToYailList(List<List<GeoPoint>> multipolygon) {
    List<YailList> result = new LinkedList<YailList>();
    for (List<GeoPoint> polygon : multipolygon) {
      result.add(pointsListToYailList(polygon));
    }
    return YailList.makeList(result);
  }

  public static List<List<GeoPoint>> multiPolygonFromYailList(YailList list) {
    List<List<GeoPoint>> multipolygon = new ArrayList<List<GeoPoint>>();
    Iterator<?> it = list.listIterator(1);
    while(it.hasNext()) {
      multipolygon.add(pointsFromYailList(TypeUtil.castNotNull(it.next(), YailList.class, "list")));
    }
    return multipolygon;
  }

  public static List<List<List<GeoPoint>>> multiPolygonHolesFromYailList(YailList points) {
    List<List<List<GeoPoint>>> holes = new ArrayList<List<List<GeoPoint>>>();
    Iterator<?> it = points.listIterator(1);  // skip *list* symbol
    int i = 1;
    try {
      while (it.hasNext()) {
        YailList yailHoles = TypeUtil.castNotNull(it.next(), YailList.class, "list");
        holes.add(multiPolygonFromYailList(yailHoles));  // holes are just negative multipolygon
        i++;
      }
    } catch (DispatchableError e) {
      throw IterationError.fromError(i, e);
    }
    return holes;
  }

  public static List<List<List<GeoPoint>>> multiPolygonHolesToList(JSONArray array) throws JSONException {
    List<List<List<GeoPoint>>> result = new ArrayList<List<List<GeoPoint>>>();
    if (array.getJSONArray(0).getJSONArray(0).optJSONArray(0) == null) {
      result.add(multiPolygonToList(array));
    } else {
      for (int i = 0; i < array.length(); i++) {
        result.add(multiPolygonToList(array.getJSONArray(i)));
      }
    }
    return result;
  }

  /**
   * Heuristic to determine whether or not to process the list <code>points</code> as a single
   * polygon.
   *
   * @see #isMultiPolygon(YailList) for a heuristic that tests whether a list is a multipolygon.
   * @param points A YailList potentially containing a sequence of lat-long coordinate pairs.
   * @return true if the YailList looks like it could be a coordinate list, otherwise false.
   */
  public static boolean isPolygon(YailList points) {
    if (points.size() < 3) {
      return false;
    }
    try {
      pointFromYailList(TypeUtil.castNotNull(points.get(1), YailList.class, "list"));
      return true;
    } catch(DispatchableError e) {
      return false;
    }
  }

  /**
   * Heuristic to determine whether or not to process the list <code>points</code> as a
   * multipolygon.
   *
   * @see #isPolygon(YailList) for a heuristic that tests whether a list is a single polygon.
   * @param points A YailList potentially containing a sequence of sequences of lat-long coordinate
   *               pairs.
   * @return true if the YailList looks like it could be a multipolygon, otherwise false.
   */
  public static boolean isMultiPolygon(YailList points) {
    return points.size() > 0 &&
        isPolygon(TypeUtil.castNotNull(points.get(1), YailList.class, "list"));
  }
}
