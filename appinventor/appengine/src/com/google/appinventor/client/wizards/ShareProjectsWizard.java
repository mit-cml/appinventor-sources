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
import com.google.gwt.core.client.GWT;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.Validator;
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

import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.Window;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Wizard for sharing Young Android projects.
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

  private List<Long> projectsToShareIds = new ArrayList<>();
  private List<String> projectsToShareNames = new ArrayList<>();
  private List<String> readOnlyAccessList = new ArrayList<>();
  private List<String> isSharedAllList = new ArrayList<>();
  private List<Long> shareIds = new ArrayList<>();
  private Ode mainOde;
  private boolean isSharedAll;

  /**
   * Creates a share YoungAndroid project wizard.
   */
  public ShareProjectsWizard() {

    bindUI();
    userNameTextBox.setValidator(new Validator() {
      @Override
      public boolean validate(String value) {
        errorMessage = TextValidators.isValidEmailList(value);
        userNameTextBox.setErrorMessage(errorMessage);
        if (errorMessage.length() > 0) {
          shareButton.setEnabled(false);
          return false;
        }
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
    linkForProject.setVisible(false);

    // TODO ADD THE ? ICON
    // PropertyHelpWidget themeHelpWidget = new PropertyHelpWidget(theme);
    // PropertyHelpWidget blocksHelpWidget = new PropertyHelpWidget(toolkit);
    // horizontalThemePanel.add(themeHelpWidget);
    // horizontalBlocksPanel.add(blocksHelpWidget);
    
    this.mainOde = Ode.getInstance();
    if (this.mainOde.getCurrentView() == Ode.PROJECTS) {
      List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      List<ProjectFolder> selectedFolders = ProjectListBox.getProjectListBox().getProjectList().getSelectedFolders();
      if (selectedProjects.size() > 0 || selectedFolders.size() > 0) {
        for (Project project : selectedProjects) {
          this.projectsToShareIds.add(project.getProjectId());
          this.projectsToShareNames.add(project.getProjectName());
        }
        for (ProjectFolder f : selectedFolders) {
          for (Project project : f.getNestedProjects()) {
            this.projectsToShareIds.add(project.getProjectId());
            this.projectsToShareNames.add(project.getProjectName());
          }
        }
      } else {
        // The user can select a project to resolve the
        // error.
        ErrorReporter.reportInfo(MESSAGES.noProjectSelectedToShare());
      }
    } else { //We are sharing a project in the designer view
      Project currentProject = this.mainOde.getProjectManager().getProject(Ode.getInstance().getCurrentYoungAndroidProjectId());
      long currentProjectId = this.mainOde.getCurrentYoungAndroidProjectId();
      this.projectsToShareIds.add(currentProjectId);
      this.projectsToShareNames.add(currentProject.getProjectName());
    }
    for (long project : this.projectsToShareIds) {
      this.mainOde.getAccessInfo(project, new OdeAsyncCallback<HashMap<String, List<String>>>() {
        @Override
        public void onSuccess(HashMap<String, List<String>> result) {
          readOnlyAccessList.addAll(result.get("readonly"));
          userNameTextBox.setText(String.join(", ", readOnlyAccessList));
          shareIds.addAll(result.get("shareIds").stream()
                  .map(Long::parseLong)
                  .collect(Collectors.toList()));
          isSharedAllList.addAll(result.get("isSharedAll"));
          isSharedAll = isSharedAllList.stream()
                              .allMatch(s -> s == "true");
          checkBoxShareAll.setValue(isSharedAll);
        }
      });
    }
    projectNameLabel.setText(String.join(", ", this.projectsToShareNames));
  }

  @UiHandler("checkBoxShareAll")
  protected void toggleShareAll(ClickEvent e) {
    // anything we wanna do?
  }

  @UiHandler("checkBoxSendEmail")
  protected void toggleSendEmail(ClickEvent e) {
    // anything we wanna do? check whether any emails are given?
  }

  public static native void copyTextToClipboard(String text) /*-{
    var textArea = document.createElement("textarea");
    //
    // *** This styling is an extra step which is likely not required. ***
    //
    // Why is it here? To ensure:
    // 1. the element is able to have focus and selection.
    // 2. if element was to flash render it has minimal visual impact.
    // 3. less flakyness with selection and copying which **might** occur if
    //    the textarea element is not visible.
    //
    // The likelihood is the element won't even render, not even a flash,
    // so some of these are just precautions. However in IE the element
    // is visible whilst the popup box asking the user for permission for
    // the web page to copy to the clipboard.
    //

    // Place in top-left corner of screen regardless of scroll position.
    textArea.style.position = 'fixed';
    textArea.style.top = 0;
    textArea.style.left = 0;

    // Ensure it has a small width and height. Setting to 1px / 1em
    // doesn't work as this gives a negative w/h on some browsers.
    textArea.style.width = '2em';
    textArea.style.height = '2em';

    // We don't need padding, reducing the size if it does flash render.
    textArea.style.padding = 0;

    // Clean up any borders.
    textArea.style.border = 'none';
    textArea.style.outline = 'none';
    textArea.style.boxShadow = 'none';

    // Avoid flash of white box if rendered for any reason.
    textArea.style.background = 'transparent';


    textArea.value = text;

    document.body.appendChild(textArea);

    textArea.select();

    try {
        var successful = document.execCommand('copy');
    } catch (err) {
        console.log('Unable to copy');
    }
    document.body.removeChild(textArea);
  }-*/;


  @UiHandler("copyButton")
  protected void copyShareLink(ClickEvent e) {
    String result = this.shareIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
    String ourURL = Window.Location.getHref();
    Integer hashtagIdx = ourURL.indexOf("#");
    String locale = Window.Location.getParameter("locale");
    LOG.info("other way of linking " + Window.Location.createUrlBuilder().setParameter("shared", result).buildString() + " with project id being " + Window.Location.getHash());
    if (hashtagIdx == -1) {
      ourURL += (locale != null ? "&" : "?") + "shared="+result;
    } else {
      ourURL = ourURL.substring(0, hashtagIdx) + (locale != null ? "&" : "?") + "shared=" + result;
    }

    LOG.info("Here is the link: " + ourURL);

    copyTextToClipboard(ourURL);

    linkForProject.setVisible(true);
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
      shareProjects(this.projectsToShareIds, validUsers, true);
    }
  }

  private List<String> checkUsers() {
    String[] users = userNameTextBox.getText().split(",");
    List<String> validUsers = new ArrayList<String>();
    for (String user: users) {
      // check that users are valid
      user = user.trim();
      TextValidators.UserStatus status = TextValidators.checkNewUserName(user);
      if (status == TextValidators.UserStatus.VALID) {
        validUsers.add(user);
      } else {
        String errorMessage = TextValidators.getErrorMessageForUser(userNameTextBox.getText());
        if (!errorMessage.isEmpty()) {
          userNameTextBox.setErrorMessage(errorMessage);
        } else {
          userNameTextBox.setErrorMessage("There has been an unexpected error validating the users.");
        }
        break;
      }
    }
    return validUsers;
  }

  public void shareProjects(List<Long> projectIDs, List<String> users, Boolean hide) {
    // TODO when do we change access url?
    // get checkbox value for share all
    Long projectID = projectIDs.get(0);
    Boolean shareAll = checkBoxShareAll.getValue();
    OdeAsyncCallback<Void> callback =
        new OdeAsyncCallback<Void>(
          // failure message
          MESSAGES.shareProjectError()) {
          @Override
          public void onSuccess(Void result) {
            if (hide) {
              shareDialog.hide();
            }
            // }  
          }
    };
    this.mainOde.getProjectService().updateProjectPermissions(projectID, shareAll, users, callback);
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
