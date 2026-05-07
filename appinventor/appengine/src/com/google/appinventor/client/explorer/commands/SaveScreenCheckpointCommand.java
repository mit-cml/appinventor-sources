// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.UserProject;
import java.util.List;

/**
 * A command that brings up a wizard to copy a Young Android project.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class SaveScreenCheckpointCommand extends ChainableCommand {
  private final boolean checkpoint;

  /**
   * Creates a new command for copying a project.
   *
   * @param checkpoint whether the copy is a checkpoint
   */
  public SaveScreenCheckpointCommand(boolean checkpoint, ChainableCommand next) {
    super(next);
    this.checkpoint = checkpoint;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    handleOkClick(node.getProjectRoot());
  }

  private void handleOkClick(ProjectRootNode oldProjectNode) {
    String oldName = oldProjectNode.getName() + "_Screen";
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

  protected void copyProjectAction(final ProjectRootNode oldProjectNode, String newName) {
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
        executeNextCommand(oldProjectNode);
      }
    };
    // Create new copy on the backend
    ode.getProjectService().copyProject(oldProjectNode.getProjectId(), newName, callback);
  }
}
