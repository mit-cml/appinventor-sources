// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;

import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidHorizontalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidVerticalAlignmentChoicePropertyEditor;

import com.google.appinventor.client.output.OdeLog;

import com.google.appinventor.client.properties.BadPropertyEditorException;

import com.google.appinventor.components.common.ComponentConstants;

import com.google.gwt.resources.client.ImageResource;

import com.google.gwt.user.client.ui.AbsolutePanel;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Superclass for RadioGroup based mock components
 *
 * @author markf@google.com (Mark Friedman)
 * @author sharon@google.com (Sharon Perl)
 * @author hal@mit.edu (Hal Abelson) (added adjust alignment dropdowns)
 * @author kkashi01@gmail.com (Hossein Amerkashi) (added Image and BackgroundColors)
 */
public class MockRadioGroup extends MockContainer {
  // Form UI components
  protected final AbsolutePanel layoutWidget;

  // Property names
  private static final String PROPERTY_NAME_HORIZONTAL_ALIGNMENT = "AlignHorizontal";
  private static final String PROPERTY_NAME_VERTICAL_ALIGNMENT = "AlignVertical";

  private String backgroundColor;

  private MockHVLayout myLayout;

  private YoungAndroidHorizontalAlignmentChoicePropertyEditor myHAlignmentPropertyEditor;
  private YoungAndroidVerticalAlignmentChoicePropertyEditor myVAlignmentPropertyEditor;

  private MockRadioButton checkedRadioButton;
  private boolean scrollAble;
  private int orientation;

  /**
   * Creates a new MockRadioGroup component.
   */
  public MockRadioGroup(SimpleEditor editor, String type, ImageResource icon, int orientation,
    boolean scrollable) {
    // Note(Hal): This helper thing is a kludge because I really want to write:
    // myLayout = new MockHVLayout(orientation);
    // super(editor, type, icon, myLayout);
    // but Java won't let me do that.

    super(editor, type, icon, MockRadioGroupHelper.makeLayout(orientation));
    // Note(hal): There better not be any calls to MockRadioGroupHelper before the
    // next instruction.  Note that the Helper methods are synchronized to avoid possible
    // future problems if we ever have threads creating arrangements in parallel.
    this.myLayout = MockRadioGroupHelper.getLayout();
    this.scrollAble = scrollable;
    this.checkedRadioButton = null;
    
    this.orientation = orientation;

    if (orientation != ComponentConstants.LAYOUT_ORIENTATION_VERTICAL &&
        orientation != ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
      throw new IllegalArgumentException("Illegal orientation: " + orientation);
    }

    rootPanel.setHeight("100%");

    layoutWidget = new AbsolutePanel();
    layoutWidget.setStylePrimaryName("ode-SimpleMockContainer");
    layoutWidget.add(rootPanel);

    initComponent(layoutWidget);
    try {
      myHAlignmentPropertyEditor = PropertiesUtil.getHAlignmentEditor(properties);
      myVAlignmentPropertyEditor = PropertiesUtil.getVAlignmentEditor(properties);
    } catch (BadPropertyEditorException e) {
      OdeLog.log(MESSAGES.badAlignmentPropertyEditorForArrangement());
      return;
    }
    adjustAlignmentDropdowns();
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    if  (propertyName.equals(PROPERTY_NAME_HORIZONTAL_ALIGNMENT)) {
      myLayout.setHAlignmentFlags(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_VERTICAL_ALIGNMENT)) {
      myLayout.setVAlignmentFlags(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    } else {
      if (propertyName.equals(PROPERTY_NAME_WIDTH) || propertyName.equals(PROPERTY_NAME_HEIGHT)) {
        refreshForm();
      }
    }
  }

  private void adjustAlignmentDropdowns() {
    if (scrollAble) {
      if (orientation == ComponentConstants.LAYOUT_ORIENTATION_VERTICAL) {
        myLayout.setVAlignmentFlags(ComponentConstants.GRAVITY_TOP + "");
        changeProperty(PROPERTY_NAME_VERTICAL_ALIGNMENT, ComponentConstants.GRAVITY_TOP + "");
        myVAlignmentPropertyEditor.disable();
      } else {
        myLayout.setHAlignmentFlags(ComponentConstants.GRAVITY_LEFT + "");
        changeProperty(PROPERTY_NAME_HORIZONTAL_ALIGNMENT, ComponentConstants.GRAVITY_LEFT+ "");
        myHAlignmentPropertyEditor.disable();
      }
      refreshForm();
    } else {
      myVAlignmentPropertyEditor.enable();
      myHAlignmentPropertyEditor.enable();
    }
  }

  /**
  * Sets the MockRadioButton that is checked in this MockRadioGroup.
  */
  public void setCheckedRadioButton(MockRadioButton button){
    this.checkedRadioButton = button;
  }

  /**
  * Returns the MockRadioButton that is checked in this MockRadioGroup.
  */
  public MockRadioButton getCheckedRadioButton(){
    return this.checkedRadioButton;
  }

  /**
  * Sets the button's BackgroundColor property to a new value.
  */
  private void setBackgroundColorProperty(String text) {
    backgroundColor = text;

    if (MockComponentsUtil.isDefaultColor(text)) {
      // CSS background-color for ode-SimpleMockButton (copied from Ya.css)
      text = "&HFFE8E8E8";
    }
    MockComponentsUtil.setWidgetBackgroundColor(layoutWidget, text);
  }
}