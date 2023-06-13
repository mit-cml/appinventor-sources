// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.settings;

import com.google.appinventor.shared.properties.json.JSONUtil;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.client.settings.user.UserSettings;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.gwt.junit.client.GWTTestCase;

public class GeneralSettingsTestCase extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient";
  }    
  public void testUserDyslexicFontTrue() {
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);

    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      changePropertyValue(SettingsConstants.USER_DYSLEXIC_FONT,
        "" + true);

    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      getPropertyValue(SettingsConstants.USER_DYSLEXIC_FONT);
    assertTrue(Boolean.parseBoolean(value));
  }

  
  public void testSetUserDyslexicFont() {
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);

    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      changePropertyValue(SettingsConstants.USER_DYSLEXIC_FONT,
                          "" + false);
    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      getPropertyValue(SettingsConstants.USER_DYSLEXIC_FONT);
    assertFalse(Boolean.parseBoolean(value));
  }

  public void testUserAutoloadProjectTrue() {
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);

    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      changePropertyValue(SettingsConstants.USER_AUTOLOAD_PROJECT,
                          "" + true);

    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      getPropertyValue(SettingsConstants.USER_AUTOLOAD_PROJECT);
    assertTrue(Boolean.parseBoolean(value));
  }

  
  public void testSetUserAutoloadProject() {
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);

    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      changePropertyValue(SettingsConstants.USER_AUTOLOAD_PROJECT,
                          "" + false);
    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      getPropertyValue(SettingsConstants.USER_AUTOLOAD_PROJECT);
    assertFalse(Boolean.parseBoolean(value));
  }

  public void testCurrentProjectId() {
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);
    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      changePropertyValue(SettingsConstants.GENERAL_SETTINGS_CURRENT_PROJECT_ID,
          "" + 1);
    final String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
        .getPropertyValue(SettingsConstants.GENERAL_SETTINGS_CURRENT_PROJECT_ID);
    assertEquals(value, "1");
  }

  public void testUserTemplateURLSetting() {
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings settings = new UserSettings(user);
    String string = "{'name': 'HelloPurr', 'subtitle': 'A purring kitty app', 'description':'<p>This is App Inventor version of the HelloWorld app. ...', 'screenshot':'screenshot.png',  'thumbnail':'thumbnail.png'}";
    settings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      changePropertyValue(SettingsConstants.USER_TEMPLATE_URLS,
        string);
      String userTemplates = settings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
        .getPropertyValue(SettingsConstants.USER_TEMPLATE_URLS);
      assertEquals(string, userTemplates);
  }
}
