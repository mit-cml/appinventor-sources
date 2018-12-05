// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mock CheckBox component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockCheckBox extends MockToggleBase {

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

  /**
   * Class that extends CheckBox so we can use a protected constructor.
   *
   * <p/>The purpose of this class is to create a clone of the CheckBox
   * passed to the constructor. It will be used to determine the preferred size
   * of the CheckBox, without having the size constrained by its parent,
   * since the cloned CheckBox won't have a parent.
   */
  static class ClonedCheckBox extends CheckBox {
    ClonedCheckBox(CheckBox ptb) {
      // Get the Element from the CheckBox.
      // Call DOM.clone to make a deep clone of that element.
      // Pass that cloned element to the super constructor.
      super(DOM.clone(ptb.getElement(), true));
    }
  }

  /*
   * Sets the checkbox's Text property to a new value.
   */
  private void setTextProperty(String text) {
    ((CheckBox)toggleWidget).setText(text);
    updatePreferredSize();
  }

  /*
   * Sets the checkbox's Checked property to a new value.
   */
  private void setCheckedProperty(String text) {
    ((CheckBox)toggleWidget).setChecked(Boolean.parseBoolean(text));
  }

    @Override
  protected Widget createClonedWidget() {
    return new ClonedCheckBox((CheckBox)toggleWidget);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_TEXT)) {
      setTextProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_CHECKED)) {
      setCheckedProperty(newValue);
      refreshForm();
    }
  }
}
