package com.google.appinventor.client.actions;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.gwt.user.client.Command;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

public class RestoreProjectAction implements Command {
  @Override
  public void execute() {
    List<Project> selectedProjects = ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
    if (selectedProjects.size() > 0) {
      for (Project project : selectedProjects) {
        project.restoreFromTrash();
      }
      Ode.getInstance().switchToTrash();
    } else {
      // The user can select a project to resolve the
      // error.
      ErrorReporter.reportInfo(MESSAGES.noProjectSelectedForRestore());
    }
  }
}

