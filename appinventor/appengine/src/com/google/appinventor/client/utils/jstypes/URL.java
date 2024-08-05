package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class URL {
  public static native String createObjectURL(Object object);

  public static native String revokeObjectURL(String url);

  public static native URL parse(String url);
}
