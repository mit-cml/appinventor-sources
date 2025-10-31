// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.client.widgets.properties.AdditionalChoicePropertyEditor;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetService;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetServiceAsync;
import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.gwt.core.client.GWT;
import com.google.appinventor.client.wizards.FileUploadWizard;
import com.google.appinventor.client.wizards.FileUploadWizard.FileUploadedCallback;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.FolderNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import java.util.List;
import java.util.ArrayList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Property editor for selecting an asset for a property.
 *
 */
public final class YoungAndroidAssetSelectorPropertyEditor extends AdditionalChoicePropertyEditor
    implements ProjectChangeListener {
  // UI elements
  private final ListBox assetsList;
private final List<String> assetFileIds = new ArrayList<String>(); // To store actual fileIds

  private final ListWithNone choices;

  private final YoungAndroidAssetsFolder assetsFolder;
  private final GlobalAssetServiceAsync globalAssetService = GWT.create(GlobalAssetService.class);

  /**
   * Creates a new property editor for selecting a Young Android asset.
   *
   * @param editor the editor that this property editor belongs to
   */
  public YoungAndroidAssetSelectorPropertyEditor(final DesignerEditor editor) {
    Project project = Ode.getInstance().getProjectManager().getProject(editor.getProjectId());
    assetsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();
    project.addProjectChangeListener(this);

    VerticalPanel selectorPanel = new VerticalPanel();
    assetsList = new ListBox();
    assetsList.setVisibleItemCount(10);
    assetsList.setWidth("100%");
    assetsList.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setOkButtonEnabled(true);
      }
    });
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

      @Override
      public int getItemCount() {
        return assetsList.getItemCount();
      }
    });

    // Fill choices with the assets.
    loadAssetChoices();

    Button addButton = new Button(MESSAGES.addButton());
    addButton.setWidth("100%");
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        FileUploadedCallback callback = new FileUploadedCallback() {
          @Override
          public void onFileUploaded(FolderNode folderNode, FileNode fileNode) {
            loadAssetChoices();
            if (fileNode != null) {
              choices.selectValue(fileNode.getFileId());
            }
            closeAdditionalChoiceDialog(true);
          }
        };
        new FileUploadWizard(assetsFolder, callback).show();
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
    if (!isMultipleValues()) {
      choices.selectValue(property.getValue());
    } else {
      setOkButtonEnabled(false);
    }
    super.openAdditionalChoiceDialog();
    assetsList.setFocus(true);
  }

  @Override
  protected String getPropertyValueSummary() {
    if (isMultipleValues()) {
      return MESSAGES.multipleValues();
    }
    String currentFileId = property.getValue();
    if (currentFileId == null || currentFileId.isEmpty()) {
      return MESSAGES.noneCaption();
    }

    for (int i = 0; i < assetFileIds.size(); i++) {
      if (assetFileIds.get(i).equals(currentFileId)) {
        int indexInListBox = i;
        // Adjust index if "None" is the first item in the displayed list
        if (assetsList.getItemCount() > 0 && assetsList.getItemText(0).equals(MESSAGES.noneCaption())) {
          indexInListBox = i + 1;
        }
        if (indexInListBox >= 0 && indexInListBox < assetsList.getItemCount()) {
          return assetsList.getItemText(indexInListBox);
        }
      }
    }
    // If the fileId is not in assetFileIds, it might be a manually entered path or an old value.
    // Display it, possibly formatted if it's a global asset.
    if (currentFileId.startsWith("assets/_global_/")) {
      String pathWithoutPrefix = currentFileId.substring("assets/_global_/".length());
      return "[G] " + pathWithoutPrefix + " (missing?)";
    }
    // For project assets, it might be just "asset.png" if it's an old project or manually entered.
    // Or "assets/asset.png" if it was saved with the new logic.
    if (currentFileId.startsWith("assets/")) {
         return currentFileId.substring("assets/".length()) + " (missing?)";
    }
    return currentFileId + " (missing?)";
  }

  @Override
  protected boolean okAction() {
    int selectedIndexInListBox = assetsList.getSelectedIndex();
    if (selectedIndexInListBox == -1) {
      Window.alert(MESSAGES.noAssetSelected());
      return false;
    }

    String selectedDisplayName = assetsList.getItemText(selectedIndexInListBox);
    String valueToSet;

    if (selectedDisplayName.equals(MESSAGES.noneCaption())) {
      valueToSet = ""; // Represents "None"
    } else {
      int actualIndexInAssetFileIds = selectedIndexInListBox;
      // Adjust index if "None" is the first item in the displayed list
      if (assetsList.getItemCount() > 0 && assetsList.getItemText(0).equals(MESSAGES.noneCaption())) {
        actualIndexInAssetFileIds = selectedIndexInListBox - 1;
      }

      if (actualIndexInAssetFileIds >= 0 && actualIndexInAssetFileIds < assetFileIds.size()) {
        valueToSet = assetFileIds.get(actualIndexInAssetFileIds);
      } else {
        Window.alert("Error: Asset selection out of sync. Please try again.");
        return false;
      }
    }

    boolean multiple = isMultipleValues();
    setMultipleValues(false);
    property.setValue(valueToSet, multiple);
    return true;
  }

  private void loadAssetChoices() {
    assetsList.clear(); // Directly clear the ListBox
    assetFileIds.clear();

    choices.clearNoneItem();

    if (assetsFolder != null) {
      for (ProjectNode node : assetsFolder.getChildren()) {
        addAssetChoice(node.getName(), node.getFileId());
      }
    }
    
    finalizeAssetLoading();
  }

  private void finalizeAssetLoading() {
    choices.updateNoneItem();
  }

  private void addAssetChoice(String name, String fileId) {
    String displayName = name;
    
    // Handle old-style global assets that were added to the project
    if (fileId.startsWith("assets/_global_/")) {
      String pathWithoutPrefix = fileId.substring("assets/_global_/".length());
      // Show global assets with a [G] prefix to distinguish them
      displayName = "[G] " + pathWithoutPrefix;
    } 
    // Handle new-style global assets with folder prefix (e.g., "assets/icons_home.png")
    else if (fileId.startsWith("assets/") && name.contains("_")) {
      // Check if this might be a folder-prefixed global asset
      String assetName = fileId.substring("assets/".length());
      if (assetName.contains("_")) {
        int underscoreIndex = assetName.indexOf("_");
        String possibleFolder = assetName.substring(0, underscoreIndex);
        String possibleFilename = assetName.substring(underscoreIndex + 1);
        // If it looks like a folder_filename pattern, show with [G] prefix
        if (possibleFolder.length() > 0 && possibleFilename.length() > 0) {
          displayName = "[G] " + possibleFolder + "/" + possibleFilename;
        }
      }
    }
    
    // choices.addItem will add to assetsList (the UI ListBox)
    choices.addItem(displayName);
    assetFileIds.add(fileId); // Store the actual fileId in parallel
  }

  private void removeAssetChoice(String fileIdToRemove) {
    int indexToRemove = -1;
    for (int i = 0; i < assetFileIds.size(); i++) {
      if (assetFileIds.get(i).equals(fileIdToRemove)) {
        indexToRemove = i;
        break;
      }
    }
    if (indexToRemove != -1) {
      assetFileIds.remove(indexToRemove);
      int indexInListBox = indexToRemove;
      if (assetsList.getItemCount() > 0 && assetsList.getItemText(0).equals(MESSAGES.noneCaption())) {
        if (indexToRemove + 1 < assetsList.getItemCount()) {
          indexInListBox = indexToRemove + 1;
        }
      }
      String currentPropertyValue = property.getValue();
      loadAssetChoices();
      choices.selectValue(currentPropertyValue);
    }
  }

  // ProjectChangeListener implementation

  @Override
  public void onProjectLoaded(Project project) {
    loadAssetChoices();
  }

  @Override
  public void onProjectNodeAdded(Project project, ProjectNode node) {
    if (node instanceof YoungAndroidAssetNode && node.getProjectId() == assetsFolder.getProjectId()) {
      // Check if it's a new asset not yet in our list (could be a replacement)
      boolean found = false;
      for (String fileId : assetFileIds) {
        if (fileId.equals(node.getFileId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        addAssetChoice(node.getName(), node.getFileId());
      }

      // If this newly added node corresponds to the current property value,
      // refresh the component (e.g. image preview).
      String currentValue = property.getValue();
      if (node.getFileId().equals(currentValue)) {
        property.setValue(""); // Force refresh
        property.setValue(currentValue);
      }
    }
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
    if (node instanceof YoungAndroidAssetNode && node.getProjectId() == assetsFolder.getProjectId()) {
      String removedFileId = node.getFileId();
      String currentPropertyValue = property.getValue();

      if (removedFileId.equals(currentPropertyValue)) {
        property.setValue(""); // Clear property if the selected asset was removed
      }
      removeAssetChoice(removedFileId); // Remove from our lists
    }
  }
}
