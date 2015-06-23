// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Wizard for uploading previously archived (downloaded) projects.
 *
 */
public class ProjectUploadWizard extends Wizard {
  // Project archive extension
  private static final String PROJECT_ARCHIVE_EXTENSION = ".aia";

  /**
   * Creates a new project upload wizard.
   */
  public ProjectUploadWizard() {
    super(MESSAGES.projectUploadWizardCaption(), true, false);

    // Initialize UI
    final FileUpload upload = new FileUpload();
    upload.setName(ServerLayout.UPLOAD_PROJECT_ARCHIVE_FORM_ELEMENT);
    setStylePrimaryName("ode-DialogBox");
    VerticalPanel panel = new VerticalPanel();
    panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    panel.add(upload);
    addPage(panel);

    // Create finish command (upload a project archive)
    initFinishCommand(new Command() {
      @Override
      public void execute() {
        String filename = upload.getFilename();
        if (filename.endsWith(PROJECT_ARCHIVE_EXTENSION)) {
          // Strip extension and leading path off filename. We need to support both Unix ('/') and
          // Windows ('\\') path separators. File.pathSeparator is not available in GWT.
          filename = filename.substring(0, filename.length() - PROJECT_ARCHIVE_EXTENSION.length()).
              substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);

          // Make sure the project name is legal and unique.
          if (!TextValidators.checkNewProjectName(filename)) {
            return;
          }

          String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.UPLOAD_SERVLET + "/" +
              ServerLayout.UPLOAD_PROJECT + "/" + filename;
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
        } else {
          Window.alert(MESSAGES.notProjectArchiveError());
          center();
        }
      }
    });
  }

  @Override
  public void show() {
    super.show();
    // Wizard size (having it resize between page changes is quite annoying)
    int width = 320;
    int height = 40;
    this.center();

    setPixelSize(width, height);
    super.setPagePanelHeight(40);
  }
}
