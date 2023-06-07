package com.google.appinventor.client.actions;

import com.google.appinventor.client.wizards.MoveProjectsWizard;
import com.google.gwt.user.client.Command;

public class MoveProjectsAction implements Command {
  public void execute() {
    new MoveProjectsWizard();
  }

}
