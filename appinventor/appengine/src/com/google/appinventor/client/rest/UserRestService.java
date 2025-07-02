package com.google.appinventor.client.rest;

import com.google.appinventor.shared.rpc.user.Config;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.UserInfoServiceAsync;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserRestService extends RestService implements UserInfoServiceAsync {
  private static final String USER_BASE = REST_BASE + "/user";

  @Override
  public void getSystemConfig(String sessionId, AsyncCallback<Config> callback) {
    final Config configPatch = new Config();
    configPatch.setUser(new User(null, null, false, false, sessionId));

    patch(USER_BASE + "/system-config", configPatch, Config.class, callback);
  }

  @Override
  public void getUserBackpack(AsyncCallback<String> callback) {
    get(USER_BASE + "/backpack", String.class, callback);
  }

  @Override
  public void loadUserSettings(AsyncCallback<String> callback) {
    get(USER_BASE + "/settings", String.class, callback);
  }

  @Override
  public void storeUserBackpack(String backpack, AsyncCallback<Void> callback) {
    put(USER_BASE + "/backpack", backpack, Void.class, callback);
  }

  @Override
  public void storeUserSettings(String settings, AsyncCallback<Void> callback) {
    put(USER_BASE + "/settings", settings, Void.class, callback);
  }

  @Override
  public void hasUserFile(String fileName, AsyncCallback<Boolean> callback) {
    head(USER_BASE + "/files/" + fileName, callback);
  }

  @Override
  public void deleteUserFile(String fileName, AsyncCallback<Void> callback) {
    delete(USER_BASE + "/files/" + fileName, Void.class, callback);
  }

  @Override
  public void noop(AsyncCallback<Void> callback) {
    get(USER_BASE + "/noop", Void.class, callback);
  }

  @Override
  public void getSharedBackpack(String backPackId, AsyncCallback<String> callback) {
    get(USER_BASE + "/shared-backpacks/" + backPackId, String.class, callback);
  }

  @Override
  public void storeSharedBackpack(String backPackId, String content, AsyncCallback<Void> callback) {
    put(USER_BASE + "/shared-backpacks/" + backPackId, content, Void.class, callback);
  }

  @Override
  public void deleteAccount(AsyncCallback<String> callback) {
    delete(USER_BASE + "/account", String.class, callback);
  }
}
