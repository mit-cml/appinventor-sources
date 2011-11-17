// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

/**
 * Mock TextBox component.
 *
 */
public final class MockTextBox extends MockTextBoxBase {

  /**
   * Component type name.
   */
  public static final String TYPE = "TextBox";

  /**
   * Creates a new MockTextBox component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockTextBox(SimpleEditor editor) {
    super(editor, TYPE, images.textbox());
  }
}
