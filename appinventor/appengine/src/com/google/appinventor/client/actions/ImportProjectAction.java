// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.wizards.ProjectUploadWizard;
import com.google.gwt.user.client.Command;

public class ImportProjectAction implements Command {
  @Override
  public void execute() {
    new ProjectUploadWizard().show();
  }
}
