// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.user.client.Command;

import static com.google.appinventor.client.Ode.MESSAGES;

public class EnableAutoloadAction implements Command {
  @Override
  public void execute() {
    Ode.getUserSettings().getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
        .changePropertyValue(SettingsConstants.USER_AUTOLOAD_PROJECT, Boolean.toString(true));
    Ode.getUserSettings().saveSettings(new Command() {
      @Override
      public void execute() {
        DropDownButton settings = Ode.getInstance().getTopToolbar().getSettingsDropDown();
        settings.setCommandById("AutoloadLastProject", new DisableAutoloadAction());
        settings.setItemHtmlById("AutoloadLastProject", MESSAGES.disableAutoload());
      }
    });
  }
}
