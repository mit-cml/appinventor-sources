// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class DonateAction implements Command {
  private static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  private static final String WINDOW_OPEN_LOCATION = "_blank";

  @Override
  public void execute() {
    Window.open("https://giving.appinventor.mit.edu", WINDOW_OPEN_LOCATION, WINDOW_OPEN_FEATURES);
  }
}
