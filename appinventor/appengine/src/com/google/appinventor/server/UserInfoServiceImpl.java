// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.UserInfoService;

/**
 * Implementation of the user information service.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class UserInfoServiceImpl extends OdeRemoteServiceServlet implements UserInfoService {

  // Storage of user settings
  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  private static final long serialVersionUID = -7316312435338169166L;

  /**
   * Returns user information.
   *
   * @return  user information record
   */
  @Override
  public User getUserInformation() {
    return userInfoProvider.getUser();
  }

  /**
   * Retrieves the user's settings.
   *
   * @return  user's settings
   */
  @Override
  public String loadUserSettings() {
    return storageIo.loadSettings(userInfoProvider.getUserId());
  }

  /**
   * Stores the user's settings.
   * @param settings  user's settings
   */
  @Override
  public void storeUserSettings(String settings) {
    storageIo.storeSettings(userInfoProvider.getUserId(), settings);
  }
}
