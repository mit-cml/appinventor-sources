// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.output.MessagesOutput;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;

import java.util.Date;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Command to wait for the result of a build
 *
 * @author markf@google.com (Mark Friedman)
 */
public class WaitForBuildResultCommand extends ChainableCommand {
  // The build target
  private final String target;
  private static final int WAIT_INTERVAL_MILLIS = 10000;
  private final MessagesOutput messagesOutput;
  private final String buildRequestTime;

  /**
   * Creates a new WaitForBuildResultCommand.
   *
   * @param target the build target
   */
  public WaitForBuildResultCommand(String target) {
    this(target, null);
  }

  /**
   * Creates a new WaitForBuildResultCommand, with additional behavior provided by
   * another ChainableCommand.
   *
   * @param target the build target
   * @param nextCommand the command to execute after the build has finished
   */
  public WaitForBuildResultCommand(String target, ChainableCommand nextCommand) {
    super(nextCommand);
    this.target = target;
    messagesOutput = MessagesOutput.getMessagesOutput();
    buildRequestTime = DateTimeFormat.getMediumDateTimeFormat().format(new Date());
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  // TODO(sharon): what happens if the user has already switched projects? Do
  // we still switch back to the project to display build errors?
  @Override
  public void execute(final ProjectNode node) {
    final Ode ode = Ode.getInstance();
    messagesOutput.clear();
    messagesOutput.addMessages(MESSAGES.buildRequestedMessage(node.getName(), buildRequestTime));

    OdeAsyncCallback<RpcResult> callback =
        new OdeAsyncCallback<RpcResult>(
            // failure message
            MESSAGES.buildError()) {
      @Override
      public void onSuccess(RpcResult result) {
        messagesOutput.addMessages("Waiting for " + getElapsedMillis() / 1000 + " seconds.");
        messagesOutput.addMessages(result.getOutput());
        messagesOutput.addMessages(result.getError());
        Tracking.trackEvent(Tracking.PROJECT_EVENT, Tracking.PROJECT_SUBACTION_BUILD_YA,
                            node.getName(), getElapsedMillis());
        if (result.succeeded()) {
          ode.getTopToolbar().updateKeystoreFileMenuButtons();
          executeNextCommand(node);
        } else if (result.getResult() == 1) {
          // General build error
          String errorMsg = result.getError();
          // This is not an internal App Inventor bug. The error should be
          // reported in a yellow info box.
          ErrorReporter.reportInfo(MESSAGES.buildFailedError() + 
              (errorMsg.isEmpty() ? "" : " " + errorMsg));
          executionFailedOrCanceled();
        } else if (result.getResult() == 2) {
          // Yail generation error
          String formName = extractFormName(result);
          ErrorReporter.reportError(MESSAGES.errorGeneratingYail(formName));
          String blocksFileName = formName  + 
              YoungAndroidSourceAnalyzer.CODEBLOCKS_SOURCE_EXTENSION;
          YoungAndroidBlocksNode blocksNode =
              findBlocksNode((YoungAndroidProjectNode) node, blocksFileName);
          if (blocksNode != null) {
            showProblemBlocks(blocksNode);
          }
          executionFailedOrCanceled();
        } else {
          // Build isn't done yet
          Timer timer = new Timer() {
            @Override
            public void run() {
              execute(node);
            }
          };
          // TODO(user): Maybe do an exponential backoff here.
          timer.schedule(WAIT_INTERVAL_MILLIS);
        }
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        executionFailedOrCanceled();
      }
    };

    ode.getProjectService().getBuildResult(node.getProjectId(), target, callback);
  }

  private static YoungAndroidBlocksNode findBlocksNode(YoungAndroidProjectNode projectRootNode,
      String formName) {
    // Iterate over the YoungAndroidBlocksNodes in this project.
    for (ProjectNode source : projectRootNode.getAllSourceNodes()) {
      if (source instanceof YoungAndroidBlocksNode) {
        YoungAndroidBlocksNode blocksNode = (YoungAndroidBlocksNode) source;
        if (formName.equals(blocksNode.getFormName())) {
          return blocksNode;
        }
      }
    }
    return null;
  }

  private static String extractFormName(RpcResult result) {
    String extraString = result.getExtra();
    if (extraString != null) {
      JSONValue extraJSONValue = JSONParser.parseStrict(extraString);
      JSONObject extraJSONObject = extraJSONValue.isObject();
      if (extraJSONObject != null) {
        JSONValue formNameJSONValue = extraJSONObject.get("formName");
        if (formNameJSONValue != null) {
          JSONString formNameJSONString = formNameJSONValue.isString();
          if (formNameJSONString != null) {
            return formNameJSONString.stringValue();
          }
        }
      }
    }
    return "Screen1";
  }

  /**
   * Shows the given blocks file in the blocks editor so the user can take
   * care of any problems.
   */
  private static void showProblemBlocks(final YoungAndroidBlocksNode blocksNode) {
    ProjectEditor projectEditor = ViewerBox.getViewerBox().show(blocksNode.getProjectRoot());
    // TODO(sharon): cause the blocks editor to highlight the problem blocks=
  }
}
