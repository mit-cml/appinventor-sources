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
    description = "A component that displays a capsule in an ARView3D.  The capsule is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

  public final class CapsuleNode extends ARNodeBase implements ARCapsule {
    public CapsuleNode(final ARNodeContainer container) {
      super(container);
      // Additional updates
    }

    @Override
    @SimpleProperty(description = "How far, in centimeters, the CapsuleNode extends along the y-axis.  " +
      "Values less than zero will be treated as their absolute value.  When set to zero or when set to " +
      "less than double the CapRadius, the CapsuleNode will not appear.")
    public float HeightInCentimeters() { return 0.5f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "7")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void HeightInCentimeters(float heightInCentimeters) {}

    @Override
    @SimpleProperty(description = "The radius, in centimeters, of two hemispheres " +
      "or caps at the ends of a CapsuleNode.  Values less than zero will be treated as their " +
      "absolute values.  When set to zero or when set to greater than half of the Height, the " +
      "CapsuleNode will not appear.")
    public float CapRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "2")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void CapRadiusInCentimeters(float capRadiusInCentimeters) {}
  }
