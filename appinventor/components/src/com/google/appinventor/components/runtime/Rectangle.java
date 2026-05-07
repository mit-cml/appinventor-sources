// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

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
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureVisitor;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;

import org.locationtech.jts.geom.Geometry;
import org.osmdroid.util.GeoPoint;

import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.YailList;

/**
 * `Rectangle`s are polygons with fixed latitudes and longitudes for the north, south, east, and
 * west boundaries. Moving a vertex of the `Rectangle` updates the appropriate edges accordingly.
 */
@DesignerComponent(version = YaVersion.RECTANGLE_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "Rectangles are polygons with fixed latitudes and longitudes for the north, south, east, and west boundaries. Moving a vertex of the Rectangle updates the appropriate edges accordingly.",
    iconName = "images/rectangle.png")
@SimpleObject
public class Rectangle extends PolygonBase implements MapRectangle {
  private double east = 0;
  private double west = 0;
  private double north = 0;
  private double south = 0;

  private static final MapFeatureVisitor<Double> distanceComputation = new MapFeatureVisitor<Double>() {
    @Override
    public Double visit(MapMarker marker, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(marker, (Rectangle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(marker, (Rectangle) arguments[0]);
      }
    }

    @Override
    public Double visit(MapLineString lineString, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(lineString, (Rectangle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(lineString, (Rectangle) arguments[0]);
      }
    }

    @Override
    public Double visit(MapPolygon polygon, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(polygon, (Rectangle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(polygon, (Rectangle) arguments[0]);
      }
    }

    @Override
    public Double visit(MapCircle circle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(circle, (Rectangle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(circle, (Rectangle) arguments[0]);
      }
    }

    @Override
    public Double visit(MapRectangle rectangle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(rectangle, (Rectangle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(rectangle, (Rectangle) arguments[0]);
      }
    }
  };

  public Rectangle(MapFactory.MapFeatureContainer container) {
    super(container, distanceComputation);
    container.addFeature(this);
  }

  @Override
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Returns the type of the feature. For rectangles, this returns "
          + "MapFeature.Rectangle (\"Rectangle\").")
  public @Options(MapFeature.class) String Type() {
    return TypeAbstract().toUnderlyingValue();
  }

  /**
   * Gets the type of the feature, as a {@link MapFeature} enum.
   *
   * @return the abstract MapFeature type of this feature. In this case MapFeature.Rectangle.
   */
  @SuppressWarnings("RegularMethodName")
  public MapFeature TypeAbstract() {
    return MapFeature.Rectangle;
  }

  /**
   * Specifies the east-most edge of the `Rectangle`, in decimal degrees east of the prime meridian.
   *
   * @param east the new east bound of the rectangle, in decimal degrees. Range: [-180, 180]
   */
  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The east edge of the rectangle, in decimal degrees east "
          + "of the prime meridian.")
  public void EastLongitude(double east) {
    this.east = east;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  /**
   * @suppressdoc
   */
  @Override
  @SimpleProperty
  public double EastLongitude() {
    return east;
  }

  /**
   * Specifies the north-most edge of the `Rectangle`, in decimal degrees north of the equator.
   *
   * @param north the new northern bound of the rectangle, in decimal degrees. Range: [-90, 90]
   */
  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The north edge of the rectangle, in decimal degrees north"
          + " of the equator.")
  public void NorthLatitude(double north) {
    this.north = north;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  /**
   * @suppressdoc
   */
  @Override
  @SimpleProperty
  public double NorthLatitude() {
    return north;
  }

  /**
   * Specifies the west-most edge of the `Rectangle`, in decimal degrees east of the prime meridian.
   *
   * @param south the new southern bound of the rectangle, in decimal degrees. Range: [-90, 90]
   */
  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The south edge of the rectangle, in decimal degrees north"
          + " of the equator.")
  public void SouthLatitude(double south) {
    this.south = south;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  /**
   * @suppressdoc
   */
  @Override
  @SimpleProperty
  public double SouthLatitude() {
    return south;
  }

  /**
   * Specifies the south-most edge of the `Rectangle`, in decimal degrees south of the equator.
   *
   * @param west the new western bound of the rectangle, in decimal degrees. Range: [-180, 180]
   */
  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The west edge of the rectangle, in decimal degrees east"
          + " of the equator.")
  public void WestLongitude(double west) {
    this.west = west;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  /**
   * @suppressdoc
   */
  @Override
  @SimpleProperty
  public double WestLongitude() {
    return west;
  }

  /**
   * Returns the center of the `Rectangle` as a list of the form `(Latitude Longitude)`.
   */
  @Override
  @SimpleFunction(description = "Returns the center of the Rectangle as a list of the form " +
      "(Latitude Longitude).")
  public YailList Center() {
    return GeometryUtil.asYailList(getCentroid());
  }

  /**
   * Returns the bounding box of the `Rectangle` in the format `((North West) (South East))`.
   */
  @Override
  @SimpleFunction(description = "Returns the bounding box of the Rectangle in the format " +
      "((North West) (South East)).")
  public YailList Bounds() {
    YailList nw = YailList.makeList(new Double[] { north, west });
    YailList se = YailList.makeList(new Double[] { south, east });
    return YailList.makeList(new YailList[] { nw, se });
  }

  /**
   * Move the `Rectangle` to be centered on the given `latitude` and `longitude`, attempting to keep
   * the width and height (in meters) as equal as possible adjusting for changes in latitude.
   *
   * @param latitude the latitude of the new rectangle center
   * @param longitude the longitude of the new rectangle center.
   */
  @Override
  @SimpleFunction(description = "Moves the Rectangle so that it is centered on the given " +
      "latitude and longitude while attempting to maintain the width and height of the Rectangle " +
      "as measured from the center to the edges.")
  public void SetCenter(double latitude, double longitude) {
    if (latitude < -90 || latitude > 90) {
      container.$form().dispatchErrorOccurredEvent(this, "SetCenter", ErrorMessages.ERROR_INVALID_POINT, latitude, longitude);
      return;
    }
    if (longitude < -180 || longitude > 180) {
      container.$form().dispatchErrorOccurredEvent(this, "SetCenter", ErrorMessages.ERROR_INVALID_POINT, latitude, longitude);
      return;
    }
    GeoPoint currentCenter = getCentroid();
    GeoPoint northPoint = new GeoPoint(north, currentCenter.getLongitude());
    GeoPoint southPoint = new GeoPoint(south, currentCenter.getLongitude());
    GeoPoint eastPoint = new GeoPoint(currentCenter.getLatitude(), east);
    GeoPoint westPoint = new GeoPoint(currentCenter.getLatitude(), west);
    double latExtent2 = GeometryUtil.distanceBetween(northPoint, southPoint) / 2.0;
    double longExtent2 = GeometryUtil.distanceBetween(eastPoint, westPoint) / 2.0;
    currentCenter.setCoords(latitude, longitude);
    north = currentCenter.destinationPoint(latExtent2, 0.0f).getLatitude();
    south = currentCenter.destinationPoint(latExtent2, 180.0f).getLatitude();
    east = currentCenter.destinationPoint(longExtent2, 90.0f).getLongitude();
    west = currentCenter.destinationPoint(longExtent2, 270.0f).getLongitude();
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  @Override
  public <T> T accept(MapFeatureVisitor<T> visitor, Object... arguments) {
    return visitor.visit(this, arguments);
  }

  @Override
  protected Geometry computeGeometry() {
    return GeometryUtil.createGeometry(north, east, south, west);
  }

  @Override
  public void updateBounds(double north, double west, double south, double east) {
    this.north = north;
    this.west = west;
    this.south = south;
    this.east = east;
    clearGeometry();
  }
}
