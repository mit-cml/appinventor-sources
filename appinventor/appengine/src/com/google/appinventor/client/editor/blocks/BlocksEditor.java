// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2012-2025 MIT, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.blocks.BlocklyPanel.BlocklyWorkspaceChangeListener;
import com.google.appinventor.client.editor.designer.DesignerChangeListener;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.events.EventHelper;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.explorer.SourceStructureExplorerItem;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.SourceNode;
import com.google.appinventor.shared.simple.ComponentDatabaseChangeListener;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * BlocksEditor is an ancestor of all blocks editors in App Inventor.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class BlocksEditor<S extends SourceNode, T extends DesignerEditor<?, ?, ?, ?, ?>>
    extends FileEditor implements DesignerChangeListener, ComponentDatabaseChangeListener,
    BlockDrawerSelectionListener, BlocklyWorkspaceChangeListener {
  public static final String EDITOR_TYPE = BlocksEditor.class.getSimpleName();
  protected static final Images IMAGES = Ode.getImageBundle();
  private static final Logger LOG = Logger.getLogger(BlocksEditor.class.getName());

  // A constant to substract from the total height of the Viewer window, set through
  // the computed height of the user's window (Window.getClientHeight())
  // This is an approximation of the size of the header navigation panel
  private static final int VIEWER_WINDOW_OFFSET = 170;

  protected final BlocksLanguage language;
  protected final ComponentDatabaseInterface componentDatabase;
  protected final S blocksNode;

  // Panel that is used as the content of the palette box
  protected SimplePalettePanel palettePanel;

  // Blocks area. Note that the blocks area is a part of the "document" in the
  // browser (via the deckPanel in the ProjectEditor). So if the document changes (which happens
  // when we switch projects) we will lose the blocks editor state, even though
  // YaBlocksEditor objects are kept around when switching projects. If we come
  // back to this blocks editor after having switched projects, the blocksArea
  // will get reinitialized.
  protected final BlocklyPanel blocksArea;

  // projectid_formname for this blocks editor. Our index into the static formToBlocksEditor map.
  protected final String entityName;
  protected T designer;

  // References to other panels that we need to control.
  private final SourceStructureExplorer sourceStructureExplorer;

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

  // Keep a map from projectid_formname -> YaBlocksEditor for handling blocks workspace changed
  // callbacks from the BlocklyPanel objects. This has to be static because it is used by
  // static methods that are called from the Javascript Blockly world.
  private static Map<String, BlocksEditor<?, ?>> formToBlocksEditor = new HashMap<String, BlocksEditor<?, ?>>();

  /**
   * Creates a {@code FileEditor} instance.
   *
   * @param projectEditor the project editor that contains this file editor
   * @param blocksNode      FileNode associated with this file editor
   */
  public BlocksEditor(ProjectEditor projectEditor, S blocksNode, int systemVersion,
                      BlocksLanguage language, BlocksCodeGenerationTarget target,
                      ComponentDatabaseInterface componentDatabase) {
    super(projectEditor, blocksNode);
    this.blocksNode = blocksNode;
    this.language = language;
    this.componentDatabase = componentDatabase;
    entityName = blocksNode.getProjectId() + "_" + blocksNode.getEntityName();
    blocksArea = new BlocklyPanel(entityName, target);
    blocksArea.setLanguageVersion(systemVersion, language.getVersion());
    blocksArea.setWidth("100%");
    // This code seems to be using a rather old layout, so we cannot simply pass 100% for height.
    // Instead, it needs to be calculated from the client's window, and a listener added to Window
    // We use VIEWER_WINDOW_OFFSET as an approximation of the size of the top navigation bar
    // New layouts don't need all this messing; see comments on selected answer at:
    // http://stackoverflow.com/questions/86901/creating-a-fluid-panel-in-gwt-to-fill-the-page
    initWidget(blocksArea);
    blocksArea.populateComponentTypes(componentDatabase.getComponentsJSONString());

    // Get references to the source structure explorer
    sourceStructureExplorer = BlockSelectorBox.getBlockSelectorBox().getSourceStructureExplorer();

    // Listen for selection events for built-in drawers
    BlockSelectorBox.getBlockSelectorBox().addBlockDrawerSelectionListener(this);

    // Create palettePanel, which will be used as the content of the PaletteBox.
    designer = (T) projectEditor.getFileEditor(blocksNode.getEntityName(), DesignerEditor.EDITOR_TYPE);
    if (designer != null) {
      palettePanel = designer.getComponentPalettePanel().copy();
      palettePanel.loadComponents(new DropTargetProvider() {
        // TODO(sharon): make the tree in the BlockSelectorBox a drop target
        @Override
        public DropTarget[] getDropTargets() {
          return new DropTarget[0];
        }
      });
      ((Widget) palettePanel).setSize("100%", "100%");
    }

    formToBlocksEditor.put(entityName, this);
  }

  public void setDesigner(T designer) {
    if (this.designer == designer) {
      return;
    }
    this.designer = designer;
    if (designer != null) {
      palettePanel = designer.getComponentPalettePanel().copy();
      palettePanel.loadComponents(new DropTargetProvider() {
        @Override
        public DropTarget[] getDropTargets() {
          return new DropTarget[0];
        }
      });
    }
  }

  public abstract void prepareForUnload();

  public Set<String> getBlockTypeSet() {
    return null;
  }

  public HashMap<String, Set<String>> getComponentBlockTypeSet(HashMap<String, Set<String>> componentBlocks) {
    return null;
  }

  @Override
  public void makeActiveWorkspace() {
    blocksArea.makeActive();
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

  public void hideBlocksDrawer() {
    blocksArea.hideDrawer();
    selectedDrawer = null;
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

  public boolean isLoaded() {
    return loadComplete;
  }

  public WorkspaceSvg getWorkspace() {
    return blocksArea.getWorkspace();
  }

  // FileEditor implementation
  @Override
  public void loadFile(final Command afterFileLoaded) {
    loadFile(true, afterFileLoaded);
  }

  public void loadFile(final boolean upgrade, final Command afterFileLoaded) {
    if (loadComplete) {
      return;  // Already loaded
    }
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
        String designerJson = designer.getJson();
        try {
          blocksArea.loadBlocksContent(designerJson, blkFileContent, upgrade);
          blocksArea.addChangeListener(BlocksEditor.this);
        } catch(LoadBlocksException e) {
          setDamaged(true);
          ErrorReporter.reportError(MESSAGES.blocksNotSaved(entityName));
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
    return MESSAGES.blocksEditorTabName(blocksNode.getEntityName());
  }

  @Override
  public String getRawFileContent() {
    return blocksArea.getBlocksContent();
  }

  @Override
  public void onSave() {
    // Nothing to do after blocks are saved.
  }

  public String getComponentInfo(String typeName) {
    return componentDatabase.getTypeDescription(typeName);
  }

  public String getComponentsJSONString() {
    return componentDatabase.getComponentsJSONString();
  }

  public String getComponentInstanceTypeName(String instanceName) {
    //get type name from form editor
    return designer.getComponentInstanceTypeName(instanceName);
  }

  /**
   * Get the UUID for the parent component identified by {@code instanceName}. If the component
   * is not contained within another component (e.g., Form), the empty string is returned.
   *
   * @param instanceName the name of the component instance of interest
   * @return the parent component UUID
   */
  public String getComponentContainerUuid(String instanceName) {
    MockComponent component = designer.getComponents().get(instanceName);
    component = component.getContainer();
    if (component == null) {
      return "";
    } else {
      return component.getUuid();
    }
  }

  public String getComponentInstancePropertyValue(String instanceName, String propertyName) {
    Map<String, MockComponent> componentMap = designer.getComponents();
    MockComponent mockComponent = componentMap.get(instanceName);
    if (mockComponent == null) {
      return "";
    }
    return mockComponent.getPropertyValue(propertyName);
  }

  @Override
  public final String getEditorType() {
    return EDITOR_TYPE;
  }

  @Override
  public void getBlocksImage(Callback<String, String> callback) {
    blocksArea.getBlocksImage(callback);
  }

  // BlocksDrawerSelectionListener implementation
  @Override
  public void onBuiltinDrawerSelected(String drawerName) {
    // Only do something if we are the current file editor
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      showBuiltinBlocks(drawerName);
    }
  }

  @Override
  public void onGenericDrawerSelected(String drawerName) {
    // Only do something if we are the current file editor
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      showGenericBlocks(drawerName);
    }
  }

  // BlocklyWorkspaceChangeListener implementation
  @Override
  public void onWorkspaceChange(BlocklyPanel panel, JavaScriptObject event) {
    if (!EventHelper.isTransient(event)) {
      Ode.getInstance().getEditorManager().scheduleAutoSave(this);
    }
  }

  // ComponentDatabaseChangeListener implementation
  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {
    blocksArea.populateComponentTypes(componentDatabase.getComponentsJSONString());
    blocksArea.verifyAllBlocks();
  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    return true;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {
    blocksArea.populateComponentTypes(componentDatabase.getComponentsJSONString());
    blocksArea.verifyAllBlocks();
  }

  @Override
  public void onResetDatabase() {
    blocksArea.populateComponentTypes(componentDatabase.getComponentsJSONString());
    blocksArea.verifyAllBlocks();
  }

  // DesignerChangeListener
  // Note: our companion designer adds us as a listener on the form
  @Override
  public void onComponentPropertyChanged(MockComponent component, String propertyName, String propertyValue) {
    // nothing to do here
  }

  @Override
  public void onComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    if (permanentlyDeleted) {
      removeComponent(component.getType(), component.getName(), component.getUuid());
      if (loadComplete) {
        updateSourceStructureExplorer();
      }
    }
  }

  @Override
  public void onComponentAdded(MockComponent component) {
    addComponent(component.getType(), component.getName(), component.getUuid());
    if (loadComplete) {
      // Update source structure panel
      updateSourceStructureExplorer();
    }
  }

  @Override
  public void onComponentRenamed(MockComponent component, String oldName) {
    renameComponent(oldName, component.getName(), component.getUuid());
    if (loadComplete) {
      updateSourceStructureExplorer();
      // renaming could potentially confuse an open drawer so close just in case
      hideBlocksDrawer();
      selectedDrawer = null;
    }
  }

  @Override
  public void onComponentSelectionChange(MockComponent component, boolean selected) {
    // not relevant for blocks editor - this happens on clicks in the mock form areas
  }

  // BlocksEditor implementation
  @Override
  public void onShow() {
    super.onShow();
    loadBlocksEditor();
    blocksArea.setBlocklyVisible(true);
    Tracking.trackEvent(Tracking.EDITOR_EVENT, Tracking.EDITOR_ACTION_SHOW_BLOCKS);
  }

  @Override
  public void onHide() {
    // When an editor is detached, if we are the "current" editor,
    // set the current editor to null and clean up the UI.
    // Note: I'm not sure it is possible that we would not be the "current"
    // editor when this is called, but we check just to be safe.
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      blocksArea.setBlocklyVisible(false);
      unloadBlocksEditor();
    }
    super.onHide();
  }

  @Override
  public void onClose() {
    // our partner designer added us as a DesignerChangeListener, but we remove ourself.
    designer.getRoot().removeDesignerChangeListener(this);
    BlockSelectorBox.getBlockSelectorBox().removeBlockDrawerSelectionListener(this);
    blocksArea.removeChangeListener(this);
    formToBlocksEditor.remove(entityName);
  }

  public String getEntityName() {
    return entityName;
  }

  /**
   * Perform a hideChaff of Blockly
   */
  public void hideChaff() {
    blocksArea.hideChaff();
  }

  @Override
  public void resize() {
    blocksArea.resize();
  }

  public static void toggleWarning() {
    BlocklyPanel.switchWarningVisibility();
    for (BlocksEditor<?, ?> editor : formToBlocksEditor.values()) {
      editor.blocksArea.toggleWarning();
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
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocksEditor::blocksArea.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    if ($wnd.Blockly.Events.isEnabled()) {
      $wnd.Blockly.Events.setGroup(true);
    }
    blocks.forEach(function(blockXml) {
      var dom = $wnd.Blockly.utils.xml.textToDom(blockXml);
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
                var neighbour = connection.closest($wnd.Blockly.config.snapRadius,
                  new $wnd.goog.math.Coordinate(blockX, blockY));
                if (neighbour.connection) {
                  collide = true;
                  break;
                }
              }
            }
            if (collide) {
              if (workspace.RTL) {
                blockX -= $wnd.Blockly.config.snapRadius;
              } else {
                blockX += $wnd.Blockly.config.snapRadius;
              }
              blockY += $wnd.Blockly.config.snapRadius * 2;
            }
          } while (collide);
          block.moveBy(blockX, blockY);
        }
        if (workspace.rendered) {
          block.initSvg();
          block.queueRender();
        }
      } catch(e) {
        console.error(e);
      }
    });
    if ($wnd.Blockly.Events.isEnabled()) {
      $wnd.Blockly.Events.setGroup(false);
    }
  }-*/;

  public native JsArrayString getTopBlocksForComponentByName(String name)/*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocksEditor::blocksArea.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var topBlocks = workspace.getTopBlocks();
    var result = [];
    for (var i = 0, block; block = topBlocks[i]; i++) {
      if (block.instanceName === name) {
        result.push('<xml>' + $wnd.Blockly.Xml.domToText($wnd.Blockly.Xml.blockToDomWithXY(block)) + '</xml>');
      }
    }
    return result;
  }-*/;

  /**
   * Updates the the whole designer: form, palette, source structure explorer, assets list, and
   * properties panel.
   */
  private void loadBlocksEditor() {

    // Set the palette box's content.
    if (palettePanel != null) {
      PaletteBox paletteBox = PaletteBox.getPaletteBox();
      paletteBox.setContent(palettePanel.getWidget());
    }
    PaletteBox.getPaletteBox().setVisible(false);

    // Update the source structure explorer with the tree of this form's components.
    DesignerRootComponent root = designer == null ? null : designer.getRoot();
    if (root != null) {
      // start with no component selected in sourceStructureExplorer. We
      // don't want a component drawer open in the blocks editor when we
      // come back to it.
      updateBlocksTree(root, null);

      Ode.getInstance().getWorkColumns().remove(Ode.getInstance().getStructureAndAssets()
          .getWidget(2));
      Ode.getInstance().getWorkColumns().insert(Ode.getInstance().getStructureAndAssets(), 1);
      Ode.getInstance().getStructureAndAssets().insert(BlockSelectorBox.getBlockSelectorBox(), 0);
      BlockSelectorBox.getBlockSelectorBox().setVisible(true);
      AssetListBox.getAssetListBox().setVisible(true);
      blocksArea.injectWorkspace(Ode.getUserDarkThemeEnabled());
      hideBlocksDrawer();
    } else {
      LOG.warning("Can't get designer for blocks: " + getFileId());
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
    hideBlocksDrawer();
    blocksArea.hideChaff();
  }

  // private implementation
  private void updateSourceStructureExplorer() {
    DesignerRootComponent root = designer == null ? null : designer.getRoot();
    if (root != null) {
      updateBlocksTree(root, root.getLastSelectedComponent().getSourceStructureExplorerItem());
    }
  }

  private void updateBlocksTree(DesignerRootComponent root,
                                SourceStructureExplorerItem itemToSelect) {
    TreeItem items[] = new TreeItem[3];
    items[0] = BlockSelectorBox.getBlockSelectorBox().getBuiltInBlocksTree(language, root);
    items[1] = root.buildComponentsTree();
    items[2] = BlockSelectorBox.getBlockSelectorBox().getGenericComponentsTree(root);
    sourceStructureExplorer.updateTree(items, itemToSelect);
  }
}
