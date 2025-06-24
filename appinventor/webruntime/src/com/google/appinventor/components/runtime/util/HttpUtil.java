package com.google.appinventor.components.runtime.util;

public class HttpUtil {
  public interface HttpCallback {
    void onResponse(int responseCode, byte[] content, String error);
  }

  public static native void post(String tag, String serviceUrl, byte[] content, HttpCallback callback) /*-{
    fetch(serviceUrl, {
      method: 'POST',
      body: content
    }).then(
      function(response) {
        if (response.ok) {
          return response.arrayBuffer();
        } else {
          throw new Error('Network response was not ok: ' + response.statusText);
        }
      }
    ).then(
      function(buffer) {
        var byteArray = new Uint8Array(buffer);
        callback.@com.google.appinventor.components.runtime.util.HttpUtil.HttpCallback::onResponse(*)(200, byteArray, null);
      }
    )['catch'](
      function(error) {
        callback.@com.google.appinventor.components.runtime.util.HttpUtil.HttpCallback::onResponse(*)(500, null, "" + error);
      }
    )
  }-*/;
}
