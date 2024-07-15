package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class URL {
  @JsMethod
  public static native String createObjectURL(Object object);
}
