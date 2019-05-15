// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

// TODO: update the component version
/**
 * The component is listed as INTERNAL as it should not be in the pallette.
 * This component is created by the AR engine and made accessible by EventSetting
 * and properties on its ARView3D.
 */
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component placed at a detected real-world plane.",
    category = ComponentCategory.INTERNAL)

@SimpleObject
public class DetectedPlane implements ARDetectedPlane {
  protected ARView3D arView = null;

  public DetectedPlane(ARDetectedPlaneContainer container) {
    // Additional
  }

  @SimpleProperty(description = "The width, in centimeters, of the DetectedPlane.")
  public float WidthInCentimeters() { return 0.5f; }

  @SimpleProperty(description = "The height, in centimeters, of the DetectedPlane.")
  public float HeightInCentimeters() { return 0.5f; }

  @Override
  @SimpleProperty(description = "This property returns true is its a horizontal plane, " +
  "false otherwise, if it is a vertical plane.")
  public boolean IsHorizontal() { return false; }

  @Override
  @SimpleProperty(description = "The opacity of the DetectedPlane.  Values less than 0 " +
    "will be treated as 0, and values greater than 100 will be treated as 100.")
  public int Opacity() { return 100; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Opacity(int opacity) {}

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
        description = "The color of the DetectedPlane.  The default is None.  If the Texture " +
          "is set, the color is not shown.")
  public int FillColor() { return 0; }

  // TODO: change this default color to not black
  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FillColor(int color) {}

  @Override
  @SimpleProperty(description = "The opacity of the DetectedPlane's FillColor.  Values " +
    "less than 0 will be treated as 0, and values greater than 100 will be treated as 100.")
  public int FillColorOpacity() { return 100; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FillColorOpacity(int colorOpacity) {}

  @Override
  @SimpleProperty(description = "The image used to texture the DetectedPlane.  If set, the FillColor is not shown.")
  public String Texture()  { return ""; }

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  public void Texture(String texture) {}

  @Override
  @SimpleProperty(description = "The opacity of the DetectedPlane's Texture.  Values " +
    "less than 0 will be treated as 0, and values greater than 100 will be treated as 100.")
  public int TextureOpacity()  { return 100; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "100")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void TextureOpacity(int textureOpacity) {}


  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return arView.getDispatchDelegate();
  }
}
