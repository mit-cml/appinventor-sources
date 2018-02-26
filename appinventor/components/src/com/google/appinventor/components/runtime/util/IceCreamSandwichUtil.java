// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.widget.TextView;

/**
 * IceCreamSandwichUtil provides implementation of functionality that was added in Android Ice Cream
 * Sandwich 4.0 (API 14).
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public final class IceCreamSandwichUtil {

  private IceCreamSandwichUtil() {
  }

  /**
   * Sets the font face of {@code view} to all caps (true) or regular (false).
   * @param view the text view to update
   * @param allCaps true for all caps, false otherwise
   */
  public static void setAllCaps(TextView view, boolean allCaps) {
    if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
      view.setAllCaps(allCaps);
    }
  }
}
