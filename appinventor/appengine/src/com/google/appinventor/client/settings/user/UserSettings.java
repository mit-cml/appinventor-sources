// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.settings.user;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.settings.CommonSettings;
import com.google.appinventor.client.settings.SettingsAccessProvider;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

/**
 * Collection of user settings.
 *
 */
public final class UserSettings extends CommonSettings implements SettingsAccessProvider {
  private boolean loading;

  /**
   * Creates new user settings object.
   */
  public UserSettings(UserInfoProvider user) {
    addSettings(SettingsConstants.USER_GENERAL_SETTINGS, new GeneralSettings(user));
    addSettings(SettingsConstants.USER_YOUNG_ANDROID_SETTINGS, new YoungAndroidSettings(user));
    addSettings(SettingsConstants.SPLASH_SETTINGS, new SplashSettings(user));
  }

  // SettingsAccessProvider implementation

  @Override
  public void loadSettings() {
    loading = true;
    Ode.getInstance().getUserInfoService().loadUserSettings(
        new OdeAsyncCallback<String>(MESSAGES.settingsLoadError()) {
          @Override
          public void onSuccess(String result) {
            OdeLog.log("Loaded global settings: " + result);
            decodeSettings(result);

            loading = false;
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            loading = false;
          }
        });
  }

  @Override
  public void saveSettings(final Command command) {
    if (loading) {
      // If we are in the process of loading, we must defer saving.
      DeferredCommand.addCommand(new Command() {
        @Override
        public void execute() {
          saveSettings(command);
        }
      });
    } else {
      String s = encodeSettings();
      OdeLog.log("Saving global settings: " + s);
      Ode.getInstance().getUserInfoService().storeUserSettings(
          s,
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
}
