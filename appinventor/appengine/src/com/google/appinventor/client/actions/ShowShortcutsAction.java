// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
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

        shortcutKeyHandler();
    }

    @Override
    public void execute() {
        VerticalPanel DialogBoxContents = new VerticalPanel();
        HTML message = new HTML(MESSAGES.KeyBoardShortcuts());
        Button button = new Button(Ode.MESSAGES.okButton());
        button.addClickHandler(event -> db.hide());
        DialogBoxContents.add(message);
        DialogBoxContents.add(button);
        db.setWidget(DialogBoxContents);
        db.center();
        db.show();
        button.setFocus(true);
    }

    private void shortcutKeyHandler() {
        Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                NativeEvent nativeEvent = event.getNativeEvent();
                if (event.getTypeInt() == Event.ONKEYDOWN) {
                    if (nativeEvent.getKeyCode() == 191 && nativeEvent.getAltKey()) {
                        shortcutPressed();
                    }
                    if (nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        escPressed();
                    }
                }
            }
        });
    }

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