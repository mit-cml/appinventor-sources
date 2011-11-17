// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.user;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Interface for the service providing user related information.
 *
 */
@RemoteServiceRelativePath(ServerLayout.USER_INFO_SERVICE)
public interface UserInfoService extends RemoteService {

  /**
   * Retrieves information about the current user
   *
   * @return  user information
   */
  User getUserInformation();

  /**
   * Retrieves the user's settings.
   *
   * @return  user's settings
   */
  String loadUserSettings();

  /**
   * Stores the user's settings.
   * @param settings  user's settings
   */
  void storeUserSettings(String settings);
}
