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
import com.google.appinventor.client.settings.Settings;
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

  /**
   * Returns whether this project carries the marker set by an LTI launch.
   */
  public boolean isLtiLaunched() {
    Settings settings = getSettings(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);
    return settings != null && "true".equals(settings.getPropertyValue(
        SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED));
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
          Ode.getInstance().getTopToolbar().updateSubmitToLmsMenuItem();
          return resolve(ProjectSettings.this);
        });
  }

  /**
   * Saves the settings, and answers the given command once, whatever happens.
   *
   * <p>The caller counts the answer, so a path that returned without answering would leave a
   * save looking unfinished forever. A failure also puts the settings back in the queue, since
   * the caller has already taken them out of it and would otherwise never try them again.
   */
  @Override
  public void saveSettings(final Command command) {
    if (Ode.getInstance().isReadOnly() || !changed) {
      // Nothing to write, either because the session may not write or because nothing moved.
      if (command != null) {
        command.execute();
      }
      return;
    }
    final String sent = encodeSettings();
    LOG.info("Saving project settings: " + sent);
    Ode.getInstance().getProjectService().storeProjectSettings(
        Ode.getInstance().getSessionId(),
        project.getProjectId(), sent,
        new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.settingsSaveError()) {
          @Override
          public void onSuccess(Void result) {
            // Only what was sent is on the server. A setting the learner moved while this was
            // in flight is not, so saying nothing has changed would lose it.
            if (sent.equals(encodeSettings())) {
              changed = false;
            }
            if (command != null) {
              command.execute();
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            // Put them back the way a failed file save is put back, so the next save retries
            // them and anything asking what is still unsaved is told the truth. No timer is
            // started for them, since retrying a failure on a clock of its own would hammer
            // a server that is already in trouble.
            Ode.getInstance().getEditorManager().settingsSaveFailed(ProjectSettings.this);
            super.onFailure(caught);
            if (command != null) {
              command.execute();
            }
          }
        });
  }
}
