// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.util;

import android.view.Display;

/**
 * Helper methods for calling methods added in Froyo (2.2, API level 8).
 *
 */
public class FroyoUtil {

  private FroyoUtil() {
  }

  /**
   * Calls {@link Display#getRotation()}
   *
   * @return one of {@link android.view.Surface#ROTATION_0},
   *         {@link android.view.Surface#ROTATION_90},
   *         {@link android.view.Surface#ROTATION_180},
   *         or {@link android.view.Surface#ROTATION_180}.
   */
  public static int getRotation(Display display) {
    return display.getRotation();
  }
}
