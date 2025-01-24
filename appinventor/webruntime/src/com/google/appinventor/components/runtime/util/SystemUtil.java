package com.google.appinventor.components.runtime.util;

public class SystemUtil {
  public static native void exit(int code) /*-{
    $wnd.close();
  }-*/;
}
