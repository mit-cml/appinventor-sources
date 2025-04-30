// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.logging.Logger;

/**
 * Wizard for uploading previously archived (downloaded) projects.
 *
 */
public class ProjectUploadWizard {
  interface ProjectUploadWizardUiBinder extends UiBinder<Dialog, ProjectUploadWizard> {}
  private static final Logger LOG = Logger.getLogger(ProjectUploadWizard.class.getName());

  private static final ProjectUploadWizardUiBinder uibinder =
      GWT.create(ProjectUploadWizardUiBinder.class);

  @UiField Dialog uploadDialog;
  @UiField FileUpload upload;
  @UiField Button okButton;
  @UiField Button cancelButton;

  // Project archive extension
  private static final String PROJECT_ARCHIVE_EXTENSION = ".aia";

  /**
   * Creates a new project upload wizard.
   */
  public ProjectUploadWizard() {
    LOG.warning("Create ProjectUploadWizard");
    // Initialize UI
    uibinder.createAndBindUi(this);
    upload.setName(ServerLayout.UPLOAD_PROJECT_ARCHIVE_FORM_ELEMENT);
    upload.getElement().setAttribute("accept", PROJECT_ARCHIVE_EXTENSION);
  }

  public void show() {
    uploadDialog.center();
  }

  @UiHandler("cancelButton")
  void cancelUpload(ClickEvent e) {
    uploadDialog.hide();
  }


  @UiHandler("okButton")
  void executeUpload(ClickEvent e) {
    String filename = upload.getFilename();
    if (filename.endsWith(PROJECT_ARCHIVE_EXTENSION)) {
      // Strip extension and leading path off filename. We need to support both Unix ('/') and
      // Windows ('\\') path separators. File.pathSeparator is not available in GWT.
      filename = filename.substring(0, filename.length() - PROJECT_ARCHIVE_EXTENSION.length()).
                     substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);

      // Make sure the project name is legal and unique.
      if (TextValidators.checkNewProjectName(filename, true)
              != TextValidators.ProjectNameStatus.SUCCESS) {

        // Show Dialog Box and rename the project
        new RequestNewProjectNameWizard(new RequestProjectNewNameInterface() {
          @Override
          public void getNewName(String name) {
            upload(upload, name);
          }
        }, filename, true);
        uploadDialog.hide();
      } else {
        upload(upload, filename);
        uploadDialog.hide();
      }
    } else {
      Window.alert(MESSAGES.notProjectArchiveError());
    }
  }

  private void upload(FileUpload upload, String filename) {
    String uploadUrl = ServerLayout.getModuleBaseURL() + ServerLayout.UPLOAD_SERVLET + "/"
        + ServerLayout.UPLOAD_PROJECT + "/" + filename;
    Uploader.getInstance().upload(upload, uploadUrl,
        new OdeAsyncCallback<UploadResponse>(
        // failure message
        MESSAGES.projectUploadError()) {
          @Override
          public void onSuccess(UploadResponse uploadResponse) {
            switch (uploadResponse.getStatus()) {
              case SUCCESS:
                String info = uploadResponse.getInfo();
                UserProject userProject = UserProject.valueOf(info);
                Ode ode = Ode.getInstance();
                Project uploadedProject = ode.getProjectManager().addProject(userProject);
                ode.openYoungAndroidProjectInDesigner(uploadedProject);
                break;
              case NOT_PROJECT_ARCHIVE:
                // This may be a "severe" error; but in the
                // interest of reducing the number of red errors, the 
                // line has been changed to report info not an error.
                // This error is triggered when the user attempts to
                // upload a zip file that is not a project.
                ErrorReporter.reportInfo(MESSAGES.notProjectArchiveError());
                break;
              default:
                ErrorReporter.reportError(MESSAGES.projectUploadError());
                break;
            }
          }
      });
  }
}
