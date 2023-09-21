// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.BuildCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.GenerateYailCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.commands.ShowBarcodeCommand;
import com.google.appinventor.client.explorer.commands.ShowProgressBarCommand;
import com.google.appinventor.client.explorer.commands.WaitForBuildResultCommand;
import com.google.appinventor.client.explorer.commands.WarningDialogCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.user.client.Command;

public class BarcodeAction implements Command {

  private boolean secondBuildserver = false;
  private boolean isAab = false;

  public BarcodeAction() {
    this(false);
  }

  public BarcodeAction(boolean secondBuildserver) {
    this.secondBuildserver = secondBuildserver;
  }

  public BarcodeAction(boolean secondBuildserver, boolean isAab)
  {
    this.secondBuildserver = secondBuildserver;
    this.isAab = isAab;
  }

  public void setSecond(boolean second) {
    secondBuildserver = second;
  }
  public void setIsAab(boolean isAab_p) {
    isAab = isAab_p;
  }

  @Override
  public void execute() {
    ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
    if (projectRootNode != null) {
      String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
      ChainableCommand cmd = new SaveAllEditorsCommand(
          new GenerateYailCommand(
              new BuildCommand(target, secondBuildserver, isAab,
                  new ShowProgressBarCommand(target,
                      new WaitForBuildResultCommand(target,
                          new ShowBarcodeCommand(target, isAab)), "BarcodeAction"))));
      if (!Ode.getInstance().getWarnBuild(secondBuildserver)) {
        cmd = new WarningDialogCommand(target, secondBuildserver, cmd);
        Ode.getInstance().setWarnBuild(secondBuildserver, true);
      }
      cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_BARCODE_YA, projectRootNode,
          new Command() {
            @Override
            public void execute() {
            }
          });
    }
  }
}
