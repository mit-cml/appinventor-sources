// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.gwt.user.client.Command;
import com.google.appinventor.client.wizards.UISettingsWizard;

public class UISettingsAction implements Command {
  @Override
  public void execute() {
    new UISettingsWizard().show();
  }
}
