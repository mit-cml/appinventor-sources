package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL)
public class PropertyChangeMessage {
  @JsProperty(name = "propertyName")
  public final String propertyName;

  @JsProperty(name = "newValue")
  public final String newValue;

  public PropertyChangeMessage(String propertyName, String newValue) {
    this.propertyName = propertyName;
    this.newValue = newValue;
  }
}
