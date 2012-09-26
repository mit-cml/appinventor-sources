// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.youngandroid.CodeblocksManager;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Tells codeblocks to save the blocks.
 * <p>
 * Should be called before a Young Android build is triggered in order to get
 * the most up to date blocks.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class SaveBlocksCommand extends ChainableCommand {

  /**
   * Creates a new SaveBlocksCommand, with additional behavior provided
   * by another ChainableCommand.
   *
   * @param nextCommand the command to execute after the blocks have been saved
   */
  public SaveBlocksCommand(ChainableCommand nextCommand) {
    super(nextCommand);
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    // Tell codeblocks to save the blocks. If codeblocks isn't open, onSuccess will be called right
    // away.
    CodeblocksManager.getCodeblocksManager().saveCodeblocksSource(new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        executeNextCommand(node);
      }

      @Override
      public void onFailure(Throwable caught) {
        // The error has already been reported in CodeblocksManager.
        executionFailedOrCanceled();
      }
    });
  }
}
