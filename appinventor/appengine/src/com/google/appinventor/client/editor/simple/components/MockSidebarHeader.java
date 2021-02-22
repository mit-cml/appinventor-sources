// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Mock SidebarHeader component.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
public class MockSidebarHeader extends MockHVArrangement {
  public static final String TYPE = "SidebarHeader";

  /**
   * Creates a new MockHVArrangement component.
   *
   * @param editor
   */
  public MockSidebarHeader(SimpleEditor editor) {
    super(editor, TYPE, images.sidebarHeader(), ComponentConstants.LAYOUT_ORIENTATION_VERTICAL, ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
  }
}
