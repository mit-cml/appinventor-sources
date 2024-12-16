// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ShowShortcutsAction implements Command {

    private DialogBox db;

    public ShowShortcutsAction() {
        db = new DialogBox(true, false);
        db.setText("Keyboard Shortcuts");
        db.setStyleName("ode-DialogBox");
        db.setHeight("200px");
        db.setWidth("400px");
        db.setGlassEnabled(true);
        db.setAnimationEnabled(true);

        shortcutKeyHandler(this);
    }

    @Override
    public void execute() {
        VerticalPanel DialogBoxContents = new VerticalPanel();
        HTML message = new HTML(MESSAGES.KeyBoardShortcuts());
        Button button = new Button("OK");
        button.addClickHandler(event -> db.hide());
        DialogBoxContents.add(message);
        DialogBoxContents.add(button);
        db.setWidget(DialogBoxContents);
        db.center();
        db.show();
    }

    private native void shortcutKeyHandler(ShowShortcutsAction action) /*-{
      $wnd.document.addEventListener("keydown", function (event) {
          if (event.altKey && event.key === "?") {
              event.preventDefault();
              action.@com.google.appinventor.client.actions.ShowShortcutsAction::shortcutPressed()();
          } else if (event.key === "Escape"){
              action.@com.google.appinventor.client.actions.ShowShortcutsAction::escPressed()();
          }
      });
    }-*/;

    private void shortcutPressed() {
      if (!db.isShowing()) {
          execute();
      }
    }

    private void escPressed() {
      if (db.isShowing()) {
        db.hide();
      }      
    }
}