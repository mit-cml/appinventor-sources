// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MapFactory;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureVisitor;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;

import android.util.Log;

@DesignerComponent(version = YaVersion.MARKER_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "<p>An icon positioned at a point to indicate information on a map. Markers " +
        "can be used to provide an info window, custom fill and stroke colors, and custom " +
        "images to convey information to the user.</p>")
@SimpleObject
@UsesAssets(fileNames = "marker.svg")
@UsesLibraries(libraries = "osmdroid.aar, androidsvg.jar")
public class Marker extends MapFeatureBaseWithFill implements MapMarker {
  private static final String TAG = Marker.class.getSimpleName();

  /**
   * Path to the asset used as the marker drawable. If null or the empty string (the default),
   * then a prepackaged marker is used.
   */
  private String imagePath = "";

  /**
   * Horizontal alignment of the marker drawable relative to its longitude. The default value is 3,
   * which places the anchor at the horizontal center of the drawable.
   */
  private int anchorHAlign = 3;

  /**
   * Vertical alignment of the marker drawable relative to its latitude. The default value is 3,
   * which places the anchor at the bottom of the drawable.
   */
  private int anchorVAlign = 3;

  /**
   * Location of the marker's anchor.
   */
  private GeoPoint location = new GeoPoint(0.0, 0.0);

  /**
   * Width of the marker
   */
  private int width = LENGTH_PREFERRED;

  /**
   * Height of the marker
   */
  private int height = LENGTH_PREFERRED;

  private static final MapFeatureVisitor<Double> distanceComputation = new MapFeatureVisitor<Double>() {
    @Override
    public Double visit(MapMarker marker, Object... arguments) {
      return GeometryUtil.distanceBetween((Marker)arguments[0], marker);
    }

    @Override
    public Double visit(MapLineString lineString, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((Marker) arguments[0], lineString);
      } else {
        return GeometryUtil.distanceBetweenEdges((Marker) arguments[0], lineString);
      }
    }

    @Override
    public Double visit(MapPolygon polygon, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((Marker) arguments[0], polygon);
      } else {
        return GeometryUtil.distanceBetweenEdges((Marker) arguments[0], polygon);
      }
    }

    @Override
    public Double visit(MapCircle circle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((Marker) arguments[0], circle);
      } else {
        return GeometryUtil.distanceBetweenEdges((Marker) arguments[0], circle);
      }
    }

    @Override
    public Double visit(MapRectangle rectangle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids((Marker) arguments[0], rectangle);
      } else {
        return GeometryUtil.distanceBetweenEdges((Marker) arguments[0], rectangle);
      }
    }
  };

  private static final MapFeatureVisitor<Double> bearingComputation = new MapFeatureVisitor<Double>() {
    @Override
    public Double visit(MapMarker marker, Object... arguments) {
      return GeometryUtil.bearingTo((Marker) arguments[0], marker);
    }

    @Override
    public Double visit(MapLineString lineString, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.bearingToCentroid((MapMarker) arguments[0], lineString);
      } else {
        return GeometryUtil.bearingToEdge((MapMarker) arguments[0], lineString);
      }
    }

    @Override
    public Double visit(MapPolygon polygon, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.bearingToCentroid((MapMarker) arguments[0], polygon);
      } else {
        return GeometryUtil.bearingToEdge((MapMarker) arguments[0], polygon);
      }
    }

    @Override
    public Double visit(MapCircle circle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.bearingToCentroid((MapMarker) arguments[0], circle);
      } else {
        return GeometryUtil.bearingToEdge((MapMarker) arguments[0], circle);
      }
    }

    @Override
    public Double visit(MapRectangle rectangle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.bearingToCentroid((MapMarker) arguments[0], rectangle);
      } else {
        return GeometryUtil.bearingToEdge((MapMarker) arguments[0], rectangle);
      }
    }
  };

  /**
   * Creates a new Marker object contained within the <code>container</code>.
   * <code>container</code> is only valid if it is a {@link Map} or {@link FeatureCollection}.
   *
   * @param container the container the Marker belongs to.
   */
  public Marker(MapFactory.MapFeatureContainer container) {
    super(container, distanceComputation);
    container.addFeature(this);
    ShowShadow(false);
    AnchorHorizontal(ComponentConstants.GRAVITY_CENTER_HORIZONTAL);
    AnchorVertical(ComponentConstants.GRAVITY_BOTTOM);
    ImageAsset("");
    Width(LENGTH_PREFERRED);
    Height(LENGTH_PREFERRED);
    Latitude(0);
    Longitude(0);
  }

  @SimpleProperty
  @Override
  public String Type() {
    return MapFactory.MapFeatureType.TYPE_MARKER;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LATITUDE,
      defaultValue = "0")
  @SimpleProperty
  public void Latitude(double latitude) {
    Log.d(TAG, "Latitude");
    if (latitude < -90 || latitude > 90) {
      container.$form().dispatchErrorOccurredEvent(this, "Latitude", ErrorMessages.ERROR_INVALID_LATITUDE, latitude);
    } else {
      location.setLatitude(latitude);
      clearGeometry();
      map.getController().updateFeaturePosition(this);
    }
  }

  @SimpleProperty
  public double Latitude() {
    return location.getLatitude();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LONGITUDE,
      defaultValue = "0")
  @SimpleProperty
  public void Longitude(double longitude) {
    Log.d(TAG, "Longitude");
    if (longitude < -180 || longitude > 180) {
      container.$form().dispatchErrorOccurredEvent(this, "Longitude", ErrorMessages.ERROR_INVALID_LONGITUDE, longitude);
    } else {
      location.setLongitude(longitude);
      clearGeometry();
      map.getController().updateFeaturePosition(this);
    }
  }

  @SimpleProperty
  public double Longitude() {
    return location.getLongitude();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
  @SimpleProperty
  public void ImageAsset(String path) {
    Log.d(TAG, "ImageAsset");
    this.imagePath = path;
    map.getController().updateFeatureImage(this);
  }

  @SimpleProperty(description = "The ImageAsset property is used to provide an alternative image " +
      "for the Marker.")
  public String ImageAsset() {
    return imagePath;
  }

  @SimpleProperty
  public void StrokeColor(int argb) {
    super.StrokeColor(argb);
    map.getController().updateFeatureStroke(this);
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
      defaultValue = "3")
  @SimpleProperty
  public void AnchorHorizontal(int horizontal) {
    if (horizontal == anchorHAlign) {
      return;
    } else if (horizontal > 3 || horizontal < 1) {
      container.$form().dispatchErrorOccurredEvent(this, "AnchorHorizontal", ErrorMessages.ERROR_INVALID_ANCHOR_HORIZONTAL, horizontal);
      return;
    }
    anchorHAlign = horizontal;
    map.getController().updateFeaturePosition(this);
  }

  @Override
  @SimpleProperty(description = "The horizontal alignment property controls where the Marker's " +
      "anchor is located relative to its width.")
  public int AnchorHorizontal() {
    return anchorHAlign;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
      defaultValue = "3")
  @SimpleProperty
  public void AnchorVertical(int vertical) {
    if (vertical == anchorVAlign) {
      return;
    } else if (vertical > 3 || vertical < 1) {
      container.$form().dispatchErrorOccurredEvent(this, "AnchorVertical", ErrorMessages.ERROR_INVALID_ANCHOR_VERTICAL, vertical);
      return;
    }
    anchorVAlign = vertical;
    map.getController().updateFeaturePosition(this);
  }

  @Override
  @SimpleProperty(description = "The vertical alignment property controls where the Marker's " +
      "anchor is located relative to its height.")
  public int AnchorVertical() {
    return anchorVAlign;
  }

  @Override
  @SimpleProperty(userVisible = false)
  public void ShowShadow(boolean show) {
    // This method has been deprecated.
  }

  @Override
  @SimpleProperty(description = "Gets whether or not the shadow of the Marker is shown.")
  public boolean ShowShadow() {
    return false;
  }

  @Override
  @SimpleProperty
  public void Width(int width) {
    this.width = width;
    map.getController().updateFeatureSize(this);
  }

  @Override
  @SimpleProperty
  public int Width() {
    if (this.width == LENGTH_FILL_PARENT) {
      return map.getView().getWidth();
    } else if (this.width < LENGTH_PERCENT_TAG) {
      return (int)(((double) -this.width + LENGTH_PERCENT_TAG)/100.0 * map.getView().getWidth());
    }
    return this.width;
  }

  @SuppressWarnings("squid:S00100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void WidthPercent(int pCent) {
    this.width = LENGTH_PERCENT_TAG - pCent;
    map.getController().updateFeatureSize(this);
  }

  @Override
  @SimpleProperty
  public void Height(int height) {
    this.height = height;
    map.getController().updateFeatureSize(this);
  }

  @Override
  @SimpleProperty
  public int Height() {
    if (this.height == LENGTH_FILL_PARENT) {
      return map.getView().getHeight();
    } else if (this.height < LENGTH_PERCENT_TAG) {
      return (int)(((double) -this.height + LENGTH_PERCENT_TAG)/100.0 * map.getView().getHeight());
    }
    return this.height;
  }

  @SuppressWarnings("squid:S00100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void HeightPercent(int pCent) {
    this.height = LENGTH_PERCENT_TAG - pCent;
    map.getController().updateFeatureSize(this);
  }

  @SimpleFunction(description = "Set the location of the marker.")
  public void SetLocation(double latitude, double longitude) {
    Log.d(TAG, "SetLocation");
    location.setCoords(latitude, longitude);
    clearGeometry();
    map.getController().updateFeaturePosition(this);
  }

  /**
   * Suppresses the blocks version of this method so that {@link #DistanceToPoint(double, double)}
   * will be provided instead. Note that this will call through to the other method in case the
   * Marker is passed to an "Any Component" block that computes distance.
   *
   * @param latitude  latitude of the point to compute distance to
   * @param longitude  longitude of the point to compute distance to
   * @param centroid  ignored--a marker and a (lat, lon) are already both points
   * @return the distance in meters from the marker to the given (lat, lon)
   */
  @Override
  public double DistanceToPoint(double latitude, double longitude, boolean centroid) {
    return DistanceToPoint(latitude, longitude);
  }

  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Compute the distance, in meters, between a map feature and a " +
      "latitude, longitude point.")
  public double DistanceToPoint(double latitude, double longitude) {
    return GeometryUtil.distanceBetween(this, new GeoPoint(latitude, longitude));
  }

  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Returns the bearing from the Marker to the given latitude and " +
      "longitude, in degrees " +
      "from due north.")
  public double BearingToPoint(double latitude, double longitude) {
    return location.bearingTo(new GeoPoint(latitude, longitude));
  }

  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Returns the bearing from the Marker to the given map feature, " +
      "in degrees from due north. If the centroids parameter is true, the bearing will be to the " +
      "center of the map feature. Otherwise, the bearing will be computed to the point in the " +
      "feature nearest the Marker.")
  public double BearingToFeature(MapFeature mapFeature, final boolean centroids) {
    return mapFeature == null ? -1 : mapFeature.accept(bearingComputation, this, centroids);
  }

  @Override
  public IGeoPoint getLocation() {
    return location;
  }

  @Override
  public void updateLocation(double latitude, double longitude) {
    this.location = new GeoPoint(latitude, longitude);
    clearGeometry();
  }

  @Override
  public <T> T accept(MapFeatureVisitor<T> visitor, Object... arguments) {
    return visitor.visit(this, arguments);
  }

  @Override
  protected Geometry computeGeometry() {
    return GeometryUtil.createGeometry(location);
  }
}
