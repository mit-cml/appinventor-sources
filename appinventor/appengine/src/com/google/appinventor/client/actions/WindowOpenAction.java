// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class WindowOpenAction implements Command {
  private static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  private static final String WINDOW_OPEN_LOCATION = "_ai2";

  private final String url;

  public WindowOpenAction(String url) {
    this.url = url;
  }

  @Override
  public void execute() {
    Window.open(url, WINDOW_OPEN_LOCATION, WINDOW_OPEN_FEATURES);
  }
}
