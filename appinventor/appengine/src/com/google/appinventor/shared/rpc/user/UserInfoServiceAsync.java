// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.user;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface for the service providing user related information. All
 * declarations in this interface are mirrored in {@link UserInfoService}.
 * For further information see {@link UserInfoService}.
 *
 */
public interface UserInfoServiceAsync {
  /**
   * getUserCommunityLogin
   */
  void getUserCommunityLogin(AsyncCallback<Boolean> callback);

  /**
   * setUserCommunityLogin
   */
  void setUserCommunityLogin(boolean communityLogin, AsyncCallback<Void> callback);

  /**
   * gets users community email address
   */
  public void getCommunityLoginEmail(AsyncCallback<String> callback);

  /**
   * sets users community email address
   */
  public void setCommunityLoginEmail(String email, AsyncCallback<Void> callback);


  /**
   * login user to community account
   */
  public void loginToCommunity(String email, AsyncCallback<Void> callback);

  /**
   * logout user from community account
   */
  public void logoutFromCommunity(AsyncCallback<Void> callback);


  /**
   * @see UserInfoService#getSystemConfig()
   */
  void getSystemConfig(String sessionId, AsyncCallback<Config> callback);

  /**
   * @see UserInfoService#getUserBackpack()
   */
  void getUserBackpack(AsyncCallback<String> callback);

  /**
   * @see UserInfoService#getUserInformation()
   */
  void getUserInformation(String sessionId, AsyncCallback<User> callback);

  /**
   * @see UserInfoService#loadUserSettings()
   */
  void loadUserSettings(AsyncCallback<String> callback);

  /**
   * @see UserInfoService#storeUserBackpack(String)
   */
  void storeUserBackpack(String backpack, AsyncCallback<Void> callback);

  /**
   * @see UserInfoService#storeUserSettings(String)
   */
  void storeUserSettings(String settings, AsyncCallback<Void> callback);

  /**
   * @see UserInfoService#hasUserFile(String)
   */
  void hasUserFile(String fileName, AsyncCallback<Boolean> callback);

  /**
   * @see UserInfoService#deleteUserFile(String)
   */
  void deleteUserFile(String fileName, AsyncCallback<Void> callback);

  /**
   * @see UserInfoService#noop(String)
   */
  void noop(AsyncCallback<Void> callback);

  /**
   * @see UserInfoService#getSharedBackpack(String)
   */
  void getSharedBackpack(String backPackId, AsyncCallback<String> callback);

  /**
   * @see UserInfoService#storeSharedBackpack(String, String)
   */
  void storeSharedBackpack(String backPackId, String content, AsyncCallback<Void> callback);

  /**
   * @see UserInfoService#deleteAccount()
   */

  void deleteAccount(AsyncCallback<String> callback);

}
