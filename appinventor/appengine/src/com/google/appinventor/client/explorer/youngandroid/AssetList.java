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
import com.google.appinventor.shared.rpc.project.ProjectNode;
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
        if (assetsFolder != null) {
          new FileUploadWizard(assetsFolder).show();
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
        ProjectNode node = (ProjectNode) selected.getUserObject();
        // The actual menu is determined by what is registered for the filenode
        // type in CommandRegistry.java
        ProjectNodeContextMenu.show(node, selected.getWidget(), clientX, clientY);
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
   * Populate the asset tree with files from the project's assets folder.
   */
  private void refreshAssetList() {
    final Images images = Ode.getImageBundle();
    LOG.info("AssetList: refreshing for project " + projectId);
    assetList.clear();

    if (assetsFolder != null) {
      for (ProjectNode node : assetsFolder.getChildren()) {
        // Add the name to the tree. We need to enclose it in a span
        // because the CSS style for selection specifies a span.
        String nodeName = node.getName();
        if (nodeName.length() > 20)
          nodeName = nodeName.substring(0, 8) + "..." + nodeName.substring(nodeName.length() - 9,
              nodeName.length());

        String fileSuffix = node.getProjectId() + "/" + node.getFileId();
        String treeItemText = "<span style='cursor: pointer'>";
        if (StorageUtil.isImageFile(fileSuffix)) {
          treeItemText += new Image(images.mediaIconImg());
        } else if (StorageUtil.isAudioFile(fileSuffix )) {
          treeItemText += new Image(images.mediaIconAudio());
        } else if (StorageUtil.isVideoFile(fileSuffix )) {
          treeItemText += new Image(images.mediaIconVideo());
        }
        treeItemText += nodeName + "</span>";
        TreeItem treeItem = new TreeItem(new HTML(treeItemText));
        // keep a pointer from the tree item back to the actual node
        treeItem.setUserObject(node);
        assetList.addItem(treeItem);
      }
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
