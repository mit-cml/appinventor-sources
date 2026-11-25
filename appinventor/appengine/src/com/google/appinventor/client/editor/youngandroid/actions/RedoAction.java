// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.gwt.user.client.Command;

public class RedoAction implements Command {
  @Override
  public void execute() {
    FileEditor currentEditor = Ode.getInstance().getCurrentFileEditor();
    if (currentEditor instanceof YaFormEditor) {
      YaFormEditor editor = (YaFormEditor) currentEditor;
      if (editor.getUndoManager().canRedo()) {
        editor.getUndoManager().redo();
      }
    }
  }
}
