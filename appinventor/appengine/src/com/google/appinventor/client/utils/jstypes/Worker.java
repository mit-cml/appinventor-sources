package com.google.appinventor.client.utils.jstypes;

import jsinterop.annotations.*;

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

  @JsType(isNative = true, namespace = JsPackage.GLOBAL)
  public interface ErrorEvent extends Event {
    @JsProperty(name = "message")
    String getMessage();

    @JsProperty(name = "error")
    Object getError();

    @JsProperty(name = "fileno")
    String getFileName();

    @JsProperty(name = "lineno")
    int getLineNo();

    @JsProperty(name = "colno")
    int getColNo();
  }

  public Worker(String scriptURL, WorkerOptions opts) {}

  public native void postMessage(Object message);

  public native void terminate();

  public native <T extends Event> void addEventListener(String type, EventListener<T> listener);

  public native <T extends Event> void removeEventListener(String type, EventListener<T> listener);
}
