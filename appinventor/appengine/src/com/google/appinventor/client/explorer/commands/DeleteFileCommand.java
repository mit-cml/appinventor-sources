// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.EditorManager;
import com.google.appinventor.client.explorer.project.Project;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
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

      if (node instanceof YoungAndroidSourceNode) {
        // node could be either a YoungAndroidFormNode or a YoungAndroidBlocksNode.
        // Before we delete the form, we need to close both the form editor and the blocks editor
        // (in the browser).
        String qualifiedFormName = ((YoungAndroidSourceNode) node).getQualifiedName();
        final String formFileId = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
        final String blocksFileId = YoungAndroidBlocksNode.getBlocksFileId(qualifiedFormName);
        final long projectId = node.getProjectId();
        EditorManager editorManager = ode.getEditorManager();
        editorManager.closeFileEditor(projectId, formFileId);
        editorManager.closeFileEditor(projectId, blocksFileId);

        // When we tell the project service to delete either the form (.scm) file or the blocks
        // (.blk) file, it will delete both of them
        ode.getProjectService().deleteFile(projectId, node.getFileId(),
            new OdeAsyncCallback<Long>(
        // message on failure
            MESSAGES.deleteFileError()) {
          @Override
          public void onSuccess(Long date) {
            // Remove both nodes (form and blocks) from the project.
            Project project = getProject(node);
            for (ProjectNode sourceNode : node.getProjectRoot().getAllSourceNodes()) {
              if (sourceNode.getFileId().equals(formFileId) ||
                  sourceNode.getFileId().equals(blocksFileId)) {
                project.deleteNode(sourceNode);
              }
            }
            Ode.getInstance().updateModificationDate(projectId, date);
            executeNextCommand(node);
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            executionFailedOrCanceled();
          }
        });
      } else {
        executionFailedOrCanceled();
        throw new IllegalArgumentException("node must be a YoungAndroidSourceNode");
      }
    } else {
      executionFailedOrCanceled();
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
