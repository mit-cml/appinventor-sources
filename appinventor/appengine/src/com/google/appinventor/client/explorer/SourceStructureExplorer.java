// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.TextButton;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.user.client.ui.*;

import java.util.Iterator;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * This explorer is used to outline the structure of a source file. Note that
 * this explorer is shared by all it's clients. That means that clients (most
 * likely editors) need to update its content upon activation.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class SourceStructureExplorer extends Composite {
  // UI elements
  private final Tree tree;
  private final TextButton renameButton;
  private final TextButton deleteButton;

  /**
   * Creates a new source structure explorer.
   */
  public SourceStructureExplorer() {
    // Initialize UI elements
    tree = new Tree(Ode.getImageBundle());
    tree.setAnimationEnabled(true);
    tree.addCloseHandler(new CloseHandler<TreeItem>() {
      @Override
      public void onClose(CloseEvent<TreeItem> event) {
        TreeItem treeItem = event.getTarget();
        if (treeItem != null) {
          Object userObject = treeItem.getUserObject();
          if (userObject instanceof SourceStructureExplorerItem) {
            SourceStructureExplorerItem item = (SourceStructureExplorerItem) userObject;
            item.onStateChange(false);
          }
        }
      }
    });
    tree.addOpenHandler(new OpenHandler<TreeItem>() {
      @Override
      public void onOpen(OpenEvent<TreeItem> event) {
        TreeItem treeItem = event.getTarget();
        if (treeItem != null) {
          Object userObject = treeItem.getUserObject();
          if (userObject instanceof SourceStructureExplorerItem) {
            SourceStructureExplorerItem item = (SourceStructureExplorerItem) userObject;
            item.onStateChange(true);
          }
        }
      }
    });
    tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        TreeItem treeItem = event.getSelectedItem();
        if (treeItem != null) {
          Object userObject = treeItem.getUserObject();
          if (userObject instanceof SourceStructureExplorerItem) {
            SourceStructureExplorerItem item = (SourceStructureExplorerItem) userObject;
            enableButtons(item);
            //showBlocks(item);
            item.onSelected();
          } else {
            disableButtons();
            //hideComponent();
          }
        } else {
          disableButtons();
        }
      }
    });
    tree.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_DELETE || keyCode == KeyCodes.KEY_BACKSPACE) {
          event.preventDefault();
          deleteItemFromTree();
        }
      }
    });

    // Put a ScrollPanel around the tree.
    ScrollPanel scrollPanel = new ScrollPanel(tree);
    scrollPanel.setWidth("200px");  // wide enough to avoid a horizontal scrollbar most of the time
    scrollPanel.setHeight("480px"); // approximately the same height as the viewer

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setStyleName("ode-PanelButtons");
    buttonPanel.setSpacing(4);

    renameButton = new TextButton(MESSAGES.renameButton());
    renameButton.setEnabled(false);
    renameButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        TreeItem treeItem = tree.getSelectedItem();
        if (treeItem != null) {
          Object userObject = treeItem.getUserObject();
          if (userObject instanceof SourceStructureExplorerItem) {
            SourceStructureExplorerItem item = (SourceStructureExplorerItem) userObject;
            item.rename();
          }
        }
      }
    });
    buttonPanel.add(renameButton);
    buttonPanel.setCellHorizontalAlignment(renameButton, HorizontalPanel.ALIGN_RIGHT);

    deleteButton = new TextButton(MESSAGES.deleteButton());
    deleteButton.setEnabled(false);
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        deleteItemFromTree();
      }
    });
    buttonPanel.add(deleteButton);
    buttonPanel.setCellHorizontalAlignment(deleteButton, HorizontalPanel.ALIGN_LEFT);

    VerticalPanel panel = new VerticalPanel();
    panel.add(scrollPanel);
    panel.add(new Label());
    panel.add(buttonPanel);
    panel.setCellHorizontalAlignment(buttonPanel, HorizontalPanel.ALIGN_CENTER);
    initWidget(panel);
  }

  private void deleteItemFromTree() {
    TreeItem treeItem = tree.getSelectedItem();
    if (treeItem != null) {
      Object userObject = treeItem.getUserObject();
      if (userObject instanceof SourceStructureExplorerItem) {
        SourceStructureExplorerItem item = (SourceStructureExplorerItem) userObject;
        item.delete();
      }
    }
  }

  private void enableButtons(SourceStructureExplorerItem item) {
    renameButton.setEnabled(item.canRename());
    deleteButton.setEnabled(item.canDelete());
  }

  private void disableButtons() {
    renameButton.setEnabled(false);
    deleteButton.setEnabled(false);
  }

  
  /* move this logic to declarations of SourceStructureExplorerItem subtypes
  private void showBlocks(SourceStructureExplorerItem item) {
    // are we showing the blocks editor?
    if (Ode.getInstance().getCurrentFileEditor() instanceof YaBlocksEditor) {
      YaBlocksEditor editor = 
          (YaBlocksEditor) Ode.getInstance().getCurrentFileEditor();
      OdeLog.log("Showing item " + item.getItemName());
      if (item.isComponent()) {
        editor.showComponentBlocks(item.getItemName());
      } else {
        editor.showBuiltinBlocks(item.getItemName());
      }
    }
  }

  private void hideComponent() {
    if (Ode.getInstance().getCurrentFileEditor() instanceof YaBlocksEditor) {
      YaBlocksEditor editor =
          (YaBlocksEditor) Ode.getInstance().getCurrentFileEditor();
      OdeLog.log("Hiding selected item");
      editor.hideComponentBlocks();
    }  
  }
   */  

  /**
   * Clears the tree.
   */
  public void clearTree() {
    tree.clear();
    disableButtons();
  }

  /**
   * Updates the tree
   *
   * @param root the new root TreeItem
   * @param itemToSelect item to select, or null for no selected item
   */
  public void updateTree(TreeItem root, SourceStructureExplorerItem itemToSelect) {
    TreeItem items[] = new TreeItem[1];
    items[0] = root;
    updateTree(items, itemToSelect);
  }

  
  /**
   * Updates the tree
   *
   * @param roots An array of root items (all top level)
   * @param itemToSelect item to select, or null for no selected item
   */
  public void updateTree(TreeItem[] roots, SourceStructureExplorerItem itemToSelect) {
    tree.clear();
    for (TreeItem root : roots) {
      tree.addItem(root);
    }
    if (itemToSelect != null) {
      selectItem(itemToSelect, true);
    } else {
      disableButtons();
    }
  }

  /**
   * Select or unselect an item in the tree
   *
   * @param item to select or unselect
   * @param select true to select, false to unselect
   */
  private void selectItem(SourceStructureExplorerItem item, boolean select) {
    Iterator<TreeItem> iter = tree.treeItemIterator();
    while (iter.hasNext()) {
      TreeItem treeItem = iter.next();
      if (item.equals(treeItem.getUserObject())) {
        // NOTE(lizlooney) - It turns out that calling TreeItem.setSelected(true) doesn't actually
        // select the item in the tree. It looks selected, but Tree.getSelectedItem() will return
        // null. Instead, we have to call Tree.setSelectedItem.
        if (select) {
          tree.setSelectedItem(treeItem, false); // false means don't trigger a SelectionEvent
          enableButtons(item);
          //showBlocks(item);
        } else {
          tree.setSelectedItem(null, false); // false means don't trigger a SelectionEvent
          disableButtons();
          //hideComponent();
        }
        break;
      }
    }
  }

  /**
   * Select an item in the tree
   *
   * @param item item to select
   */
  public void selectItem(SourceStructureExplorerItem item) {
    selectItem(item, true);
  }

  /**
   * Select an item in the tree
   *
   * @param item item to unselect
   */
  public void unselectItem(SourceStructureExplorerItem item) {
    selectItem(item, false);
  }
}
