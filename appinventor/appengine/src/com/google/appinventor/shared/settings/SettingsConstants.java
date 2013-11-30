// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.settings;

/**
 * Constants for settings.
 *
 */
public class SettingsConstants {
  private SettingsConstants() {
  }

  /**
   * General settings.
   */
  public static final String USER_GENERAL_SETTINGS = "GeneralSettings";

  public static final String GENERAL_SETTINGS_CURRENT_PROJECT_ID = "CurrentProjectId";

  public static final String SPLASH_SETTINGS = "SplashSettings";

  public static final String SPLASH_SETTINGS_SHOWSURVEY = "ShowSurvey";
  public static final String SPLASH_SETTINGS_DECLINED = "DeclinedSurvey";
  /**
   *  YoungAndroid settings.
   */
  // TODO(markf): There are some things which assume "SimpleSettings" which were hard to duplicate
  public static final String USER_YOUNG_ANDROID_SETTINGS = "SimpleSettings";
  public static final String PROJECT_YOUNG_ANDROID_SETTINGS = "SimpleSettings";

  // Project settings
  public static final String YOUNG_ANDROID_SETTINGS_ICON = "Icon";
  public static final String YOUNG_ANDROID_SETTINGS_SHOW_HIDDEN_COMPONENTS = "ShowHiddenComponents";
  public static final String YOUNG_ANDROID_SETTINGS_VERSION_CODE = "VersionCode";
  public static final String YOUNG_ANDROID_SETTINGS_VERSION_NAME = "VersionName";
  public static final String YOUNG_ANDROID_SETTINGS_USES_LOCATION = "UsesLocation";
}
