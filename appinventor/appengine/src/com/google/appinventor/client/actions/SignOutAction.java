package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class SignOutAction implements Command {
  private static final String SIGNOUT_URL = "/ode/_logout";

  @Override
  public void execute() {
    // Maybe take a screenshot
    Ode.getInstance().screenShotMaybe(new Runnable() {
      @Override
      public void run() {
        Window.Location.replace(SIGNOUT_URL);
      }
    }, true);               // Wait for i/o
  }
}
