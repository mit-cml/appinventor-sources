// Copyright 2012 Massachusetts Institute of Technology. All Rights Reserved.

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.ui.HTMLPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Blocks editor panel. 
 * The contents of the blocks editor panel is in an iframe identified by
 * the formName passed to the constructor. This class provides methods for 
 * calling the Javascript Blockly code from the rest of the Designer.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class BlocklyPanel extends HTMLPanel {
  public static enum OpType {ADD, REMOVE, RENAME}
  
  private static class ComponentOp {
    public OpType op;
    public String instanceName;     // for ADD, REMOVE, RENAME
    public String uid;              // for ADD, REMOVE, RENAME
    public String typeDescription;  // for ADD
    public String typeName;         // for REMOVE, RENAME
    public String oldName;          // for RENAME
  }
  
  static final String EDITOR_HTML = 
      "<style>\n" +
      ".svg {\n" +
      "  height: 100%;\n" +
      "  width: 100%;\n" +
      "  border: solid black 1px;\n" +
      "}\n" +
      "</style>\n" +
      "<iframe src=\"blocklyframe.html#FORM_NAME\" class=\"svg\">";
    
  // My form name
  private final String formName;
  
  // Keep track of component additions/removals/renames that happen before
  // blocks editor is inited. Replay them in order after initialized. Keys
  // are form names. If there is an entry for a given form name in the map,
  // its blocks have not yet been inited.
  private static Map<String, List<ComponentOp>> componentOps = 
      new HashMap<String, List<ComponentOp>>();
  
  // For each form, keep track of the components currently in that form, stored
  // as "add" operations that can be replayed to restore those components when 
  // the underlying Blockly state is re-inited (when the document is replaced).
  // This component state is updated as components are added, removed, 
  // and renamed. The outer map is keyed by form name, and the inner map is 
  // keyed by component uid.
  private static Map<String, Map<String, ComponentOp>> currentComponents =
      new HashMap<String, Map<String, ComponentOp>>();

  public BlocklyPanel(String formName) {
    super(EDITOR_HTML.replace("FORM_NAME", formName));
    this.formName = formName;
    componentOps.put(formName, new ArrayList<ComponentOp>());
    currentComponents.put(formName, new HashMap<String, ComponentOp>());
    initJS();
    OdeLog.log("Created BlocklyPanel for " + formName);
  }
  
  /*
   * Do whatever is needed for App Inventor UI initialization. In this case
   * we just need to export the init components method so that we can
   * get called back from the Blockly Javascript when it finishes loading.
   */
  public static void initUi() {
    exportInitComponentsMethod();
  }
  
  /*
   * Initialize the blocks area so that it can be updated as components are
   * added, removed, or changed. Replay any previous component operations that
   * we weren't able to run before the blocks editor was initialized. This 
   * method is static so that it can be called by the native Javascript code
   * after it finishes loading. We export this method to Javascript in
   * exportInitComponentsMethod().
   */
  private static void initComponents(String formName) {
    // TODO(sharon): check whether we stashed away components from a previous 
    // initialization and restore if necessary. works in conjunction with 
    // saveComponents.
    OdeLog.log("BlocklyPanel: Got initComponents call for " + formName);
    Map<String, ComponentOp> savedComponents = currentComponents.get(formName);
    if (savedComponents != null) { // shouldn't be!
      OdeLog.log("Restoring " + savedComponents.size() + 
          " previous blockly components for form " + formName);
      for (ComponentOp op : savedComponents.values()) {
        doAddComponent(formName, op.typeDescription, op.instanceName, op.uid);
      }
    }

    if (componentOps.containsKey(formName)) {
      OdeLog.log("Replaying " + componentOps.get(formName).size() + " ops waiting in queue");
      for (ComponentOp op: componentOps.get(formName)) {
        switch (op.op){
          case ADD:
            doAddComponent(formName, op.typeDescription, op.instanceName, op.uid);
            addSavedComponent(formName, op.typeDescription, op.instanceName, op.uid);
            break;
          case REMOVE:
            doRemoveComponent(formName, op.typeName, op.instanceName, op.uid);
            removeSavedComponent(formName, op.typeName, op.instanceName, op.uid);
            break;
          case RENAME:
            doRenameComponent(formName, op.typeName, op.oldName, op.instanceName, op.uid);
            renameSavedComponent(formName, op.typeName, op.oldName, op.instanceName, op.uid);
            break;
        }
      }
      componentOps.remove(formName);
    }
  }
  
  // Returns true if the blocks for formName have been initialized (i.e.,
  // no componentOps entry exists for formName).
  private static boolean blocksInited(String formName) {
    return !componentOps.containsKey(formName);
  }
  
  /**
   * Add a component to the blocks workspace
   * @param typeDescription JSON string describing the component type,
   *   formatted as described in 
   *   {@link com.google.appinventor.components.scripts.ComponentDescriptorGenerator}
   * @param instanceName the name of the component instance
   * @param uid  the unique id of the component instance
   */
  public void addComponent(String typeDescription, String instanceName, String uid) {
    if (!blocksInited(formName)) {
      ComponentOp cop = new ComponentOp();
      cop.op = OpType.ADD;
      cop.instanceName = instanceName;
      cop.typeDescription = typeDescription;
      cop.uid = uid;
      if (!componentOps.containsKey(formName)) {
        componentOps.put(formName,  new ArrayList<ComponentOp>());
      }
      componentOps.get(formName).add(cop);
    } else {
      doAddComponent(formName, typeDescription, instanceName, uid);
      addSavedComponent(formName, typeDescription, instanceName, uid);
    }
  }
  
  private static void addSavedComponent(String formName, String typeDescription, 
      String instanceName, String uid) {
    Map<String, ComponentOp> myComponents = currentComponents.get(formName);
    if (!myComponents.containsKey(uid)) {
      // we expect there to be no saved component with this uid yet!
      ComponentOp savedComponent = new ComponentOp();
      savedComponent.op = OpType.ADD;
      savedComponent.instanceName = instanceName;
      savedComponent.typeDescription = typeDescription;
      savedComponent.uid = uid;
      myComponents.put(uid, savedComponent);
    } else {
      OdeLog.wlog("BlocklyPanel: already have component with uid " + uid
          + ", instanceName is " + myComponents.get(uid).instanceName);
    }

  }
  
  /**
   * Remove the component instance instanceName, with the given typeName
   * and uid from the workspace.
   * @param typeName component type name (e.g., "Canvas" or "Button")
   * @param instanceName  instance name
   * @param uid  unique id
   */
  public void removeComponent(String typeName, String instanceName, String uid) {
    if (!blocksInited(formName)) {
      ComponentOp cop = new ComponentOp();
      cop.op = OpType.REMOVE;
      cop.instanceName = instanceName;
      cop.typeName = typeName;
      cop.uid = uid;
      if (!componentOps.containsKey(formName)) {
        componentOps.put(formName,  new ArrayList<ComponentOp>());
      }
      componentOps.get(formName).add(cop);
    } else {
      doRemoveComponent(formName, typeName, instanceName, uid);
      removeSavedComponent(formName, typeName, instanceName, uid);
    }
  }
  
  private static void removeSavedComponent(String formName, String typeName, 
      String instanceName, String uid) {
    Map<String, ComponentOp> myComponents = currentComponents.get(formName);
    if (myComponents.containsKey(uid) 
        && myComponents.get(uid).instanceName.equals(instanceName)) {
      // we expect it to be there
      myComponents.remove(uid);
    } else {
      OdeLog.wlog("BlocklyPanel: can't find saved component with uid " + uid
          + " and name " + instanceName);
    }
  }
  
  /**
   * Rename the component whose old name is oldName (and whose
   * unique id is uid and type name is typeName) to newName.
   * @param typeName  component type name (e.g., "Canvas" or "Button")
   * @param oldName  old instance name
   * @param newName  new instance name
   * @param uid  unique id
   */
  public void renameComponent(String typeName, String oldName, 
      String newName, String uid) {
    if (!blocksInited(formName)) {
      ComponentOp cop = new ComponentOp();
      cop.op = OpType.RENAME;
      cop.instanceName = newName;
      cop.oldName = oldName;
      cop.typeName = typeName;
      cop.uid = uid;
      if (!componentOps.containsKey(formName)) {
        componentOps.put(formName,  new ArrayList<ComponentOp>());
      }
      componentOps.get(formName).add(cop);
    } else {
      doRenameComponent(formName, typeName, oldName, newName, uid);
      renameSavedComponent(formName, typeName, oldName, newName, uid);
    }
  }
  
  private static void renameSavedComponent(String formName, String typeName, 
      String oldName, String newName, String uid) {
    Map<String, ComponentOp> myComponents = currentComponents.get(formName);
    if (myComponents.containsKey(uid)) {
      // we expect it to be there
      ComponentOp savedComponent = myComponents.get(uid);
      if (savedComponent.instanceName.equals(oldName)) {  // it should!
        savedComponent.instanceName = newName;  // rename saved component
      } else {
        OdeLog.wlog("BlocklyPanel: saved component with uid " + uid + 
            " has name " + savedComponent.instanceName + ", expected " + oldName);
      }
    } else {
      OdeLog.wlog("BlocklyPanel: can't find saved component with uid " + uid + 
          " and name " + oldName);
    }
  }
  
  /**
   * Show the drawer for component with the specified instance name
   * @param name
   */
  public void showComponentBlocks(String name) {
    if (blocksInited(formName)) {
      doShowComponentBlocks(formName, name);
    }
  }
  
  /**
   * Hide the component blocks drawer
   */
  public void hideComponentBlocks() {
    if (blocksInited(formName)) {
      doHideComponentBlocks(formName);
    }
  }
  
  /**
   * Show the built-in blocks drawer with the specified name
   * @param drawerName
   */
  public void showBuiltinBlocks(String drawerName) {
    if (blocksInited(formName)) {
      doShowBuiltinBlocks(formName, drawerName);
    }
  }
  
  /**
   * Hide the built-in blocks drawer
   */
  public void hideBuiltinBlocks() {
    if (blocksInited(formName)) {
      doHideBuiltinBlocks(formName);
    }
  }

  /**
   * Remember any component instances for this form in case
   * the workspace gets reinitialized later (we get detached from
   * our parent object and then our blocks editor gets loaded
   * again later).
   */
  public void saveComponents() {
    // Actually, we already have the components saved, but take this as an 
    // indication that we are going to reinit the blocks editor the next
    // time it is shown.
    OdeLog.log("BlocklyEditor: prepared for reinit for form " + formName);
    componentOps.put(formName, new ArrayList<ComponentOp>());
  }
  
  /**
   * @returns true if the blocks drawer is showing, false otherwise.
   */
  public boolean drawerShowing() {
    if (blocksInited(formName)) {
      return doDrawerShowing(formName);
    } else {
      return false;
    }
  }
  
  // ------------ Native methods ------------
  
  private static native void exportInitComponentsMethod() /*-{ 
    $wnd.BlocklyPanel_initComponents = 
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::initComponents(Ljava/lang/String;));
  }-*/;
  
  private native void initJS() /*-{
    $wnd.myBlocklyPanel = this;
    $wnd.Blockly = null;  // will be set to our iframe's Blockly object when
                          // the iframe finishes loading
  }-*/;
  
  private static native void doAddComponent(String formName, String typeDescription, 
      String instanceName, String uid) /*-{
    $wnd.Blocklies[formName].Component.add(typeDescription, instanceName, uid);
  }-*/;
  
  private static native void doRemoveComponent(String formName, String typeName, 
      String instanceName, String uid) /*-{
    $wnd.Blocklies[formName].Component.remove(typeName, instanceName, uid);
  }-*/;
  
  private static native void doRenameComponent(String formName, String typeName, String oldName,
      String newName, String uid) /*-{
    $wnd.Blocklies[formName].Component.rename(oldName, newName, uid)
  }-*/;
  
  private static native void doShowComponentBlocks(String formName, String name) /*-{
    $wnd.Blocklies[formName].Drawer.showComponent(name);
  }-*/;

  public static native void doHideComponentBlocks(String formName) /*-{
    $wnd.Blocklies[formName].Drawer.hide();
  }-*/;

  private static native void doShowBuiltinBlocks(String formName, String drawerName) /*-{
    var myBlockly = $wnd.Blocklies[formName];
    myBlockly.Drawer.hide();
    myBlockly.Drawer.showBuiltin(drawerName);
  }-*/;

  public static native void doHideBuiltinBlocks(String formName) /*-{
    $wnd.Blocklies[formName].Drawer.hide();
  }-*/;
  
  public static native boolean doDrawerShowing(String formName) /*-{
    return $wnd.Blocklies[formName].Drawer.isShowing();
  }-*/;

}
