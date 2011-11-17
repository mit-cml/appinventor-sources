// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.ErrorReporter;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.youngandroid.CodeblocksManager;
import com.google.appinventor.shared.rpc.project.ProjectNode;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Command for testing if the phone is connected
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class EnsurePhoneConnectedCommand extends ChainableCommand {

  /**
   * Creates a new ensure phone connected command, with additional behavior
   * provided by another ChainableCommand.
   *
   * @param nextCommand the command to execute iff the phone is connected
   */
  public EnsurePhoneConnectedCommand(ChainableCommand nextCommand) {
    super(nextCommand);
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
        ErrorReporter.reportError(caught.getMessage());
        executionFailedOrCanceled();
      }

      @Override
      public void onSuccess(Boolean result) {
        if (result) {
          executeNextCommand(node);
        } else {
          ErrorReporter.reportError(MESSAGES.phoneNotConnected());
          executionFailedOrCanceled();
        }
      }
    };

    CodeblocksManager.getCodeblocksManager().isPhoneConnected(callback);
  }
}
