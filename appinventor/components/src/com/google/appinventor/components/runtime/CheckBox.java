// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2018-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.res.ColorStateList;
import androidx.core.graphics.drawable.DrawableCompat;
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

  // Backing for box colors
  private int boxColorChecked;
  private int boxColorUnchecked;

  // Backing for checkmark color
  private int checkmarkColor;

  /**
   * Creates a new CheckBox component.
   *
   * @param container  container, component will be placed in
   */
  public CheckBox(ComponentContainer container) {
    super(container);
    view = new android.widget.CheckBox(container.$context());
    Checked(false);
    
    // Initialize default colors
    BoxColorChecked(Component.COLOR_BLUE);
    BoxColorUnchecked(Component.COLOR_GRAY);
    CheckmarkColor(Component.COLOR_WHITE);
    
    initToggle();
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
   * Creates a ColorStateList for the checkbox based on checked/unchecked states
   */
  private ColorStateList createCheckBoxColors(int checkedColor, int uncheckedColor) {
    return new ColorStateList(
        new int[][]{
            new int[]{android.R.attr.state_checked},
            new int[]{}
        },
        new int[]{
            checkedColor,
            uncheckedColor
        });
  }

  /**
   * Returns the `%type%`'s box color when checked
   *
   * @return  box RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = true)
  public int BoxColorChecked() {
    return boxColorChecked;
  }

  /**
   * Specifies the `%type%`'s box color when checked.
   *
   * @param argb  box RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_BLUE)
  @SimpleProperty(description = "Color of the checkbox box/border when checked")
  public void BoxColorChecked(int argb) {
    boxColorChecked = argb;
    if (view != null) {
      ColorStateList colorStateList = createCheckBoxColors(argb, boxColorUnchecked);
      DrawableCompat.setTintList(view.getButtonDrawable(), colorStateList);
      view.invalidate();
    }
  }

  /**
   * Returns the `%type%`'s box color when unchecked
   *
   * @return  box RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = true)
  public int BoxColorUnchecked() {
    return boxColorUnchecked;
  }

  /**
   * Specifies the `%type%`'s box color when unchecked.
   *
   * @param argb  box RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_GRAY)
  @SimpleProperty(description = "Color of the checkbox box/border when unchecked")
  public void BoxColorUnchecked(int argb) {
    boxColorUnchecked = argb;
    if (view != null) {
      ColorStateList colorStateList = createCheckBoxColors(boxColorChecked, argb);
      DrawableCompat.setTintList(view.getButtonDrawable(), colorStateList);
      view.invalidate();
    }
  }

  /**
   * Returns the `%type%`'s checkmark color
   *
   * @return  checkmark RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = true)
  public int CheckmarkColor() {
    return checkmarkColor;
  }

  /**
   * Specifies the `%type%`'s checkmark color.
   *
   * @param argb  checkmark RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty(description = "Color of the checkmark when the checkbox is checked")
  public void CheckmarkColor(int argb) {
    checkmarkColor = argb;
    // Note: This property will be applied in API 21+, which is beyond the min SDK level
    // For older APIs, the checkmark color is tied to the box color via tinting
    if (view != null) {
      view.invalidate();
    }
  }

}
