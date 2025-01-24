package java.net;

public class URI {
  public static native URI create(String uri) /*-{
    return new $wnd.URL.parse(uri);
  }-*/;
}
