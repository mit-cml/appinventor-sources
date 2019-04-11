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
  private boolean loaded;

  /**
   * Creates new user settings object.
   */
  public UserSettings(UserInfoProvider user) {
    addSettings(SettingsConstants.USER_GENERAL_SETTINGS, new GeneralSettings(user));
    addSettings(SettingsConstants.USER_YOUNG_ANDROID_SETTINGS, new YoungAndroidSettings(user));
    addSettings(SettingsConstants.SPLASH_SETTINGS, new SplashSettings(user));
    addSettings(SettingsConstants.BLOCKS_SETTINGS, new BlocksSettings(user));
  }

  // SettingsAccessProvider implementation

  @Override
  public void loadSettings() {
    loadSettings(null);
  }

  public void loadSettings(final Command next) {
    loading = true;
    Ode.getInstance().getUserInfoService().loadUserSettings(
        new OdeAsyncCallback<String>(MESSAGES.settingsLoadError()) {
          @Override
          public void onSuccess(String result) {
            OdeLog.log("Loaded global settings: " + result);
            decodeSettings(result);

            changed = false;
            loaded = true;
            loading = false;

            if (Ode.handleUserLocale() && next != null) {
              next.execute();
            }
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
    if (Ode.getInstance().isReadOnly()) {
      return;                   // Don't save when in read-only mode
    }
    if (loading) {
      // If we are in the process of loading, we must defer saving.
      DeferredCommand.addCommand(new Command() {
        @Override
        public void execute() {
          saveSettings(command);
        }
      });
    } else if (!loaded) {
      // Do not save settings that have not been loaded. We should
      // only wind up in this state if we are in the early phases of
      // loading the App Inventor client code. If saveSettings is
      // called in this state, it is from the onWindowClosing
      // handler. We do *not* want to over-write a persons valid
      // settings with this empty version, so we just return.
      return;

    } else if (!changed) {
      // Do not save UserSettings if they haven't changed.
      return;
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
              changed = false;
              if (command != null) {
                command.execute();
              }
            }
          });
    }
  }
}
