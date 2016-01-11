// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.components;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async for the Firebase Authentication RPC.
 *
 * @author will2596@gmail.com (William Byrne)
 */
public interface FirebaseAuthServiceAsync {

  void getToken(String project, AsyncCallback<String> callback);
}
