package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Blob {

  @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
  public static class BlobOptions {
    @JsProperty private String type;

    @JsProperty private String ending;

    @JsConstructor
    public BlobOptions(String type, String ending) {}
  }

  @JsConstructor
  public Blob(Object[] parts, BlobOptions options) {}
}
