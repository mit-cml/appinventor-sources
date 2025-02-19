// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.utils.Uploader;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.component.Component;

import com.google.gwt.core.client.GWT;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

public class ComponentUploadWizard extends Wizard {
  private static final String COMPONENT_ARCHIVE_EXTENSION = ".aix";

  public ComponentUploadWizard() {
    super(MESSAGES.componentUploadWizardCaption(), true, false);

    final FileUpload uploadWiget = new FileUpload();
    uploadWiget.getElement().setAttribute("accept", COMPONENT_ARCHIVE_EXTENSION);
    uploadWiget.setName(ServerLayout.UPLOAD_COMPONENT_ARCHIVE_FORM_ELEMENT);

    VerticalPanel panel = new VerticalPanel();
    panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    panel.add(uploadWiget);

    addPage(panel);

    setStylePrimaryName("ode-DialogBox");

    initFinishCommand(new Command() {
      @Override
      public void execute() {
        if (!uploadWiget.getFilename().endsWith(COMPONENT_ARCHIVE_EXTENSION)) {
          Window.alert(MESSAGES.notComponentArchiveError());
          return;
        }

        String url = ServerLayout.getModuleBaseURL() +
          ServerLayout.UPLOAD_SERVLET + "/" +
          ServerLayout.UPLOAD_COMPONENT + "/" +
          trimLeadingPath(uploadWiget.getFilename());

        Uploader.getInstance().upload(uploadWiget, url,
          new OdeAsyncCallback<UploadResponse>() {
            @Override
            public void onSuccess(UploadResponse uploadResponse) {
              Component component = Component.valueOf(uploadResponse.getInfo());
              ErrorReporter.reportInfo("Uploaded successfully");
            }
          });
      }

      private String trimLeadingPath(String filename) {
        // Strip leading path off filename.
        // We need to support both Unix ('/') and Windows ('\\') separators.
        return filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);
      }
    });
  }

  @Override
  public void show() {
    super.show();
    setPagePanelHeight(40);
    setPixelSize(320, 40);
    center();
  }
}
