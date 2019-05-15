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
    description = "A component that displays a box in an ARView3D.  The box is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

  public final class BoxNode extends ARNodeBase implements ARBox {
    public BoxNode(final ARNodeContainer container) {
      super(container);
      // Additional updates
    }

    @Override
    @SimpleProperty(description = "How far, in centimeters, the BoxNode extends along the x-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode " +
      "will not appear.")
    public float WidthInCentimeters() { return 5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "5")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void WidthInCentimeters(float widthInCentimeters) {}

    @Override
    @SimpleProperty(description = "How far, in centimeters, the BoxNode extends along the y-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode " +
      "will not appear.")
    public float HeightInCentimeters() { return 5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "5")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void HeightInCentimeters(float heightInCentimeters) {}

    @Override
    @SimpleProperty(description = "How far, in centimeters, the BoxNode extends along the z-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero, the BoxNode " +
      "will not appear.")
    public float LengthInCentimeters() { return 5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "5")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void LengthInCentimeters(float lengthInCentimeters) {}

    @Override
    @SimpleProperty(description = "This determines how rounded the boxes corners will be.  " +
      "A value of zero specifies no rounded corners, and a value of half the length, " +
      "height, or width of the BoxNode (whichever is greater) makes it fully rounded, with " +
      "no straight edges.  Values less than zero will be set to zero.")
    public float CornerRadius() { return 0f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "0")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void CornerRadius(float cornerRadius) {}
  }
