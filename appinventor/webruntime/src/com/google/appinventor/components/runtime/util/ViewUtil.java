package com.google.appinventor.components.runtime.util;

import android.graphics.drawable.Drawable;
import android.view.View;

public class ViewUtil {

  private static View childContainer;
  
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

  public static void setBackgroundDrawable(View view, Drawable drawable) {
    view.setBackgroundDrawable(drawable);
    view.requestLayout();
  }

  public static void setChildContainer(View container) {
    childContainer = container;
  }

  public static View getChildContainer() {
    return childContainer;
  }

  public static void addChild(View child) {
    if (childContainer != null && child != null) {
      childContainer.getElement().appendChild(child.getElement());
    }
  }
}
