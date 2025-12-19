// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.appinventor.components.common.ComponentConstants;

@SuppressWarnings("WeakerAccess")
public abstract class MockARLightBase extends MockVisibleComponent implements MockARLight {

  protected final SimplePanel panel;
  protected int xInDesigner = 0;
  protected int yInDesigner = 0;

  MockARLightBase(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, icon);
    panel = new SimplePanel();
    panel.setStylePrimaryName("ode-SimpleMockComponent");
    initComponent(panel);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.AR_NODE_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.AR_NODE_PREFERRED_HEIGHT;
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }
}
