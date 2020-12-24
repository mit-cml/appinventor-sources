package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class EmulatorAction implements Command {
  @Override
  public void execute() {
    if (Ode.getInstance().okToConnect()) {
      Ode.getInstance().getTopToolbar().startRepl(true, false, true, false); // true means we are the
                                    // emulator
    }
  }
}
