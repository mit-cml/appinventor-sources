// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.widgetideas.graphics.client.Color;

public abstract class MockMapFeatureBaseWithFill extends MockMapFeatureBase {
  public static final String PROPERTY_NAME_FILLCOLOR = "FillColor";
  public static final String CSS_PROPERTY_FILL = "fill";

  protected String fillColor = "#FF0000";

  MockMapFeatureBaseWithFill(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, icon);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_FILLCOLOR)) {
      setFillColorProperty(newValue);
    }
  }

  protected void setFillColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFF0000";  // red
    }
    Color color = MockComponentsUtil.getColor(text);
    fillColor = color.toString();
    setFillColor(fillColor);
  }

  protected native void setFillColor(String color)/*-{
    var feature = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (feature) {
      feature.options.fillColor = color;
      if (feature.getElement()) {
        feature.getElement().setAttribute('fill', color);
      }
    }
  }-*/;


}
