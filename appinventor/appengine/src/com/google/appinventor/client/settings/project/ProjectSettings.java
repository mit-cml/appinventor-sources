// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.settings.project;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.utils.Promise.resolve;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.settings.CommonSettings;
import com.google.appinventor.client.settings.SettingsAccessProvider;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.user.client.Command;
import java.util.logging.Logger;

/**
 * Collection of project settings.
 *
 */
public final class ProjectSettings extends CommonSettings implements SettingsAccessProvider {

  private static final Logger LOG = Logger.getLogger(ProjectSettings.class.getName());

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
  public Promise<ProjectSettings> loadSettings() {
    return Promise.<String>call(MESSAGES.settingsLoadError(),
        c -> Ode.getInstance().getProjectService().loadProjectSettings(project.getProjectId(), c))
        .then(result -> {
          LOG.info("Loaded project settings: " + result);
          decodeSettings(result);
          changed = false;
          return resolve(ProjectSettings.this);
        });
  }

  @Override
  public void saveSettings(final Command command) {
    if (Ode.getInstance().isReadOnly()) {
      return;                   // No changes when in read only mode
    } else if (!changed) {
      // Do not save project settings if they haven't changed.
      return;
    }
    String s = encodeSettings();
    LOG.info("Saving project settings: " + s);
    Ode.getInstance().getProjectService().storeProjectSettings(
        Ode.getInstance().getSessionId(),
        project.getProjectId(), s,
        new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.settingsSaveError()) {
          @Override
          public void onSuccess(Void result) {
            changed = false;
            if (command != null) {
              command.execute();
            }
          }
        });
  }
}
