// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.GeometryUtil;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.HasStroke;
import com.google.appinventor.components.runtime.util.MapFactory.MapCircle;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeature;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureContainer;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureVisitor;
import com.google.appinventor.components.runtime.util.MapFactory.MapLineString;
import com.google.appinventor.components.runtime.util.MapFactory.MapMarker;
import com.google.appinventor.components.runtime.util.MapFactory.MapPolygon;
import com.google.appinventor.components.runtime.util.MapFactory.MapRectangle;
import com.google.appinventor.components.runtime.util.YailList;
import org.locationtech.jts.geom.Geometry;
import org.osmdroid.util.GeoPoint;

@SimpleObject
public abstract class MapFeatureBase implements MapFeature, HasStroke {
  protected MapFeatureContainer container = null;
  protected Map map = null;
  private boolean visible = true;
  private int strokeColor = COLOR_BLACK;
  private float strokeOpacity = 1;
  private int strokeWidth = 1;
  private String title = "";
  private String description = "";
  private boolean draggable = false;
  private boolean infobox = false;
  private GeoPoint centroid = null;
  private final MapFeatureVisitor<Double> distanceComputation;
  private MapFeatureVisitor<Double> distanceToPoint = new MapFeatureVisitor<Double>() {
    @Override
    public Double visit(MapMarker marker, Object... arguments) {
      return GeometryUtil.distanceBetween(marker, (GeoPoint) arguments[0]);
    }

    @Override
    public Double visit(MapLineString lineString, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(lineString, (GeoPoint) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(lineString, (GeoPoint) arguments[0]);
      }
    }

    @Override
    public Double visit(MapPolygon polygon, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(polygon, (GeoPoint) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(polygon, (GeoPoint) arguments[0]);
      }
    }

    @Override
    public Double visit(MapCircle circle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(circle, (GeoPoint) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(circle, (GeoPoint) arguments[0]);
      }
    }

    @Override
    public Double visit(MapRectangle rectangle, Object... arguments) {
      if ((Boolean) arguments[1]) {
        return GeometryUtil.distanceBetweenCentroids(rectangle, (GeoPoint) arguments[0]);
      } else {
        return GeometryUtil.distanceBetweenEdges(rectangle, (GeoPoint) arguments[0]);
      }
    }
  };

  /**
   * A cached JTS geometry used in computations. This is lazily computed when
   * {@link #getGeometry()} is called.
   */
  private Geometry geometry = null;

  @SuppressWarnings("WeakerAccess")
  protected MapFeatureBase(MapFeatureContainer container,
      MapFeatureVisitor<Double> distanceComputation) {
    this.container = container;
    this.map = container.getMap();
    this.distanceComputation = distanceComputation;
    Description("");
    Draggable(false);
    EnableInfobox(false);
    StrokeColor(COLOR_BLACK);
    StrokeOpacity(1);
    StrokeWidth(1);
    Title("");
    Visible(true);
  }

  public void setMap(MapFactory.MapFeatureContainer container) {
    this.map = container.getMap();
  }

  @Override
  public void removeFromMap() {
    map.getController().removeFeature(this);
  }

  @SuppressWarnings("squid:S00100")
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY,
      defaultValue = "True")
  @SimpleProperty
  public void Visible(boolean visibility) {
    if (visible == visibility) {
      return;
    }
    this.visible = visibility;
    if (this.visible) {
      map.getController().showFeature(this);
    } else {
      map.getController().hideFeature(this);
    }
    map.getView().invalidate();
  }

  /**
   * Specifies whether the `%type%` should be visible on the screen.  Value is `true`{:.logic.block}
   * if the `%type%` is showing and `false`{:.logic.block} if hidden.
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies whether the %type% should be visible on the screen. "
          + "Value is true if the component is showing and false if hidden.")
  public boolean Visible() {
    return visible;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty
  public void StrokeColor(int argb) {
    strokeColor = argb;
    map.getController().updateFeatureStroke(this);
  }

  /**
   * Sets or gets the color used to outline the `%type%`.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The paint color used to outline the %type%.")
  @IsColor
  public int StrokeColor() {
    return strokeColor;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT,
      defaultValue = "1.0")
  @SimpleProperty
  public void StrokeOpacity(float opacity) {
    strokeOpacity = opacity;
    strokeColor = (strokeColor & 0x00FFFFFF) | (Math.round(0xFF * opacity) << 24);
    map.getController().updateFeatureStroke(this);
  }

  /**
   * Sets or gets the opacity of the outline of the `%type%`. A value of 0.0 will be invisible and
   * a value of 1.0 will be opaque.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
    description = "The opacity of the stroke used to outline the map feature.")
  public float StrokeOpacity() {
    return strokeOpacity;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
      defaultValue = "1")
  @SimpleProperty
  public void StrokeWidth(int width) {
    strokeWidth = width;
    map.getController().updateFeatureStroke(this);
  }

  /**
   * Sets or gets the width of the stroke used to outline the `%type%`.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The width of the stroke used to outline the %type%.")
  public int StrokeWidth() {
    return strokeWidth;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void Draggable(boolean draggable) {
    this.draggable = draggable;
    map.getController().updateFeatureDraggable(this);
  }

  /**
   * Sets or gets whether or not the user can drag a map feature. This feature is accessed by
   * long-pressing and then dragging the `%type%` to a new location.
   */
  @Override
  @SimpleProperty(description = "The Draggable property is used to set whether or not the user " +
      "can drag the %type% by long-pressing and then dragging the %type% to a new location.")
  public boolean Draggable() {
    return this.draggable;
  }

  @Override
  @DesignerProperty
  @SimpleProperty
  public void Title(String title) {
    this.title = title;
    map.getController().updateFeatureText(this);
  }

  /**
   * Sets or gets the title displayed in the info window that appears when the user clicks on the
   * map feature.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The title displayed in the info window that appears when the user clicks " +
      "on the %type%.")
  public String Title() {
    return title;
  }

  @Override
  @DesignerProperty
  @SimpleProperty
  public void Description(String description) {
    this.description = description;
    map.getController().updateFeatureText(this);
  }

  /**
   * Sets or gets the description displayed in the info window. The info window appears when the
   * user taps on the `%type%`.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The description displayed in the info window that appears when the user " +
      "clicks on the %type%.")
  public String Description() {
    return description;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void EnableInfobox(boolean enable) {
    this.infobox = enable;
    map.getController().updateFeatureText(this);
  }

  /**
   * Enables or disables the infobox window display when the user taps the `%type%`.
   */
  @Override
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
  description = "Enable or disable the infobox window display when the user taps the %type%.")
  public boolean EnableInfobox() {
    return infobox;
  }

  /**
   * Shows the info box for the `%type%` if it is not visible. Otherwise, this method has no effect.
   * This method can be used to show the info box even if {@link #EnableInfobox()} is false.
   */
  @Override
  @SimpleFunction(description = "Show the infobox for the %type%. This will show the infobox " +
      "even if EnableInfobox is set to false.")
  public void ShowInfobox() {
    map.getController().showInfobox(this);
  }

  /**
   * Hides the `%type%`'s info box if it is visible. Otherwise, no action is taken.
   */
  @Override
  @SimpleFunction(description = "Hide the infobox if it is shown. If the infobox is not " +
      "visible this function has no effect.")
  public void HideInfobox() {
    map.getController().hideInfobox(this);
  }

  @SuppressWarnings("squid:S00100")
  public YailList Centroid() {
    return GeometryUtil.asYailList(getCentroid());
  }

  /**
   * Computes the distance between the `%type%` and the given `latitude` and `longitude`. If
   * `centroids` is `true`{:.logic.block}, the distance is computed from the center of the `%type%`
   * to the given point. Otherwise, the distance is computed from the closest point on the `%type%`
   * to the given point. Further, this method will return 0 if `centroids` is `false`{:.logic.block}
   * and the point is in the `%type%`. If an error occurs, -1 will be returned.
   *
   * @param latitude The latitude of the point to compute the distance to
   * @param longitude The longitude of the point to compute the distance to
   * @param centroid A flag to indicate whether the distance should be computed from the centroid
   *                 of the feature (true) or from the edge (false).
   * @return The distance from the feature to the (latitude, longitude) point, or -1 if there was
   *         an error.
   */
  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Compute the distance, in meters, between a %type% and a " +
      "latitude, longitude point.")
  public double DistanceToPoint(double latitude, double longitude, boolean centroid) {
    return accept(distanceToPoint, new GeoPoint(latitude, longitude), centroid);
  }

  /**
   * Computes the distance between the `%type%` and the given `mapFeature`. If `centroids` is
   * `true`{:.logic.block}, the computation is done between the centroids of the two features.
   * Otherwise, the distance will be computed between the two features based on the closest points.
   * Further, when `centroids` is `false`{:.logic.block}, this method will return 0 if the `%type%`
   * intersects or contains the `mapFeature`. If an error occurs, this method will return -1.
   *
   * @param mapFeature The feature to compute the distance to
   * @param centroids A flag to indicate whether the distance should be computed from the centroids
   *                  of the two features (true) or from the edges of the two features (false).
   * @return The distance in meters between the two features or -1 if there is an error.
   */
  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Compute the distance, in meters, between two map features.")
  public double DistanceToFeature(MapFeature mapFeature, final boolean centroids) {
    return mapFeature == null ? -1 : mapFeature.accept(distanceComputation, this, centroids);
  }

  // Component Events

  /**
   * The `Click` event runs when the user taps on the `%type%`.
   */
  @Override
  @SimpleEvent(description = "The user clicked on the %type%.")
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
    container.FeatureClick(this);
  }

  /**
   * The `LongClick` event runs when the user presses and holds the `%type%` and then releases it.
   * This event will only trigger if {@link #Draggable()} is `false`{:.logic.block} because it
   * uses the same gesture as {@link #StartDrag()}.
   */
  @Override
  @SimpleEvent(description = "The user long-pressed on the %type%. This event will only " +
      "trigger if Draggable is false.")
  public void LongClick() {
    EventDispatcher.dispatchEvent(this, "LongClick");
    container.FeatureLongClick(this);
  }

  /**
   * The `StartDrag` event runs when the user presses and holds the `%type%` and then proceeds to
   * move their finger on the screen. It will be followed by the {@link #Drag()} and
   * {@link #StopDrag()} events.
   */
  @Override
  @SimpleEvent(description = "The user started a drag operation.")
  public void StartDrag() {
    EventDispatcher.dispatchEvent(this, "StartDrag");
    container.FeatureStartDrag(this);
  }

  /**
   * The `Drag` event runs in response to position updates of the `%type%` as the user drags it.
   */
  @Override
  @SimpleEvent(description = "The user dragged the %type%.")
  public void Drag() {
    EventDispatcher.dispatchEvent(this, "Drag");
    container.FeatureDrag(this);
  }

  /**
   * The `StopDrag` event runs when the user releases the `%type%` at the end of a drag.
   */
  @Override
  @SimpleEvent(description = "The user stopped a drag operation.")
  public void StopDrag() {
    EventDispatcher.dispatchEvent(this, "StopDrag");
    container.FeatureStopDrag(this);
  }

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return map.getDispatchDelegate();
  }

  @Override
  public final synchronized GeoPoint getCentroid() {
    if (centroid == null) {
      centroid = GeometryUtil.jtsPointToGeoPoint(getGeometry().getCentroid());
    }
    return centroid;
  }

  @Override
  public final synchronized Geometry getGeometry() {
    if (geometry == null) {
      geometry = computeGeometry();
    }
    return geometry;
  }

  @SuppressWarnings("WeakerAccess")
  protected final synchronized void clearGeometry() {
    centroid = null;
    geometry = null;
  }

  protected abstract Geometry computeGeometry();
}
