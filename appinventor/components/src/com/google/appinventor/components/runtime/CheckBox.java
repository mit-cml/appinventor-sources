// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

/**
 * Check box with the ability to detect initialization, focus
 * change (mousing on or off of it), and user clicks.
 *
 */
@DesignerComponent(version = YaVersion.CHECKBOX_COMPONENT_VERSION,
    description = "Checkbox that raises an event when the user clicks on it. " +
    "There are many properties affecting its appearance that can be set in " +
    "the Designer or Blocks Editor.",
    category = ComponentCategory.USERINTERFACE)
@SimpleObject
public final class CheckBox extends ToggleBase<android.widget.CheckBox> {

  /**
   * Creates a new CheckBox component.
   *
   * @param container  container, component will be placed in
   */
  public CheckBox(ComponentContainer container) {
    super(container);
    view = new android.widget.CheckBox(container.$context());
    Checked(false);
    initToggle();
  }

  /**
   * Returns true if the checkbox is checked.
   *
   * @return  {@code true} indicates checked, {@code false} unchecked
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Checked() {
    return view.isChecked();
  }

  /**
   * Checked property setter method.
   *
   * @param value  {@code true} indicates checked, {@code false} unchecked
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void Checked(boolean value) {
    view.setChecked(value);
    view.invalidate();
  }

}
