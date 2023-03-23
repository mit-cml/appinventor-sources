package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.wizards.MoveProjectsWizard;
import com.google.gwt.user.client.Command;

import java.util.List;

public class MoveProjectsAction implements Command {
  public void execute() {
    new MoveProjectsWizard("Test Title").execute(new MoveProjectsWizard.MoveProjectsCallback() {
      @Override
      public void onSuccess(Folder destination) {
        List<Project> selectedProjects = ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
        List<Folder> selectedFolders = ProjectListBox.getProjectListBox().getProjectList().getSelectedFolders();
        Ode.getInstance().getFolderManager().moveItemsToFolder(selectedProjects, selectedFolders,
            destination);
      }
    });
  }
}
