// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;

/**
 * A WebViewClient that provides functionality that needs Android 2.2 Froyo or higher.
 */
public class FroyoWebViewClient<T extends Component> extends WebViewClient {
  private final boolean followLinks;
  private final boolean ignoreErrors;
  private final Form form;
  private final T component;

  public FroyoWebViewClient(boolean followLinks, boolean ignoreErrors, Form form, T component) {
    this.followLinks = followLinks;
    this.ignoreErrors = ignoreErrors;
    this.form = form;
    this.component = component;
  }

  public T getComponent() {
    return component;
  }

  public Form getForm() {
    return form;
  }

  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    return !followLinks;
  }

  @Override
  public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    if (ignoreErrors) {
      handler.proceed();
    } else {
      handler.cancel();
      form.dispatchErrorOccurredEvent(component, "WebView",
        ErrorMessages.ERROR_WEBVIEW_SSL_ERROR);
    }
  }

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    EventDispatcher.dispatchEvent(component, "BeforePageLoad", url);
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    EventDispatcher.dispatchEvent(component, "PageLoaded", url);
  }

  @Override
  public void onReceivedError(WebView view, final int errorCode, final String description, final String failingUrl) {
    form.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(component, "ErrorOccurred", errorCode, description, failingUrl);
      }
    });
  }
}
