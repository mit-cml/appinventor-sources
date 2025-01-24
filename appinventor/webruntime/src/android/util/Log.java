package android.util;

public class Log {

  public static native void d(String tag, String message)
    /*-{
      console.debug(tag + ": " + message);
    }-*/;

  public static native void e(String tag, String message)
    /*-{
      console.error(tag + ": " + message);
    }-*/;

    public static native void e(String tag, String message, Throwable e) /*-{
      console.error(tag + ": " + message, e);
    }-*/;

  public static native void i(String tag, String message)
    /*-{
      console.info(tag + ": " + message);
    }-*/;

  public static native void v(String tag, String message)
    /*-{
      console.trace(tag + ": " + message);
    }-*/;

  public static native void w(String tag, String message)
    /*-{
      console.warn(tag + ": " + message);
    }-*/;

  public static native void wtf(String tag, String message)
    /*-{
      console.error(tag + ": " + message);
    }-*/;

}
