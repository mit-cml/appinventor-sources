// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;


/**
 * Superclass for HVArrangement based mock components
 *
 * @author markf@google.com (Mark Friedman)
 * @author sharon@google.com (Sharon Perl)
 */
abstract class MockHVArrangement extends MockContainer {

  // Form UI components
  protected final AbsolutePanel layoutWidget;

  /**
   * Creates a new MockHVArrangement component.
   */
  MockHVArrangement(SimpleEditor editor, String type, ImageResource icon, int orientation) {
    super(editor, type, icon, new MockHVLayout(orientation));

    if (orientation != ComponentConstants.LAYOUT_ORIENTATION_VERTICAL &&
        orientation != ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
      throw new IllegalArgumentException("Illegal orientation: " + orientation);
    }

    rootPanel.setHeight("100%");

    layoutWidget = new AbsolutePanel();
    layoutWidget.setStylePrimaryName("ode-SimpleMockContainer");
    layoutWidget.add(rootPanel);

    initComponent(layoutWidget);
  }
}
