// Copyright 2008 Google Inc. All Rights Reserved.

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
  void getUserInformation(AsyncCallback<User> callback);

  /**
   * @see UserInfoService#loadUserSettings()
   */
  void loadUserSettings(AsyncCallback<String> callback);

  /**
   * @see UserInfoService#storeUserSettings(String)
   */
  void storeUserSettings(String settings, AsyncCallback<Void> callback);
}
