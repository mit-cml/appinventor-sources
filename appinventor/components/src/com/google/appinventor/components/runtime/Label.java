// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.TextViewUtil;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Labels are components used to show text.
 *
 * ![Example of a label](images/label.png)
 *
 * A label displays text which is specified by the `Text` property. Other properties, all of which
 * can be set in the Designer or Blocks Editor, control the appearance and placement of the text.
 */
@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
    description = "A Label displays a piece of text, which is " +
    "specified through the <code>Text</code> property.  Other properties, " +
    "all of which can be set in the Designer or Blocks Editor, control " +
    "the appearance and placement of the text.",
    category = ComponentCategory.USERINTERFACE,
    iconName = "images/label.png")
@SimpleObject
public final class Label extends AndroidViewComponent implements AccessibleComponent{

  // default margin around a label in DPs
  // note that the spacing between adjacent labels will be twice this value
  // because each label has a margin
  private static final int DEFAULT_LABEL_MARGIN = 2;

  // default margin in density-independent pixels. This must be
  // computed using the view
  private int defaultLabelMarginInDp = 0;

  private final TextView view;

  private final LinearLayout.LayoutParams linearLayoutParams;

  // Backing for text alignment
  private int textAlignment;

  // Backing for background color
  private int backgroundColor;

  // Backing for font typeface
  private String fontTypeface;

  // Backing for font bold
  private boolean bold;

  // Backing for font italic
  private boolean italic;

  // Whether or not the label should have a margin
  private boolean hasMargins;

  // Backing for text color
  private int textColor;

  // Label Format
  private boolean htmlFormat;

  // HTML content of the label
  private String htmlContent;

  //Whether or not the text should be big
  private boolean isBigText = false;

  /**
   * Creates a new Label component.
   *
   * @param container  container, component will be placed in
   */
  public Label(ComponentContainer container) {
    super(container);
    view = new TextView(container.$context());

    // Adds the component to its designated container
    container.$add(this);

    // Get the layout parameters to use in setting margins (and potentially
    // other things.
    // There will be a bug if the label view does not have linear layout params.
    // TODO(hal): Generalize this for other types of layouts
    Object lp = view.getLayoutParams();
    // The following instanceof check will fail if we have not previously
    // added the label to the container (Why?)
    if (lp instanceof LinearLayout.LayoutParams) {
        linearLayoutParams = (LinearLayout.LayoutParams) lp;
        defaultLabelMarginInDp = dpToPx(view, DEFAULT_LABEL_MARGIN);
    } else {
      defaultLabelMarginInDp = 0;
      linearLayoutParams = null;
      Log.e("Label", "Error: The label's view does not have linear layout parameters");
      new RuntimeException().printStackTrace();
    }

    // Default property values
    TextAlignment(Component.ALIGNMENT_NORMAL);
    BackgroundColor(Component.COLOR_NONE);
    fontTypeface = Component.TYPEFACE_DEFAULT;
    TextViewUtil.setFontTypeface(container.$form(), view, fontTypeface, bold, italic);
    FontSize(Component.FONT_DEFAULT_SIZE);
    Text("");
    TextColor(Component.COLOR_DEFAULT);
    HTMLFormat(false);
    HasMargins(true);
  }

  // put this in the right file
  private static int dpToPx(View view, int dp) {
    float density = view.getContext().getResources().getDisplayMetrics().density;
    return Math.round((float)dp * density);
  }

  @Override
  public View getView() {
    return view;
  }

  /**
   * Returns the alignment of the label's text: center, normal
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
   * Specifies the alignment of the label's text: center, normal
   * (e.g., left-justified if text is written left to right), or
   * opposite (e.g., right-justified if text is written left to right).
   *
   * @param alignment  one of {@link Component#ALIGNMENT_NORMAL},
   *                   {@link Component#ALIGNMENT_CENTER} or
   *                   {@link Component#ALIGNMENT_OPPOSITE}
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT,
      defaultValue = Component.ALIGNMENT_NORMAL + "")
  @SimpleProperty(
      userVisible = false)
  public void TextAlignment(int alignment) {
    this.textAlignment = alignment;
    TextViewUtil.setAlignment(view, alignment, false);
  }

  /**
   * Returns the label's background color as an alpha-red-green-blue
   * integer.
   *
   * @return  background RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int BackgroundColor() {
    return backgroundColor;
  }

  /**
   * Specifies the label's background color as an alpha-red-green-blue
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
   * Returns true if the label's text should be bold.
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
   * Specifies whether the label's text should be bold.
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
    TextViewUtil.setFontTypeface(container.$form(), view, fontTypeface, bold, italic);
  }

  /**
   * Returns true if the label's text should be italic.
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
   * Specifies whether the label's text should be italic.
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
    TextViewUtil.setFontTypeface(container.$form(), view, fontTypeface, bold, italic);
  }

  /**
   * Returns true if the label should have  margins.
   *
   * @return  {@code true} indicates margins, {@code false} no margins
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Reports whether or not the label appears with margins.  All four "
      + "margins (left, right, top, bottom) are the same.  This property has no effect "
      + "in the designer, where labels are always shown with margins.",
      userVisible = true)
  public boolean HasMargins() {
    return hasMargins;
  }

  /**
   * Specifies whether the label should have margins.
   * This margin value is not well coordinated with the
   * designer, where the margins are defined for the arrangement, not just for individual
   * labels.
   *
   * @param hasMargins {@code true} indicates that there are margins, {@code false} no margins
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(
      userVisible = true)
  public void HasMargins(boolean hasMargins) {
    this.hasMargins = hasMargins;
    setLabelMargins(hasMargins);
  }

private void setLabelMargins(boolean hasMargins) {
  int m = hasMargins ? defaultLabelMarginInDp : 0 ;
  linearLayoutParams.setMargins(m, m, m, m);
  view.invalidate();
}

  /**
   * Returns the label's text's font size, measured in sp(scale-independent pixels).
   *
   * @return  font size in sp(scale-independent pixels).
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public float FontSize() {
    return TextViewUtil.getFontSize(view, container.$context());
  }

  /**
   * Specifies the label's text's font size, measured in sp(scale-independent pixels).
   *
   * @param size  font size in sp (scale-independent pixels)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
      defaultValue = Component.FONT_DEFAULT_SIZE + "")
  @SimpleProperty
  public void FontSize(float size) {

    if (size == FONT_DEFAULT_SIZE && (isBigText || container.$form().BigDefaultText())) {
      TextViewUtil.setFontSize(view, 24);
    } else {
      TextViewUtil.setFontSize(view, size);
    }
  }

  /**
   * Returns the label's text's font face as default, serif, sans
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
  public String FontTypeface() {
    return fontTypeface;
  }

  /**
   * Specifies the label's text's font face as default, serif, sans
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
  public void FontTypeface(String typeface) {
    fontTypeface = typeface;
    TextViewUtil.setFontTypeface(container.$form(), view, fontTypeface, bold, italic);
  }

  /**
   * Returns the text displayed by the label.
   *
   * @return  label caption
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public String Text() {
    return TextViewUtil.getText(view);
  }

  /**
   * Specifies the text displayed by the label.
   *
   * @param text  new caption for label
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA,
      defaultValue = "")
  @SimpleProperty
  public void Text(String text) {
    if (htmlFormat) {
      TextViewUtil.setTextHTML(view, text);
    } else {
      TextViewUtil.setText(view, text);
    }
    htmlContent = text;
  }

  /**
   * Returns the content of the Label as HTML. This is only useful if the
   * HTMLFormat property is true.
   *
   * @return the HTML content of the label
   */
  @SimpleProperty
  public String HTMLContent() {
    if (htmlFormat) {
      return htmlContent;
    } else {
      return TextViewUtil.getText(view);
    }
  }


  /**
   * Returns the label's text's format
   *
   * @return {@code true} indicates that the label format is html text
   *         {@code false} lines that the label format is plain text
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "If true, then this label will show html text else it " +
      "will show plain text. Note: Not all HTML is supported.")
  public boolean HTMLFormat() {
    return htmlFormat;
  }

  /**
   * Specifies the label's text's format
   *
   * @return {@code true} indicates that the label format is html text
   *         {@code false} lines that the label format is plain text
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(userVisible = false)
  public void HTMLFormat(boolean fmt) {
    htmlFormat = fmt;
    if (htmlFormat) {
      String txt = TextViewUtil.getText(view);
      TextViewUtil.setTextHTML(view, txt);
    } else {
      String txt = TextViewUtil.getText(view);
      TextViewUtil.setText(view, txt);
    }
  }

  /**
   * Returns the label's text color as an alpha-red-green-blue
   * integer.
   *
   * @return  text RGB color with alpha
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  @IsColor
  public int TextColor() {
    return textColor;
  }

  /**
   * Specifies the label's text color as an alpha-red-green-blue
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
}
