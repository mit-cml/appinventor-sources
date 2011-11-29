// Copyright 2008 Google Inc. All Rights Reserved.

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

  /**
   *  YoungAndroid settings.
   */
  // TODO(markf): There are some things which assume "SimpleSettings" which were hard to duplicate
  public static final String USER_YOUNG_ANDROID_SETTINGS = "SimpleSettings";
  public static final String PROJECT_YOUNG_ANDROID_SETTINGS = "SimpleSettings";

  // Project settings
  public static final String YOUNG_ANDROID_SETTINGS_ICON = "Icon";
  public static final String YOUNG_ANDROID_SETTINGS_SHOW_HIDDEN_COMPONENTS = "ShowHiddenComponents";
}
