// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;

public class ResetAction implements Command {

  public ResetAction() {
    shortcutKeyHandler();
  }

  @Override
  public void execute() {
    if (Ode.getInstance().okToConnect()) {
      Ode.getInstance().getTopToolbar().startRepl(false, false, false, false); // We are really stopping the repl here
    }
  }

  private void shortcutKeyHandler() {
    Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if (event.getTypeInt() == Event.ONKEYDOWN && nativeEvent.getKeyCode() == KeyCodes.KEY_R
            && nativeEvent.getAltKey() && nativeEvent.getShiftKey()) {
          execute();
          nativeEvent.preventDefault();
        }
      }
    });
  }
}
