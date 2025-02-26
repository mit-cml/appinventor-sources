package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class CSSStyleSheet {
  public native void replaceSync(String text);
}
