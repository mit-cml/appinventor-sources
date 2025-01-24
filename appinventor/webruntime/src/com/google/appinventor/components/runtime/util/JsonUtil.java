package com.google.appinventor.components.runtime.util;

import org.json.JSONException;

public class JsonUtil {
  public static native Object getObjectFromJson(String json, boolean useDicts) throws JSONException /*-{
    return JSON.parse(json);
  }-*/;

  public static native String getJsonRepresentation(Object obj) throws JSONException /*-{
    return JSON.stringify(obj);
  }-*/;
}
