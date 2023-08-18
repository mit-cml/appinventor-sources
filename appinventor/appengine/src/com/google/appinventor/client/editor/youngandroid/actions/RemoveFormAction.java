// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.DeleteFileCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class RemoveFormAction implements Command {
  @Override
  public void execute() {
    Ode ode = Ode.getInstance();
    if (ode.screensLocked()) {
      return;                 // Don't permit this if we are locked out (saving files)
    }
    YoungAndroidSourceNode sourceNode = ode.getCurrentYoungAndroidSourceNode();
    if (sourceNode != null && !sourceNode.isScreen1()) {
      // DeleteFileCommand handles the whole operation, including displaying the confirmation
      // message dialog, closing the form editor and the blocks editor,
      // deleting the files in the server's storage, and deleting the
      // corresponding client-side nodes (which will ultimately trigger the
      // screen deletion in the DesignToolbar).
      final String deleteConfirmationMessage = MESSAGES.reallyDeleteForm(
          sourceNode.getFormName());
      ChainableCommand cmd = new DeleteFileCommand() {
        @Override
        protected boolean deleteConfirmation() {
          return Window.confirm(deleteConfirmationMessage);
        }
      };
      cmd.startExecuteChain(Tracking.PROJECT_ACTION_REMOVEFORM_YA, sourceNode);
    }
  }
}
