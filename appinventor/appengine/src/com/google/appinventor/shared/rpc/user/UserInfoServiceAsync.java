// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
   * @see UserInfoService#getUserInformation()
   */
  void getUserInformation(String sessionId, AsyncCallback<User> callback);

  /**
   * @see UserInfoService#loadUserSettings()
   */
  void loadUserSettings(AsyncCallback<String> callback);

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
}
