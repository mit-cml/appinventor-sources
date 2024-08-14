package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public interface WorkerMessage {
  @JsProperty(name = "type")
  String getType();

  @JsProperty(name = "data")
  Object getData();
}
