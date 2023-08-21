// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Build;
import com.google.appinventor.components.runtime.Component;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;
import android.text.Html;
import android.content.Context;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;

import java.io.File;
import java.io.IOException;

/**
 * Helper methods for manipulating {@link TextView} objects.
 *
 */
public class TextViewUtil {

  private TextViewUtil() {
  }

  /**
   * TextView alignment setter.
   *
   * @param alignment  one of {@link Component#ALIGNMENT_NORMAL},
   *                   {@link Component#ALIGNMENT_CENTER} or
   *                   {@link Component#ALIGNMENT_OPPOSITE}
   * @param centerVertically whether the text should be centered vertically
   */
  public static void setAlignment(TextView textview, int alignment, boolean centerVertically) {
    int horizontalGravity;
    switch (alignment) {
      default:
        throw new IllegalArgumentException();

      case Component.ALIGNMENT_NORMAL:
        horizontalGravity = Gravity.LEFT;
        break;

      case Component.ALIGNMENT_CENTER:
        horizontalGravity = Gravity.CENTER_HORIZONTAL;
        break;

      case Component.ALIGNMENT_OPPOSITE:
        horizontalGravity = Gravity.RIGHT;
        break;
    }
    int verticalGravity = centerVertically ? Gravity.CENTER_VERTICAL : Gravity.TOP;
    textview.setGravity(horizontalGravity | verticalGravity);
    textview.invalidate();
  }

  /**
   * {@link TextView} background color setter.  Generally, the caller will
   * not pass {@link Component#COLOR_DEFAULT}, instead substituting in the
   * appropriate color.
   *
   * @param textview   text view instance
   * @param argb  background RGB color with alpha
   */
  public static void setBackgroundColor(TextView textview, int argb) {
    textview.setBackgroundColor(argb);
    textview.invalidate();
  }

  /**
   * Returns the enabled state a {@link TextView}.
   *
   * @param textview   text view instance
   * @return  {@code true} for enabled, {@code false} disabled
   */
  public static boolean isEnabled(TextView textview) {
    return textview.isEnabled();
  }

  /**
   * Enables a {@link TextView}.
   *
   * @param textview   text view instance
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  public static void setEnabled(TextView textview, boolean enabled) {
    textview.setEnabled(enabled);
    textview.invalidate();
  }

  /**
   * Returns the font size for a {@link TextView}.
   *
   * @param textview   text view instance
   * @param context   Context in the screen to get the density of
   * @return  font size in pixel
   */
  public static float getFontSize(TextView textview, Context context) {
    float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
    return textview.getTextSize()/scaledDensity;
  }

  /**
   * Sets the font size for a {@link TextView}.
   *
   * @param textview   text view instance
   * @param size  font size in pixel
   */
  public static void setFontSize(TextView textview, float size) {
    textview.setTextSize(size);
    textview.requestLayout();
  }

  /**
   * Sets the font typeface for a {@link TextView}.
   *
   * @param textview   text view instance
   * @param typeface  one of @link Component#TYPEFACE_DEFAULT},
   *                  {@link Component#TYPEFACE_SERIF},
   *                  {@link Component#TYPEFACE_SANSSERIF} or
   *                  {@link Component#TYPEFACE_MONOSPACE}
   * @param bold true for bold, false for not bold
   * @param italic true for italic, false for not italic
   */
  public static void setFontTypeface(Form form, TextView textview, String typeface,
      boolean bold, boolean italic) {
    Typeface tf;
    if (typeface.equals(Component.TYPEFACE_DEFAULT)) {
      tf = Typeface.DEFAULT;
    } else if (typeface.equals(Component.TYPEFACE_SANSSERIF)) {
      tf = Typeface.SANS_SERIF;
    } else if (typeface.equals(Component.TYPEFACE_SERIF)) {
      tf = Typeface.SERIF;
    } else if (typeface.equals(Component.TYPEFACE_MONOSPACE)) {
      tf = Typeface.MONOSPACE;
    } else {
      tf = getTypeFace(form, typeface);
    }

    int style = 0;
    if (bold) {
      style |= Typeface.BOLD;
    }
    if (italic) {
      style |= Typeface.ITALIC;
    }
    textview.setTypeface(Typeface.create(tf, style));
    textview.requestLayout();
  }

  /**
   * Gets typeface.
   *
   * @param form     the form
   * @param fontFile the font file
   * @return the typeface
   */
  public static Typeface getTypeFace(Form form, String fontFile) {
    if (fontFile == null || fontFile.isEmpty()) {
      return null;
    }
    Typeface typeface = null;
    File file;
    if (!fontFile.contains("/")) {
      if (form instanceof ReplForm) {
        try {
          file = new File(MediaUtil.fileUrlToFilePath(form.getAssetPath(fontFile)));
          typeface = Typeface.createFromFile(file);
        } catch (IOException ioe) {
        }                
      } else {
        typeface = Typeface.createFromAsset(form.$context().getAssets(), fontFile);
      }
    } else {
      file = new File(fontFile);
      typeface = Typeface.createFromFile(file);
    }
    return typeface;
  }

  /**
   * Returns the text for a {@link TextView}.
   *
   * @param textview   text view instance
   * @return  text shown in text view
   */
  public static String getText(TextView textview) {
    return textview.getText().toString();
  }

  /**
   * Sets the text for a {@link TextView}.
   *
   * @param textview   text view instance
   * @param text  new text to be shown
   */
  public static void setTextHTML(TextView textview, String text) {
    textview.setText(Html.fromHtml(text));
    textview.requestLayout();
  }

  /**
   * Sets the text for a {@link TextView}.
   *
   * @param textview   text view instance
   * @param text  new text to be shown
   */
  public static void setText(TextView textview, String text) {
    textview.setText(text);
    textview.requestLayout();
  }

  /**
   * Sets the padding for a {@link TextView}.
   *
   * @param textview   text view instance
   * @param padding  left and right padding to be set
   */
  public static void setPadding(TextView textview, int padding) {
    textview.setPadding(padding, padding, 0, 0);
    textview.requestLayout();
  }

  /**
   * Sets the text color for a {@link TextView}.
   *
   * @param textview   text view instance
   * @param argb  text RGB color with alpha
   */
  public static void setTextColor(TextView textview, int argb) {
    textview.setTextColor(argb);
    textview.invalidate();
  }

  public static void setTextColors(TextView textview, ColorStateList colorStateList) {
    textview.setTextColor(colorStateList);
  }

  /**
   * Sets the minimum width of a text view.
   *
   * @param textview text view instance
   * @param minWidth minimum width of the text view in pixels
   */
  public static void setMinWidth(TextView textview, int minWidth) {
    // According to https://developer.android.com/reference/android/widget/TextView.html#setMinWidth(int), the minimum
    // width of TextView is the maximum of setMinWidth and setMinimumWidth. Talk about NIH syndrome!
    textview.setMinWidth(minWidth);
    textview.setMinimumWidth(minWidth);
  }

  /**
   * Sets the minimum height of a text view.
   *
   * @param textview text view instance
   * @param minHeight minimum height of the text view in pixels
   */
  public static void setMinHeight(TextView textview, int minHeight) {
    // According to https://developer.android.com/reference/android/widget/TextView.html#setMinHeight(int), the minimum
    // height of TextView is the maximum of setMinHeight and setMinimumHeight. Talk about NIH syndrome!
    textview.setMinHeight(minHeight);
    textview.setMinimumHeight(minHeight);
  }

  /**
   * Sets the minimum size for a text view.
   *
   * @param textview text view instance
   * @param minWidth minimum width of the text view in pixels
   * @param minHeight minimum height of the text view in pixels
   */
  public static void setMinSize(TextView textview, int minWidth, int minHeight) {
    TextViewUtil.setMinWidth(textview, minWidth);
    TextViewUtil.setMinHeight(textview, minHeight);
  }
}
