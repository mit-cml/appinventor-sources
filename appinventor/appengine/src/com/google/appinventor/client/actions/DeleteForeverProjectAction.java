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

public class DeleteForeverProjectAction implements Command {
  @Override
  public void execute() {
    Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
      @Override
      public void execute() {
        if (Ode.getInstance().getCurrentView() == Ode.TRASHCAN) {
          List<Project> selectedProjects = ProjectListBox.getProjectListBox().getProjectList()
              .getSelectedProjects();
          List<ProjectFolder> selectedFolders = ProjectListBox.getProjectListBox().getProjectList()
              .getSelectedFolders();
          if (!selectedProjects.isEmpty() || !selectedFolders.isEmpty()) {
            List<Project> projectsToDelete = new ArrayList<>(selectedProjects);
            List<ProjectFolder> foldersToDelete = new ArrayList<>(selectedFolders);
            for (ProjectFolder f : selectedFolders) {
              projectsToDelete.addAll(f.getNestedProjects());
              foldersToDelete.addAll(f.getNestedFolders());
            }
            // Show one confirmation window for selected projects.
            if (DeleteAction.deleteConfirmation(false, projectsToDelete, foldersToDelete)) {
              for (Project project : selectedProjects) {
                project.deleteFromTrash();
              }
              for (ProjectFolder folder : selectedFolders) {
                folder.deleteFromTrash();
              }
            }
            Ode.getInstance().getFolderManager().saveAllFolders();
            Ode.getInstance().switchToTrash();
          } else {
            // The user can select a project to resolve the
            // error.
            ErrorReporter.reportInfo(MESSAGES.noProjectSelectedForDelete());
          }
        }
      }
    });
  }

  private boolean deleteConfirmation(List<Project> projects) {
    String message;
    if (projects.size() == 1) {
      message = MESSAGES.confirmDeleteSingleProject(projects.get(0).getProjectName());
    } else {
      StringBuilder sb = new StringBuilder();
      String separator = "";
      for (Project project : projects) {
        sb.append(separator).append(project.getProjectName());
        separator = ", ";
      }
      String projectNames = sb.toString();
      message = MESSAGES.confirmDeleteManyProjects(projectNames);
    }
    return Window.confirm(message);
  }
}

