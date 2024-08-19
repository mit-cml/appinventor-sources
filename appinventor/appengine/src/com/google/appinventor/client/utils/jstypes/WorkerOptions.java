package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class WorkerOptions {
  private String type;

  @JsOverlay
  public static WorkerOptions create(String type) {
    final WorkerOptions opts = new WorkerOptions();
    opts.type = type;
    return opts;
  }
}
