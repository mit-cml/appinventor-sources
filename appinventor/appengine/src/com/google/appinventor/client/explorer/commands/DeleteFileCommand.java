// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidYailNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.explorer.commands.SaveScreenCheckpointCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.Command;

import java.util.HashSet;
import java.util.Set;
import com.google.gwt.user.client.Window;

/**
 * Command for deleting files.
 *
 */
public class DeleteFileCommand extends ChainableCommand {
  /**
   * Creates a new command for deleting a file.
   */
  private static final int MAX_FORM_COUNT = 10; 
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
    } 
    else {
      executionFailedOrCanceled();
      throw new IllegalArgumentException("node must be a YoungAndroidProjectNode");
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
    private final LabeledTextBox newNameTextBox;
    private final CheckBox cb = new CheckBox(MESSAGES.checkBoxButton());
    private final Set<String> otherFormNames;

    DeleteFormDialog(final YoungAndroidSourceNode projectRootNode) {
      super(false, true);
      setStylePrimaryName("ode-DialogBox");
      VerticalPanel contentPanel = new VerticalPanel();
      setText(MESSAGES.removeFormButton());
      final String prefix = "Screen";
      final String abc;
      final int prefixLength = prefix.length();
      int highIndex = 0;
      // Collect the existing form names so we can prevent duplicate form names.
      otherFormNames = new HashSet<String>();
      String defaultFormName = prefix + (highIndex + 1);
      newNameTextBox = new LabeledTextBox(MESSAGES.formNameLabel());
	    
      String cancelText = MESSAGES.cancelButton();
      String okText = MESSAGES.okButton();

      // Keeps track of the total number of screens.
      int formCount = otherFormNames.size() + 1;
      if (formCount > MAX_FORM_COUNT) {
        HorizontalPanel errorPanel = new HorizontalPanel();
        HTML tooManyScreensLabel = new HTML(MESSAGES.formCountErrorLabel());
        errorPanel.add(tooManyScreensLabel);
        errorPanel.setSize("100%", "24px");
        contentPanel.add(errorPanel);

        okText = MESSAGES.addScreenButton();
        cancelText = MESSAGES.cancelScreenButton();
      }
      Button cancelButton = new Button(cancelText);
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
          executionFailedOrCanceled();
        }
      });
      
      cb.setValue(true);
      final Button okButton = new Button(okText);
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
    	   handleOkClick(projectRootNode);    
        }
      });
   
      if (projectRootNode instanceof YoungAndroidFormNode) {
        final String abc1 = ((YoungAndroidFormNode) projectRootNode).getFormName();
        HorizontalPanel labelPanel = new HorizontalPanel();
        Label warnmsg = new Label(MESSAGES.reallyDeleteWarning(abc1));
  	labelPanel.add(warnmsg);
  	labelPanel.setSize("100%", "22px");
  	contentPanel.add(labelPanel);
  	contentPanel.add(newNameTextBox);
        HorizontalPanel buttonPanel = new HorizontalPanel();
  	HorizontalPanel checkboxPanel = new HorizontalPanel();
  	buttonPanel.add(cancelButton);
  	buttonPanel.add(okButton);
  	checkboxPanel.add(cb);
  	checkboxPanel.setSize("100%", "20px");
  	contentPanel.add(checkboxPanel);
  	buttonPanel.setSize("100%", "24px");
  	contentPanel.add(buttonPanel);
  	contentPanel.setSize("320px", "100%");
  	okButton.setEnabled(false); 
	newNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
    	  @Override
    	  public void onKeyUp(KeyUpEvent event) {
      	    if(newNameTextBox.getText().equals(abc1)) {
              okButton.setEnabled(true);
	      newNameTextBox.setColor("#00c8ff");
	    }	  
            else{
              okButton.setEnabled(false);
	      newNameTextBox.setColor("red");    
            }
    	  }
  	});	     
      }
      add(contentPanel);
    }
	
    private void handleOkClick(YoungAndroidSourceNode projectRootNode) {
      String newFormName = newNameTextBox.getText();
      if (validate(newFormName)) {
  	allowCheckPoint();
        hide();
        removeForm(projectRootNode);
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

      // Check for reserved words
      if(TextValidators.isReservedName(newFormName)) {
        Window.alert(MESSAGES.reservedNameError());
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
    public void removeForm(final ProjectNode node) {
      if (deleteConfirmation()) {
        final Ode ode = Ode.getInstance();

      if (node instanceof YoungAndroidSourceNode) {
        // node could be either a YoungAndroidFormNode or a YoungAndroidBlocksNode.
        // Before we delete the form, we need to close both the form editor and the blocks editor
        // (in the browser).
        final String qualifiedFormName = ((YoungAndroidSourceNode) node).getQualifiedName();
        final String formFileId = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
        final String blocksFileId = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);
        final String yailFileId = YoungAndroidYailNode.getYailFileId(qualifiedFormName);
        final long projectId = node.getProjectId();
        String fileIds[] = new String[2];
        fileIds[0] = formFileId;
        fileIds[1] = blocksFileId;
        ode.getEditorManager().closeFileEditors(projectId, fileIds);

        // When we tell the project service to delete either the form (.scm) file or the blocks
        // (.bky) file, it will delete both of them, and also the yail (.yail) file.
        ode.getProjectService().deleteFile(ode.getSessionId(), projectId, node.getFileId(), new OdeAsyncCallback<Long>(
        // message on failure
        MESSAGES.deleteFileError()) {
          @Override
          public void onSuccess(Long date) {
            // Remove all related nodes (form, blocks, yail) from the project.
            Project project = getProject(node);
            for (ProjectNode sourceNode : node.getProjectRoot().getAllSourceNodes()) {
              if (sourceNode.getFileId().equals(formFileId) ||
                sourceNode.getFileId().equals(blocksFileId) ||
                sourceNode.getFileId().equals(yailFileId)) {
                project.deleteNode(sourceNode);
              }
            }
            ode.getDesignToolbar().removeScreen(project.getProjectId(), ((YoungAndroidSourceNode) node).getFormName());
            ode.updateModificationDate(projectId, date);
            executeNextCommand(node);
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            executionFailedOrCanceled();
          }
        });
      } else {  // asset file
        ode.getProjectService().deleteFile(ode.getSessionId(), node.getProjectId(), node.getFileId(), new OdeAsyncCallback<Long>(MESSAGES.deleteFileError()) {
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
      }
    } else {
      executionFailedOrCanceled();
    }
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
    public void allowCheckPoint(){
      if (cb.getValue() == true) {
        ProjectRootNode projectRootNode1 = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
        if (projectRootNode1 != null) {
    	  ChainableCommand cmd = new SaveAllEditorsCommand(
          new SaveScreenCheckpointCommand(true));
    	  cmd.startExecuteChain(Tracking.PROJECT_ACTION_CHECKPOINT_YA, projectRootNode1);
        }		
      }
    }
  }
}
