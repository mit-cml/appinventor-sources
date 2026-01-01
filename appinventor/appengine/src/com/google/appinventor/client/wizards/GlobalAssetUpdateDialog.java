// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetService;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetServiceAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.logging.Logger;

public class GlobalAssetUpdateDialog extends DialogBox {

  interface GlobalAssetUpdateDialogUiBinder extends UiBinder<DialogBox, GlobalAssetUpdateDialog> {}

  private static final GlobalAssetUpdateDialogUiBinder uibinder = GWT.create(GlobalAssetUpdateDialogUiBinder.class);

  private static final Logger LOG = Logger.getLogger(GlobalAssetUpdateDialog.class.getName());

  @UiField HTML message;
  @UiField Button updateButton;
  @UiField Button cancelButton;

  private final GlobalAssetServiceAsync globalAssetService = GWT.create(GlobalAssetService.class);
  private final long projectId;
  private final GlobalAsset linkedAsset;
  private final GlobalAsset latestAsset;

  public GlobalAssetUpdateDialog(long projectId, GlobalAsset linkedAsset, GlobalAsset latestAsset) {
    super(false, true); // Auto-hide, modal
    this.projectId = projectId;
    this.linkedAsset = linkedAsset;
    this.latestAsset = latestAsset;

    setWidget(uibinder.createAndBindUi(this));
    setText(MESSAGES.globalAssetUpdateDialogTitle());
    setAnimationEnabled(true);
    setGlassEnabled(true);

    // Display message about the update
    String msg = MESSAGES.globalAssetUpdateMessage(linkedAsset.getFileName(),
        new java.util.Date(linkedAsset.getTimestamp()).toString(),
        new java.util.Date(latestAsset.getTimestamp()).toString());
    message.setHTML(msg);
  }

  @UiHandler("updateButton")
  void onUpdateClick(ClickEvent e) {
    LOG.info("Updating global asset " + linkedAsset.getFileName() + " in project " + projectId);
    // Call RPC to update the asset in the project
    globalAssetService.linkGlobalAssetToProject(projectId, latestAsset.getFileName(), latestAsset.getTimestamp(),
        new OdeAsyncCallback<Void>(MESSAGES.globalAssetUpdateError()) {
          @Override
          public void onSuccess(Void result) {
            Ode.getInstance().getEditorManager().getOpenProjectEditor(projectId).getFileEditor(linkedAsset.getFileName());
            // Refresh asset list to reflect the updated timestamp/status
            // TODO: Need a way to refresh AssetList from here
            // Ode.getInstance().getAssetManager().refreshAssetList();
            hide();
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            hide();
          }
        });
  }

  @UiHandler("cancelButton")
  void onCancelClick(ClickEvent e) {
    hide();
  }
}
