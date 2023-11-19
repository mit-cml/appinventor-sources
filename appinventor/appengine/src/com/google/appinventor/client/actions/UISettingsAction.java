package com.google.appinventor.client.actions;

import com.google.gwt.user.client.Command;
import com.google.appinventor.client.wizards.UISettingsWizard;

public class UISettingsAction implements Command {
  @Override
  public void execute() {
    new UISettingsWizard();
    // The wizard will switch to the design view when the new
    // project is created.
  }
}