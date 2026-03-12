// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;
import static com.google.appinventor.client.utils.Promise.resolve;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;

public class SaveProjectToCompanionAction implements Command {

  public SaveProjectToCompanionAction() {
  }

  @Override
  public void execute() {
    BlocklyPanel.startCache().done(result -> {
        return resolve(result);
      });
  }
}
