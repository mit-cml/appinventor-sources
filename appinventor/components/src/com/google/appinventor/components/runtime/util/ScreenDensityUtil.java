// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2015 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper methods for computing scaled sizes for screen compatibility mode
 *
 */
public final class ScreenDensityUtil {

  private static final String LOG_TAG = "ScreenDensityUtil";

  // Much of this compatibility scaling code/constant is taken
  // from the Android source code.
  public static final int DEFAULT_NORMAL_SHORT_DIMENSION = 320;
  public static final float MAXIMUM_ASPECT_RATIO = (854f/480f);


  private ScreenDensityUtil() {
  }

  /**
   * Compute the scaling for applications runs under compatibility mode.
   * This code is partially taken from CompatibilityInfo.java from the Android 5.0 source
   *
   * @param context Context in the screen to get the density of
   * @return Returns the scaling factor for the window.
   */
  public static float computeCompatibleScaling(Context context) {
    DisplayMetrics dm = context.getResources().getDisplayMetrics();

    Point rawDims = new Point();
    getRawScreenDim(context, rawDims);

    int width = rawDims.x;
    int height = rawDims.y;

    int shortSize, longSize;
    if (width < height) {
      shortSize = width;
      longSize = height;
    } else {
      shortSize = height;
      longSize = width;
    }
    int newShortSize = (int)(DEFAULT_NORMAL_SHORT_DIMENSION * dm.density + 0.5f);
    float aspect = ((float)longSize) / shortSize;
    if (aspect > MAXIMUM_ASPECT_RATIO) {
      aspect = MAXIMUM_ASPECT_RATIO;
    }
    int newLongSize = (int)(newShortSize * aspect + 0.5f);
    int newWidth, newHeight;
    if (width < height) {
      newWidth = newShortSize;
      newHeight = newLongSize;
    } else {
      newWidth = newLongSize;
      newHeight = newShortSize;
    }

    float sw = width/(float)newWidth;
    float sh = height/(float)newHeight;
    float scale = sw < sh ? sw : sh;
    if (scale < 1) {
      scale = 1;
    }

    return scale;
  }

  /**
   * Determine the actual size of the screen in pixels.
   * Inspired by http://stackoverflow.com/a/17512853/135135
   *
   * @param context context to get screen size of.
   * @param outSize Set to the real size of the display.
   */
  public static void getRawScreenDim(Context context, Point outSize) {

    final DisplayMetrics metrics = new DisplayMetrics();
    final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();

    int sdkLevel = SdkLevel.getLevel();
    if (sdkLevel >= SdkLevel.LEVEL_JELLYBEAN_MR1) {
      // On API level 17, a public method was added to get the actual sizes
      JellybeanUtil.getRealSize(display, outSize);
    } else if ( sdkLevel > SdkLevel.LEVEL_GINGERBREAD_MR1){
      // Before API level 17, the realsize method did not exist
      // We use reflection instead to access some hidden methods
      // Does not work for 3.x, will just error
      try {
        Method getRawH = Display.class.getMethod("getRawHeight");
        Method getRawW = Display.class.getMethod("getRawWidth");
        try {
          outSize.x = (Integer) getRawW.invoke(display);
          outSize.y = (Integer) getRawH.invoke(display);
        } catch (IllegalArgumentException e) {
          Log.e(LOG_TAG, "Error reading raw screen size", e);
        } catch (IllegalAccessException e) {
          Log.e(LOG_TAG, "Error reading raw screen size", e);
        } catch (InvocationTargetException e) {
          Log.e(LOG_TAG, "Error reading raw screen size", e);
        }
      } catch (NoSuchMethodException e) {
        Log.e(LOG_TAG, "Error reading raw screen size", e);
      }
    } else {
      // The raw height and width functions were added after verison 10
      // Before that, the methods actually returned the raw values
      outSize.x = display.getWidth();
      outSize.y = display.getHeight();
    }

  }
}
