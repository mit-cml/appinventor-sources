// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.TranslationComponentParams;
import com.google.appinventor.client.TranslationDesignerPallete;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.components.common.YaVersion;

import com.google.common.collect.Maps;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Blocks editor panel.
 * The contents of the blocks editor panel is in an iframe identified by
 * the formName passed to the constructor. That identifier is also the hashtag
 * on the URL that is the source of the iframe. This class provides methods for
 * calling the Javascript Blockly code from the rest of the Designer.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class BlocklyPanel extends HTMLPanel implements ComponentDatabaseChangeListener{
  public static enum OpType {ADD, REMOVE, RENAME}

  // The currently displayed form (project/screen)
  private static String currentForm;
  private static String languageSetting;

  private static class ComponentOp {
    public OpType op;
    public String instanceName;     // for ADD, REMOVE, RENAME
    public String uid;              // for ADD, REMOVE, RENAME
    public String typeDescription;  // for ADD
    public String typeName;         // for REMOVE, RENAME
    public String oldName;          // for RENAME
  }

  private static class LoadStatus {
    public boolean complete = false; // true if loading blocks completed
    public boolean error = false;     // true if got an error loading blocks
  }

  private final SimpleComponentDatabase COMPONENT_DATABASE;

  private static final String EDITOR_HTML =
    "<style>\n" +
    ".svg {\n" +
    "  height: 100%;\n" +
    "  width: 100%;\n" +
    "  border: solid black 1px;\n" +
    "}\n" +
    "</style>\n" +
    "<iframe src=\"blocklyframe.html#FORM_NAME\" class=\"svg\">";

  // Keep track of component additions/removals/renames that happen before
  // blocks editor is inited for the first time, or before reinitialization
  // after the blocks editor's project has been detached from the document.
  // Replay them in order after initialized. Keys are form names. If there is
  // an entry for a given form name in the map, its blocks have not yet been
  // (re)inited.
  // Note: Javascript is single-threaded. Since this code is compiled by GWT
  // into Javascript, we don't need to worry about concurrent access to
  // this map.
  private static Map<String, List<ComponentOp>> componentOps = Maps.newHashMap();

  // When a user switches projects, the ProjectEditor widget gets detached
  // from the main document in the browser. If the user switches back to a
  // previously open project (in the same session), when the ProjectEditor
  // widget gets reattached, all of its FileEditors in its deckPanel get
  // reloaded, causing the Blockly objects for the blocks editors
  // to be created anew. Since the FileEditor Java objects themselves are
  // not recreated, we need to reconstruct the set of components in the Blockly
  // object when the object gets recreated. For each form, we keep track of the
  // components currently in that form, stored as "add" operations that can be
  // replayed to restore those components when the underlying Blockly state
  // is re-inited. This component state is updated as components are added,
  // removed, and renamed. The outer map is keyed by form name, and the
  // inner map is keyed by component uid.
  private static final Map<String, Map<String, ComponentOp>> currentComponents = Maps.newHashMap();

  // Pending blocks file content, indexed by form name. Waiting to be loaded when the corresponding
  // blocks area is initialized.
  private static final Map<String, String> pendingBlocksContentMap = Maps.newHashMap();

  // [lyn, 2014/10/27] added formJson for upgrading
  // Pending form JSON content, indexed by form name. Waiting to be loaded when the corresponding
  // blocks area is initialized.
  private static final Map<String, String> pendingFormJsonMap = Maps.newHashMap();

  // Status of blocks loading, indexed by form name.
  private static final Map<String, LoadStatus> loadStatusMap = Maps.newHashMap();

  // Blockly backpack
  private static String backpack = "[]";

  // My form name
  private String formName;

  // My blocks editor
  private YaBlocksEditor myBlocksEditor;  // [lyn, 2014/10/28] Added to access current form json

  public static boolean isWarningVisible = false;

  public BlocklyPanel(YaBlocksEditor blocksEditor, String formName) {
    super(EDITOR_HTML.replace("FORM_NAME", formName));
    this.formName = formName;
    this.myBlocksEditor = blocksEditor;
    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance(blocksEditor.getProjectId());
    componentOps.put(formName, new ArrayList<ComponentOp>());
    // note: using Maps.newHashMap() gives a type error in Eclipse in the following line
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
    exportMethodsToJavascript();
    // Tell the blockly world about companion versions.
    setPreferredCompanion(YaVersion.PREFERRED_COMPANION, YaVersion.COMPANION_UPDATE_URL);
    for (int i = 0; i < YaVersion.ACCEPTABLE_COMPANIONS.length; i++) {
      addAcceptableCompanion(YaVersion.ACCEPTABLE_COMPANIONS[i]);
    }
    addAcceptableCompanionPackage(YaVersion.ACCEPTABLE_COMPANION_PACKAGE);
  }

  /*
   * Initialize the blocks area so that it can be updated as components are
   * added, removed, or changed. Replay any previous component operations that
   * we weren't able to run before the blocks editor was initialized. This
   * method is static so that it can be called by the native Javascript code
   * after it finishes loading. We export this method to Javascript in
   * exportInitComponentsMethod().
   */
  private static void initBlocksArea(String formName) {

    OdeLog.log("BlocklyPanel: Got initBlocksArea call for " + formName);

    // if there are any components added, add them first before we load
    // block content that might reference them

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
      for (ComponentOp op : componentOps.get(formName)) {
        switch (op.op) {
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

    // If we've gotten any block content to load, load it now
    // Note: Map.remove() returns the value (null if not present), as well as removing the mapping
    String pendingBlocksContent = pendingBlocksContentMap.remove(formName);
    // [lyn, 2014/10/27] added formJson for upgrading
    String pendingFormJson = pendingFormJsonMap.remove(formName);
    if (pendingBlocksContent != null) {
      OdeLog.log("Loading blocks area content for " + formName);
      loadBlocksContentNow(formName, pendingFormJson, pendingBlocksContent);
    }
  }

  private static void blocklyWorkspaceChanged(String formName) {
    LoadStatus loadStat = loadStatusMap.get(formName);
    // ignore workspaceChanged events until after the load finishes.
    if (loadStat == null || !loadStat.complete) {
      return;
    }
    if (loadStat.error) {
      YaBlocksEditor.setBlocksDamaged(formName);
      ErrorReporter.reportError(MESSAGES.blocksNotSaved(formName));
    } else {
      YaBlocksEditor.onBlocksAreaChanged(formName);
    }
  }

  // Returns true if the blocks for formName have been initialized (i.e.,
  // no componentOps entry exists for formName).
  public static boolean blocksInited(String formName) {
    return !componentOps.containsKey(formName);
  }

  public static String getBackpack() {
    return backpack;
  }
  public static void setBackpack(String bp_contents) {
    backpack = bp_contents;
  }

  /**
   * Add a component to the blocks workspace
   *
   * @param typeDescription JSON string describing the component type,
   *                        formatted as described in
   *                        {@link com.google.appinventor.components.scripts.ComponentDescriptorGenerator}
   * @param instanceName    the name of the component instance
   * @param uid             the unique id of the component instance
   */
  public void addComponent(String typeDescription, String instanceName, String uid) {
    if (!blocksInited(formName)) {
      ComponentOp cop = new ComponentOp();
      cop.op = OpType.ADD;
      cop.instanceName = instanceName;
      cop.typeDescription = typeDescription;
      cop.uid = uid;
      if (!componentOps.containsKey(formName)) {
        componentOps.put(formName, new ArrayList<ComponentOp>());
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
   *
   * @param typeName     component type name (e.g., "Canvas" or "Button")
   * @param instanceName instance name
   * @param uid          unique id
   */
  public void removeComponent(String typeName, String instanceName, String uid) {
    if (!blocksInited(formName)) {
      ComponentOp cop = new ComponentOp();
      cop.op = OpType.REMOVE;
      cop.instanceName = instanceName;
      cop.typeName = typeName;
      cop.uid = uid;
      if (!componentOps.containsKey(formName)) {
        componentOps.put(formName, new ArrayList<ComponentOp>());
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
   *
   * @param typeName component type name (e.g., "Canvas" or "Button")
   * @param oldName  old instance name
   * @param newName  new instance name
   * @param uid      unique id
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
        componentOps.put(formName, new ArrayList<ComponentOp>());
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
   *
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
   *
   * @param drawerName
   */
  public void showBuiltinBlocks(String drawerName) {
    try {
      if (blocksInited(formName)) {
        doShowBuiltinBlocks(formName, drawerName);
      }
    } catch (JavaScriptException e) {
      ErrorReporter.reportInfo("Not yet implemented: " + drawerName);
    }
  }

  /**
   * Hide the built-in blocks drawer
   */
  public void hideBuiltinBlocks() {
    if (blocksInited(formName)) {
      doHideBlocks(formName);
    }
  }

  /**
   * Show the generic blocks drawer with the specified name
   *
   * @param drawerName
   */
  public void showGenericBlocks(String drawerName) {
    if (blocksInited(formName)) {
      doShowGenericBlocks(formName, drawerName);
    }
  }

  /**
   * Hide the generic blocks drawer
   */
  public void hideGenericBlocks() {
    if (blocksInited(formName)) {
      doHideBlocks(formName);
    }
  }

  public void renderBlockly() {
    if (blocksInited(formName)) {
      doRenderBlockly(formName);
    }
  }

  public static void toggleWarning(String formName) {
    if (blocksInited(formName)) {
      doToggleWarning(formName);
    }
  }

  public static void switchWarningVisibility() {
    if (BlocklyPanel.isWarningVisible) {
      BlocklyPanel.isWarningVisible = false;
    } else {
      BlocklyPanel.isWarningVisible = true;
    }
  }

  public static void checkWarningState(String formName) {
    if (BlocklyPanel.isWarningVisible) {
      toggleWarning(formName);
    }
    doCheckWarnings(formName);
  }

  public static void callToggleWarning() {
    YaBlocksEditor.toggleWarning();
  }

  /**
   * Remember any component instances for this form in case
   * the workspace gets reinitialized later (we get detached from
   * our parent object and then our blocks editor gets loaded
   * again later). Also, remember the current state of the blocks
   * area in case we get reloaded.
   */
  public void saveComponentsAndBlocks() {
    // Actually, we already have the components saved, but take this as an
    // indication that we are going to reinit the blocks editor the next
    // time it is shown.
    OdeLog.log("BlocklyEditor: prepared for reinit for form " + formName);
    // Call doResetYail which will stop the timer that is polling the phone. It is important
    // that it be stopped to avoid a race condition where the last timer on this form fires
    // while the new form is loading.
    doResetYail(formName);
    // Get blocks content before putting anything in the componentOps map since an entry in
    // the componentOps map is taken as an indication that the blocks area has not initialized yet.
    pendingBlocksContentMap.put(formName, getBlocksContent());
    // [lyn, 2014/10/28] added formJson for upgrading
    pendingFormJsonMap.put(formName, getFormJson());
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

  /**
   * Load the blocks described by blocksContent into the blocks workspace.
   *
   * @param blocksContent XML description of a blocks workspace in format expected by Blockly
   */
  // [lyn, 2014/10/27] added formJson for upgrading
  public void loadBlocksContent(String formJson, String blocksContent) {
    LoadStatus loadStat = new LoadStatus();
    loadStatusMap.put(formName, loadStat);
    if (blocksInited(formName)) {
      OdeLog.log("Loading blocks content for " + formName);
      loadBlocksContentNow(formName, formJson, blocksContent);
    } else {
      // save it to load when the blocks area is initialized
      OdeLog.log("Caching blocks content for " + formName + " for loading when blocks area inited");
      pendingBlocksContentMap.put(formName, blocksContent);
      // [lyn, 2014/10/27] added formJson for upgrading
      pendingFormJsonMap.put(formName, formJson);
    }
  }

  // [lyn, 2014/10/27] added formJson for upgrading
  public static void loadBlocksContentNow(String formName, String formJson, String blocksContent) {
    LoadStatus loadStat = loadStatusMap.get(formName);  // should not be null!
    try {
      doLoadBlocksContent(formName, formJson, blocksContent);
    } catch (JavaScriptException e) {
      ErrorReporter.reportError(MESSAGES.blocksLoadFailure(formName));
      OdeLog.elog("Error loading blocks for screen " + formName + ": "
          + e.getDescription());
      loadStat.error = true;
    }
    loadStat.complete = true;
  }

  /**
   * Return the XML string describing the current state of the blocks workspace
   */
  public String getBlocksContent() {
    if (blocksInited(formName)) {
      return doGetBlocksContent(formName);
    } else {
      // in case someone clicks Save before the blocks area is inited
      String blocksContent = pendingBlocksContentMap.get(formName);
      return (blocksContent != null) ? blocksContent : "";
    }
  }

  /**
   * Return the JSON string describing the current state of the associated form
   */
  // [lyn, 2014/10/28] Handle these cases
  public String getFormJson() {
    if (blocksInited(formName)) {
      return myBlocksEditor.encodeFormAsJsonString(true);
    } else {
      // in case someone clicks Save before the blocks area is inited
      String formJson = pendingFormJsonMap.get(formName);
      return (formJson != null) ? formJson : "";
    }
  }



  /**
   * Get Yail code for current blocks workspace
   *
   * @return the yail code as a String
   * @throws YailGenerationException if there was a problem generating the Yail
   */
  public String getYail(String formJson, String packageName) throws YailGenerationException {
    if (!blocksInited(formName)) {
      throw new YailGenerationException("Blocks area is not initialized yet", formName);
    }
    try {
      return doGetYail(formName, formJson, packageName);
    } catch (JavaScriptException e) {
      throw new YailGenerationException(e.getDescription(), formName);
    }
  }

  /**
   * Send component data (json and form name) to Blockly for building
   * yail for the REPL.
   *
   * @throws YailGenerationException if there was a problem generating the Yail
   */
  public void sendComponentData(String formJson, String packageName) throws YailGenerationException {
    if (!currentForm.equals(formName)) { // Not working on the current form...
      OdeLog.log("Not working on " + currentForm + " (while sending for " + formName + ")");
      return;
    }
    if (!blocksInited(formName)) {
      throw new YailGenerationException("Blocks area is not initialized yet", formName);
    }
    try {
      doSendJson(formName, formJson, packageName);
    } catch (JavaScriptException e) {
      throw new YailGenerationException(e.getDescription(), formName);
    }
  }

  public void showDifferentForm(String newFormName) {
    OdeLog.log("showDifferentForm changing from " + formName + " to " + newFormName);
    // Nuke Yail for form we are leaving so it will reload when we return
    if (!formName.equals(newFormName))
      doResetYail(formName);
    formName = newFormName;
    blocklyWorkspaceChanged(formName);
  }

  public void startRepl(boolean alreadyRunning, boolean forEmulator, boolean forUsb) { // Start the Repl
    doStartRepl(formName, alreadyRunning, forEmulator, forUsb);
  }

  public void hardReset() {
    doHardReset(formName);
  }

  public void verifyAllBlocks() {
    doVerifyAllBlocks(formName);
  }

  public static boolean checkIsAdmin() {
    return Ode.getInstance().getUser().getIsAdmin();
  }

  // Set currentScreen
  // We use this to determine if we should send Yail to a
  // a connected device.
  public static void setCurrentForm(String formName) {
    currentForm = formName;
    if (blocksInited(formName))
      blocklyWorkspaceChanged(formName); // Update the device now if the blocks are ready.
  }

  public static void indicateDisconnect() {
    TopToolbar.indicateDisconnect();
    DesignToolbar.clearScreens();
  }

  public static boolean pushScreen(String newScreen) {
    return DesignToolbar.pushScreen(newScreen);
  }

  public static void popScreen() {
    DesignToolbar.popScreen();
  }

  public void getBlocksImage(Callback callback) {
    doFetchBlocksImage(formName, callback);
  }

  // The code below (4 methods worth) is for creating a GWT dialog box
  // from the blockly code. See the comment in replmgr.js for more
  // information.

  /**
   * Create a Dialog Box. We call this from Javascript (blockly) to
   * display a dialog box.  We do this here because we can get calls
   * from the blocklyframe when it is not visible.  Because we are in
   * the parent window, we can display dialogs that will be visible
   * even when the blocklyframe is not visible.
   *
   * @param title      Title for the Dialog Box
   * @param mess       The message to display
   * @param buttonName The string to display in the "OK" button.
   * @param size       0 or 1. 0 makes a smaller box 1 makes a larger box.
   * @param callback   an opague JavaScriptObject that contains the
   *                   callback function provided by the Javascript code.
   * @return The created dialog box.
   */

  public static DialogBox createDialog(String title, String mess, final String buttonName, final String cancelButtonName, int size, final JavaScriptObject callback) {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(title);
    if (size == 0) {
      dialogBox.setHeight("150px");
    } else {
      dialogBox.setHeight("400px");
    }
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(mess);
    message.setStyleName("DialogBox-message");
    HorizontalPanel holder = new HorizontalPanel();
    if (buttonName != null) {           // If buttonName and cancelButtonName are null
      Button ok = new Button(buttonName); // We won't have any buttons and other
      ok.addClickListener(new ClickListener() { // code is needed to dismiss us
          public void onClick(Widget sender) {
            doCallBack(callback, buttonName);
          }
        });
      holder.add(ok);
    }
    if (cancelButtonName != null) {
      Button cancel = new Button(cancelButtonName);
      cancel.addClickListener(new ClickListener() {
          public void onClick(Widget sender) {
            doCallBack(callback, cancelButtonName);
          }
        });
      holder.add(cancel);
    }
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
    return dialogBox;
  }

  /**
   * Hide a dialog box. This function is here so it can be called from
   * the blockly code. We cannot call "hide" directly from the blockly
   * code because when this code is compiled, the "hide" method disappears!
   *
   * @param dialog The dialogbox to hide.
   */

  public static void HideDialog(DialogBox dialog) {
    dialog.hide();
  }

  public static void SetDialogContent(DialogBox dialog, String mess) {
    HTML html = (HTML) ((VerticalPanel) dialog.getWidget()).getWidget(0);
    html.setHTML(mess);
  }

  public static String getComponentInfo(String typeName) {
    return YaBlocksEditor.getComponentInfo(typeName);
  }

  public static String getComponentsJSONString(String projectId) {
    return YaBlocksEditor.getComponentsJSONString(Long.parseLong(projectId));
  }

  public static String getComponentInstanceTypeName(String formName, String instanceName) {
    return YaBlocksEditor.getComponentInstanceTypeName(formName, instanceName);
  }

  public static int getYaVersion() {
    return YaVersion.YOUNG_ANDROID_VERSION;
  }

  public static int getBlocksLanguageVersion() {
    return YaVersion.BLOCKS_LANGUAGE_VERSION;
  }

  public static String getQRCode(String inString) {
    if (currentForm == null) {  // Cannot build a QR code without a current form
      return "";                // This only happens when you have no projects
    }
    return doQRCode(currentForm, inString);
  }

  /**
   * Update the language setting within BlocklyPanel.java and switch to the
   * desired language.
   *
   * @param newLanguage The desired new language setting
   */
  public void switchLanguage(String newLanguage) {
    languageSetting = newLanguage;
    doSwitchLanguage(formName, languageSetting);
  }

  /**
   * Update the language setting within BlocklyPanel.java and switch to the
   * desired language.
   *
   * @param newLanguage The desired new language setting
   * @param formName
   */
  public static void switchLanguage(String formName, String newLanguage) {
    languageSetting = newLanguage;
    doSwitchLanguage(formName, languageSetting);
  }

  /**
   * Trigger and Update of the Companion if the Companion is connected
   * and an update is available. Note: We do not compare the currently
   * running Companion's version against the version we are going to load
   * we just do it. If YaVersion.COMPANION_UPDATE_URL is "", then no
   * Update is available.
   */

  public void updateCompanion() {
    updateCompanion(formName);
  }

  public static void updateCompanion(String formName) {
    doUpdateCompanion(formName);
  }

  public static String getLocalizedPropertyName(String key) {
    return ComponentsTranslation.getPropertyName(key);
  }

  public static String getLocalizedParameterName(String key) {
    return TranslationComponentParams.getName(key);
  }

  public static String getLocalizedMethodName(String key) {
    return ComponentsTranslation.getMethodName(key);
  }

  public static String getLocalizedEventName(String key) {
    return ComponentsTranslation.getEventName(key);
  }

  public static String getLocalizedComponentType(String key) {
    return TranslationDesignerPallete.getCorrespondingString(key);
  }

  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {
    populateComponentTypes(formName);
    verifyAllBlocks();

  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    return true;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {
    populateComponentTypes(formName);
  }

  @Override
  public void onResetDatabase() {
    populateComponentTypes(formName);
  }
  // ------------ Native methods ------------

  /**
   * Take a Javascript function, embedded in an opaque JavaScriptObject,
   * and call it.
   *
   * @param callback the Javascript callback.
   */
  private static native void doCallBack(JavaScriptObject callback, String buttonName) /*-{
    callback.call(null, buttonName);
  }-*/;

  private static native void exportMethodsToJavascript() /*-{
    $wnd.BlocklyPanel_initBlocksArea =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::initBlocksArea(Ljava/lang/String;));
    $wnd.BlocklyPanel_blocklyWorkspaceChanged =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::blocklyWorkspaceChanged(Ljava/lang/String;));
    $wnd.BlocklyPanel_switchLanguage =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::switchLanguage(Ljava/lang/String;Ljava/lang/String;));
    $wnd.BlocklyPanel_checkWarningState =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::checkWarningState(Ljava/lang/String;));
    $wnd.BlocklyPanel_callToggleWarning =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::callToggleWarning());
    $wnd.BlocklyPanel_checkIsAdmin =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::checkIsAdmin());
    $wnd.BlocklyPanel_indicateDisconnect =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::indicateDisconnect());
    // Note: above lines are longer than 100 chars but I'm not sure whether they can be split
    $wnd.BlocklyPanel_pushScreen =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::pushScreen(Ljava/lang/String;));
    $wnd.BlocklyPanel_popScreen =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::popScreen());
    $wnd.BlocklyPanel_createDialog =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::createDialog(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILcom/google/gwt/core/client/JavaScriptObject;));
    $wnd.BlocklyPanel_hideDialog =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::HideDialog(Lcom/google/gwt/user/client/ui/DialogBox;));
    $wnd.BlocklyPanel_setDialogContent =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::SetDialogContent(Lcom/google/gwt/user/client/ui/DialogBox;Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentInstanceTypeName =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getComponentInstanceTypeName(Ljava/lang/String;Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentInfo =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getComponentInfo(Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentsJSONString =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getComponentsJSONString(Ljava/lang/String;));
    $wnd.BlocklyPanel_getYaVersion =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getYaVersion());
    $wnd.BlocklyPanel_getBlocksLanguageVersion =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getBlocksLanguageVersion());
    $wnd.BlocklyPanel_getLocalizedPropertyName =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getLocalizedPropertyName(Ljava/lang/String;));
    $wnd.BlocklyPanel_getLocalizedParameterName =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getLocalizedParameterName(Ljava/lang/String;));
    $wnd.BlocklyPanel_getLocalizedMethodName =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getLocalizedMethodName(Ljava/lang/String;));
    $wnd.BlocklyPanel_getLocalizedEventName =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getLocalizedEventName(Ljava/lang/String;));
    $wnd.BlocklyPanel_getLocalizedComponentType =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getLocalizedComponentType(Ljava/lang/String;));
    $wnd.BlocklyPanel_getBackpack =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getBackpack());
    $wnd.BlocklyPanel_setBackpack =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::setBackpack(Ljava/lang/String;));
  }-*/;

  private native void initJS() /*-{
    $wnd.myBlocklyPanel = this;
    $wnd.Blockly = null;  // will be set to our iframe's Blockly object when
                          // the iframe finishes loading
  }-*/;

  private static native void doAddComponent(String formName, String typeDescription,
                                            String instanceName, String uid) /*-{
    $wnd.Blocklies[formName].Component.add(instanceName, uid);
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

  public static native void doHideBlocks(String formName) /*-{
    $wnd.Blocklies[formName].Drawer.hide();
  }-*/;

  private static native void doShowGenericBlocks(String formName, String drawerName) /*-{
    var myBlockly = $wnd.Blocklies[formName];
    myBlockly.Drawer.hide();
    myBlockly.Drawer.showGeneric(drawerName);
  }-*/;

  public static native boolean doDrawerShowing(String formName) /*-{
    return $wnd.Blocklies[formName].Drawer.isShowing();
  }-*/;

  // [lyn, 2014/10/27] added formJson for upgrading
  public static native void doLoadBlocksContent(String formName, String formJson, String blocksContent) /*-{
    $wnd.Blocklies[formName].SaveFile.load(formJson, blocksContent);
    $wnd.Blocklies[formName].Component.verifyAllBlocks();
  }-*/;

  public static native String doGetBlocksContent(String formName) /*-{
    return $wnd.Blocklies[formName].SaveFile.get();
  }-*/;

  public static native String doGetYailRepl(String formName, String formJson, String packageName) /*-{
    return $wnd.Blocklies[formName].Yail.getFormYail(formJson, packageName, true);
  }-*/;

  public static native String doGetYail(String formName, String formJson, String packageName) /*-{
    return $wnd.Blocklies[formName].Yail.getFormYail(formJson, packageName);
  }-*/;

  public static native void doSendJson(String formName, String formJson, String packageName) /*-{
    $wnd.Blocklies[formName].ReplMgr.sendFormData(formJson, packageName);
  }-*/;

  public static native void doResetYail(String formName) /*-{
    $wnd.Blocklies[formName].ReplMgr.resetYail();
  }-*/;

  public static native void doPollYail(String formName) /*-{
    try {
      $wnd.Blocklies[formName].ReplMgr.pollYail();
    } catch (e) {
      $wnd.console.log("doPollYail() Failed");
      $wnd.console.log(e);
    }
  }-*/;

  public static native void doStartRepl(String formName, boolean alreadyRunning, boolean forEmulator, boolean forUsb) /*-{
    $wnd.Blocklies[formName].ReplMgr.startRepl(alreadyRunning, forEmulator, forUsb);
  }-*/;

  public static native void doHardReset(String formName) /*-{
    $wnd.Blocklies[formName].ReplMgr.ehardreset(formName);
  }-*/;

  public static native void doRenderBlockly(String formName) /*-{
    $wnd.Blocklies[formName].BlocklyEditor.render();
  }-*/;

  public static native void doToggleWarning(String formName) /*-{
    $wnd.Blocklies[formName].WarningHandler.warningToggle();
  }-*/;

  public static native void doCheckWarnings(String formName) /*-{
    $wnd.Blocklies[formName].WarningHandler.checkAllBlocksForWarningsAndErrors();
  }-*/;

  public static native String getCompVersion() /*-{
    return $wnd.PREFERRED_COMPANION;
  }-*/;

  static native void setPreferredCompanion(String comp, String url) /*-{
    $wnd.PREFERRED_COMPANION = comp;
    $wnd.COMPANION_UPDATE_URL = url;
  }-*/;

  static native void addAcceptableCompanionPackage(String comp) /*-{
    $wnd.ACCEPTABLE_COMPANION_PACKAGE = comp;
  }-*/;

  static native void addAcceptableCompanion(String comp) /*-{
    if ($wnd.ACCEPTABLE_COMPANIONS === null ||
        $wnd.ACCEPTABLE_COMPANIONS === undefined) {
      $wnd.ACCEPTABLE_COMPANIONS = [];
    }
    $wnd.ACCEPTABLE_COMPANIONS.push(comp);
  }-*/;

  static native String doQRCode(String formName, String inString) /*-{
    return $wnd.Blocklies[formName].ReplMgr.makeqrcode(inString);
  }-*/;

  /*
   * Switch the Blockly's language setting to "language" as specified in the
   * parameter argument.
   */
  public static native void doSwitchLanguage(String formName, String language) /*-{
    $wnd.Blocklies[formName].language_switch.switchLanguage(language);
  }-*/;

  public static native void doUpdateCompanion(String formName) /*-{
    $wnd.Blocklies[formName].ReplMgr.triggerUpdate();
  }-*/;

  public static native String getURL() /*-{
    return $wnd.location.href;
  }-*/;

  /*
   * Update Component Types in Blockly ComponentTypes
   */
  public static native void populateComponentTypes(String formName) /*-{
      $wnd.Blocklies[formName].ComponentTypes.populateTypes();
  }-*/;

  /*
   * Update Component Types in Blockly ComponentTypes
   */
  public static native void doVerifyAllBlocks(String formName) /*-{
      $wnd.Blocklies[formName].Component.verifyAllBlocks();
  }-*/;

  public static native void doFetchBlocksImage(String formName, Callback<String,String> callback) /*-{
      var callb = $entry(function(result, error) {
          if (error) {
             callback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;)(error);
          } else {
             callback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)(result);
          }
      });
      $wnd.Blocklies[formName].ExportBlocksImage.getUri(callb);
  }-*/;

}
