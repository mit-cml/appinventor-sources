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
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

// TODO: update the component version
@DesignerComponent(version = YaVersion.CAMERA_COMPONENT_VERSION,
    description = "A component that displays a pyramid in an ARView3D.  The pyramid " +
    "is positioned at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

  public final class PyramidNode extends ARNodeBase implements ARPyramid {
    public PyramidNode(final ARNodeContainer container) {
      super(container);
      // Additional updates
    }

    @Override
    @SimpleProperty(description = "How far, in centimeters, the PyramidNode extends along the x-axis.  " +
      "Values less than zero will be treated as their absolute values.  When set to zero, the PyramidNode " +
      "will not be shown.")
    public float WidthInCentimeters() { return 0.04f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "4")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void WidthInCentimeters(float widthInCentimeters) {}

    @Override
    @SimpleProperty(description = "How far, in centimeters, the PyramidNode extends along the y-axis.  " +
      "Values less than zero will be treated as their absolute values.  When set to zero, the PyramidNode " +
      "will not be shown.")
    public float HeightInCentimeters() { return 0.04f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "4")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void HeightInCentimeters(float heightInCentimeters) {}

    @Override
    @SimpleProperty(description = "How far, in centimeters, the PyramidNode extends along the z-axis.  " +
      "Values less than zero will be treated as their absolute values.  When set to zero, the PyramidNode " +
      "will not be shown.")
    public float LengthInCentimeters() { return 0.04f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "4")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void LengthInCentimeters(float lengthInCentimeters) {}
  }
