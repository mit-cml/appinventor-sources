package com.google.appinventor.client.utils;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Worker")
public class DedicatedWorker {
    @JsMethod
    public native void postMessage(Object message);

    @JsMethod
    public native void addEventListener(String type, EventListener listener);

    @JsConstructor
    public DedicatedWorker(String scriptURL) {}

    @JsType
    public interface EventListener {
        void handleEvent(MessageEvent event);
    }

    @JsType
    public interface MessageEvent {
        @JsProperty(name = "data")
        Object getData();
    }
}

