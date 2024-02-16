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
import com.google.appinventor.client.explorer.project.Project;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import java.util.List;

public class DeleteForeverProjectAction implements Command {
  @Override
  public void execute() {
    Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
      @Override
      public void execute() {
        if (Ode.getInstance().getCurrentView() == Ode.TRASHCAN) {
          List<Project> deletedProjects = ProjectListBox.getProjectListBox().getProjectList()
                                              .getSelectedProjects();
          if (deletedProjects.size() > 0) {
            // Show one confirmation window for selected projects.
            if (deleteConfirmation(deletedProjects)) {
              for (Project project : deletedProjects) {
                project.deleteFromTrash();
              }
            }
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

