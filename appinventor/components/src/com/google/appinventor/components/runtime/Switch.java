// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.SwitchCompat;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * Toggle switch with the ability to detect initialization, focus
 * change (mousing on or off of it), and user clicks.
 *
 */
@DesignerComponent(version = YaVersion.SWITCH_COMPONENT_VERSION,
    description = "Toggle switch that raises an event when the user clicks on it. " +
    "There are many properties affecting its appearance that can be set in " +
    "the Designer or Blocks Editor.",
    category = ComponentCategory.USERINTERFACE)
@SimpleObject
public final class Switch extends ToggleBase<CompoundButton> {

  // Backing for thumb color
  private int thumbColorActive;
  private int thumbColorInactive;

  // Backing for track color
  private int trackColorActive;
  private int trackColorInactive;

  private final Activity activity;
  private final SwitchCompat switchView;

  /**
   * Creates a new Switch component.
   *
   * @param container container, component will be placed in
   */
  public Switch(ComponentContainer container) {
    super(container);

    this.activity = container.$context();

    // Using AppCompat, Switch component is only supported in API 14 and higher
    // TODO: If we bump minSDK to 14, then we can change this to only use SwitchCompat.
    if (SdkLevel.getLevel() < SdkLevel.LEVEL_ICE_CREAM_SANDWICH) {
      switchView = null;
      view = new CheckBox(activity);
    } else {
      switchView = new SwitchCompat(activity);
      view = switchView;
    }

    On(false);

    ThumbColorActive(Component.COLOR_WHITE);
    ThumbColorInactive(Component.COLOR_LTGRAY);
    TrackColorActive(Component.COLOR_GREEN);
    TrackColorInactive(Component.COLOR_GRAY);
    initToggle();
  }

  private ColorStateList createSwitchColors(int active_color, int inactive_color) {
    return new ColorStateList(new int[][]{
            new int[]{android.R.attr.state_checked},
            new int[]{}
            },
            new int[]{
                    active_color,
                    inactive_color
            });
  }

  /**
   * Returns the switch's thumb color (button that toggles back and forth)
   * when the switch is ON/Checked
   *
   * @return  thumb RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
  public int ThumbColorActive() {
    return thumbColorActive;
  }

  /**
   * Specifies the switch's thumb color when switch is ON/Checked
   *
   * @param argb  thumb RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
  @SimpleProperty
  public void ThumbColorActive(int argb) {
    thumbColorActive = argb;
    if (switchView != null) {
      DrawableCompat.setTintList(switchView.getThumbDrawable(), createSwitchColors(argb, thumbColorInactive));
      view.invalidate();
    }
  }

  /**
   * Returns the switch's thumb color (button that toggles back and forth)
   * when the switch is Off/Unchecked
   *
   * @return  thumb RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = true)
  public int ThumbColorInactive() {
    return thumbColorInactive;
  }

  /**
   * Specifies the switch's thumb color when switch is Off/Unchecked
   *
   * @param argb  thumb RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
  @SimpleProperty
  public void ThumbColorInactive(int argb) {
    thumbColorInactive = argb;
    if (switchView != null) {
      DrawableCompat.setTintList(switchView.getThumbDrawable(), createSwitchColors(thumbColorActive, argb));
      view.invalidate();
    }
  }

  /**
   * Returns the switch's track color
   *
   * @return  track RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = true)
  public int TrackColorActive() {
    return trackColorActive;
  }
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = true)
  public int TrackColorInactive() {
    return trackColorInactive;
  }

  /**
   * Specifies the switch's track color
   *
   * @param argb  track RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_GREEN)
  @SimpleProperty(description = "Color of the toggle track when switched on", userVisible = true)
  public void TrackColorActive(int argb) {
    trackColorActive = argb;
    if (switchView != null) {
      DrawableCompat.setTintList(switchView.getTrackDrawable(), createSwitchColors(argb, trackColorInactive));
      view.invalidate();
    }
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
  @SimpleProperty(description = "Color of the toggle track when switched off", userVisible = true)
  public void TrackColorInactive(int argb) {
    trackColorInactive = argb;
    if (switchView != null) {
      DrawableCompat.setTintList(switchView.getTrackDrawable(), createSwitchColors(trackColorActive, argb));
      view.invalidate();
    }
  }

  /**
   * Returns true if the checkbox is checked.
   *
   * @return  {@code true} indicates checked, {@code false} unchecked
   */
  @SimpleProperty(
          category = PropertyCategory.BEHAVIOR)
  public boolean On() {
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
  public void On(boolean value) {
    view.setChecked(value);
    view.invalidate();
  }

}
