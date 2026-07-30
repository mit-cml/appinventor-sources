// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.junit.client.GWTTestCase;

public class ProjectSettingsTest extends GWTTestCase {

  /** The menu gate accepts only the exact string true and survives a settings save. */
  public void testLtiLaunchMarkerRequiresExactTrueValue() {
    ProjectSettings projectSettings = settingsForType(
        YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    Settings settings =
        projectSettings.getSettings(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);

    assertNotNull(settings);
    assertFalse(projectSettings.isLtiLaunched());

    settings.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED, "true");
    assertTrue(projectSettings.isLtiLaunched());
    assertTrue(projectSettings.encodeSettings().contains("\"LtiLaunched\":\"true\""));

    settings.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED, "");
    assertFalse(projectSettings.isLtiLaunched());

    settings.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED, "True");
    assertFalse(projectSettings.isLtiLaunched());

    settings.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED, "false");
    assertFalse(projectSettings.isLtiLaunched());
  }

  /** A project type with no Young Android settings never reports the marker. */
  public void testProjectWithoutYoungAndroidSettingsIsNotLtiLaunched() {
    assertFalse(settingsForType("Unknown").isLtiLaunched());
  }

  private static ProjectSettings settingsForType(String projectType) {
    UserProject projectInfo = new UserProject(1, "TestProject", projectType, 0, false);
    return new ProjectSettings(new Project(projectInfo));
  }

  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient";
  }
}
