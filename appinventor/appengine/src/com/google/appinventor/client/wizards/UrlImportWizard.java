// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.HashSet;
import java.util.Set;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.project.FolderNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.util.Base64Util;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class UrlImportWizard extends Wizard {
  private final Set<OnImportListener> listeners = new HashSet<OnImportListener>();

  public interface OnImportListener {
    void onSuccess(byte[] content);
  }

  public UrlImportWizard(final FolderNode assetsFolder, OnImportListener listener) {
    super(MESSAGES.urlImportWizardCaption(), true, false);

    listeners.add(listener);

    final FlowPanel urlPanel = createUrlPanel();
    FlowPanel panel = new FlowPanel();
    panel.setStyleName("ode-VerticalLayout");
    panel.add(urlPanel);

    addPage(panel);

    getConfirmButton().setText("Import");

    setPagePanelHeight(150);
    setPixelSize(200, 150);
    setStylePrimaryName("ode-DialogBox");

    initFinishCommand(new Command() {
      @Override
      public void execute() {
        Ode ode = Ode.getInstance();
        final long projectId = ode.getCurrentYoungAndroidProjectId();
        final Project project = ode.getProjectManager().getProject(projectId);

        TextBox urlTextBox = (TextBox) urlPanel.getWidget(1);
        String url = urlTextBox.getText();
        if (url.trim().isEmpty()) {
          Window.alert(MESSAGES.noUrlError());
          return;
        }

        ode.getProjectService().importMedia(ode.getSessionId(), projectId, url, true, new OdeAsyncCallback<TextFile>() {
          @Override
          public void onSuccess(TextFile file) {
            ProjectNode node = new YoungAndroidAssetNode(assetsFolder.getFileId(), file.getFileName().replaceFirst("assets/", ""));
            project.addNode(assetsFolder, node);
            byte[] content = Base64Util.decodeLines(file.getContent());
            for (OnImportListener l : listeners) {
              l.onSuccess(content);
            }
            listeners.clear();
          }
        });
      }
    });
  }

  public void addImportListener(OnImportListener listener) {
    listeners.add(listener);
  }

  private static FlowPanel createUrlPanel() {
    TextBox urlTextBox = new TextBox();
    urlTextBox.setWidth("100%");
    FlowPanel panel = new FlowPanel();
    panel.setStyleName("ode-VerticalLayout");
    panel.add(new Label("Url:"));
    panel.add(urlTextBox);
    return panel;
  }
}
