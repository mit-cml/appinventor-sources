// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2016-2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import com.google.appinventor.components.annotations.Asset;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.HorizontalAlignment;
import com.google.appinventor.components.common.MapFeature;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.VerticalAlignment;
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

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

/**
 * The `Marker` component indicates points on a {@link Map}, such as buildings or other points of
 * interest. `Marker`s can be customized in many ways, such as using custom images from the app's
 * assets or by changing the `Marker` {@link #FillColor(int)}. `Marker`s can also be created
 * dynamically by calling the {@link Map#CreateMarker(double, double)} method and configured using
 * the ["Any Component"](../other/any-component-blocks.html) blocks.
 */
@DesignerComponent(version = YaVersion.MARKER_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "<p>An icon positioned at a point to indicate information on a map. Markers "
        + "can be used to provide an info window, custom fill and stroke colors, and custom "
        + "images to convey information to the user.</p>")
@SimpleObject
@UsesLibraries(libraries = "osmdroid.aar, androidsvg.jar")
public class Marker extends MapFeatureBaseWithFill implements MapMarker {
  private static final String TAG = Marker.class.getSimpleName();

  /**
   * Path to the asset used as the marker drawable. If null or the empty string (the default),
   * then a prepackaged marker is used.
   */
  private String imagePath = "";

  /**
   * Horizontal alignment of the marker drawable relative to its longitude. Defaults to center.
   */
  private HorizontalAlignment anchorHAlign = HorizontalAlignment.Center;

  /**
   * Vertical alignment of the marker relative to its latitude. Defaults to bottom.
   */
  private VerticalAlignment anchorVAlign = VerticalAlignment.Bottom;

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

  private volatile boolean pendingUpdate = false;

  private final Handler handler = new Handler(Looper.getMainLooper());

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

  /**
   * Return the type of the map feature. For Marker, this returns the text "Marker".
   * @return the type of the feature
   */
  @Override
  @SimpleProperty(description = "Returns the type of the feature. For Markers, "
      + "this returns MapFeature.Marker (\"Marker\").")
  public @Options(MapFeature.class) String Type() {
    return TypeAbstract().toUnderlyingValue();
  }

  /**
   * Gets the type of the feature, as a {@link MapFeature} enum.
   *
   * @return the abstract MapFeature type of this feature. In this case MapFeature.Marker.
   */
  @SuppressWarnings("RegularMethodName")
  public MapFeature TypeAbstract() {
    return MapFeature.Marker;
  }

  /**
   * Sets or gets the latitude of the `Marker`, in degrees, with positive values representing
   * north of the equator and negative values representing south of the equator. To update the
   * `Latitude` and {@link #Longitude(double)} simultaneously, use the
   * {@link #SetLocation(double, double)} method.
   *
   * @param latitude the new latitude of the marker. Range: [-90, 90]
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LATITUDE,
      defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void Latitude(double latitude) {
    Log.d(TAG, "Latitude");
    if (latitude < -90 || latitude > 90) {
      container.$form().dispatchErrorOccurredEvent(this, "Latitude",
          ErrorMessages.ERROR_INVALID_LATITUDE, latitude);
    } else {
      location.setLatitude(latitude);
      clearGeometry();
      map.getController().updateFeaturePosition(this);
    }
  }

  /**
   * @suppressdoc
   */
  @SimpleProperty
  public double Latitude() {
    return location.getLatitude();
  }

  /**
   * Sets or gets the longitude of the `Marker`, in degrees, with positive values representing east
   * of the prime meridian and negative values representing west of the prime meridian. To update
   * the {@link #Latitude(double)} and `Longitude` simultaneously, use the
   * {@link #SetLocation(double, double)} method.
   *
   * @param longitude the new longitude of the marker. Range: [-180, 180]
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_LONGITUDE,
      defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void Longitude(double longitude) {
    Log.d(TAG, "Longitude");
    if (longitude < -180 || longitude > 180) {
      container.$form().dispatchErrorOccurredEvent(this, "Longitude",
          ErrorMessages.ERROR_INVALID_LONGITUDE, longitude);
    } else {
      location.setLongitude(longitude);
      clearGeometry();
      map.getController().updateFeaturePosition(this);
    }
  }

  /**
   * @suppressdoc
   */
  @SimpleProperty
  public double Longitude() {
    return location.getLongitude();
  }

  /**
   * Specifies the image shown for the `Marker`. If set to the empty string "", then the default
   * marker icon will be used.
   *
   * @param path a relative or absolute path, or a url, to an image asset to use for the marker.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ImageAsset(@Asset String path) {
    Log.d(TAG, "ImageAsset");
    this.imagePath = path;
    setNeedsUpdate();
  }

  /**
   * Specifies the image shown for the `Marker`. If set to the empty string "", then the default
   * marker icon will be used.
   */
  @SimpleProperty(description = "The ImageAsset property is used to provide an alternative image "
      + "for the Marker.")
  public String ImageAsset() {
    return imagePath;
  }

  /**
   * @suppressdoc
   * @param argb the outline paint color
   */
  @SimpleProperty
  public void StrokeColor(int argb) {
    super.StrokeColor(argb);
    map.getController().updateFeatureStroke(this);
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
      defaultValue = "3")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void AnchorHorizontal(@Options(HorizontalAlignment.class) int horizontal) {
    // Make sure the horizontal alignment is a valid HorizontalAlignment.
    HorizontalAlignment alignment = HorizontalAlignment.fromUnderlyingValue(horizontal);
    if (alignment == null) {
      container.$form().dispatchErrorOccurredEvent(this, "AnchorHorizontal",
          ErrorMessages.ERROR_INVALID_ANCHOR_HORIZONTAL, horizontal);
      return;
    }
    AnchorHorizontalAbstract(alignment);
  }

  /**
   * Sets or gets the horizontal offset of the `Marker` center relative to its image. Valid values
   * are: `1` (Left), `2` (Right), or `3` (Center).
   */
  @Override
  @SimpleProperty(description = "The horizontal alignment property controls where the Marker's "
      + "anchor is located relative to its width. The choices are: 1 = left aligned,"
      + " 3 = horizontally centered, 2 = right aligned.")
  public @Options(HorizontalAlignment.class) int AnchorHorizontal() {
    return AnchorHorizontalAbstract().toUnderlyingValue();
  }

  /**
   * Returns the current horizontal alignment of this marker relative to its longitude.
   * @return the current horizontal alignment of this marker relative to its longitude.
   */
  @SuppressWarnings("RegularMethodName")
  public HorizontalAlignment AnchorHorizontalAbstract() {
    return anchorHAlign;
  }

  /**
   * Sets the horizontal anchor point of this marker relative to its longitude.
   * @param alignment the alignment to set the anchor point to.
   */
  @SuppressWarnings("RegularMethodName")
  public void AnchorHorizontalAbstract(HorizontalAlignment alignment) {
    if (alignment != anchorHAlign) {
      anchorHAlign = alignment;
      map.getController().updateFeaturePosition(this);
    }
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
      defaultValue = "3")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void AnchorVertical(@Options(VerticalAlignment.class) int vertical) {
    // Make sure the vertical alignment is a valid VerticalAlignment.
    VerticalAlignment alignment = VerticalAlignment.fromUnderlyingValue(vertical);
    if (alignment == null) {
      container.$form().dispatchErrorOccurredEvent(this, "AnchorVertical", ErrorMessages.ERROR_INVALID_ANCHOR_VERTICAL, vertical);
      return;
    }
    AnchorVerticalAbstract(alignment);
  }

  /**
   * Sets or gets the vertical offset of the `Marker` center relative to its image. Valid values
   * are: `1` (Top), `2` (Center), or `3` (Bottom).
   */
  @Override
  @SimpleProperty(description = "The vertical alignment property controls where the Marker's "
      + "anchor is located relative to its height. The choices are: 1 = aligned at the top, 2 = "
      + "vertically centered, 3 = aligned at the bottom.")
  public @Options(VerticalAlignment.class) int AnchorVertical() {
    return AnchorVerticalAbstract().toUnderlyingValue();
  }

  /**
   * Returns the current vertical alignment of this marker relative to its latitude.
   * @return the current vertical alignment of this marker relative to its latitude.
   */
  @SuppressWarnings("RegularMethodName")
  public VerticalAlignment AnchorVerticalAbstract() {
    return anchorVAlign;
  }

  /**
   * Sets the vertical anchor point of this marker relative to its latitude.
   * @param alignment the alignment to set the anchor point to.
   */
  @SuppressWarnings("RegularMethodName")
  public void AnchorVerticalAbstract(VerticalAlignment alignment) {
    if (alignment != null) {
      anchorVAlign = alignment;
      map.getController().updateFeaturePosition(this);
    }
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

  /**
   * Specifies the horizontal width of the `%type%`, measured in pixels.
   *
   * @param width the new width of the marker image
   */
  @Override
  @SimpleProperty
  public void Width(int width) {
    this.width = width;
    setNeedsUpdate();
  }

  /**
   * Specifies the horizontal width of the `%type%`, measured in pixels.
   */
  @Override
  @SimpleProperty
  public int Width() {
    if (this.width == LENGTH_FILL_PARENT) {
      return map.getView().getWidth();
    } else if (this.width < LENGTH_PERCENT_TAG) {
      return (int)(((double) -this.width + LENGTH_PERCENT_TAG) / 100.0 * map.getView().getWidth());
    }
    return this.width;
  }

  /**
   * Specifies the horizontal width of the `%type%` as a percentage
   * of the [`Screen`'s `Width`](userinterface.html#Screen.Width).
   *
   * @param pCent the new width, in percent, of the marker image
   */
  @SuppressWarnings("squid:S00100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void WidthPercent(int pCent) {
    this.width = LENGTH_PERCENT_TAG - pCent;
    setNeedsUpdate();
  }

  /**
   * Specifies the `%type%`'s vertical height, measured in pixels.
   *
   * @param height the new height of the marker image
   */
  @Override
  @SimpleProperty
  public void Height(int height) {
    this.height = height;
    setNeedsUpdate();
  }

  /**
   * Specifies the `%type%`'s vertical height, measured in pixels.
   */
  @Override
  @SimpleProperty
  public int Height() {
    if (this.height == LENGTH_FILL_PARENT) {
      return map.getView().getHeight();
    } else if (this.height < LENGTH_PERCENT_TAG) {
      return (int)(((double) -this.height + LENGTH_PERCENT_TAG) / 100.0
          * map.getView().getHeight());
    }
    return this.height;
  }

  /**
   * Specifies the `%type%`'s vertical height as a percentage
   * of the [`Screen`'s `Height`](userinterface.html#Screen.Height).
   *
   * @param pCent The new height, in percent, of the marker image.
   */
  @SuppressWarnings("squid:S00100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void HeightPercent(int pCent) {
    this.height = LENGTH_PERCENT_TAG - pCent;
    setNeedsUpdate();
  }

  /**
   * Sets the location of the `Marker`.
   *
   * @param latitude the new latitude of the marker
   * @param longitude the new longitude of the marker
   */
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

  /**
   * Compute the distance, in meters, between a `Marker` and a `latitude`, `longitude` point.
   *
   * @param latitude
   * @param longitude
   * @return
   */
  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Compute the distance, in meters, between a Marker and a "
      + "latitude, longitude point.")
  public double DistanceToPoint(double latitude, double longitude) {
    return GeometryUtil.distanceBetween(this, new GeoPoint(latitude, longitude));
  }

  /**
   * Returns the bearing from the `Marker` to the given `latitude` and `longitude`, in degrees
   * from due north.
   *
   * @param latitude
   * @param longitude
   * @return
   */
  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Returns the bearing from the Marker to the given latitude and "
      + "longitude, in degrees from due north.")
  public double BearingToPoint(double latitude, double longitude) {
    return location.bearingTo(new GeoPoint(latitude, longitude));
  }

  /**
   * Returns the bearing from the `Marker` to the given map feature, in degrees from due north.
   * If the `centroids` parameter is `true`{:.logic.block}, the bearing will be to the center of
   * the map feature. Otherwise, the bearing will be computed to the point in the feature nearest
   * the `Marker`.
   *
   * @param mapFeature The target map feature used for computing the bearing from the Marker
   * @param centroids True if the centroid of the target feature should be used for the computation,
   *                  otherwise false for the nearest point on the edge of the feature
   * @return The bearing in degrees east of due north
   */
  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Returns the bearing from the Marker to the given map feature, "
      + "in degrees from due north. If the centroids parameter is true, the bearing will be to the "
      + "center of the map feature. Otherwise, the bearing will be computed to the point in the "
      + "feature nearest the Marker.")
  public double BearingToFeature(MapFactory.MapFeature mapFeature, final boolean centroids) {
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

  private void setNeedsUpdate() {
    synchronized (this) {
      if (pendingUpdate) {
        return;
      }
      pendingUpdate = true;
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          map.getController().updateFeatureImage(Marker.this);
          synchronized (Marker.this) {
            pendingUpdate = false;
          }
        }
      }, 1);
    }
  }
}
