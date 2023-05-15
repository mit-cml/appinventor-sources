package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.wizards.MoveProjectsWizard;
import com.google.appinventor.client.wizards.NewFolderWizard;
import com.google.gwt.user.client.Command;

import java.util.List;

public class MoveProjectsAction implements Command {
  public void execute() {
    new MoveProjectsWizard();
  }


  public class NewFolderAction implements Command {
    @Override
    public void execute() {
      new NewFolderWizard();
    }
  }

}
