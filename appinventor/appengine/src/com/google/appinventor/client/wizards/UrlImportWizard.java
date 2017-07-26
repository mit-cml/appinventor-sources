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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UrlImportWizard extends Wizard {
  private final Set<OnImportListener> listeners = new HashSet<OnImportListener>();

  public interface OnImportListener {
    void onSuccess(byte[] content);
  }

  public UrlImportWizard(final FolderNode assetsFolder, OnImportListener listener) {
    super(MESSAGES.urlImportWizardCaption(), true, false);

    listeners.add(listener);

    final Grid urlGrid = createUrlGrid();
    VerticalPanel panel = new VerticalPanel();
    panel.add(urlGrid);

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

        TextBox urlTextBox = (TextBox) urlGrid.getWidget(1, 0);
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

  private static Grid createUrlGrid() {
    TextBox urlTextBox = new TextBox();
    urlTextBox.setWidth("100%");
    Grid grid = new Grid(2, 1);
    grid.setWidget(0, 0, new Label("Url:"));
    grid.setWidget(1, 0, urlTextBox);
    return grid;
  }
}
