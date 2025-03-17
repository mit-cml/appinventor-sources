// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.actions.EnableAutoloadAction;
import com.google.appinventor.client.actions.SetFontDyslexicAction;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.DesignProject;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.Screen;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.logging.Logger;

/**
 * TopToolbar lives in the TopPanel, to create functionality in the designer.
 */
public class TopToolbar extends Composite {
  private static final String WIDGET_NAME_NEW = "New";
  private static final String WIDGET_NAME_DELETE = "Delete";
  private static final String WIDGET_NAME_UPLOAD_KEYSTORE = "UploadKeystore";
  private static final String WIDGET_NAME_DELETE_KEYSTORE = "DeleteKeystore";
  private static final String WIDGET_NAME_SAVE = "Save";
  private static final String WIDGET_NAME_SAVE_AS = "SaveAs";
  private static final String WIDGET_NAME_CHECKPOINT = "Checkpoint";
  private static final String WIDGET_NAME_MY_PROJECTS = "MyProjects";
  private static final String WIDGET_NAME_BUILD = "Build";
  private static final String WIDGET_NAME_BUILD_ANDROID_APK = "BuildApk";
  private static final String WIDGET_NAME_BUILD_ANDROID_AAB = "BuildAab";
  private static final String WIDGET_NAME_BUILD_ANDROID_APK2 = "BuildApk2";
  private static final String WIDGET_NAME_BUILD_ANDROID_AAB2 = "BuildAab2";
  private static final String WIDGET_NAME_BUILD_YAIL = "Yail";
  private static final String WIDGET_NAME_CONNECT_TO = "ConnectTo";
  private static final String WIDGET_NAME_WIRELESS_BUTTON = "Wireless";
  private static final String WIDGET_NAME_CHROMEBOOK = "Chromebook";
  private static final String WIDGET_NAME_EMULATOR_BUTTON = "Emulator";
  private static final String WIDGET_NAME_USB_BUTTON = "Usb";
  private static final String WIDGET_NAME_RESET_BUTTON = "Reset";
  private static final String WIDGET_NAME_HARDRESET_BUTTON = "HardReset";
  private static final String WIDGET_NAME_REFRESHCOMPANION_BUTTON = "RefreshCompanion";
  private static final String WIDGET_NAME_PROJECT = "Project";
  private static final String WIDGET_NAME_SETTINGS = "Settings";
  private static final String WIDGET_NAME_AUTOLOAD = "Autoload Last Project";
  private static final String WIDGET_NAME_DYSLEXIC_FONT = "DyslexicFont";
  private static final String WIDGET_NAME_NEW_LAYOUT = "NewLayout";
  private static final String WIDGET_NAME_OLD_LAYOUT = "OldLayout";
  private static final String WIDGET_NAME_DARK_THEME_ENABLED = "DarkThemeEnabled";
  private static final String WIDGET_NAME_HELP = "Help";
  private static final String WIDGET_NAME_ABOUT = "About";
  private static final String WIDGET_NAME_IMPORTPROJECT = "ImportProject";
  private static final String WIDGET_NAME_IMPORTTEMPLATE = "ImportTemplate";
  private static final String WIDGET_NAME_EXPORTPROJECT = "ExportProject";
  private static final String WIDGET_NAME_PROJECTPROPERTIES = "ProjectProperties";

  private static final String WIDGET_NAME_ADMIN = "Admin";
  private static final String WIDGET_NAME_USER_ADMIN = "UserAdmin";
  private static final String WIDGET_NAME_DOWNLOAD_USER_SOURCE = "DownloadUserSource";
  private static final String WIDGET_NAME_SWITCH_TO_DEBUG = "SwitchToDebugPane";
  private static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  private static final String WINDOW_OPEN_LOCATION = "_blank";

  private static final boolean iamChromebook = isChromeBook();

  private static final Logger LOG = Logger.getLogger(TopToolbar.class.getName());

  @UiField protected DropDownButton fileDropDown;
  @UiField protected DropDownButton connectDropDown;
  @UiField protected DropDownButton buildDropDown;
  @UiField protected DropDownButton settingsDropDown;
  @UiField protected DropDownButton adminDropDown;
  @UiField (provided = true) Boolean hasWriteAccess;

  protected boolean readOnly;

  /**
   * This flag is set to true when a check for the android.keystore file is in progress.
   */
  private volatile boolean isKeystoreCheckPending = false;

  /**
   * This flag is set to true when a call to {@link #updateKeystoreFileMenuButtons(boolean)} has
   * returned and the value was cached.
   */
  private volatile boolean isKeystoreCached = false;

  /**
   * This flag is the cached result of an earlier check for android.keystore.
   */
  private volatile boolean isKeystorePresent = false;

  interface TopToolbarUiBinder extends UiBinder<FlowPanel, TopToolbar> {}

  public TopToolbar() {
    // The boolean needs to be reversed here so it is true when items need to be visible.
    // UIBinder can't negate the boolean itself.
    readOnly = Ode.getInstance().isReadOnly();
    hasWriteAccess = !readOnly;

    bindUI();
    if (iamChromebook) {
      RootPanel.getBodyElement().addClassName("onChromebook");
    }

    // Second Buildserver Menu Items
    //
    // We may have a second buildserver which if present permits us to build applications
    // using different components. This was added primarily to support the "target 26 SDK"
    // effort where we needed a way for people to package applications against SDK 26 in
    // order for them to be available in Google's Play Store (Google Requirement as of
    // 8/1/2018). However such applications have a minSdk of 14 (Ice Cream Sandwich).
    //
    // To support the creation of packages for older devices, we leave the buildserver
    // (as of 8/1/2018) generating minSdk 7 packages (no target SDK) which will run on
    // much older devices. The second buildserver will package applications with a target
    // SDK of 26 for those MIT App Inventor users who wish to put their applications in
    // the Play Store after 8/1/2018.
    // template.
    if (!Ode.getInstance().hasSecondBuildserver()) {
      buildDropDown.removeItemById(WIDGET_NAME_BUILD_ANDROID_APK2);
      buildDropDown.removeItemById(WIDGET_NAME_BUILD_ANDROID_AAB2);
    }
    if (!AppInventorFeatures.hasYailGenerationOption()
        || !Ode.getInstance().getUser().getIsAdmin()) {
      buildDropDown.removeItemById(WIDGET_NAME_BUILD_YAIL);
    }
    buildDropDown.removeUnneededSeparators();

    if (!Ode.getUserAutoloadProject()) {
      settingsDropDown.setItemHtmlById("AutoloadLastProject", MESSAGES.enableAutoload());
      settingsDropDown.setCommandById("AutoloadLastProject", new EnableAutoloadAction());
    }
    if (!Ode.getUserDyslexicFont()) {
      settingsDropDown.setItemHtmlById("DyslexicFont", MESSAGES.enableOpenDyslexic());
      settingsDropDown.setCommandById("DyslexicFont", new SetFontDyslexicAction());
    }
    if (!Ode.getInstance().getUser().getIsAdmin()) {
      adminDropDown.removeFromParent();
    }
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  public void bindUI() {
    TopToolbarUiBinder uibinder = GWT.create(TopToolbarUiBinder.class);
    initWidget(uibinder.createAndBindUi(this));
  }

  public void updateMoveToTrash(boolean moveToTrash) {
    if (moveToTrash) {
      // Move projects from trash.
      fileDropDown.setItemVisible(MESSAGES.trashProjectMenuItem(), true);
      fileDropDown.setItemVisible(MESSAGES.deleteFromTrashButton(), false);
    } else {
      // Projects are alreayd in trash. Completely delete them.
      fileDropDown.setItemVisible(MESSAGES.trashProjectMenuItem(), false);
      fileDropDown.setItemVisible(MESSAGES.deleteFromTrashButton(), true);
    }
  }

  public void updateMenuState(int numSelectedProjects, int numProjects) {
    boolean allowDelete = hasWriteAccess && numSelectedProjects > 0;
    boolean allowExport = numSelectedProjects > 0;
    boolean allowExportAll = numProjects > 0;
    fileDropDown.setItemEnabled(MESSAGES.trashProjectMenuItem(), allowDelete);
    fileDropDown.setItemEnabled(MESSAGES.deleteFromTrashButton(), allowDelete);
    String exportProjectLabel = numSelectedProjects > 1
        ? MESSAGES.exportSelectedProjectsMenuItem(numSelectedProjects)
        : MESSAGES.exportProjectMenuItem();
    fileDropDown.setItemHtmlById(WIDGET_NAME_EXPORTPROJECT, exportProjectLabel);
    fileDropDown.setItemEnabledById(WIDGET_NAME_EXPORTPROJECT, allowExport);
    fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(), allowExportAll);
  }

  public void updateKeystoreStatus(boolean present) {
    isKeystoreCached = true;
    isKeystorePresent = present;
    isKeystoreCheckPending = false;
    fileDropDown.setItemEnabled(MESSAGES.deleteKeystoreMenuItem(), present);
    fileDropDown.setItemEnabled(MESSAGES.downloadKeystoreMenuItem(), present);
  }

  private void updateConnectToDropDownButton(boolean isEmulatorRunning, boolean isCompanionRunning,
      boolean isUsbRunning) {
    if (!isEmulatorRunning && !isCompanionRunning && !isUsbRunning) {
      connectDropDown.setItemEnabled(MESSAGES.AICompanionMenuItem(), true);
      if (iamChromebook) {
        connectDropDown.setItemEnabled(MESSAGES.chromebookMenuItem(), true);
      } else {
        connectDropDown.setItemEnabled(MESSAGES.emulatorMenuItem(), true);
        connectDropDown.setItemEnabled(MESSAGES.usbMenuItem(), true);
      }
      connectDropDown.setItemEnabled(MESSAGES.refreshCompanionMenuItem(), false);
    } else {
      connectDropDown.setItemEnabled(MESSAGES.AICompanionMenuItem(), false);
      if (iamChromebook) {
        connectDropDown.setItemEnabled(MESSAGES.chromebookMenuItem(), false);
      } else {
        connectDropDown.setItemEnabled(MESSAGES.emulatorMenuItem(), false);
        connectDropDown.setItemEnabled(MESSAGES.usbMenuItem(), false);
      }
      connectDropDown.setItemEnabled(MESSAGES.refreshCompanionMenuItem(), true);
    }
  }

  /**
   * Indicate that we are no longer connected to the Companion, adjust
   * buttons accordingly. Called from BlocklyPanel
   */
  public static void indicateDisconnect() {
    TopToolbar instance = Ode.getInstance().getTopToolbar();
    instance.updateConnectToDropDownButton(false, false, false);
  }

  /**
   * startRepl -- Start/Stop the connection to the companion.
   * If both forEmulator and forUsb are false, then we are connecting
   * via Wireless.
   *
   * @param start -- true to start the repl, false to stop it.
   * @param forChromebook -- true if we are connecting to a chromebook.
   * @param forEmulator -- true if we are connecting to the emulator.
   * @param forUsb -- true if this is a USB connection.
   */

  public void startRepl(boolean start, boolean forChromebook, boolean forEmulator, boolean forUsb) {
    DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    if (currentProject == null) {
      LOG.warning("DesignToolbar.currentProject is null. "
            + "Ignoring attempt to start the repl.");
      return;
    }
    Screen screen = currentProject.screens.get(currentProject.currentScreen);
    screen.blocksEditor.startRepl(!start, forChromebook, forEmulator, forUsb);
    if (start) {
      if (forEmulator) {        // We are starting the emulator...
        updateConnectToDropDownButton(true, false, false);
      } else if (forUsb) {      // We are starting the usb connection
        updateConnectToDropDownButton(false, false, true);
      } else {                  // We are connecting via wifi to a Companion
        updateConnectToDropDownButton(false, true, false);
      }
    } else {
      updateConnectToDropDownButton(false, false, false);
    }
  }

  public void replHardReset() {
    DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    if (currentProject == null) {
      LOG.warning("DesignToolbar.currentProject is null. "
            + "Ignoring attempt to do hard reset.");
      return;
    }
    Screen screen = currentProject.screens.get(currentProject.currentScreen);
    ((YaBlocksEditor)screen.blocksEditor).hardReset();
    updateConnectToDropDownButton(false, false, false);
  }

  public void replUpdate() {
    DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    if (currentProject == null) {
      LOG.warning("DesignToolbar.currentProject is null. "
              + "Ignoring attempt to refresh companion screen.");
      return;
    }
    Screen screen = currentProject.screens.get(currentProject.currentScreen);
    ((YaBlocksEditor)screen.blocksEditor).sendComponentData(true);
  }

  /**
   * Enables and/or disables buttons based on how many projects exist
   * (in the case of "Download All Projects") or are selected (in the case
   * of "Delete" and "Download Source").
   */
  public void updateFileMenuButtons(int view) {
    if (readOnly) {
      // This may be too simple
      return;
    }

    // TODO: This code will work only so long as these menu items stay located in the file/build
    // menus as expected. It should be refactored.
    int projectCount = ProjectListBox.getProjectListBox().getProjectList().getMyProjectsCount();
    if (view == 0) {  // We are in the Projects view
      if ("ProjectDesignOnly".equals(fileDropDown.getName())) {
        fileDropDown.setVisible(false);
      }
      fileDropDown.setItemEnabled(MESSAGES.deleteProjectButton(), false);
      fileDropDown.setItemVisible(MESSAGES.deleteFromTrashButton(), false);
      fileDropDown.setItemEnabled(MESSAGES.trashProjectMenuItem(), projectCount == 0);
      fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(), projectCount > 0);
      fileDropDown.setItemEnabledById(WIDGET_NAME_EXPORTPROJECT, false);
      fileDropDown.setItemEnabled(MESSAGES.saveMenuItem(), false);
      fileDropDown.setItemEnabled(MESSAGES.saveAsMenuItem(), false);
      fileDropDown.setItemEnabled(MESSAGES.checkpointMenuItem(), false);
      fileDropDown.setItemEnabled(MESSAGES.projectPropertiesMenuItem(), false);
      buildDropDown.setItemEnabled(MESSAGES.showExportAndroidApk(), false);
      buildDropDown.setItemEnabled(MESSAGES.showExportAndroidAab(), false);
      if (Ode.getInstance().hasSecondBuildserver()) {
        buildDropDown.setItemEnabled(MESSAGES.showExportAndroidApk2(), false);
        buildDropDown.setItemEnabled(MESSAGES.showExportAndroidAab2(), false);
      }
    } else { // We have to be in the Designer/Blocks view
      if ("ProjectDesignOnly".equals(fileDropDown.getName())) {
        fileDropDown.setVisible(true);
      }
      fileDropDown.setItemEnabled(MESSAGES.deleteProjectButton(), true);
      fileDropDown.setItemEnabled(MESSAGES.projectPropertiesMenuItem(), true);
      fileDropDown.setItemEnabled(MESSAGES.trashProjectMenuItem(), true);
      fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(), projectCount > 0);
      fileDropDown.setItemEnabledById(WIDGET_NAME_EXPORTPROJECT, true);
      fileDropDown.setItemEnabled(MESSAGES.saveMenuItem(), true);
      fileDropDown.setItemEnabled(MESSAGES.saveAsMenuItem(), true);
      fileDropDown.setItemEnabled(MESSAGES.checkpointMenuItem(), true);
      fileDropDown.setItemEnabled(MESSAGES.projectPropertiesMenuItem(), true);
      buildDropDown.setItemEnabled(MESSAGES.showExportAndroidApk(), true);
      buildDropDown.setItemEnabled(MESSAGES.showExportAndroidAab(), true);
      if (Ode.getInstance().hasSecondBuildserver()) {
        buildDropDown.setItemEnabled(MESSAGES.showExportAndroidApk2(), true);
        buildDropDown.setItemEnabled(MESSAGES.showExportAndroidAab2(), true);
      }
    }
    updateKeystoreFileMenuButtons(true);
  }

  /**
   * Enables or disables buttons based on whether the user has an android.keystore file.
   */
  public void updateKeystoreFileMenuButtons() {
    Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
        new AsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean keystoreFileExists) {
            isKeystoreCached = true;
            isKeystorePresent = keystoreFileExists;
            fileDropDown.setItemEnabled(MESSAGES.deleteKeystoreMenuItem(), keystoreFileExists);
            fileDropDown.setItemEnabled(MESSAGES.downloadKeystoreMenuItem(), keystoreFileExists);
          }

          @Override
          public void onFailure(Throwable caught) {
            // Enable the MenuItems. If they are clicked, we'll check again if the keystore exists.
            fileDropDown.setItemEnabled(MESSAGES.deleteKeystoreMenuItem(), true);
            fileDropDown.setItemEnabled(MESSAGES.downloadKeystoreMenuItem(), true);
          }
        });
  }

  /**
   * Enables or disables buttons based on whether the user has an android.keystore file. If the
   * useCache parameter is true, then the last value returned from the UserInfoService is used.
   * Otherwise, the behavior is identical to {@link #updateKeystoreFileMenuButtons()}.
   *
   * @param useCache true if a cached value of a previous call is acceptable.
   */
  public void updateKeystoreFileMenuButtons(boolean useCache) {
    if (useCache && isKeystoreCheckPending) {
      return;
    }
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean keystoreFileExists) {
        isKeystoreCached = true;
        isKeystorePresent = keystoreFileExists;
        isKeystoreCheckPending = false;
        fileDropDown.setItemEnabled(MESSAGES.deleteKeystoreMenuItem(), keystoreFileExists);
        fileDropDown.setItemEnabled(MESSAGES.downloadKeystoreMenuItem(), keystoreFileExists);
      }

      @Override
      public void onFailure(Throwable caught) {
        // Enable the MenuItems. If they are clicked, we'll check again if the keystore exists.
        isKeystoreCached = false;
        isKeystorePresent = true;
        isKeystoreCheckPending = false;
        fileDropDown.setItemEnabled(MESSAGES.deleteKeystoreMenuItem(), true);
        fileDropDown.setItemEnabled(MESSAGES.downloadKeystoreMenuItem(), true);
      }
    };
    if (useCache && isKeystoreCached) {
      callback.onSuccess(isKeystorePresent);
    } else {
      isKeystoreCheckPending = true;
      Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
          callback);
    }
  }

  public DropDownButton getSettingsDropDown() {
    return settingsDropDown;
  }

  private static native boolean isChromeBook() /*-{
    if (/\bCrOS\b/.test(navigator.userAgent)) {
      return true;
    } else {
      return false;
    }
  }-*/;
}
