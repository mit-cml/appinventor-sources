// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.content.Context;
import com.bugsense.trace.BugSenseHandler;

/**
 * Helper methods for calling methods added in Eclair (2.0, API level 5)
 *
 * @author Ryan Bis
 *
 */
public class EclairUtil {

  private EclairUtil() {
  }

  /**
   * Calls {@link Activity#overridePendingTransition(int, int)}. This is used
   * to set a different animation type for screen transition animations.
   *
   * @param activity - The activity handling the animation
   * @param enterAnim - The enter animation type
   * @param exitAnim - The exit animation type
   */
  public static void overridePendingTransitions(Activity activity, int enterAnim, int exitAnim) {
    activity.overridePendingTransition(enterAnim, exitAnim);
  }

  public static void setupBugSense(Context context, String api_key) {
    BugSenseHandler.initAndStartSession(context, api_key);
  }

}
