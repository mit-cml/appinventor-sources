// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Command for building a target in a project.
 *
 */
public class BuildCommand extends ChainableCommand {
  private static final Logger LOG = Logger.getLogger(BuildCommand.class.getName());

  // The build target
  private final String target;

  // Whether or not to use the second buildserver
  private boolean secondBuildserver = false;
  private boolean isAab;

  // The next chainable command to be executed if a cached version of the build exists.
  private final ChainableCommand nextCommandIfCacheHit;

  /**
   * Creates a new build command.
   *
   * @param target the build target
   */
  public BuildCommand(String target, boolean secondBuildserver, boolean isAab) {
    this(target, secondBuildserver, isAab, null);
  }

  /**
   * Creates a new build command, with additional behavior provided by
   * another ChainableCommand.
   *
   * @param target the build target
   * @param nextCommand the command to execute after the build has finished
   */
  public BuildCommand(String target, boolean secondBuildserver, boolean isAab, ChainableCommand nextCommand) {
    this(target, secondBuildserver, isAab, nextCommand, null);
  }

  /**
   * Creates a new build command, with additional behavior provided by
   * two ChainableCommands, one which executes if a fresh build was started, and
   * another if a cached build exists.
   *
   * @param target the build target
   * @param nextCommand the command to execute after the build has finished
   * @param nextCommandIfCacheHit the command to execute if an up-to-date version
   *        of the build already exists
   */
  public BuildCommand(String target, boolean secondBuildserver, boolean isAab,
      ChainableCommand nextCommand, ChainableCommand nextCommandIfCacheHit) {
    super(nextCommand);
    this.isAab = isAab;
    this.target = target;
    this.secondBuildserver = secondBuildserver;
    this.nextCommandIfCacheHit = nextCommandIfCacheHit;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    final Ode ode = Ode.getInstance();
    LOG.info(MESSAGES.buildRequestedMessage(node.getName(),
        DateTimeFormat.getMediumDateTimeFormat().format(new Date())));

    OdeAsyncCallback<RpcResult> callback =
        new OdeAsyncCallback<RpcResult>(
            // failure message
            MESSAGES.buildError()) {
      @Override
      public void onSuccess(RpcResult result) {
        LOG.info(result.getOutput());
        LOG.info(result.getError());
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
            case Response.SC_REQUEST_ENTITY_TOO_LARGE:
              // SC_REQUEST_ENTITY_TOO_LARGE (response code 413) means that the project file was
              // too large to be sent to the build server.
              ClientJsonParser parser = new ClientJsonParser();
              JSONObject info = parser.parse(result.getError()).asObject();
              int maxSize = info.get("maxSize").asNumber().getInt();
              double appSize = info.get("aiaSize").asNumber().getDouble();
              ErrorReporter.reportError(MESSAGES.buildProjectTooLargeError(maxSize, appSize));
              break;
            case -2:
              // A cached version of the build output exists and no request to the buildserver
              // was made.
              // Skip build process and execute the cache hit command
              if (nextCommandIfCacheHit != null) {
                nextCommandIfCacheHit.startExecuteChain(Tracking.PROJECT_ACTION_DOWNLOAD_CACHED_BUILD, node,
                  new Command() {
                    @Override
                    public void execute() {
                    }
                });
              }
              return;

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
    ode.getProjectService().build(node.getProjectId(), nonce, target, secondBuildserver, isAab, callback);
  }
}
