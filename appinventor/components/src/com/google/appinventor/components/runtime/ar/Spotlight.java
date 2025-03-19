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
description = "A component that adds spotlight in an ARView3D.  A spotlight lights " +
"illuminates the space in a cone-shaped area from a specific position and in a specific direction.",
category = ComponentCategory.AR)

@SimpleObject
public final class Spotlight extends ARLightBase implements ARSpotlight {
  public Spotlight(final ARLightContainer container) {
    super(container);
    // Additional updates
  }

  @Override
  @SimpleProperty(description = "The x position in centimeters of the Spotlight.")
  public float XPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void XPosition(float xPosition) {}

  @Override
  @SimpleProperty(description = "The y position in centimeters of the Spotlight.")
  public float YPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void YPosition(float yPosition) {}

  @Override
  @SimpleProperty(description = "The z position in centimeters of the Spotlight.")
  public float ZPosition() { return 0f; }

  @Override
  @SimpleProperty
  public void ZPosition(float zPosition) {}

  @Override
  @SimpleProperty(description = "The x rotation of the Spotlight in degrees.")
  public float XRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void XRotation(float xRotation) {}

  @Override
  @SimpleProperty(description = "The y rotation of the Spotlight in degrees.")
  public float YRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void YRotation(float yRotation) {}

  @Override
  @SimpleProperty(description = "The z rotation of the Spotlight in degrees.")
  public float ZRotation() { return 0f; }

  @Override
  @SimpleProperty
  public void ZRotation(float zRotation) {}

  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public boolean CastsShadows() { return true; }


  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(description = "If this property is set to true, then nodes " +
    "illuminated by the Spotlight will cast shadows, if ShowShadow for the node is true.  " +
    "Otherwise, the nodes it illuminates will not cast shadows.")
  public void CastsShadows(boolean castsShadows) {}

  @Override
  @SimpleProperty(description = "The distance, in centimeters, at which the light's intensity " +
    "starts to falloff or diminish.  Values less than zero will be treated as zero.")
  public float FalloffStartDistance() { return 1.0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FalloffStartDistance(float distance) {}

  @Override
  @SimpleProperty(description = "The distance, in centimeters, at which the light's intensity " +
    "goes to zero.  Points past this distance are not lit by the light.  Values less than zero " +
    "will be treated as zero.  When set to zero, the light's intensity does not falloff.")
  public float FalloffEndDistance() { return 1.0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FalloffEndDistance(float distance) {}

  @Override
  @SimpleProperty(description = "<p>This specifies the speed at which the light falloffs.  " +
    "None means that the light never falls off to zero.  Linear means that it decreases " +
    "evenly throughout, and Quadratic means it decreases faster the further away from the light.</p>" +
    "Valid values are: 0 (None), 1 (Linear), 2 (Quadratic)")
  public int FalloffType() { return 2; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AR_FALLOFF_TYPE, defaultValue = "2")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void FalloffType(int falloffType) {}

  @Override
  @SimpleProperty(description = "This specifies the area at which the Spotlight's intensity " +
    "is at full strength.  This area is defined by an angle in degrees.  From the SpotInnerAngle to " +
    "the SpotOuterAngle, the light's intensity transitions to zero.  Values less than zero will be " +
    "treated as zero, and values greater than 180 will be treated as 180.")
  public float SpotInnerAngle() { return 0.0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void SpotInnerAngle(float angle) {}

  @Override
  @SimpleProperty(description = "This specifies the area at which the Spotlight's intensity " +
    "is at non-zero strength.  This area is defined by an angle in degrees.  From the SpotInnerAngle to " +
    "the SpotOuterAngle, the light's intensity transitions to zero.  Outside the SpotOuterAngle, the " +
    "intensity is zero.  Values less than zero will be treated as zero, and values greater than 180 " +
    "will be treated as 180.")
  public float SpotOuterAngle() { return 45.0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "45")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void SpotOuterAngle(float angle) {}

  @Override
  @SimpleProperty(description = "This specifies the furthest distance away an object can " +
    "be in order for the Spotlight to make it cast shadows.  Objects further than this will not " +
    "cast shadows due to this Spotlight.  Values less than zero will be treated as zero.")
  public float MaximumDistanceForShadows() { return 100f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1000")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void MaximumDistanceForShadows(float distance) {}

  @Override
  @SimpleProperty(description = "This specifies the closest distance away an object can " +
    "be in order for the Spotlight to make it cast shadows.  Objects closer than this will not " +
    "cast shadows due to this Spotlight. Values less than zero will be treated as zero.")
  public float MinimumDistanceForShadows() { return 0.1f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void MinimumDistanceForShadows(float distance) {}

  @Override
  @SimpleProperty(description = "The color of the shadows that nodes illuminated " +
  "by the Spotlight will cast, if CastsShadows is true.")
  public int ShadowColor()  { return 0; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ShadowColor(int shadowColor) {}

  @Override
  @SimpleProperty(description = "The opacity of the shadows that nodes illuminated " +
  "by the Spotlight will cast, if CastsShadows is true.  This determines how " +
  "intense the shadows are.  Values less than zero will be treated as zero, and values " +
  "greater than 100 will be treated as 100.")
  public int ShadowOpacity()  { return 50; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "50")
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
  @SimpleFunction(description = "Change its rotation to shine light in the direction of the PointLight.")
  public void LookAtPointLight(ARPointLight light) {}

  @Override
  @SimpleFunction(description = "Change its rotation to shine light in the direction of the (x,y,z) position.")
  public void LookAtPosition(float x, float y, float z) {}

  @Override
  @SimpleFunction(description = "Changes the PointLight's position by (x,y,z).")
  public void MoveBy(float x, float y, float z) {}

  @Override
  @SimpleFunction(description = "Changes the PointLight's position to (x,y,z).")
  public void MoveTo(float x, float y, float z) {}

  @Override
  @SimpleFunction(description = "move a capsule node properties to detectedplane.")
  public void MoveToDetectedPlane(ARDetectedPlane targetPlane, Object p) {}

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between the Spotlight and a node.")
  public float DistanceToNode(ARNode node) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between two Spotlights.")
  public float DistanceToSpotlight(ARSpotlight light) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between the Spotlight and a PointLight.")
  public float DistanceToPointLight(ARPointLight light) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Caluates the distance, in centimeters, between the Spotlight and a DetectedPlane.")
  public float DistanceToDetectedPlane(ARDetectedPlane detectedPlane) { return 1.0f; }

  @Override
  @SimpleFunction(description = "Changes the Spotlight's x rotation by the given degrees.")
  public void RotateXBy(float degrees) {}

  @Override
  @SimpleFunction(description = "Changes the Spotlight's y rotation by the given degrees.")
  public void RotateYBy(float degrees) {}

  @Override
  @SimpleFunction(description = "Changes the Spotlight's z rotation by the given degrees.")
  public void RotateZBy(float degrees) {}

}
