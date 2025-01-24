package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.TextView;

public class TextViewUtil {
  public static void setMinSizes(TextView textView, int minWidth, int minHeight) {
    // TODO(ewpatton): Real implementation
  }

  public static float getFontSize(TextView textView, Context context) {
    // TODO(ewpatton): Real implementation
    return 14.0f;
  }

  public static void setFontSize(TextView textView, float fontSize) {
    // TODO(ewpatton): Real implementation
  }

  public static void setText(TextView textView, String text) {
    textView.setText(text);
  }

  public static String getText(TextView textView) {
    return textView.getText();
  }

  public static void setFontTypeface(Context context, TextView textView, String fontTypeface, boolean bold, boolean italic) {
    // TODO(ewpatton): Real implementation
  }

  public static void setTextColor(TextView textView, int color) {
    // TODO(ewpatton): Real implementation
  }

  public static void setTextColors(TextView textView, ColorStateList colors) {
    // TODO(ewpatton): Real implementation
  }

  public static void setEnabled(TextView textView, boolean enabled) {
    textView.setEnabled(enabled);
  }

  public static boolean isEnabled(TextView textView) {
    return textView.isEnabled();
  }

  public static void setAlignment(TextView textView, int alignment, boolean b) {
    // TODO(ewpatton): Real implementation
  }

  public static void setMinSize(TextView textView, int width, int height) {
    // TODO(ewpatton): Real implementation
  }

  public static void setBackgroundColor(TextView textView, int color) {
    // TODO(ewpatton): Real implementation
  }

  public static void setTextHTML(TextView textView, String text) {
    // TODO(ewpatton): Real implementation
  }
}
