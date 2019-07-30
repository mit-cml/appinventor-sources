// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2009-2011 Google, All Rights reserved
// Copyright © 2011-2019 Massachusetts Institute of Technology, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.ConnectProgressBar;
import com.google.appinventor.client.DesignToolbar;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.appinventor.client.settings.user.BlocksSettings;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;

import com.google.gwt.query.client.builders.JsniBundle;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
public class BlocklyPanel extends HTMLPanel {

  public static interface BlocklySource extends JsniBundle {
    @LibrarySource(value="blockly.js",
                   prepend="(function(window, document, console){\nthis.goog = goog = top.goog;\n",
                   postpend="\n}.apply(window, [$wnd, $doc, $wnd.console]));\n" +
                   "for(var ns in window.goog.implicitNamespaces_) {\n" +
                   "  if(ns.indexOf('.') !== false) ns = ns.split('.')[0];\n" +
                   "  top[ns] = window.goog.global[ns];\n" +
                   "}\nwindow['Blockly'] = top['Blockly'];\nwindow['AI'] = top['AI'];")
    public void initBlockly();
  }

  private static final String EDITOR_HTML = "<div id=\"FORM_NAME\" class=\"svg\" tabindex=\"-1\"></div>";
  private static final NativeTranslationMap SIMPLE_COMPONENT_TRANSLATIONS;

  static {
    ((BlocklySource) GWT.create(BlocklySource.class)).initBlockly();
    exportMethodsToJavascript();
    // Tell the blockly world about companion versions.
    setLanguageVersion(YaVersion.YOUNG_ANDROID_VERSION, YaVersion.BLOCKS_LANGUAGE_VERSION);
    setPreferredCompanion(YaVersion.PREFERRED_COMPANION, YaVersion.COMPANION_UPDATE_URL,
      YaVersion.COMPANION_UPDATE_URL1,
      YaVersion.COMPANION_UPDATE_EMULATOR_URL);
    for (int i = 0; i < YaVersion.ACCEPTABLE_COMPANIONS.length; i++) {
      addAcceptableCompanion(YaVersion.ACCEPTABLE_COMPANIONS[i]);
    }
    addAcceptableCompanionPackage(YaVersion.ACCEPTABLE_COMPANION_PACKAGE);
    SIMPLE_COMPONENT_TRANSLATIONS = NativeTranslationMap.transform(ComponentsTranslation.myMap);
  }

  /**
   * BlocklyWorkspaceChangeListener allows other parts of the App Inventor system to subscribe to
   * events in the Blockly panel.
   *
   * @see com.google.appinventor.client.editor.youngandroid.events.EventHelper
   *
   * @author ewpatton
   *
   */
  public interface BlocklyWorkspaceChangeListener {
    /**
     * Event callback when a workspace change occurs.
     *
     * @param panel Source BlocklyPanel where the event occurred.
     * @param event Native object representing the event.
     */
    public void onWorkspaceChange(BlocklyPanel panel, JavaScriptObject event);
  }

  // The currently displayed form (project/screen)
  private static String currentForm;

  // Warning indicator visibility status
  private static boolean isWarningVisible = false;

  // My form name
  private final String formName;

  /**
   * Objects registered to listen for workspace changes.
   */
  private final Set<BlocklyWorkspaceChangeListener> listeners = Sets.newHashSet();

  /**
   * Reference to the native Blockly.WorkspaceSvg.
   */
  private JavaScriptObject workspace;

  /**
   * If true, the loading of the blocks editor has not completed.
   */
  private boolean loadComplete = false;

  /**
   * If true, the loading of the blocks editor resulted in an error.
   */
  private boolean loadError = false;

  public BlocklyPanel(YaBlocksEditor blocksEditor, String formName) {
    this(blocksEditor, formName, false);
  }

  public BlocklyPanel(YaBlocksEditor blocksEditor, String formName, boolean readOnly) {
    super("");
    getElement().addClassName("svg");
    getElement().setId(formName);
    this.formName = formName;
    /* Blockly initialization now occurs in three stages. This is due to the fact that certain
     * Blockly objects rely on SVG methods such as getScreenCTM(), which are not properly
     * initialized and/or null prior to the svg element being attached to the DOM. The first
     * stage of initialization happens here.
     *
     * Stages 2 and 3 can occur in different orders depending on network latency. On a fast
     * connection, the second stage will be loading of the .bky content into the workspace.
     * The third stage will then be rendering of the workspace when the user switches to the
     * Blocks editor. On slow connections, the workspace may render blank until the blocks file
     * has been downloaded from the server.
     */
    initWorkspace(Long.toString(blocksEditor.getProjectId()), readOnly, LocaleInfo.getCurrentLocale().isRTL());
    OdeLog.log("Created BlocklyPanel for " + formName);
  }

  /**
   * Register an object to listen for changes in the Blockly workspace.
   *
   * @param listener
   */
  public void addChangeListener(BlocklyWorkspaceChangeListener listener) {
    listeners.add(listener);
  }

  /**
   * Unregister an object from listening to changes in the Blockly workspace.
   *
   * @param listener
   */
  public void removeChangeListener(BlocklyWorkspaceChangeListener listener) {
    listeners.remove(listener);
  }

  /**
   * Notify listeners that an event has occurred in the Blockly workspace.
   *
   * @param event Native JavaScript event object with additional details.
   */
  private void workspaceChanged(JavaScriptObject event) {
    // ignore workspaceChanged events until after the load finishes
    if (!loadComplete) {
      return;
    }
    if (loadError) {
      YaBlocksEditor.setBlocksDamaged(formName);
      ErrorReporter.reportError(MESSAGES.blocksNotSaved(formName));
    } else {
      for (BlocklyWorkspaceChangeListener listener : listeners) {
        listener.onWorkspaceChange(this, event);
      }
    }
  }

  public static void switchWarningVisibility() {
    if (BlocklyPanel.isWarningVisible) {
      BlocklyPanel.isWarningVisible = false;
    } else {
      BlocklyPanel.isWarningVisible = true;
    }
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
   *
   * This method originally stashed a bunch of iframe related state
   * that is no longer necessary due to the removal of blocklyframe.html.
   * To maintain the correct logic with the ReplMgr, it remains for now.
   */
  public void saveComponentsAndBlocks() {
    // Call doResetYail which will stop the timer that is polling the phone. It is important
    // that it be stopped to avoid a race condition where the last timer on this form fires
    // while the new form is loading.
    doResetYail();
  }

  /**
   * Load the blocks described by blocksContent into the blocks workspace.
   *
   * @param formJson JSON description of Form's structure for upgrading
   * @param blocksContent XML description of a blocks workspace in format expected by Blockly
   * @throws LoadBlocksException if Blockly throws an uncaught exception
   */
  // [lyn, 2014/10/27] added formJson for upgrading
  public void loadBlocksContent(String formJson, String blocksContent) throws LoadBlocksException {
    try {
      doLoadBlocksContent(formJson, blocksContent);
    } catch (JavaScriptException e) {
      loadError = true;
      ErrorReporter.reportError(MESSAGES.blocksLoadFailure(formName));
      OdeLog.elog("Error loading blocks for screen " + formName + ": "
          + e.getDescription());
      throw new LoadBlocksException(e, formName);
    } finally {
      loadComplete = true;
    }
  }

  /**
   * Get Yail code for current blocks workspace
   *
   * @return the yail code as a String
   * @throws YailGenerationException if there was a problem generating the Yail
   */
  public String getYail(String formJson, String packageName) throws YailGenerationException {
    try {
      return doGetYail(formJson, packageName);
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
    try {
      doSendJson(formJson, packageName);
    } catch (JavaScriptException e) {
      throw new YailGenerationException(e.getDescription(), formName);
    }
  }

  public void startRepl(boolean alreadyRunning, boolean forEmulator, boolean forUsb) { // Start the Repl
    makeActive();
    doStartRepl(alreadyRunning, forEmulator, forUsb);
  }

  public void hardReset() {
    doHardReset();
  }

  public void verifyAllBlocks() {
    doVerifyAllBlocks();
  }

  public static boolean checkIsAdmin() {
    return Ode.getInstance().getUser().getIsAdmin();
  }

  // Set currentScreen
  // We use this to determine if we should send Yail to a
  // a connected device.
  public static void setCurrentForm(String formName) {
    currentForm = formName;
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

  public void getBlocksImage(Callback<String, String> callback) {
    doFetchBlocksImage(callback);
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
   * @param destructive Indicates if the button should be styled as a destructive action.
   * @param callback   an opague JavaScriptObject that contains the
   *                   callback function provided by the Javascript code.
   * @return The created dialog box.
   */

  public static DialogBox createDialog(String title, String mess, final String buttonName, Boolean destructive,
                                       final String cancelButtonName, int size, final JavaScriptObject callback) {
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
    if (cancelButtonName != null) {
      Button cancel = new Button(cancelButtonName);
      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          doCallBack(callback, cancelButtonName);
        }
      });
      holder.add(cancel);
    }
    if (buttonName != null) {           // If buttonName and cancelButtonName are null
      Button ok = new Button(buttonName); // We won't have any buttons and other
      if (destructive) {
        ok.addStyleName("destructive-action");
      }
      ok.addClickHandler(new ClickHandler() { // code is needed to dismiss us
        @Override
        public void onClick(ClickEvent event) {
          doCallBack(callback, buttonName);
        }
      });
      holder.add(ok);
    }
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    terminateDrag();  // cancel a drag before showing the modal dialog
    ConnectProgressBar.tempHide(true); // Hide any connection progress bar
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
    ConnectProgressBar.tempHide(false); // unhide the progress bar if it was hidden
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

  public static String getComponentInstancePropertyValue(String formName, String instanceName, String propertyName) {
    return YaBlocksEditor.getComponentInstancePropertyValue(formName, instanceName, propertyName);
  }

  public static int getYaVersion() {
    return YaVersion.YOUNG_ANDROID_VERSION;
  }

  public static int getBlocksLanguageVersion() {
    return YaVersion.BLOCKS_LANGUAGE_VERSION;
  }

  public static String getQRCode(String inString) {
    return doQRCode(inString);
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

  /**
   * Access UI translations for generating a deletion warning dialog.
   * @param message Identifier of message
   * @return Translated message
   * @throws IllegalArgumentException if the identifier is not understood
   */
  public static String getOdeMessage(String message) {
    // TODO(ewpatton): Investigate using a generator to work around
    // lack of reflection
    if ("deleteButton".equals(message)) {
      return Ode.getMessages().deleteButton();
    } else if ("cancelButton".equals(message)) {
      return Ode.getMessages().cancelButton();
    } else {
      throw new IllegalArgumentException("Unexpected argument in getOdeMessage: " + message);
    }
  }

  /**
   * Update the user's grid setting.
   * This method is called via JSNI.
   * @param enable true if the grid should be enabled, otherwise false.
   */
  private static void setGridEnabled(boolean enable) {
    BlocksSettings settings = (BlocksSettings) Ode.getUserSettings().getSettings(SettingsConstants.BLOCKS_SETTINGS);
    settings.changePropertyValue(SettingsConstants.GRID_ENABLED, Boolean.toString(enable));
  }

  /**
   * Update the user's snap-to-grid setting.
   * This method is called via JSNI.
   * @param enable true if snapping should be enabled, otherwise false.
   */
  private static void setSnapEnabled(boolean enable) {
    BlocksSettings settings = (BlocksSettings) Ode.getUserSettings().getSettings(SettingsConstants.BLOCKS_SETTINGS);
    settings.changePropertyValue(SettingsConstants.SNAP_ENABLED, Boolean.toString(enable));
  }

  /**
   * Get the current state of the user's grid setting.
   * This method is called via JSNI.
   * @return true if the setting is present and set to true, otherwise false.
   */
  private static boolean getGridEnabled() {
    BlocksSettings settings = (BlocksSettings) Ode.getUserSettings().getSettings(SettingsConstants.BLOCKS_SETTINGS);
    String snap = settings.getPropertyValue(SettingsConstants.GRID_ENABLED);
    return Boolean.parseBoolean(snap);
  }

  /**
   * Get the current state of the user's snap setting.
   * This method is called via JSNI.
   * @return true if the setting is present and set to true, otherwise false.
   */
  private static boolean getSnapEnabled() {
    BlocksSettings settings = (BlocksSettings) Ode.getUserSettings().getSettings(SettingsConstants.BLOCKS_SETTINGS);
    String snap = settings.getPropertyValue(SettingsConstants.SNAP_ENABLED);
    return Boolean.parseBoolean(snap);
  }

  /**
   * Trigger a save of the user's settings. This is used to prevent two updates being sent to the
   * server in the event a Blockly operation sets both grid and snap back-to-back.
   * This method is called via JSNI.
   */
  private static void saveUserSettings() {
    Ode.getUserSettings().saveSettings(null);
  }

  /**
   * Fetch a shared backpack from the server, call the callback with the
   * backpack content.
   *
   * @param backPackId the backpack id
   * @param callback callback to call with the backpack contents
   */

  public static void getSharedBackpack(String backPackId, final JavaScriptObject callback) {
    Ode.getInstance().getUserInfoService().getSharedBackpack(backPackId,
      new AsyncCallback<String>() {
        @Override
        public void onSuccess(String content) {
          doCallBack(callback, content);
        }
        @Override
        public void onFailure(Throwable caught) {
          OdeLog.log("getSharedBackpack failed.");
        }
      });
  }

  /**
   * Store shared backpack to the server.
   *
   * @param backPackId the backpack id
   * @param content the contents to store (XML String)
   */

  public static void storeSharedBackpack(String backPackId, String content) {
    Ode.getInstance().getUserInfoService().storeSharedBackpack(backPackId, content,
      new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void v) {
          // Nothing to do
        }
        @Override
        public void onFailure(Throwable caught) {
          OdeLog.log("storeSharedBackpack failed.");
        }
      });
  }

  // ------------ Native methods ------------

  /**
   * Take a Javascript function, embedded in an opaque JavaScriptObject,
   * and call it.
   *
   * @param callback the Javascript callback.
   * @param arg argument to the callback
   */

  private static native void doCallBack(JavaScriptObject callback, String arg) /*-{
    callback.call(null, arg);
  }-*/;

  private static native void exportMethodsToJavascript() /*-{
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
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::createDialog(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/String;ILcom/google/gwt/core/client/JavaScriptObject;));
    $wnd.BlocklyPanel_hideDialog =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::HideDialog(Lcom/google/gwt/user/client/ui/DialogBox;));
    $wnd.BlocklyPanel_setDialogContent =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::SetDialogContent(Lcom/google/gwt/user/client/ui/DialogBox;Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentInstanceTypeName =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getComponentInstanceTypeName(Ljava/lang/String;Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentInstancePropertyValue =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getComponentInstancePropertyValue(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentInfo =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getComponentInfo(Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentsJSONString =
        $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getComponentsJSONString(Ljava/lang/String;));
    $wnd.BlocklyPanel_storeBackpack =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::storeBackpack(Ljava/lang/String;));
    $wnd.BlocklyPanel_getOdeMessage =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getOdeMessage(Ljava/lang/String;));
    $wnd.BlocklyPanel_setGridEnabled =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::setGridEnabled(Z));
    $wnd.BlocklyPanel_setSnapEnabled =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::setSnapEnabled(Z));
    $wnd.BlocklyPanel_getGridEnabled =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getGridEnabled());
    $wnd.BlocklyPanel_getSnapEnabled =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getSnapEnabled());
    $wnd.BlocklyPanel_saveUserSettings =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::saveUserSettings());
    $wnd.BlocklyPanel_getSharedBackpack =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::getSharedBackpack(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;));
    $wnd.BlocklyPanel_storeSharedBackpack =
      $entry(@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::storeSharedBackpack(Ljava/lang/String;Ljava/lang/String;));

  }-*/;

  private native void initWorkspace(String projectId, boolean readOnly, boolean rtl)/*-{
    var el = this.@com.google.gwt.user.client.ui.UIObject::getElement()();
    var workspace = Blockly.BlocklyEditor.create(el,
      this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::formName,
      readOnly, rtl);
    workspace.projectId = projectId;
    var cb = $entry(this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspaceChanged(Lcom/google/gwt/core/client/JavaScriptObject;));
    cb = cb.bind(this);
    workspace.addChangeListener(function(e) {
      var block = this.getBlockById(e.blockId);
      if ( block && e.name == Blockly.ComponentBlock.COMPONENT_SELECTOR ) {
        block.rename(e.oldValue, e.newValue);
      }
      cb(e);
      if (workspace.rendered && !e.isTransient) {
        var handler = this.getWarningHandler();
        if (handler) {
          // [lyn 12/31/2013] Check for duplicate component event handlers before
          // running any error handlers to avoid quadratic time behavior.
          handler.determineDuplicateComponentEventHandlers();
          this.requestErrorChecking(block);
        }
      }
    }.bind(workspace));
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace = workspace;
  }-*/;

  /**
   * Inject the workspace into the &lt;div&gt; element.
   */
  native void injectWorkspace()/*-{
    var el = this.@com.google.gwt.user.client.ui.UIObject::getElement()();
    Blockly.ai_inject(el, this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace);
  }-*/;

  /**
   * Make the workspace associated with the BlocklyPanel the main workspace.
   */
  native void makeActive()/*-{
    Blockly.mainWorkspace = this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace;
    Blockly.mainWorkspace.refreshBackpack();
    // Trigger a screen switch to send new YAIL.
    var parts = Blockly.mainWorkspace.formName.split(/_/);
    if (Blockly.ReplMgr.isConnected()) {
      Blockly.ReplMgr.pollYail(Blockly.mainWorkspace);
    }
    Blockly.mainWorkspace.fireChangeListener(new AI.Events.ScreenSwitch(parts[0], parts[1]));
  }-*/;

  // [lyn, 2014/10/27] added formJson for upgrading
  public native void doLoadBlocksContent(String formJson, String blocksContent) /*-{
    var workspace = this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace;
    var previousMainWorkspace = Blockly.mainWorkspace;
    try {
      Blockly.mainWorkspace = workspace;
      workspace.loadBlocksFile(formJson, blocksContent).verifyAllBlocks();
    } catch(e) {
      workspace.loadError = true;
      throw e;
    } finally {
      workspace.loadComplete = true;
      Blockly.mainWorkspace = previousMainWorkspace;
    }
  }-*/;

  /**
   * Return the XML string describing the current state of the blocks workspace
   */
  public native String getBlocksContent() /*-{
    return this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .saveBlocksFile();
  }-*/;

  /**
   * Add a component to the blocks workspace
   *
   * @param uid             the unique id of the component instance
   * @param instanceName    the name of the component instance
   * @param typeName        the type of the component instance
   */
  public native void addComponent(String uid, String instanceName, String typeName)/*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .addComponent(uid, instanceName, typeName);
  }-*/;

  /**
   * Remove the component instance instanceName, with the given typeName
   * and uid from the workspace.
   *
   * @param uid          unique id
   */
  public native void removeComponent(String uid)/*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .removeComponent(uid);
  }-*/;

  /**
   * Rename the component whose old name is oldName (and whose
   * unique id is uid and type name is typeName) to newName.
   *
   * @param uid      unique id
   * @param oldName  old instance name
   * @param newName  new instance name
   */
  public native void renameComponent(String uid, String oldName, String newName)/*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .renameComponent(uid, oldName, newName);
  }-*/;

  /**
   * Show the drawer for component with the specified instance name
   *
   * @param name
   */
  public native void showComponentBlocks(String name)/*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .hideDrawer()
      .showComponent(name);
  }-*/;

  /**
   * Show the built-in blocks drawer with the specified name
   *
   * @param drawerName
   */
  public native void showBuiltinBlocks(String drawerName)/*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .hideDrawer()
      .showBuiltin(drawerName);
  }-*/;

  /**
   * Show the generic blocks drawer with the specified name
   *
   * @param drawerName
   */
  public native void showGenericBlocks(String drawerName)/*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .hideDrawer()
      .showGeneric(drawerName);
  }-*/;

  /**
   * Hide the blocks drawer
   */
  public native void hideDrawer()/*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .hideDrawer();
  }-*/;

  /**
   * @returns true if the blocks drawer is showing, false otherwise.
   */
  public native boolean drawerShowing()/*-{
    return this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .isDrawerShowing();
  }-*/;

  public native void render()/*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .resize()
      .render();
  }-*/;

  public native void hideChaff()/*-{
    Blockly.hideChaff();
  }-*/;

  public native void toggleWarning()/*-{
    var handler =
      this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
        .getWarningHandler();
    if (handler) {  // handler won't exist if the workspace hasn't rendered yet.
      handler.toggleWarning();
    }
  }-*/;

  public native String doGetYail(String formJson, String packageName) /*-{
    return this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .getFormYail(formJson, packageName);
  }-*/;

  public native void doSendJson(String formJson, String packageName) /*-{
    Blockly.ReplMgr.sendFormData(formJson, packageName,
      this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace);
  }-*/;

  public native void doResetYail() /*-{
    Blockly.ReplMgr.resetYail(true);
  }-*/;

  public native void doPollYail() /*-{
    try {
      Blockly.ReplMgr.pollYail();
    } catch (e) {
      $wnd.console.log("doPollYail() Failed");
      $wnd.console.log(e);
    }
  }-*/;

  public native void doStartRepl(boolean alreadyRunning, boolean forEmulator, boolean forUsb) /*-{
    Blockly.ReplMgr.startRepl(alreadyRunning, forEmulator, forUsb);
  }-*/;

  public native void doHardReset() /*-{
    Blockly.ReplMgr.ehardreset(
      this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::formName
    );
  }-*/;

  public native void doCheckWarnings() /*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .checkAllBlocksForWarningsAndErrors();
  }-*/;

  static native void setLanguageVersion(int yaVersion, int blocksVersion)/*-{
    $wnd.YA_VERSION = yaVersion;
    $wnd.BLOCKS_VERSION = blocksVersion;
  }-*/;

  public static native String getCompVersion() /*-{
    return $wnd.PREFERRED_COMPANION;
  }-*/;

  static native void setPreferredCompanion(String comp, String url, String url1, String url2) /*-{
    $wnd.PREFERRED_COMPANION = comp;
    $wnd.COMPANION_UPDATE_URL = url;
    $wnd.COMPANION_UPDATE_URL1 = url1;
    $wnd.COMPANION_UPDATE_EMULATOR_URL = url2;
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

  static native String doQRCode(String inString) /*-{
    return Blockly.ReplMgr.makeqrcode(inString);
  }-*/;

  public static native void doUpdateCompanion(String formName) /*-{
    Blockly.ReplMgr.triggerUpdate();
  }-*/;

  /**
   * Update Component Types in Blockly ComponentTypes
   */
  public native void populateComponentTypes(String jsonComponentsStr) /*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .populateComponentTypes(jsonComponentsStr, @com.google.appinventor.client.editor.youngandroid.BlocklyPanel::SIMPLE_COMPONENT_TRANSLATIONS);
  }-*/;

  /**
   * Update Component Types in Blockly ComponentTypes
   */
  public native void doVerifyAllBlocks() /*-{
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .verifyAllBlocks();
  }-*/;

  public native void doFetchBlocksImage(Callback<String,String> callback) /*-{
    var callb = $entry(function(result, error) {
      if (error) {
        callback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;)(error);
      } else {
        callback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)(result);
      }
    });
    this.@com.google.appinventor.client.editor.youngandroid.BlocklyPanel::workspace
      .exportBlocksImageToUri(callb);
  }-*/;

  /**
   * Set the initial backpack contents from the server.
   *
   * This is an optimization that reduces the number of serializations to and from JSON. The
   * backpack in earlier versions needed to be marshalled between iframes, but now lives in
   * the same environment so can remain as JavaScript content.
   *
   * @param backpack JSON-serialized backpack contents.
   */
  public static native void setInitialBackpack(String backpack)/*-{
    Blockly.Backpack.contents = JSON.parse(backpack);
  }-*/;

  public static native void setSharedBackpackId(String backPackId)/*-{
    Blockly.Backpack.backPackId = backPackId;
  }-*/;

  /**
   * Cancel an ongoing drag operation.
   */
  public static native void terminateDrag()/*-{
    if (Blockly) Blockly.terminateDrag_();
  }-*/;

  /**
   * Store the backpack's contents to the App Inventor server.
   *
   * @param backpack JSON-serialized backpack contents.
   */
  private static void storeBackpack(String backpack) {
    Ode.getInstance().getUserInfoService().storeUserBackpack(backpack,
      new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void nothing) {
          // Nothing to do...
        }
        @Override
        public void onFailure(Throwable caught) {
          OdeLog.elog("Failed setting the backpack");
        }
      });
  }

  /**
   * NativeTranslationMap is a plain JavaScriptObject that provides key-value mappings for
   * user interface translations in Blockly. This reduces the overhead of crossing GWT's
   * JSNI barrier by replacing a more expensive function call with a dictionary lookup.
   *
   * @author ewpatton
   *
   */
  private static class NativeTranslationMap extends JavaScriptObject {
    // GWT requires JSO constructors to be non-visible.
    protected NativeTranslationMap() {}

    /**
     * Instantiate a new NativeTranslationMap.
     * @return An empty NativeTranslationMap
     */
    private static native NativeTranslationMap make()/*-{
      return {};
    }-*/;

    /**
     * Add a key-value pair to the translation map.
     * @param key Untranslated term
     * @param value Translated term for the user's current locale
     */
    private native void put(String key, String value)/*-{
      this[key] = value;
    }-*/;

    /**
     * Transforms a Java Collections Map into a NativeTranslationMap.
     * @param map The source mapping of key-value pairs
     * @return A new NativeTranslationMap with the same contents as <i>map</i> but as a
     * JavaScript Object usable in native code.
     */
    public static NativeTranslationMap transform(Map<String, String> map) {
      NativeTranslationMap result = make();
      for(Entry<String, String> entry : map.entrySet()) {
        result.put(entry.getKey(), entry.getValue());
      }
      return result;
    }
  }

}
