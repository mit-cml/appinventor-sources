// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
