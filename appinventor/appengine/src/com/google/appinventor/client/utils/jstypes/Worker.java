package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Worker {

  @JsFunction
  public interface EventListener<T extends Event> {
    void handleEvent(T event);
  }

  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  public interface Event {}

  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  public interface MessageEvent extends Event {
    @JsProperty(name = "data")
    Object getData();
  }

  public Worker(String scriptURL) {}

  public native void postMessage(Object message);

  public native void terminate();

  public native <T extends Event> void addEventListener(String type, EventListener<T> listener);

  public native <T extends Event> void removeEventListener(String type, EventListener<T> listener);
}
