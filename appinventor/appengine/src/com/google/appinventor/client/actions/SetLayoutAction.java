package com.google.appinventor.client.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

// Note: If we implement more than one alternate layout, the parameter
// can be changed from a boolean to a layout identifier
public class SetLayoutAction implements Command {
  private boolean isNewLayout = false;

  public void setIsNewLayout(boolean isNew) {
    isNewLayout = isNew;
  }

  @Override
  public void execute() {
    Ode.setUserNewLayout(isNewLayout);
    Window.Location.reload();
    // Not: See above comment
  }
}
