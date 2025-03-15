// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2020 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLatitude;
import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLongitude;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
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
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.util.GeoPoint;

/**
 * `LineString` is a component for drawing an open, continuous sequence of lines on a `Map`. To add
 * new points to a `LineString` in the designer, drag the midpoint of any segment away from the
 * line to introduce a new vertex. Move a vertex by clicking and dragging the vertex to a new
 * location. Clicking on a vertex will delete the vertex, unless only two remain.
 */
@DesignerComponent(version = YaVersion.LINESTRING_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "LineString is a component for drawing an open, continuous sequence of lines on a Map. To add new points to a LineString in the designer, drag the midpoint of any segment away from the line to introduce a new vertex. Move a vertex by clicking and dragging the vertex to a new location. Clicking on a vertex will delete the vertex, unless only two remain.",
    iconName = "images/linestring.png")
@SimpleObject
public class LineString extends MapFeatureBase implements MapLineString {
  private static final String TAG = LineString.class.getSimpleName();
  private List<GeoPoint> points = new ArrayList<GeoPoint>();

  private static final MapFeatureVisitor<Double> distanceComputation = new MapFeatureVisitor<Double>() {
    @Override
    public Double visit(MapMarker marker, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(marker, (LineString) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(marker, (LineString) arguments[0]);
      }
    }

    @Override
    public Double visit(MapLineString lineString, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(lineString, (LineString) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(lineString, (LineString) arguments[0]);
      }
    }

    @Override
    public Double visit(MapPolygon polygon, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((LineString) arguments[0], polygon);
      } else {
        return GeometryUtil.distanceBetweenEdges((LineString) arguments[0], polygon);
      }
    }

    @Override
    public Double visit(MapCircle circle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((LineString) arguments[0], circle);
      } else {
        return GeometryUtil.distanceBetweenEdges((LineString) arguments[0], circle);
      }
    }

    @Override
    public Double visit(MapRectangle rectangle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((LineString) arguments[0], rectangle);
      } else {
        return GeometryUtil.distanceBetweenEdges((LineString) arguments[0], rectangle);
      }
    }
  };

  public LineString(MapFactory.MapFeatureContainer container) {
    super(container, distanceComputation);
    StrokeWidth(3);
    container.addFeature(this);
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns the type of the map feature. For LineString, this returns "
          + "MapFeature.LineString (\"LineString\").")
  @Override
  public @Options(MapFeature.class) String Type() {
    return TypeAbstract().toUnderlyingValue();
  }

  /**
   * Gets the type of this feature, as a {@link MapFeature}.
   *
   * @return the abstract MapFeature type of this feature. In this case MapFeature.LineString.
   */
  @SuppressWarnings("RegularMethodName")
  public MapFeature TypeAbstract() {
    return MapFeature.LineString;
  }

  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "A list of latitude and longitude pairs that represent the line segments " +
      "of the polyline.")
  @Override
  public YailList Points() {
    return GeometryUtil.pointsListToYailList(points);
  }

  /**
   * The list of points, as pairs of latitudes and longitudes, in the `LineString`.
   * @param points the new coordinates for the LineString.
   */
  @SimpleProperty
  @Override
  public void Points(@NonNull YailList points) {
    if (points.size() < 2) {
      container.$form().dispatchErrorOccurredEvent(this, "Points",
          ErrorMessages.ERROR_LINESTRING_TOO_FEW_POINTS, points.length() - 1);
    } else {
      try {
        this.points = GeometryUtil.pointsFromYailList(points);
        clearGeometry();
        map.getController().updateFeaturePosition(this);
      } catch (DispatchableError e) {
        container.$form().dispatchErrorOccurredEvent(this, "Points",
            e.getErrorCode(), e.getArguments());
      }
    }
  }

  /**
   * Set the points of the LineString from a specially-coded character string of the form:
   * [[latitude1, longitude1], [latitude2, longitude2], ...]
   *
   * @param points String containing a sequence of points for the LineString.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void PointsFromString(String points) {
    final String functionName = "PointsFromString";
    try {
      List<GeoPoint> geopoints = new ArrayList<GeoPoint>();
      JSONArray array = new JSONArray(points);
      if (array.length() < 2) {
        // Need at least two points
        throw new DispatchableError(ErrorMessages.ERROR_LINESTRING_TOO_FEW_POINTS, array.length());
      }
      int length = array.length();
      for (int i = 0; i < length; ++i) {
        JSONArray point = array.optJSONArray(i);
        if (point == null) {
          throw new DispatchableError(ErrorMessages.ERROR_EXPECTED_ARRAY_AT_INDEX, i,
              array.get(i).toString());
        } else if (point.length() < 2) {
          throw new DispatchableError(ErrorMessages.ERROR_LINESTRING_TOO_FEW_FIELDS, i,
              points.length());
        }
        double latitude = point.optDouble(0, Double.NaN);
        double longitude = point.optDouble(1, Double.NaN);
        if (!isValidLatitude(latitude)) {
          throw new DispatchableError(ErrorMessages.ERROR_INVALID_LATITUDE_IN_POINT_AT_INDEX,
              i, array.get(0).toString());
        } else if (!isValidLongitude(longitude)) {
          throw new DispatchableError(ErrorMessages.ERROR_INVALID_LONGITUDE_IN_POINT_AT_INDEX,
              i, array.get(1).toString());
        }
        geopoints.add(new GeoPoint(latitude, longitude));
      }
      this.points = geopoints;
      clearGeometry();
      map.getController().updateFeaturePosition(this);
    } catch(JSONException e) {
      Log.e(TAG, "Malformed string to LineString.PointsFromString", e);
      container.$form().dispatchErrorOccurredEvent(this, functionName, ErrorMessages.ERROR_LINESTRING_PARSE_ERROR, e.getMessage());
    } catch(DispatchableError e) {
      container.$form().dispatchErrorOccurredEvent(this, functionName, e.getErrorCode(), e.getArguments());
    }
  }

  /**
   * Sets or gets the width of the stroke used to outline the `%type%`.
   * @param width the outline width
   */
  @Override
  @DesignerProperty(defaultValue = "3")
  @SimpleProperty
  public void StrokeWidth(int width) {
    super.StrokeWidth(width);
  }

  @Override
  public List<GeoPoint> getPoints() {
    return points;
  }

  @Override
  public <T> T accept(MapFeatureVisitor<T> visitor, Object... arguments) {
    return visitor.visit(this, arguments);
  }

  @Override
  protected Geometry computeGeometry() {
    return GeometryUtil.createGeometry(points);
  }

  @Override
  public void updatePoints(List<GeoPoint> points) {
    this.points = points;
    clearGeometry();
  }
}
