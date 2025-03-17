// Copyright 2012 Massachusetts Institute of Technology. All Rights Reserved.

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslationTable;
import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.BlockDrawerSelectionListener;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.explorer.SourceStructureExplorerItem;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TreeItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Box implementation for block selector. Shows a tree containing the built-in
 * blocks as well as the components for the current form. Clicking on an item
 * opens the blocks drawer for the item (or closes it if it was already open).
 * This box shares screen real estate with the SourceStructureBox. It uses a
 * SourceStructureExplorer to handle the components part of the tree.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public final class BlockSelectorBox extends Box {
  private static final Logger LOG = Logger.getLogger(BlockSelectorBox.class.getName());

  private static class BlockSelectorItem implements SourceStructureExplorerItem {
    BlockSelectorItem() {
    }

    @Override
    public void onSelected(NativeEvent source) {
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
  }

  // Singleton block selector box instance
  // Starts out not visible. call setVisible(true) to make it visible
  private static final BlockSelectorBox INSTANCE = new BlockSelectorBox();

  private static final Set<String> BUILTIN_DRAWER_NAMES = new HashSet<String>(
      Arrays.asList("Control", "Logic", "Math", "Text", "Lists", "Dictionaries", "Colors",
          "Variables", "Procedures"));

  private static final Images images = Ode.getImageBundle();
  private static final Map<String, ImageResource> bundledImages = Maps.newHashMap();

  // Source structure explorer (for components and built-in blocks)
  private final SourceStructureExplorer sourceStructureExplorer;

  private List<BlockDrawerSelectionListener> drawerListeners;

  /**
   * Return the singleton BlockSelectorBox box.
   *
   * @return block selector box
   */

  /**
   * getSubsetDrawerNames
   * @param form
   * @return array of built-in block drawers for the blocks editor that contain blocks included in
   * the subset parameter. This keeps empty drawers from displaying in the Blocks Editor
   */
  private Set<String> getSubsetDrawerNames(MockForm form) {
    String subsetJsonString = form.getPropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET);
    if (subsetJsonString.length() > 0) {
      JSONObject subsetJSON = JSONParser.parseStrict(subsetJsonString).isObject();
      Set<String> subsetDrawers = new HashSet<String>(subsetJSON.get("shownBlockTypes").isObject().keySet());
      subsetDrawers.retainAll(BUILTIN_DRAWER_NAMES);
      return subsetDrawers;
    } else {
      return BUILTIN_DRAWER_NAMES;
    }
  }

  public static BlockSelectorBox getBlockSelectorBox() {
    return INSTANCE;
  }

  /**
   * Creates new block selector box.
   */
  private BlockSelectorBox() {
    super(MESSAGES.blockSelectorBoxCaption(), 300, // height
        false, // minimizable
        false); // removable

    sourceStructureExplorer = new SourceStructureExplorer();

    setContent(sourceStructureExplorer);
    setVisible(false);
    drawerListeners = new ArrayList<BlockDrawerSelectionListener>();
  }

  private static void initBundledImages() {
    bundledImages.put("Control", images.control());
    bundledImages.put("Logic", images.logic());
    bundledImages.put("Math", images.math());
    bundledImages.put("Text", images.text());
    bundledImages.put("Lists", images.lists());
    bundledImages.put("Colors", images.colors());
    bundledImages.put("Variables", images.variables());
    bundledImages.put("Procedures", images.procedures());
    bundledImages.put("Dictionaries", images.dictionaries());
  }

  /**
   * Returns source structure explorer associated with box.
   *
   * @return source structure explorer
   */
  public SourceStructureExplorer getSourceStructureExplorer() {
    return sourceStructureExplorer;
  }

  /**
   * Constructs a tree item for built-in blocks.
   *
   * @return tree item
   */
  public TreeItem getBuiltInBlocksTree(MockForm form) {
    initBundledImages();
    TreeItem builtinNode = new TreeItem(new HTML("<span>" + MESSAGES.builtinBlocksLabel()
        + "</span>"));
    for (final String drawerName : getSubsetDrawerNames(form)) {
      Image drawerImage = new Image(bundledImages.get(drawerName));
      TreeItem itemNode = new TreeItem(new HTML("<span>" + drawerImage
          + getBuiltinDrawerNames(drawerName) + "</span>"));
      SourceStructureExplorerItem sourceItem = new BlockSelectorItem() {
        @Override
        public void onSelected(NativeEvent source) {
          fireBuiltinDrawerSelected(drawerName);
        }
      };
      itemNode.setUserObject(sourceItem);
      builtinNode.addItem(itemNode);
    }
    builtinNode.setState(true);
    return builtinNode;
  }

  /**
   * Given the drawerName, return the name in current language setting
   */
  private String getBuiltinDrawerNames(String drawerName) {
    String name;

    LOG.warning("getBuiltinDrawerNames: drawerName = " + drawerName);

    if (drawerName.equals("Control")) {
       name = MESSAGES.builtinControlLabel();
    } else if (drawerName.equals("Logic")) {
       name = MESSAGES.builtinLogicLabel();
    } else if (drawerName.equals("Math")) {
       name = MESSAGES.builtinMathLabel();
    } else if (drawerName.equals("Text")) {
       name = MESSAGES.builtinTextLabel();
    } else if (drawerName.equals("Lists")) {
       name = MESSAGES.builtinListsLabel();
    } else if (drawerName.equals("Colors")) {
       name = MESSAGES.builtinColorsLabel();
    } else if (drawerName.equals("Variables")) {
       name = MESSAGES.builtinVariablesLabel();
    } else if (drawerName.equals("Procedures")) {
       name = MESSAGES.builtinProceduresLabel();
    } else if (drawerName.equals("Dictionaries")) {
       name = MESSAGES.builtinDictionariesLabel();
    } else {
       name = drawerName;
    }
    return name;
  }

  /**
   * Constructs a tree item for generic ("Advanced") component blocks for
   * component types that appear in form.
   *
   * @param form
   *          only component types that appear in this Form will be included
   * @return tree item for this form
   */
  public TreeItem getGenericComponentsTree(MockForm form) {
    Map<String, String> typesAndIcons = Maps.newHashMap();
    form.collectTypesAndIcons(typesAndIcons);
    TreeItem advanced = new TreeItem(new HTML("<span>" + MESSAGES.anyComponentLabel() + "</span>"));
    List<String> typeList = new ArrayList<String>(typesAndIcons.keySet());
    Collections.sort(typeList);
    for (final String typeName : typeList) {
      TreeItem itemNode = new TreeItem(new HTML("<span>" + typesAndIcons.get(typeName)
          + MESSAGES.textAnyComponentLabel()
          + ComponentTranslationTable.getComponentName(typeName) + "</span>"));
      SourceStructureExplorerItem sourceItem = new BlockSelectorItem() {
        @Override
        public void onSelected(NativeEvent source) {
          fireGenericDrawerSelected(typeName);
        }
      };
      itemNode.setUserObject(sourceItem);
      advanced.addItem(itemNode);
    }
    return advanced;
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

  private void fireGenericDrawerSelected(String drawerName) {
    for (BlockDrawerSelectionListener listener : drawerListeners) {
      listener.onGenericDrawerSelected(drawerName);
    }
  }

}
