// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLengthPropertyEditor;

/**
 * Mock ImageSprite component.
 *
 */
public final class MockImageSprite extends MockImageBase implements MockSprite {
  int x;
  int y;

  /**
   * Component type name.
   */
  public static final String TYPE = "ImageSprite";

  /**
   * Creates a new MockImageSprite component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockImageSprite(SimpleEditor editor) {
    super(editor, TYPE, images.imageSprite());
  }

  @Override
  protected void addWidthHeightProperties() {
    // Percent based size will scale images strangely, so remove percent based sizes
    addProperty(PROPERTY_NAME_WIDTH, "" + LENGTH_PREFERRED, MESSAGES.widthPropertyCaption(),
        "Appearance", MESSAGES.ImageSprite__WidthPropertyDescriptions(),
        new YoungAndroidLengthPropertyEditor(false));
    addProperty(PROPERTY_NAME_HEIGHT, "" + LENGTH_PREFERRED, MESSAGES.heightPropertyCaption(),
        "Appearance", MESSAGES.ImageSprite__HeightPropertyDescriptions(),
        new YoungAndroidLengthPropertyEditor(false));
  }

  private void setXProperty(String text) {
    try {
      x = (int) Math.round(Double.parseDouble(text));
    } catch (NumberFormatException e) {
      // Don't change value if unparseable (should not happen).
    }
    MockCanvas mockCanvas = (MockCanvas) getContainer();
    // mockCanvas will be null for the MockImageSprite on the palette
    if (mockCanvas != null) {
      mockCanvas.reorderComponents(this);
    }
  }
  
  private void setYProperty(String text) {
    try {
      y = (int) Math.round(Double.parseDouble(text));
    } catch (NumberFormatException e) {
      // Don't change value if unparseable (should not happen).
    }
    MockCanvas mockCanvas = (MockCanvas) getContainer();
    // mockCanvas will be null for the MockImageSprite on the palette
    if (mockCanvas != null) {
      mockCanvas.reorderComponents(this);
    }
  }

  private void setZProperty(String text) {
    MockCanvas mockCanvas = (MockCanvas) getContainer();
    // mockCanvas will be null for the MockImageSprite on the palette
    if (mockCanvas != null) {
      mockCanvas.reorderComponents(this);
    }
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if (propertyName.equals(PROPERTY_NAME_Z)) {
      setZProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_X)) {
      setXProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_Y)) {
      setYProperty(newValue);
    } 
  }

  @Override
  public int getLeftX() {
    return x;
  }

  @Override
  public int getTopY() {
    return y;
  }

  @Override
  public int getXOffset() {
    return 0;
  }

  @Override
  public int getYOffset() {
    return 0;
  }
}
