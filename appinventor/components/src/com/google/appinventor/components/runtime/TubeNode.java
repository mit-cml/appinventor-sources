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
    description = "A component that displays a tube in an ARView3D.  The tube " +
    "is positioned at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

  public final class TubeNode extends ARNodeBase implements ARTube {
    public TubeNode(final ARNodeContainer container) {
      super(container);
      // Additional updates
    }

    @Override
    @SimpleProperty(description = "How far, in centimeters, the TubeNode extends along the y-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the TubeNode " +
      "will not be shown.")
    public float HeightInCentimeters() { return 0.5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "8")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void HeightInCentimeters(float heightInCentimeters) {}

    @Override
    @SimpleProperty(description = "The outer radius of the TubeNode determines " +
      "the overall size of the cicular base and top.  Values less than zero will be treated " +
      "as ther absolute value.  When set to zero or when set to equal to or less than the " +
      "InnerRadius, the TubeNode will not be shown.")
    public float OuterRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "3")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void OuterRadiusInCentimeters(float outerRadiusInCentimeters) {}

    @Override
    @SimpleProperty(description = "The inner radius of the TubeNode determines " +
      "the size of the cicular cutout from the middle of the TubeNode.  Values " +
      "less than zero will be treated as ther absolute value.  When set to zero " +
      "or when set to equal to or greater than the OuterRadius, the TubeNode will not be shown.")
    public float InnerRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void InnerRadiusInCentimeters(float innerRadiusInCentimeters) {}
  }
