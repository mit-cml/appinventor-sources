// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class SignOutAction implements Command {
  private static final String SIGNOUT_URL = "/ode/_logout";

  @Override
  public void execute() {
    // Maybe take a screenshot
    Ode.getInstance().screenShotMaybe(new Runnable() {
      @Override
      public void run() {
        Window.Location.replace(SIGNOUT_URL);
      }
    }, true);               // Wait for i/o
  }
}
