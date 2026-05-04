package com.google.appinventor.client.editor.yjs;

import jsinterop.annotations.JsType;
import jsinterop.annotations.JsMethod;

@JsType(namespace = "Y", isNative = true)
public class Doc {
  public Doc() {}

  @JsMethod
    public native YMap getMap(String name);

    public native void transact(Runnable f);

}