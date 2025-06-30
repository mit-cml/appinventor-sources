// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.local;

import com.google.appinventor.shared.rpc.user.Config;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.UserInfoServiceAsync;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LocalUserInfoService implements UserInfoServiceAsync {
  @Override
  public void getSystemConfig(String sessionId, AsyncCallback<Config> callback) {
    Config config = new Config();
    User user = new User("1", "test@example.com", true, false, "1");
    user.setReadOnly(false);
    config.setUser(user);
    SplashConfig splashConfig = new SplashConfig(0, 0, 0, "");
    config.setSplashConfig(splashConfig);
    config.setIosExtensions("[]");
    callback.onSuccess(config);
  }

  @Override
  public void getUserBackpack(AsyncCallback<String> callback) {
    callback.onSuccess("[]");
  }

  @Override
  public void loadUserSettings(AsyncCallback<String> callback) {
    String settings = "{\"GeneralSettings\":{\"LastLocale\":\"en\",\"Folders\":\"\",\"AutoloadLastProject\":\"false\",\"ShowUIPicker\":\"false\",\"NewLayout\":\"true\"}}";
    callback.onSuccess(settings);
  }

  @Override
  public void storeUserBackpack(String backpack, AsyncCallback<Void> callback) {

  }

  @Override
  public void storeUserSettings(String settings, AsyncCallback<Void> callback) {

  }

  @Override
  public void hasUserFile(String fileName, AsyncCallback<Boolean> callback) {

  }

  @Override
  public void deleteUserFile(String fileName, AsyncCallback<Void> callback) {

  }

  @Override
  public void noop(AsyncCallback<Void> callback) {

  }

  @Override
  public void getSharedBackpack(String backPackId, AsyncCallback<String> callback) {

  }

  @Override
  public void storeSharedBackpack(String backPackId, String content, AsyncCallback<Void> callback) {

  }

  @Override
  public void deleteAccount(AsyncCallback<String> callback) {

  }
}
