package com.google.appinventor.client.actions;

import com.google.appinventor.client.wizards.NewFolderWizard;
import com.google.gwt.user.client.Command;

public class NewFolderAction implements Command {
  @Override
  public void execute() {
    new NewFolderWizard();
  }
}
