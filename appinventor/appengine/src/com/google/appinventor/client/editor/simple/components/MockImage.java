// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

/**
 * Mock Image component.
 *
 */
public final class MockImage extends MockImageBase {

  /**
   * Component type name.
   */
  public static final String TYPE = "Image";

  /**
   * Creates a new MockImage component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockImage(SimpleEditor editor) {
    super(editor, TYPE, images.image());
  }
}
