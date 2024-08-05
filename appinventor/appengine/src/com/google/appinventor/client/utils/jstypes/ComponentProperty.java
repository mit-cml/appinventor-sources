package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL)
public class ComponentProperty {
  public final String name;
  public final Object value;

  public ComponentProperty(String name, Object value) {
    this.name = name;
    this.value = value;
  }
}
