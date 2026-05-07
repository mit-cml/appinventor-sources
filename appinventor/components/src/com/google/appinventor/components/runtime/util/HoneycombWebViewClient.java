// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.WebViewer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * A WebViewClient that provides functionality that needs Android 3.0 Honeycomb or higher.
 */
@androidx.annotation.RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class HoneycombWebViewClient extends FroyoWebViewClient<WebViewer> {
  private static final String TAG = HoneycombWebViewClient.class.getSimpleName();
  private static final String ASSET_PREFIX = "file:///appinventor_asset/";

  public HoneycombWebViewClient(boolean followLinks, boolean ignoreErrors, Form form,
      WebViewer component) {
    super(followLinks, ignoreErrors, form, component);
  }

  @SuppressWarnings("deprecation")
  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
    if (url.startsWith("http://localhost/") || url.startsWith(ASSET_PREFIX)) {
      return handleAppRequest(url);
    }
    return super.shouldInterceptRequest(view, url);
  }

  @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
    Log.d(TAG, "scheme = " + request.getUrl().getScheme());
    if ("localhost".equals(request.getUrl().getAuthority())
        || request.getUrl().toString().startsWith(ASSET_PREFIX)) {
      return handleAppRequest(request.getUrl().toString());
    }
    return super.shouldInterceptRequest(view, request);
  }

  protected WebResourceResponse handleAppRequest(String url) {
    String path;
    if (url.startsWith(ASSET_PREFIX)) {
      path = url.substring(ASSET_PREFIX.length());
    } else {
      path = url.substring(url.indexOf("//localhost/") + 12);
    }
    InputStream stream;
    try {
      Log.i(TAG, "webviewer requested path = " + path);
      stream = getForm().openAsset(path);
      Map<String, String> headers = new HashMap<>();
      headers.put("Access-Control-Allow-Origin", "localhost");
      String mimeType = URLConnection.getFileNameMap().getContentTypeFor(path);
      String encoding = "utf-8";
      Log.i(TAG, "Mime type = " + mimeType);
      if (mimeType == null
          || (!mimeType.startsWith("text/") && !mimeType.equals("application/javascript"))) {
        encoding = null;
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        return new WebResourceResponse(mimeType, encoding, 200, "OK", headers, stream);
      } else {
        return new WebResourceResponse(mimeType, encoding, stream);
      }
    } catch (IOException e) {
      ByteArrayInputStream error = new ByteArrayInputStream("404 Not Found".getBytes());
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found", null, error);
      } else {
        return new WebResourceResponse("text/plain", "utf-8", error);
      }
    }
  }
}
