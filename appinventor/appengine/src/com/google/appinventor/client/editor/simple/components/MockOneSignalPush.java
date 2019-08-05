// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.user.client.ui.Image;

public class MockOneSignalPush extends MockNonVisibleComponent {

  public static final String TYPE = "OneSignalPush";

  private static boolean warningGiven = false; // Whether or not we have given experimental warning

  private boolean persistToken = false;

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   */
  public MockOneSignalPush(SimpleEditor editor) {
    super(editor, TYPE, new Image(images.onesignal()));
    OdeLog.log("MockOneSignal ");
  }


  @Override
    public void onPropertyChange(String propertyName, String newValue){
        super.onPropertyChange(propertyName, newValue);
    if (propertyName.equals("AppId")) {
      setAppId(newValue);
    }

        // Force to refresh/repaint the form
        refreshForm();

  }

  public void setAppId(String text) {
    editor.getProjectEditor().changeProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_ONE_SIGNAL_APP_ID, text);
  }
}
