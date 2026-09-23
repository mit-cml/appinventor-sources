// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class BrowserAction implements Command {
  @Override
  public void execute() {
    if(Ode.getInstance().okToConnect()) {
      Ode.getInstance().getTopToolbar().startRepl(true, false, false, false, true);
    }
  }
}
