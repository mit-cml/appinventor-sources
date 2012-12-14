// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
