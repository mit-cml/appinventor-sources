// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.PermissionsPanel;
import com.google.gwt.user.client.Command;

/**
 * Command to show the project permissions panel.
 */
public class ProjectPermissionsAction implements Command {
  @Override
  public void execute() {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId != 0) {
      new PermissionsPanel(projectId).show();
    }
  }
}
