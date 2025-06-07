package com.google.appinventor.components.runtime.util;

public class FormUtil {
  public static native void callInitialize(Object o) /*-{
    if (o && o.Initialize) {
      o.Initialize();
    } else {
      console.warn("No Initialize method found for object: " + o);
    }
  }-*/;

  public static String getPackageName(Object o) {
    // TODO(ewpatton): Real implementation
    return "com.google.appinventor.components.runtime";
  }
}
