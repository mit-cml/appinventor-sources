package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class ToggleTutorialAction implements Command {
  @Override
  public void execute() {
    Ode ode = Ode.getInstance();
    ode.setTutorialVisible(!ode.isTutorialVisible());
  }
}
