package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class BlobOptions {
  private String type;
  private String ending;

  @JsOverlay
  public static BlobOptions create(String type, String ending) {
    final BlobOptions opts = new BlobOptions();
    opts.type = type;
    opts.ending = ending;
    return opts;
  }
}
