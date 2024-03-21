// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.gwt.user.client.Command;

public class ProjectPropertiesAction implements Command {
  @Override
  public void execute() {
    YaProjectEditor projectEditor = (YaProjectEditor)Ode.getInstance().getEditorManager()
        .getOpenProjectEditor(Ode.getInstance().getCurrentYoungAndroidProjectId());
    projectEditor.openProjectPropertyDialog();
  }
}
