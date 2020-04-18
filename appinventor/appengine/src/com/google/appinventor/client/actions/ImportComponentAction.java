package com.google.appinventor.client.actions;

import com.google.appinventor.client.wizards.ComponentImportWizard;
import com.google.gwt.user.client.Command;

public class ImportComponentAction implements Command {
  @Override
  public void execute() {
    new ComponentImportWizard().center();
  }
}
