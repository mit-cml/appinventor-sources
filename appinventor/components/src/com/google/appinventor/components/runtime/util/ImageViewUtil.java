// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.support.v4.widget.ImageViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.Queue;

import static android.graphics.PorterDuff.Mode.MULTIPLY;

public final class ImageViewUtil {

  /**
   * We will determine when this class is first referenced whether we can update the color of
   * the menu button. If true, we will attempt to update. Otherwise, the button will get out of
   * sync, but this will only happen in the companion so it shouldn't be too much of a problem.
   */
  private ImageViewUtil() {}

  public static void setMenuButtonColor(Activity activity, int color) {
    ColorStateList stateList = new ColorStateList(new int[][]{new int[]{}}, new int[]{color});
    ImageView view = findOverflowMenuView(activity);
    if (view != null) {
      ImageViewCompat.setImageTintMode(view, MULTIPLY);
      ImageViewCompat.setImageTintList(view, stateList);
    }
  }

  private static ImageView findOverflowMenuView(Activity activity) {
    ViewGroup vg = (ViewGroup) activity.getWindow().getDecorView();
    Queue<ViewGroup> children = new LinkedList<ViewGroup>();
    children.add(vg);
    while (children.size() > 0) {
      vg = children.poll();
      for (int i = 0; i < vg.getChildCount(); i++) {
        View child = vg.getChildAt(i);
        if (child instanceof ImageView) {
          return (ImageView) child;
        } else if (child instanceof ViewGroup) {
          children.add((ViewGroup) child);
        }
      }
    }
    return null;
  }
}
