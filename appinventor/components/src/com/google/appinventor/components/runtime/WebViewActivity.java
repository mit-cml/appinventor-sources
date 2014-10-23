// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Creates a WebView to display the web page identified by the URL passed in the
 * Intent used to start this activity. Javascript is enabled in the WebView
 * and the WebView is set up to handle all subsequent URLs. If it ever gets
 * directed to a URL with scheme equal to {@link Form#APPINVENTOR_URL_SCHEME},
 * returns that URL to the invoking activity via a result intent.
 *
 * This is intended for dealing with the web-based part of the OAuth protocol.
 * Start up this activity with an OAuth authorize URL and set up a redirect URL
 * for the authorization to have scheme {@link Form#APPINVENTOR_URL_SCHEME}.
 * See the {@link Twitter} component for an example.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public final class WebViewActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    WebView webview;

    super.onCreate(savedInstanceState);
    webview = new WebView(this);
    webview.getSettings().setJavaScriptEnabled(true);
    webview.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i("WebView", "Handling url " + url);
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if (scheme.equals(Form.APPINVENTOR_URL_SCHEME)) {
          Intent resultIntent = new Intent();
          resultIntent.setData(uri);
          setResult(RESULT_OK, resultIntent);
          finish();
        } else {
          view.loadUrl(url);
        }
        return true;
      }
    });
    setContentView(webview);

    Intent uriIntent = getIntent();
    if (uriIntent != null && uriIntent.getData() != null) {
      Uri uri = uriIntent.getData();
      String scheme = uri.getScheme();
      String host = uri.getHost();
      Log.i("WebView", "Got intent with URI: " + uri + ", scheme="
          + scheme + ", host=" + host);
      webview.loadUrl(uri.toString());
    }
  }
}
