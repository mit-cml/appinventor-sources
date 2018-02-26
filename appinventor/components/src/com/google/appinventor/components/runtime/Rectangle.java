// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
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

@DesignerComponent(version = YaVersion.RECTANGLE_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "Rectangle")
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
      description = "The type of the feature. For rectangles, this returns the text \"Rectangle\".")
  public String Type() {
    return MapFactory.MapFeatureType.TYPE_RECTANGLE;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0")
  @SimpleProperty
  public void EastLongitude(double east) {
    this.east = east;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  @Override
  @SimpleProperty
  public double EastLongitude() {
    return east;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0")
  @SimpleProperty
  public void NorthLatitude(double north) {
    this.north = north;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  @Override
  @SimpleProperty
  public double NorthLatitude() {
    return north;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0")
  @SimpleProperty
  public void SouthLatitude(double south) {
    this.south = south;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  @Override
  @SimpleProperty
  public double SouthLatitude() {
    return south;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "0")
  @SimpleProperty
  public void WestLongitude(double west) {
    this.west = west;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  @Override
  @SimpleProperty
  public double WestLongitude() {
    return west;
  }

  @Override
  @SimpleFunction(description = "Returns the center of the Rectangle as a list of the form " +
      "(Latitude Longitude).")
  public YailList Center() {
    return GeometryUtil.asYailList(getCentroid());
  }

  @Override
  @SimpleFunction(description = "Returns the bounding box of the Rectangle in the format " +
      "((North West) (South East)).")
  public YailList Bounds() {
    YailList nw = YailList.makeList(new Double[] { north, west });
    YailList se = YailList.makeList(new Double[] { south, east });
    return YailList.makeList(new YailList[] { nw, se });
  }

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
