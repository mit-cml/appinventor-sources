package android.view;

import android.util.DisplayMetrics;

public class Display {
  public void getMetrics(DisplayMetrics outMetrics) {
    // TODO(ewpatton): Real implementation
  }

  public native int getWidth() /*-{
    return $wnd.innerWidth;
  }-*/;

  public native int getHeight() /*-{
    return $wnd.innerHeight;
  }-*/;
}
