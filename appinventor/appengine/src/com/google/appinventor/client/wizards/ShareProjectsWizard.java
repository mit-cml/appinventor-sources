// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;


import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.wizards.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.Validator;
import com.google.appinventor.client.widgets.properties.PropertyHelpWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.ProjectService;
import com.google.gwt.user.client.ui.FlowPanel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Wizard for creating new Young Android projects.
 *
 * @author markf@google.com (Mark Friedman)
 */

public class ShareProjectsWizard {

  interface ShareProjectsWizardUiBinder extends UiBinder<Dialog, ShareProjectsWizard> {}

  private static final Logger LOG = Logger.getLogger(ShareProjectsWizard.class.getName());

  // users
  // shared with all
  // UI element for project name
  @UiField protected Dialog shareDialog;
  @UiField protected Button shareButton;
  @UiField protected Button cancelButton;
  @UiField protected Label projectNameLabel;
  @UiField protected LabeledTextBox userNameTextBox;
  @UiField protected FlowPanel usersContainer;
  @UiField protected CheckBox checkBoxShareAll;
  @UiField protected CheckBox checkBoxSendEmail;
  @UiField protected Button copyButton;
  @UiField protected Button topInvisible;
  @UiField protected Button bottomInvisible;
  @UiField protected Label linkForProject;

  private List<Long> projectsToShare = new ArrayList<>();
  private List<String> readOnlyAccessList = new ArrayList<>();
  private List<Long> shareIds = new ArrayList<>();
  private Ode mainOde;

  /**
   * Creates a new YoungAndroid project wizard.
   */
  public ShareProjectsWizard() {

    bindUI();
    userNameTextBox.setValidator(new Validator() {
      @Override
      public boolean validate(String value) {
        LOG.warning("error?: " + value);
        errorMessage = TextValidators.isValidEmailList(value);
        userNameTextBox.setErrorMessage(errorMessage);
        if (errorMessage.length() > 0) {
          shareButton.setEnabled(false);
          return false;
        }
        // errorMessage = TextValidators.getWarningMessages(value);
        shareButton.setEnabled(true);
        return true;
      }
      @Override
      public String getErrorMessage() {
        return errorMessage;
      }
    });
    userNameTextBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        // TODO DEPENDS ON HOW MUCH WE COPY GOOGLE DOCS
        if (keyCode == KeyCodes.KEY_ENTER) {
          shareButton.click();
        } else if (keyCode == KeyCodes.KEY_ESCAPE) {
          cancelButton.click();
        }
      }
    });

    userNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) { //Validate the text each time a key is lifted
        userNameTextBox.validate();
      }
    });
  }

  public void bindUI() {
    ShareProjectsWizardUiBinder uibinder = GWT.create(ShareProjectsWizardUiBinder.class);
    uibinder.createAndBindUi(this);
  }

  public void show() {
    shareDialog.center();
    userNameTextBox.setFocus(true);

    // TODO ADD THE ? ICON
    // PropertyHelpWidget themeHelpWidget = new PropertyHelpWidget(theme);
    // PropertyHelpWidget blocksHelpWidget = new PropertyHelpWidget(toolkit);
    // horizontalThemePanel.add(themeHelpWidget);
    // horizontalBlocksPanel.add(blocksHelpWidget);
    
    this.mainOde = Ode.getInstance();
    LOG.warning("open wizard: " + this.mainOde);
    if (this.mainOde.getCurrentView() == Ode.PROJECTS) {
      List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      List<ProjectFolder> selectedFolders = ProjectListBox.getProjectListBox().getProjectList().getSelectedFolders();
      if (selectedProjects.size() > 0 || selectedFolders.size() > 0) {
        for (Project project : selectedProjects) {
          this.projectsToShare.add(project.getProjectId());
        }
        for (ProjectFolder f : selectedFolders) {
          for (Project project : f.getNestedProjects()) {
            this.projectsToShare.add(project.getProjectId());
          }
        }
        // Show one confirmation window for selected projects.
        // if (deleteConfirmation(projectsToDelete)) {
        //   for (Project project : projectsToDelete) {
        //     project.moveToTrash();
        //   }
        //   for (ProjectFolder f : selectedFolders) {
        //     f.getParentFolder().removeChildFolder(f);
        //   }
        // }

      } else {
        // The user can select a project to resolve the
        // error.
        ErrorReporter.reportInfo(MESSAGES.noProjectSelectedToShare());
      }
    } else { //We are sharing a project in the designer view
      // List<Project> selectedProjects = new ArrayList<Project>();
      long currentProject = this.mainOde.getCurrentYoungAndroidProjectId();
      this.projectsToShare.add(currentProject);
      // if (deleteConfirmation(selectedProjects)) {
      //   currentProject.moveToTrash();
      //   //Add the command to stop this current project from saving
      // }
    }
    for (long project : this.projectsToShare) {
      this.mainOde.getAccessInfo(project, new OdeAsyncCallback<HashMap<String, List<String>>>() {
        @Override
        public void onSuccess(HashMap<String, List<String>> result) {
          readOnlyAccessList.addAll(result.get("readonly"));
          userNameTextBox.setText(String.join(", ", readOnlyAccessList));
          shareIds.addAll(result.get("shareIds").stream()
                  .map(Long::parseLong)
                  .collect(Collectors.toList()));
        }
      });
    }
    LOG.warning("project names: " + this.projectsToShare);
    projectNameLabel.setText(this.projectsToShare.toString());
  }

  @UiHandler("checkBoxShareAll")
  protected void toggleShareAll(ClickEvent e) {
    // anything we wanna do?
  }

  @UiHandler("checkBoxSendEmail")
  protected void toggleSendEmail(ClickEvent e) {
    // anything we wanna do? check whether any emails are given?
  }

  @UiHandler("copyButton")
  protected void copyShareLink(ClickEvent e) {
    String result = this.shareIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
    LOG.info("Here is the link: " + result);
    // anything we wanna do? check whether any emails are given?
    // String[] users = userNameTextBox.getText().split(",");
    // List<String> validUsers = this.checkUsers();

    // if (users.length == validUsers.size()) {
    //   String link = shareProjects(this.projectsToShare, validUsers, true);
    // }
    // this.mainOde.getProjectService().getProjectPermissions(project, shareAll, users, callback);
    // OdeAsyncCallback<Void> callback =
    //     new OdeAsyncCallback<Void>(
    //       // failure message
    //       MESSAGES.getLinkForProjectError()) {
    //       @Override
    //       public void onSuccess(String result) {
    //           LOG.info("Finished getting link for project");
    //           linkForProject.setText(result);
    //           linkForProject.setVisible(true);
    //         }  
    //       }
    // };
    // this.ode.getProjectService().getLinkForProject(project, callback);
  }

  @UiHandler("cancelButton")
  protected void cancelAdd(ClickEvent e) {
    shareDialog.hide();
  }

  @UiHandler("shareButton")
  protected void shareProject(ClickEvent e) {
    String[] users = userNameTextBox.getText().split(",");
    List<String> validUsers = this.checkUsers();

    if (users.length == validUsers.size()) {
      shareProjects(this.projectsToShare, validUsers, true);
    }
  }

  private List<String> checkUsers() {
    String[] users = userNameTextBox.getText().split(",");
    List<String> validUsers = new ArrayList<String>();
    for (String user: users) {
      // check that users are valid
      user = user.trim();
      LOG.info("Checking usernames: " + user);
      TextValidators.UserStatus status = TextValidators.checkNewUserName(user);
      if (status == TextValidators.UserStatus.VALID) {
        LOG.info("Valid user");
        validUsers.add(user);
      } else {
        LOG.info("Checking for error");
        String errorMessage = TextValidators.getErrorMessageForUser(userNameTextBox.getText());
        if (!errorMessage.isEmpty()) {
          LOG.info("Found error: " + errorMessage);
          userNameTextBox.setErrorMessage(errorMessage);
        } else {
          // errorMessage = TextValidators.getWarningMessages(userNameTextBox.getText());
          // if (!errorMessage.isEmpty()) {
          //   userNameTextBox.setErrorMessage(errorMessage);
          // } else {
            // Internationalize or change handling here.
          userNameTextBox.setErrorMessage("There has been an unexpected error validating the users.");
            // TODO(zamanova) also break???
          // }
        }
        break;
      }
    }
    return validUsers;
  }

  public void shareProjects(List<Long> projectIDs, List<String> users, Boolean hide) {
    // TODO when do we change access url?
    LOG.info("trying to share projects");
    // get checkbox value for share all
    Long projectID = projectIDs.get(0);
    Boolean shareAll = checkBoxShareAll.getValue();
    LOG.info("go whether to share all " + shareAll);
    // AtomicInteger remainingProjects = new AtomicInteger(projectIDs.size());
    // LOG.warning("got ode ?" + remainingProjects);
    // update project parameters with new users
    LOG.info("got ode ?" + this.mainOde);
    OdeAsyncCallback<Void> callback =
        new OdeAsyncCallback<Void>(
          // failure message
          MESSAGES.shareProjectError()) {
          @Override
          public void onSuccess(Void result) {
            // int left = remainingProjects.decrementAndGet();
            // LOG.info("shared one project left " + left);
            // if (left == 0) {
            //   LOG.info("Finished sharing all projects");
            if (hide) {
              shareDialog.hide();
            }
            // }  
          }
    };
    LOG.info("got callback ?");
    // LOG.warning("shre wizard: " + this.mainOde);
    LOG.info("got ode ?" + this.mainOde);
    // share the project on the back-end
    // for (long projectID : projectIDs) {
    LOG.info("sharing:" + projectID);
    this.mainOde.getProjectService().updateProjectPermissions(projectID, shareAll, users, callback);
    // }
    // shareDialog.hide();
  }

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
     shareButton.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
    userNameTextBox.setFocus(true);
  }
}
