// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Wizard for specifying userid and projectid to download a user's source 
 * (available for admins only)
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public final class DownloadUserSourceWizard extends Wizard {
  // UI elements for user id and project id
  private LabeledTextBox userIdTextBox;
  private LabeledTextBox projectIdTextBox;

  public DownloadUserSourceWizard() {
    super(MESSAGES.downloadUserSourceDialogTitle(), true, false);

    // Initialize the UI
    setStylePrimaryName("ode-DialogBox");
  
    userIdTextBox = new LabeledTextBox(MESSAGES.userIdLabel());
    userIdTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_ENTER) {
          handleOkClick();
        } else if (keyCode == KeyCodes.KEY_ESCAPE) {
          handleCancelClick();
        }
      }
    });
    projectIdTextBox = new LabeledTextBox(MESSAGES.projectIdLabel());
    projectIdTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_ENTER) {
          handleOkClick();
        } else if (keyCode == KeyCodes.KEY_ESCAPE) {
          handleCancelClick();
        }
      }
    });

    VerticalPanel page = new VerticalPanel();
  
    page.add(userIdTextBox);
    page.add(projectIdTextBox);
    addPage(page);
  
    // Create finish command (do the download)
    initFinishCommand(new Command() {
      @Override
      public void execute() {
        String projectId = projectIdTextBox.getText();
        String userId = userIdTextBox.getText();
        if (!projectId.isEmpty() && !userId.isEmpty()) {
          Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
              ServerLayout.DOWNLOAD_USER_PROJECT_SOURCE + "/" + projectId +
              "/" + userId);
        } else {
          Window.alert(MESSAGES.invalidUserIdOrProjectIdError());
          new DownloadUserSourceWizard().center();
          return;
        }
      }
    });
  }
  
  @Override
  public void show() {
    super.show();
    this.center();
  }
}
