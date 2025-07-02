// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.ar;

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
    description = "A component that displays a text in an ARView3D.  The text is positioned " +
      "at a point and can be colored or textured as well as rotated.",
    category = ComponentCategory.AR)

  @SimpleObject

  public final class TextNode extends ARNodeBase implements ARText {
    public TextNode(final ARNodeContainer container) {
      super(container);
      // Additional updates
    }

    @Override
    @SimpleProperty(description = "Text to display by the TextNode.  If this is " +
      "set to \"\", the TextNode will not be shown.")
    public String Text() { return ""; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void Text(String text) {}

    @Override
    @SimpleProperty(description = "The font size in centimeters.  Values less than " +
      "zero will be treated as their absolute value.  When set to zero, the TextNode " +
      "will not be shown.")
    public float FontSizeInCentimeters() { return 6.0f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "6.0")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void FontSizeInCentimeters(float fontSizeInCentimeters) {}

    // @Override
    // @SimpleProperty(description = "")
    // public boolean WrapText() { return false; }
    //
    // @Override
    // @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    //     defaultValue = "False")
    // @SimpleProperty
    // public void WrapText(boolean wrapText) {}

    // @Override
    // @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT,
    //                   defaultValue = Component.ALIGNMENT_NORMAL + "")
    // @SimpleProperty(description = "")
    // public int TextAlignment() { return 0; }
    //
    // @Override
    // @SimpleProperty
    // public void TextAlignment(int textAlignment) {}

    // @Override
    // @SimpleProperty(description = "")
    // public int Truncation() { return 0; }
    //
    // @Override
    // @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "0")
    // @SimpleProperty
    // public void Truncation(int truncation) {}

    @Override
    @SimpleProperty(description = "How far, in centimeters, the TextNode extends along the z-axis.  " +
      "Values less than zero will be treated as zero.")
    public float DepthInCentimeters() { return 1.0f; }

    @Override
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = "1.0")
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void DepthInCentimeters(float depthInCentimeters) {}

    // @Override
    // @SimpleProperty(description = "")
    // public float CornerRadius() { return 0f; }
    //
    // @Override
    // @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0")
    // @SimpleProperty
    // public void CornerRadius(float cornerRadius) {}


    /**
     * Functions from ARNodeBase that should not be user facing.
     */
    @Override
    @SimpleProperty(userVisible = false)
    public float Scale() { return 1f; }

    @Override
    public void Scale(float scalar) {}
  }
