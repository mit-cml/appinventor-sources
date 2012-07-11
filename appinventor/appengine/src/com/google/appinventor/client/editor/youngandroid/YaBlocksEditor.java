package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.FormChangeListener;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.explorer.SourceStructureExplorerItem;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * Editor for Young Android Blocks (.blk) files.
 * 
 * TODO(sharon): blocks file loading and saving is not implemented yet!!
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author sharon@google.com (Sharon Perl) added Blockly functionality
 */
public final class YaBlocksEditor extends FileEditor
    implements FormChangeListener, BlockDrawerSelectionListener {

  private final YoungAndroidBlocksNode blocksNode;

  // References to other panels that we need to control.
  private final SourceStructureExplorer sourceStructureExplorer;

  // Panel that is used as the content of the palette box
  private final YoungAndroidPalettePanel palettePanel;

  // Blocks area. Note that the blocks area is a part of the "document" in the
  // browser (via the deckPanel in the ProjectEditor). So if the document changes (which happens
  // when we switch projects) we will lose the blocks editor state, even though 
  // YaBlocksEditor objects are kept around when switching projects. If we come 
  // back to this blocks editor after having switched projects, the blocksArea
  // will get reinitialized.
  private final BlocklyPanel blocksArea;

  // Database of component type descriptions
  private static final SimpleComponentDatabase COMPONENT_DATABASE =
      SimpleComponentDatabase.getInstance();

  // True once we've finished loading the current file.
  private boolean loadComplete = false;

  // if selectedDrawer != null, it is either "component_" + instance name or
  // "builtin_" + drawer name
  private String selectedDrawer = null;

  YaBlocksEditor(YaProjectEditor projectEditor, YoungAndroidBlocksNode blocksNode) {
    super(projectEditor, blocksNode);

    this.blocksNode = blocksNode;

    blocksArea = new BlocklyPanel(blocksNode.getProjectId() + "_" + blocksNode.getFormName());
    // We would like the blocks area to fill the available space automatically,
    // but apparently we need to give it a height or else it ends up too short.
    blocksArea.setSize("100%", "600px");

    initWidget(blocksArea);

    // Get references to the source structure explorer
    sourceStructureExplorer = BlockSelectorBox.getBlockSelectorBox().getSourceStructureExplorer();
    
    // Listen for selection events for built-in drawers
    BlockSelectorBox.getBlockSelectorBox().addBlockDrawerSelectionListener(this);

    // Create palettePanel, which will be used as the content of the PaletteBox.
    YaFormEditor myFormEditor = projectEditor.getFormFileEditor(blocksNode.getFormName());
    if (myFormEditor != null) {
      palettePanel = new YoungAndroidPalettePanel(myFormEditor);
      palettePanel.loadComponents(new DropTargetProvider() {
        // TODO(sharon): make the tree in the BlockSelectorBox a drop target
        @Override
        public DropTarget[] getDropTargets() {
          return new DropTarget[0];
        }
      });
      palettePanel.setSize("100%", "100%");
    } else {
      palettePanel = null;
      OdeLog.wlog("Can't get form editor for blocks: " + getFileId());
    }
  }

  // FileEditor methods

  @Override
  public void loadFile(final Command afterFileLoaded) {
    OdeAsyncCallback<String> callback = new OdeAsyncCallback<String>(MESSAGES.loadError()) {
      @Override
      public void onSuccess(String blkFileContent) {
        blocksArea.loadBlockContent(blkFileContent);
        loadComplete = true;
        selectedDrawer = null;
        if (afterFileLoaded != null) {
          afterFileLoaded.execute();
        }
      }
    };
    Ode.getInstance().getProjectService().load(getProjectId(), getFileId(), callback);
  }

  @Override
  public String getTabText() {
    return MESSAGES.blocksEditorTabName(blocksNode.getFormName());
  }

  @Override
  public void onShow() {
    OdeLog.log("YaBlocksEditor: got onShow() for " + getFileId());

    // When this editor is shown, update the "current" editor.
    Ode.getInstance().setCurrentFileEditor(this, getMyForm().getName());

    loadBlocksEditor();
  }

  /*
   * Updates the the whole designer: form, palette, source structure explorer, assets list, and
   * properties panel.
   */
  private void loadBlocksEditor() {

    // Set the palette box's content.
    if (palettePanel != null) {
      PaletteBox paletteBox = PaletteBox.getPaletteBox();
      paletteBox.setContent(palettePanel);
    }

    // Update the source structure explorer with the tree of this form's components.
    MockForm form = getMyForm();
    if (form != null) {
      // start with no component selected in sourceStructureExplorer. We
      // don't want a component drawer open in the blocks editor when we
      // come back to it.
      updateBlocksTree(form, null);
      BlockSelectorBox.getBlockSelectorBox().setVisible(true);
      hideComponentBlocks();
    } else {
      OdeLog.wlog("Can't get form editor for blocks: " + getFileId());
    }

    // Show the assets box.
    AssetListBox assetListBox = AssetListBox.getAssetListBox();
    assetListBox.setVisible(true);
  }

  @Override
  public void onHide() {
    // When an editor is detached, if we are the "current" editor,
    // set the current editor to null. 
    // Note: I'm not sure it is possible that we would not be the "current"
    // editor when this is called, but we check just to be safe.
    OdeLog.log("YaBlocksEditor: got onHide() for " + getFileId());
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      Ode.getInstance().setCurrentFileEditor(null, null);
      unloadBlocksEditor();
    } else {
      OdeLog.wlog("YaBlocksEditor.onHide: Not doing anything since we're not the "
          + "current file editor!");
    }
  }

  private void unloadBlocksEditor() {
    // TODO(sharon): do something about form change listener?

    // Clear the palette box.
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.clear();

    // Clear and hide the blocks selector tree
    sourceStructureExplorer.clearTree();
    BlockSelectorBox.getBlockSelectorBox().setVisible(false);

    // Hide the assets box.
    AssetListBox assetListBox = AssetListBox.getAssetListBox();
    assetListBox.setVisible(false);

    hideComponentBlocks();
  }

  private void updateBlocksTree(MockForm form, SourceStructureExplorerItem itemToSelect) {
    TreeItem items[] = new TreeItem[2];
    items[0] = BlockSelectorBox.getBuiltInBlocksTree();
    items[1] = form.buildComponentsTree();
    sourceStructureExplorer.updateTree(items, itemToSelect);
  }

  // Do whatever is needed to save Blockly state when our project is about to be
  // detached from the parent document
  public void prepareForUnload() {
    blocksArea.saveComponents();
  }

  @Override
  public String getRawFileContent() {
    return blocksArea.getBlockContent();
  }

  @Override
  public void onSave() {
    // Nothing to do after blocks are saved.
  }

  public void addComponent(String typeName, String instanceName, String uid) {
    String typeDescription = COMPONENT_DATABASE.getTypeDescription(typeName);
    blocksArea.addComponent(typeDescription, instanceName, uid);
  }

  public void removeComponent(String typeName, String instanceName, String uid) {
    blocksArea.removeComponent(typeName, instanceName, uid);
  }

  public void renameComponent(String typeName, String oldName, String newName, String uid) {
    blocksArea.renameComponent(typeName, oldName, newName, uid);
  }

  public void showComponentBlocks(String instanceName) {
    String instanceDrawer = "component_" + instanceName;
    if (selectedDrawer == null || !blocksArea.drawerShowing()
        || !selectedDrawer.equals(instanceDrawer)) {
      blocksArea.showComponentBlocks(instanceName);
      selectedDrawer = instanceDrawer;
    } else {
      blocksArea.hideComponentBlocks();
      selectedDrawer = null;
    }
  }

  public void hideComponentBlocks() {
    blocksArea.hideComponentBlocks();
    selectedDrawer = null;
  }

  public void showBuiltinBlocks(String drawerName) {
    OdeLog.log("Showing built-in drawer " + drawerName);
    String builtinDrawer = "builtin_" + drawerName;
    if (selectedDrawer == null || !blocksArea.drawerShowing()
        || !selectedDrawer.equals(builtinDrawer)) {
      blocksArea.showBuiltinBlocks(drawerName);
      selectedDrawer = builtinDrawer;
    } else {
      blocksArea.hideBuiltinBlocks();
      selectedDrawer = null;
    }
  }

  public void hideBuiltinBlocks() {
    blocksArea.hideBuiltinBlocks();
  }

  public MockForm getMyForm() {
    YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
    YaFormEditor myFormEditor = yaProjectEditor.getFormFileEditor(blocksNode.getFormName());
    if (myFormEditor != null) {
      return myFormEditor.getForm();
    } else {
      return null;
    }
  }

  // FormChangeListener implementation
  // Note: our companion YaFormEditor adds us as a listener on the form

  /*
   * @see com.google.appinventor.client.editor.simple.components.FormChangeListener#
   * onComponentPropertyChanged
   * (com.google.appinventor.client.editor.simple.components.MockComponent, java.lang.String,
   * java.lang.String)
   */
  @Override
  public void onComponentPropertyChanged(
      MockComponent component, String propertyName, String propertyValue) {
    // nothing to do here
  }

  /*
   * @see
   * com.google.appinventor.client.editor.simple.components.FormChangeListener#onComponentRemoved
   * (com.google.appinventor.client.editor.simple.components.MockComponent, boolean)
   */
  @Override
  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (permanentlyDeleted) {
      removeComponent(component.getType(), component.getName(),
          component.getPropertyValue(MockComponent.PROPERTY_NAME_UUID));
      if (loadComplete) {
        updateSourceStructureExplorer();
      }
    }
  }

  /*
   * @see
   * com.google.appinventor.client.editor.simple.components.FormChangeListener#onComponentAdded
   * (com.google.appinventor.client.editor.simple.components.MockComponent)
   */
  @Override
  public void onComponentAdded(MockComponent component) {
    addComponent(component.getType(), component.getName(),
        component.getPropertyValue(MockComponent.PROPERTY_NAME_UUID));
    if (loadComplete) {
      // Update source structure panel
      updateSourceStructureExplorer();
    }
  }

  /*
   * @see
   * com.google.appinventor.client.editor.simple.components.FormChangeListener#onComponentRenamed
   * (com.google.appinventor.client.editor.simple.components.MockComponent, java.lang.String)
   */
  @Override
  public void onComponentRenamed(MockComponent component, String oldName) {
    renameComponent(component.getType(), oldName, component.getName(),
        component.getPropertyValue(MockComponent.PROPERTY_NAME_UUID));
    if (loadComplete) {
      updateSourceStructureExplorer();
      // renaming could potentially confuse an open drawer so close just in case
      hideComponentBlocks();
      selectedDrawer = null;
    }
  }
  
  private void updateSourceStructureExplorer() {
    MockForm form = getMyForm();
    if (form != null) {
      updateBlocksTree(form, form.getSelectedComponent().getSourceStructureExplorerItem());
    }
  }

  /*
   * @see com.google.appinventor.client.editor.simple.components.FormChangeListener#
   * onComponentSelectionChange
   * (com.google.appinventor.client.editor.simple.components.MockComponent, boolean)
   */
  @Override
  public void onComponentSelectionChange(MockComponent component, boolean selected) {
    // not relevant for blocks editor - this happens on clicks in the mock form areas
  }
  
  // BlockDrawerSelectionListener implementation
  
  /*
   * @see com.google.appinventor.client.editor.youngandroid.BlockDrawerSelectionListener#
   * onBlockDrawerSelected(java.lang.String)
   */
  @Override
  public void onBuiltinDrawerSelected(String drawerName) {
    showBuiltinBlocks(drawerName);
  }

  /*
   * @see com.google.appinventor.client.editor.youngandroid.BlockDrawerSelectionListener#
   * onBlockDrawerUnselected(java.lang.String)
   */
  @Override
  public void onBuiltinDrawerUnselected(String drawerName) {
    hideBuiltinBlocks();
  }
}
