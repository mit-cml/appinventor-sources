package com.google.appinventor.components.runtime.util;

public final class PaintUtil {
  private PaintUtil() {
  }

  public static int hexStringToInt(String argb) {
    String unprefixed = argb;
    if (argb.startsWith("#x") || argb.startsWith("&H")) {
      unprefixed = argb.substring(2);
    }
    // Integer.parseInt will throw
    long l = Long.parseLong(unprefixed, 16);
    if (l > Integer.MAX_VALUE) {
      l += 2 * Integer.MIN_VALUE;
    }
    return (int) l;
  }
}
