// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
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

  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies whether the component should be visible on the screen. "
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

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The paint color used to outline the map feature.")
  public int StrokeColor() {
    return strokeColor;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
      defaultValue = "1")
  @SimpleProperty
  public void StrokeWidth(int width) {
    strokeWidth = width;
    map.getController().updateFeatureStroke(this);
  }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The width of the stroke used to outline the map feature.")
  public int StrokeWidth() {
    return strokeWidth;
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void Draggable(boolean draggable) {
    this.draggable = draggable;
    map.getController().updateFeatureDraggable(this);
  }

  @Override
  @SimpleProperty(description = "The Draggable property is used to set whether or not the user " +
      "can drag the Marker by long-pressing and then dragging the marker to a new location.")
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

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The title displayed in the info window that appears when the user clicks " +
      "on the map feature.")
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

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The description displayed in the info window that appears when the user " +
      "clicks on the map feature.")
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

  @Override
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
  description = "Enable or disable the infobox window display when the user taps the feature.")
  public boolean EnableInfobox() {
    return infobox;
  }

  @Override
  @SimpleFunction(description = "Show the infobox for the feature. This will show the infobox " +
      "even if {@link #EnableInfobox} is set to false.")
  public void ShowInfobox() {
    map.getController().showInfobox(this);
  }

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

  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Compute the distance, in meters, between a map feature and a " +
      "latitude, longitude point.")
  public double DistanceToPoint(double latitude, double longitude, boolean centroid) {
    return accept(distanceToPoint, new GeoPoint(latitude, longitude), centroid);
  }

  @SuppressWarnings("squid:S00100")
  @SimpleFunction(description = "Compute the distance, in meters, between two map features.")
  public double DistanceToFeature(MapFeature mapFeature, final boolean centroids) {
    return mapFeature == null ? -1 : mapFeature.accept(distanceComputation, this, centroids);
  }

  // Component Events
  @Override
  @SimpleEvent(description = "The user clicked on the feature.")
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
    container.FeatureClick(this);
  }

  @Override
  @SimpleEvent(description = "The user long-pressed on the feature. This event will only " +
      "trigger if Draggable is false.")
  public void LongClick() {
    EventDispatcher.dispatchEvent(this, "LongClick");
    container.FeatureLongClick(this);
  }

  @Override
  @SimpleEvent(description = "The user started a drag operation.")
  public void StartDrag() {
    EventDispatcher.dispatchEvent(this, "StartDrag");
    container.FeatureStartDrag(this);
  }

  @Override
  @SimpleEvent(description = "The user dragged the map feature.")
  public void Drag() {
    EventDispatcher.dispatchEvent(this, "Drag");
    container.FeatureDrag(this);
  }

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
