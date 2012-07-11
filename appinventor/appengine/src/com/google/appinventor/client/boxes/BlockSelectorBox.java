// Copyright 2012 Massachusetts Institute of Technology. All Rights Reserved.

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.youngandroid.BlockDrawerSelectionListener;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.explorer.SourceStructureExplorerItem;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TreeItem;

import java.util.ArrayList;
import java.util.List;


/**
 * Box implementation for block selector. Shows a tree containing the
 * built-in blocks as well as the components for the current form. 
 * Clicking on an item toggles the blocks drawer for the item. This box 
 * shares screen real estate with the SourceStructureBox. It uses a 
 * SourceStructureExplorer to handle the components part of the tree.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public final class BlockSelectorBox extends Box {
  // Singleton block selector box instance
  // Starts out not visible. call setVisible(true) to make it visible
  private static final BlockSelectorBox INSTANCE = new BlockSelectorBox();
  
  private static final String BUILTIN_DRAWER_NAMES[] = {"Control",
                                                      "Lists",
                                                      "Logic",
                                                      "Math",
                                                      "Text",
                                                      "Variables",
                                                      "Procedures"};

  // Source structure explorer (for components and built-in blocks)
  private final SourceStructureExplorer sourceStructureExplorer;
  
  private List<BlockDrawerSelectionListener> drawerListeners;

  /**
   * Return the singleton BlockSelectorBox box.
   *
   * @return  block selector box
   */
  public static BlockSelectorBox getBlockSelectorBox() {
    return INSTANCE;
  }

  /**
   * Creates new block selector box.
   */
  private BlockSelectorBox() {
    super(MESSAGES.blockSelectorBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    sourceStructureExplorer = new SourceStructureExplorer();

    setContent(sourceStructureExplorer);
    setVisible(false);
    drawerListeners = new ArrayList<BlockDrawerSelectionListener>();
  }

  /**
   * Returns source structure explorer associated with box.
   *
   * @return  source structure explorer
   */
  public SourceStructureExplorer getSourceStructureExplorer() {
    return sourceStructureExplorer;
  }
  
  /**
   * Constructs a tree item for built-in blocks.
   *
   * @return  tree item for this component
   */
  public TreeItem getBuiltInBlocksTree() {
    TreeItem builtinNode = new TreeItem(new HTML("<span>" 
        + MESSAGES.builtinBlocksLabel() + "</span>"));
    for (final String drawerName : BUILTIN_DRAWER_NAMES) {
      TreeItem itemNode = new TreeItem(new HTML("<span>" + drawerName + "</span>"));
      SourceStructureExplorerItem sourceItem = new SourceStructureExplorerItem() {
        @Override
        public void onSelected() {
          fireBuiltinDrawerSelected(drawerName);
        }

        @Override
        public void onStateChange(boolean open) {
        }

        @Override
        public boolean canRename() {
          return false;
        }

        @Override
        public void rename() {
        }

        @Override
        public boolean canDelete() {
          return false;
        }

        @Override
        public void delete() {
        }
      };
      itemNode.setUserObject(sourceItem);
      builtinNode.addItem(itemNode);
    }
    builtinNode.setState(true);
    return builtinNode;
  }
  
  public void addBlockDrawerSelectionListener(BlockDrawerSelectionListener listener) {
    drawerListeners.add(listener);
  }
  
  public void removeBlockDrawerSelectionListener(BlockDrawerSelectionListener listener) {
    drawerListeners.remove(listener);
  }

  private void fireBuiltinDrawerSelected(String drawerName) {
    for (BlockDrawerSelectionListener listener : drawerListeners) {
      listener.onBuiltinDrawerSelected(drawerName);
    }
  }
}

