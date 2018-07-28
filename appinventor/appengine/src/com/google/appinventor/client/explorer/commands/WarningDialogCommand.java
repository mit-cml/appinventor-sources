// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Command for displaying a barcode for the target of a project.
 *
 * This command is used to display a dialog that explains what each
 * build option does.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class WarningDialogCommand extends ChainableCommand {

  // The build target
  private String target;
  private boolean secondBuildserver;

  /**
   * Creates a new command for showing a barcode for the target of a project.
   *
   * @param target the build target
   */
  public WarningDialogCommand(String target, boolean secondBuildserver, ChainableCommand nextCommand) {
    super(nextCommand);
    this.target = target;
    this.secondBuildserver = secondBuildserver;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    new WarningDialogBox(secondBuildserver, node).center();
  }

  private class WarningDialogBox extends DialogBox {

    WarningDialogBox(final boolean secondBuildserver, final ProjectNode node) {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.NoticeTitle());

      ClickHandler okButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
          WarningDialogCommand.this.executeNextCommand(node);
        }
      };

      ClickHandler cancelButtonHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
          WarningDialogCommand.this.executionFailedOrCanceled();
        }
      };

      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addClickHandler(cancelButtonHandler);
      Button okButton = new Button(MESSAGES.okButton());
      okButton.addClickHandler(okButtonHandler);
      HTML message;
      if (secondBuildserver) {
        message = new HTML(MESSAGES.Package26Notice());
      } else {
        message = new HTML(MESSAGES.PackageNotice());
      }
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      VerticalPanel contentPanel = new VerticalPanel();
      contentPanel.add(message);
      contentPanel.add(buttonPanel);
//      contentPanel.setSize("320px", "100%");
      add(contentPanel);
    }
  }
}
