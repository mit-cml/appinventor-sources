package com.google.appinventor.components.runtime.util;

import android.view.View;

public class HoneycombUtil {
  public static void viewSetRotate(View view, double degrees) {
    view.setRotation((float) degrees);
  }
}
