// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Helper methods for calling methods added in Donut (1.6, API level 4).
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class DonutUtil {
  private DonutUtil() {
  }

  /**
   * Calls {@link View#buildDrawingCache(boolean)}.  If you call this
   * manually without calling {@link View#setDrawingCacheEnabled(boolean)
   * setDrawingCacheEnabled(true)}, you should clean up by calling
   * {@link View#destroyDrawingCache()} afterward.
   *
   * @param view what to build a drawing cache of
   */
  public static void buildDrawingCache(View view, boolean autoScale) {
    view.buildDrawingCache(autoScale);
  }

  /**
   * Calls {@link View#getDrawingCache(boolean)}.
   *
   * @return A bitmap representing this view, or null if caching is disabled
   *         or {@link View#buildDrawingCache(boolean)} has not been called
   */
  public static Bitmap getDrawingCache(View view, boolean autoScale) {
    return view.getDrawingCache(autoScale);
  }
}
