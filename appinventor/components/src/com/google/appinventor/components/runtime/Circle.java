// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLatitude;
import static com.google.appinventor.components.runtime.util.GeometryUtil.isValidLongitude;

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
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureVisitor;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.util.GeoPoint;

/**
 * The `Circle` component visualizes a circle of a given {@link #Radius(double)}, in meters,
 * centered at a {@link #Latitude(double)} and {@link #Longitude(double)}. The `Circle`'s appearance
 * cnn be customized using properties such as {@link #FillColor(int)}, {@link #StrokeColor(int)},
 * and {@link #StrokeWidth(int)}.
 *
 * The `Circle` component can also be used to implement features such as geofencing, a mechanism
 * where the user's presence within an area is used to trigger other behaviors. Using the
 * {@link #DistanceToPoint(double, double, boolean)} method combined with the
 * [`LocationSensor`](sensors.html#LocationSensor), you can determine whether a user's location is
 * inside or outside of the `Circle`. You can use this feature to trigger additional actions.
 */
@DesignerComponent(version = YaVersion.CIRCLE_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "Circle",
    iconName = "images/circle.png")
@SimpleObject
public class Circle extends PolygonBase implements MapCircle {
  /**
   * The center of the circle, as a GeoPoint.
   */
  private GeoPoint center = new GeoPoint(0.0, 0.0);

  /**
   * Latitude of the center of the circle.
   */
  private double latitude;

  /**
   * Longitude of the center of the circle.
   */
  private double longitude;

  /**
   * Radius of the circle, in meters.
   */
  private double radius;

  private static final MapFeatureVisitor<Double> distanceComputation = new MapFeatureVisitor<Double>() {
    @Override
    public Double visit(MapMarker marker, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(marker, (Circle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(marker, (Circle) arguments[0]);
      }
    }

    @Override
    public Double visit(MapLineString lineString, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(lineString, (Circle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(lineString, (Circle) arguments[0]);
      }
    }

    @Override
    public Double visit(MapPolygon polygon, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(polygon, (Circle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(polygon, (Circle) arguments[0]);
      }
    }

    @Override
    public Double visit(MapCircle circle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(circle, (Circle) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(circle, (Circle) arguments[0]);
      }
    }

    @Override
    public Double visit(MapRectangle rectangle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((Circle) arguments[0], rectangle);
      } else {
        return GeometryUtil.distanceBetweenEdges((Circle) arguments[0], rectangle);
      }
    }
  };

  public Circle(MapFactory.MapFeatureContainer container) {
    super(container, distanceComputation);
    container.addFeature(this);
  }

  @Override
  @SimpleProperty(description = "Returns the type of the feature. For Circles, "
      + "this returns MapFeature.Circle (\"Circle\").")
  public @Options(MapFeature.class) String Type() {
    return TypeAbstract().toUnderlyingValue();
  }

  /**
   * Gets the type of the feature.
   *
   * @return the abstract MapFeature type of this feature. In this case MapFeature.Circle.
   */
  @SuppressWarnings("RegularMethodName")
  public MapFeature TypeAbstract() {
    return MapFeature.Circle;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = "0")
  @SimpleProperty
  public void Radius(double radius) {
    this.radius = radius;
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  /**
   * Sets or gets the radius of the circle, in meters.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The radius of the circle in meters.")
  public double Radius() {
    return radius;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LATITUDE,
      defaultValue = "0")
  @SimpleProperty
  public void Latitude(double latitude) {
    if (isValidLatitude(latitude)) {
      this.latitude = latitude;
      this.center.setLatitude(latitude);
      clearGeometry();
      map.getController().updateFeaturePosition(this);
    } else {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "Latitude",
          ErrorMessages.ERROR_INVALID_LATITUDE, latitude);
    }
  }

  /**
   * Sets or gets the latitude of the center of the circle, in degrees. Positive values represent
   * north of the equator and negative values represent south of the equator. To update the
   * latitude and longitude simultaneously, use the {@link #SetLocation(double, double)} method.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The latitude of the center of the circle.")
  public double Latitude() {
    return latitude;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LONGITUDE,
      defaultValue = "0")
  @SimpleProperty
  public void Longitude(double longitude) {
    if (isValidLongitude(longitude)) {
      this.longitude = longitude;
      this.center.setLongitude(longitude);
      clearGeometry();
      map.getController().updateFeaturePosition(this);
    } else {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "Longitude",
          ErrorMessages.ERROR_INVALID_LONGITUDE, longitude);
    }
  }

  /**
   * Sets or gets the longitude of the center of the circle, in degrees. Positive values represent
   * east of the prime meridian and negative values represent west of the prime meridian. To update
   * the latitude and longitude simultaneously, use the {@link #SetLocation(double, double)} method.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The longitude of the center of the circle.")
  public double Longitude() {
    return longitude;
  }

  /**
   * Moves the center of the `Circle` to the given `latitude` and `longitude`. This method is more
   * efficient than setting {@link #Latitude(double)} and {@link #Longitude(double)} separately.
   * @param latitude the new latitude of the circle center, in decimal degrees.
   * @param longitude the new longitude of the circle center, in decimal degrees.
   */
  @Override
  @SimpleFunction(description = "Set the center of the Circle.")
  public void SetLocation(double latitude, double longitude) {
    if (!isValidLatitude(latitude)) {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "SetLocation",
          ErrorMessages.ERROR_INVALID_LATITUDE, latitude);
    } else if (!isValidLongitude(longitude)) {
      getDispatchDelegate().dispatchErrorOccurredEvent(this, "SetLocation",
          ErrorMessages.ERROR_INVALID_LONGITUDE, longitude);
    } else {
      this.latitude = latitude;
      this.longitude = longitude;
      this.center.setLatitude(latitude);
      this.center.setLongitude(longitude);
      clearGeometry();
      map.getController().updateFeaturePosition(this);
    }
  }

  @Override
  public <T> T accept(MapFeatureVisitor<T> visitor, Object... arguments) {
    return visitor.visit(this, arguments);
  }

  @Override
  protected Geometry computeGeometry() {
    return GeometryUtil.createGeometry(center);
  }

  @Override
  public void updateCenter(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    clearGeometry();
  }
}
