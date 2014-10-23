// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Mock Slider component.
 *
 * @author M. Hossein Amerkashi - kkashi01@gmail.com
 */
public final class MockSlider extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "Slider";

  private static final int DEFAULT_WIDTH = 70;

  // Widget for showing the mock slider
  private final SimplePanel sliderWidget;

  /**
   * Creates a new MockSlider component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockSlider(SimpleEditor editor) {
    super(editor, TYPE, images.slider());

    // Initialize mock slider UI
    sliderWidget = new SimplePanel();
    sliderWidget.setStylePrimaryName("ode-SimpleMockComponent");

    sliderWidget.setWidget(getIconImage());

    initComponent(sliderWidget);
  }


  @Override
  protected boolean isPropertyVisible(String propertyName) {
    //We don't want to allow user to change the slider height. S/he can only change the
    //slider width
    if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  @Override
  public int getPreferredWidth() {
    // The superclass uses getOffsetWidth, which won't work for us.
    return DEFAULT_WIDTH;
  }

  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

  }
}
