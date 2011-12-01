// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * A command that creates a new form.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class AddFormCommand extends ChainableCommand {
  /**
   * Creates a new command for creating a new form
   */
  public AddFormCommand() {
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(ProjectNode node) {
    if (node instanceof YoungAndroidProjectNode) {
      new NewFormDialog((YoungAndroidProjectNode) node).center();
    } else {
      executionFailedOrCanceled();
      throw new IllegalArgumentException("node must be a YoungAndroidProjectNode");
    }
  }

  /**
   * Dialog for getting the name for the new form.
   */
  private class NewFormDialog extends DialogBox {
    // UI elements
    private final LabeledTextBox newNameTextBox;

    private final List<String> otherFormNames;

    NewFormDialog(final YoungAndroidProjectNode projectRootNode) {
      super(false, true);

      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.newFormTitle());
      VerticalPanel contentPanel = new VerticalPanel();

      final String prefix = "Screen";
      final int prefixLength = prefix.length();
      int highIndex = 0;
      otherFormNames = new ArrayList<String>();
      for (ProjectNode source : projectRootNode.getAllSourceNodes()) {
        if (source instanceof YoungAndroidFormNode) {
          String formName = StorageUtil.trimOffExtension(StorageUtil.basename(source.getFileId()));
          otherFormNames.add(formName);

          if (formName.startsWith(prefix)) {
            try {
              highIndex = Math.max(highIndex, Integer.parseInt(formName.substring(prefixLength)));
            } catch (NumberFormatException e) {
              continue;
            }
          }
        }
      }
      String defaultFormName = prefix + (highIndex + 1);

      newNameTextBox = new LabeledTextBox(MESSAGES.formNameLabel());
      newNameTextBox.setText(defaultFormName);
      newNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          int keyCode = event.getNativeKeyCode();
          if (keyCode == KeyCodes.KEY_ENTER) {
            handleOkClick(projectRootNode);
          } else if (keyCode == KeyCodes.KEY_ESCAPE) {
            hide();
            executionFailedOrCanceled();
          }
        }
      });
      contentPanel.add(newNameTextBox);

      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
          executionFailedOrCanceled();
        }
      });
      Button okButton = new Button(MESSAGES.okButton());
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleOkClick(projectRootNode);
        }
      });
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);
      contentPanel.setSize("320px", "100%");

      add(contentPanel);
    }

    private void handleOkClick(YoungAndroidProjectNode projectRootNode) {
      String newFormName = newNameTextBox.getText();
      if (validate(newFormName)) {
        hide();
        addFormAction(projectRootNode, newFormName);
      } else {
        newNameTextBox.setFocus(true);
      }
    }

    private boolean validate(String newFormName) {
      // Check that it meets the formatting requirements.
      if (!TextValidators.isValidIdentifier(newFormName)) {
        Window.alert(MESSAGES.malformedFormNameError());
        return false;
      }

      // Check that it's unique.
      if (otherFormNames.contains(newFormName)) {
        Window.alert(MESSAGES.duplicateFormNameError());
        return false;
      }

      return true;
    }

    /**
     * Adds a new form to the project.
     *
     * @param formName the new form name
     */
    protected void addFormAction(final YoungAndroidProjectNode projectRootNode, String formName) {
      final Ode ode = Ode.getInstance();
      final YoungAndroidPackageNode packageNode = projectRootNode.getPackageNode();
      String qualifiedFormName = packageNode.getPackageName() + '.' + formName;
      final String formFileId = YoungAndroidFormNode.getFormFileId(qualifiedFormName);

      OdeAsyncCallback<Long> callback = new OdeAsyncCallback<Long>(
          // failure message
          MESSAGES.addFormError()) {
        @Override
        public void onSuccess(Long modDate) {
          Ode.getInstance().updateModificationDate(projectRootNode.getProjectId(), modDate);

          // Add the new form node to the project
          YoungAndroidFormNode newFormNode = new YoungAndroidFormNode(formFileId);
          Project project = Ode.getInstance().getProjectManager().getProject(projectRootNode);
          project.addNode(packageNode, newFormNode);
          final String fileId = newFormNode.getFileId();

          // Select the new form editor. We need to do this later because the form editor isn't
          // added to the project editor until the form file is completely loaded.
          Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
              ProjectEditor projectEditor = ViewerBox.getViewerBox().show(projectRootNode);
              FileEditor fileEditor = projectEditor.getFileEditor(fileId);
              if (fileEditor != null) {
                projectEditor.selectFileEditor(fileEditor);

                executeNextCommand(projectRootNode);

              } else {
                // The form editor is still not there. Try again later.
                Scheduler.get().scheduleDeferred(this);
              }
            }
          });

        }

        @Override
        public void onFailure(Throwable caught) {
          super.onFailure(caught);
          executionFailedOrCanceled();
        }
      };

      // Create the new form on the backend
      ode.getProjectService().addFile(projectRootNode.getProjectId(), formFileId, callback);
    }

    @Override
    public void show() {
      super.show();

      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          newNameTextBox.setFocus(true);
        }
      });
    }
  }
}
