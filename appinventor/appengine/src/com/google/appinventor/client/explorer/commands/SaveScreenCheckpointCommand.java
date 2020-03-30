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
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
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
 * A command that brings up a wizard to copy a Young Android project.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class SaveScreenCheckpointCommand extends ChainableCommand {
  private boolean checkpoint;

  /**
   * Creates a new command for copying a project.
   *
   * @param checkpoint whether the copy is a checkpoint
   */
  public SaveScreenCheckpointCommand(boolean checkpoint) {
    this.checkpoint = checkpoint;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return false;  // Is there a way to do this for this command?
  }

  @Override
  public void execute(final ProjectNode node) {
    handleOkClick(node.getProjectRoot());        
  }
       
  private void handleOkClick(ProjectRootNode oldProjectNode) {
    String oldName = oldProjectNode.getName()+"_Screen";
    String defaultNewName;
    if (checkpoint) {
      String prefix = MESSAGES.defaultCheckpointProjectName(oldName, "");
      List<Project> checkpointProjects =
      Ode.getInstance().getProjectManager().getProjects(prefix);

      String nextSuffix;
      if (checkpointProjects.isEmpty()) {
        nextSuffix = "1";
      } else {
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
   copyProjectAction(oldProjectNode, defaultNewName);
  }
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
}
