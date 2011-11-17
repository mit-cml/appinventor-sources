// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Mock HorizontalArrangement component.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public final class MockHorizontalArrangement extends MockHVArrangement {

  /**
   * Component type name.
   */
  public static final String TYPE = "HorizontalArrangement";

  /**
   * Creates a new MockHorizontalArrangement component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockHorizontalArrangement(SimpleEditor editor) {
    super(editor, TYPE, images.horizontal(),
        ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL);
  }
}
