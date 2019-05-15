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
    description = "A component that displays a torus in an ARView3D.  The torus is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

  public final class TorusNode extends ARNodeBase implements ARTorus {
    public TorusNode(final ARNodeContainer container) {
      super(container);
      // Additional updates
    }

    @Override
    @SimpleProperty(description = "The ring radius defines the size of the overall " +
      "torus (or major radius) in centimeters.  Values less than zero will be treated " +
      "as their absolute value.  When set to zero, the TorusNode will not be shown.")
    public float RingRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "4")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void RingRadiusInCentimeters(float ringRadiusInCentimeters) {}

    @Override
    @SimpleProperty(description = "The pipe radius defines the size of the surface that " +
    "encircles the ring, or the pipe radius.  This can be thought of as the band of the ring.  " +
    "Values less than zero will be treated as their absolute value.  When set to zero, the " +
    "TorusNode will not be shown.")
    public float PipeRadiusInCentimeters() { return 0.05f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void PipeRadiusInCentimeters(float pipeRadiusInCentimeters) {}
  }
