package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Blob {
  @JsConstructor
  public Blob(Object[] parts, BlobOptions options) {}
}
