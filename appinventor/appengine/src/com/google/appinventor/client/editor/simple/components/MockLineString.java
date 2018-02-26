// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

public class MockLineString extends MockMapFeatureBase {
  public static final String TYPE = "LineString";
  private static final String PROPERTY_NAME_POINTS = "PointsFromString";
  private JavaScriptObject svgContent = null;

  public MockLineString(SimpleEditor editor) {
    super(editor, TYPE, images.linestring());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(42, 42);
    svgpanel.setInnerSVG("<path d=\"M0 0L42.4264 42.4624\" stroke-width=\"3\" stroke=\"black\" />");
    panel.setWidget(svgpanel);
    panel.setWidth("42px");
    panel.setHeight("42px");

    initLine();
  }

  static MockLineString fromGeoJSON(MockFeatureCollection parent, JSONObject properties, JavaScriptObject layer) {
    MockLineString line = new MockLineString(parent.editor);
    line.feature = layer;
    line.setContainer(parent);
    String name = null;
    for (String key : properties.keySet()) {
      String value;
      if (key.equalsIgnoreCase(PROPERTY_NAME_STROKEWIDTH) || key.equalsIgnoreCase(CSS_PROPERTY_STROKEWIDTH)) {
        value = properties.get(key).isString().stringValue();
        line.changeProperty(PROPERTY_NAME_STROKEWIDTH, value);
        line.onPropertyChange(PROPERTY_NAME_STROKEWIDTH, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_STROKECOLOR) || key.equalsIgnoreCase(CSS_PROPERTY_STROKE)) {
        value = properties.get(key).isString().stringValue();
        line.changeProperty(PROPERTY_NAME_STROKECOLOR, value);
        line.onPropertyChange(PROPERTY_NAME_STROKECOLOR, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_TITLE)) {
        value = properties.get(key).isString().stringValue();
        line.changeProperty(PROPERTY_NAME_TITLE, value);
        line.onPropertyChange(PROPERTY_NAME_TITLE, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_DESCRIPTION)) {
        value = properties.get(key).isString().stringValue();
        line.changeProperty(PROPERTY_NAME_DESCRIPTION, value);
        line.onPropertyChange(PROPERTY_NAME_DESCRIPTION, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_NAME)) {
        name = properties.get(key).isString().stringValue();
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_VISIBLE)) {
        value = properties.get(key).isString().stringValue();
        line.changeProperty(PROPERTY_NAME_VISIBLE, value);
        line.onPropertyChange(PROPERTY_NAME_VISIBLE, value);
      }
    }
    if (name == null) {
      name = line.getPropertyValue(PROPERTY_NAME_TITLE);
    }
    name = name.replaceAll("[ \t]+", "_");
    if (name.equalsIgnoreCase("")) {
      name = ComponentsTranslation.getComponentName(TYPE) + "1";
    }
    name = ensureUniqueName(name, parent.editor.getComponentNames());
    line.changeProperty(PROPERTY_NAME_NAME, name);
    line.onPropertyChange(PROPERTY_NAME_NAME, name);
    line.getForm().fireComponentRenamed(line, ComponentsTranslation.getComponentName(TYPE));
    line.preserveLayerData();
    return line;
  }

  @Override
  public boolean onDrop(MockMap map, int x, int y, int offsetX, int offsetY) {
    MockMap.LatLng nw = map.projectFromXY(x - offsetX, y - offsetY);
    MockMap.LatLng se = map.projectFromXY(x - offsetX + 42, y - offsetY + 42);
    setPoints2(nw.toNative(), se.toNative());
    String pointstr = "[[" + nw.latitude + "," + nw.longitude + "],[" + se.latitude + "," + se.longitude + "]]";
    getProperties().changePropertyValue(PROPERTY_NAME_POINTS, pointstr);
    super.onPropertyChange(PROPERTY_NAME_POINTS, pointstr);
    return true;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_POINTS)) {
      setPointsStr(newValue);
    }
  }

  @Override
  public int getPreferredWidth() {
    return 42;
  }

  @Override
  public int getPreferredHeight() {
    return 42;
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  private void updatePointsStr(String points) {
    getProperties().changePropertyValue(PROPERTY_NAME_POINTS, points);
    super.onPropertyChange(PROPERTY_NAME_POINTS, points);
  }

  // JSNI methods
  private native void initLine()/*-{
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature =
      top.L.polyline([], {
        className: 'leaflet-interactive',
        weight: 3,
        color: '#000',
        draggable: true,
        pointerEvents: 'auto'
      });
  }-*/;

  private native void setPoints2(JavaScriptObject nw, JavaScriptObject se)/*-{
    var polyline = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    polyline.setLatLngs([nw, se]);
  }-*/;

  private native void setPointsStr(String values)/*-{
    try {
      if (values != '') {
        var points = JSON.parse(values);
        var polyline = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
        polyline.setLatLngs(points);
        if (polyline.editor) polyline.editor.reset();  // reset vertices of the editor
      }
    } catch(e) {
      // pass
    }
  }-*/;

  protected native void addToMap(JavaScriptObject map)/*-{
    var el = this.@com.google.appinventor.client.editor.simple.components.MockLineString::getElement()();
    var polyline = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    map.addLayer(polyline);
    if (!polyline.clickHandler) {
      while (el.lastChild) el.removeChild(el.lastChild);  // clear the div
      polyline.clickHandler = function(e) {
        this.@com.google.appinventor.client.editor.simple.components.MockLineString::select()();
        if (e.originalEvent) e.originalEvent.stopPropagation();
      };
      polyline.dragHandler = function() {
        var points = polyline.getLatLngs();
        var str = '[';
        for (var i = 0; i < points.length; i++) {
          if (i > 0) str += ',';
          str += '[' + points[i].lat + ',' + points[i].lng + ']';
        }
        str += ']';
        if (str == '[]') str = '';
        this.@com.google.appinventor.client.editor.simple.components.MockLineString::updatePointsStr(*)(str);
      };
      polyline.on('click dragstart editable:dragstart editable:vertex:dragstart editable:vertex:clicked',
        polyline.clickHandler, this);
      polyline.on('dragend editable:dragend editable:vertex:dragend editable:vertex:deleted',
        polyline.dragHandler, this);
    }
    el = polyline.getElement();
    el.style['pointer-events'] = 'auto';
    el.style['cursor'] = 'pointer';
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::setNativeTooltip(*)(
      this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::getTooltip()()
    );
  }-*/;

  private native void preserveLayerData()/*-{
    var line = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (line) {
      var latlngs = line.getLatLngs();
      var resultJson = [];
      if (latlngs[0][0] instanceof top.L.LatLng) {
        for (var i = 0; i < latlngs[0].length; i++) {
          resultJson.push([latlngs[0][i].lat, latlngs[0][i].lng]);
        }
      } else {
        for (var i = 0; i < latlngs.length; i++) {
          var part = [];
          for (var j = 0; j < latlngs[i].length; j++) {
            part.push([latlngs[i][j].lat, latlngs[i][j].lng]);
          }
          resultJson.push(part);
        }
      }
    }
    this.@com.google.appinventor.client.editor.simple.components.MockLineString::updatePointsStr(*)(JSON.stringify(resultJson));
  }-*/;

}
