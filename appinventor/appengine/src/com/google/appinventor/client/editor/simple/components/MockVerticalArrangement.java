// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Mock VerticalArrangement component.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public final class MockVerticalArrangement extends MockHVArrangement {

  /**
   * Component type name.
   */
  public static final String TYPE = "VerticalArrangement";

  /**
   * Creates a new MockVerticalArrangement component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockVerticalArrangement(SimpleEditor editor) {
    super(editor, TYPE, images.vertical(),
        ComponentConstants.LAYOUT_ORIENTATION_VERTICAL);
  }
  
}
