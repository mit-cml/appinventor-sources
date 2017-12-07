// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.client.widgets.Validator;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Rename a project
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author 502470184@qq.com (ColinTree Yang)
 */
public final class RenameYoungAndroidProjectCommand extends ChainableCommand {

  /**
   * Creates a new command for renaming a project.
   */
  public RenameYoungAndroidProjectCommand() {
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return false;  // Is there a way to do this for this command?
  }

  @Override
  public void execute(final ProjectNode node) {
    new NewProjectNameDialog(node.getProjectRoot()).center();
  }

  /**
   * Dialog for getting the new name for saving.
   */
  private class NewProjectNameDialog extends DialogBox {

    // UI elements
    private final LabeledTextBox newNameTextBox;

    /**
     * Creates a new dialog to get new project name.
     *
     * @param projectNode  project to rename
     */
    public NewProjectNameDialog(final ProjectRootNode projectNode) {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");

      String oldName = projectNode.getName();
      setText(MESSAGES.renameProjectTitle(oldName));

      VerticalPanel contentPanel = new VerticalPanel();
      contentPanel.setSpacing(10);

      newNameTextBox = new LabeledTextBox(MESSAGES.newNameLabel(), new Validator(){
        @Override
        public boolean validate(String value) {
          errorMessage = TextValidators.getErrorMessage(value);
          return !(errorMessage.length()>0);
        }

        @Override
        public String getErrorMessage() {
          return errorMessage;
        }
      });
      newNameTextBox.setText(oldName);
      newNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          int keyCode = event.getNativeKeyCode();
          if (keyCode == KeyCodes.KEY_ENTER) {
            handleOkClick(projectNode);
          } else if (keyCode == KeyCodes.KEY_ESCAPE) {
            hide();
          } else {
            newNameTextBox.validate();
          }
        }
      });
      contentPanel.add(newNameTextBox);

      HorizontalPanel buttonPanel = new HorizontalPanel();
      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
        }
      });
      buttonPanel.add(cancelButton);
      Button okButton = new Button(MESSAGES.okButton());
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleOkClick(projectNode);
        }
      });
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);

      contentPanel.setSize("320px", "100%");

      add(contentPanel);
    }

    private void handleOkClick(ProjectRootNode projectNode) {
      String newProjectName = newNameTextBox.getText();
      if (TextValidators.checkNewProjectName(newProjectName)) {
        hide();
        renameProjectAction(projectNode, newProjectName);
      } else {
        newNameTextBox.setFocus(true);
        newNameTextBox.selectAll();
      }
    }

    /**
     * Rename a project and gives it a new name.
     *
     * @param newName the new project name
     */
    protected void renameProjectAction(ProjectRootNode projectNode, String newName) {
      final Ode ode = Ode.getInstance();
      final long projectId = projectNode.getProjectId();

      OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(
          // failure message
          MESSAGES.renameProjectError()) {
        @Override
        public void onSuccess(UserProject newProjectInfo) {
          // Update project list
          Project newProject = ode.getProjectManager().addProject(newProjectInfo);
          ode.getProjectManager().removeProject(projectId);
          ode.openYoungAndroidProjectInDesigner(newProject);
        }
      };

      ode.getProjectService().renameProject(projectId, newName, callback);
    }

    @Override
    public void show() {
      super.show();

      DeferredCommand.addCommand(new Command() {
        @Override
        public void execute() {
          newNameTextBox.setFocus(true);
          newNameTextBox.selectAll();
        }
      });
    }
  }
}
