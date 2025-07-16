// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2009-2011 Google, All Rights reserved
// Copyright © 2011-2021 Massachusetts Institute of Technology, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.BLOCKLY_SOURCE_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.YAIL_FILE_EXTENSION;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.appinventor.client.editor.blocks.BlocksCategory;
import com.google.appinventor.client.editor.blocks.BlocksCodeGenerationException;
import com.google.appinventor.client.editor.blocks.BlocksCodeGenerationTarget;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.blocks.BlocksLanguage;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.youngandroid.events.EventHelper;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Editor for Young Android Blocks (.blk) files.
 *
 * @author lizlooney@google.com (Liz Looney)
 * @author sharon@google.com (Sharon Perl) added Blockly functionality
 * @author ewpatton@mit.edu (Evan W. Patton) refactored for additional blocks editor types
 */
public final class YaBlocksEditor extends BlocksEditor<YoungAndroidBlocksNode, YaFormEditor>
    implements ProjectChangeListener {
  private static final BlocksLanguage YAIL =
      new BlocksLanguage("Yail", YaVersion.BLOCKS_LANGUAGE_VERSION,
          new BlocksCategory("Control", IMAGES.control()),
          new BlocksCategory("Logic", IMAGES.logic()),
          new BlocksCategory("Math", IMAGES.math()),
          new BlocksCategory("Text", IMAGES.text()),
          new BlocksCategory("Lists", IMAGES.lists()),
          new BlocksCategory("Dictionaries", IMAGES.dictionaries()),
          new BlocksCategory("Colors", IMAGES.colors()),
          new BlocksCategory("Variables", IMAGES.variables()), new BlocksCategory("Procedures", IMAGES.procedures()));
  private static final Logger LOG = Logger.getLogger(YaBlocksEditor.class.getName());

  // The project associated with this blocks editor.
  private Project project;

  YaBlocksEditor(YaProjectEditor projectEditor, YoungAndroidBlocksNode blocksNode) {
    super(projectEditor, blocksNode, YaVersion.YOUNG_ANDROID_VERSION, YAIL,
        BlocksCodeGenerationTarget.YAIL,
        SimpleComponentDatabase.getInstance(blocksNode.getProjectId()));

    project = Ode.getInstance().getProjectManager().getProject(blocksNode.getProjectId());
    project.addProjectChangeListener(this);
    onProjectLoaded(project);
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
  public void onShow() {
    super.onShow();
    sendComponentData();  // Send Blockly the component information for generating Yail
  }

  @Override
  public void onClose() {
    super.onClose();
    project.removeProjectChangeListener(this);
  }

  @Override
  public String getEntityName() {
    return getForm().getName();
  }

  @Override
  public void onWorkspaceChange(BlocklyPanel panel, JavaScriptObject event) {
    super.onWorkspaceChange(panel, event);
    if (!EventHelper.isUi(event)) {
      sendComponentData();
    }
  }

  public synchronized void sendComponentData() {
    sendComponentData(false);
  }

  public synchronized void sendComponentData(boolean force) {
    try {
      blocksArea.setActiveFormWorkspace();
      blocksArea.sendComponentData(designer.encodeFormAsJsonString(true),
          packageNameFromPath(getFileId()), force);
    } catch (BlocksCodeGenerationException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void makeActiveWorkspace() {
    blocksArea.setActiveFormWorkspace();
    super.makeActiveWorkspace();
  }

  // Do whatever is needed to save Blockly state when our project is about to be
  // detached from the parent document. Note that this is not for saving the blocks file itself.
  // We use EditorManager.scheduleAutoSave for that.
  public void prepareForUnload() {
    blocksArea.saveComponentsAndBlocks();
//    blocksArea.saveBackpackContents();
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

  public FileDescriptorWithContent getYail() throws BlocksCodeGenerationException {
    return new FileDescriptorWithContent(getProjectId(), yailFileName(),
        blocksArea.getCode(designer.encodeFormAsJsonString(true),
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

  public MockForm getForm() {
    YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
    YaFormEditor myFormEditor = (YaFormEditor) yaProjectEditor.getFormFileEditor(blocksNode.getFormName());
    if (myFormEditor != null) {
      return myFormEditor.getForm();
    } else {
      return null;
    }
  }

  private String yailFileName() {
    String fileId = getFileId();
    return fileId.replace(BLOCKLY_SOURCE_EXTENSION, YAIL_FILE_EXTENSION);
  }

  /*
   * Start up the Repl (call into the Blockly.ReplMgr via the BlocklyPanel.
   */
  @Override
  public void startRepl(boolean alreadyRunning, boolean forChromebook, boolean forEmulator, boolean forUsb) {
    blocksArea.setActiveFormWorkspace();
    blocksArea.startRepl(alreadyRunning, forChromebook, forEmulator, forUsb);
  }

  /*
   * Perform a Hard Reset of the Emulator
   */
  public void hardReset() {
    blocksArea.hardReset();
  }

  /*
   * Trigger a Companion Update
   */
  @Override
  public void updateCompanion() {
    blocksArea.updateCompanion();
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
