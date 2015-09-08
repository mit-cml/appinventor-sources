// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
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
        new YoungAndroidLengthPropertyEditor(false));
    addProperty(PROPERTY_NAME_HEIGHT, "" + LENGTH_PREFERRED, MESSAGES.heightPropertyCaption(),
        new YoungAndroidLengthPropertyEditor(false));
  }

  // Support for Z layers

  private void setZProperty(String text) {
    MockCanvas mockCanvas = (MockCanvas) getContainer();
    // mockCanvas will be null for the MockImageSprite on the palette
    if (mockCanvas != null) {
      mockCanvas.reorderComponents(this);
    }
  }
  
  private void setXProperty(String text) {
    MockCanvas mockCanvas = (MockCanvas) getContainer();
    // mockCanvas will be null for the MockImageSprite on the palette
    if (mockCanvas != null) {
      mockCanvas.reorderComponents(this);
    }
  }
  
  private void setYProperty(String text) {
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
}
