// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.gwt.user.client.Window;

/**
 * Command for deleting files.
 *
 */
public class DeleteFileCommand extends ChainableCommand {
  /**
   * Creates a new command for deleting a file.
   */
  public DeleteFileCommand() {
    super(null); // no next command
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    if (deleteConfirmation()) {
      Ode ode = Ode.getInstance();
      ode.getEditorManager().closeFileEditor(node.getProjectId(), node.getFileId());

      ode.getProjectService().deleteFile(
          node.getProjectId(), node.getFileId(),
          new OdeAsyncCallback<Long>(
              // message on failure
              MESSAGES.deleteFileError()) {
            @Override
            public void onSuccess(Long date) {
              getProject(node).deleteNode(node);
              Ode.getInstance().updateModificationDate(node.getProjectId(), date);
              executeNextCommand(node);
            }

            @Override
            public void onFailure(Throwable caught) {
              super.onFailure(caught);
              executionFailedOrCanceled();
            }
          });
    }
  }

  /**
   * Shows a confirmation dialog.
   *
   * @return {@code true} if the delete file command should be executed or
   *         {@code false} if it should be canceled
   */
  protected boolean deleteConfirmation() {
    return Window.confirm(MESSAGES.reallyDeleteFile());
  }
}
