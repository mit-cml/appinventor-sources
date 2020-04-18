package com.google.appinventor.client.actions;

import com.google.appinventor.client.wizards.ProjectUploadWizard;
import com.google.gwt.user.client.Command;

public class ImportProjectAction implements Command {
  @Override
  public void execute() {
    new ProjectUploadWizard().center();
  }
}
