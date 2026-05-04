package com.google.appinventor.client.editor.yjs;

import jsinterop.annotations.JsType;

@JsType(namespace = "Y", isNative = true, name = "Array")
public class YArray {
  public YArray() {}

  public native void insert(int index, Object[] content);
  public native int length();
  public native void push(Object[] content);

}