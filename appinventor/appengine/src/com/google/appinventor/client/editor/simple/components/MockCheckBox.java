// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * Mock CheckBox component, inherited from MockToggleBase
 *
 * @author lizlooney@google.com (Liz Looney), srlane@mit.edu (Susan Rati Lane)
 */
public final class MockCheckBox extends MockToggleBase<CheckBox> {

  /**
   * Component type name.
   */
  public static final String TYPE = "CheckBox";

  /**
   * Creates a new MockCheckbox component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockCheckBox(SimpleEditor editor) {
    super(editor, TYPE, images.checkbox());
    toggleWidget = new CheckBox();
    initWrapper(toggleWidget);
  }

  /*
   * Sets the checkbox's Text property to a new value.
   */
  protected void setTextProperty(String text) {
    toggleWidget.setText(text);
    updatePreferredSize();
  }

  /*
   * Sets the checkbox's Checked property to a new value.
   */
  private void setCheckedProperty(String text) {
    toggleWidget.setChecked(Boolean.parseBoolean(text));
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_CHECKED)) {
      setCheckedProperty(newValue);
      refreshForm();
    }
  }
}
