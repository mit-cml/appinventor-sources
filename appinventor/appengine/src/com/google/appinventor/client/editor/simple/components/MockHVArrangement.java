// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidHorizontalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLengthPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidVerticalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.properties.BadPropertyEditorException;
import com.google.appinventor.client.properties.Property;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;


/**
 * Superclass for HVArrangement based mock components
 *
 * @author markf@google.com (Mark Friedman)
 * @author sharon@google.com (Sharon Perl)
 * @author hal@mit.edu (Hal Abelson) (added adjust alignment dropdowns)
 */
public class MockHVArrangement extends MockContainer {
  //!!! why was this abstract?

  // Form UI components
  protected final AbsolutePanel layoutWidget;

  private MockHVLayout myLayout;
  
  private static final String PROPERTY_NAME_HORIZONTAL_ALIGNMENT = "AlignHorizontal";
  private static final String PROPERTY_NAME_VERTICAL_ALIGNMENT = "AlignVertical";
  
  private YoungAndroidHorizontalAlignmentChoicePropertyEditor myHAlignmentPropertyEditor;
  private YoungAndroidVerticalAlignmentChoicePropertyEditor myVAlignmentPropertyEditor;
  
  private boolean initialized = false;
  
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
    try {
      myHAlignmentPropertyEditor = PropertiesUtil.getHAlignmentEditor(properties);
      myVAlignmentPropertyEditor = PropertiesUtil.getVAlignmentEditor(properties);
    } catch (BadPropertyEditorException e) {
      OdeLog.log(MESSAGES.badAlignmentPropertyEditorForArrangement());
      return;
    };
    enableAndDisableDropdowns();
    initialized = true;
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
    } else {
      if (propertyName.equals(PROPERTY_NAME_WIDTH) || propertyName.equals(PROPERTY_NAME_HEIGHT)) {
        adjustAlignmentDropdowns();
        refreshForm();
      }
    }
  }

  // enableAndDisable It should not be called until the component is initialized.
  // Otherwise, we'll get NPEs in trying to use myAlignmentPropertyEditor.
  private void adjustAlignmentDropdowns() {
    if (initialized) enableAndDisableDropdowns();
  }

  // If the width is automatic, the selector for horizontal alignment should be disabled.  
  // If the length is automatic, the selector for vertical alignment should be disabled.
  private void enableAndDisableDropdowns() {
    String width = properties.getProperty(MockVisibleComponent.PROPERTY_NAME_WIDTH).getValue();
    if (width.equals(YoungAndroidLengthPropertyEditor.CONST_AUTOMATIC)) {
      myHAlignmentPropertyEditor.disable();
    } else myHAlignmentPropertyEditor.enable();

    String height = properties.getProperty(MockVisibleComponent.PROPERTY_NAME_HEIGHT).getValue();
    if (height.equals(YoungAndroidLengthPropertyEditor.CONST_AUTOMATIC)) {
      myVAlignmentPropertyEditor.disable();
    } else myVAlignmentPropertyEditor.enable();
  }
      
  }

