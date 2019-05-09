// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.explorer.commands;

import java.util.Date;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.dialogs.ProgressBarDialogBox;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;

/**
 * Command for displaying a barcode for the target of a project.
 *
 * <p/>This command is often chained with SaveAllEditorsCommand and BuildCommand.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class ShowProgressBarCommand extends ChainableCommand {
  private static final String DOWNLOAD_ACTION = "DownloadAction";
  private static final String DOWNLOAD_TO_PHONE_ACTION = "DownloadToPhoneAction";

  // The build target
  private int counter = 0;
  private int currentProgress = 0;
  // 0 means just initialize, 1 means click once, 2 means click twice
  private int progressBarShow = 0;
  private String target;
  private ChainableCommand nextCommand;
  private final String buildRequestTime;
  private static final int WAIT_INTERVAL_MILLIS = 5000;
  private ProjectNode projectNode;
  private ProgressBarDialogBox minPB;
  private String serviceName;

  /**
   * Creates a new command for showing a barcode for the target of a project.
   *
   * @param target the build target
   * @param nextCommand
   * @param serviceName
   */
  public ShowProgressBarCommand(String target, ChainableCommand nextCommand, String serviceName) {
    // Since we don't know when the barcode dialog is finished, we can't
    // support a command after this one.
    super(nextCommand); // no next command
    this.target = target;
    this.nextCommand = nextCommand;
    this.buildRequestTime = DateTimeFormat.getMediumDateTimeFormat().format(new Date());
    this.serviceName = serviceName;
  }

  @Override
    public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  //the main function to be called
    public void execute(final ProjectNode node) {
    final Ode ode = Ode.getInstance();
    if (counter<1) {
      projectNode = node;
      minPB = new ProgressBarDialogBox(serviceName, node);
      minPB.center();
      executeNextCommand(node);
    }
    counter++;
    //call back function - dynamic DialogBox
    OdeAsyncCallback<RpcResult> callback = new OdeAsyncCallback<RpcResult>(MESSAGES.buildError())  // failure message
      {
      @Override
      public void onSuccess(RpcResult result) {
        addMessages(node.getName(),result);
        if (result.succeeded()) {
            minPB.hide();
        } else if (progressBarShow != 2 ) {
          // Build isn't done yet
          Timer timer = new Timer() {
              @Override
                public void run() {
                execute(node); }
            };
          // TODO(user): Maybe do an exponential backoff here.
          timer.schedule(WAIT_INTERVAL_MILLIS);
        }
      }
      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        executionFailedOrCanceled();}
    };
    ode.getProjectService().getBuildResult(node.getProjectId(), target, callback);
  }

  public void addMessages(String projectName, RpcResult result) {
    String labelContent;
    int currentProgress = 0;
    if (result.succeeded()) {
      minPB.show();
      currentProgress = 100;
      if (DOWNLOAD_ACTION.equals(serviceName)) {
        labelContent = "<br />" + MESSAGES.apkSavedToComputer();
      } else if (DOWNLOAD_TO_PHONE_ACTION.equals(serviceName)) {
        labelContent = "<br />" + MESSAGES.apkInstalledToPhone();
      } else {
        labelContent = "<br />" + MESSAGES.waitingForBarcode();
      }
    } else {
      try {
        currentProgress = Math.max(currentProgress,
            Integer.parseInt(result.getOutput()));
        if (currentProgress <= 10) {
          labelContent = "<br />" + MESSAGES.preparingApplicationIcon();
        } else if (currentProgress < 15) {
          labelContent = "<br />" + MESSAGES.determiningPermissions();
        } else if (currentProgress < 20) {
          labelContent = "<br />" + MESSAGES.generatingApplicationInformation();
        } else if (currentProgress < 35) {
          labelContent = "<br />" + MESSAGES.compilingPart1();
        } else if (currentProgress < 85) {
          labelContent = "<br />" + MESSAGES.compilingPart2();
        } else if (currentProgress < 90) {
          labelContent = "<br />" + MESSAGES.preparingFinalPackage();
        } else if (currentProgress <= 95) {
          labelContent = "<br />" + MESSAGES.buildingApk();
        } else {
          if (DOWNLOAD_ACTION.equals(serviceName)) {
            labelContent = "<br />" + MESSAGES.apkSavedToComputer();
          } else if (DOWNLOAD_TO_PHONE_ACTION.equals(serviceName)) {
            labelContent = "<br />" + MESSAGES.apkInstalledToPhone();
          } else {
            labelContent = "<br />" + MESSAGES.waitingForBarcode();
          }
        }
      } catch (NumberFormatException e) {
        // If the result is an error message, then the number parse will fail,
        // so we pick up the case of a compilation failure here.
        currentProgress = 0;
        // show the dismiss button to dismiss error
        minPB.showDismissButton();
        labelContent = MESSAGES.unableToCompile(result.getOutput());
      }
    }
    minPB.setProgress(currentProgress, labelContent);
  }

}
