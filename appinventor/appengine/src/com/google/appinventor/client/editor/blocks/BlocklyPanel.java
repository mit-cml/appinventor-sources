// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2009-2011 Google, All Rights reserved
// Copyright © 2011-2021 Massachusetts Institute of Technology, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslationTable;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.GenerateYailCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.settings.user.BlocksSettings;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.Promise.WrappedException;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Blocks editor panel.
 * The contents of the blocks editor panel is in an iframe identified by
 * the formName passed to the constructor. That identifier is also the hashtag
 * on the URL that is the source of the iframe. This class provides methods for
 * calling the Javascript Blockly code from the rest of the Designer.
 *
 * @author sharon@google.com (Sharon Perl)
 * @author ewpatton@mit.edu (Evan W. Patton) refactor for Blockly update
 */
public class BlocklyPanel extends HTMLPanel {
  private static final Logger LOG = Logger.getLogger(BlocklyPanel.class.getName());

  private static final NativeTranslationMap SIMPLE_COMPONENT_TRANSLATIONS;

  static {
    exportMethodsToJavascript();
    // Tell the blockly world about companion versions.
    setPreferredCompanion(
        MESSAGES.useCompanion(YaVersion.PREFERRED_COMPANION, YaVersion.PREFERRED_COMPANION + "u"),
        YaVersion.COMPANION_UPDATE_URL,
        YaVersion.COMPANION_UPDATE_URL1,
        YaVersion.COMPANION_UPDATE_EMULATOR_URL,
        YaVersion.EMULATOR_UPDATE_URL);
    for (int i = 0; i < YaVersion.ACCEPTABLE_COMPANIONS.length; i++) {
      addAcceptableCompanion(YaVersion.ACCEPTABLE_COMPANIONS[i]);
    }
    addAcceptableCompanionPackage(YaVersion.ACCEPTABLE_COMPANION_PACKAGE);
    SIMPLE_COMPONENT_TRANSLATIONS = NativeTranslationMap.transform(ComponentTranslationTable.myMap);
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
    void onWorkspaceChange(BlocklyPanel panel, JavaScriptObject event);
  }

  // The currently displayed entity (project/screen)
  private static String currentForm;

  // Warning indicator visibility status
  private static boolean isWarningVisible = false;

  // My entity name
  private final String formName;

  /**
   * Objects registered to listen for workspace changes.
   */
  private final Set<BlocklyWorkspaceChangeListener> listeners = new HashSet<>();

  /**
   * Reference to the native Blockly.WorkspaceSvg.
   */
  private WorkspaceSvg workspace;

  /**
   * If true, the loading of the blocks editor has not completed.
   */
  private boolean loadComplete = false;

  /**
   * If true, the loading of the blocks editor resulted in an error.
   */
  private boolean loadError = false;

  private final BlocksCodeGenerationTarget targetPlatform;

  public BlocklyPanel(String formName, BlocksCodeGenerationTarget targetPlatform) {
    this(formName, targetPlatform, false);
  }

  public BlocklyPanel(String formName, BlocksCodeGenerationTarget targetPlatform,
                      boolean readOnly) {
    super("");
    getElement().addClassName("svg");
    getElement().setId(formName);
    this.formName = formName;
    this.targetPlatform = targetPlatform;
    String projectId = formName.split("_")[0];

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
    initWorkspace(projectId, readOnly, LocaleInfo.getCurrentLocale().isRTL(), targetPlatform.getTarget());

    LOG.info("Created BlocklyPanel for " + formName);
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
      BlocksEditor<?, ?> editor = getBlocksEditor(formName);
      if (editor != null) {
        editor.setDamaged(true);
      }
      ErrorReporter.reportError(MESSAGES.blocksNotSaved(formName));
    } else {
      for (BlocklyWorkspaceChangeListener listener : listeners) {
        listener.onWorkspaceChange(this, event);
      }
    }
  }

  public static void switchWarningVisibility() {
    BlocklyPanel.isWarningVisible = !BlocklyPanel.isWarningVisible;
  }

  public static void callToggleWarning() {
    BlocksEditor.toggleWarning();
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
  public void loadBlocksContent(String formJson, String blocksContent, boolean upgrade)
      throws LoadBlocksException {
    try {
      doLoadBlocksContent(formJson, blocksContent, upgrade);
    } catch (JavaScriptException e) {
      loadError = true;
      ErrorReporter.reportError(MESSAGES.blocksLoadFailure(formName));
      LOG.log(Level.SEVERE, "Error loading blocks for screen " + formName, e);
      throw new LoadBlocksException(e, formName);
    } finally {
      loadComplete = true;
    }
  }

  /**
   * Get code for current blocks workspace
   *
   * @return the code as a String
   * @throws BlocksCodeGenerationException if there was a problem generating code for the target
   * platform
   */
  public String getCode(String formJson, String packageName) throws BlocksCodeGenerationException {
    try {
      return doGetYail(formJson, packageName);
    } catch (JavaScriptException e) {
      throw new BlocksCodeGenerationException(e.getDescription(), formName);
    }
  }

  /**
   * Send component data (json and form name) to Blockly for building code for the target REPL.
   *
   * @throws BlocksCodeGenerationException if there was a problem generating the code for the
   * target platform
   */
  public void sendComponentData(String formJson, String packageName)
      throws BlocksCodeGenerationException {
    sendComponentData(formJson, packageName, false);
  }

  public void sendComponentData(String formJson, String packageName, boolean force)
      throws BlocksCodeGenerationException {
    if (!currentForm.equals(formName)) { // Not working on the current form...
      LOG.info("Not working on " + currentForm + " (while sending for " + formName + ")");
      return;
    }
    try {
      doSendJson(formJson, packageName, force);
    } catch (JavaScriptException e) {
      throw new BlocksCodeGenerationException(e.getDescription(), formName);
    }
  }

  public WorkspaceSvg getWorkspace() {
    return workspace;
  }

  public native void setActiveFormWorkspace()/*-{
    $wnd.Blockly.activeFormWorkspace =
      this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
  }-*/;

  public void startRepl(boolean alreadyRunning, boolean forChromebook, boolean forEmulator, boolean forUsb) { // Start the Repl
    makeActive();
    doStartRepl(alreadyRunning, forChromebook, forEmulator, forUsb);
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

  /**
   * Returns true if the AI agent feature is available on this server.
   * Called from Blockly JS via the exported bridge function.
   */
  public static boolean isAIAgentAvailable() {
    return Ode.getSystemConfig().getAiAgentAvailable();
  }

  /**
   * Returns the current AI agent mode for the active project.
   * Returns "Off" if no project is open or no mode is configured.
   * Called from Blockly JS via the exported bridge function.
   */
  public static String getAIAgentMode() {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      return SettingsConstants.AI_AGENT_MODE_OFF;
    }
    com.google.appinventor.client.editor.ProjectEditor projectEditor =
        Ode.getInstance().getEditorManager().getOpenProjectEditor(projectId);
    if (projectEditor == null) {
      return SettingsConstants.AI_AGENT_MODE_OFF;
    }
    String mode = projectEditor.getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE);
    return (mode == null || mode.isEmpty()) ? SettingsConstants.AI_AGENT_MODE_OFF : mode;
  }

  /**
   * Opens the AI chat and sends an explain-block message.
   * Called from Blockly JS via the exported bridge function.
   */
  public static void explainBlock(String displayText, String contextHint) {
    Ode.getInstance().sendExplainToAIChat(displayText, contextHint);
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

  public static Promise<Boolean> startCache() {
    return new Promise<Boolean>((resolve, reject) -> {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();

      ChainableCommand cmd = new SaveAllEditorsCommand(
        new GenerateYailCommand(
          new ChainableCommand() {
            @Override
            public void execute(ProjectNode projectRootNode) {
              try {
                long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
                String projectName = Ode.getCurrentProject().getProjectName();
                boolean result = connectCache(Long.toString(projectId), projectName);
                resolve.apply(result);
              } catch (WrappedException e) {
                reject.apply(new WrappedException(e));
              }
            }
            @Override
            public boolean willCallExecuteNextCommand() {
              return false;
            }
          })
      );
      final ChainableCommand finalCmd = cmd;
      finalCmd.startExecuteChain(Tracking.PROJECT_ACTION_CACHE_PROJECT, projectRootNode, null);       
    });
    
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
   * @param callback   an opague JavaScriptObject that contains the
   *                   callback function provided by the Javascript code.
   * @return The created dialog box.
   */

  public static DialogBox createDialog(String title, String mess, final String buttonName, boolean destructive,
                                       final String cancelButtonName, int size, final JavaScriptObject callback) {
    // Holds a reference to an event handler to process key.
    // AtomicReference would be a better way to do this but GWT doesn't support it.
    final List<HandlerRegistration> registrationHolder = new ArrayList<>();
    final DialogBox dialogBox = new DialogBox() {
      @Override
      public void hide(boolean autoClosed) {
        super.hide(autoClosed);
        // Clean up the registration
        registrationHolder.get(0).removeHandler();
      }
    };
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
    HorizontalPanel holder = new HorizontalPanel();
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
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    terminateDrag();  // cancel a drag before showing the modal dialog
    dialogBox.show();
    // Note that this MUST be after dialogBox.show() so that it runs after the dialog's
    // event handlers, which cancel key events like Ctrl+C. We want people to be able to
    // copy text, so we 'consume' the event to let the browser perform its default behavior.
    registrationHolder.add(Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        if ((event.getTypeInt() & Event.KEYEVENTS) != 0) {
          event.consume();
        }
      }
    }));
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

  public static String getQRCode(String inString) {
    return doQRCode(inString);
  }

  private static BlocksEditor<?, ?> getBlocksEditor(String formId) {
    String[] parts = formId.split("_");
    long projectId = Long.parseLong(parts[0]);
    String formName = parts[1];
    return (BlocksEditor) Ode.getInstance().getEditorManager().getOpenProjectEditor(projectId)
        .getFileEditor(formName, BlocksEditor.EDITOR_TYPE);
  }

  public static String getComponentInfo(String typeName) {
    BlocksEditor<?, ?> blocksEditor = getBlocksEditor(currentForm);
    return blocksEditor == null ? "" : blocksEditor.getComponentInfo(typeName);
  }

  public static String getComponentsJSONString() {
    BlocksEditor<?, ?> blocksEditor = getBlocksEditor(currentForm);
    return blocksEditor == null ? "" : blocksEditor.getComponentsJSONString();
  }

  public static String getComponentInstanceTypeName(String instanceName) {
    BlocksEditor<?, ?> blocksEditor = getBlocksEditor(currentForm);
    return blocksEditor == null ? "" : blocksEditor.getComponentInstanceTypeName(instanceName);
  }

  public static String getComponentContainerUuid(String instanceName) {
    BlocksEditor<?, ?> blocksEditor = getBlocksEditor(currentForm);
    return blocksEditor == null ? "" : blocksEditor.getComponentContainerUuid(instanceName);
  }

  public static String getComponentInstancePropertyValue(String instanceName, String propertyName) {
    BlocksEditor<?, ?> blocksEditor = getBlocksEditor(currentForm);
    return blocksEditor == null ? "" : blocksEditor.getComponentInstancePropertyValue(instanceName, propertyName);
  }

  public static int getYaVersion() {
    return YaVersion.YOUNG_ANDROID_VERSION;
  }

  public static int getBlocksLanguageVersion() {
    return YaVersion.BLOCKS_LANGUAGE_VERSION;
  }

  public static String getProjectName() {
    String name = Ode.getInstance().getProjectManager().getProject(Long.parseLong(currentForm.split("_")[0])).getProjectName();
    return name;
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
            LOG.info("getSharedBackpack failed.");
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
            LOG.info("storeSharedBackpack failed.");
          }
        });
  }

  private static String getDefaultCloudDBServer() {
    return Ode.getSystemConfig().getDefaultCloudDBserver();
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

  @SuppressWarnings("LineLength")
  private static native void exportMethodsToJavascript() /*-{
    $wnd.BlocklyPanel_callToggleWarning =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::callToggleWarning());
    $wnd.BlocklyPanel_checkIsAdmin =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::checkIsAdmin());
    $wnd.BlocklyPanel_indicateDisconnect =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::indicateDisconnect());
    // Note: above lines are longer than 100 chars but I'm not sure whether they can be split
    $wnd.BlocklyPanel_pushScreen =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::pushScreen(Ljava/lang/String;));
    $wnd.BlocklyPanel_popScreen =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::popScreen());
    $wnd.BlocklyPanel_startCache =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::startCache());
    $wnd.BlocklyPanel_createDialog =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::createDialog(*));
    $wnd.BlocklyPanel_hideDialog =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::HideDialog(Lcom/google/gwt/user/client/ui/DialogBox;));
    $wnd.BlocklyPanel_setDialogContent =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::SetDialogContent(Lcom/google/gwt/user/client/ui/DialogBox;Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentInstanceTypeName =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getComponentInstanceTypeName(Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentInstancePropertyValue =
        $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getComponentInstancePropertyValue(Ljava/lang/String;Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentInfo =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getComponentInfo(Ljava/lang/String;));
    $wnd.BlocklyPanel_getComponentsJSONString =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getComponentsJSONString());
    $wnd.BlocklyPanel_storeBackpack =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::storeBackpack(Ljava/lang/String;));
    $wnd.BlocklyPanel_getOdeMessage =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getOdeMessage(Ljava/lang/String;));
    $wnd.BlocklyPanel_setGridEnabled =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::setGridEnabled(Z));
    $wnd.BlocklyPanel_setSnapEnabled =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::setSnapEnabled(Z));
    $wnd.BlocklyPanel_getGridEnabled =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getGridEnabled());
    $wnd.BlocklyPanel_getSnapEnabled =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getSnapEnabled());
    $wnd.BlocklyPanel_saveUserSettings =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::saveUserSettings());
    $wnd.BlocklyPanel_getSharedBackpack =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getSharedBackpack(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;));
    $wnd.BlocklyPanel_storeSharedBackpack =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::storeSharedBackpack(Ljava/lang/String;Ljava/lang/String;));
    $wnd.BlocklyPanel_getProjectName =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getProjectName());
    $wnd.BlocklyPanel_getDefaultCloudDBServer =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getDefaultCloudDBServer());
    $wnd.BlocklyPanel_getComponentContainerUuid =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getComponentContainerUuid(*));
    $wnd.BlocklyPanel_isAIAgentAvailable =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::isAIAgentAvailable());
    $wnd.BlocklyPanel_getAIAgentMode =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getAIAgentMode());
    $wnd.BlocklyPanel_explainBlock =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::explainBlock(Ljava/lang/String;Ljava/lang/String;));
  }-*/;

  private native void initWorkspace(String projectId, boolean readOnly, boolean rtl, String targetLang)/*-{
    var el = this.@com.google.gwt.user.client.ui.UIObject::getElement()();
    var workspace = $wnd.Blockly.BlocklyEditor.create(el,
      this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::formName,
      readOnly, rtl, targetLang);
    workspace.projectId = projectId;
    var cb = $entry(this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspaceChanged(Lcom/google/gwt/core/client/JavaScriptObject;));
    cb = cb.bind(this);
    workspace.addChangeListener(function(e) {
      var block = this.getBlockById(e.blockId);
      if ( block && e.name == $wnd.Blockly.ComponentBlock.COMPONENT_SELECTOR ) {
        block.rename(e.oldValue, e.newValue);
      }
      cb(e);
      if (workspace.rendered &&
          (!@com.google.appinventor.client.editor.youngandroid.events.EventHelper::isTransient(*)(e) ||
            e.type == 'finished_loading')) {
        var handler = this.getWarningHandler();
        if (handler) {
          // [lyn 12/31/2013] Check for duplicate component event handlers before
          // running any error handlers to avoid quadratic time behavior.
          handler.determineDuplicateComponentEventHandlers();
          this.requestErrorChecking(block);
        }
      }
    }.bind(workspace));
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace = workspace;
    workspace.setVisible(false);  // The workspace is invisible by default
  }-*/;

  /**
   * Inject the workspace into the &lt;div&gt; element with specific mode
   */
  native void injectWorkspace(boolean isDarkMode)/*-{
    var el = this.@com.google.gwt.user.client.ui.UIObject::getElement()();
    $wnd.AI.inject(el, this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace, isDarkMode);
  }-*/;

  /**
   * Make the workspace associated with the BlocklyPanel the main workspace.
   */
  native void makeActive()/*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    $wnd.Blockly.common.setMainWorkspace(workspace);
    workspace.refreshBackpack();
    if (workspace.pendingRender === true) {
      workspace.pendingRenderFunc();
    }
    // Trigger a screen switch to send new YAIL.
    var parts = workspace.formName.split(/_(.+)/);  // Split string on first _
    if ($wnd.Blockly.ReplMgr.isConnected()) {
      $wnd.Blockly.ReplMgr.pollYail(workspace);
    }
    workspace.fireChangeListener(new $wnd.AI.Events.ScreenSwitch(parts[0], parts[1]));
    var handler = workspace.getWarningHandler();
    if (handler) {
      handler.determineDuplicateComponentEventHandlers();
      workspace.requestErrorChecking();
    }
  }-*/;

  // [lyn, 2014/10/27] added formJson for upgrading
  public native void doLoadBlocksContent(String formJson, String blocksContent, boolean upgrade) /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var previousMainWorkspace = $wnd.Blockly.common.getMainWorkspace();
    try {
      $wnd.Blockly.common.setMainWorkspace(workspace);
      workspace.loadBlocksFile(formJson, blocksContent, upgrade).verifyAllBlocks();
    } catch(e) {
      workspace.loadError = true;
      throw e;
    } finally {
      workspace.loadComplete = true;
      $wnd.Blockly.common.setMainWorkspace(previousMainWorkspace);
    }
  }-*/;

  public native void upgradeWorkspace(String formJson, String blocksContent)/*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var previousMainWorkspace = $wnd.Blockly.mainWorkspace;
    try {
      $wnd.Blockly.mainWorkspace = workspace;
      workspace.upgrade(formJson, blocksContent).verifyAllBlocks();
    } finally {
      $wnd.Blockly.mainWorkspace = previousMainWorkspace;
    }
  }-*/;

  /**
   * Return the XML string describing the current state of the blocks workspace
   */
  public native String getBlocksContent() /*-{
    return this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .saveBlocksFile(@com.google.appinventor.common.version.AppInventorFeatures::doPrettifyXml()());
  }-*/;

  public native void addScreen(String name)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .addScreen(name);
  }-*/;

  public native void removeScreen(String name)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .removeScreen(name);
  }-*/;

  public native void addAsset(String name)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .addAsset(name);
  }-*/;

  public native void removeAsset(String name)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .removeAsset(name);
  }-*/;

  public static native boolean connectCache(String projectId, String projectName)/*-{
    return $wnd.Blockly.ReplMgr.connectCache(projectId, projectName);
  }-*/;

  /**
   * Add a component to the blocks workspace
   *
   * @param uid             the unique id of the component instance
   * @param instanceName    the name of the component instance
   * @param typeName        the type of the component instance
   */
  public native void addComponent(String uid, String instanceName, String typeName)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .addComponent(uid, instanceName, typeName);
  }-*/;

  /**
   * Remove the component instance instanceName, with the given typeName
   * and uid from the workspace.
   *
   * @param uid          unique id
   */
  public native void removeComponent(String uid)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
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
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .renameComponent(uid, oldName, newName);
  }-*/;

  /**
   * Show the drawer for component with the specified instance name
   *
   * @param name
   */
  public native void showComponentBlocks(String name)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .hideDrawer()
      .showComponent(name);
  }-*/;

  /**
   * Show the built-in blocks drawer with the specified name
   *
   * @param drawerName
   */
  public native void showBuiltinBlocks(String drawerName)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .hideDrawer()
      .showBuiltin(drawerName);
  }-*/;

  /**
   * Show the generic blocks drawer with the specified name
   *
   * @param drawerName
   */
  public native void showGenericBlocks(String drawerName)/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .hideDrawer()
      .showGeneric(drawerName);
  }-*/;

  /**
   * Hide the blocks drawer
   */
  public native void hideDrawer()/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .hideDrawer();
  }-*/;

  /**
   * @returns true if the blocks drawer is showing, false otherwise.
   */
  public native boolean drawerShowing()/*-{
    return this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .isDrawerShowing();
  }-*/;

  public native void render()/*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .resize()
      .render();
  }-*/;

  public native void hideChaff()/*-{
    $wnd.Blockly.hideChaff();
  }-*/;

  public native void resize()/*-{
    $wnd.Blockly.common.svgResize(
      this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace);
  }-*/;

  public native void toggleWarning()/*-{
    var handler =
      this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
        .getWarningHandler();
    if (handler) {  // handler won't exist if the workspace hasn't rendered yet.
      handler.toggleWarning();
    }
  }-*/;

  public native String doGetYail(String formJson, String packageName) /*-{
    return this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .getFormYail(formJson, packageName);
  }-*/;

  public void doSendJson(String formJson, String packageName) {
    doSendJson(formJson, packageName, false);
  };

  public native void doSendJson(String formJson, String packageName, boolean force) /*-{
    $wnd.Blockly.ReplMgr.sendFormData(formJson, packageName,
      this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace, force);
  }-*/;

  public native void doResetYail() /*-{
    $wnd.Blockly.ReplMgr.resetYail(true);
  }-*/;

  public native void doPollYail() /*-{
    try {
      $wnd.Blockly.ReplMgr.pollYail();
    } catch (e) {
      $wnd.console.log("doPollYail() Failed");
      $wnd.console.log(e);
    }
  }-*/;

  public native void doStartRepl(boolean alreadyRunning, boolean forChromebook, boolean forEmulator, boolean forUsb) /*-{
    $wnd.Blockly.ReplMgr.startRepl(alreadyRunning, forChromebook, forEmulator, forUsb);
  }-*/;

  public native void doHardReset() /*-{
    $wnd.Blockly.ReplMgr.ehardreset(
      this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::formName
    );
  }-*/;

  public native void doCheckWarnings() /*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .checkAllBlocksForWarningsAndErrors();
  }-*/;

  static native void setLanguageVersion(int yaVersion, int blocksVersion)/*-{
    $wnd.YA_VERSION = yaVersion;
    $wnd.BLOCKS_VERSION = blocksVersion;
  }-*/;

  public static native String getCompVersion() /*-{
    return $wnd.PREFERRED_COMPANION;
  }-*/;

  static native void setPreferredCompanion(String comp, String url, String url1, String url2, String url3) /*-{
    $wnd.PREFERRED_COMPANION = comp;
    $wnd.COMPANION_UPDATE_URL = url;
    $wnd.COMPANION_UPDATE_URL1 = url1;
    $wnd.COMPANION_UPDATE_EMULATOR_URL = url2;
    $wnd.EMULATOR_UPDATE_URL = url3;
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
    return $wnd.Blockly.ReplMgr.makeqrcode(inString);
  }-*/;

  public static native void doUpdateCompanion(String formName) /*-{
    $wnd.Blockly.ReplMgr.triggerUpdate();
  }-*/;

  /**
   * Update Component Types in Blockly ComponentTypes
   */
  public native void populateComponentTypes(String jsonComponentsStr) /*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .populateComponentTypes(jsonComponentsStr, @com.google.appinventor.client.editor.blocks.BlocklyPanel::SIMPLE_COMPONENT_TRANSLATIONS);
  }-*/;

  /**
   * Update Component Types in Blockly ComponentTypes
   */
  public native void doVerifyAllBlocks() /*-{
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .verifyAllBlocks();
  }-*/;

  /**
   * Append blocks to the existing workspace from an XML string.
   * Saves and restores the main workspace for multi-workspace safety.
   * Calls block.verify() on new blocks after injection.
   *
   * @param xmlString Blockly XML containing blocks to inject
   */
  public native void injectBlocksXml(String xmlString) /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var previousMainWorkspace = $wnd.Blockly.common.getMainWorkspace();
    try {
      $wnd.Blockly.common.setMainWorkspace(workspace);
      var parser = new DOMParser();
      var dom = parser.parseFromString(xmlString, 'text/xml');
      var xmlNode = dom.documentElement;
      var newBlockIds = $wnd.Blockly.Xml.domToWorkspace(xmlNode, workspace);
      // Verify new blocks
      for (var i = 0; i < newBlockIds.length; i++) {
        var block = workspace.getBlockById(newBlockIds[i]);
        if (block && block.verify) {
          block.verify();
        }
      }
    } finally {
      $wnd.Blockly.common.setMainWorkspace(previousMainWorkspace);
    }
  }-*/;

  /**
   * Delete a top-level block by type and identifying information.
   * For component_event: matches mutation instance_name + event_name.
   * For global_declaration: matches NAME field value.
   * For procedures_defnoreturn/defreturn: matches NAME field value.
   *
   * @param blockType the Blockly block type
   * @param instanceName the component instance name (for component_event), or null
   * @param identifier the event/variable/procedure name
   * @return true if a matching block was found and deleted
   */
  public native boolean deleteBlockByTypeAndId(String blockType, String instanceName,
      String identifier) /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var topBlocks = workspace.getTopBlocks(false);
    for (var i = 0; i < topBlocks.length; i++) {
      var block = topBlocks[i];
      if (block.type === blockType) {
        if (blockType === 'component_event') {
          var mutation = block.mutationToDom && block.mutationToDom();
          if (mutation
              && mutation.getAttribute('instance_name') === instanceName
              && mutation.getAttribute('event_name') === identifier) {
            block.dispose(true);
            return true;
          }
        } else if (blockType === 'global_declaration') {
          var nameField = block.getField('NAME');
          if (nameField && nameField.getValue() === identifier) {
            block.dispose(true);
            return true;
          }
        } else if (blockType === 'procedures_defnoreturn'
            || blockType === 'procedures_defreturn') {
          var nameField = block.getField('NAME');
          if (nameField && nameField.getValue() === identifier) {
            block.dispose(true);
            return true;
          }
        }
      }
    }
    return false;
  }-*/;

  /**
   * Replace a block by deleting the existing one (if any) and injecting new XML.
   * Combines deleteBlockByTypeAndId + injectBlocksXml for create-or-replace semantics.
   *
   * @param blockType the Blockly block type
   * @param instanceName the component instance name (for component_event), or null
   * @param identifier the event/variable/procedure name
   * @param newBlockXml Blockly XML for the replacement block
   */
  public void replaceBlock(String blockType, String instanceName, String identifier,
      String newBlockXml) {
    deleteBlockByTypeAndId(blockType, instanceName, identifier);
    injectBlocksXml(newBlockXml);
  }

  /**
   * Generate YAIL for just the blocks in the workspace (no form scaffolding).
   * Used by the AI agent to get a code representation of current blocks.
   *
   * @return YAIL string containing event handlers, global variables, and procedures
   */
  public native String getBlocksYail() /*-{
    return this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
      .getBlocksYail();
  }-*/;

  /**
   * Collect warnings and errors from the Blockly WarningHandler.
   * Walks up to the top-level block for each warning/error to provide
   * a meaningful identity string the LLM can correlate with YAIL blocks.
   *
   * @return JSON string with errors, warnings, errorCount, warningCount
   */
  public native String getBlocksWarningsAndErrors() /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var handler = workspace.getWarningHandler();
    var result = { errors: [], warnings: [], errorCount: 0, warningCount: 0 };
    if (!handler) return JSON.stringify(result);

    // Helper: walk up to the top-level block
    function getTopBlock(block) {
      while (block.getParent()) {
        block = block.getParent();
      }
      return block;
    }

    // Helper: get component name from a block — handles both instance
    // (COMPONENT_SELECTOR field) and generic (component_type mutation) variants
    function getComponentName(blk) {
      var selector = blk.getField('COMPONENT_SELECTOR');
      if (selector) return selector.getValue();
      // Generic blocks store the type in the mutation
      var mutation = blk.mutationToDom && blk.mutationToDom();
      if (mutation) {
        var ct = mutation.getAttribute('component_type');
        if (ct) return 'any ' + ct;
      }
      return '?';
    }

    // Helper: describe a top-level block for the LLM
    function describeBlock(block) {
      var top = getTopBlock(block);
      if (top.type === 'component_event') {
        var comp = getComponentName(top);
        var eventField = top.getField('EVENT_NAME');
        var evt = eventField ? eventField.getValue() : '?';
        return comp + '.' + evt + ' event handler';
      } else if (top.type === 'global_declaration') {
        var name = top.getField('NAME');
        return 'global variable ' + (name ? name.getValue() : '?');
      } else if (top.type === 'procedures_defnoreturn' || top.type === 'procedures_defreturn') {
        var name = top.getField('NAME');
        return 'procedure ' + (name ? name.getValue() : '?');
      } else if (top.type === 'component_method') {
        var comp = getComponentName(top);
        var method = top.getField('METHOD_NAME');
        return comp + '.' + (method ? method.getValue() : '?') + ' method call';
      } else if (top.type === 'component_set_get') {
        var comp = getComponentName(top);
        var prop = top.getField('PROP');
        var mutation = top.mutationToDom && top.mutationToDom();
        var setOrGet = mutation ? mutation.getAttribute('set_or_get') : 'get';
        return comp + '.' + (prop ? prop.getValue() : '?')
            + (setOrGet === 'set' ? ' property setter' : ' property getter');
      } else if (top.type === 'component_component_block') {
        var comp = getComponentName(top);
        return comp + ' component reference';
      } else if (top.type === 'component_all_component_block') {
        var typeSelector = top.getField('COMPONENT_TYPE_SELECTOR');
        return 'all ' + (typeSelector ? typeSelector.getValue() : '?') + ' components';
      }
      // Fallback: use the block type, replacing underscores for readability
      return top.type.replace(/_/g, ' ');
    }

    // Collect errors
    var errorHash = handler.errorIdHash;
    if (errorHash) {
      var errorIds = Object.keys(errorHash);
      for (var i = 0; i < errorIds.length; i++) {
        var block = workspace.getBlockById(errorIds[i]);
        if (block && block.error) {
          var text = block.error.getText ? block.error.getText() : '';
          if (text) {
            result.errors.push({ block: describeBlock(block), message: text });
          }
        }
      }
    }

    // Collect warnings
    var warningHash = handler.warningIdHash;
    if (warningHash) {
      var warningIds = Object.keys(warningHash);
      for (var i = 0; i < warningIds.length; i++) {
        var block = workspace.getBlockById(warningIds[i]);
        if (block && block.warning) {
          var text = block.warning.getText ? block.warning.getText() : '';
          if (text) {
            result.warnings.push({ block: describeBlock(block), message: text });
          }
        }
      }
    }

    result.errorCount = result.errors.length;
    result.warningCount = result.warnings.length;
    return JSON.stringify(result);
  }-*/;

  /**
   * Convert a YAIL string into Blockly blocks and add them to the workspace.
   * Uses the AI.YailToBlocks module to parse YAIL and create blocks with
   * upsert semantics (replaces existing block with same identity).
   *
   * @param yail the YAIL S-expression string
   * @return JSON string with {success: boolean, error: ?string, blockId: ?string}
   */
  public native String doWriteBlock(String yail) /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var result = $wnd.AI.YailToBlocks.convert(workspace, yail);
    return JSON.stringify(result);
  }-*/;

  /**
   * Delete a block identified by its YAIL head tokens.
   * For example: "define-event Button1 Click", "def g$score", "def p$factorial".
   *
   * @param identifier the block identifier string
   * @return JSON string with {success: boolean, error: ?string}
   */
  public native String doDeleteBlock(String identifier) /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var result = $wnd.AI.YailToBlocks.deleteBlock(workspace, identifier);
    return JSON.stringify(result);
  }-*/;

  /**
   * Notify the positioning algorithm that certain blocks will be deleted
   * later in the current execution batch, so new blocks should not try
   * to avoid them.
   *
   * @param identifiersJson JSON array of YAIL identifier strings
   */
  public native void doSetPendingDeletions(String identifiersJson) /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var identifiers = JSON.parse(identifiersJson);
    $wnd.AI.YailToBlocks.setPendingDeletions(workspace, identifiers);
  }-*/;

  /**
   * Clear the pending deletion set after the deletion phase completes.
   */
  public native void doClearPendingDeletions() /*-{
    $wnd.AI.YailToBlocks.clearPendingDeletions();
  }-*/;

  /**
   * Mark existing blocks that will be replaced by WRITE_BLOCK upserts,
   * adding them to the pending deletion set for positioning.
   *
   * @param yailJsonArray JSON array of YAIL S-expression strings
   */
  public native void doAddPendingUpserts(String yailJsonArray) /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    var yailStrings = JSON.parse(yailJsonArray);
    $wnd.AI.YailToBlocks.addPendingUpserts(workspace, yailStrings);
  }-*/;

  /**
   * Check whether a block with the given YAIL identifier exists.
   *
   * @param identifier YAIL identifier (e.g. "define-event Button1 Click")
   * @return true if the block exists on the workspace
   */
  public native boolean doBlockExists(String identifier) /*-{
    var workspace = this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace;
    return $wnd.AI.YailToBlocks.blockExists(workspace, identifier);
  }-*/;

  /**
   * Validate a YAIL string without creating any blocks (dry-run).
   * Parses the S-expression and checks structural validity.
   *
   * @param yail the YAIL S-expression string to validate
   * @return JSON string with {valid: boolean, error: ?string}
   */
  public native String doValidateYail(String yail) /*-{
    var result = $wnd.AI.YailToBlocks.validate(yail);
    return JSON.stringify(result);
  }-*/;

  /**
   * Validate a DELETE_BLOCK identifier string without touching the workspace.
   *
   * @param identifier the block identifier (e.g., "define-event Button1 Click")
   * @return JSON string with {valid: boolean, error: ?string}
   */
  public native String doValidateDeleteId(String identifier) /*-{
    var result = $wnd.AI.YailToBlocks.validateDeleteId(identifier);
    return JSON.stringify(result);
  }-*/;

  public native void doFetchBlocksImage(Callback<String,String> callback) /*-{
    var callb = $entry(function(result, error) {
      if (error) {
        callback.@com.google.gwt.core.client.Callback::onFailure(Ljava/lang/Object;)(error);
      } else {
        callback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)(result);
      }
    });
    this.@com.google.appinventor.client.editor.blocks.BlocklyPanel::workspace
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
    $wnd.AI.Blockly.Backpack.contents = JSON.parse(backpack);
  }-*/;

  public static native void setSharedBackpackId(String backPackId)/*-{
    $wnd.AI.Blockly.Backpack.backPackId = backPackId;
  }-*/;

  /**
   * Cancel an ongoing drag operation.
   */
  public static native void terminateDrag()/*-{
    if ($wnd.Blockly && $wnd.Blockly.common.getMainWorkspace()) {
      $wnd.Blockly.common.getMainWorkspace().cancelCurrentGesture();
    }
  }-*/;

  /**
   * Store the backpack's contents to the App Inventor service.
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
          LOG.severe("Failed setting the backpack");
        }
      });
  }

  public void setBlocklyVisible(boolean visible) {
    workspace.setVisible(visible);
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
