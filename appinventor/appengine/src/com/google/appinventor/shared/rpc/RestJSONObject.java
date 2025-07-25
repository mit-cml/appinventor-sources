package com.google.appinventor.shared.rpc;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

public abstract class RestJSONObject {
  public RestJSONObject() {}

  public RestJSONObject(Object json) {
    JsObject.assign(this, json);
  }

  @JsMethod
  public native Object toJSON() /*-{
    var prototype = Object.getPrototypeOf(this);
    var propertyNames = Object.getOwnPropertyNames(prototype);

    var hasGetter = propertyNames.some(function(propertyName) {
      var propertyDescriptor = Object.getOwnPropertyDescriptor(prototype, propertyName);
      return propertyDescriptor.get;
    });

    if (!hasGetter) {
      return this;
    }

    var json = {};
    propertyNames.forEach(function(propertyName) {
      var propertyDescriptor = Object.getOwnPropertyDescriptor(prototype, propertyName);
      if (propertyDescriptor.get) {
        json[propertyName] = this[propertyName];
      }
    }, this);

    return json;
  }-*/;

  @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
  protected static class JsObject {
    public static native Object assign(Object target, Object source);
  }
}
