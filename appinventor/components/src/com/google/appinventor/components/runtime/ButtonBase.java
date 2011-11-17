// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;

import java.io.IOException;

/**
 * Underlying base class for click-based components, not directly accessible to Simple programmers.
 *
 */
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public abstract class ButtonBase extends AndroidViewComponent
    implements OnClickListener, OnFocusChangeListener, OnLongClickListener {

  private static final String LOG_TAG = "ButtonBase";

  private final android.widget.Button view;

  // Backing for text alignment
  private int textAlignment;

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

  // Image path
  private String imagePath = "";

  // This is our handle on Android's nice 3-d default button.
  private Drawable defaultButtonDrawable;

  // This is our handle in Android's default button color states;
  private ColorStateList defaultColorStateList;

  // This is the Drawable corresponding to the Image property.
  // If an Image has never been set or if the most recent Image
  // could not be loaded, this is null.
  private Drawable backgroundImageDrawable;

  /**
   * Creates a new ButtonBase component.
   *
   * @param container  container, component will be placed in
   */
  public ButtonBase(ComponentContainer container) {
    super(container);
    view = new android.widget.Button(container.$context());

    // Save the default values in case the user wants them back later.
    defaultButtonDrawable = view.getBackground();
    defaultColorStateList = view.getTextColors();

    // Adds the component to its designated container
    container.$add(this);

    // Listen to clicks and focus changes
    view.setOnClickListener(this);
    view.setOnFocusChangeListener(this);
    view.setOnLongClickListener(this);

    // Default property values
    TextAlignment(Component.ALIGNMENT_CENTER);
    // BackgroundColor and Image are dangerous properties:
    // Once either of them is set, the 3D bevel effect for the button is
    // irretrievable, except by reloading defaultButtonDrawable, defined above.
    BackgroundColor(Component.COLOR_DEFAULT);
    Image("");
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
   * Indicates the cursor moved over the button so it is now possible
   * to click it.
   */
  @SimpleEvent
  public void GotFocus() {
    EventDispatcher.dispatchEvent(this, "GotFocus");
  }

  /**
   * Indicates the cursor moved away from the button so it is now no
   * longer possible to click it.
   */
  @SimpleEvent
  public void LostFocus() {
    EventDispatcher.dispatchEvent(this, "LostFocus");
  }

  /**
   * Returns the alignment of the button's text: center, normal
   * (e.g., left-justified if text is written left to right), or
   * opposite (e.g., right-justified if text is written left to right).
   *
   * @return  one of {@link Component#ALIGNMENT_NORMAL},
   *          {@link Component#ALIGNMENT_CENTER} or
   *          {@link Component#ALIGNMENT_OPPOSITE}
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public int TextAlignment() {
    return textAlignment;
  }

  /**
   * Specifies the alignment of the button's text: center, normal
   * (e.g., left-justified if text is written left to right), or
   * opposite (e.g., right-justified if text is written left to right).
   *
   * @param alignment  one of {@link Component#ALIGNMENT_NORMAL},
   *                   {@link Component#ALIGNMENT_CENTER} or
   *                   {@link Component#ALIGNMENT_OPPOSITE}
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_TEXTALIGNMENT,
                    defaultValue = Component.ALIGNMENT_CENTER + "")
  @SimpleProperty(userVisible = false)
  public void TextAlignment(int alignment) {
    this.textAlignment = alignment;
    TextViewUtil.setAlignment(view, alignment, true);
  }

  /**
   * Returns the path of the button's image.
   *
   * @return  the path of the button's image
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Image() {
    return imagePath;
  }

  /**
   * Specifies the path of the button's image.
   *
   * <p/>See {@link MediaUtil#determineMediaSource} for information about what
   * a path can be.
   *
   * @param path  the path of the button's image
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_ASSET,
      defaultValue = "")
  @SimpleProperty(description = "Specifies the path of the button's image.  " +
      "If there is both an Image and a BackgroundColor, only the Image will be " +
      "visible.")
  public void Image(String path) {
    // If it's the same as on the prior call and the prior load was successful,
    // do nothing.
    if (path.equals(imagePath) && backgroundImageDrawable != null) {
      return;
    }

    imagePath = (path == null) ? "" : path;

    // Clear the prior background image.
    backgroundImageDrawable = null;

    // Load image from file.
    if (imagePath.length() > 0) {
      try {
        backgroundImageDrawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
      } catch (IOException ioe) {
        // TODO(user): Maybe raise Form.ErrorOccurred.
        Log.e(LOG_TAG, "Unable to load " + imagePath);
        // Fall through with a value of null for backgroundImageDrawable.
      }
    }

    // Update the appearance based on the new value of backgroundImageDrawable.
    updateAppearance();
  }

  /**
   * Returns the button's background color as an alpha-red-green-blue
   * integer.
   *
   * @return  background RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Returns the button's background color")
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the button's background color as an alpha-red-green-blue
   * integer.  If the parameter is {@link Component#COLOR_DEFAULT}, the
   * original beveling is restored.  If an Image has been set, the color
   * change will not be visible until the Image is removed.
   *
   * @param argb background RGB color with alpha
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_COLOR,
                    defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty(description = "Specifies the button's background color. " +
      "The background color will not be visible if an Image is being displayed.")
  public void BackgroundColor(int argb) {
    backgroundColor = argb;
    updateAppearance();
  }

  // Update appearance based on values of backgroundImageDrawable and backgroundColor.
  // Images take precedence over background colors.
  private void updateAppearance() {
    // If there is no background image, the appearance depends solely on the background color.
    if (backgroundImageDrawable == null) {
      if (backgroundColor == Component.COLOR_DEFAULT) {
        // Restore original 3D bevel appearance.
        ViewUtil.setBackgroundDrawable(view, defaultButtonDrawable);
      } else {
        // Clear the background image.
        ViewUtil.setBackgroundDrawable(view, null);
        // Set to the specified color (possibly COLOR_NONE for transparent).
        TextViewUtil.setBackgroundColor(view, backgroundColor);
      }
      return;
    }

    ViewUtil.setBackgroundImage(view, backgroundImageDrawable);
  }

  /**
   * Returns true if the button is active and clickable.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean Enabled() {
    return TextViewUtil.isEnabled(view);
  }

  /**
   * Specifies whether the button should be active and clickable.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    TextViewUtil.setEnabled(view, enabled);
  }

  /**
   * Returns true if the button's text should be bold.
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
   * Specifies whether the button's text should be bold.
   * Some fonts do not support bold.
   *
   * @param bold  {@code true} indicates bold, {@code false} normal
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(
      userVisible = false)
  public void FontBold(boolean bold) {
    this.bold = bold;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * Returns true if the button's text should be italic.
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
   * Specifies whether the button's text should be italic.
   * Some fonts do not support italic.
   *
   * @param italic  {@code true} indicates italic, {@code false} normal
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(
      userVisible = false)
  public void FontItalic(boolean italic) {
    this.italic = italic;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * Returns the button's text's font size, measured in pixels.
   *
   * @return  font size in pixel
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      userVisible = false)
  public float FontSize() {
    return TextViewUtil.getFontSize(view);
  }

  /**
   * Specifies the button's text's font size, measured in pixels.
   *
   * @param size  font size in pixel
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = Component.FONT_DEFAULT_SIZE + "")
  @SimpleProperty(
      userVisible = false)
  public void FontSize(float size) {
    TextViewUtil.setFontSize(view, size);
  }

  /**
   * Returns the button's text's font face as default, serif, sans
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
   * Specifies the button's text's font face as default, serif, sans
   * serif, or monospace.
   *
   * @param typeface  one of {@link Component#TYPEFACE_DEFAULT},
   *                  {@link Component#TYPEFACE_SERIF},
   *                  {@link Component#TYPEFACE_SANSSERIF} or
   *                  {@link Component#TYPEFACE_MONOSPACE}
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_TYPEFACE,
      defaultValue = Component.TYPEFACE_DEFAULT + "")
  @SimpleProperty(
      userVisible = false)
  public void FontTypeface(int typeface) {
    fontTypeface = typeface;
    TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
  }

  /**
   * Returns the text displayed by the button.
   *
   * @return  button caption
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Text() {
    return TextViewUtil.getText(view);
  }

  /**
   * Specifies the text displayed by the button.
   *
   * @param text  new caption for button
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void Text(String text) {
    TextViewUtil.setText(view, text);
  }

  /**
   * Returns the button's text color as an alpha-red-green-blue
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
   * Specifies the button's text color as an alpha-red-green-blue
   * integer.
   *
   * @param argb  text RGB color with alpha
   */
  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_COLOR,
      defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
  @SimpleProperty
  public void TextColor(int argb) {
    // TODO(user): I think there is a way of only setting the color for the enabled state
    textColor = argb;
    if (argb != Component.COLOR_DEFAULT) {
      TextViewUtil.setTextColor(view, argb);
    } else {
      TextViewUtil.setTextColors(view, defaultColorStateList);
    }
  }

  public abstract void click();

  // Override this if your component actually will consume a long
  // click.  A 'false' returned from this function will cause a long
  // click to be interpreted as a click (and the click function will
  // be called).
  public boolean longClick() {
    return false;
  }

  // OnClickListener implementation

  @Override
  public void onClick(View view) {
    click();
  }

  // OnFocusChangeListener implementation

  @Override
  public void onFocusChange(View previouslyFocused, boolean gainFocus) {
    if (gainFocus) {
      GotFocus();
    } else {
      LostFocus();
    }
  }

  // OnLongClickListener implementation

  @Override
  public boolean onLongClick(View view) {
    return longClick();
  }

}
