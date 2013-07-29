// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.SimplePanel;

import com.google.gwt.user.client.ui.Image;

/**
 * Mock Slider component.
 *
 * @author M. Hossein Amerkashi - kkashi01@gmail.com
 */
public final class MockAdMob extends MockVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "AdMob";

  private static final int DEFAULT_WIDTH = 320;
  private static final int DEFAULT_HEIGHT = 40;

  // Widget for showing the mock slider
  private final SimplePanel admobWidget;

  // Large icon image for use in designer.  Smaller version is in the palette.
  private final Image largeImage = new Image(images.admobbaner());

  /**
   * Creates a new MockSlider component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockAdMob(SimpleEditor editor) {
    super(editor, TYPE, images.admob());

    // Initialize mock slider UI
    admobWidget = new SimplePanel();
    admobWidget.setStylePrimaryName("ode-SimpleMockComponent");

    admobWidget.setWidget(largeImage);

    initComponent(admobWidget);
  }


  @Override
  protected boolean isPropertyVisible(String propertyName) {
    //We don't want to allow user to change the slider height. S/he can only change the
    //slider width
    if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
	if (propertyName.equals(PROPERTY_NAME_WIDTH)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  @Override
  public int getPreferredWidth() {
    // The superclass uses getOffsetWidth, which won't work for us.
    return DEFAULT_WIDTH;
  }
  
  @Override
  public int getPreferredHeight() {
    // The superclass uses getOffsetWidth, which won't work for us.
    return DEFAULT_HEIGHT;
  }

  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

  }
}
