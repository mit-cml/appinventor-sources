// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.explorer.commands.AddFormCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.user.client.Command;

public class AddFormAction implements Command {
  @Override
  public void execute() {
    Ode ode = Ode.getInstance();
    if (ode.screensLocked()) {
      return;                 // Don't permit this if we are locked out (saving files)
    }
    final ProjectRootNode projectRootNode = ode.getCurrentYoungAndroidProjectRootNode();
    if (projectRootNode != null) {
      Runnable doSwitch = new Runnable() {
        @Override
        public void run() {
          ChainableCommand cmd = new AddFormCommand();
          cmd.startExecuteChain(Tracking.PROJECT_ACTION_ADDFORM_YA, projectRootNode);
        }
      };
      // take a screenshot of the current blocks if we are in the blocks editor
      if (ode.getDesignToolbar().currentView == DesignToolbar.View.BLOCKS) {
        Ode.getInstance().screenShotMaybe(doSwitch, false);
      } else {
        doSwitch.run();
      }
    }
  }
}
