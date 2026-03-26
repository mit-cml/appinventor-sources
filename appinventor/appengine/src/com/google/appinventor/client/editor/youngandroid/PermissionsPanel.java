// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.project.ComponentPermission;
import com.google.appinventor.shared.rpc.project.PermissionMetadata;
import com.google.appinventor.client.wizards.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

import java.util.logging.Logger;

/**
 * A dialog that shows permissions required by components in the project.
 */
public class PermissionsPanel {
  private static final Logger LOG = Logger.getLogger(PermissionsPanel.class.getName());

  interface PermissionsPanelUiBinder extends UiBinder<Widget, PermissionsPanel> {}
  private static final PermissionsPanelUiBinder uiBinder = GWT.create(PermissionsPanelUiBinder.class);

  @UiField
  Dialog permissionsDialog;

  @UiField
  FlexTable permissionsTable;

  @UiField
  Button closeButton;

  private final long projectId;

  public PermissionsPanel(long projectId) {
    this.projectId = projectId;
    uiBinder.createAndBindUi(this);
    
    permissionsDialog.setCaption("Project Permissions");
    permissionsDialog.setAutoHideEnabled(true);
    permissionsDialog.setGlassEnabled(true);
    permissionsDialog.setModal(true);

    setupTableHeaders();
    loadPermissionData();
  }

  private void setupTableHeaders() {
    permissionsTable.setText(0, 0, "Component");
    permissionsTable.setText(0, 1, "Permissions");
    permissionsTable.setText(0, 2, "Min SDK");
    permissionsTable.getRowFormatter().addStyleName(0, "ode-PermissionsTableHeader");
  }

  private void loadPermissionData() {
    Ode.getInstance().getProjectService().getProjectPermissionMetadata(projectId,
        new OdeAsyncCallback<PermissionMetadata>("Error loading permission metadata") {
          @Override
          public void onSuccess(PermissionMetadata result) {
            updateUI(result);
          }
        });
  }

  private void updateUI(PermissionMetadata metadata) {
    int row = 1;
    permissionsTable.removeAllRows();
    setupTableHeaders();

    for (ComponentPermission cp : metadata.getComponents()) {
      permissionsTable.setText(row, 0, cp.getComponentName());
      
      StringBuilder perms = new StringBuilder();
      for (String p : cp.getPermissions()) {
        if (perms.length() > 0) perms.append(", ");
        perms.append(p);
      }
      permissionsTable.setText(row, 1, perms.toString());
      permissionsTable.setText(row, 2, String.valueOf(cp.getMinSdkVersion()));
      
      row++;
    }
    
    if (row == 1) {
      permissionsTable.setHTML(1, 0, "<center colspan=\"3\">No special permissions required</center>");
    }
  }

  public void show() {
    permissionsDialog.center();
  }

  @UiHandler("closeButton")
  void handleClose(ClickEvent e) {
    permissionsDialog.hide();
  }
}
