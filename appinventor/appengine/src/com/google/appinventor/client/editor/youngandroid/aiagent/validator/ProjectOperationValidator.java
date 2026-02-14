// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.validator;

import com.google.appinventor.client.editor.youngandroid.DesignToolbar.DesignProject;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.json.client.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates project-level AI operations: SWITCH_SCREEN, CREATE_SCREEN,
 * DELETE_SCREEN, SET_PROJECT_PROP, TOGGLE_EDITOR.
 */
final class ProjectOperationValidator {

  /** Whitelist of properties allowed for SET_PROJECT_PROP. */
  private static final Set<String> ALLOWED_PROJECT_PROPERTIES = new HashSet<>(Arrays.asList(
      SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_ACTIONBAR,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_DEFAULTFILESCOPE
  ));

  private ProjectOperationValidator() {}

  static String validateSwitchScreen(JSONObject json) {
    String screen = AIJsonUtils.getStringField(json, "screen_name");
    if (screen == null || screen.isEmpty()) {
      return "SWITCH_SCREEN: missing 'screen_name' field";
    }
    DesignProject project = AIEditorState.getCurrentProject();
    if (project == null) {
      return "SWITCH_SCREEN: no current project";
    }
    if (!project.screens.containsKey(screen)) {
      return "SWITCH_SCREEN: screen '" + screen + "' does not exist";
    }
    return null;
  }

  static String validateToggleEditor(JSONObject json) {
    String view = AIJsonUtils.getStringField(json, "view");
    if (view == null || view.isEmpty()) {
      return "TOGGLE_EDITOR: missing 'view' field";
    }
    if (!"Designer".equals(view) && !"Blocks".equals(view)) {
      return "TOGGLE_EDITOR: 'view' must be 'Designer' or 'Blocks', got '" + view + "'";
    }
    return null;
  }

  static String validateCreateScreen(JSONObject json) {
    String screen = AIJsonUtils.getStringField(json, "screen_name");
    if (screen == null || screen.isEmpty()) {
      return "CREATE_SCREEN: missing 'screen_name' field";
    }
    if (!TextValidators.isValidIdentifier(screen)) {
      return "CREATE_SCREEN: '" + screen + "' is not a valid identifier";
    }
    if (TextValidators.isReservedName(screen)) {
      return "CREATE_SCREEN: '" + screen + "' is a reserved name";
    }
    DesignProject project = AIEditorState.getCurrentProject();
    if (project == null) {
      return "CREATE_SCREEN: no current project";
    }
    if (project.screens.containsKey(screen)) {
      return "CREATE_SCREEN: screen '" + screen + "' already exists";
    }
    return null;
  }

  static String validateDeleteScreen(JSONObject json) {
    String screen = AIJsonUtils.getStringField(json, "screen_name");
    if (screen == null || screen.isEmpty()) {
      return "DELETE_SCREEN: missing 'screen_name' field";
    }
    if ("Screen1".equals(screen)) {
      return "DELETE_SCREEN: cannot delete Screen1";
    }
    DesignProject project = AIEditorState.getCurrentProject();
    if (project == null) {
      return "DELETE_SCREEN: no current project";
    }
    if (!project.screens.containsKey(screen)) {
      return "DELETE_SCREEN: screen '" + screen + "' does not exist";
    }
    return null;
  }

  static String validateSetProjectProp(JSONObject json) {
    String property = AIJsonUtils.getStringField(json, "property");
    String value = AIJsonUtils.getStringField(json, "value");
    if (property == null || property.isEmpty()) {
      return "SET_PROJECT_PROP: missing 'property' field";
    }
    if (value == null) {
      return "SET_PROJECT_PROP: missing 'value' field";
    }
    if (!ALLOWED_PROJECT_PROPERTIES.contains(property)) {
      return "SET_PROJECT_PROP: '" + property + "' is not a valid project property. "
          + "Allowed properties: " + ALLOWED_PROJECT_PROPERTIES;
    }
    return null;
  }
}
