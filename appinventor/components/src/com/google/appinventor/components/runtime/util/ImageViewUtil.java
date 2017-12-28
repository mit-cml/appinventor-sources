// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.TintImageView;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.Queue;

public final class ImageViewUtil {

  /**
   * We will determine when this class is first referenced whether we can update the color of
   * the menu button. If true, we will attempt to update. Otherwise, the button will get out of
   * sync, but this will only happen in the companion so it shouldn't be too much of a problem.
   */
  private static final boolean canUpdate;

  static {
    boolean updateable = false;
    try {
      TintImageView.class.getMethod("setImageTintMode", PorterDuff.Mode.class);
      updateable = true;
    } catch(NoSuchMethodException e) {
    }
    canUpdate = updateable;
  }

  private ImageViewUtil() {}

  public static void setMenuButtonColor(AppCompatActivity activity, int color) {
    if (canUpdate) {
      TintImageView overflowMenuView = findOverflowMenuView(activity);
      if (overflowMenuView != null) {
        ColorStateList stateList = new ColorStateList(new int[][]{new int[]{}}, new int[]{color});
        try {
          overflowMenuView.setImageTintMode(PorterDuff.Mode.MULTIPLY);
          overflowMenuView.setImageTintList(stateList);
        } catch (NoSuchMethodError e) {
          // extra insurance in case the canUpdate flag is lying...
        }
      }
    }
  }

  private static TintImageView findOverflowMenuView(AppCompatActivity activity) {
    TintImageView overflowMenuView = null;
    ViewGroup vg = (ViewGroup) activity.getWindow().getDecorView();
    Queue<ViewGroup> children = new LinkedList<ViewGroup>();
    children.add(vg);
    while (children.size() > 0) {
      vg = children.poll();
      for (int i = 0; i < vg.getChildCount(); i++) {
        View child = vg.getChildAt(i);
        if (child instanceof TintImageView) {
          overflowMenuView = (TintImageView) child;
          return overflowMenuView;
        } else if (child instanceof ViewGroup) {
          children.add((ViewGroup) child);
        }
      }
    }
    return overflowMenuView;
  }
}
