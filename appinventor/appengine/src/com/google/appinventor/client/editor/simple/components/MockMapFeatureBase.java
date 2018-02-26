// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.widgetideas.graphics.client.Color;

import java.util.Collection;
import java.util.HashSet;

@SuppressWarnings("WeakerAccess")
public abstract class MockMapFeatureBase extends MockVisibleComponent implements MockMapFeature {
  public static final String PROPERTY_NAME_DESCRIPTION = "Description";
  public static final String PROPERTY_NAME_STROKEWIDTH = "StrokeWidth";
  public static final String PROPERTY_NAME_STROKECOLOR = "StrokeColor";
  public static final String PROPERTY_NAME_TITLE = "Title";
  public static final String CSS_PROPERTY_STROKEWIDTH = "stroke-width";
  public static final String CSS_PROPERTY_STROKE = "stroke";

  protected final SimplePanel panel;
  protected MockMap map = null;
  protected JavaScriptObject feature = null;
  protected String strokeColor = "#000000";
  protected int strokeWidth = 1;

  MockMapFeatureBase(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, icon);
    panel = new SimplePanel();
    panel.setStylePrimaryName("ode-SimpleMockComponent");
    initComponent(panel);
    this.unsinkEvents(Event.MOUSEEVENTS);
  }

  @Override
  public void addToMap(MockMap map) {
    if (panel.getWidget() != null) {
      panel.remove(panel.getWidget());
    }
    panel.setVisible(false);
    panel.setWidth("0");
    panel.setHeight("0");
    this.map = map;
    addToMap(map.getMapInstance());
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_STROKEWIDTH)) {
      setStrokeWidthProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_STROKECOLOR)) {
      setStrokeColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_VISIBLE)) {
      setVisibleProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_NAME)) {
      setNativeTooltip(newValue);
    }
  }

  @Override
  protected void onSelectedChange(boolean selected) {
    super.onSelectedChange(selected);
    setSelected(selected);
    setEditing(selected);
  }

  @Override
  public void onRemoved() {
    super.onRemoved();
    setNativeVisible(map.getMapInstance(), false);
  }

  String getTooltip() {
    return getPropertyValue(PROPERTY_NAME_NAME);
  }

  protected void setVisibleProperty(String text) {
    if (map == null) {
      // cannot change marker visibility if there is no map
      return;
    }
    boolean isVisible = Boolean.parseBoolean(text);
    setNativeVisible(map.getMapInstance(), isVisible);
  }

  protected void setStrokeWidthProperty(String text) {
    try {
      int width = Integer.parseInt(text);
      if (width < 0) {
        width = 0;
        getProperties().changePropertyValue(PROPERTY_NAME_STROKEWIDTH, "0");
        super.onPropertyChange(PROPERTY_NAME_STROKEWIDTH, "0");
      }
      strokeWidth = width;
    } catch(NumberFormatException e) {
      // pass
      getProperties().changePropertyValue(PROPERTY_NAME_STROKEWIDTH, "1");
      super.onPropertyChange(PROPERTY_NAME_STROKEWIDTH, "1");
      strokeWidth = 1;
    }
    setStrokeWidth(strokeWidth);
  }

  protected void setStrokeColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";
    }
    Color color = MockComponentsUtil.getColor(text);
    strokeColor = color.toString();
    setStrokeColor(strokeColor);
  }

  protected static String ensureUniqueName(String name, Collection<String> names) {
    names = new HashSet<String>(names);
    if (!names.contains(name)) {
      return name;
    }
    int i = name.length() - 1;
    while (name.charAt(i) >= '0' && name.charAt(i) <= '9') i--;
    String base = name.substring(0, i);
    int counter = 1;
    if (i < name.length() - 1) {
      counter = Integer.parseInt(name.substring(i + 1)) + 1;
    }
    while (names.contains(base + counter)) counter++;
    return base + counter;
  }

  // JSNI Methods
  public static native double distanceBetweenPoints(JavaScriptObject map, MockMap.LatLng point1, MockMap.LatLng point2)/*-{
    var pt1 = [point1.@com.google.appinventor.client.editor.simple.components.MockMap.LatLng::latitude,
          point1.@com.google.appinventor.client.editor.simple.components.MockMap.LatLng::longitude],
        pt2 = [point2.@com.google.appinventor.client.editor.simple.components.MockMap.LatLng::latitude,
          point2.@com.google.appinventor.client.editor.simple.components.MockMap.LatLng::longitude];
    return map.distance(pt1, pt2);
  }-*/;

  protected native void setEditing(boolean editing)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (feature) {
      if (editing) {
        feature.bringToFront();
        if (feature.editor) {
          feature.editor.enable();
        } else {
          feature.enableEdit();
        }
      } else if (feature.editor) {
        feature.editor.disable();
      }
    }
  }-*/;

  protected native void setNativeVisible(JavaScriptObject map, boolean visible)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (feature) {
      if (visible) {
        map.addLayer(feature);
        feature.getElement().style['pointer-events'] = 'auto';
        feature.getElement().style['cursor'] = 'pointer';
      } else {
        map.removeLayer(feature);
      }
    }
  }-*/;

  protected native void setSelected(boolean selected)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (!feature) return;
    var el = feature.getElement();
    if (!el) return;

    var classes = el.getAttribute('class').split(/\s+/);
    /// @type {?number}
    var index = null;
    for (var i = 0; i < classes.length; i++) {
      if (classes[i] === 'ode-SimpleMockMapFeature-selected') {
        if (selected) {
          return;  // nothing to change
        } else {
          index = i;
        }
      }
    }
    if (index !== null) {
      classes.splice(index, 1);  // remove selected class
    } else if (selected) {
      classes.push('ode-SimpleMockMapFeature-selected');
    } else {
      return;  // no change, so don't trigger a superfluous redraw
    }
    el.setAttribute('class', classes.join(' '));
  }-*/;

  protected native void setNativeTooltip(String tooltip)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (feature && feature.getElement()) {
      var el = feature.getElement(),
          text = document.createTextNode(tooltip),
          titleEl;
      if (el.firstElementChild && el.firstElementChild.tagName.toLowerCase() === 'title') {
        titleEl = el.firstElementChild;
        while(titleEl.lastChild) {
          titleEl.removeChild(titleEl.lastChild);
        }
      } else {
        titleEl = document.createElementNS('http://www.w3.org/2000/svg', 'title');
        el.appendChild(titleEl);
      }
      titleEl.appendChild(text);
    }
  }-*/;

  protected native void setStrokeColor(String color)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (feature) {
      feature.options.color = color;
      if (feature.getElement()) {
        feature.getElement().setAttribute('stroke', color);
      }
    }
  }-*/;

  protected native void setStrokeWidth(int weight)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (feature) {
      feature.options.weight = weight;
      if (feature.getElement()) {
        feature.getElement().setAttribute('stroke-width', weight.toString());
      }
    }
  }-*/;

  protected abstract void addToMap(JavaScriptObject map);

}
