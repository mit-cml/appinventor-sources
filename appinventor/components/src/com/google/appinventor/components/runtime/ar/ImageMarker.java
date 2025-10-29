// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import java.util.List;
import java.util.ArrayList;

// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that allows for image detection in a ARView3D.",
    category = ComponentCategory.AR)
@SimpleObject
public final class ImageMarker implements ARImageMarker {
  protected ARView3D arView = null;
  protected String name = "";

  public ImageMarker(ARImageMarkerContainer container) {
    // added to container implicitly
    container.ImageMarkers().add(this);
  }

  @Override
  @SimpleProperty(
      description = "Name",
      category = PropertyCategory.APPEARANCE)
  public void setComponentName(String componentName) {
    this.name = componentName;
  }

  @Override
  @SimpleProperty(
      description = "The image file asset to be detected.",
      category = PropertyCategory.APPEARANCE)
  public String Image() { return ""; }


  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void Image(String image) {}


  @Override
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
                  description = "The width of the image, in centimeters, in the real-world.  " +
    "This is used to determine how far the image is from the camera when it is detected.  " +
    "This property must be greater than zero for the ImageMarker to be detected.  Values less " +
    "than zero will be treated as their absolute value.")
  public float PhysicalWidthInCentimeters() { return 0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
  public void PhysicalWidthInCentimeters(float width) {}

  @Override
  @SimpleProperty(description = "The height of the image, in centimeters, in the real-world.")
  public float PhysicalHeightInCentimeters() { return 0.5f; }

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return arView.getDispatchDelegate();
  }

  // Functions
  @Override
  @SimpleProperty(description = "The nodes that are following the ImageMarker.")
  public List<ARNode> AttachedNodes() { return new ArrayList<ARNode> (); }

  // Events
  @Override
  @SimpleEvent(description = "The ImageMarker's image has been detected in the real-world " +
    "for the first time.  This will only trigger if the PhysicalWidthInCentimeters is greater than zero, " +
    "the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.")
  public void FirstDetected() {}

  @Override
  @SimpleEvent(description = "The position of the detected real-world image has changed to (x,y,z).  " +
    "This will only trigger if the PhysicalWidthInCentimeters is greater than zero, the Image is set to " +
    "a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.")
  public void PositionChanged(float x, float y, float z) {}

  @Override
  @SimpleEvent(description = "The rotation of the detected real-world image has updated.  " +
    "This will only trigger if the PhysicalWidthInCentimeters is greater than zero, the Image " +
    "is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.")
  public void RotationChanged(float x, float y, float z) {}

  @Override
  @SimpleEvent(description = "The ImageMarker's Image is no longer detected in the view of the camera, " +
    "after having been in view.  This will only trigger if the PhysicalWidthInCentimeters is greater " +
    "than zero, the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking " +
    "or ImageTracking.")
  public void NoLongerInView() {}

  @Override
  @SimpleEvent(description = "The ImageMarker's Image has been detected in the view of the camera, " +
    "after having not been detected in the view.  This will only trigger if the " +
    "PhysicalWidthInCentimeters is greater than zero, the Image is set to a valid image asset, " +
    "and the ARView3D's TrackingType is WorldTracking or ImageTracking.")
  public void AppearedInView() {}

  @Override
  @SimpleEvent(description = "The ImageMarker's detection has been reset.  This is triggered by " +
  "calling ResetDetectedItems on the ARView3D or if the detection information is no longer saved by " +
  "the AR engine.  This will only trigger if the PhysicalWidthInCentimeters is greater than zero, " +
  "the Image is set to a valid image asset, and the ARView3D's TrackingType is WorldTracking or ImageTracking.")
  public void Reset() {}
}
