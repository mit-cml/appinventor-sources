package com.google.appinventor.client.json;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL, isNative = true)
public class JSON {
  public static native String stringify(Object object);

  public static native Object parse(String json);
}
