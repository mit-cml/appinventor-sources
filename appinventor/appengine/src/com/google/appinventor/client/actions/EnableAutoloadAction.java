package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class EnableAutoloadAction implements Command {
  @Override
  public void execute() {
    Ode.setUserAutoloadProject(true);
  }
}
