// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class SetFontDyslexicAction implements Command {
  @Override
  public void execute() {
    Ode.setUserDyslexicFont(true);
    // Window.Location.reload();
    // Note: We used to reload here, but this causes
    // a race condition with the saving of the user
    // settings. So we now reload in the callback to
    // saveSettings (in Ode.java)
  }
}
