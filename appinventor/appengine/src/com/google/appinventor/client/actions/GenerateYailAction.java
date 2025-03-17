// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.GenerateYailCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.user.client.Command;

/**
 *  Made changes to the now Projects menu made name changes to the menu items
 * Implements the action to generate the ".yail" file for each screen in the current project.
 * It does not build the entire project. The intention is that this will be helpful for
 * debugging during development, and will most likely be disabled in the production system.
 */
public class GenerateYailAction implements Command {
  @Override
  public void execute() {
    ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
    if (projectRootNode != null) {
      String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
      ChainableCommand cmd = new SaveAllEditorsCommand(new GenerateYailCommand(null));
      //updateBuildButton(true);
      cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_YAIL_YA, projectRootNode,
          new Command() {
            @Override
            public void execute() {
              //updateBuildButton(false);
            }
          });
    }
  }
}
