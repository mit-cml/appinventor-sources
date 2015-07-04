// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.settings.project;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.settings.CommonSettings;
import com.google.appinventor.client.settings.SettingsAccessProvider;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.user.client.Command;

/**
 * Collection of project settings.
 *
 */
public final class ProjectSettings extends CommonSettings implements SettingsAccessProvider {

  // Corresponding project
  private final Project project;

  /**
   * Creates new project settings object.
   */
  public ProjectSettings(Project project) {
    this.project = project;
    String projectType = project.getProjectType();

    if (projectType.equals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE)) {
      addSettings(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          new YoungAndroidSettings(project));
    }
  }

  public long getProjectId() {
    return project.getProjectId();
  }

  // SettingsAccessProvider implementation

  @Override
  public void loadSettings() {
    Ode.getInstance().getProjectService().loadProjectSettings(
        project.getProjectId(),
        new OdeAsyncCallback<String>(
            // failure message
            MESSAGES.settingsLoadError()) {
          @Override
          public void onSuccess(String result) {
            OdeLog.log("Loaded project settings: " + result);
            decodeSettings(result);
          }
        });
  }

  @Override
  public void saveSettings(final Command command) {
    String s = encodeSettings();
    OdeLog.log("Saving project settings: " + s);
    Ode.getInstance().getProjectService().storeProjectSettings(
        Ode.getInstance().getSessionId(),
        project.getProjectId(), s,
        new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.settingsSaveError()) {
          @Override
          public void onSuccess(Void result) {
            if (command != null) {
              command.execute();
            }
          }
        });
  }
}
