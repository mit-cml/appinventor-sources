// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * `Switch` components can detect user taps and can change their boolean state in response. They
 * are identical to {@link CheckBox}es except in appearance.
 *
 * Switches have an on (true) state and an off (false) state. A `Switch` component raises an event
 * when the user taps it to toggle between states.
 */
@DesignerComponent(version = YaVersion.SWITCH_COMPONENT_VERSION,
    description = "Toggle switch that raises an event when the user clicks on it. " +
    "There are many properties affecting its appearance that can be set in " +
    "the Designer or Blocks Editor.",
    category = ComponentCategory.USERINTERFACE,
    iconName = "images/switch.png")
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
   * Returns the `%type%`'s thumb color (button that toggles back and forth)
   * when the switch is ON/Checked
   *
   * @return  thumb RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public int ThumbColorActive() {
    return thumbColorActive;
  }

  /**
   * Specifies the `%type%`'s thumb color when switch is in the On state.
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
   * Returns the `%type%`'s thumb color (button that toggles back and forth)
   * when the switch is Off/Unchecked
   *
   * @return  thumb RGB color with alpha
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = true)
  public int ThumbColorInactive() {
    return thumbColorInactive;
  }

  /**
   * Specifies the `%type%`'s thumb color when switch is in the Off state.
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
   * Returns the `%type%`'s track color
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
   * Specifies the `%type%`'s track color when in the On state.
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

  /**
   * Specifies the `%type%`'s track color when in the Off state.
   * @param argb
   */
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
   * Returns true if the `%type%` is on.
   *
   * @return  {@code true} indicates checked, {@code false} unchecked
   */
  @SimpleProperty(
          category = PropertyCategory.BEHAVIOR)
  public boolean On() {
    return view.isChecked();
  }

  /**
   * True if the switch is in the On state, false otherwise.
   *
   * @internaldoc
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

  @Override
  @SimpleEvent(description = "User change the state of the `Switch` from On to Off or back.")
  public void Changed() {
    super.Changed();
  }

}
