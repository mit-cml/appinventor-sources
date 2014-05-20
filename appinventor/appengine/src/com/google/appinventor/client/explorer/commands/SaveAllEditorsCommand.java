// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;

import java.util.Date;

/**
 * Command for saving all editors.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class SaveAllEditorsCommand extends ChainableCommand {
  /**
   * Creates a new save all editors command, with additional behavior provided
   * by another ChainableCommand.
   *
   * @param nextCommand the command to execute after the save has finished
   */
  public SaveAllEditorsCommand(ChainableCommand nextCommand) {
    super(nextCommand);
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    // Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
    final Ode ode = Ode.getInstance();
    ode.lockScreens(true);      // Lock out screen switching
    ode.getEditorManager().saveDirtyEditors(new Command() {
      @Override
      public void execute() {
        ode.lockScreens(false); // Screen switch OK now
        ErrorReporter.reportInfo(MESSAGES.savedProject(
            DateTimeFormat.getMediumDateTimeFormat().format(new Date())));
        executeNextCommand(node);
      }
    });
  }
}
