package com.google.appinventor.components.runtime.util;

public class AsynchUtil {
  public static native void runAsynchronously(final Runnable call) /*-{
    setTimeout(function() {
      call.@java.lang.Runnable::run()();
    }, 0);
  }-*/;
}
