package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.BuildCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.DownloadProjectOutputCommand;
import com.google.appinventor.client.explorer.commands.GenerateYailCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.commands.ShowProgressBarCommand;
import com.google.appinventor.client.explorer.commands.WaitForBuildResultCommand;
import com.google.appinventor.client.explorer.commands.WarningDialogCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.user.client.Command;

public class DownloadAction implements Command {

  private boolean secondBuildserver = false;

  public DownloadAction() {
    this(false);
  }

  DownloadAction(boolean secondBuildserver) {
    this.secondBuildserver = secondBuildserver;
  }

  public void setSecond(boolean second) {
    secondBuildserver = second;
  }

  @Override
  public void execute() {
    ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
    if (projectRootNode != null) {
      String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
      // TODO: Implement aab boolean
      ChainableCommand cmd = new SaveAllEditorsCommand(
          new GenerateYailCommand(
              new BuildCommand(target, secondBuildserver, false)));
      if (!Ode.getInstance().getWarnBuild(secondBuildserver)) {
        cmd = new WarningDialogCommand(target, secondBuildserver, cmd);
        Ode.getInstance().setWarnBuild(secondBuildserver, true);
      }
      cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_DOWNLOAD_YA, projectRootNode,
          new Command() {
            @Override
            public void execute() {
            }
          });
    }
  }
}
