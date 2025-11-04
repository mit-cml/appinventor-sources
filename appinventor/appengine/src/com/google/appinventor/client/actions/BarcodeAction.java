// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.shared.storage.StorageUtil.APPSTORE_CREDENTIALS_FILENAME;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.commands.BuildCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.GenerateYailCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.commands.ShowBarcodeCommand;
import com.google.appinventor.client.explorer.commands.ShowProgressBarCommand;
import com.google.appinventor.client.explorer.commands.WaitForBuildResultCommand;
import com.google.appinventor.client.explorer.commands.WarningDialogCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.wizards.AppStoreSettingsDialog;
import com.google.appinventor.client.wizards.ErrorDialog;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.user.client.Command;

public class BarcodeAction implements Command {

  private boolean secondBuildserver = false;
  private boolean isAab = false;
  private boolean foriOS = false;
  private boolean forAppStore = false;

  public BarcodeAction() {
    this(false);
  }

  public BarcodeAction(boolean secondBuildserver) {
    this(secondBuildserver, false, false, false);
  }

  public BarcodeAction(boolean secondBuildserver, boolean isAab) {
    this(secondBuildserver, isAab, false, false);
  }

  public BarcodeAction(boolean secondBuildserver, boolean isAab, boolean foriOS, boolean forAppStore) {
    this.secondBuildserver = secondBuildserver;
    this.isAab = isAab;
    this.foriOS = foriOS;
    this.forAppStore = forAppStore;
  }

  public void setSecond(boolean second) {
    secondBuildserver = second;
  }

  public void setIsAab(boolean isAab_p) {
    isAab = isAab_p;
  }

  public void setForiOS(boolean isForIos) {
    this.foriOS = isForIos;
  }

  public void setForAppStore(boolean forAppStore) {
    this.forAppStore = forAppStore;
  }

  @Override
  public void execute() {
    ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
    if (projectRootNode != null) {
      if (foriOS && !hasProvisioningProfile(projectRootNode)) {
        new ErrorDialog(MESSAGES.iosBuildError(), MESSAGES.provisioningProfileNeeded())
            .show();
        return;
      }
      String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
      ChainableCommand cmd = new SaveAllEditorsCommand(
          new GenerateYailCommand(
              new BuildCommand(target, secondBuildserver, isAab, foriOS, forAppStore,
                  new ShowProgressBarCommand(target,
                      new WaitForBuildResultCommand(target,
                          new ShowBarcodeCommand(target, getFormat())), "BarcodeAction"))));
      if (!Ode.getInstance().getWarnBuild(secondBuildserver)) {
        cmd = new WarningDialogCommand(target, secondBuildserver, cmd);
        Ode.getInstance().setWarnBuild(secondBuildserver, true);
      }
      final ChainableCommand finalCmd = cmd;
      Command next = new Command() {
        @Override
        public void execute() {
          finalCmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_BARCODE_YA, projectRootNode,
              new Command() {
                @Override
                public void execute() {
                }
              });
        }
      };
      if (forAppStore) {
        Ode.getInstance().getUserInfoService().hasUserFile(APPSTORE_CREDENTIALS_FILENAME,
            new OdeAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                if (result) {
                  next.execute();
                } else {
                  new AppStoreSettingsDialog(next).show();
                }
              }
            });
      } else {
        next.execute();
      }
    }
  }

  private ShowBarcodeCommand.BuildFormat getFormat() {
    if (foriOS && forAppStore) {
      return ShowBarcodeCommand.BuildFormat.ASC;
    } else if (foriOS) {
      return ShowBarcodeCommand.BuildFormat.IPA;
    } else if (isAab) {
      return ShowBarcodeCommand.BuildFormat.AAB;
    } else {
      return ShowBarcodeCommand.BuildFormat.APK;
    }
  }

  private static boolean hasProvisioningProfile(ProjectRootNode root) {
    YoungAndroidProjectNode node = (YoungAndroidProjectNode) root;
    for (ProjectNode asset : node.getAssetsFolder().getChildren()) {
      if (asset.getName().endsWith(".mobileprovision")) {
        return true;
      }
    }
    return false;
  }
}
