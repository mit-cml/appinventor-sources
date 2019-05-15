// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.appinventor.components.common.ComponentConstants;

public class MockModelNode extends MockARNodeBase {
  public static final String TYPE = "ModelNode";
  private final Image iconImage = new Image(images.modelNodeBig());

  // Property names that we need to treat specially
  private static final String PROPERTY_NAME_FILLCOLOR = "FillColor";
  private static final String PROPERTY_NAME_FILLCOLOROPACITY = "FillColorOpacity";
  private static final String PROPERTY_NAME_TEXTURE = "Texture";
  private static final String PROPERTY_NAME_TEXTUREOPACITY = "TextureOpacity";
  private static final String PROPERTY_NAME_SHOWSHADOW = "ShowShadow";

  public MockModelNode(SimpleEditor editor) {
    super(editor, TYPE, images.modelNode());

    panel.setWidget(iconImage);
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_TEXTURE) ||
        propertyName.equals(PROPERTY_NAME_TEXTUREOPACITY) ||
        propertyName.equals(PROPERTY_NAME_FILLCOLOR) ||
        propertyName.equals(PROPERTY_NAME_FILLCOLOROPACITY) ||
        propertyName.equals(PROPERTY_NAME_SHOWSHADOW)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }
}
