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
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a cone in an ARView3D.  The cone " +
    "is positioned at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

  public final class ConeNode extends ARNodeBase implements ARCone {
    public ConeNode(final ARNodeContainer container) {
      super(container);
      // Additional updates
    }

    @Override
    @SimpleProperty(description = "How far, in centimeters, the ConeNode extends along the y-axis.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the ConeNode " +
    "will not be shown.")
    public float HeightInCentimeters() { return 0.5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "7")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void HeightInCentimeters(float heightInCentimeters) {}

    @Override
    @SimpleProperty(description = "This defines the radius of the top of the ConeNode.  " +
      "A value of zero causes the cone to meet at a point at the top.  Values less than " +
      "zero will be treated as their absolute value.  If the BottomRadius is set to zero " +
      "and this is set to zero, the ConeNode will not be shown.")
    public float TopRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void TopRadiusInCentimeters(float radiusInCentimeters) {}

    @Override
    @SimpleProperty(description = "This defines the radius of the bottom of the ConeNode.  " +
      "A value of zero causes the cone to meet at a point at the bottom.  Values less than " +
      "zero will be treated as their absolute value.  If the BottomRadius is set to zero " +
      "and this is set to zero, the ConeNode will not be shown.")
    public float BottomRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "3")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void BottomRadiusInCentimeters(float radiusInCentimeters) {}

  }
