// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;
import com.google.appinventor.components.common.ComponentConstants;

public class MockDirectionalLight extends MockARLightBase {
  public static final String TYPE = "DirectionalLight";
  private final Image iconImage = new Image(images.directionalLightBig());

  public MockDirectionalLight(SimpleEditor editor) {
    super(editor, TYPE, images.directionalLight());

    panel.setWidget(iconImage);
  }
}
