// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.res.ColorStateList;
import androidx.core.widget.CompoundButtonCompat;
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
 * ![Example of a CheckBox](images/checkbox.png)
 *
 * `CheckBox` components can detect user taps and can change their boolean state in response.
 *
 * A `CheckBox` component raises an event when the user taps it. There are many properties affecting
 * its appearance that can be set in the Designer or Blocks Editor.
 */
@DesignerComponent(version = YaVersion.CHECKBOX_COMPONENT_VERSION,
    description = "Checkbox that raises an event when the user clicks on it. " +
    "There are many properties affecting its appearance that can be set in " +
    "the Designer or Blocks Editor.",
    category = ComponentCategory.USERINTERFACE,
    iconName = "images/checkbox.png")
@SimpleObject
public final class CheckBox extends ToggleBase<android.widget.CheckBox> {

  // Backing for box color when checked
  private int checkedColor;

  // Backing for box color when unchecked
  private int uncheckedColor;

  /**
   * Creates a new CheckBox component.
   *
   * @param container  container, component will be placed in
   */
  public CheckBox(ComponentContainer container) {
    super(container);
    view = new android.widget.CheckBox(container.$context());
    Checked(false);
    CheckedColor(Component.COLOR_GREEN);
    UncheckedColor(Component.COLOR_LTGRAY);
    initToggle();
  }

  private ColorStateList createCheckBoxColors(int checked_color, int unchecked_color) {
    return new ColorStateList(new int[][]{
            new int[]{android.R.attr.state_checked},
            new int[]{}
            },
            new int[]{
                    checked_color,
                    unchecked_color
            });
  }

  /**
   * Set to `true`{:.logic.block} if the box is checked, `false`{:.logic.block} otherwise.
   *
   * @return  {@code true} indicates checked, {@code false} unchecked
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "True if the box is checked, false otherwise.")
  public boolean Checked() {
    return view.isChecked();
  }

  /**
   * Checked property setter method.
   *
   * @suppressdoc
   * @param value  {@code true} indicates checked, {@code false} unchecked
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty
  public void Checked(boolean value) {
    view.setChecked(value);
    view.invalidate();
  }

  /**
   * Returns the checkbox's color when checked.
   *
   * @return  checkbox RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Color of the checkbox when checked.")
  public int CheckedColor() {
    return checkedColor;
  }

  /**
   * Specifies the checkbox's color when checked.
   *
   * @param argb  checkbox RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_GREEN)
  @SimpleProperty
  public void CheckedColor(int argb) {
    checkedColor = argb;
    CompoundButtonCompat.setButtonTintList(view, createCheckBoxColors(argb, uncheckedColor));
    view.invalidate();
  }

  /**
   * Returns the checkbox's color when unchecked.
   *
   * @return  checkbox RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "Color of the checkbox when unchecked.")
  public int UncheckedColor() {
    return uncheckedColor;
  }

  /**
   * Specifies the checkbox's color when unchecked.
   *
   * @param argb  checkbox RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
  @SimpleProperty
  public void UncheckedColor(int argb) {
    uncheckedColor = argb;
    CompoundButtonCompat.setButtonTintList(view, createCheckBoxColors(checkedColor, argb));
    view.invalidate();
  }

}
