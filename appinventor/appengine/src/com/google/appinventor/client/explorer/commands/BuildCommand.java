// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.output.MessagesOutput;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/**
 * Command for building a target in a project.
 *
 */
public class BuildCommand extends ChainableCommand {
  // The build target
  private String target;

  /**
   * Creates a new build command.
   *
   * @param target the build target
   */
  public BuildCommand(String target) {
    this(target, null);
  }

  /**
   * Creates a new build command, with additional behavior provided by
   * another ChainableCommand.
   *
   * @param target the build target
   * @param nextCommand the command to execute after the build has finished
   */
  public BuildCommand(String target, ChainableCommand nextCommand) {
    super(nextCommand);
    this.target = target;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    final Ode ode = Ode.getInstance();
    final MessagesOutput messagesOutput = MessagesOutput.getMessagesOutput();
    messagesOutput.clear();
    messagesOutput.addMessages(MESSAGES.buildRequestedMessage(node.getName(),
        DateTimeFormat.getMediumDateTimeFormat().format(new Date())));

    OdeAsyncCallback<RpcResult> callback =
        new OdeAsyncCallback<RpcResult>(
            // failure message
            MESSAGES.buildError()) {
      @Override
      public void onSuccess(RpcResult result) {
        messagesOutput.addMessages(result.getOutput());
        messagesOutput.addMessages(result.getError());
        Tracking.trackEvent(Tracking.PROJECT_EVENT, Tracking.PROJECT_SUBACTION_BUILD_YA,
                            node.getName(), getElapsedMillis());
        if (result.succeeded()) {
          executeNextCommand(node);
        } else {
          ErrorReporter.reportError((result.getError().isEmpty())
                                    ? MESSAGES.buildFailedError()
                                    : result.getError());
          executionFailedOrCanceled();
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        executionFailedOrCanceled();
      }
    };

    ode.getProjectService().build(node.getProjectId(), target, callback);
  }
}
