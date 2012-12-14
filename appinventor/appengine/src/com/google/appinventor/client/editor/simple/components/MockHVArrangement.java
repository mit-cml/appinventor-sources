// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
public abstract class MockHVArrangement extends MockContainer {


  // Form UI components
  protected final AbsolutePanel layoutWidget;

  private MockHVLayout myLayout;

  private static final String PROPERTY_NAME_HORIZONTAL_ALIGNMENT = "AlignHorizontal";
  private static final String PROPERTY_NAME_VERTICAL_ALIGNMENT = "AlignVertical";


 /**
   * Creates a new MockHVArrangement component.
   */
  MockHVArrangement(SimpleEditor editor, String type, ImageResource icon, int orientation) {
    // Note(Hal): This helper thing is a kludge because I really want to write:
    // myLayout = new MockHVLayout(orientation);
    // super(editor, type, icon, myLayout);
    // but Java won't let me do that.

    super(editor, type, icon, MockHVArrangementHelper.makeLayout(orientation));
    // Note(hal): There better not be any calls to MockHVArrangementHelper before the
    // next instruction.  Note that the Helper methods are synchronized to avoid possible
    // future problems if we ever have threads creating arrangements in parallel.
    myLayout = MockHVArrangementHelper.getLayout();

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


  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    // Note(hal): Is it necessary to call super here?
    super.onPropertyChange(propertyName, newValue);
    if  (propertyName.equals(PROPERTY_NAME_HORIZONTAL_ALIGNMENT)) {
      myLayout.setHAlignmentFlags(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_VERTICAL_ALIGNMENT)) {
      myLayout.setVAlignmentFlags(newValue);
      refreshForm();
    }
  }
}
