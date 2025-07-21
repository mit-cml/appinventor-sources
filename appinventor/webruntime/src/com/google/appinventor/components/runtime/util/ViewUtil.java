package com.google.appinventor.components.runtime.util;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class ViewUtil {
  public static void setBackgroundImage(View view, Drawable drawable) {
    view.setBackgroundDrawable(drawable);
    view.requestLayout();
  }

  public static void setChildHeightForVerticalLayout(View child, int height) {
    // TODO(ewpatton): Real implementation
  }

  public static void setChildWidthForVerticalLayout(View child, int width) {
    // TODO(ewpatton): Real implementation
  }

  /**
   * Sets the image for an ImageView.
   */
  public static void setImage(ImageView view, Drawable drawable) {
    view.setImageDrawable(drawable);
    if (drawable != null) {
      view.setAdjustViewBounds(true);
    }
    view.requestLayout();
  }

  public static void setBackgroundDrawable(View view, Drawable drawable) {
    view.setBackgroundDrawable(drawable);
    view.requestLayout();
  }
}
