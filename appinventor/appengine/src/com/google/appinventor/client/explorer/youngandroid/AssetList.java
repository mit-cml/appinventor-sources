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
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
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
  private boolean ignoreNextSelection;

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
        if (ignoreNextSelection) {
          ignoreNextSelection = false;
          return;
        }
        TreeItem selected = event.getSelectedItem();
        showContextMenu(selected, clientX, clientY);
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
        String assetName = node.getName();
        String displayName = assetName;
        if (displayName.length() > 20)
          displayName = displayName.substring(0, 8) + "..." +
              displayName.substring(displayName.length() - 9, displayName.length());

        String fileSuffix = node.getProjectId() + "/" + node.getFileId();
        String treeItemText = "<span style='cursor: grab'>";
        if (StorageUtil.isImageFile(fileSuffix)) {
          treeItemText += new Image(images.mediaIconImg());
        } else if (StorageUtil.isAudioFile(fileSuffix )) {
          treeItemText += new Image(images.mediaIconAudio());
        } else if (StorageUtil.isVideoFile(fileSuffix )) {
          treeItemText += new Image(images.mediaIconVideo());
        }
        treeItemText += displayName + "</span>";
        final HTML treeItemWidget = new HTML(treeItemText);
        final TreeItem treeItem = new TreeItem(treeItemWidget);
        // keep a pointer from the tree item back to the actual node
        treeItem.setUserObject(node);
        assetList.addItem(treeItem);
        configureDraggable(treeItem.getElement(), assetName);
        treeItemWidget.addDomHandler(new MouseDownHandler() {
          @Override
          public void onMouseDown(MouseDownEvent event) {
            clientX = event.getClientX();
            clientY = event.getClientY();
            suppressNextSelection();
            event.stopPropagation();
          }
        }, MouseDownEvent.getType());
        treeItemWidget.addDomHandler(new MouseUpHandler() {
          @Override
          public void onMouseUp(MouseUpEvent event) {
            event.stopPropagation();
          }
        }, MouseUpEvent.getType());
        treeItemWidget.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            clientX = event.getClientX();
            clientY = event.getClientY();
            event.stopPropagation();
            suppressNextSelection();
            if (!isAssetDragClick(treeItem.getElement())) {
              showContextMenu(treeItem, clientX, clientY);
            }
          }
        });
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

  private static native void configureDraggable(Element el, String assetName)/*-{
    function setCursor(root, cursor) {
      root.style.cursor = cursor;
      if (root.querySelectorAll) {
        var descendants = root.querySelectorAll('*');
        for (var i = 0; i < descendants.length; i++) {
          descendants[i].style.cursor = cursor;
        }
      }
    }
    function setAssetDragName(name) {
      $wnd.__aiAssetDragName = name;
      try {
        if ($wnd.top) {
          $wnd.top.__aiAssetDragName = name;
        }
      } catch (err) {
        // Ignore cross-frame access errors. The data transfer payload is primary.
      }
    }
    function clearAssetDragName(name) {
      if ($wnd.__aiAssetDragName === name) {
        $wnd.__aiAssetDragName = null;
      }
      try {
        if ($wnd.top && $wnd.top.__aiAssetDragName === name) {
          $wnd.top.__aiAssetDragName = null;
        }
      } catch (err) {
        // Ignore cross-frame access errors. The data transfer payload is primary.
      }
    }
    if (!el) {
      return;
    }
    el.setAttribute('draggable', 'true');
    el.setAttribute('data-assetname', assetName);
    setCursor(el, 'grab');
    if (el.getAttribute('data-ai-assetdrag') == 'true') {
      return;
    }
    el.setAttribute('data-ai-assetdrag', 'true');
    el.addEventListener('dragstart', function(e) {
      var name = this.getAttribute('data-assetname');
      this.classList.add('ode-AssetItem-dragging');
      this.setAttribute('data-ai-assetdragging', 'true');
      setCursor(this, 'grabbing');
      e.dataTransfer.effectAllowed = 'move';
      e.dataTransfer.clearData();
      e.dataTransfer.setData('application/x-appinventor-asset', name);
      e.dataTransfer.setData('text/plain', name);
      setAssetDragName(name);
      if (e.dataTransfer.setDragImage) {
        var dragGhost = $wnd.__aiAssetDragGhost;
        if (!dragGhost) {
          dragGhost = $doc.createElement('img');
          dragGhost.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==';
          dragGhost.style.position = 'fixed';
          dragGhost.style.top = '-1000px';
          dragGhost.style.left = '-1000px';
          $doc.body.appendChild(dragGhost);
          $wnd.__aiAssetDragGhost = dragGhost;
        }
        e.dataTransfer.setDragImage(dragGhost, 0, 0);
      }
    });
    el.addEventListener('dragend', function() {
      this.classList.remove('ode-AssetItem-dragging');
      this.removeAttribute('data-ai-assetdragging');
      $wnd.__aiAssetDragSuppressClickUntil = new Date().getTime() + 250;
      setCursor(this, 'grab');
      clearAssetDragName(this.getAttribute('data-assetname'));
    });
  }-*/;

  private static native boolean isAssetDragClick(Element el)/*-{
    var now = new Date().getTime();
    return !!(el && el.getAttribute('data-ai-assetdragging') == 'true') ||
        !!($wnd.__aiAssetDragSuppressClickUntil && now < $wnd.__aiAssetDragSuppressClickUntil);
  }-*/;

  private void showContextMenu(TreeItem selected, int clientX, int clientY) {
    ProjectNode node = (ProjectNode) selected.getUserObject();
    // The actual menu is determined by what is registered for the filenode
    // type in CommandRegistry.java
    ProjectNodeContextMenu.show(node, selected.getWidget(), clientX, clientY);
  }

  private void suppressNextSelection() {
    ignoreNextSelection = true;
    new Timer() {
      @Override
      public void run() {
        ignoreNextSelection = false;
      }
    }.schedule(250);
  }

  public Tree getTree() {
    return assetList;
  }
}
