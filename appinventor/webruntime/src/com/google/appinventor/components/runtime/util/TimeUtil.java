package com.google.appinventor.components.runtime.util;

public class TimeUtil {
  private static long lastToastTime = 0L;

  // To control volume of error complaints
  private static final long MIN_TOAST_WAIT_TIME = 10000L; // 10 seconds


  public static boolean toastAllowed() {
    long now = System.currentTimeMillis();
    if (now > lastToastTime + MIN_TOAST_WAIT_TIME) {
      lastToastTime = now;
      return true;
    }
    return false;
  }
}
