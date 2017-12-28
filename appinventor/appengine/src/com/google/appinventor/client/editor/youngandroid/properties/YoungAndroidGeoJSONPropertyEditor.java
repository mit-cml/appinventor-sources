// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;
import com.google.appinventor.client.wizards.FileUploadWizard;
import com.google.appinventor.client.wizards.FileUploadWizard.FileUploadedCallback;
import com.google.appinventor.client.wizards.UrlImportWizard;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.FolderNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

public class YoungAndroidGeoJSONPropertyEditor extends AdditionalChoicePropertyEditor
    implements ProjectChangeListener {
  private final ListBox assetsList;
  private final ListWithNone choices;
  private final YoungAndroidAssetsFolder assetsFolder;

  public YoungAndroidGeoJSONPropertyEditor(final YaFormEditor editor) {
    Project project = Ode.getInstance().getProjectManager().getProject(editor.getProjectId());
    assetsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();
    project.addProjectChangeListener(this);

    VerticalPanel selectorPanel = new VerticalPanel();
    assetsList = new ListBox();
    assetsList.setVisibleItemCount(10);
    assetsList.setWidth("100%");
    selectorPanel.add(assetsList);

    choices = new ListWithNone(MESSAGES.noneCaption(), new ListWithNone.ListBoxWrapper() {
      @Override
      public void setSelectedIndex(int index) {
        assetsList.setSelectedIndex(index);
      }

      @Override
      public void removeItem(int index) {
        assetsList.removeItem(index);
      }

      @Override
      public String getItem(int index) {
        return assetsList.getItemText(index);
      }

      @Override
      public void addItem(String item) {
        assetsList.addItem(item);
      }
    });

    // Fill choices with assets.
    if (assetsFolder != null) {
      for (ProjectNode node : assetsFolder.getChildren()) {
        String lowerCaseName = node.getName();
        if (lowerCaseName.endsWith(".json") || lowerCaseName.endsWith(".geojson")) {
          choices.addItem(node.getName());
        }
      }
    }

    Button addButton = new Button(MESSAGES.addButton());
    addButton.setWidth("100%");
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        FileUploadedCallback callback = new FileUploadedCallback() {
          @Override
          public void onFileUploaded(FolderNode folderNode, FileNode fileNode) {
            choices.selectValue(fileNode.getName());
            closeAdditionalChoiceDialog(true);
          }
        };
        FileUploadWizard uploader = new FileUploadWizard(assetsFolder, callback);
        uploader.show();
      }
    });
    Button urlButton = new Button(MESSAGES.fromUrlButton());
    urlButton.setWidth("100%");
    urlButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        UrlImportWizard.OnImportListener callback = new UrlImportWizard.OnImportListener() {
          @Override
          public void onSuccess(byte[] content) {
            
          }
        };
        UrlImportWizard wizard = new UrlImportWizard(assetsFolder, callback);
        wizard.show();
      }
    });
    selectorPanel.add(addButton);
    selectorPanel.add(urlButton);
    selectorPanel.setWidth("100%");

    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        if (editor.isLoadComplete()) {
          finishInitialization();
        } else {
          DeferredCommand.addCommand(this);
        }
      }
    });

    initAdditionalChoicePanel(selectorPanel);
  }

  private void finishInitialization() {
    String value = property.getValue();
    if (value.equals("None") && !choices.containsValue(value)) {
      property.setValue("");
    }
  }

  @Override
  public void orphan() {
    Project project = Ode.getInstance().getProjectManager().getProject(assetsFolder.getProjectId());
    project.removeProjectChangeListener(this);
    super.orphan();
  }

  @Override
  protected boolean okAction() {
    int selected = assetsList.getSelectedIndex();
    if (selected == -1) {
      Window.alert(MESSAGES.noAssetSelected());
      return false;
    }
    property.setValue(choices.getValueAtIndex(selected));
    return true;
  }

  // ProjectChangeListener implementation

  @Override
  public void onProjectLoaded(Project project) {
  }

  @Override
  public void onProjectNodeAdded(Project project, ProjectNode node) {
    if (node instanceof YoungAndroidAssetNode) {
      String assetName = node.getName();
      if (!choices.containsValue(assetName)) {
        choices.addItem(assetName);;
      }
      String currentValue = property.getValue();
      if (assetName.equals(currentValue)) {
        property.setValue("");
        property.setValue(currentValue);
      }
    }
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
    if (node instanceof YoungAndroidAssetNode) {
      String assetName = node.getName();
      String currentValue = property.getValue();
      if (node.getName().equals(currentValue)) {
        property.setValue("");
      }
      choices.removeValue(assetName);
    }
  }

}
