// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
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
public abstract class ToggleBase<T extends CompoundButton> extends AndroidViewComponent
        implements OnCheckedChangeListener, OnFocusChangeListener, AccessibleComponent {

  protected T view;

  // Backing for background color
  private int backgroundColor;

  // Backing for font typeface
  private String fontTypeface;

  // Backing for font bold
  private boolean bold;

  // Backing for font italic
  private boolean italic;

  // Backing for text color
  private int textColor;

  // Whether the text is big or not
  private boolean isBigText = false;

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
    TextViewUtil.setFontTypeface(container.$form(), view, fontTypeface, bold, italic);
    FontSize(Component.FONT_DEFAULT_SIZE);
    Text("");
    TextColor(Component.COLOR_DEFAULT);

  }

  @Override
  public View getView() {
    return view;
  }


  @Override
  public void setHighContrast(boolean isHighContrast) {

  }

  @Override
  public boolean getHighContrast() {
    return false;
  }

  @Override
  public void setLargeFont(boolean isLargeFont) {
    if (TextViewUtil.getFontSize(view, container.$context()) == 24.0 || TextViewUtil.getFontSize(view, container.$context()) == Component.FONT_DEFAULT_SIZE) {
      if (isLargeFont) {
        TextViewUtil.setFontSize(view, 24);
      } else {
        TextViewUtil.setFontSize(view, Component.FONT_DEFAULT_SIZE);
      }
    }
  }

  @Override
  public boolean getLargeFont() {
    return isBigText;
  }

  /**
   * User tapped and released the `%type%`.
   */
  @SimpleEvent(description = "User tapped and released the %type%.")
  public void Changed() {
    EventDispatcher.dispatchEvent(this, "Changed");
  }

  /**
   * `%type%` became the focused component.
   */
  @SimpleEvent(description = "%type% became the focused component.")
  public void GotFocus() {
    EventDispatcher.dispatchEvent(this, "GotFocus");
  }

  /**
   * `%type%` stopped being the focused component.
   */
  @SimpleEvent(description = "%type% stopped being the focused component.")
  public void LostFocus() {
    EventDispatcher.dispatchEvent(this, "LostFocus");
  }

  /**
   * Specifies the background color of the `%type%` as an alpha-red-green-blue
   * integer.
   *
   * @param argb  background RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
          defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
  @SimpleProperty(description = "The background color of the %type% as an alpha-red-green-blue integer.")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    if (argb != Component.COLOR_DEFAULT) {
      TextViewUtil.setBackgroundColor(view, argb);
    } else {
      TextViewUtil.setBackgroundColor(view, Component.COLOR_NONE);
    }
  }

  /**
   * Returns the background color of the `%type%` as an alpha-red-green-blue
   * integer.
   *
   * @suppressdoc
   * @return  background RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies whether the `%type%` should be active and clickable.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "True")
  @SimpleProperty(description = "True if the %type% is active and clickable.")
  public void Enabled(boolean enabled) {
    TextViewUtil.setEnabled(view, enabled);
  }

  /**
   * Returns true if the `%type%` is active and clickable.
   *
   * @suppressdoc
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return view.isEnabled();
  }

  /**
   * Specifies whether the text of the `%type%` should be bold.
   * Some fonts do not support bold.
   *
   * @param bold  {@code true} indicates bold, {@code false} normal
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(
      userVisible = false,
      description = "Set to true if the text of the %type% should be bold.")
  public void FontBold(boolean bold) {
    this.bold = bold;
    TextViewUtil.setFontTypeface(container.$form(), view, fontTypeface, bold, italic);
  }

  /**
   * Returns true if the text of the `%type%` should be bold.
   * If bold has been requested, this property will return true, even if the
   * font does not support bold.
   *
   * @suppressdoc
   * @return  {@code true} indicates bold, {@code false} normal
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public boolean FontBold() {
    return bold;
  }

  /**
   * Specifies whether the text of the `%type%` should be italic.
   * Some fonts do not support italic.
   *
   * @param italic  {@code true} indicates italic, {@code false} normal
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(
      userVisible = false,
      description = "Set to true if the text of the %type% should be italic.")
  public void FontItalic(boolean italic) {
    this.italic = italic;
    TextViewUtil.setFontTypeface(container.$form(), view, fontTypeface, bold, italic);
  }

  /**
   * Returns true if the text of the `%type%` should be italic.
   * If italic has been requested, this property will return true, even if the
   * font does not support italic.
   *
   * @suppressdoc
   * @return  {@code true} indicates italic, {@code false} normal
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public boolean FontItalic() {
    return italic;
  }

  /**
   * Specifies the text font size of the `%type%`, measured in sp(scale-independent pixels).
   *
   * @param size  font size in sp(scale-independent pixels)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = Component.FONT_DEFAULT_SIZE + "")
  @SimpleProperty(description = "Specifies the text font size of the %type% in scale-independent "
      + "pixels.")
  public void FontSize(float size) {
    if (Math.abs(size-Component.FONT_DEFAULT_SIZE)<.01 || Math.abs(size-24)<.01) {
      if (isBigText || container.$form().BigDefaultText()) {
        TextViewUtil.setFontSize(view, 24);
      }
      else {
        TextViewUtil.setFontSize(view, Component.FONT_DEFAULT_SIZE);
      }
    }
    else {
      TextViewUtil.setFontSize(view, size);
    }
  }

  /**
   * Returns the text font size of the `%type%`, measured in sp(scale-independent pixels).
   *
   * @suppressdoc
   * @return  font size in sp (scale-independent pixels)
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public float FontSize() {
    return TextViewUtil.getFontSize(view, container.$context());
  }

  /**
   * Specifies the text font face of the `%type%` as default, serif, sans
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
      description = "Specifies the text font face of the %type%.",
      userVisible = false)
  public void FontTypeface(String typeface) {
    fontTypeface = typeface;
    TextViewUtil.setFontTypeface(container.$form(), view, fontTypeface, bold, italic);
  }

  /**
   * Returns the text font face of the `%type%` as default, serif, sans
   * serif, or monospace.
   *
   * @suppressdoc
   * @return  one of {@link Component#TYPEFACE_DEFAULT},
   *          {@link Component#TYPEFACE_SERIF},
   *          {@link Component#TYPEFACE_SANSSERIF} or
   *          {@link Component#TYPEFACE_MONOSPACE}
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public String FontTypeface() {
    return fontTypeface;
  }

  /**
   * Specifies the text displayed by the `%type%`.
   *
   * @param text  new caption for toggleable button
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(description = "Specifies the text displayed by the %type%.")
  public void Text(String text) {
    TextViewUtil.setText(view, text);
  }

  /**
   * Returns the text displayed by the `%type%`.
   *
   * @suppressdoc
   * @return  toggle's caption
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Text() {
    return TextViewUtil.getText(view);
  }

  /**
   * Specifies the text color of the `%type%` as an alpha-red-green-blue
   * integer.
   *
   * @param argb  text RGB color with alpha
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
  @SimpleProperty(description = "Specifies the text color of the %type% as an "
      + "alpha-red-green-blue integer.")
  public void TextColor(int argb) {
    textColor = argb;
    if (argb != Component.COLOR_DEFAULT) {
      TextViewUtil.setTextColor(view, argb);
    } else {
      TextViewUtil.setTextColor(view, container.$form().isDarkTheme() ? Component.COLOR_WHITE : Component.COLOR_BLACK);
    }
  }

  /**
   * Returns the text color of the `%type%` as an alpha-red-green-blue
   * integer.
   *
   * @suppressdoc
   * @return  text RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int TextColor() {
    return textColor;
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
