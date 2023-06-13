// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.SettingsConstants;

import com.google.appinventor.client.settings.user.UserSettings;
import com.google.gwt.user.client.Window;
import com.google.gwt.junit.client.GWTTestCase;

public class OdeTestCase extends GWTTestCase {
  public void testCompareLocales() {
    assertTrue("Handles default case one is null",
        Ode.compareLocales(null, "en", "en"));
    assertTrue("Handles both cases being null",
        Ode.compareLocales(null, null, "en"));
    assertTrue("Handles when both cases are the same",
        Ode.compareLocales("en", "en", "en"));
    assertFalse("Handles when the cases are different",
        Ode.compareLocales("en", "fr_FR", "en"));
    assertFalse("Handles when the default is different",
        Ode.compareLocales(null, "fr_FR", "en"));
  }

  public void testhandleUserLocale() {
    String locale = Window.Location.getParameter("locale");
    User user = new User("1","abc@email.com",true,true,null);
    UserSettings userSettings = new UserSettings(user);
    String lastUserLocale = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).getPropertyValue(SettingsConstants.USER_LAST_LOCALE);
    boolean value = Ode.compareLocales(locale, lastUserLocale, "en");
    assertTrue(value);
  }
  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient";
  }
}
