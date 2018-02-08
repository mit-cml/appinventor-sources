// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Color;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.HasFill;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureVisitor;

@SimpleObject
public abstract class MapFeatureBaseWithFill extends MapFeatureBase implements HasFill {
  private int fillColor = COLOR_RED;

  public MapFeatureBaseWithFill(MapFactory.MapFeatureContainer container,
      MapFeatureVisitor<Double> distanceComputation) {
    super(container, distanceComputation);
    FillColor(Color.RED);
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_RED)
  @SimpleProperty
  @Override
  public void FillColor(int argb) {
    fillColor = argb;
    map.getController().updateFeatureFill(this);
  }

  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "The paint color used to fill in the map feature.")
  @Override
  public int FillColor() {
    return fillColor;
  }
}
