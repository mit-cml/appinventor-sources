// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
  public static final String USER_TEMPLATE_URLS = "TemplateUrls";
  // If DISABLED_USER_URL is non-empty, then it is the URL to display in a frame
  // inside of a modal dialog, displayed at login, with no exit. This is used to
  // disable someone's account. The URL can be user specific in order to deliver
  // a particular message to a particular user.
  public static final String DISABLED_USER_URL = "DisabledUserUrl";
  public static final String USER_LAST_LOCALE = "LastLocale";

  public static final String SPLASH_SETTINGS = "SplashSettings";

  public static final String SPLASH_SETTINGS_SHOWSURVEY = "ShowSurvey";
  public static final String SPLASH_SETTINGS_DECLINED = "DeclinedSurvey";
  public static final String SPLASH_SETTINGS_VERSION = "SplashVersion";

  /**
   *  YoungAndroid settings.
   */
  // TODO(markf): There are some things which assume "SimpleSettings" which were hard to duplicate
  public static final String USER_YOUNG_ANDROID_SETTINGS = "SimpleSettings";
  public static final String PROJECT_YOUNG_ANDROID_SETTINGS = "SimpleSettings";

  // Project settings
  public static final String YOUNG_ANDROID_SETTINGS_ICON = "Icon";
  public static final String YOUNG_ANDROID_SETTINGS_SHOW_HIDDEN_COMPONENTS = "ShowHiddenComponents";
  public static final String YOUNG_ANDROID_SETTINGS_PHONE_TABLET = "PhoneTablet";
  public static final String YOUNG_ANDROID_SETTINGS_VERSION_CODE = "VersionCode";
  public static final String YOUNG_ANDROID_SETTINGS_VERSION_NAME = "VersionName";
  public static final String YOUNG_ANDROID_SETTINGS_USES_LOCATION = "UsesLocation";
  public static final String YOUNG_ANDROID_SETTINGS_SIZING = "Sizing";
  public static final String YOUNG_ANDROID_SETTINGS_APP_NAME = "AppName";
  public static final String YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON = "ShowListsAsJson";
  public static final String YOUNG_ANDROID_SETTINGS_TUTORIAL_URL = "TutorialURL";
  public static final String YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET = "BlocksToolkit";
  public static final String YOUNG_ANDROID_SETTINGS_ACTIONBAR = "ActionBar";
  public static final String YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR = "PrimaryColor";
  public static final String YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK = "PrimaryColorDark";
  public static final String YOUNG_ANDROID_SETTINGS_ACCENT_COLOR = "AccentColor";
  public static final String YOUNG_ANDROID_SETTINGS_THEME = "Theme";

  /**
   * Settings for the Blocks editor.
   */
  public static final String BLOCKS_SETTINGS = "BlocksSettings";
  public static final String GRID_ENABLED = "Grid";
  public static final String SNAP_ENABLED = "Snap";
}
