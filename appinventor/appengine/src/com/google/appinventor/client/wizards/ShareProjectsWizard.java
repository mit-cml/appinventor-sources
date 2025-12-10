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
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.appinventor.client.wizards.ShareProjectUser;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.ShareResponse;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.Window;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
  // @UiField protected Label projectNameLabel;
  @UiField protected LabeledTextBox userNameTextBox;
  @UiField protected ListBox permissionDropdown;
  @UiField protected ListBox permissionDropdownForAll;
  @UiField protected Button okButton;
  @UiField protected FlowPanel usersContainer;
  @UiField protected CheckBox checkBoxShareAll;
  @UiField protected CheckBox checkBoxSendEmail;
  @UiField protected Button copyButton;
  @UiField protected Button topInvisible;
  @UiField protected Button bottomInvisible;
  @UiField protected Label linkForProject;
  @UiField protected Label updatedUsersLabel;

  private List<Long> projectsToShareIds = new ArrayList<>();
  private Long projectToShareId = 0L;
  private String projectToShareName = "";
  private Long shareId = 0L;
  private List<String> readOnlyAccessList = new ArrayList<>();
  private Ode mainOde;
  private Boolean isSharedAll;
  private HashMap<Integer, List<String>> accessInfo = new HashMap<>();
  private List<ShareProjectUser> usersContainerElements = new ArrayList<>();

  HashMap<Integer, Integer> permissionMap = new HashMap<Integer, Integer>() {{
        put(1, 0);
        put(3, 1);
        put(5, 2);
    }};

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

  public void handlePermissionChange () {
      updatedUsersLabel.setVisible(true);
  }

  public void bindUI() {
    ShareProjectsWizardUiBinder uibinder = GWT.create(ShareProjectsWizardUiBinder.class);
    uibinder.createAndBindUi(this);
  }

  public void show() {
    shareDialog.center();
    userNameTextBox.setFocus(true);
    linkForProject.setVisible(false);
    permissionDropdownForAll.setSelectedIndex(2);

    // TODO ADD THE ? ICON
    // PropertyHelpWidget themeHelpWidget = new PropertyHelpWidget(theme);
    // PropertyHelpWidget blocksHelpWidget = new PropertyHelpWidget(toolkit);
    // horizontalThemePanel.add(themeHelpWidget);
    // horizontalBlocksPanel.add(blocksHelpWidget);

    this.mainOde = Ode.getInstance();
    if (this.mainOde.getCurrentView() == Ode.PROJECTS) {
        List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
        if (selectedProjects.size() == 1) {
            this.projectToShareId = selectedProjects.get(0).getProjectId();
            this.projectToShareName = selectedProjects.get(0).getProjectName();
        } else {
        // The user can select a project to resolve the
        // error.
            ErrorReporter.reportInfo(MESSAGES.noProjectSelectedToShare());
        }
    } else { //We are sharing a project in the designer view
      Project currentProject = this.mainOde.getProjectManager().getProject(Ode.getInstance().getCurrentYoungAndroidProjectId());
      this.projectToShareId = this.mainOde.getCurrentYoungAndroidProjectId();
      this.projectToShareName = currentProject.getProjectName();
    }
    this.projectsToShareIds.add(this.projectToShareId);
    this.mainOde.getAccessInfo(this.projectToShareId, new OdeAsyncCallback<HashMap<Integer, List<String>>>() {
        @Override
        public void onSuccess(HashMap<Integer, List<String>> result) {
          accessInfo = result;
          LOG.info("access" + result.toString());
          if (result.containsKey(3)) {
            readOnlyAccessList.addAll(result.get(3));
            // userNameTextBox.setText(String.join(", ", readOnlyAccessList));
          }
          // update the users container;
          updateUsersContainer(result);
          // isSharedAll = (result.containsKey(4) && !result.get(4).isEmpty()) || (result.containsKey(3) && result.get(3).contains("ALL")) || result.toString().contains("ALL");
          // LOG.info("is shared all: " + isSharedAll);
          // checkBoxShareAll.setValue(isSharedAll);
        }
      });
    
    this.mainOde.getShareLink(this.mainOde.getUser().getUserEmail(), this.projectToShareId, new OdeAsyncCallback<Long>() {
        @Override
        public void onSuccess(Long result) {
          LOG.info("link" + result.toString());
          shareId = result;
        }
      });
    shareDialog.setText("Share Project " + this.projectToShareName);
    // projectNameLabel.setText(this.projectToShareName);
  }

  private void updateUsersContainer(HashMap<Integer, List<String>> accessInfo) {
    for (Map.Entry<Integer, List<String>> entry : accessInfo.entrySet()) {
        Integer permission = entry.getKey();
        LOG.info("permission index " + permission);
        List<String> users = entry.getValue();
        for (String user: users) {
            LOG.info("user " + user);
            if (user != "" && permission != 0 && permission != 4 && user != "ALL") { // skip ALL entry
              ShareProjectUser userPerm = new ShareProjectUser(user, permission.toString(), this::handlePermissionChange);
              usersContainerElements.add(userPerm);
              usersContainer.add(userPerm);
            } else if (user == "ALL") {
              LOG.info("should become all");
              isSharedAll = true;
              checkBoxShareAll.setValue(isSharedAll);
              permissionDropdownForAll.setSelectedIndex(permissionMap.get(permission));
            }
        }
    }
  }

  @UiHandler("checkBoxShareAll")
  protected void toggleShareAll(ClickEvent e) {
    // anything we wanna do?
    handlePermissionChange();
  }

  @UiHandler("checkBoxSendEmail")
  protected void toggleSendEmail(ClickEvent e) {
    // anything we wanna do? check whether any emails are given?
    handlePermissionChange();
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
    String result = this.shareId.toString();
    String ourURL = Window.Location.getHref();
    Integer hashtagIdx = ourURL.indexOf("#");
    String locale = Window.Location.getParameter("locale");
    if (hashtagIdx == -1) {
      ourURL += (locale != null ? "&" : "?") + "shared="+result;
    } else {
      ourURL = ourURL.substring(0, hashtagIdx) + (locale != null ? "&" : "?") + "shared=" + result;
    }

    copyTextToClipboard(ourURL);
    linkForProject.setVisible(true);
  }

  @UiHandler("cancelButton")
  protected void cancelAdd(ClickEvent e) {
    shareDialog.hide();
  }

  @UiHandler("shareButton")
  protected void shareProject(ClickEvent e) {
    HashMap<Integer, List<String>> newAccessInfo = new HashMap<>();
    for (ShareProjectUser user : usersContainerElements) {
      Integer perm = user.getPermInteger();
      LOG.info("newEmail: " + user.getEmail() + " " + perm.toString());

      if (newAccessInfo.get(perm) != null) { // Check if the key exists
        List<String> emails = newAccessInfo.get(perm);
        emails.add(user.getEmail());
        newAccessInfo.put(perm, emails);
      } else {
        // Handle the case where the key doesn't exist yet
        List<String> emails = new ArrayList<>();
        emails.add(user.getEmail());
        newAccessInfo.put(perm, emails);
      }
    }

    LOG.info("access: " + newAccessInfo.toString());
    Boolean updatedWithOthers = false;
    Integer permForAll = Integer.parseInt(permissionDropdownForAll.getSelectedValue());
    Boolean shareAll = checkBoxShareAll.getValue();

    for (Map.Entry<Integer, List<String>> entry : newAccessInfo.entrySet()) {
      Integer perm = entry.getKey();
      List<String> emails = entry.getValue();
      List<String> validUsers = this.checkUsers(emails);
      LOG.info("share emails " + emails.toString());
      if (emails.size() == validUsers.size()) {
        if (shareAll && permForAll == perm) {
          validUsers.add("ALL");
          updatedWithOthers = true;
        }
        LOG.info("share emails " + validUsers.toString() + " with perm " + perm);
        shareProjects(this.projectsToShareIds, validUsers, perm, true);
      }
    }
    
    if (shareAll && !updatedWithOthers) {
      shareProjects(this.projectsToShareIds, (new ArrayList<>(Arrays.asList("ALL"))), permForAll, true);
    }


    // List<String> users = new ArrayList<>();

    // for (ShareProjectUser user : usersContainerElements) {
    //   if (user.getPermInteger() == 3) {
    //     users.add(user.getEmail());
    //   }
    // }

    // List<String> validUsers = this.checkUsers(users);

    // if (users.size() == validUsers.size()) {
    //   shareProjects(this.projectsToShareIds, validUsers, 3, true);
    // }
  }

  @UiHandler("okButton")
  protected void updatePermission(ClickEvent e) {
    List<String> users = Arrays.asList(userNameTextBox.getText().split(","));
    List<String> validUsers = this.checkUsers(users);

    if (users.size() == validUsers.size() && users.size() == 1) {
      ShareProjectUser user = new ShareProjectUser(validUsers.get(0), permissionDropdown.getSelectedValue(), this::handlePermissionChange);
      usersContainerElements.add(user);
      usersContainer.add(user);
      userNameTextBox.setText("");
      permissionDropdown.setSelectedIndex(0);
    //   permissionDropdown.setValue(0, "Edit");
      handlePermissionChange();
    }
  }

  private List<String> checkUsers(List<String> users) {
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

  private void showMessageForResponse(ShareResponse response) {
    String message = "";
    switch(response.getStatus()) {
      case SHARED:
        message = MESSAGES.projectSharedSuccessfully();
        break;
      case SELF_SHARE:
        message = MESSAGES.cannotSelfShareProject();
        break;
      case UNKNOWN_USER:
      case INVALID_USER:
        message = MESSAGES.sharedUserDoesNotExist();
        break;
      case ALREADY_SHARED:
        message = MESSAGES.alreadySharedProject();
        break;
      case UNAUTHORIZED:
        message = MESSAGES.sharingUnauthorized();
        break;
    }
    final DecoratedPopupPanel panel = new DecoratedPopupPanel(true);
    Label label = new Label();
    label.setText(message);
    panel.setWidget(label);
    panel.setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        panel.setPopupPosition((Window.getClientWidth() - offsetWidth) >> 1, Window.getScrollTop());
      }
    });
  }

  public void shareProjects(List<Long> projectIDs, List<String> users, Integer perm, Boolean hide) {
    // TODO when do we change access url?
    // get checkbox value for share all
    Long projectID = projectIDs.get(0);
    
    OdeAsyncCallback<List<ShareResponse>> removeCallback =
        new OdeAsyncCallback<List<ShareResponse>>(
          // failure message
          MESSAGES.shareProjectError()) {
          @Override
          public void onSuccess(List<ShareResponse> result) {
            if (hide) {
              shareDialog.hide();
            }
            LOG.info("results: " + result.toString());
            for (ShareResponse r: result) {
              LOG.info("result: " + r.getStatus());
              switch (r.getStatus()) {
                case SHARED:
                  Project project = Ode.getInstance().getProjectManager().getProject(r.getProjectId());
                  project.setShared(true);
                  ProjectListBox.getProjectListBox().getProjectList().onProjectSharedOrUnshared();
                default:
                  showMessageForResponse(r);
                  break;
              }
            }
          }
    };
    List<String> addUsers = new ArrayList<>();
    List<String> removeUsers = new ArrayList<>();
    
    
    if (accessInfo.get(perm) != null && perm != 5) {
        List<String> prevUsers = accessInfo.get(perm);
        for (String user : users) {
            if (!prevUsers.contains(user)) {
                addUsers.add(user);
            }
        }
        for (String user: prevUsers) {
            if (!users.contains(user)) {
                removeUsers.add(user);
            }
        }
    } else {
      if (perm == 5) {
        removeUsers.addAll(users);
      } else {
        addUsers.addAll(users);
      }
    }
    OdeAsyncCallback<List<ShareResponse>> callback =
        new OdeAsyncCallback<List<ShareResponse>>(
          // failure message
          MESSAGES.shareProjectError()) {
          @Override
          public void onSuccess(List<ShareResponse> result) {
            LOG.info("giving access");
            // mainOde.getProjectService().shareProject(mainOde.getUser().getUserId(), mainOde.getUser().getUserEmail(), projectID, removeUsers, 5, removeCallback);
            mainOde.getProjectService().shareProject(mainOde.getUser().getUserId(), mainOde.getUser().getUserEmail(), projectID, addUsers, perm, checkBoxSendEmail.getValue(), removeCallback);
          }
    };
    LOG.info("add " + addUsers.toString());
    LOG.info("remove " + removeUsers.toString());
    LOG.info("giving access " + perm);
    // this.mainOde.getProjectService().shareProject(this.mainOde.getUser().getUserId(), this.mainOde.getUser().getUserEmail(), projectID, addUsers, perm, callback);
    this.mainOde.getProjectService().shareProject(this.mainOde.getUser().getUserId(), this.mainOde.getUser().getUserEmail(), projectID, removeUsers, 5, false, callback);
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