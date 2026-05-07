// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2022 Massachusetts Institute of Technology. All rights reserved.

package com.google.appinventor.client.json;

public class JsonUtil {

  public static native Object getProperty(Object self, String name)/*-{
    return self[name];
  }-*/;

  public static native boolean containsKey(Object self, String name)/*-{
    return self[name] !== undefined;
  }-*/;

  public static native Object setProperty(Object self, String name, Object value)/*-{
    var old = self[name];
    self[name] = value;
    return old;
  }-*/;

  public static native <E> Object removeProperty(JsObject<E> self, Object name)/*-{
    var old = self[name];
    delete self[name];
    return old;
  }-*/;

}
