package com.google.appinventor.client.utils;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "URL")
public class JsURL {
  @JsMethod
  public static native String createObjectURL(Object object);
}
