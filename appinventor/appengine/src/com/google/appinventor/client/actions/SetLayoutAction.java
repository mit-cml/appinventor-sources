// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

// Note: If we implement more than one alternate layout, the parameter
// can be changed from a boolean to a layout identifier
public class SetLayoutAction implements Command {
  private boolean isNewLayout = false;

  public void setIsNewLayout(boolean isNew) {
    isNewLayout = isNew;
  }

  @Override
  public void execute() {
    Ode.setUserNewLayout(isNewLayout);
    Window.Location.reload();
    // Not: See above comment
  }
}
