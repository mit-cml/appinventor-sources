package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class ResetAction implements Command {
  @Override
  public void execute() {
    if (Ode.getInstance().okToConnect()) {
      Ode.getInstance().getTopToolbar().startRepl(false, false, false, false); // We are really stopping the repl here
    }
  }
}
