// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.explorer.commands;

import java.util.Date;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.appinventor.client.output.MessagesOutput;
import com.google.appinventor.client.explorer.commands.MiniProgressBar;

/**
 * Command for displaying a barcode for the target of a project.
 *
 * <p/>This command is often chained with SaveAllEditorsCommand and BuildCommand.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class ShowProgressBarCommand extends ChainableCommand {

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
  public ShowProgressBarCommand(String target,ChainableCommand nextCommand, String serviceName) {
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
      minPB = new ProgressBarDialogBox();
      minPB.center();
      executeNextCommand(node);
    }
    counter++;
    //call back function - dynamic DialogBox
    OdeAsyncCallback<RpcResult> callback = new OdeAsyncCallback<RpcResult>(MESSAGES.buildError())  // failure message
      {
      @Override
      public void onSuccess(RpcResult result) {
        minPB.addMessages(node.getName(),result);
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

  class ProgressBarDialogBox extends DialogBox{
    public ClickHandler buttonHandler;
    public Button dismissButton = new Button(MESSAGES.dismissButton());
    public HTML warningLabel;
    public VerticalPanel contentPanel;
    public HorizontalPanel buttonPanel = new HorizontalPanel();
    public HorizontalPanel warningPanel = new HorizontalPanel();
    public MiniProgressBar mpb = new MiniProgressBar(0,100,0);

    //constructor
    ProgressBarDialogBox() {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");
      setText(projectNode.getName() + " " + MESSAGES.ProgressBarFor());

      //click handler for the mini html buttons
      buttonHandler = new ClickHandler() {
          @Override
            public void onClick(ClickEvent event) {
               hide();
               progressBarShow++;
          }
       };

      //declare the ok button
      dismissButton.addClickHandler(buttonHandler);
      buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      dismissButton.setVisible(false); // we don't need the button unless we get an error

      //warning label
      warningLabel = new HTML("");
      warningLabel.setWordWrap(true);
      warningLabel.setWidth("60em");  // set width to get the text to wrap

      //warning panel
      warningPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
      warningPanel.add(warningLabel);

      // button panel
      buttonPanel.add(dismissButton);
      buttonPanel.setSize("100%", "24px");

      //content panel
      contentPanel = new VerticalPanel();
      contentPanel.add(mpb);
      contentPanel.add(warningPanel);
      contentPanel.add(buttonPanel);
      setWidget(contentPanel);
    }

    public void clear() {
      warningLabel.setHTML("");
    }

    public void addMessages(String projectName, RpcResult result) {
      if (result.succeeded()) {
        show();
        mpb.setProgress(100);
        if(serviceName == "DownloadAction") {
          warningLabel.setHTML("<br />The APK file will be saved in the download folder.");
        } else if (serviceName == "DownloadToPhoneAction"){
          warningLabel.setHTML("<br />The APK file will be installed in the phone.");
        } else {
          warningLabel.setHTML("<br />Waiting for the barcode.");
        }
      } else {
        try {
          currentProgress = Math.max(currentProgress,
                                     Integer.parseInt(result.getOutput()));
          mpb.setProgress(currentProgress);
          if (currentProgress <= 10) {
            warningLabel.setHTML("<br />Preparing application icon");
          } else if (currentProgress < 15) {
            warningLabel.setHTML("<br />Determining permissions");
          } else if (currentProgress < 20) {
            warningLabel.setHTML("<br />Generating application information");
          } else if (currentProgress < 35) {
            warningLabel.setHTML("<br />Compiling part 1");
          } else if (currentProgress < 85) {
            warningLabel.setHTML("<br />Compiling part 2 (please wait)");
          } else if (currentProgress < 90) {
            warningLabel.setHTML("<br />Preparing final package");
          } else if (currentProgress <= 95) {
            warningLabel.setHTML("<br />Building APK");
          } else {
            if(serviceName == "DownloadAction") {
              warningLabel.setHTML("<br />The APK file will be saved in your downloads folder.");
            } else if (serviceName == "DownloadToPhoneAction") {
              warningLabel.setHTML("<br />The APK file will be installed in the phone.");
            } else {
              warningLabel.setHTML("<br />Waiting for the barcode.");
            }
          }
        } catch (NumberFormatException e) {
          // If the result is an error message, then the number parse will fail,
          // so we pick up the case of a compilation failure here.
          mpb.setProgress(0);
          // show the dismiss button to dismiss error
          dismissButton.setVisible(true);
          warningLabel.setHTML(MESSAGES.unableToCompile(result.getOutput()));
        }
      }
    }
  }
}
