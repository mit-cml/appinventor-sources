// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.TextViewUtil;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

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
public abstract class ToggleBase<T extends CompoundButton> extends AndroidViewComponent
        implements OnCheckedChangeListener, OnFocusChangeListener {

  protected T view;

  // Backing for background color
  private int backgroundColor;

  // Backing for font typeface
  private int fontTypeface;

  // Backing for font bold
  private boolean bold;

  // Backing for font italic
  private boolean italic;

  // Backing for text color
  private int textColor;

  /**
   * Creates a new CheckBox component.
   *
   * @param container  container, component will be placed in
   */
  public ToggleBase(ComponentContainer container) {
    super(container);
  }

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
  public View getView() {
    return view;
  }

  /**
   * Default Changed event handler.
   */
  @SimpleEvent
  public void Changed() {
    EventDispatcher.dispatchEvent(this, "Changed");
  }

  /**
   * Default GotFocus event handler.
   */
  @SimpleEvent
  public void GotFocus() {
    EventDispatcher.dispatchEvent(this, "GotFocus");
  }

  /**
   * Default LostFocus event handler.
   */
  @SimpleEvent
  public void LostFocus() {
    EventDispatcher.dispatchEvent(this, "LostFocus");
  }

  /**
   * Returns the checkbox's background color as an alpha-red-green-blue
   * integer.
   *
   * @return  background RGB color with alpha
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE)
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the checkbox's background color as an alpha-red-green-blue
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
   * Returns true if the checkbox is active and clickable.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(
          category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return view.isEnabled();
  }

  /**
   * Specifies whether the checkbox should be active and clickable.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    TextViewUtil.setEnabled(view, enabled);
  }

  /**
   * Returns true if the checkbox's text should be bold.
   * If bold has been requested, this property will return true, even if the
   * font does not support bold.
   *
   * @return  {@code true} indicates bold, {@code false} normal
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          userVisible = false)
  public boolean FontBold() {
    return bold;
  }

  /**
   * Specifies whether the checkbox's text should be bold.
   * Some fonts do not support bold.
   *
   * @param bold  {@code true} indicates bold, {@code false} normal
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty(
          userVisible = false)
  public void FontBold(boolean bold) {
    this.bold = bold;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * Returns true if the checkbox's text should be italic.
   * If italic has been requested, this property will return true, even if the
   * font does not support italic.
   *
   * @return  {@code true} indicates italic, {@code false} normal
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          userVisible = false)
  public boolean FontItalic() {
    return italic;
  }

  /**
   * Specifies whether the checkbox's text should be italic.
   * Some fonts do not support italic.
   *
   * @param italic  {@code true} indicates italic, {@code false} normal
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty(
          userVisible = false)
  public void FontItalic(boolean italic) {
    this.italic = italic;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * Returns the checkbox's text's font size, measured in sp(scale-independent pixels).
   *
   * @return  font size in sp (scale-independent pixels)
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE)
  public float FontSize() {
    return TextViewUtil.getFontSize(view, container.$context());
  }

  /**
   * Specifies the checkbox's text's font size, measured in sp(scale-independent pixels).
   *
   * @param size  font size in sp(scale-independent pixels)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
          defaultValue = Component.FONT_DEFAULT_SIZE + "")
  @SimpleProperty
  public void FontSize(float size) {
    TextViewUtil.setFontSize(view, size);
  }

  /**
   * Returns the checkbox's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @return  one of {@link Component#TYPEFACE_DEFAULT},
   *          {@link Component#TYPEFACE_SERIF},
   *          {@link Component#TYPEFACE_SANSSERIF} or
   *          {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          userVisible = false)
  public int FontTypeface() {
    return fontTypeface;
  }

  /**
   * Specifies the checkbox's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @param typeface  one of {@link Component#TYPEFACE_DEFAULT},
   *                  {@link Component#TYPEFACE_SERIF},
   *                  {@link Component#TYPEFACE_SANSSERIF} or
   *                  {@link Component#TYPEFACE_MONOSPACE}
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE,
          defaultValue = Component.TYPEFACE_DEFAULT + "")
  @SimpleProperty(
          userVisible = false)
  public void FontTypeface(int typeface) {
    fontTypeface = typeface;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * Returns the text displayed by the checkbox.
   *
   * @return  checkbox caption
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE)
  public String Text() {
    return TextViewUtil.getText(view);
  }

  /**
   * Specifies the text displayed by the checkbox.
   *
   * @param text  new caption for checkbox
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
          defaultValue = "")
  @SimpleProperty
  public void Text(String text) {
    TextViewUtil.setText(view, text);
  }

  /**
   * Returns the checkbox's text color as an alpha-red-green-blue
   * integer.
   *
   * @return  text RGB color with alpha
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE)
  public int TextColor() {
    return textColor;
  }

  /**
   * Specifies the checkbox's text color as an alpha-red-green-blue
   * integer.
   *
   * @param argb  text RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty
  public void TextColor(int argb) {
    textColor = argb;
    if (argb != Component.COLOR_DEFAULT) {
      TextViewUtil.setTextColor(view, argb);
    } else {
      TextViewUtil.setTextColor(view, container.$form().isDarkTheme() ? Component.COLOR_WHITE : Component.COLOR_BLACK);
    }
  }

  // OnCheckedChangeListener implementation

  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Changed();
  }

  // OnFocusChangeListener implementation

  public void onFocusChange(View previouslyFocused, boolean gainFocus) {
    if (gainFocus) {
      GotFocus();
    } else {
      LostFocus();
    }
  }
}
