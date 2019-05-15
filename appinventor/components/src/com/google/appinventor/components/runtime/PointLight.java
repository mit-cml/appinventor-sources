// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.AR3DFactory.*;

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
description = "A component that adds a point light in an ARView3D.  A point light is " +
"a light that lights the scene in all directions with equal intensity from a specific point in space.",
category = ComponentCategory.AR)

@SimpleObject
public final class PointLight extends ARLightBase implements ARPointLight {
  public PointLight(final ARLightContainer container) {
    super(container);
    // Additional updates
  }

  @Override
  @SimpleProperty(description = "The x position in centimeters of the PointLight.")
  public float XPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void XPosition(float xPosition) {}

  @Override
  @SimpleProperty(description = "The y position in centimeters of the PointLight.")
  public float YPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void YPosition(float yPosition) {}

  @Override
  @SimpleProperty(description = "The z position in centimeters of the PointLight.")
  public float ZPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void ZPosition(float zPosition) {}

  @Override
  @SimpleProperty(description = "The distance, in centimeters, at which the light's intensity " +
    "starts to falloff or diminish.  A value of 0 specifies no falloff.  Values less than zero " +
    "will be treated as zero.")
  public float FalloffStartDistance() { return 1.0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void FalloffStartDistance(float distance) {}

  @Override
  @SimpleProperty(description = "The distance, in centimeters, at which the light's intensity " +
    "goes to zero.  Points past this distance are not lit by the light.  Values less than zero " +
    "will be treated as zero.  When set to zero, the light's intensity does not falloff.")
  public float FalloffEndDistance() { return 1.0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void FalloffEndDistance(float distance) {}

  @Override
  @SimpleProperty(description = "<p>This specifies the speed at which the light falloffs.  " +
    "None means that the light never falls off to zero.  Linear means that it decreases " +
    "evenly throughout, and Quadratic means it decreases faster the further away from the light.</p>" +
    "Valid values are: 0 (None), 1 (Linear), 2 (Quadratic)")
  public int FalloffType() { return 2; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AR_FALLOFF_TYPE, defaultValue = "2")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void FalloffType(int fallOffType) {}

  @Override
  @SimpleFunction(description = "Changes the PointLight's position by (x,y,z).")
  public void MoveBy(float x, float y, float z) {}

  @Override
  @SimpleFunction(description = "Changes the PointLight's position to (x,y,z).")
  public void MoveTo(float x, float y, float z) {}

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between the PointLight and a node.")
  public float DistanceToNode(ARNode node) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between the PointLight and a Spotlight.")
  public float DistanceToSpotlight(ARSpotlight light) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between two PointLights.")
  public float DistanceToPointLight(ARPointLight light) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between the PointLight and a detectedPlane.")
  public float DistanceToDetectedPlane(ARDetectedPlane detectedPlane) { return 1.0f; }
}
