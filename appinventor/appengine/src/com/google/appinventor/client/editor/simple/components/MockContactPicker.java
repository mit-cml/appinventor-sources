// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

/**
 * Mock ContactPicker component.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author markf@google.com (Mark Friedman)
 */
public final class MockContactPicker extends MockButtonBase {

  /**
   * Component type name.
   */
  public static final String TYPE = "ContactPicker";

  /**
   * Creates a new MockContactPicker component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockContactPicker(SimpleEditor editor) {
    super(editor, TYPE, images.contactpicker());
  }
}
