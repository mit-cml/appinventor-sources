// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

/**
 * Mock PhoneNumberPicker component.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author markf@google.com (Mark Friedman)
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockPhoneNumberPicker extends MockButtonBase {

  /**
   * Component type name.
   */
  public static final String TYPE = "PhoneNumberPicker";

  /**
   * Creates a new MockPhoneNumberPicker component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockPhoneNumberPicker(SimpleEditor editor) {
    super(editor, TYPE, images.phonenumberpicker());
  }
}
