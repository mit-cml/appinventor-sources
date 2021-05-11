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

public class MockRectangle extends MockPolygonBase {
  public static final String TYPE = "Rectangle";

  private static final String PROPERTY_NAME_EAST = "EastLongitude";
  private static final String PROPERTY_NAME_NORTH = "NorthLatitude";
  private static final String PROPERTY_NAME_SOUTH = "SouthLatitude";
  private static final String PROPERTY_NAME_WEST = "WestLongitude";

  private double east = 0;
  private double north = 0;
  private double south = 0;
  private double west = 0;

  public MockRectangle(SimpleEditor editor) {
    super(editor, TYPE, images.rectangle());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(ComponentConstants.RECTANGLE_PREFERRED_WIDTH + 2,
        ComponentConstants.RECTANGLE_PREFERRED_HEIGHT + 2);
    svgpanel.setInnerSVG("<path d=\"M0 0L50 0L50 30L0 30Z\" stroke-width=\"1\" stroke=\"black\" fill=\"red\" />");
    panel.setWidget(svgpanel);
    panel.setWidth(Integer.toString(ComponentConstants.RECTANGLE_PREFERRED_WIDTH + 2) + "px");
    panel.setHeight(Integer.toString(ComponentConstants.RECTANGLE_PREFERRED_HEIGHT + 2) + "px");

    initRectangle(south, west, north, east);
  }

  static MockRectangle fromGeoJSON(MockFeatureCollection parent, JSONObject properties, JavaScriptObject layer) {
    MockRectangle rectangle = new MockRectangle(parent.editor);
    rectangle.feature = layer;
    // TODO(ewpatton): Process properties
    return rectangle;
  }

  @Override
  public boolean onDrop(MockMap map, int x, int y, int offsetX, int offsetY) {
    MockMap.LatLng sw = map.projectFromXY(x - offsetX, y - offsetY + ComponentConstants.RECTANGLE_PREFERRED_HEIGHT);
    MockMap.LatLng ne = map.projectFromXY(x - offsetX + ComponentConstants.RECTANGLE_PREFERRED_WIDTH, y - offsetY);
    setPoints2(sw.toNative(), ne.toNative());
    changeProperty(PROPERTY_NAME_NORTH, Double.toString(ne.latitude));
    changeProperty(PROPERTY_NAME_WEST, Double.toString(sw.longitude));
    changeProperty(PROPERTY_NAME_SOUTH, Double.toString(sw.latitude));
    changeProperty(PROPERTY_NAME_EAST, Double.toString(ne.longitude));
    return true;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_EAST)) {
      setEastLongitudeProperty(newValue);
    } else if(propertyName.equals(PROPERTY_NAME_NORTH)) {
      setNorthLatitudeProperty(newValue);
    } else if(propertyName.equals(PROPERTY_NAME_SOUTH)) {
      setSouthLatitudeProperty(newValue);
    } else if(propertyName.equals(PROPERTY_NAME_WEST)) {
      setWestLongitudeProperty(newValue);
    }
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.RECTANGLE_PREFERRED_WIDTH + 2;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.RECTANGLE_PREFERRED_HEIGHT + 2;
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  private void setEastLongitudeProperty(String text) {
    try {
      east = Double.parseDouble(text);
      setBounds(south, west, north, east);
    } catch(NumberFormatException e) {
      // pass
    }
  }

  private void setNorthLatitudeProperty(String text) {
    try {
      north = Double.parseDouble(text);
      setBounds(south, west, north, east);
    } catch(NumberFormatException e) {
      // pass
    }
  }

  private void setSouthLatitudeProperty(String text) {
    try {
      south = Double.parseDouble(text);
      setBounds(south, west, north, east);
    } catch(NumberFormatException e) {
      // pass
    }
  }

  private void setWestLongitudeProperty(String text) {
    try {
      west = Double.parseDouble(text);
      setBounds(south, west, north, east);
    } catch(NumberFormatException e) {
      // pass
    }
  }

  private void updateBounds(double south, double west, double north, double east) {
    changeProperty(PROPERTY_NAME_SOUTH, Double.toString(south));
    changeProperty(PROPERTY_NAME_WEST, Double.toString(west));
    changeProperty(PROPERTY_NAME_NORTH, Double.toString(north));
    changeProperty(PROPERTY_NAME_EAST, Double.toString(east));
    super.onPropertyChange(PROPERTY_NAME_SOUTH, Double.toString(south));
    super.onPropertyChange(PROPERTY_NAME_WEST, Double.toString(west));
    super.onPropertyChange(PROPERTY_NAME_NORTH, Double.toString(north));
    super.onPropertyChange(PROPERTY_NAME_EAST, Double.toString(east));
  }

  // JSNI Methods
  private native void initRectangle(double south, double west, double north, double east)/*-{
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature =
      top.L.rectangle([[south, west], [north, east]], {
        className: 'leaflet-interactive',
        weight: 1,
        color: '#000',
        fillColor: '#f00',
        fillOpacity: 1,
        draggable: true,
        pointerEvents: 'auto'
      });
  }-*/;

  private native void setPoints2(JavaScriptObject se, JavaScriptObject nw)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    feature.setBounds(top.L.latLngBounds(se, nw));
  }-*/;

  protected native void addToMap(JavaScriptObject map)/*-{
    var el = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::getElement()();
    var rect = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    map.addLayer(rect);
    if (!rect.clickHandler) {
      while (el.lastChild) el.removeChild(el.lastChild);  // clear the div
      rect.clickHandler = function (e) {
        this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::select(*)(e);
        if (e.originalEvent) e.originalEvent.stopPropagation();
      };
      rect.dragHandler = function () {
        var bounds = rect.getBounds();
        this.@com.google.appinventor.client.editor.simple.components.MockRectangle::updateBounds(*)(
          bounds.getSouth(), bounds.getWest(), bounds.getNorth(), bounds.getEast()
        );
      };
      rect.on('click dragstart editable:dragstart editable:vertex:dragstart editable:vertex:clicked editable:drawing:click editable:drawing:clicked',
        rect.clickHandler, this);
      rect.on('dragend editable:dragend editable:vertex:dragend editable:vertex:deleted editable:drawing:commit',
        rect.dragHandler, this);
    }
    el = rect.getElement();
    el.style['pointer-events'] = 'auto';
    el.style['cursor'] = 'pointer';
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::setNativeTooltip(*)(
      this.@com.google.appinventor.client.editor.simple.components.MockPolygon::getTooltip()()
    );
    var isVisible = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::getVisibleProperty()();
    if (!isVisible) {
      map.removeLayer(rect);
    }
  }-*/;

  private native void setBounds(double south, double west, double north, double east)/*-{
    var rect = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature,
        map = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::map,
        mapjso = map ? map.@com.google.appinventor.client.editor.simple.components.MockMap::getMapInstance()() : null;
    if (rect) {
      rect.setBounds(top.L.latLngBounds([south, west], [north, east]));
      rect.redraw();
      if (rect.editor) {
        rect.editor.reset();
      } else if (mapjso) {
        rect.enableEdit(mapjso);
      }
    }
  }-*/;

}
