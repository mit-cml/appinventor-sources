// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;
import com.google.appinventor.client.wizards.FileUploadWizard;
import com.google.appinventor.client.wizards.FileUploadWizard.FileUploadedCallback;
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

import java.util.Iterator;

/**
 * Property editor for selecting an asset for a property.
 *
 */
public final class YoungAndroidAssetSelectorPropertyEditor extends AdditionalChoicePropertyEditor
    implements ProjectChangeListener {
  // UI elements
  private final ListBox assetsList;

  private final ListWithNone choices;

  private final YoungAndroidAssetsFolder assetsFolder;

  /**
   * Creates a new property editor for selecting a Young Android asset.
   *
   * @param editor the editor that this property editor belongs to
   */
  public YoungAndroidAssetSelectorPropertyEditor(final YaFormEditor editor) {
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
      public void addItem(String item) {
        assetsList.addItem(item);
      }

      @Override
      public String getItem(int index) {
        return assetsList.getItemText(index);
      }

      @Override
      public void removeItem(int index) {
        assetsList.removeItem(index);
      }

      @Override
      public void setSelectedIndex(int index) {
        assetsList.setSelectedIndex(index);
      }
    });

    // Fill choices with the assets.
    if (assetsFolder != null) {
      for (ProjectNode node : assetsFolder.getChildren()) {
        choices.addItem(node.getName());
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
            // At this point, the asset has been uploaded to the server, and
            // has even been added to the assetsFolder. We are all set!
            choices.selectValue(fileNode.getName());
            closeAdditionalChoiceDialog(true);
          }
        };
        FileUploadWizard uploader = new FileUploadWizard(assetsFolder, callback);
        uploader.show();
      }
    });
    selectorPanel.add(addButton);
    selectorPanel.setWidth("100%");

    // At this point, the editor hasn't finished loading.
    // Use a DeferredCommand to finish the initialization after the editor has finished loading.
    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        if (editor.isLoadComplete()) {
          finishInitialization();
        } else {
          // Editor still hasn't finished loading.
          DeferredCommand.addCommand(this);
        }
      }
    });

    initAdditionalChoicePanel(selectorPanel);
  }

  private void finishInitialization() {
    // Previous version had a bug where the value could be accidentally saved as "None".
    // If the property value is "None" and choices doesn't contain the value "None", set the
    // property value to "".
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
  protected void openAdditionalChoiceDialog() {
    choices.selectValue(property.getValue());
    super.openAdditionalChoiceDialog();
    assetsList.setFocus(true);
  }

  @Override
  protected String getPropertyValueSummary() {
    String value = property.getValue();
    if (choices.containsValue(value)) {
      return choices.getDisplayItemForValue(value);
    }
    return value;
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
    // Check whether our asset was updated.
    if (node instanceof YoungAndroidAssetNode) {
      String assetName = node.getName();

      // Add it to the list if it isn't already there.
      // It could already be there if the user adds an asset that's already there, which is the way
      // to replace the asset.
      if (!choices.containsValue(assetName)) {
        choices.addItem(assetName);
      }

      // Check whether our asset was updated.
      String currentValue = property.getValue();
      if (assetName.equals(currentValue)) {
        // Our asset was updated.
        // Set the property value to blank and then back to the current value.
        // This will force the component to update itself (for example, it will refresh its image).
        property.setValue("");
        property.setValue(currentValue);
      }
    }
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
    if (node instanceof YoungAndroidAssetNode) {
      String assetName = node.getName();

      // Check whether our asset was removed.
      String currentValue = property.getValue();
      if (node.getName().equals(currentValue)) {
        // Our asset was removed.
        property.setValue("");
      }

      // Remove the asset from the list.
      choices.removeValue(assetName);
    }
  }
}
