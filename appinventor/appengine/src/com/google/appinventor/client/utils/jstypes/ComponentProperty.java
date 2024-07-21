package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL)
public class ComponentProperty {
  public final String name;
  public final String value;

  public ComponentProperty(String name, String value) {
    this.name = name;
    this.value = value;
  }
}
