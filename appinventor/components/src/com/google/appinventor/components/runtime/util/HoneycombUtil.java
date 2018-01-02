// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

/**
 * Helper methods for calling methods added in HONEYCOMB (3.0, API level 11)
 *
 *
 */
public class HoneycombUtil {

  public static final int VIEWGROUP_MEASURED_HEIGHT_STATE_SHIFT = ViewGroup.MEASURED_HEIGHT_STATE_SHIFT;

  private HoneycombUtil() {
  }

  public static int combineMeasuredStates(ViewGroup view, int curState, int newState) {
    return view.combineMeasuredStates(curState, newState);
  }

  public static int getMeasuredState(View view) {
    return view.getMeasuredState();
  }

  public static int resolveSizeAndState(ViewGroup view, int maxWidth, int widthMeasureSpec, int childState) {
    return view.resolveSizeAndState(maxWidth, widthMeasureSpec, childState);
  }

  public static void viewSetRotate(View view, double rotationAngle) {
    view.setRotation((float) rotationAngle);
  }

  public static Spinner makeSpinner(Context activity) {
    return new Spinner(activity, Spinner.MODE_DIALOG);
  }
}
