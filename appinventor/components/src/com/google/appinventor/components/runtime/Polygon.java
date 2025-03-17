// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.MapFeature;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.errors.DispatchableError;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureVisitor;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.util.GeoPoint;

/**
 * `Polygon` encloses an arbitrary 2-dimensional area on a {@link Map}. `Polygon`s can be used for
 * drawing a perimeter, such as a campus, city, or country. `Polygon`s begin as basic triangles.
 * New vertices can be created by dragging the midpoint of a polygon away from the edge. Clicking
 * on a vertex will remove the vertex, but a minimum of 3 vertices must exist at all times.
 */
@DesignerComponent(version = YaVersion.POLYGON_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "Polygon encloses an arbitrary 2-dimensional area on a Map. Polygons can be used for drawing a perimeter, such as a campus, city, or country. Polygons begin as basic triangles. New vertices can be created by dragging the midpoint of a polygon away from the edge. Clicking on a vertex will remove the vertex, but a minimum of 3 vertices must exist at all times.",
    iconName = "images/polygon.png")
@SimpleObject
public class Polygon extends PolygonBase implements MapPolygon {
  private static final String TAG = Polygon.class.getSimpleName();

  private List<List<GeoPoint>> points = new ArrayList<List<GeoPoint>>();
  private List<List<List<GeoPoint>>> holePoints = new ArrayList<List<List<GeoPoint>>>();
  private boolean multipolygon = false;
  private boolean initialized = false;

  private static final MapFeatureVisitor<Double> distanceComputation = new MapFeatureVisitor<Double>() {
    @Override
    public Double visit(MapMarker marker, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(marker, (Polygon) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(marker, (Polygon) arguments[0]);
      }
    }

    @Override
    public Double visit(MapLineString lineString, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(lineString, (Polygon) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(lineString, (Polygon) arguments[0]);
      }
    }

    @Override
    public Double visit(MapPolygon polygon, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(polygon, (Polygon) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(polygon, (Polygon) arguments[0]);
      }
    }

    @Override
    public Double visit(MapCircle circle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((Polygon) arguments[0], circle);
      } else {
        return GeometryUtil.distanceBetweenEdges((Polygon) arguments[0], circle);
      }
    }

    @Override
    public Double visit(MapRectangle rectangle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((Polygon) arguments[0], rectangle);
      } else {
        return GeometryUtil.distanceBetweenEdges((Polygon) arguments[0], rectangle);
      }
    }
  };

  public Polygon(MapFactory.MapFeatureContainer container) {
    super(container, distanceComputation);
    container.addFeature(this);
  }

  public void Initialize() {
    initialized = true;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
    map.getController().updateFeatureHoles(this);
    map.getController().updateFeatureText(this);
  }

  @Override
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns the type of the feature. For polygons, this returns "
          + "MapFeature.Polygon (\"Polygon\").")
  public @Options(MapFeature.class) String Type() {
    return TypeAbstract().toUnderlyingValue();
  }

  /**
   * Gets the type of the feature, as a {@link MapFeature} enum.
   *
   * @return the abstract MapFeature type of this feature. In this case MapFeature.Polygon.
   */
  @SuppressWarnings("RegularMethodName")
  public MapFeature TypeAbstract() {
    return MapFeature.Polygon;
  }

  @Override
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets or sets the sequence of points used to draw the polygon.")
  public YailList Points() {
    if (points.isEmpty()) {
      return YailList.makeEmptyList();
    } else if (multipolygon) {
      // Return a 2-deep list of points for multipolygons
      List<YailList> result = new LinkedList<YailList>();
      for (List<GeoPoint> part : points) {
        result.add(GeometryUtil.pointsListToYailList(part));
      }
      return YailList.makeList(result);
    } else {
      // Return a 1-deep list of points for simple polygons
      return GeometryUtil.pointsListToYailList(points.get(0));
    }
  }

  /**
   * Specifies the Points used for drawing the `Polygon`. The Points are specified as a list of
   * lists containing latitude and longitude values, such as `[[lat1, long1], [lat2, long2], ...]`.
   *
   * @param points a list of points defining the polygon.
   */
  @Override
  @SimpleProperty
  public void Points(YailList points) {
    try {
      if (GeometryUtil.isPolygon(points)) {
        multipolygon = false;
        this.points.clear();
        this.points.add(GeometryUtil.pointsFromYailList(points));
      } else if (GeometryUtil.isMultiPolygon(points)) {
        multipolygon = true;
        this.points = GeometryUtil.multiPolygonFromYailList(points);
      } else {
        throw new DispatchableError(ErrorMessages.ERROR_POLYGON_PARSE_ERROR,
            "Unable to determine the structure of the points argument.");
      }
      if (initialized) {
        clearGeometry();
        map.getController().updateFeaturePosition(this);
      }
    } catch(DispatchableError e) {
      container.$form().dispatchErrorOccurredEvent(this, "Points", e.getErrorCode(), e.getArguments());
    }
  }

  /**
   * Specifies the points for the `Polygon` from a GeoJSON string. Unlike {@link #Points(YailList)},
   * this property expects that the longitude comes first in the point rather than the latitude.
   *
   * @param pointString
   */
  @SuppressWarnings("squid:S00100")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA)
  @SimpleProperty(description = "Constructs a polygon from the given list of coordinates.",
      category = PropertyCategory.APPEARANCE)
  public void PointsFromString(String pointString) {
    if (TextUtils.isEmpty(pointString)) {
      points = new ArrayList<List<GeoPoint>>();  // create a new list in case the user has saved a reference
      map.getController().updateFeaturePosition(this);
      return;
    }
    try {
      JSONArray content = new JSONArray(pointString);
      if (content.length() == 0) {
        points = new ArrayList<List<GeoPoint>>();  // create a new list in case the user has saved a reference
        multipolygon = false;
        map.getController().updateFeaturePosition(this);
        return;
      }
      points = GeometryUtil.multiPolygonToList(content);
      multipolygon = points.size() > 1;
      if (initialized) {
        clearGeometry();
        map.getController().updateFeaturePosition(this);
      }
    } catch(JSONException e) {
      container.$form().dispatchErrorOccurredEvent(this, "PointsFromString",
          ErrorMessages.ERROR_POLYGON_PARSE_ERROR, e.getMessage());
    } catch(DispatchableError e) {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "PointsFromString",
          e.getErrorCode(), e.getArguments());
    }
  }

  @Override
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets or sets the sequence of points used to draw holes in the polygon.")
  public YailList HolePoints() {
    if (holePoints.isEmpty()) {
      return YailList.makeEmptyList();
    } else if (multipolygon) {
      List<YailList> result = new LinkedList<YailList>();
      for (List<List<GeoPoint>> polyholes : holePoints) {
        result.add(GeometryUtil.multiPolygonToYailList(polyholes));
      }
      return YailList.makeList(result);
    } else {
      return GeometryUtil.multiPolygonToYailList(holePoints.get(0));
    }
  }

  /**
   * Specifies the points of any holes in the `Polygon`. The `HolePoints` property is a list of
   * lists, with each sublist containing `(latitude, longitude)` points representing a hole.
   *
   * @param points a list of lists of lists defining the holes in each part of the multipolygon.
   */
  @Override
  @SimpleProperty
  public void HolePoints(YailList points) {
    try {
      if (points.size() == 0) {
        holePoints = new ArrayList<List<List<GeoPoint>>>();
      } else if (multipolygon) {
        this.holePoints = GeometryUtil.multiPolygonHolesFromYailList(points);
      } else if (GeometryUtil.isMultiPolygon(points)) {
        List<List<List<GeoPoint>>> holes = new ArrayList<List<List<GeoPoint>>>();
        holes.add(GeometryUtil.multiPolygonFromYailList(points));
        this.holePoints = holes;
      } else {
        throw new DispatchableError(ErrorMessages.ERROR_POLYGON_PARSE_ERROR,
            "Unable to determine the structure of the points argument.");
      }
      if (initialized) {
        clearGeometry();
        map.getController().updateFeatureHoles(this);
      }
    } catch(DispatchableError e) {
      container.$form().dispatchErrorOccurredEvent(this, "HolePoints",
          e.getErrorCode(), e.getArguments());
    }
  }

  /**
   * Specifies holes in a `Polygon`from a GeoJSON string. In contrast to
   * {@link #HolePoints(YailList)}, the longitude of each point comes before the latitude as
   * stated in the GeoJSON specification.
   *
   * @param pointString
   */
  @SuppressWarnings("squid:S00100")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA)
  @SimpleProperty(description = "Constructs holes in a polygon from a given list of coordinates per hole.",
      category = PropertyCategory.APPEARANCE)
  public void HolePointsFromString(String pointString) {
    if (TextUtils.isEmpty(pointString)) {
      holePoints = new ArrayList<List<List<GeoPoint>>>();  // create a new list in case the user has saved a reference
      map.getController().updateFeatureHoles(this);
      return;
    }
    try {
      JSONArray content = new JSONArray(pointString);
      if (content.length() == 0) {
        holePoints = new ArrayList<List<List<GeoPoint>>>();  // create a new list in case the user has saved a reference
        map.getController().updateFeatureHoles(this);
        return;
      }
      holePoints = GeometryUtil.multiPolygonHolesToList(content);
      if (initialized) {
        clearGeometry();
        map.getController().updateFeatureHoles(this);
      }
      Log.d(TAG, "Points: " + points);
    } catch(JSONException e) {
      Log.e(TAG, "Unable to parse point string", e);
      container.$form().dispatchErrorOccurredEvent(this, "HolePointsFromString",
          ErrorMessages.ERROR_POLYGON_PARSE_ERROR, e.getMessage());
    }
  }

  /**
   * Gets the centroid of the `Polygon` as a `(latitude, longitude)` pair.
   */
  @Override
  @SimpleFunction(description = "Returns the centroid of the Polygon as a (latitude, longitude) pair.")
  public YailList Centroid() {
    return super.Centroid();
  }

  @Override
  public List<List<GeoPoint>> getPoints() {
    return points;
  }

  @Override
  public List<List<List<GeoPoint>>> getHolePoints() {
    return holePoints;
  }

  @Override
  public <T> T accept(MapFeatureVisitor<T> visitor, Object... arguments) {
    return visitor.visit(this, arguments);
  }

  @Override
  protected Geometry computeGeometry() {
    return GeometryUtil.createGeometry(points, holePoints);
  }

  @Override
  public void updatePoints(List<List<GeoPoint>> points) {
    this.points.clear();
    this.points.addAll(points);
    clearGeometry();
  }

  @Override
  public void updateHolePoints(List<List<List<GeoPoint>>> points) {
    this.holePoints.clear();
    this.holePoints.addAll(points);
    clearGeometry();
  }

  @VisibleForTesting
  boolean isInitialized() {
    return initialized;
  }
}
