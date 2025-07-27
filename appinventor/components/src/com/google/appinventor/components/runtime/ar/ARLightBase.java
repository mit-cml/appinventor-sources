// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;

@SimpleObject
public abstract class ARLightBase implements ARLight {

  protected ARView3D arView = null;

  @SuppressWarnings("WeakerAccess")
  protected ARLightBase(ARLightContainer container) {
    // any initializing that needs to be done here
  }

  @Override
  @SimpleProperty(description = "Returns the type of light as a String.")
  public String Type() { return ""; };

  @Override
  @SimpleProperty(description = "The color of the light's rays.")
  public int Color() { return 0; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Color(int color) {}


  @Override
  @SimpleProperty(description = "<p>The temperature of the light, in degrees Kelvin. " +
    "This, paired with the Color, determine the color of the light rays.  The default " +
    "value, 6,500 represents white light.  Lower values add a warmer, or yellow, effect " +
    "to the light, and greater values as a cooler, or blue, effect to the light.</p>" +
    "Values less than zero will be treated as zero, and values greater than 40,000 will " +
    "be treated as 40,000.")
  public float Temperature() { return 1.0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "6500")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Temperature(float temperature) {}


  @Override
  @SimpleProperty(description = "The brightness of the light.  The default value is 1000. " +
    "Lower values darken the light, and its color, whereas higher values brighten it.")
  public float Intensity() { return 1.0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1000")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Intensity(float intensity) {}

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY,
      defaultValue = "True")
  @SimpleProperty
  public boolean Visible() { return true; }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Specifies whether the component should be visible on the screen. "
          + "Value is true if the component is showing and false if hidden.")
  public void Visible(boolean visibility) {}

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return arView.getDispatchDelegate();
  }
}
