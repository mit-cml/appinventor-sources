// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

public class MockCircle extends MockMapFeatureBaseWithFill {
  public static final String TYPE = "Circle";

  private static final String PROPERTY_NAME_RADIUS = "Radius";

  public MockCircle(SimpleEditor editor) {
    super(editor, TYPE, images.circle());

    SVGPanel svgpanel = new SVGPanel();
    final int diameter = ComponentConstants.CIRCLE_PREFERRED_RADIUS * 2;
    final int center = ComponentConstants.CIRCLE_PREFERRED_RADIUS + 1;
    svgpanel.setPixelSize(diameter + 2, diameter + 2);
    svgpanel.setInnerSVG("<circle cx=\"" + center + "\" cy=\"" + center + "\" r=\"" +
        ComponentConstants.CIRCLE_PREFERRED_RADIUS + "\" stroke-width=\"1\" stroke=\"black\" fill=\"red\" />");
    panel.setWidget(svgpanel);
    panel.setWidth((diameter + 2) + "px");
    panel.setHeight((diameter + 2) + "px");

    initCircle();
  }

  static MockCircle fromGeoJSON(MockFeatureCollection parent, JSONObject properties, JavaScriptObject layer) {
    MockCircle circle = new MockCircle(parent.editor);
    circle.feature = layer;
    // TODO(ewpatton): Process properties
    return circle;
  }

  @Override
  public boolean onDrop(MockMap map, int x, int y, int offsetX, int offsetY) {
    final int radius = ComponentConstants.CIRCLE_PREFERRED_RADIUS + 1;
    // Unfortunately due to the mercator web projection there is no way to ensure the circle projected matches the
    // flat circle dragged from the palette. It will be most approximate at high zoom levels and closer to the equator.
    MockMap.LatLng point = map.projectFromXY(x - offsetX + radius, y - offsetY);
    MockMap.LatLng center = map.projectFromXY(x - offsetX + radius, y - offsetY + radius);
    final double radiusInM = distanceBetweenPoints(map.getMapInstance(), center, point);
    setCenterAndRadius(center.toNative(), radiusInM);
    getProperties().changePropertyValue(PROPERTY_NAME_LATITUDE, Double.toString(center.latitude));
    getProperties().changePropertyValue(PROPERTY_NAME_LONGITUDE, Double.toString(center.longitude));
    getProperties().changePropertyValue(PROPERTY_NAME_RADIUS, Double.toString(radiusInM));
    return true;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_LATITUDE)) {
      setLatitudeProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_LONGITUDE)) {
      setLongitudeProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_RADIUS)) {
      setRadiusProperty(newValue);
    }
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.CIRCLE_PREFERRED_RADIUS;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.CIRCLE_PREFERRED_RADIUS;
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }

    return super.isPropertyVisible(propertyName);
  }

  private void setLatitudeProperty(String newValue) {
    try {
      double latitude = Double.parseDouble(newValue);
      setCenter(latitude, getLongitude());
    } catch(NumberFormatException e) {
      // pass
    }
  }

  private void setLongitudeProperty(String newValue) {
    try {
      double longitude = Double.parseDouble(newValue);
      setCenter(getLatitude(), longitude);
    } catch(NumberFormatException e) {
      // pass
    }
  }

  private void setRadiusProperty(String newValue) {
    if (newValue == null || newValue.equals("")) {
      return;  // invalid value set in the MockComponent constructor
    }
    try {
      double radius = Double.parseDouble(newValue);
      setRadius(radius);
    } catch(NumberFormatException e) {
      // pass
    }
  }

  @SuppressWarnings("unused")  // called from JSNI
  private void updateCenter(double latitude, double longitude) {
    getProperties().changePropertyValue(PROPERTY_NAME_LATITUDE, Double.toString(latitude));
    getProperties().changePropertyValue(PROPERTY_NAME_LONGITUDE, Double.toString(longitude));
    super.onPropertyChange(PROPERTY_NAME_LATITUDE, Double.toString(latitude));
    super.onPropertyChange(PROPERTY_NAME_LONGITUDE, Double.toString(longitude));
  }

  @SuppressWarnings("unused")  // called from JSNI
  private void updateRadius(double radius) {
    getProperties().changePropertyValue(PROPERTY_NAME_RADIUS, Double.toString(radius));
    super.onPropertyChange(PROPERTY_NAME_RADIUS, Double.toString(radius));
  }

  // JSNI Methods
  private native void initCircle()/*-{
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature =
      top.L.circle([], @com.google.appinventor.components.common.ComponentConstants::CIRCLE_PREFERRED_RADIUS, {
        className: 'leaflet-interactive',
        weight: 1,
        color: '#000',
        fillColor: '#f00',
        fillOpacity: 1,
        draggable: true,
        pointerEvents: 'auto'
      });
  }-*/;

  private native void setCenterAndRadius(JavaScriptObject center, double radius)/*-{
    var circle = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (circle) {
      circle.setLatLng(center);
      circle.setRadius(radius);
      if (circle.editor) circle.editor.reset();  // reset vertices of the editor
    }
  }-*/;

  private native void setCenter(double latitude, double longitude)/*-{
    var circle = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (circle) {
      circle.setLatLng([latitude, longitude]);
      if (circle.editor) circle.editor.reset();  // reset vertices of the editor
    }
  }-*/;

  private native void setRadius(double radius)/*-{
    var circle = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (circle) {
      circle.setRadius(radius);
      if (circle.editor) circle.editor.reset();  // reset vertices of the editor
    }
  }-*/;

  private native double getLatitude()/*-{
    var circle = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (circle && circle.getLatLng()) {
      return circle.getLatLng().lat;
    } else {
      return 0;
    }
  }-*/;

  private native double getLongitude()/*-{
    var circle = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (circle && circle.getLatLng()) {
      return circle.getLatLng().lng;
    } else {
      return 0;
    }
  }-*/;

  protected native void addToMap(JavaScriptObject map)/*-{
    var el = this.@com.google.appinventor.client.editor.simple.components.MockCircle::getElement()();
    var circle = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    map.addLayer(circle);
    if (!circle.clickHandler) {
      while (el.lastChild) el.removeChild(el.lastChild);  // clear the div
      circle.clickHandler = function(e) {
        this.@com.google.appinventor.client.editor.simple.components.MockCircle::select()();
        if (e.originalEvent) e.originalEvent.stopPropagation();
      };
      circle.dragHandler = function(e) {
        var center = circle.getLatLng();
        var radius = circle.getRadius();
        this.@com.google.appinventor.client.editor.simple.components.MockCircle::updateCenter(DD)(center.lat, center.lng);
        this.@com.google.appinventor.client.editor.simple.components.MockCircle::updateRadius(D)(radius);
        if (e.originalEvent) e.originalEvent.stopPropagation();
      };
      circle.on('dragend editable:dragend editable:vertex:dragend editable:vertex:deleted editable:drawing:commit',
        circle.dragHandler, this);
      circle.on('click dragstart editable:dragstart editable:vertex:dragstart editable:vertex:clicked editable:drawing:click editable:drawing:clicked dragend editable:dragend',
        circle.clickHandler, this);
    }
    el = circle.getElement();
    el.style['pointer-events'] = 'auto';
    el.style['cursor'] = 'pointer';
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::setNativeTooltip(*)(
      this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::getTooltip()()
    );
  }-*/;

}
