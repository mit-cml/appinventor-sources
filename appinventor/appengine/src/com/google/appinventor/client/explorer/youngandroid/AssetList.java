// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.client.explorer.project.ProjectNodeContextMenu;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.wizards.FileUploadWizard;
import com.google.appinventor.client.wizards.FileUploadWizard.FileUploadedCallback;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.FolderNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetService;
import com.google.appinventor.shared.rpc.globalasset.GlobalAssetServiceAsync;
import com.google.gwt.core.client.GWT;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * The asset list shows all the project's assets, and lets the
 * user delete assets.
 *
 */

public class AssetList extends Composite implements ProjectChangeListener {
  private static final Logger LOG = Logger.getLogger(AssetList.class.getName());

  // The asset "list" is represented as a tree and follows the same GWT conventions.
  private Tree assetList;
  private final VerticalPanel panel;

  private long projectId;
  private Project project;
  private YoungAndroidAssetsFolder assetsFolder;
  private int clientX;
  private int clientY;
  private final GlobalAssetServiceAsync globalAssetService = GWT.create(GlobalAssetService.class);

  // Helper class to store data for each item in the Tree
  private static class AssetListItemData {
    String displayName;
    String fullPath; // e.g., "assets/img.png" or "_global_/icons/img.png"
    boolean isGlobal;
    ProjectNode projectNode; // Null if global, used for existing context menu

    AssetListItemData(String displayName, String fullPath, boolean isGlobal, ProjectNode projectNode) {
      this.displayName = displayName;
      this.fullPath = fullPath;
      this.isGlobal = isGlobal;
      this.projectNode = projectNode;
    }
  }

  /**
   * Creates a new AssetList
   */
  public AssetList() {

    assetList = new Tree();
    assetList.setWidth("100%");

    panel = new VerticalPanel();
    panel.setWidth("100%");

    panel.add(assetList);

    TextButton addButton = new TextButton(MESSAGES.addButton());
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (assetsFolder != null) { // assetsFolder is null if no project is loaded
          // Define a callback that always refreshes the AssetList
          FileUploadWizard.FileUploadedCallback refreshCallback =
              new FileUploadWizard.FileUploadedCallback() {
            @Override
            public void onFileUploaded(FolderNode folderNode, FileNode fileNode) {
              // folderNode and fileNode might be null if a global asset was uploaded
              // by the wizard, depending on its internal changes.
              // Regardless, always refresh the AssetList.
              refreshAssetList();
            }
          };
          new FileUploadWizard(assetsFolder, refreshCallback).show();
        } else {
          // Handle case where no project is loaded, perhaps disable add button or show message
          // Or, if global assets can be uploaded without a project context (requires a different wizard invocation)
          // For now, assume addButton is for project context or wizard handles null assetsFolder for global-only.
          // The current FileUploadWizard constructor takes a FolderNode, so it expects a project context.
          // This implies the "Add global asset" checkbox in the wizard is for adding to the user's global store
          // *while a project is open*.
        }
      }
    });

    SimplePanel buttonPanel = new SimplePanel();
    buttonPanel.setStyleName("ode-PanelButtons");
    buttonPanel.add(addButton);

    panel.add(buttonPanel);
    panel.setCellHorizontalAlignment(buttonPanel, VerticalPanel.ALIGN_CENTER);

    initWidget(panel);

    assetList.setScrollOnSelectEnabled(false);
    assetList.sinkEvents(Event.ONMOUSEMOVE);
    assetList.addMouseMoveHandler(new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent event) {
        clientX = event.getClientX();
        clientY = event.getClientY();
      }
    });
    assetList.addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem selected = event.getSelectedItem();
        Object userObject = selected.getUserObject();
        if (userObject instanceof AssetListItemData) {
          AssetListItemData itemData = (AssetListItemData) userObject;
          if (!itemData.isGlobal && itemData.projectNode != null) {
            ProjectNodeContextMenu.show(itemData.projectNode, selected.getWidget(), clientX, clientY);
          } else {
            // Optionally, clear or hide any existing context menu if one might be sticky
            // For global assets, no context menu for now via this path.
            // A future enhancement could be:
            // ProjectNodeContextMenu.showForGlobalAsset(itemData, selected.getWidget(), clientX, clientY);
          }
        }
      }});
    assetList.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        assetList.addStyleName("gwt-Tree-focused");
      }
    });
    assetList.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        assetList.removeStyleName("gwt-Tree-focused");
      }
    });
  }

  /*
   * Populate the asset tree with files from the project's assets folder and global assets.
   */
  private void refreshAssetList() {
    LOG.info("AssetList: refreshing for project " + projectId);
    assetList.clear();
    final List<AssetListItemData> itemsToDisplay = new ArrayList<AssetListItemData>();

    // Load Project Assets
    if (assetsFolder != null) {
      for (ProjectNode node : assetsFolder.getChildren()) {
        String nodeName = node.getName();
        // TODO: Apply existing truncation logic for nodeName if needed for display consistency
        // String truncatedNodeName = nodeName;
        // if (nodeName.length() > 20)
        //   truncatedNodeName = nodeName.substring(0, 8) + "..." + nodeName.substring(nodeName.length() - 9, nodeName.length());
        itemsToDisplay.add(new AssetListItemData(nodeName, node.getFileId(), false, node));
      }
    }

    // Fetch and Prepare Global Assets
    globalAssetService.getGlobalAssetPaths(new OdeAsyncCallback<List<String>>(
        MESSAGES.errorFetchingGlobalAssets()) { // Create this message in OdeMessages.java
      @Override
      public void onSuccess(List<String> relativeGlobalPaths) {
        for (String relativePath : relativeGlobalPaths) {
          // TODO: Apply truncation logic for display name if needed
          String displayName = "(g) " + relativePath; 
          String fullPath = "_global_/" + relativePath;
          itemsToDisplay.add(new AssetListItemData(displayName, fullPath, true, null));
        }
        populateTreeItems(itemsToDisplay);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        populateTreeItems(itemsToDisplay); // Populate with project assets even if global fails
      }
    });
  }

  private void populateTreeItems(List<AssetListItemData> items) {
    // Sort items by displayName
    Collections.sort(items, new Comparator<AssetListItemData>() {
      @Override
      public int compare(AssetListItemData o1, AssetListItemData o2) {
        return o1.displayName.compareToIgnoreCase(o2.displayName);
      }
    });

    assetList.clear(); // Clear again before adding sorted items

    final Images images = Ode.getImageBundle();
    for (AssetListItemData itemData : items) {
      String treeItemText = "<span style='cursor: pointer'>";
      // Icon logic
      String pathForIconDetection = itemData.isGlobal ? itemData.fullPath : 
          (itemData.projectNode != null ? itemData.projectNode.getProjectId() + "/" + itemData.projectNode.getFileId() : itemData.fullPath);

      if (StorageUtil.isImageFile(pathForIconDetection)) {
        treeItemText += new Image(images.mediaIconImg());
      } else if (StorageUtil.isAudioFile(pathForIconDetection)) {
        treeItemText += new Image(images.mediaIconAudio());
      } else if (StorageUtil.isVideoFile(pathForIconDetection)) {
        treeItemText += new Image(images.mediaIconVideo());
      } else {
         treeItemText += new Image(images.fileIcon()); // A generic file icon for others or if projectNode is null for project asset
      }
      treeItemText += itemData.displayName + "</span>";
      TreeItem treeItem = new TreeItem(new HTML(treeItemText));
      treeItem.setUserObject(itemData); // Store AssetListItemData
      assetList.addItem(treeItem);
    }
  }

  public void refreshAssetList(long projectId) {
    LOG.info("AssetList: switching projects from  " + this.projectId +
        " to " + projectId);

    if (project != null) {
      project.removeProjectChangeListener(this);
    }

    this.projectId = projectId;
    if (projectId != 0) {
      project = Ode.getInstance().getProjectManager().getProject(projectId);
      assetsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();
      project.addProjectChangeListener(this);
    } else {
      project = null;
      assetsFolder = null;
    }

    refreshAssetList();
  }

  // ProjectChangeListener implementation
  @Override
  public void onProjectLoaded(Project project) {
    LOG.info("AssetList: got onProjectLoaded for " + project.getProjectId() +
        ", current project is " + projectId);
    refreshAssetList();
  }

  @Override
  public void onProjectNodeAdded(Project project, ProjectNode node) {
    LOG.info("AssetList: got projectNodeAdded for node " + node.getFileId()
        + " and project "  + project.getProjectId() + ", current project is " + projectId);
    if (node instanceof YoungAndroidAssetNode) {
      refreshAssetList();
    }
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
    LOG.info("AssetList: got onProjectNodeRemoved for node " + node.getFileId()
        + " and project "  + project.getProjectId() + ", current project is " + projectId);
    if (node instanceof YoungAndroidAssetNode) {
      refreshAssetList();
    }
  }

  public Tree getTree() {
    return assetList;
  }
}
