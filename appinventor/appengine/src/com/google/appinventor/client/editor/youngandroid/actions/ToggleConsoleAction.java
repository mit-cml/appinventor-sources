package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.Command;

public class ToggleConsoleAction implements Command {
    @Override
    public void execute() {
        Ode ode = Ode.getInstance();
        ode.setConsoleVisible(!ode.isConsoleVisible());
    }
}