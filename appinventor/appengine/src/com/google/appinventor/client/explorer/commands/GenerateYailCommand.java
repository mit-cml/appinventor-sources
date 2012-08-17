// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.Command;

/**
 * Command for generating the Yail code for the current project
 * and saving it to the server.
 * 
 * @author sharon@google.com (Sharon Perl)
 */
public class GenerateYailCommand extends ChainableCommand {

  /**
   * Creates a new generate yail command, with additional behavior provided
   * by another ChainableCommand.
   *
   * @param nextCommand the command to execute after the save has finished
   */
  public GenerateYailCommand(ChainableCommand nextCommand) {
    super(nextCommand);
  }
  
  @Override
  protected boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  protected void execute(final ProjectNode node) {
    Ode.getInstance().getEditorManager().generateYailForBlocksEditors(
        new Command() {
          @Override
          public void execute() {
            executeNextCommand(node);
          }
        },
        new Command() {
          @Override
          public void execute() {
            executionFailedOrCanceled();
          }
        });
  }
}
