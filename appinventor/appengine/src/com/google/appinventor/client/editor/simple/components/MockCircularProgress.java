// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2023 Kodular, All rights reserved
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.InlineHTML;


public final class MockCircularProgress extends MockVisibleComponent {


  public static final String TYPE = "CircularProgress";
  private static final String PROPERTY_NAME_COLOR = "Color";
  private InlineHTML progressBarWidget;

  public MockCircularProgress(SimpleEditor editor) {
    super(editor, TYPE, images.circularProgress());

    progressBarWidget = new InlineHTML();
    progressBarWidget.setStylePrimaryName("ode-SimpleMockComponent");
    progressBarWidget.setText("\u25ef");
    MockComponentsUtil.setWidgetFontSize(progressBarWidget, "35");
    MockComponentsUtil.setWidgetTextAlign(progressBarWidget, "1");
    MockComponentsUtil.setWidgetFontBold(progressBarWidget, "bold");
    initComponent(progressBarWidget);
  }

  private void setIndeterminateColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  //white
    }
    MockComponentsUtil.setWidgetTextColor(progressBarWidget, text);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (propertyName.equals(PROPERTY_NAME_COLOR)) {
      setIndeterminateColorProperty(newValue);
    }
  }
}
