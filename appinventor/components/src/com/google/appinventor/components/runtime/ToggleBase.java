// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.TextViewUtil;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Abstract base class for toggleable items with the ability to detect initialization, focus
 * change (mousing on or off of it), and user clicks.
 *
 */
@SimpleObject
public abstract class ToggleBase<T extends CompoundButton> extends ButtonBase<T>
        implements OnCheckedChangeListener, OnFocusChangeListener {
  /**
   * Creates a new ToggleBase component.
   *
   * @param container  container, component will be placed in
   */
  @SuppressWarnings("WeakerAccess")  // Could be used by extensions
  public ToggleBase(ComponentContainer container) {
    super(container);
  }

  @SuppressWarnings("WeakerAccess")  // Could be used by extensions
  protected void initToggle() {
    // Listen to focus changes
    view.setOnFocusChangeListener(this);
    view.setOnCheckedChangeListener(this);

    // Adds the component to its designated container
    container.$add(this);
    BackgroundColor(Component.COLOR_NONE);
    Enabled(true);
    fontTypeface = Component.TYPEFACE_DEFAULT;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
    FontSize(Component.FONT_DEFAULT_SIZE);
    Text("");
    TextColor(Component.COLOR_DEFAULT);

  }

  @Override
  public void click() {}

  @Override
  public View getView() {
    return view;
  }

  @Override
  public String Image() {
    return null;
  }

  @Override
  public void Image(String path) {}

  @Override
  public int Shape() {
    return 0;
  }

  @Override
  public void Shape(int shape) {
    this.shape = shape;
  }

  @Override
  public boolean ShowFeedback() {
    return false;
  }

  @Override
  public void ShowFeedback(boolean showFeedback) {}

  @Override
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Left, center, or right.",
      userVisible = false)
  public int TextAlignment() {
    return textAlignment;
  }

  @Override
  @SimpleProperty(userVisible = false)
  public void TextAlignment(int alignment) {
    textAlignment = alignment;
  }

  /**
   * Default Changed event handler.
   */
  @SimpleEvent
  public void Changed() {
    EventDispatcher.dispatchEvent(this, "Changed");
  }

  /**
   * Specifies the background color of the %type% as an alpha-red-green-blue
   * integer.
   *
   * @param argb  background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
  @SimpleProperty
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    if (argb != Component.COLOR_DEFAULT) {
      TextViewUtil.setBackgroundColor(view, argb);
    } else {
      TextViewUtil.setBackgroundColor(view, Component.COLOR_NONE);
    }
  }

  /**
   * Returns true if the %type% is active and clickable.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(
          category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return view.isEnabled();
  }

  // OnCheckedChangeListener implementation

  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Changed();
  }
}
