// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.util.AR3DFactory.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
description = "<p>A component that adds a directional light in an ARView3D.  A directional light " +
  "can be thought of like the sun.  It's light that is infinitely far away, and shines on everything " +
  "with the same intensity, regardless of how far away the lights are. A DirectionalLight lights " +
  "all objects with the same intensity and from the same direction.</p>" +
  "<p>By default, the light shines down the negative z-axis.</p>",
category = ComponentCategory.AR)

@SimpleObject
public final class DirectionalLight extends ARLightBase implements ARDirectionalLight {
  public DirectionalLight(final ARLightContainer container) {
    super(container);
    // Additional updates
  }

  @Override
  @SimpleProperty(description = "The x rotation of the DirectionalLight in degrees.")
  public float XRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void XRotation(float xRotation) {}

  @Override
  @SimpleProperty(description = "The y rotation of the DirectionalLight in degrees.")
  public float YRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void YRotation(float yRotation) {}

  @Override
  @SimpleProperty(description = "The z rotation of the DirectionalLight in degrees.")
  public float ZRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void ZRotation(float zRotation) {}

  @Override
  @SimpleProperty(description = "If this property is set to true, then nodes " +
    "illuminated by the DirectionalLight will cast shadows, if ShowShadow for the node is true.  " +
    "Otherwise, the nodes it illuminates will not cast shadows.")
  public boolean CastsShadows() { return true; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void CastsShadows(boolean castsShadows) {}

  @Override
  @SimpleProperty(description = "The color of the shadows that nodes illuminated " +
  "by the DirectionalLight will cast, if CastsShadows is true.")
  public int ShadowColor()  { return 0; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ShadowColor(int shadowColor) {}

  @Override
  @SimpleProperty(description = "The opacity of the shadows that nodes illuminated " +
  "by the DirectionalLight will cast, if CastsShadows is true.  This determines how " +
  "intense the shadows are.  Values less than zero will be treated as zero, and values " +
  "greater than 100 will be treated as 100.")
  public int ShadowOpacity()  { return 50; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "50")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ShadowOpacity(int shadowOpacity) {}

  // FUNCTIONS

  @Override
  @SimpleFunction(description = "Change its rotation to shine light in the direction of the node.")
  public void LookAtNode(ARNode node) {}

  @Override
  @SimpleFunction(description = "Change its rotation to shine light in the direction of the DetectedPlane.")
  public void LookAtDetectedPlane(ARDetectedPlane detectedPlane) {}

  @Override
  @SimpleFunction(description = "Change its rotation to shine light in the direction of the Spotlight.")
  public void LookAtSpotlight(ARSpotlight light) {}

  @Override
  @SimpleFunction(description = "Change its rotation to shine light in the direction of the Pointlight.")
  public void LookAtPointLight(ARPointLight light) {}

  @Override
  @SimpleFunction(description = "Change its rotation to shine light in the direction of the (x,y,z) position.")
  public void LookAtPosition(float x, float y, float z) {}

  @Override
  @SimpleFunction(description = "Changes the DirectionalLight's x rotation by the given degrees.")
  public void RotateXBy(float degrees) {}

  @Override
  @SimpleFunction(description = "Changes the DirectionalLight's y rotation by the given degrees.")
  public void RotateYBy(float degrees) {}

  @Override
  @SimpleFunction(description = "Changes the DirectionalLight's z rotation by the given degrees.")
  public void RotateZBy(float degrees) {}

}
