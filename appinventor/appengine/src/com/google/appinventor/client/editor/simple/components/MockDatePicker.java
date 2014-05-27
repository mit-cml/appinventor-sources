// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

/**
 * Mock DatePicker component.
 */
public class MockDatePicker extends MockButtonBase {

  /**
   * Component type name.
   */
  public static final String TYPE = "DatePicker";

  /**
   * Creates a new MockDatePicker component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockDatePicker(SimpleEditor editor) {
    super(editor, TYPE, images.datePickerComponent());
  }
}