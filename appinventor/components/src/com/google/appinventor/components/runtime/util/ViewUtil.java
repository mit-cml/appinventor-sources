// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.util;

import android.util.DisplayMetrics;
import com.google.appinventor.components.runtime.Component;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;

/**
 * Helper methods for manipulating {@link View} objects.
 *
 */
public final class ViewUtil {

  private ViewUtil() {
  }

  public static void setChildWidthForHorizontalLayout(View view, int width) {
    // In a horizontal layout, if a child's width is set to fill parent, we must set the
    // LayoutParams width to 0 and the weight to 1. For other widths, we set the weight to 0
    Object layoutParams = view.getLayoutParams();
    if (layoutParams instanceof LinearLayout.LayoutParams) {
      LinearLayout.LayoutParams linearLayoutParams = (LinearLayout.LayoutParams) layoutParams;
      switch (width) {
        case Component.LENGTH_PREFERRED:
          linearLayoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
          linearLayoutParams.weight = 0;
          break;
        case Component.LENGTH_FILL_PARENT:
          linearLayoutParams.width = 0;
          linearLayoutParams.weight = 1;
          break;
        default:
          linearLayoutParams.width = calculatePixels(view, width);
          linearLayoutParams.weight = 0;
          break;
      }
      view.requestLayout();
    } else {
      Log.e("ViewUtil", "The view does not have linear layout parameters");
    }
  }

  public static void setChildHeightForHorizontalLayout(View view, int height) {
    // In a horizontal layout, if a child's height is set to fill parent, we can simply set the
    // LayoutParams height to fill parent.
    Object layoutParams = view.getLayoutParams();
    if (layoutParams instanceof LinearLayout.LayoutParams) {
      LinearLayout.LayoutParams linearLayoutParams = (LinearLayout.LayoutParams) layoutParams;
      switch (height) {
        case Component.LENGTH_PREFERRED:
          linearLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
          break;
        case Component.LENGTH_FILL_PARENT:
          linearLayoutParams.height = LinearLayout.LayoutParams.FILL_PARENT;
          break;
        default:
          linearLayoutParams.height = calculatePixels(view, height);
          break;
      }
      view.requestLayout();
    } else {
      Log.e("ViewUtil", "The view does not have linear layout parameters");
    }
  }

  public static void setChildWidthForVerticalLayout(View view, int width) {
    // In a vertical layout, if a child's width is set to fill parent, we can simply set the
    // LayoutParams width to fill parent.
    Object layoutParams = view.getLayoutParams();
    if (layoutParams instanceof LinearLayout.LayoutParams) {
      LinearLayout.LayoutParams linearLayoutParams = (LinearLayout.LayoutParams) layoutParams;
      switch (width) {
        case Component.LENGTH_PREFERRED:
          linearLayoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
          break;
        case Component.LENGTH_FILL_PARENT:
          linearLayoutParams.width = LinearLayout.LayoutParams.FILL_PARENT;
          break;
        default:
          linearLayoutParams.width = calculatePixels(view, width);
          break;
      }
      view.requestLayout();
    } else {
      Log.e("ViewUtil", "The view does not have linear layout parameters");
    }
  }

  /**
   * Calculate the device dependent pixels to render this view. The size in the designer is given
   * in Density Independent Pixels, and we need to transform that to real pixels depending on the
   * device running the app. Formula taken from
   * http://stackoverflow.com/questions/5012840/android-specifying-pixel-units-like-sp-px-dp-without-using-xml/5012893#5012893
   * @param view the view is needed to grab the Context object
   * @param sizeInDP the size (in DP) specified in the designer
   * @return size in Pixels for the particular device running the app.
   */
  private static int calculatePixels(View view, int sizeInDP) {
    return (int) ((view.getContext().getResources().getDisplayMetrics().density * sizeInDP) + 0.5f);
  }

  public static void setChildHeightForVerticalLayout(View view, int height) {
    // In a vertical layout, if a child's height is set to fill parent, we must set the
    // LayoutParams height to 0 and the weight to 1. For other heights, we set the weight to 0
    Object layoutParams = view.getLayoutParams();
    if (layoutParams instanceof LinearLayout.LayoutParams) {
      LinearLayout.LayoutParams linearLayoutParams = (LinearLayout.LayoutParams) layoutParams;
      switch (height) {
        case Component.LENGTH_PREFERRED:
          linearLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
          linearLayoutParams.weight = 0;
          break;
        case Component.LENGTH_FILL_PARENT:
          linearLayoutParams.height = 0;
          linearLayoutParams.weight = 1;
          break;
        default:
          linearLayoutParams.height = calculatePixels(view, height);
          linearLayoutParams.weight = 0;
          break;
      }
      view.requestLayout();
    } else {
      Log.e("ViewUtil", "The view does not have linear layout parameters");
    }
  }

  public static void setChildWidthForTableLayout(View view, int width) {
    Object layoutParams = view.getLayoutParams();
    if (layoutParams instanceof TableRow.LayoutParams) {
      TableRow.LayoutParams tableLayoutParams = (TableRow.LayoutParams) layoutParams;
      switch (width) {
        case Component.LENGTH_PREFERRED:
          tableLayoutParams.width = TableRow.LayoutParams.WRAP_CONTENT;
          break;
        case Component.LENGTH_FILL_PARENT:
          tableLayoutParams.width = TableRow.LayoutParams.FILL_PARENT;
          break;
        default:
          tableLayoutParams.width = calculatePixels(view, width);
          break;
      }
      view.requestLayout();
    } else {
      Log.e("ViewUtil", "The view does not have table layout parameters");
    }
  }

  public static void setChildHeightForTableLayout(View view, int height) {
    Object layoutParams = view.getLayoutParams();
    if (layoutParams instanceof TableRow.LayoutParams) {
      TableRow.LayoutParams tableLayoutParams = (TableRow.LayoutParams) layoutParams;
      switch (height) {
        case Component.LENGTH_PREFERRED:
          tableLayoutParams.height = TableRow.LayoutParams.WRAP_CONTENT;
          break;
        case Component.LENGTH_FILL_PARENT:
          tableLayoutParams.height = TableRow.LayoutParams.FILL_PARENT;
          break;
        default:
          tableLayoutParams.height = calculatePixels(view, height);
          break;
      }
      view.requestLayout();
    } else {
      Log.e("ViewUtil", "The view does not have table layout parameters");
    }
  }

  /**
   * Sets the background image for a view.
   */
  public static void setBackgroundImage(View view, Drawable drawable) {
    view.setBackgroundDrawable(drawable);
    view.requestLayout();
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
    view.invalidate();
  }
}
