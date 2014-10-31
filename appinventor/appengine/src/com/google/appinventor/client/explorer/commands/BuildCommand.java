// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.output.MessagesOutput;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.http.client.Response;
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
          // The result is the HTTP response code from the build server.
          int responseCode = result.getResult();
          switch (responseCode) {
            case Response.SC_SERVICE_UNAVAILABLE:
              // SC_SERVICE_UNAVAILABLE (response code 503), means that the build server is too busy
              // at this time to accept this build request.
              // We use ErrorReporter.reportInfo so that the message has yellow background instead of
              // red background.
              ErrorReporter.reportInfo(MESSAGES.buildServerBusyError());
              break;
            case Response.SC_CONFLICT:
              // SC_CONFLICT (response code 409), means that the build server is running a
              // different version of the App Inventor code.
              // We use ErrorReporter.reportInfo so that the message has yellow background instead
              // of red background.
              ErrorReporter.reportInfo(MESSAGES.buildServerDifferentVersion());
              break;
            default:
              String errorMsg = result.getError();
              // This is not an internal App Inventor bug. The error is reported as info so that
              // the red background is not shown.
              ErrorReporter.reportInfo(MESSAGES.buildFailedError() + 
                  (errorMsg.isEmpty() ? "" : " " + errorMsg));
              break;
          }
          executionFailedOrCanceled();
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        executionFailedOrCanceled();
      }
    };

    String nonce = ode.generateNonce();
    ode.getProjectService().build(node.getProjectId(), nonce, target, callback);
  }
}
