// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.local;

import com.google.appinventor.shared.rpc.tokenauth.TokenAuthServiceAsync;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LocalTokenAuthService implements TokenAuthServiceAsync {
  @Override
  public void getCloudDBToken(AsyncCallback<String> callback) {
    callback.onSuccess("YOUR CLOUDDB TOKEN HERE");
  }

  @Override
  public void getTranslateToken(AsyncCallback<String> callback) {
    callback.onSuccess("YOUR TRANSLATE TOKEN HERE");
  }

  @Override
  public void getChatBotToken(AsyncCallback<String> callback) {
    callback.onSuccess("YOUR CHATBOT TOKEN HERE");
  }
}
