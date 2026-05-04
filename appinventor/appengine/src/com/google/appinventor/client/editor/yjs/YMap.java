package com.google.appinventor.client.editor.yjs;

import jsinterop.annotations.JsType;

@JsType(namespace = "Y", isNative = true, name = "Map")
public class YMap {
  public YMap() {}

  public native void set(String key, Object value);
  public native Object get(String key);
  public native void delete(String key);

}