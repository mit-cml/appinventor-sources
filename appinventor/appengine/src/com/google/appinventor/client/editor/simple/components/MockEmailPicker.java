// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

/**
 * Mock EmailPicker component.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public final class MockEmailPicker extends MockTextBoxBase {

  /**
   * Component type name.
   */
  public static final String TYPE = "EmailPicker";

  /**
   * Creates a new MockEmailPicker component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockEmailPicker(SimpleEditor editor) {
    super(editor, TYPE, images.emailpicker());
  }
}
