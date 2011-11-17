// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.settings.user;

import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.shared.settings.SettingsConstants;

/**
 * General Young Android settings.
 *
 */
public final class YoungAndroidSettings extends Settings {

  /**
   * Creates a new instance of user-specific YoungAndroid settings.
   *
   * @param user  user associated with settings
   */
  public YoungAndroidSettings(UserInfoProvider user) {
    super(SettingsConstants.USER_YOUNG_ANDROID_SETTINGS);
  }
}
