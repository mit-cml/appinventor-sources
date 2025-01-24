// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2009-2011 Google, All Rights reserved
// Copyright © 2011-2016 Massachusetts Institute of Technology, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.ErrorReporter;
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
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel.BlocklyWorkspaceChangeListener;
import com.google.appinventor.client.editor.youngandroid.events.EventHelper;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.explorer.SourceStructureExplorerItem;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Editor for Young Android Blocks (.blk) files.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author sharon@google.com (Sharon Perl) added Blockly functionality
 */
public final class YaBlocksEditor extends FileEditor
    implements FormChangeListener, BlockDrawerSelectionListener, ComponentDatabaseChangeListener,
    BlocklyWorkspaceChangeListener, ProjectChangeListener {

  private static final Logger LOG = Logger.getLogger(YaBlocksEditor.class.getName());

  // A constant to substract from the total height of the Viewer window, set through
  // the computed height of the user's window (Window.getClientHeight())
  // This is an approximation of the size of the header navigation panel
  private static final int VIEWER_WINDOW_OFFSET = 170;

  // Database of component type descriptions
  private final SimpleComponentDatabase COMPONENT_DATABASE;

  // Keep a map from projectid_formname -> YaBlocksEditor for handling blocks workspace changed
  // callbacks from the BlocklyPanel objects. This has to be static because it is used by
  // static methods that are called from the Javascript Blockly world.
  private static final Map<String, YaBlocksEditor> formToBlocksEditor = Maps.newHashMap();

  // projectid_formname for this blocks editor. Our index into the static formToBlocksEditor map.
  private String fullFormName;

  private final YoungAndroidBlocksNode blocksNode;

  // References to other panels that we need to control.
  private final SourceStructureExplorer sourceStructureExplorer;

  // Panel that is used as the content of the palette box
  private YoungAndroidPalettePanel palettePanel;

  // Blocks area. Note that the blocks area is a part of the "document" in the
  // browser (via the deckPanel in the ProjectEditor). So if the document changes (which happens
  // when we switch projects) we will lose the blocks editor state, even though
  // YaBlocksEditor objects are kept around when switching projects. If we come
  // back to this blocks editor after having switched projects, the blocksArea
  // will get reinitialized.
  private final BlocklyPanel blocksArea;

  // True once we've finished loading the current file.
  private boolean loadComplete = false;

  // if selectedDrawer != null, it is either "component_" + instance name or
  // "builtin_" + drawer name
  private String selectedDrawer = null;

  // Keep a list of components that we know about. Need this to detect when a call to add a
  // component is adding one that we already have (which can happen when a component gets
  // moved from one container to another). In that case we do not want to add it to the
  // blocks area again.
  private Set<String> componentUuids = new HashSet<String>();

  // The form editor associated with this blocks editor.
  private YaFormEditor myFormEditor;

  // The project associated with this blocks editor.
  private Project project;

  YaBlocksEditor(YaProjectEditor projectEditor, YoungAndroidBlocksNode blocksNode) {
    super(projectEditor, blocksNode);

    this.blocksNode = blocksNode;
    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance(getProjectId());

    fullFormName = blocksNode.getProjectId() + "_" + blocksNode.getFormName();
    formToBlocksEditor.put(fullFormName, this);
    blocksArea = new BlocklyPanel(this, fullFormName); // [lyn, 2014/10/28] pass in editor so can extract form json from it
    blocksArea.setWidth("100%");
    // This code seems to be using a rather old layout, so we cannot simply pass 100% for height.
    // Instead, it needs to be calculated from the client's window, and a listener added to Window
    // We use VIEWER_WINDOW_OFFSET as an approximation of the size of the top navigation bar
    // New layouts don't need all this messing; see comments on selected answer at:
    // http://stackoverflow.com/questions/86901/creating-a-fluid-panel-in-gwt-to-fill-the-page
    initWidget(blocksArea);
    blocksArea.populateComponentTypes(COMPONENT_DATABASE.getComponentsJSONString());

    // Get references to the source structure explorer
    sourceStructureExplorer = BlockSelectorBox.getBlockSelectorBox().getSourceStructureExplorer();

    // Listen for selection events for built-in drawers
    BlockSelectorBox.getBlockSelectorBox().addBlockDrawerSelectionListener(this);

    project = Ode.getInstance().getProjectManager().getProject(blocksNode.getProjectId());
    project.addProjectChangeListener(this);
    onProjectLoaded(project);
  }

  /**
   * Sets the form editor associated with this blocks editor.
   *
   * @param editor the form editor
   */
  public void setFormEditor(YaFormEditor editor) {
    // Create palettePanel, which will be used as the content of the PaletteBox.
    myFormEditor = editor;
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
      LOG.warning("Can't get form editor for blocks: " + getFileId());
    }
  }

  // FileEditor methods

  @Override
  public DropTargetProvider getDropTargetProvider() {
    return new DropTargetProvider() {
      // TODO(sharon): make the tree in the BlockSelectorBox a drop target
      @Override
      public DropTarget[] getDropTargets() {
        return new DropTarget[0];
      }
    };
  }

  @Override
  public void loadFile(final Command afterFileLoaded) {
    final long projectId = getProjectId();
    final String fileId = getFileId();
    OdeAsyncCallback<ChecksumedLoadFile> callback = new OdeAsyncCallback<ChecksumedLoadFile>(MESSAGES.loadError()) {
      @Override
      public void onSuccess(ChecksumedLoadFile result) {
        String blkFileContent;
        try {
          blkFileContent = result.getContent();
        } catch (ChecksumedFileException e) {
          this.onFailure(e);
          return;
        }
        String formJson = myFormEditor.preUpgradeJsonString(); // [lyn, 2014/10/27] added formJson for upgrading
        try {
          blocksArea.loadBlocksContent(formJson, blkFileContent);
          blocksArea.addChangeListener(YaBlocksEditor.this);
        } catch(LoadBlocksException e) {
          setBlocksDamaged(fullFormName);
          ErrorReporter.reportError(MESSAGES.blocksNotSaved(fullFormName));
        }
        loadComplete = true;
        selectedDrawer = null;
        if (afterFileLoaded != null) {
          afterFileLoaded.execute();
        }
      }
      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof ChecksumedFileException) {
          Ode.getInstance().recordCorruptProject(projectId, fileId, caught.getMessage());
        }
        super.onFailure(caught);
      }
    };
    Ode.getInstance().getProjectService().load2(projectId, fileId, callback);
  }

  @Override
  public String getTabText() {
    return MESSAGES.blocksEditorTabName(blocksNode.getFormName());
  }

  @Override
  public void onShow() {
    LOG.info("YaBlocksEditor: got onShow() for " + getFileId());
    super.onShow();
    loadBlocksEditor();
    blocksArea.setBlocklyVisible(true);
    Tracking.trackEvent(Tracking.EDITOR_EVENT, Tracking.EDITOR_ACTION_SHOW_BLOCKS);
    sendComponentData();  // Send Blockly the component information for generating Yail
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
    PaletteBox.getPaletteBox().setVisible(false);

    // Update the source structure explorer with the tree of this form's components.
    MockForm form = getForm();
    if (form != null) {
      // start with no component selected in sourceStructureExplorer. We
      // don't want a component drawer open in the blocks editor when we
      // come back to it.
      updateBlocksTree(form, null);

      Ode.getInstance().getWorkColumns().remove(Ode.getInstance().getStructureAndAssets()
          .getWidget(2));
      Ode.getInstance().getWorkColumns().insert(Ode.getInstance().getStructureAndAssets(), 1);
      Ode.getInstance().getStructureAndAssets().insert(BlockSelectorBox.getBlockSelectorBox(), 0);
      BlockSelectorBox.getBlockSelectorBox().setVisible(true);
      AssetListBox.getAssetListBox().setVisible(true);
      blocksArea.injectWorkspace();
      hideComponentBlocks();
    } else {
      LOG.warning("Can't get form editor for blocks: " + getFileId());
    }
  }

  @Override
  public void onHide() {
    // When an editor is detached, if we are the "current" editor,
    // set the current editor to null and clean up the UI.
    // Note: I'm not sure it is possible that we would not be the "current"
    // editor when this is called, but we check just to be safe.
    LOG.info("YaBlocksEditor: got onHide() for " + getFileId());
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      super.onHide();
      blocksArea.setBlocklyVisible(false);
      unloadBlocksEditor();
    } else {
      LOG.warning("YaBlocksEditor.onHide: Not doing anything since we're not the "
          + "current file editor!");
    }
  }

  @Override
  public void onClose() {
    // our partner YaFormEditor added us as a FormChangeListener, but we remove ourself.
    getForm().removeFormChangeListener(this);
    project.removeProjectChangeListener(this);
    BlockSelectorBox.getBlockSelectorBox().removeBlockDrawerSelectionListener(this);
    formToBlocksEditor.remove(fullFormName);

  }

  public static void toggleWarning() {
    BlocklyPanel.switchWarningVisibility();
    for(YaBlocksEditor editor : formToBlocksEditor.values()){
      editor.blocksArea.toggleWarning();
    }
  }

  private void unloadBlocksEditor() {
    // TODO(sharon): do something about form change listener?

    // Clear the palette box.
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.clear();
    paletteBox.setVisible(true);

    Ode.getInstance().getWorkColumns().remove(Ode.getInstance().getStructureAndAssets().getWidget(0));
    Ode.getInstance().getWorkColumns().insert(Ode.getInstance().getStructureAndAssets(), 3);
    Ode.getInstance().getStructureAndAssets().insert(BlockSelectorBox.getBlockSelectorBox(), 0);
    BlockSelectorBox.getBlockSelectorBox().setVisible(false);
    AssetListBox.getAssetListBox().setVisible(true);

    // Clear and hide the blocks selector tree
    sourceStructureExplorer.clearTree();
    hideComponentBlocks();
    blocksArea.hideChaff();
  }

  @Override
  public void onWorkspaceChange(BlocklyPanel panel, JavaScriptObject event) {
    if (!EventHelper.isTransient(event)) {
      Ode.getInstance().getEditorManager().scheduleAutoSave(this);
    }
    if (!EventHelper.isUi(event)) {
      sendComponentData();
    }
  }

  @Override
  public void getBlocksImage(Callback<String, String> callback) {
    blocksArea.getBlocksImage(callback);
  }

  public synchronized void sendComponentData() {
    sendComponentData(false);
  }

  public synchronized void sendComponentData(boolean force) {
    try {
      blocksArea.sendComponentData(myFormEditor.encodeFormAsJsonString(true), packageNameFromPath(getFileId()), force);
    } catch (YailGenerationException e) {
      e.printStackTrace();
    }
  }

  private void updateBlocksTree(MockForm form, SourceStructureExplorerItem itemToSelect) {
    TreeItem items[] = new TreeItem[3];
    items[0] = BlockSelectorBox.getBlockSelectorBox().getBuiltInBlocksTree(form);
    items[1] = form.buildComponentsTree();
    items[2] = BlockSelectorBox.getBlockSelectorBox().getGenericComponentsTree(form);
    sourceStructureExplorer.updateTree(items, itemToSelect);
  }

  // Do whatever is needed to save Blockly state when our project is about to be
  // detached from the parent document. Note that this is not for saving the blocks file itself.
  // We use EditorManager.scheduleAutoSave for that.
  public void prepareForUnload() {
    blocksArea.saveComponentsAndBlocks();
//    blocksArea.saveBackpackContents();
  }

  @Override
  public String getRawFileContent() {
    return blocksArea.getBlocksContent();
  }

  public Set<String> getBlockTypeSet() {
    Set<String> blockTypes = new HashSet<String>();
    String xmlString = blocksArea.getBlocksContent();
    Document blockDoc = XMLParser.parse(xmlString);
    NodeList blockElements = blockDoc.getElementsByTagName("block");
    for (int i = 0; i < blockElements.getLength(); ++i) {
      Element blockElem = (Element) blockElements.item(i);
      blockTypes.add(blockElem.getAttribute("type"));
    }
    return blockTypes;
  }

  // This creates a hash of sets. The key is the name of a blocktype. The set is the names of
  // component blocks (events, methods, and properties) that are used in the current project.
  // The method takes the a hash of sets as an input so that it may be called multiple times
  // for separate screens, creating the set of component blocks used through the entire project.
  // TODO: Examine refactor with XPATH
  public HashMap<String, Set<String>> getComponentBlockTypeSet(HashMap<String, Set<String>> componentBlocks) {
    String xmlString = blocksArea.getBlocksContent();
    Document blockDoc = XMLParser.parse(xmlString);
    NodeList blockElements = blockDoc.getElementsByTagName("block");
    for (int i = 0; i < blockElements.getLength(); ++i) {
      Element blockElem = (Element) blockElements.item(i);
      String blockType = blockElem.getAttribute("type");
      if ("component_event".equals(blockType)) {
        Element mutElem = (Element) blockElem.getElementsByTagName("mutation").item(0);
        String component_type = mutElem.getAttribute("component_type");
        String event_name = mutElem.getAttribute("event_name");
        Set<String> blockTypes = componentBlocks.get(component_type) == null ? new HashSet<String>() : componentBlocks.get(component_type);
        blockTypes.add(event_name);
        componentBlocks.put(component_type, blockTypes);
      } else if ("component_method".equals(blockType)) {
        Element mutElem = (Element) blockElem.getElementsByTagName("mutation").item(0);
        String component_type = mutElem.getAttribute("component_type");
        String method_name = mutElem.getAttribute("method_name");
        Set<String> blockTypes = componentBlocks.get(component_type) == null ? new HashSet<String>() : componentBlocks.get(component_type);
        blockTypes.add(method_name);
        componentBlocks.put(component_type, blockTypes);
      } else if ("component_set_get".equals(blockType)) {
        Element mutElem = (Element) blockElem.getElementsByTagName("mutation").item(0);
        String component_type = mutElem.getAttribute("component_type");
        String property_name = mutElem.getAttribute("property_name");
        Set<String> blockTypes = componentBlocks.get(component_type) == null ? new HashSet<String>() : componentBlocks.get(component_type);
        blockTypes.add(property_name);
        componentBlocks.put(component_type, blockTypes);
      }
    }
    return componentBlocks;
  }

  public FileDescriptorWithContent getYail() throws YailGenerationException {
    return new FileDescriptorWithContent(getProjectId(), yailFileName(),
        blocksArea.getYail(myFormEditor.encodeFormAsJsonString(true),
            packageNameFromPath(getFileId())));
  }

  /**
   * Converts a source file path (e.g.,
   * src/com/gmail/username/project1/Form.extension) into a package
   * name (e.g., com.gmail.username.project1.Form)
   * @param path the path to convert.
   * @return a dot separated package name.
   */
  private static String packageNameFromPath(String path) {
    path = path.replaceFirst("src/", "");
    int extensionIndex = path.lastIndexOf(".");
    if (extensionIndex != -1) {
      path = path.substring(0, extensionIndex);
    }
    return path.replaceAll("/", ".");
  }

  @Override
  public void onSave() {
    // Nothing to do after blocks are saved.
  }

  public static String getComponentInfo(String typeName) {
    return SimpleComponentDatabase.getInstance().getTypeDescription(typeName);
  }

  public static String getComponentsJSONString(long projectId) {
    return SimpleComponentDatabase.getInstance(projectId).getComponentsJSONString();
  }

  public static String getComponentInstanceTypeName(String formName, String instanceName) {
      //use form name to get blocks editor
      YaBlocksEditor blocksEditor = formToBlocksEditor.get(formName);
      //get type name from form editor
      return blocksEditor.myFormEditor.getComponentInstanceTypeName(instanceName);
  }

  public static String getComponentInstancePropertyValue(String formName, String instanceName, String propertyName){
      //use form name to get blocks editor
      YaBlocksEditor blocksEditor = formToBlocksEditor.get(formName);
      Map<String, MockComponent> componentMap = blocksEditor.myFormEditor.getComponents();
      for (String key : componentMap.keySet()) {
        LOG.info(key);
      }
      MockComponent mockComponent = componentMap.get(instanceName);
      return mockComponent.getPropertyValue(propertyName);
  }

  public void addComponent(String typeName, String instanceName, String uuid) {
    if (componentUuids.add(uuid)) {
      blocksArea.addComponent(uuid, instanceName, typeName);
    }
  }

  public void removeComponent(String typeName, String instanceName, String uuid) {
    if (componentUuids.remove(uuid)) {
      blocksArea.removeComponent(uuid);
    }
  }

  public void renameComponent(String oldName, String newName, String uuid) {
    blocksArea.renameComponent(uuid, oldName, newName);
  }

  public void showComponentBlocks(String instanceName) {
    String instanceDrawer = "component_" + instanceName;
    if (selectedDrawer == null || !blocksArea.drawerShowing()
        || !selectedDrawer.equals(instanceDrawer)) {
      blocksArea.showComponentBlocks(instanceName);
      selectedDrawer = instanceDrawer;
    } else {
      blocksArea.hideDrawer();
      selectedDrawer = null;
    }
  }

  public void hideComponentBlocks() {
    blocksArea.hideDrawer();
    selectedDrawer = null;
  }

  public void showBuiltinBlocks(String drawerName) {
    LOG.info("Showing built-in drawer " + drawerName);
    String builtinDrawer = "builtin_" + drawerName;
    if (selectedDrawer == null || !blocksArea.drawerShowing()
        || !selectedDrawer.equals(builtinDrawer)) {
      blocksArea.showBuiltinBlocks(drawerName);
      selectedDrawer = builtinDrawer;
    } else {
      blocksArea.hideDrawer();
      selectedDrawer = null;
    }
  }

  public void showGenericBlocks(String drawerName) {
    LOG.info("Showing generic drawer " + drawerName);
    String genericDrawer = "generic_" + drawerName;
    if (selectedDrawer == null || !blocksArea.drawerShowing()
        || !selectedDrawer.equals(genericDrawer)) {
      blocksArea.showGenericBlocks(drawerName);
      selectedDrawer = genericDrawer;
    } else {
      blocksArea.hideDrawer();
      selectedDrawer = null;
    }
  }

  public void hideBuiltinBlocks() {
    blocksArea.hideDrawer();
  }

  public MockForm getForm() {
    YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
    YaFormEditor myFormEditor = yaProjectEditor.getFormFileEditor(blocksNode.getFormName());
    if (myFormEditor != null) {
      return myFormEditor.getForm();
    } else {
      return null;
    }
  }

  private String yailFileName() {
    String fileId = getFileId();
    return fileId.replace(YoungAndroidSourceAnalyzer.BLOCKLY_SOURCE_EXTENSION,
        YoungAndroidSourceAnalyzer.YAIL_FILE_EXTENSION);
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
      removeComponent(component.getType(), component.getName(), component.getUuid());
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
    addComponent(component.getType(), component.getName(), component.getUuid());
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
    renameComponent(oldName, component.getName(), component.getUuid());
    if (loadComplete) {
      updateSourceStructureExplorer();
      // renaming could potentially confuse an open drawer so close just in case
      hideComponentBlocks();
      selectedDrawer = null;
    }
  }

  private void updateSourceStructureExplorer() {
    MockForm form = getForm();
    if (form != null) {
      updateBlocksTree(form, form.getLastSelectedComponent().getSourceStructureExplorerItem());
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
    // Only do something if we are the current file editor
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      showBuiltinBlocks(drawerName);
    }
  }

  /*
   * @see com.google.appinventor.client.editor.youngandroid.BlockDrawerSelectionListener#
   * onBlockDrawerSelected(java.lang.String)
   */
  @Override
  public void onGenericDrawerSelected(String drawerName) {
    // Only do something if we are the current file editor
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      showGenericBlocks(drawerName);
    }
  }

  /*
   * Start up the Repl (call into the Blockly.ReplMgr via the BlocklyPanel.
   */
  @Override
  public void startRepl(boolean alreadyRunning, boolean forChromebook, boolean forEmulator, boolean forUsb) {
    blocksArea.startRepl(alreadyRunning, forChromebook, forEmulator, forUsb);
  }

  /*
   * Perform a Hard Reset of the Emulator
   */
  public void hardReset() {
    blocksArea.hardReset();
  }

  /*
   * Perform a hideChaff of Blockly
   */
  public void hideChaff () {blocksArea.hideChaff();}

  @Override
  public void resize() {
    blocksArea.resize();
  }

  // Static Function. Find the associated editor for formName and
  // set its "damaged" bit. This will cause the editor manager's scheduleAutoSave
  // method to ignore this blocks file and not save it out.

  public static void setBlocksDamaged(String formName) {
    YaBlocksEditor editor = formToBlocksEditor.get(formName);
    if (editor != null) {
      editor.setDamaged(true);
    }
  }

  /*
   * Trigger a Companion Update
   */
  @Override
  public void updateCompanion() {
    blocksArea.updateCompanion();
  }

  /*
   * [lyn, 2014/10/28] Added for accessing current form json from BlocklyPanel
   * Encodes the associated form's properties as a JSON encoded string. Used by YaBlocksEditor as well,
   * to send the form info to the blockly world during code generation.
   */
  protected String encodeFormAsJsonString(boolean forYail) {
    return myFormEditor.encodeFormAsJsonString(forYail);
  }

  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {
    blocksArea.populateComponentTypes(COMPONENT_DATABASE.getComponentsJSONString());
    blocksArea.verifyAllBlocks();
  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    return true;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {
    blocksArea.populateComponentTypes(COMPONENT_DATABASE.getComponentsJSONString());
    blocksArea.verifyAllBlocks();
  }

  @Override
  public void onResetDatabase() {
    blocksArea.populateComponentTypes(COMPONENT_DATABASE.getComponentsJSONString());
    blocksArea.verifyAllBlocks();
  }

  @Override
  public void makeActiveWorkspace() {
    blocksArea.makeActive();
  }

  @Override
  public void onProjectLoaded(Project project) {
    for (ProjectNode node : project.getRootNode().getAllSourceNodes()) {
      if (node instanceof YoungAndroidFormNode) {
        blocksArea.addScreen(((YoungAndroidSourceNode) node).getFormName());
      }
    }
    YoungAndroidAssetsFolder assetsFolder = ((YoungAndroidProjectNode) project.getRootNode())
        .getAssetsFolder();
    for (ProjectNode node : assetsFolder.getChildren()) {
      blocksArea.addAsset(((YoungAndroidAssetNode) node).getName());
    }
  }

  @Override
  public void onProjectNodeAdded(Project project, ProjectNode node) {
    if (node instanceof YoungAndroidSourceNode) {
      blocksArea.addScreen(((YoungAndroidSourceNode) node).getFormName());
    } else if (node instanceof YoungAndroidAssetNode) {
      blocksArea.addAsset(((YoungAndroidAssetNode) node).getName());
    }
  }


  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
    if (node instanceof YoungAndroidSourceNode) {
      blocksArea.removeScreen(((YoungAndroidSourceNode) node).getFormName());
    } else if (node instanceof YoungAndroidAssetNode) {
      blocksArea.removeAsset(((YoungAndroidAssetNode) node).getName());
    }
  }

  private static native void set(JavaScriptObject jso, String key, String value)/*-{
    jso[key] = value;
  }-*/;

  /**
   * Converts a Java Map from String to String into a JSON object with the same contents.
   * @param javaMap The source mapping in Java
   * @return A JSON object with the same key-value mapping as {@code javaMap}
   */
  public static JavaScriptObject toJSO(Map<String, String> javaMap) {
    JavaScriptObject jso = JavaScriptObject.createObject();
    for (Map.Entry<String, String> entry : javaMap.entrySet()) {
      set(jso, entry.getKey(), entry.getValue());
    }
    return jso;
  }

  /**
   *
   * @param array
   * @return
   */
  public static JsArrayString toJsArrayString(JSONArray array) {
    JsArrayString result = (JsArrayString) JsArrayString.createArray();
    for (JSONValue v : array.getElements()) {
      result.push(v.asString().getString());
    }
    return result;
  }

  public native void pasteFromJSNI(JavaScriptObject componentSubstitutionMap, JsArrayString blocks)/*-{
    var workspace = this.@com.google.appinventor.client.editor.youngandroid.YaBlocksEditor::blocksArea.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace;
    if ($wnd.Blockly.Events.isEnabled()) {
      $wnd.Blockly.Events.setGroup(true);
    }
    blocks.forEach(function(blockXml) {
      var dom = $wnd.Blockly.Xml.textToDom(blockXml);
      var mutations = dom.getElementsByTagName('mutation');
      for (var i = 0; i < mutations.length; i++) {
        var mutation = mutations[i];
        var instanceName = mutation.getAttribute('instance_name');
        if (instanceName && instanceName in componentSubstitutionMap) {
          mutation.setAttribute('instance_name', componentSubstitutionMap[instanceName]);
        }
      }
      var fields = dom.getElementsByTagName('field');
      for (var i = 0; i < fields.length; i++) {
        var field = fields[i];
        if (field.getAttribute('name') === 'COMPONENT_SELECTOR') {
          if (field.textContent in componentSubstitutionMap) {
            field.firstChild.nodeValue = componentSubstitutionMap[field.textContent];
          }
        }
      }
      try {
        var block = $wnd.Blockly.Xml.domToBlock(dom.firstElementChild, workspace);
        var blockX = parseInt(dom.firstElementChild.getAttribute('x'), 10);
        var blockY = parseInt(dom.firstElementChild.getAttribute('y'), 10);
        if (!isNaN(blockX) && !isNaN(blockY)) {
          if (workspace.RTL) {
            blockX = -blockX;
          }
          // Offset block until not clobbering another block and not in connection
          // distance with neighbouring blocks.
          do {
            var collide = false;
            var allBlocks = workspace.getAllBlocks();
            for (var i = 0, otherBlock; otherBlock = allBlocks[i]; i++) {
              var otherXY = otherBlock.getRelativeToSurfaceXY();
              if (Math.abs(blockX - otherXY.x) <= 1 &&
                Math.abs(blockY - otherXY.y) <= 1) {
                collide = true;
                break;
              }
            }
            if (!collide) {
              // Check for blocks in snap range to any of its connections.
              var connections = block.getConnections_(false);
              for (var i = 0, connection; connection = connections[i]; i++) {
                var neighbour = connection.closest($wnd.Blockly.SNAP_RADIUS,
                  new $wnd.goog.math.Coordinate(blockX, blockY));
                if (neighbour.connection) {
                  collide = true;
                  break;
                }
              }
            }
            if (collide) {
              if (workspace.RTL) {
                blockX -= $wnd.Blockly.SNAP_RADIUS;
              } else {
                blockX += $wnd.Blockly.SNAP_RADIUS;
              }
              blockY += $wnd.Blockly.SNAP_RADIUS * 2;
            }
          } while (collide);
          block.moveBy(blockX, blockY);
        }
        if (workspace.rendered) {
          block.initSvg();
          workspace.requestRender(block);
        }
      } catch(e) {
        console.log(e);
      }
    });
    if ($wnd.Blockly.Events.isEnabled()) {
      $wnd.Blockly.Events.setGroup(false);
    }
  }-*/;

  public native JsArrayString getTopBlocksForComponentByName(String name)/*-{
    var workspace = this.@com.google.appinventor.client.editor.youngandroid.YaBlocksEditor::blocksArea.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace;
    var topBlocks = workspace.getTopBlocks();
    var result = [];
    for (var i = 0, block; block = topBlocks[i]; i++) {
      if (block.instanceName === name) {
        result.push('<xml>' + $wnd.Blockly.Xml.domToText($wnd.Blockly.Xml.blockToDomWithXY(block)) + '</xml>');
      }
    }
    return result;
  }-*/;

  public static native void resendAssetsAndExtensions()/*-{
    if (top.ReplState && (top.ReplState.state == $wnd.Blockly.ReplMgr.rsState.CONNECTED ||
                          top.ReplState.state == $wnd.Blockly.ReplMgr.rsState.EXTENSIONS ||
                          top.ReplState.state == $wnd.Blockly.ReplMgr.rsState.ASSET)) {
      $wnd.Blockly.ReplMgr.resendAssetsAndExtensions();
    }
  }-*/;

  public static native void resendExtensionsList()/*-{
    if (top.ReplState && top.ReplState.state == $wnd.Blockly.ReplMgr.rsState.CONNECTED) {
      $wnd.Blockly.ReplMgr.loadExtensions();
    }
  }-*/;

}
