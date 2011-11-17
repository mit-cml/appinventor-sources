// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

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

  // Support for Z layers

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
    }
  }
}
