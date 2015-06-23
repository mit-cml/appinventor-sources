// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
