// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.UserProject;
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
 * A command that brings up a wizard to copy a Young Android project.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class CopyYoungAndroidProjectCommand extends ChainableCommand {
  private boolean checkpoint;

  /**
   * Creates a new command for copying a project.
   *
   * @param checkpoint whether the copy is a checkpoint
   */
  public CopyYoungAndroidProjectCommand(boolean checkpoint) {
    this.checkpoint = checkpoint;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return false;  // Is there a way to do this for this command?
  }

  @Override
  public void execute(final ProjectNode node) {
    new CopyProjectDialog(node.getProjectRoot()).center();
  }

  /**
   * Dialog for getting the new name for saving.
   */
  private class CopyProjectDialog extends DialogBox {

    // UI elements
    private final LabeledTextBox newNameTextBox;

    /**
     * Creates a new dialog to get new project name.
     *
     * @param oldProjectNode  old project to copy
     */
    public CopyProjectDialog(final ProjectRootNode oldProjectNode) {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");

      String oldName = oldProjectNode.getName();
      setText(checkpoint ? MESSAGES.checkpointTitle(oldName) : MESSAGES.saveAsTitle(oldName));

      VerticalPanel contentPanel = new VerticalPanel();
      contentPanel.setSpacing(10);

      String defaultNewName;

      if (checkpoint) {
        String prefix = MESSAGES.defaultCheckpointProjectName(oldName, "");
        List<Project> checkpointProjects =
            Ode.getInstance().getProjectManager().getProjects(prefix);

        String nextSuffix;

        if (checkpointProjects.isEmpty()) {
          nextSuffix = "1";
        } else {
          // Sort the checkpoints project by the date they were last modified, in descending order.
          Collections.sort(checkpointProjects, ProjectComparators.COMPARE_BY_DATE_MODIFIED_DESCENDING);

          VerticalPanel previousCheckpointsPanel = new VerticalPanel();
          previousCheckpointsPanel.setSpacing(0);
          previousCheckpointsPanel.add(new Label(MESSAGES.previousCheckpointsLabel()));
          Widget previousCheckpointsTable = createPreviousCheckpointsTable(checkpointProjects);
          previousCheckpointsTable.setSize("100%", "100%");
          ScrollPanel scrollPanel = new ScrollPanel(previousCheckpointsTable);
          scrollPanel.addStyleName("ode-CheckpointProjectTable");
          scrollPanel.setWidth("100%");
          if (checkpointProjects.size() > 5) {
            scrollPanel.setHeight("121px");
          }
          previousCheckpointsPanel.add(scrollPanel);
          previousCheckpointsPanel.setWidth("100%");
          contentPanel.add(previousCheckpointsPanel);

          // Find the highest number in the checkpoint projects' names.
          int highestNumber = 0;
          int prefixLength = prefix.length();
          for (Project checkpointProject : checkpointProjects) {
            String checkpointName = checkpointProject.getProjectName();
            try {
              highestNumber = Math.max(highestNumber,
                  Integer.parseInt(checkpointName.substring(prefixLength)));
            } catch (NumberFormatException e) {
              continue;
            }
          }
          nextSuffix = Integer.toString(highestNumber + 1);
        }

        defaultNewName = MESSAGES.defaultCheckpointProjectName(oldName, nextSuffix);

      } else {
        // Save As
        defaultNewName = MESSAGES.defaultSaveAsProjectName(oldName);
      }

      newNameTextBox = new LabeledTextBox(checkpoint ? MESSAGES.checkpointNameLabel()
          : MESSAGES.newNameLabel());
      newNameTextBox.setText(defaultNewName);
      newNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          int keyCode = event.getNativeKeyCode();
          if (keyCode == KeyCodes.KEY_ENTER) {
            handleOkClick(oldProjectNode);
          } else if (keyCode == KeyCodes.KEY_ESCAPE) {
            hide();
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
          handleOkClick(oldProjectNode);
        }
      });
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);

      contentPanel.setSize(checkpoint ? "400px" : "320px", "100%");

      add(contentPanel);
    }

    private void handleOkClick(ProjectRootNode oldProjectNode) {
      String newProjectName = newNameTextBox.getText();
      if (TextValidators.checkNewProjectName(newProjectName)) {
        hide();
        copyProjectAction(oldProjectNode, newProjectName);
      } else {
        newNameTextBox.setFocus(true);
        newNameTextBox.selectAll();
      }
    }

    private Widget createPreviousCheckpointsTable(List<Project> checkpointProjects) {
      Grid table = new Grid(1 + checkpointProjects.size(), 3);
      table.addStyleName("ode-ProjectTable");

      // Set the widgets for the header row.
      table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");
      table.setWidget(0, 0, new Label(MESSAGES.projectNameHeader()));
      table.setWidget(0, 1, new Label(MESSAGES.projectDateCreatedHeader()));
      table.setWidget(0, 2, new Label(MESSAGES.projectDateModifiedHeader()));

      // Set the widgets for the rows representing previous checkpoints
      DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();
      int row = 1;
      for (Project checkpointProject : checkpointProjects) {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
        Label nameLabel = new Label(checkpointProject.getProjectName());
        table.setWidget(row, 0, nameLabel);

        Date dateCreated = new Date(checkpointProject.getDateCreated());
        table.setWidget(row, 1, new Label(dateTimeFormat.format(dateCreated)));

        Date dateModified = new Date(checkpointProject.getDateModified());
        table.setWidget(row, 2, new Label(dateTimeFormat.format(dateModified)));
        row++;
      }

      return table;
    }

    /**
     * Copies a project and gives it a new name.
     *
     * @param newName the new project name
     */
    protected void copyProjectAction(ProjectRootNode oldProjectNode, String newName) {
      final Ode ode = Ode.getInstance();

      OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(
          // failure message
          MESSAGES.copyProjectError()) {
        @Override
        public void onSuccess(UserProject newProjectInfo) {
          // Update project list
          Project newProject = ode.getProjectManager().addProject(newProjectInfo);
          if (!checkpoint) {
            ode.openYoungAndroidProjectInDesigner(newProject);
          }
        }
      };

      // Create new copy on the backend
      ode.getProjectService().copyProject(oldProjectNode.getProjectId(), newName, callback);
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
