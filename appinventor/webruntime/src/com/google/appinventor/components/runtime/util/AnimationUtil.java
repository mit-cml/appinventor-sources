package com.google.appinventor.components.runtime.util;

import android.app.Activity;
import com.google.appinventor.components.common.ScreenAnimation;

public final class AnimationUtil {
  private AnimationUtil() {
  }

  public static void ApplyOpenScreenAnimation(Activity activity, String animType) {
    ApplyOpenScreenAnimation(activity, ScreenAnimation.fromUnderlyingValue(animType));
  }

  public static void ApplyOpenScreenAnimation(Activity activity, ScreenAnimation animType) {
  }

  public static void ApplyCloseScreenAnimation(Activity activity, String animType) {
    ApplyCloseScreenAnimation(activity, ScreenAnimation.fromUnderlyingValue(animType));
  }

  public static void ApplyCloseScreenAnimation(Activity activity, ScreenAnimation animType) {
  }
}
