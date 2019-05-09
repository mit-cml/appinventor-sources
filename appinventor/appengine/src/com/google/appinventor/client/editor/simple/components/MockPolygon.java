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

public class MockPolygon extends MockPolygonBase {
  public static final String TYPE = "Polygon";

  private static final String PROPERTY_NAME_POINTS = "PointsFromString";
  private static final String PROPERTY_NAME_HOLEPOINTS = "HolePointsFromString";

  public MockPolygon(SimpleEditor editor) {
    super(editor, TYPE, images.polygon());

    SVGPanel svgpanel = new SVGPanel();
    svgpanel.setPixelSize(ComponentConstants.POLYGON_PREFERRED_WIDTH + 2,
        ComponentConstants.POLYGON_PREFERRED_HEIGHT + 2);
    svgpanel.setInnerSVG("<path d=\"M0 30L25 0L50 30Z\" stroke-width=\"1\" stroke=\"black\" fill=\"red\" />");
    panel.setWidget(svgpanel);
    panel.setWidth(Integer.toString(ComponentConstants.POLYGON_PREFERRED_WIDTH + 2) + "px");
    panel.setHeight(Integer.toString(ComponentConstants.POLYGON_PREFERRED_HEIGHT + 2) + "px");

    initPolygon();
  }

  static MockPolygon fromGeoJSON(MockFeatureCollection parent, JSONObject properties, JavaScriptObject layer) {
    MockPolygon polygon = new MockPolygon(parent.editor);
    polygon.feature = layer;
    String name = null;
    boolean hadFillColor = false, hadStrokeColor = false, hadStrokeWidth = false;
    for (String key : properties.keySet()) {
      if (key.equalsIgnoreCase(PROPERTY_NAME_STROKEWIDTH) || key.equalsIgnoreCase(CSS_PROPERTY_STROKEWIDTH)) {
        polygon.setStrokeWidthProperty(properties.get(key).isString().stringValue());
        hadStrokeWidth = true;
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_STROKECOLOR) || key.equalsIgnoreCase(CSS_PROPERTY_STROKE)) {
        polygon.setStrokeColorProperty(properties.get(key).isString().stringValue());
        hadStrokeColor = true;
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_FILLCOLOR) || key.equalsIgnoreCase(CSS_PROPERTY_FILL)) {
        polygon.getProperties().changePropertyValue(PROPERTY_NAME_FILLCOLOR, properties.get(key).isString().stringValue());
        hadFillColor = true;
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_NAME)) {
        name = properties.get(key).isString().stringValue();
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_VISIBLE)) {
        polygon.setVisibleProperty(properties.get(key).isString().stringValue());
      }
    }
    if (!hadFillColor) {
      polygon.getProperties().changePropertyValue(PROPERTY_NAME_FILLCOLOR, "&HFF448800");
    }
    if (!hadStrokeColor) {
      polygon.getProperties().changePropertyValue(PROPERTY_NAME_STROKECOLOR, "&HFF000000");
    }
    if (!hadStrokeWidth) {
      polygon.getProperties().changePropertyValue(PROPERTY_NAME_STROKEWIDTH, "1");
    }
    processFeatureName(polygon, parent, name);
    polygon.preserveLayerData();
    return polygon;
  }

  @Override
  public boolean onDrop(MockMap map, int x, int y, int offsetX, int offsetY) {
    MockMap.LatLng bottomLeft = map.projectFromXY(x - offsetX, y - offsetY + 30);
    MockMap.LatLng topMid = map.projectFromXY(x - offsetX + 25, y - offsetY);
    MockMap.LatLng bottomRight = map.projectFromXY(x - offsetX + 50, y - offsetY + 30);
    setPoints3(bottomLeft.toNative(), topMid.toNative(), bottomRight.toNative());
    String pointstr = "[[" + bottomLeft.latitude + "," + bottomLeft.longitude + "],[" + topMid.latitude + "," +
        topMid.longitude + "],[" + bottomRight.latitude + "," + bottomRight.longitude + "]]";
    getProperties().changePropertyValue(PROPERTY_NAME_POINTS, pointstr);
    getProperties().changePropertyValue(PROPERTY_NAME_HOLEPOINTS, "");
    super.onPropertyChange(PROPERTY_NAME_POINTS, pointstr);
    super.onPropertyChange(PROPERTY_NAME_HOLEPOINTS, "");
    return true;
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_POINTS)) {
      setPointsStr(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_HOLEPOINTS)) {
      setHolePointsStr(newValue);
    }
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.POLYGON_PREFERRED_WIDTH + 2;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.POLYGON_PREFERRED_HEIGHT + 2;
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

  private void updateHolePointsStr(String points) {
    getProperties().changePropertyValue(PROPERTY_NAME_HOLEPOINTS, points);
    super.onPropertyChange(PROPERTY_NAME_HOLEPOINTS, points);
  }

  // JSNI Methods
  private native void initPolygon()/*-{
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature =
      top.L.polygon([], {
        className: 'leaflet-interactive',
        weight: 1,
        color: '#000',
        fillColor: '#f00',
        fillOpacity: 1,
        draggable: true,
        pointerEvents: 'auto'
      });
  }-*/;

  private native void setPoints3(JavaScriptObject bl, JavaScriptObject tc, JavaScriptObject br)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    feature.setLatLngs([[bl, tc, br]]);
  }-*/;

  private native void setPointsStr(String values)/*-{
    try {
      var polygon = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature,
          latlngs = polygon && polygon.getLatLngs();
      if (polygon && values !== '') {
        var points = JSON.parse(values);
        if (!latlngs) {
          latlngs = [points];
        } else {
          latlngs[0] = points;
        }
        polygon.setLatLngs(latlngs);
        if (polygon.editor) polygon.editor.reset();  // reset vertices of the editor
      }
    } catch(e) {
      // pass
    }
  }-*/;

  private native void setHolePointsStr(String values)/*-{
    try {
      var polygon = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature,
          latlngs = polygon && polygon.getLatLngs(),
          reset = false;
      if (polygon && values !== '') {
        var points = JSON.parse(values);
        if (!latlngs) {
          latlngs = [[]];
        }
        latlngs.splice(1, latlngs.length);
        for (var i = 0; i < points.length; i++) {
          latlngs.push(points[i]);
        }
        polygon.setLatLngs(latlngs);
        reset = true;
      } else if (latlngs) {
        if (latlngs.length > 1) {
          latlngs.splice(1);  // remove all but outer ring of polygon
        }
        polygon.setLatLngs(latlngs);
        reset = true;
      }
      if (reset && polygon.editor) polygon.editor.reset();  // reset vertices of the editor
    } catch(e) {
      // pass
    }
  }-*/;

  protected native void addToMap(JavaScriptObject map)/*-{
    var el = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::getElement()();
    var polygon = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    map.addLayer(polygon);
    if (!polygon.clickHandler) {
      while (el.lastChild) el.removeChild(el.lastChild);  // clear the div
      polygon.clickHandler = function(e) {
        this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::select()();
        if (e.originalEvent) {
          if ((e.originalEvent.metaKey || e.originalEvent.ctrlKey) && polygon.editEnabled()) {
            polygon.editor.newHole(e.latlng);
          }
          e.originalEvent.stopPropagation();
        }
      };
      polygon.dragHandler = function() {
        var points = polygon.getLatLngs(),
            pointstr = '[',
            holepointstr = '[',
            i, j;
        if (typeof points[0][0] === 'number') {
          points = [points];
        }
        for (i = 0; i < points[0].length; i++) {
          if (i > 0) pointstr += ',';
          pointstr += '[' + points[0][i].lat + ',' + points[0][i].lng + ']';
        }
        for (j = 1; j < points.length; j++) {
          if (j > 1) holepointstr += ',[';
          else holepointstr += '[';
          for (i = 0; i < points[j].length; i++) {
            if (i > 0) holepointstr += ',';
            holepointstr += '[' + points[j][i].lat + ',' + points[j][i].lng + ']';
          }
          holepointstr += ']';
        }
        pointstr += ']';
        holepointstr += ']';
        if (pointstr === '[]') pointstr = '';
        if (holepointstr === '[]' || holepointstr === '[[]]') holepointstr = '';
        this.@com.google.appinventor.client.editor.simple.components.MockPolygon::updatePointsStr(*)(pointstr);
        this.@com.google.appinventor.client.editor.simple.components.MockPolygon::updateHolePointsStr(*)(holepointstr);
      };
      polygon.on('click dragstart editable:dragstart editable:vertex:dragstart editable:vertex:clicked editable:drawing:click editable:drawing:clicked',
        polygon.clickHandler, this);
      polygon.on('dragend editable:dragend editable:vertex:dragend editable:vertex:deleted editable:drawing:commit',
        polygon.dragHandler, this);
    }
    el = polygon.getElement();
    el.style['pointer-events'] = 'auto';
    el.style['cursor'] = 'pointer';
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::setNativeTooltip(*)(
      this.@com.google.appinventor.client.editor.simple.components.MockPolygon::getTooltip()()
    );
  }-*/;

  private native void preserveLayerData()/*-{
    var polygon = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (polygon) {
      var latlngs = polygon.getLatLngs();
      var resultJson = [];
      var holesJson = [];
      if (latlngs[0][0] instanceof top.L.LatLng) {
        for (var i = 0; i < latlngs[0].length; i++) {
          resultJson.push([latlngs[0][i].lat, latlngs[0][i].lng]);
        }
        for (var i = 1; i < latlngs.length; i++) {
          var hole = [];
          for (var j = 0; j < latlngs[i].length; j++) {
            hole.push([latlngs[i][j].lat, latlngs[i][j].lng]);
          }
          holesJson.push(hole);
        }
      } else {
        for (var i = 0; i < latlngs.length; i++) {
          // process polygon body
          var part = [];
          for (var j = 0; j < latlngs[i][0].length; j++) {
            part.push([latlngs[i][0][j].lat, latlngs[i][0][j].lng]);
          }
          resultJson.push(part);

          // process polygon holes
          var holes = [];
          var canSimplify = true;
          for (var j = 1; j < latlngs[i].length; j++) {
            var hole = [];
            for (var k = 0; k < latlngs[i][j].length; k++) {
              hole.push([latlngs[i][j][k].lat, latlngs[i][j][k].lng]);
            }
            holes.push(hole);
          }
          if (holes.length > 0) {
            canSimplify = false;
          }
          holesJson.push(holes);
        }
        if (canSimplify) {
          holesJson = '';
        }
      }
      this.@com.google.appinventor.client.editor.simple.components.MockPolygon::updatePointsStr(*)(JSON.stringify(resultJson));
      this.@com.google.appinventor.client.editor.simple.components.MockPolygon::updateHolePointsStr(*)(JSON.stringify(holesJson));
    }
  }-*/;

}
