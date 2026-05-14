package com.google.appinventor.client.editor.yjs;

import jsinterop.annotations.JsType;
import jsinterop.annotations.JsProperty;

@JsType(namespace = "Y", isNative = true, name = "Array")
public class YArray {
  public YArray() {}

  public native void insert(int index, Object[] content);

  @JsProperty(name = "length")
  public native int length();

  public native void push(Object[] content);
  public native Object get(int index);
  public native void delete(int index, int length); 
}