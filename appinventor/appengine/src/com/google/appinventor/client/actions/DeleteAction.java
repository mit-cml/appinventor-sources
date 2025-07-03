// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import java.util.ArrayList;
import java.util.List;

public class DeleteAction implements Command {
  @Override
  public void execute() {
    Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
      @Override
      public void execute() {
        if (Ode.getInstance().getCurrentView() == Ode.PROJECTS) {
          List<Project> selectedProjects =
              ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
          List<ProjectFolder> selectedFolders = ProjectListBox.getProjectListBox().getProjectList().getSelectedFolders();
          if (selectedProjects.size() > 0 || selectedFolders.size() > 0) {
            List<Project> projectsToDelete = selectedProjects;
            for (ProjectFolder f : selectedFolders) {
              projectsToDelete.addAll(f.getNestedProjects());
            }
            // Show one confirmation window for selected projects.
            if (deleteConfirmation(projectsToDelete)) {
              for (Project project : projectsToDelete) {
                project.moveToTrash();
              }
              for (ProjectFolder f : selectedFolders) {
                f.getParentFolder().removeChildFolder(f);
              }
            }
          } else {
            // The user can select a project to resolve the
            // error.
            ErrorReporter.reportInfo(MESSAGES.noProjectSelectedForDelete());
          }
        } else { //We are deleting a project in the designer view
          List<Project> selectedProjects = new ArrayList<Project>();
          Project currentProject = Ode.getInstance().getProjectManager().getProject(Ode.getInstance().getCurrentYoungAndroidProjectId());
          selectedProjects.add(currentProject);
          if (deleteConfirmation(selectedProjects)) {
            currentProject.moveToTrash();
            //Add the command to stop this current project from saving
          }
        }
        Ode.getInstance().switchToProjectsView();
      }
    });
  }


  private boolean deleteConfirmation(List<Project> projects) {
    String message;
    if (projects.size() == 1) {
      message = MESSAGES.confirmMoveToTrashSingleProject(projects.get(0).getProjectName());
    } else if (projects.size() < 10) {
      StringBuilder sb = new StringBuilder();
      String separator = "";
      for (Project project : projects) {
        sb.append(separator).append(project.getProjectName());
        separator = ", ";
      }
      String projectNames = sb.toString();
      message = MESSAGES.confirmMoveToTrash(projectNames);
    } else {
      message = MESSAGES.confirmMoveToTrashCount(Integer.toString(projects.size()));
    }
    return Window.confirm(message);
  }
}
