package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class SetFontRegularAction implements Command {
  @Override
  public void execute() {
    Ode.setUserDyslexicFont(false);
    // Window.Location.reload();
    // Not: See above comment
  }
}
