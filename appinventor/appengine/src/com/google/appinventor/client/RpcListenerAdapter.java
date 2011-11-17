// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

/**
 * An empty implementation of RPC listeners to simplify implementation.
 *
 */
public class RpcListenerAdapter implements RpcListener {
  @Override
  public void onFailure(String method, Throwable caught) {
  }

  @Override
  public void onStart(String method, Object... params) {
  }

  @Override
  public void onSuccess(String method, Object result) {
  }
}
