package com.google.appinventor.client.actions;

import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.gwt.user.client.Command;

public class ImportTemplateAction implements Command {
  @Override
  public void execute() {
    new TemplateUploadWizard().center();
  }
}
