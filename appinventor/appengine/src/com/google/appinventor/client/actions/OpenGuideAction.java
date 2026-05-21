// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.TopPanel;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import static com.google.appinventor.client.Ode.getSystemConfig;

public class OpenGuideAction implements Command {
  @Override
  public void execute() {
    Config config = getSystemConfig();
    if (config.getGuideUrl() != null) {
      Window.open(config.getGuideUrl(), TopPanel.WINDOW_OPEN_LOCATION, TopPanel.WINDOW_OPEN_FEATURES);
    }
  }
}
