package com.google.appinventor.client.actions;

import com.google.gwt.user.client.Command;
import com.google.appinventor.client.wizards.ShareProjectsWizard;

public class ShareProjectsAction implements Command {
  @Override
  public void execute() {
    new ShareProjectsWizard().show();
  }

}


// package com.google.appinventor.client.actions;

// import com.google.gwt.user.client.Command;
// import com.google.appinventor.client.wizards.UISettingsWizard;

// public class ShareProjectsAction implements Command {
//   @Override
//   public void execute() {
//     new UISettingsWizard().show();
//   }
// }

