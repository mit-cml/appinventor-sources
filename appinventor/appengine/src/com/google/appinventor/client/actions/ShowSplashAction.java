package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class ShowSplashAction implements Command {
  @Override
  public void execute() {
    Ode.getInstance().showWelcomeDialog();
  }
}
