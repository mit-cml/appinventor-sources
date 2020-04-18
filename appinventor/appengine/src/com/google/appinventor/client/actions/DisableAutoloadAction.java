package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class DisableAutoloadAction implements Command {
  @Override
  public void execute() {
    Ode.setUserAutoloadProject(false);
  }
}
