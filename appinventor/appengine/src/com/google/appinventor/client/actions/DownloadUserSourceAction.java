package com.google.appinventor.client.actions;

import com.google.appinventor.client.wizards.DownloadUserSourceWizard;
import com.google.gwt.user.client.Command;

public class DownloadUserSourceAction implements Command {
  @Override
  public void execute() {
    new DownloadUserSourceWizard().center();
  }
}
