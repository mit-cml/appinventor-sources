// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.RpcStatusPopup;
import com.google.appinventor.client.output.MessagesOutput;
import com.google.appinventor.client.youngandroid.CodeblocksManager;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.common.base.Preconditions;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * Command for downloading a project target to the phone
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class DownloadToPhoneCommand extends ChainableCommand {
  // The download target
  private String target;
  private static final String BUILD_FOLDER = "build";
  // must match string in RpcStatusPopup map
  private static final String FAKE_RPC_NAME = "downloadToPhone";

  /**
   * Creates a new command.
   *
   * @param target the build target
   */
  public DownloadToPhoneCommand(String target) {
    this(target, null);
    Preconditions.checkNotNull(target);
  }

  /**
   * Creates a new download command, with additional behavior provided by
   * another ChainableCommand.
   *
   * @param target the download target
   * @param nextCommand the command to execute after the download has finished
   */
  public DownloadToPhoneCommand(String target, ChainableCommand nextCommand) {
    super(nextCommand);
    Preconditions.checkNotNull(target);
    this.target = target;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    final MessagesOutput messagesOutput = MessagesOutput.getMessagesOutput();
    messagesOutput.addMessages(MESSAGES.downloadingToPhoneMessage());

    final RpcStatusPopup rpcStatusPopup = Ode.getInstance().getRpcStatusPopup();

    AsyncCallback<Void> callback = new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable caught) {
        rpcStatusPopup.onFailure(FAKE_RPC_NAME, caught);
        ErrorReporter.reportError(MESSAGES.downloadToPhoneFailedMessage());
        executionFailedOrCanceled();
      }

      @Override
      public void onSuccess(Void result) {
        rpcStatusPopup.onSuccess(FAKE_RPC_NAME, result);
        Window.alert(MESSAGES.downloadToPhoneSucceededMessage());
        executeNextCommand(node);
      }
    };

    String packageName;
    if (node instanceof YoungAndroidProjectNode) {
      YoungAndroidProjectNode yaNode = (YoungAndroidProjectNode) node;
      packageName = yaNode.getPackageNode().getPackageName();
    } else {
      ErrorReporter.reportError(MESSAGES.downloadToPhoneFailedMessage());
      executionFailedOrCanceled();
      return;
    }

    rpcStatusPopup.onStart(FAKE_RPC_NAME);
    String apkFilePath = BUILD_FOLDER + "/" +
        YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID + "/" + node.getName() + ".apk";
    CodeblocksManager.getCodeblocksManager().installApplication(apkFilePath, node.getName(),
        packageName, callback);
  }
}
