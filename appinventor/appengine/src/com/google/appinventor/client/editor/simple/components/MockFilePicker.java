// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

public class MockFilePicker extends MockButtonBase {
  public static final String TYPE = "FilePicker";

  /**
   * Creates a new MockFilePicker component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockFilePicker(SimpleEditor editor) {
    super(editor, TYPE, images.file());
  }
}
