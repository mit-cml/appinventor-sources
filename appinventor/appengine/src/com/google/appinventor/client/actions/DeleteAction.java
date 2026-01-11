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
import java.util.Collections;
import java.util.List;

public class DeleteAction implements Command {
  @Override
  public void execute() {
    Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
      @Override
      public void execute() {
        if (Ode.getInstance().getCurrentView() == Ode.PROJECTS) {
          List<Project> selectedProjects = ProjectListBox.getProjectListBox().getProjectList()
              .getSelectedProjects();
          List<ProjectFolder> selectedFolders = ProjectListBox.getProjectListBox().getProjectList().getSelectedFolders();
          if (!selectedProjects.isEmpty() || !selectedFolders.isEmpty()) {
            List<Project> projectsToDelete = new ArrayList<>(selectedProjects);
            List<ProjectFolder> foldersToDelete = new ArrayList<>(selectedFolders);
            for (ProjectFolder f : selectedFolders) {
              projectsToDelete.addAll(f.getNestedProjects());
              foldersToDelete.addAll(f.getNestedFolders());
            }
            // Show one confirmation window for selected projects.
            if (deleteConfirmation(true, projectsToDelete, foldersToDelete)) {
              Ode.getInstance().getFolderManager().moveItemsToFolder(selectedProjects, selectedFolders,
                  Ode.getInstance().getFolderManager().getTrashFolder());
              Ode.getInstance().getFolderManager().saveAllFolders();
            }
          } else {
            // The user can select a project to resolve the
            // error.
            ErrorReporter.reportInfo(MESSAGES.noProjectSelectedForDelete());
          }
        } else { //We are deleting a project in the designer view
          List<Project> selectedProjects = new ArrayList<>();
          Project currentProject = Ode.getInstance().getProjectManager().getProject(Ode.getInstance().getCurrentYoungAndroidProjectId());
          selectedProjects.add(currentProject);
          if (deleteConfirmation(true, selectedProjects, Collections.emptyList())) {
            currentProject.moveToTrash();
            //Add the command to stop this current project from saving
          }
        }
        Ode.getInstance().switchToProjectsView();
      }
    });
  }

  public static boolean deleteConfirmation(boolean toTrash, List<Project> projects, List<ProjectFolder> folders) {
    String message = "";
    if (toTrash && projects.size() == 1 && folders.isEmpty()) {
      message = MESSAGES.confirmMoveToTrashSingleProject(projects.get(0).getProjectName());
    } else {
      String projMsg = "";
      String folderMsg = "";
      if (projects.isEmpty()) {
        projMsg = "";
      } else if (projects.size() < 10) {
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (Project project : projects) {
          sb.append(separator).append(project.getProjectName());
          separator = ", ";
        }
        String projectNames = sb.toString();
        projMsg = "\n  " +MESSAGES.confirmTrashDeleteProjects(projectNames);
      } else {
        projMsg = "\n  " +MESSAGES.confirmTrashDeleteFolders(Integer.toString(projects.size()));
      }
      if (folders.isEmpty()) {
        folderMsg = "";
      } else if (folders.size() < 10) {
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (ProjectFolder folder : folders) {
          sb.append(separator).append(folder.getName());
          separator = ", ";
        }
        String folderNames = sb.toString();
        folderMsg = "\n  " + MESSAGES.confirmTrashDeleteFolders(folderNames);
      } else {
        folderMsg = "\n  " + MESSAGES.confirmTrashDeleteFolders(Integer.toString(folders.size()));
      }
      if (toTrash) {
        message = MESSAGES.confirmMoveToTrash(projMsg + folderMsg);
      } else {
        message = MESSAGES.confirmDeleteForever(projMsg + folderMsg);
      }
    }
    return Window.confirm(message);
  }
}
