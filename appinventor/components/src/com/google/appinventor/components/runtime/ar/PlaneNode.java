// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.AR3DFactory.*;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;


// TODO: either supply a simple quad or make one
@UsesAssets(fileNames = "plane.obj, Palette.png")
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a plane in an ARView3D.  The plane is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject
  public final class PlaneNode extends ARNodeBase implements ARPlane {

  // TODO: either supply a simple quad or make one
  private String objectModel = Form.ASSETS_PREFIX + "plane.obj";
  private String texture = Form.ASSETS_PREFIX + "Palette.png";

  public PlaneNode(final ARNodeContainer container) {
    super(container);
    // Additional updates
    Model( objectModel);
    Texture(texture);
    container.addNode(this);

  }


  @Override
  @SimpleProperty(description = "How far, in centimeters, the PlaneNode extends along the x-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the PlaneNode " +
    "will not be shown.")
  public float WidthInCentimeters() { return 0.5f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "6")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void WidthInCentimeters(float widthInCentimeters) {}

  @Override
  @SimpleProperty(description = "How far, in centimeters, the PlaneNode extends along the y-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the PlaneNode " +
    "will not be shown.")
  public float HeightInCentimeters() { return 0.5f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "2")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void HeightInCentimeters(float heightInCentimeters) {}

  @Override
  @SimpleProperty(description = "This determines how rounded the boxes corners will be.  " +
    "A value of zero specifies no rounded corners, and a value of half the height or " +
    "width of the PlaneNode (whichever is greater) makes it fully rounded, with no " +
    "straight edges.  Values less than zero will be treated as zero.")
  public float CornerRadius() { return 0f; }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void CornerRadius(float cornerRadius) {}

}
