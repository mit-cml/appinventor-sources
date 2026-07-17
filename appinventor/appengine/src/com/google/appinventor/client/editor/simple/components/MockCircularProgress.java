// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2023 Kodular, All rights reserved
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.SVGPanel;
import com.google.gwt.user.client.ui.SimplePanel;


public final class MockCircularProgress extends MockVisibleComponent {
  public static final String TYPE = "CircularProgress";
  private static final String PROPERTY_NAME_COLOR = "Color";
  private static final String DEFAULT_COLOR = "&HFF0000FF"; // Blue.
  private static final int DEFAULT_SIZE = 40;

  private final SimplePanel panel; // Root panel that holds svgpanel.
  private final SVGPanel svgpanel;

  public MockCircularProgress(SimpleEditor editor) {
    super(editor, TYPE, images.circularProgress());

    panel = new SimplePanel();
    panel.setStylePrimaryName("ode-SimpleMockComponent");

    svgpanel = new SVGPanel();
    svgpanel.getElement().setAttribute("viewBox", "0 0 40 40");

    panel.setWidget(svgpanel);
    updateProgressSize(DEFAULT_SIZE, DEFAULT_SIZE);

    initComponent(panel);
    setIndeterminateColorProperty(DEFAULT_COLOR);
  }

  private void updateProgressSize(int width, int height) {
    panel.setPixelSize(width, height);
    svgpanel.setPixelSize(width, height);
  }

  private void setIndeterminateColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = DEFAULT_COLOR;
    }
    String color = MockComponentsUtil.getColor(text).toString();
    svgpanel.setInnerSVG("<circle cx=\"20\" cy=\"20\" r=\"16\" fill=\"none\" "
        + "stroke=\"" + color + "\" stroke-width=\"4\"/>");
  }

  @Override
  public int getPreferredWidth() {
    return DEFAULT_SIZE;
  }

  @Override
  public int getPreferredHeight() {
    return DEFAULT_SIZE;
  }

  @Override
  public void setPixelSize(int width, int height) {
    super.setPixelSize(width, height);
    updateProgressSize(width, height);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (propertyName.equals(PROPERTY_NAME_COLOR)) {
      setIndeterminateColorProperty(newValue);
    }
  }
}
