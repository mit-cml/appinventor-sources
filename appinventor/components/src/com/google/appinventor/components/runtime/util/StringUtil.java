package com.google.appinventor.components.runtime.util;

public class StringUtil {
  public static String formatString(String format, Object[] messageArgs) {
    return String.format(format, messageArgs);
  }
}
