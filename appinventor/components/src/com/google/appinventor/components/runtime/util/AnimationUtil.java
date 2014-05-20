// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

/**
 * This class supplies some support for pre-defined component animation.
 *
 * <p>For more information about animation on the Android platform, see
 * the <a href="http://code.google.com/android/documentation.html">
 * Android documentation</a> on code.google.com.
 *
 */
public final class AnimationUtil {

  private AnimationUtil() {
  }

  /*
   * Animates a component moving it horizontally.
   */
  private static void ApplyHorizontalScrollAnimation(View view, boolean left, int speed) {
    float sign = left ? 1f : -1f;
    AnimationSet animationSet = new AnimationSet(true);
    animationSet.setRepeatCount(Animation.INFINITE);
    animationSet.setRepeatMode(Animation.RESTART);

    TranslateAnimation move = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, sign * 0.70f,
        Animation.RELATIVE_TO_PARENT, sign * -0.70f, Animation.RELATIVE_TO_PARENT, 0,
        Animation.RELATIVE_TO_PARENT, 0);
    move.setStartOffset(0);
    move.setDuration(speed);
    move.setFillAfter(true);
    animationSet.addAnimation(move);
    view.startAnimation(animationSet);
  }

  /**
   * Animates a component (using pre-defined animation kinds).
   *
   * @param view  component to animate
   * @param animation  animation kind
   */
  public static void ApplyAnimation(View view, String animation) {
    // TODO(user): These string constants need to be extracted and defined somewhere else!
    // TODO(user): Also, the endless else-if is inefficient
    if (animation.equals("ScrollRightSlow")) {
      ApplyHorizontalScrollAnimation(view, false, 8000);
    } else if (animation.equals("ScrollRight")) {
      ApplyHorizontalScrollAnimation(view, false, 4000);
    } else if (animation.equals("ScrollRightFast")) {
      ApplyHorizontalScrollAnimation(view, false, 1000);
    } else if (animation.equals("ScrollLeftSlow")) {
      ApplyHorizontalScrollAnimation(view, true, 8000);
    } else if (animation.equals("ScrollLeft")) {
      ApplyHorizontalScrollAnimation(view, true, 4000);
    } else if (animation.equals("ScrollLeftFast")) {
      ApplyHorizontalScrollAnimation(view, true, 1000);
    } else if (animation.equals("Stop")) {
      view.clearAnimation();
    }
  }

  /**
   * Applies a specific animation for transitioning to a new
   * Screen.
   *
   * @param activity - the form which is calling another screen
   * @param animType - the animation type
   */
  public static void ApplyOpenScreenAnimation(Activity activity, String animType) {
    if (animType == null) {
      return;
    }
    if (SdkLevel.getLevel() <= SdkLevel.LEVEL_DONUT) {
      Log.e("AnimationUtil", "Screen animations are not available on android versions less than 2.0.");
      return;
    }
    int enter = 0;
    int exit = 0;

    if (animType.equalsIgnoreCase("fade")) {
      enter = activity.getResources().getIdentifier("fadein", "anim", activity.getPackageName());
      exit = activity.getResources().getIdentifier("hold", "anim", activity.getPackageName());
    } else if (animType.equalsIgnoreCase("zoom")) {
      exit = activity.getResources().getIdentifier("zoom_exit", "anim", activity.getPackageName());
      enter = activity.getResources().getIdentifier("zoom_enter", "anim", activity.getPackageName());
    } else if (animType.equalsIgnoreCase("slidehorizontal")) {
      exit = activity.getResources().getIdentifier("slide_exit", "anim", activity.getPackageName());
      enter = activity.getResources().getIdentifier("slide_enter", "anim", activity.getPackageName());
    } else if (animType.equalsIgnoreCase("slidevertical")) {
      exit = activity.getResources().getIdentifier("slide_v_exit", "anim", activity.getPackageName());
      enter = activity.getResources().getIdentifier("slide_v_enter", "anim", activity.getPackageName());
    } else if (animType.equalsIgnoreCase("none")) {
      // enter and exit are already set to 0, so
      // no animations will be played.
    } else {
      // Return here so overridePendingTransitions isn't run, and android
      // does it's default thing.
      return;
    }
    EclairUtil.overridePendingTransitions(activity, enter, exit);
  }

  /**
   * Applies a specific animation for transitioning back a screen.
   *
   * @param activity - the form which is closing
   * @param animType - the animation type
   */
  public static void ApplyCloseScreenAnimation(Activity activity, String animType) {
    if (animType == null) {
      return;
    }
    if (SdkLevel.getLevel() <= SdkLevel.LEVEL_DONUT) {
      Log.e("AnimationUtil", "Screen animations are not available on android versions less than 2.0.");
      return;
    }
    int enter = 0;
    int exit = 0;
    if (animType.equalsIgnoreCase("fade")) {
      exit = activity.getResources().getIdentifier("fadeout", "anim", activity.getPackageName());
      enter = activity.getResources().getIdentifier("hold", "anim", activity.getPackageName());
    } else if (animType.equalsIgnoreCase("zoom")) {
      exit = activity.getResources().getIdentifier("zoom_exit_reverse", "anim", activity.getPackageName());
      enter = activity.getResources().getIdentifier("zoom_enter_reverse", "anim", activity.getPackageName());
    } else if (animType.equalsIgnoreCase("slidehorizontal")) {
      exit = activity.getResources().getIdentifier("slide_exit_reverse", "anim", activity.getPackageName());
      enter = activity.getResources().getIdentifier("slide_enter_reverse", "anim", activity.getPackageName());
    } else if (animType.equalsIgnoreCase("slidevertical")) {
      exit = activity.getResources().getIdentifier("slide_v_exit_reverse", "anim", activity.getPackageName());
      enter = activity.getResources().getIdentifier("slide_v_enter_reverse", "anim", activity.getPackageName());
    } else if (animType.equalsIgnoreCase("none")) {
      // enter and exit are already set to 0, so
      // no animations will be played.
    } else {
      // Return here so overridePendingTransitions isn't run, and android
      // does it's default thing.
      return;
    }
    EclairUtil.overridePendingTransitions(activity, enter, exit);
  }

}
