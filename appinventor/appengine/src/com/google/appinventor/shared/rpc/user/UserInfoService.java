// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
   * Retrieves system configuration information, including
   * the current User information.
   *
   */

  Config getSystemConfig(String sessionId);

  /**
   * Retrieve's the stored Backpack
   *
   * @return the backpack as an xml string
   */
  String getUserBackpack();

  /**
   * Retrieves the user's settings.
   *
   * @return  user's settings
   */
  String loadUserSettings();

  /**
   * Store the user's backpack
   * @param backpack string containing the backpack xml
   */
  void storeUserBackpack(String backpack);

  /**
   * Stores the user's settings.
   * @param settings  user's settings
   */
  void storeUserSettings(String settings);

  /**
   * Returns true if the current user has a user file with the given file name
   */
  boolean hasUserFile(String fileName);

  /**
   * Deletes the user file with the given file name
   */
  void deleteUserFile(String fileName);

  /**
   * No-Op Do nothing, but will refresh the session cookie as a side-effect
   */
  void noop();

  /**
   * Retrieve the contents of a shared backpack.
   */
  public String getSharedBackpack(String backPackId);

  /**
   * Store the contents of a shared backpack.
   */

  public void storeSharedBackpack(String backPackId, String content);

  /**
   * Delete a users account
   */

  public String deleteAccount();

}
