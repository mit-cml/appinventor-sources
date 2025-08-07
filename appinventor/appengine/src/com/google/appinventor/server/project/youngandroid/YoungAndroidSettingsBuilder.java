// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.ASSETS_FOLDER;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.SRC_FOLDER;
import static com.google.appinventor.shared.settings.SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_ACTIONBAR;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_AIVERSIONING;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_DEFAULTFILESCOPE;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_LAST_OPENED;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE;
import static com.google.appinventor.shared.settings.SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME;

import com.google.appinventor.shared.settings.Settings;
import com.google.common.base.Strings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.json.JSONObject;

public class YoungAndroidSettingsBuilder {
  private String projectName = "";
  private String qualifiedFormName = "";
  private String icon = "";
  private String versionCode = "1";
  private String versionName = "1.0";
  private String usesLocation = "false";
  private String appName = "";
  private String sizing = "Fixed";
  private String showListsAsJson = "false";
  private String tutorialUrl = "";
  private String blockSubset = "";
  private String actionBar = "false";
  private String theme = "AppTheme.Light.DarkActionBar";
  private String primaryColor = "0";
  private String primaryColorDark = "0";
  private String accentColor = "0";
  private String defaultFileScope = "App";
  private String aiVersioning = "";
  private String lastOpened = "Screen1";

  public YoungAndroidSettingsBuilder() {
  }

  /**
   * Read project settings from a parsed JSON settings object.
   *
   * @param settings the original settings object to build off of
   */
  public YoungAndroidSettingsBuilder(Settings settings) {
    icon = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_ICON));
    versionCode = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_VERSION_CODE));
    versionName = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_VERSION_NAME));
    usesLocation = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_USES_LOCATION));
    sizing = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_SIZING));
    showListsAsJson = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON));
    tutorialUrl = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_TUTORIAL_URL));
    blockSubset = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET));
    appName = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_APP_NAME));
    actionBar = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_ACTIONBAR));
    theme = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_THEME));
    primaryColor = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR));
    primaryColorDark = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK));
    accentColor = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_ACCENT_COLOR));
    defaultFileScope = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_DEFAULTFILESCOPE));
    aiVersioning = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_AIVERSIONING));
    lastOpened = Strings.nullToEmpty(settings.getSetting(PROJECT_YOUNG_ANDROID_SETTINGS,
        YOUNG_ANDROID_SETTINGS_LAST_OPENED));
  }

  /**
   * Read project settings from a project.properties file.
   *
   * @param properties the original properties object to build off of
   */
  public YoungAndroidSettingsBuilder(Properties properties) {
    projectName = properties.getProperty("name", "");
    qualifiedFormName = properties.getProperty("main", "");
    appName = properties.getProperty("aname", "");
    icon = properties.getProperty("icon", "");
    versionCode = properties.getProperty("versioncode", "");
    versionName = properties.getProperty("versionname", "");
    usesLocation = properties.getProperty("useslocation", "");
    sizing = properties.getProperty("sizing", "");
    showListsAsJson = properties.getProperty("showlistsasjson", "");
    tutorialUrl = properties.getProperty("tutorialurl", "");
    blockSubset = properties.getProperty("subsetjson", "");
    actionBar = properties.getProperty("actionbar", "");
    theme = properties.getProperty("theme", "");
    primaryColor = properties.getProperty("color.primary", "");
    primaryColorDark = properties.getProperty("color.primary.dark", "");
    accentColor = properties.getProperty("color.accent", "");
    defaultFileScope = properties.getProperty("defaultfilescope", "");
    aiVersioning = properties.getProperty("aiversioning", "");
    lastOpened = properties.getProperty("lastopened", "");
  }

  public YoungAndroidSettingsBuilder setProjectName(String projectName) {
    this.projectName = projectName;
    return this;
  }

  public YoungAndroidSettingsBuilder setQualifiedFormName(String qualifiedFormName) {
    this.qualifiedFormName = qualifiedFormName;
    return this;
  }

  public YoungAndroidSettingsBuilder setIcon(String icon) {
    this.icon = icon;
    return this;
  }

  public YoungAndroidSettingsBuilder setVersionCode(String versionCode) {
    this.versionCode = versionCode;
    return this;
  }

  public YoungAndroidSettingsBuilder setVersionName(String versionName) {
    this.versionName = versionName;
    return this;
  }

  public YoungAndroidSettingsBuilder setUsesLocation(String usesLocation) {
    this.usesLocation = usesLocation;
    return this;
  }

  public YoungAndroidSettingsBuilder setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public YoungAndroidSettingsBuilder setSizing(String sizing) {
    this.sizing = sizing;
    return this;
  }

  public YoungAndroidSettingsBuilder setShowListsAsJson(String showListsAsJson) {
    this.showListsAsJson = showListsAsJson;
    return this;
  }

  public YoungAndroidSettingsBuilder setTutorialUrl(String tutorialUrl) {
    this.tutorialUrl = tutorialUrl;
    return this;
  }

  public YoungAndroidSettingsBuilder setBlocksSubset(String blockSubset) {
    this.blockSubset = blockSubset;
    return this;
  }

  public YoungAndroidSettingsBuilder setActionBar(String actionBar) {
    this.actionBar = actionBar;
    return this;
  }

  public YoungAndroidSettingsBuilder setTheme(String theme) {
    this.theme = theme;
    return this;
  }

  public YoungAndroidSettingsBuilder setPrimaryColor(String primaryColor) {
    this.primaryColor = primaryColor;
    return this;
  }

  public YoungAndroidSettingsBuilder setPrimaryColorDark(String primaryColorDark) {
    this.primaryColorDark = primaryColorDark;
    return this;
  }

  public YoungAndroidSettingsBuilder setAccentColor(String accentColor) {
    this.accentColor = accentColor;
    return this;
  }

  public YoungAndroidSettingsBuilder setDefaultFileScope(String defaultFileScope) {
    this.defaultFileScope = defaultFileScope;
    return this;
  }

  public YoungAndroidSettingsBuilder setAIVersioning(String aiVersioning) {
    this.aiVersioning = aiVersioning;
    return this;
  }

  public YoungAndroidSettingsBuilder setDefaultLastOpened(String lastOpened) {
    this.lastOpened = lastOpened;
    return this;
  }

  /**
   * Convert the internal settings into a JSON structure.
   *
   * @return a JSON string containing the settings
   */
  public String build() {
    JSONObject object = new JSONObject();
    object.put(YOUNG_ANDROID_SETTINGS_ICON, icon);
    object.put(YOUNG_ANDROID_SETTINGS_VERSION_CODE, versionCode);
    object.put(YOUNG_ANDROID_SETTINGS_VERSION_NAME, versionName);
    object.put(YOUNG_ANDROID_SETTINGS_USES_LOCATION, usesLocation);
    object.put(YOUNG_ANDROID_SETTINGS_APP_NAME, appName);
    object.put(YOUNG_ANDROID_SETTINGS_SIZING, sizing);
    object.put(YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON, showListsAsJson);
    object.put(YOUNG_ANDROID_SETTINGS_TUTORIAL_URL, tutorialUrl);
    object.put(YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET, blockSubset);
    object.put(YOUNG_ANDROID_SETTINGS_ACTIONBAR, actionBar);
    object.put(YOUNG_ANDROID_SETTINGS_THEME, theme);
    object.put(YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR, primaryColor);
    object.put(YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK, primaryColorDark);
    object.put(YOUNG_ANDROID_SETTINGS_ACCENT_COLOR, accentColor);
    object.put(YOUNG_ANDROID_SETTINGS_DEFAULTFILESCOPE, defaultFileScope);
    object.put(YOUNG_ANDROID_SETTINGS_AIVERSIONING, aiVersioning);
    object.put(YOUNG_ANDROID_SETTINGS_LAST_OPENED, lastOpened);
    JSONObject wrapper = new JSONObject();
    wrapper.put(PROJECT_YOUNG_ANDROID_SETTINGS, object);
    return wrapper.toString();
  }

  /**
   * Convert the internal settings into a Java Properties object.
   *
   * @return a properties file contents containing the settings
   */
  public String toProperties() {
    Properties result = new Properties();
    result.put("main", qualifiedFormName);
    result.put("name", projectName);
    result.put("aname", appName);
    result.put("assets", "../" + ASSETS_FOLDER);
    result.put("source", "../" + SRC_FOLDER);
    result.put("build", "../build");
    addPropertyIfSet(result, "icon", icon);
    addPropertyIfSet(result, "versioncode", versionCode);
    addPropertyIfSet(result, "versionname", versionName);
    addPropertyIfSet(result, "useslocation", usesLocation);
    addPropertyIfSet(result, "sizing", sizing);
    addPropertyIfSet(result, "showlistsasjson", showListsAsJson);
    addPropertyIfSet(result, "tutorialurl", tutorialUrl);
    addPropertyIfSet(result, "subsetjson", blockSubset);
    addPropertyIfSet(result, "actionbar", actionBar);
    addPropertyIfSet(result, "theme", theme);
    addPropertyIfSet(result, "color.primary", primaryColor);
    addPropertyIfSet(result, "color.primary.dark", primaryColorDark);
    addPropertyIfSet(result, "color.accent", accentColor);
    addPropertyIfSet(result, "defaultfilescope", defaultFileScope);
    addPropertyIfSet(result, "aiversioning", aiVersioning);
    addPropertyIfSet(result, "lastopened", lastOpened);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      result.store(out, "");
    } catch (IOException e) {
      throw new RuntimeException("Unexpected IOException writing to byte buffer", e);
    }
    return out.toString();
  }

  private static void addPropertyIfSet(Properties properties, String key, String value) {
    if (value != null && !value.isEmpty()) {
      properties.put(key, value);
    }
  }

  @Override
  public String toString() {
    return build();
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof YoungAndroidSettingsBuilder) {
      if (object == this) {
        return true;
      }
      YoungAndroidSettingsBuilder other = (YoungAndroidSettingsBuilder) object;
      boolean result;
      result = other.projectName.equals(projectName);
      result &= other.qualifiedFormName.equals(qualifiedFormName);
      result &= other.icon.equals(icon);
      result &= other.versionCode.equals(versionCode);
      result &= other.versionName.equals(versionName);
      result &= other.usesLocation.equals(usesLocation);
      result &= other.sizing.equals(sizing);
      result &= other.appName.equals(appName);
      result &= other.showListsAsJson.equals(showListsAsJson);
      result &= other.tutorialUrl.equals(tutorialUrl);
      result &= other.blockSubset.equals(blockSubset);
      result &= other.actionBar.equals(actionBar);
      result &= other.primaryColor.equals(primaryColor);
      result &= other.primaryColorDark.equals(primaryColorDark);
      result &= other.accentColor.equals(accentColor);
      result &= other.defaultFileScope.equals(defaultFileScope);
      result &= other.aiVersioning.equals(aiVersioning);
      result &= other.lastOpened.equals(lastOpened);
      return result;
    }
    return false;
  }
}
