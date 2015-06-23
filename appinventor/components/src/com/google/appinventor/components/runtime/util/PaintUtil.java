// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;

/**
 * Functionality related to {@link android.graphics.Color} and
 * {@link android.graphics.Paint}.
 *
 */
public class PaintUtil {
  private PaintUtil() {}

  /**
   * Changes the paint color to the specified value.
   *
   * @param paint the object to mutate with the new color
   * @param argb a 32-bit integer with eight bits for alpha, red, green, and blue,
   *        respectively
   */
  public static void changePaint(Paint paint, int argb) {
    // TODO(user): can the following two lines can be replaced by:
    // paint.setColor(argb)?
    paint.setColor(argb & 0x00FFFFFF);
    paint.setAlpha((argb >> 24) & 0xFF);
    paint.setXfermode(null);
  }

  /**
   * Changes the paint color to transparent
   *
   * @param paint the object to mutate with the new color
   */
  public static void changePaintTransparent(Paint paint) {
    paint.setAlpha(0x00);
    paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
  }
}
