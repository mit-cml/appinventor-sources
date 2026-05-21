// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidYailNode;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Command for deleting files.
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
    if (node instanceof YoungAndroidSourceNode) {
      new DeleteFormDialog((YoungAndroidSourceNode) node).center();
      return;
    }

    if (deleteConfirmation()) {
      // Asset files
      final Ode ode = Ode.getInstance();
      ode.getProjectService().deleteFile(ode.getSessionId(),
          node.getProjectId(), node.getFileId(),
          new OdeAsyncCallback<Long>(
              // message on failure
              MESSAGES.deleteFileError()) {
            @Override
            public void onSuccess(Long date) {
              getProject(node).deleteNode(node);
              ode.updateModificationDate(node.getProjectId(), date);
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

  private class DeleteFormDialog extends DialogBox {
    // UI elements
    private final LabeledTextBox nameTextBox  = new LabeledTextBox(MESSAGES.formNameLabel());
    private final CheckBox cb = new CheckBox(MESSAGES.checkBoxButton());
    private final YoungAndroidSourceNode node;
    private final String formName;

    DeleteFormDialog(final YoungAndroidSourceNode node) {
      super(false, false);
      setGlassEnabled(true);

      this.node = node;
      formName = node.getFormName();

      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.removeFormButton());

      Button topInvisible = new Button();
      Button bottomInvisible = new Button();
      topInvisible.setStyleName("FocusTrap");
      bottomInvisible.setStyleName("FocusTrap");

      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
          executionFailedOrCanceled();
        }
      });

      cb.setValue(true);
      final Button deleteButton = new Button(MESSAGES.deleteButton());
      deleteButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleOkClick();
        }
      });
      deleteButton.addStyleName("destructive-action");

      VerticalPanel contentPanel = new VerticalPanel();
      HorizontalPanel labelPanel = new HorizontalPanel();
      Label warnmsg = new Label(MESSAGES.reallyDeleteWarning(formName));
      labelPanel.add(warnmsg);
      labelPanel.setSize("100%", "22px");
      contentPanel.add(labelPanel);
      contentPanel.add(topInvisible);
      contentPanel.add(nameTextBox);
      HorizontalPanel buttonPanel = new HorizontalPanel();
      HorizontalPanel checkboxPanel = new HorizontalPanel();
      buttonPanel.add(cancelButton);
      buttonPanel.add(deleteButton);
      buttonPanel.add(bottomInvisible);
      checkboxPanel.add(cb);
      checkboxPanel.setSize("100%", "20px");
      contentPanel.add(checkboxPanel);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);
      contentPanel.setSize("320px", "100%");
      deleteButton.setEnabled(false);
      nameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
            hide();
            executionFailedOrCanceled();
          }
          if (nameTextBox.getText().equals(formName)) {
            deleteButton.setEnabled(true);
            nameTextBox.setColor("#00c8ff");
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
              handleOkClick();
            }
          } else {
            deleteButton.setEnabled(false);
            nameTextBox.setColor("red");
          }
        }
      });
      topInvisible.addFocusHandler(new FocusHandler() {
        public void onFocus(FocusEvent event) {
          cancelButton.setFocus(true);
        }
      });
      bottomInvisible.addFocusHandler(new FocusHandler() {
        public void onFocus(FocusEvent event) {
          nameTextBox.setFocus(true);
        }
      });
      add(contentPanel);
    }

    private void handleOkClick() {
      if (validate(nameTextBox.getText())) {
        if (cb.getValue()) {
          checkpointThenDelete();
        } else {
          removeForm(false);
        }
        hide();
      } else {
        nameTextBox.setFocus(true);
      }
    }

    private boolean validate(String formName) {
      return formName.equals(this.formName);
    }

    /**
     * Removes a form from the project.
     *
     * @param confirmed true if the user has already confirmed removal, otherwise false, in which
     *                  case one final confirmation message will be displayed.
     */
    private void removeForm(boolean confirmed) {
      if (!confirmed && !deleteConfirmation()) {
        executionFailedOrCanceled();
        return;
      }
      final Ode ode = Ode.getInstance();

      // Before we delete the form, we need to close both the form editor and the blocks editor
      // (in the browser).
      final String qualifiedFormName = node.getQualifiedName();
      final String formFileId = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
      final String blocksFileId = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
      final String yailFileId = YoungAndroidYailNode.getYailFileId(qualifiedFormName);
      final long projectId = node.getProjectId();
      String[] fileIds = new String[2];
      fileIds[0] = formFileId;
      fileIds[1] = blocksFileId;
      ode.getEditorManager().closeFileEditors(projectId, fileIds);

      // When we tell the project service to delete either the form (.scm) file or the blocks
      // (.bky) file, it will delete both of them, and also the yail (.yail) file.
      ode.getProjectService().deleteFile(ode.getSessionId(), projectId, node.getFileId(),
          new OdeAsyncCallback<Long>(
              // message on failure
              MESSAGES.deleteFileError()) {
            @Override
            public void onSuccess(Long date) {
              // Remove all related nodes (form, blocks, yail) from the project.
              Project project = getProject(node);
              for (ProjectNode sourceNode : node.getProjectRoot().getAllSourceNodes()) {
                if (sourceNode.getFileId().equals(formFileId)
                    || sourceNode.getFileId().equals(blocksFileId)
                    || sourceNode.getFileId().equals(yailFileId)) {
                  project.deleteNode(sourceNode);
                }
              }
              ode.getDesignToolbar().removeScreen(project.getProjectId(), formName);
              ode.updateModificationDate(projectId, date);
              executeNextCommand(node);
            }

            @Override
            public void onFailure(Throwable caught) {
              super.onFailure(caught);
              executionFailedOrCanceled();
            }
          });
    }

    @Override
    public void show() {
      super.show();
      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          nameTextBox.setFocus(true);
        }
      });
    }

    private void checkpointThenDelete() {
      if (deleteConfirmation()) {
        ProjectRootNode root = node.getProjectRoot();
        if (root != null) {
          ChainableCommand cmd = new SaveAllEditorsCommand(
              new SaveScreenCheckpointCommand(true, new ChainableCommand() {
                @Override
                protected boolean willCallExecuteNextCommand() {
                  return false;
                }

                @Override
                protected void execute(ProjectNode node) {
                  removeForm(true);
                }
              }));
          cmd.startExecuteChain(Tracking.PROJECT_ACTION_CHECKPOINT_YA, root);
        }
      }
    }
  }
}
