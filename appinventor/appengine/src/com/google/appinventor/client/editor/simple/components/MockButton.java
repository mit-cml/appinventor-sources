// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

/**
 * Mock Button component.
 *
 */
public final class MockButton extends MockButtonBase {

  /**
   * Component type name.
   */
  public static final String TYPE = "Button";

  /**
   * Creates a new MockButton component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockButton(SimpleEditor editor) {
    super(editor, TYPE, images.button());
  }
}
