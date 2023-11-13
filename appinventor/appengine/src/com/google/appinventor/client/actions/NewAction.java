package com.google.appinventor.client.actions;

import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class NewAction implements Command {
  @Override
  public void execute() {
    new NewYoungAndroidProjectWizard().show();
    // The wizard will switch to the design view when the new
    // project is created.
  }
}
