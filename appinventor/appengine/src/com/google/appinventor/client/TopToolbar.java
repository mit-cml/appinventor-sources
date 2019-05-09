// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.explorer.commands.BuildCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.CopyYoungAndroidProjectCommand;
import com.google.appinventor.client.explorer.commands.DownloadProjectOutputCommand;
import com.google.appinventor.client.explorer.commands.GenerateYailCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.commands.ShowBarcodeCommand;
import com.google.appinventor.client.explorer.commands.ShowProgressBarCommand;
import com.google.appinventor.client.explorer.commands.WaitForBuildResultCommand;
import com.google.appinventor.client.explorer.commands.WarningDialogCommand;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.appinventor.client.wizards.DownloadUserSourceWizard;
import com.google.appinventor.client.wizards.KeystoreUploadWizard;
import com.google.appinventor.client.wizards.ProjectUploadWizard;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.appinventor.client.wizards.ComponentImportWizard;
import com.google.appinventor.client.wizards.ComponentUploadWizard;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.GallerySettings;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;


/**
 * TopToolbar lives in the TopPanel, to create functionality in the designer.
 */
public class TopToolbar extends Composite {
  private static final String WIDGET_NAME_NEW = "New";
  private static final String WIDGET_NAME_DELETE = "Delete";
  private static final String WIDGET_NAME_DOWNLOAD_KEYSTORE = "DownloadKeystore";
  private static final String WIDGET_NAME_UPLOAD_KEYSTORE = "UploadKeystore";
  private static final String WIDGET_NAME_DELETE_KEYSTORE = "DeleteKeystore";
  private static final String WIDGET_NAME_SAVE = "Save";
  private static final String WIDGET_NAME_SAVE_AS = "SaveAs";
  private static final String WIDGET_NAME_CHECKPOINT = "Checkpoint";
  private static final String WIDGET_NAME_MY_PROJECTS = "MyProjects";
  private static final String WIDGET_NAME_BUILD = "Build";
  private static final String WIDGET_NAME_BUILD_BARCODE = "Barcode";
  private static final String WIDGET_NAME_BUILD_DOWNLOAD = "Download";
  private static final String WIDGET_NAME_BUILD_BARCODE2 = "Barcode2";
  private static final String WIDGET_NAME_BUILD_DOWNLOAD2 = "Download2";
  private static final String WIDGET_NAME_BUILD_YAIL = "Yail";
  private static final String WIDGET_NAME_CONNECT_TO = "ConnectTo";
  private static final String WIDGET_NAME_WIRELESS_BUTTON = "Wireless";
  private static final String WIDGET_NAME_EMULATOR_BUTTON = "Emulator";
  private static final String WIDGET_NAME_USB_BUTTON = "Usb";
  private static final String WIDGET_NAME_RESET_BUTTON = "Reset";
  private static final String WIDGET_NAME_HARDRESET_BUTTON = "HardReset";
  private static final String WIDGET_NAME_PROJECT = "Project";
  private static final String WIDGET_NAME_HELP = "Help";
  private static final String WIDGET_NAME_ABOUT = "About";
  private static final String WIDGET_NAME_LIBRARY = "Library";
  private static final String WIDGET_NAME_GETSTARTED = "GetStarted";
  private static final String WIDGET_NAME_TUTORIALS = "Tutorials";
  private static final String WIDGET_NAME_EXTENSIONS = "Extensions";
  private static final String WIDGET_NAME_SHOWSPLASH = "ShowSplash";
  private static final String WIDGET_NAME_TROUBLESHOOTING = "Troubleshooting";
  private static final String WIDGET_NAME_FORUMS = "Forums";
  private static final String WIDGET_NAME_FEEDBACK = "ReportIssue";
  private static final String WIDGET_NAME_COMPANIONINFO = "CompanionInformation";
  private static final String WIDGET_NAME_COMPANIONUPDATE = "CompanionUpdate";
  private static final String WIDGET_NAME_IMPORTPROJECT = "ImportProject";
  private static final String WIDGET_NAME_IMPORTTEMPLATE = "ImportTemplate";
  private static final String WIDGET_NAME_EXPORTALLPROJECTS = "ExportAllProjects";
  private static final String WIDGET_NAME_EXPORTPROJECT = "ExportProject";
  private static final String WIDGET_NAME_COMPONENTS = "Components";
  private static final String WIDGET_NAME_MY_COMPONENTS = "MyComponents";
  private static final String WIDGET_NAME_START_NEW_COMPONENT = "StartNewComponent";
  private static final String WIDGET_NAME_IMPORT_COMPONENT = "ImportComponent";
  private static final String WIDGET_NAME_BUILD_COMPONENT = "BuildComponent";
  private static final String WIDGET_NAME_UPLOAD_COMPONENT = "UploadComponent";

  private static final String WIDGET_NAME_ADMIN = "Admin";
  private static final String WIDGET_NAME_USER_ADMIN = "UserAdmin";
  private static final String WIDGET_NAME_DOWNLOAD_USER_SOURCE = "DownloadUserSource";
  private static final String WIDGET_NAME_SWITCH_TO_DEBUG = "SwitchToDebugPane";
  private static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  private static final String WINDOW_OPEN_LOCATION = "_ai2";

  public DropDownButton fileDropDown;
  public DropDownButton connectDropDown;
  public DropDownButton buildDropDown;
  public DropDownButton helpDropDown;
  public DropDownButton adminDropDown;

  private boolean isReadOnly;
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

  public TopToolbar() {
    /*
     * Layout is as follows:
     * +--------------------------------------------------+
     * | Project ▾ | Connect ▾ | Build ▾| Help ▾| Admin ▾ |
     * +--------------------------------------------------+
     */
    HorizontalPanel toolbar = new HorizontalPanel();
    toolbar.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    List<DropDownItem> fileItems = Lists.newArrayList();
    List<DropDownItem> componentItems = Lists.newArrayList();
    List<DropDownItem> connectItems = Lists.newArrayList();
    List<DropDownItem> buildItems = Lists.newArrayList();
    List<DropDownItem> helpItems = Lists.newArrayList();

    // Should the UI be in read only mode?
    isReadOnly = Ode.getInstance().isReadOnly();

    // File -> {New Project; Save; Save As; Checkpoint; |; Delete this Project; My Projects;}
    fileItems.add(new DropDownItem(WIDGET_NAME_MY_PROJECTS, MESSAGES.projectMenuItem(),
        new SwitchToProjectAction()));
    fileItems.add(null);
    if (!isReadOnly) {
      fileItems.add(new DropDownItem(WIDGET_NAME_NEW, MESSAGES.newProjectMenuItem(),
          new NewAction()));
      fileItems.add(new DropDownItem(WIDGET_NAME_IMPORTPROJECT, MESSAGES.importProjectMenuItem(),
          new ImportProjectAction()));
      fileItems.add(new DropDownItem(WIDGET_NAME_IMPORTTEMPLATE, MESSAGES.importTemplateButton(),
        new ImportTemplateAction()));
      fileItems.add(new DropDownItem(WIDGET_NAME_DELETE, MESSAGES.deleteProjectButton(),
          new DeleteAction()));
      fileItems.add(null);
      fileItems.add(new DropDownItem(WIDGET_NAME_SAVE, MESSAGES.saveMenuItem(),
          new SaveAction()));
      fileItems.add(new DropDownItem(WIDGET_NAME_SAVE_AS, MESSAGES.saveAsMenuItem(),
          new SaveAsAction()));
      fileItems.add(new DropDownItem(WIDGET_NAME_CHECKPOINT, MESSAGES.checkpointMenuItem(),
          new CheckpointAction()));
      fileItems.add(null);
    }
    fileItems.add(new DropDownItem(WIDGET_NAME_EXPORTPROJECT, MESSAGES.exportProjectMenuItem(),
        new ExportProjectAction()));
    fileItems.add(new DropDownItem(WIDGET_NAME_EXPORTALLPROJECTS, MESSAGES.exportAllProjectsMenuItem(),
        new ExportAllProjectsAction()));
    fileItems.add(null);
    if (!isReadOnly) {
      fileItems.add(new DropDownItem(WIDGET_NAME_UPLOAD_KEYSTORE, MESSAGES.uploadKeystoreMenuItem(),
          new UploadKeystoreAction()));
    }
    fileItems.add(new DropDownItem(WIDGET_NAME_DOWNLOAD_KEYSTORE, MESSAGES.downloadKeystoreMenuItem(),
        new DownloadKeystoreAction()));
    if (!isReadOnly) {
      fileItems.add(new DropDownItem(WIDGET_NAME_DELETE_KEYSTORE, MESSAGES.deleteKeystoreMenuItem(),
          new DeleteKeystoreAction()));
    }

    // Connect -> {Connect to Companion; Connect to Emulator; Connect to USB; Reset Connections}
    connectItems.add(new DropDownItem(WIDGET_NAME_WIRELESS_BUTTON,
        MESSAGES.AICompanionMenuItem(), new WirelessAction()));
    connectItems.add(new DropDownItem(WIDGET_NAME_EMULATOR_BUTTON,
        MESSAGES.emulatorMenuItem(), new EmulatorAction()));
    connectItems.add(new DropDownItem(WIDGET_NAME_USB_BUTTON, MESSAGES.usbMenuItem(),
        new UsbAction()));
    connectItems.add(null);
    connectItems.add(new DropDownItem(WIDGET_NAME_RESET_BUTTON, MESSAGES.resetConnectionsMenuItem(),
        new ResetAction()));
    connectItems.add(new DropDownItem(WIDGET_NAME_HARDRESET_BUTTON, MESSAGES.hardResetConnectionsMenuItem(),
        new HardResetAction()));

    // Build -> {Show Barcode; Download to Computer; Generate YAIL only when logged in as an admin}
    buildItems.add(new DropDownItem(WIDGET_NAME_BUILD_BARCODE, MESSAGES.showBarcodeMenuItem(),
        new BarcodeAction(false)));
    buildItems.add(new DropDownItem(WIDGET_NAME_BUILD_DOWNLOAD, MESSAGES.downloadToComputerMenuItem(),
        new DownloadAction(false)));

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

    if (Ode.getInstance().hasSecondBuildserver()) {
      buildItems.add(null);
      buildItems.add(new DropDownItem(WIDGET_NAME_BUILD_BARCODE2, MESSAGES.showBarcodeMenuItem2(),
          new BarcodeAction(true)));
      buildItems.add(new DropDownItem(WIDGET_NAME_BUILD_DOWNLOAD2, MESSAGES.downloadToComputerMenuItem2(),
          new DownloadAction(true)));
    }

    if (AppInventorFeatures.hasYailGenerationOption() && Ode.getInstance().getUser().getIsAdmin()) {
      buildItems.add(null);
      buildItems.add(new DropDownItem(WIDGET_NAME_BUILD_YAIL, MESSAGES.generateYailMenuItem(),
          new GenerateYailAction()));
    }

    // Help -> {About, Library, Get Started, Tutorials, Troubleshooting, Forums, Report an Issue,
    //  Companion Information, Show Splash Screen}
    helpItems.add(new DropDownItem(WIDGET_NAME_ABOUT, MESSAGES.aboutMenuItem(),
        new AboutAction()));
    helpItems.add(null);
    Config config = Ode.getInstance().getSystemConfig();
    String libraryUrl = config.getLibraryUrl();
    if (!Strings.isNullOrEmpty(libraryUrl)) {
      helpItems.add(new DropDownItem(WIDGET_NAME_LIBRARY, MESSAGES.libraryMenuItem(),
          new WindowOpenAction(libraryUrl)));
    }
    String getStartedUrl = config.getGetStartedUrl();
    if (!Strings.isNullOrEmpty(getStartedUrl)) {
      helpItems.add(new DropDownItem(WIDGET_NAME_GETSTARTED, MESSAGES.getStartedMenuItem(),
          new WindowOpenAction(getStartedUrl)));
    }
    String extensionsUrl = config.getExtensionsUrl();
    if (!Strings.isNullOrEmpty(extensionsUrl)) {
      helpItems.add(new DropDownItem(WIDGET_NAME_EXTENSIONS, MESSAGES.extensionsMenuItem(),
          new WindowOpenAction(extensionsUrl)));
    }
    String tutorialsUrl = config.getTutorialsUrl();
    if (!Strings.isNullOrEmpty(tutorialsUrl)) {
      helpItems.add(new DropDownItem(WIDGET_NAME_TUTORIALS, MESSAGES.tutorialsMenuItem(),
          new WindowOpenAction(tutorialsUrl)));
    }
    String troubleshootingUrl = config.getTroubleshootingUrl();
    if (!Strings.isNullOrEmpty(troubleshootingUrl)) {
      helpItems.add(new DropDownItem(WIDGET_NAME_TROUBLESHOOTING, MESSAGES.troubleshootingMenuItem(),
          new WindowOpenAction(troubleshootingUrl)));
    }
    String forumsUrl = config.getForumsUrl();
    if (!Strings.isNullOrEmpty(forumsUrl)) {
      helpItems.add(new DropDownItem(WIDGET_NAME_FORUMS, MESSAGES.forumsMenuItem(),
          new WindowOpenAction(forumsUrl)));
    }
    helpItems.add(null);
    String feedbackUrl = config.getFeedbackUrl();
    if (!Strings.isNullOrEmpty(feedbackUrl)) {
      helpItems.add(new DropDownItem(WIDGET_NAME_FEEDBACK, MESSAGES.feedbackMenuItem(),
          new WindowOpenAction(feedbackUrl)));
      helpItems.add(null);
    }
    helpItems.add(new DropDownItem(WIDGET_NAME_COMPANIONINFO, MESSAGES.companionInformation(),
        new AboutCompanionAction()));
/* Commented out for now, we do not update the Companion ourselves anymore (except for
   the emulator). Instead we display a bar-code when necessary or direct people to the
   companionInformation menu item above */
/*    helpItems.add(new DropDownItem(WIDGET_NAME_COMPANIONUPDATE, MESSAGES.companionUpdate(),
      new CompanionUpdateAction())); */
    helpItems.add(new DropDownItem(WIDGET_NAME_SHOWSPLASH, MESSAGES.showSplashMenuItem(),
        new ShowSplashAction()));

    // Create the TopToolbar drop down menus.
    fileDropDown = new DropDownButton(WIDGET_NAME_PROJECT, MESSAGES.projectsTabName(),
        fileItems, false);
    connectDropDown = new DropDownButton(WIDGET_NAME_CONNECT_TO, MESSAGES.connectTabName(),
        connectItems, false);
    buildDropDown = new DropDownButton(WIDGET_NAME_BUILD, MESSAGES.buildTabName(),
        buildItems, false);
    helpDropDown = new DropDownButton(WIDGET_NAME_HELP, MESSAGES.helpTabName(),
        helpItems, false);

    // Set the DropDown Styles
    fileDropDown.setStyleName("ode-TopPanelButton");
    connectDropDown.setStyleName("ode-TopPanelButton");
    buildDropDown.setStyleName("ode-TopPanelButton");
    helpDropDown.setStyleName("ode-TopPanelButton");

    // Add the Buttons to the Toolbar.
    toolbar.add(fileDropDown);
    toolbar.add(connectDropDown);
    toolbar.add(buildDropDown);

    // Commented out language switching until we have a clean Chinese translation. (AFM)
    toolbar.add(helpDropDown);

    //Only if logged in as an admin, add the Admin Button
    if (Ode.getInstance().getUser().getIsAdmin()) {
      List<DropDownItem> adminItems = Lists.newArrayList();
      adminItems.add(new DropDownItem(WIDGET_NAME_DOWNLOAD_USER_SOURCE,
          MESSAGES.downloadUserSourceMenuItem(), new DownloadUserSourceAction()));
      adminItems.add(new DropDownItem(WIDGET_NAME_SWITCH_TO_DEBUG,
          MESSAGES.switchToDebugMenuItem(), new SwitchToDebugAction()));
      adminItems.add(new DropDownItem(WIDGET_NAME_USER_ADMIN,
          "User Admin", new SwitchToUserAdminAction()));
      adminDropDown = new DropDownButton(WIDGET_NAME_ADMIN, MESSAGES.adminTabName(), adminItems,
          false);
      adminDropDown.setStyleName("ode-TopPanelButton");
      toolbar.add(adminDropDown);
    }

    initWidget(toolbar);

  }

  // -----------------------------
  // List of Commands for use in Drop-Down Menus
  // -----------------------------

  private static class NewAction implements Command {
    @Override
    public void execute() {
      new NewYoungAndroidProjectWizard(null).center();
      // The wizard will switch to the design view when the new
      // project is created.
    }
  }

  private class SaveAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(null);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_YA, projectRootNode);
      }
    }
  }

  private class SaveAsAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new CopyYoungAndroidProjectCommand(false));
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_AS_YA, projectRootNode);
      }
    }
  }

  private class CheckpointAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new CopyYoungAndroidProjectCommand(true));
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_CHECKPOINT_YA, projectRootNode);
      }
    }
  }

  private static class SwitchToProjectAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().switchToProjectsView();
      Ode.getInstance().getTopToolbar().updateFileMenuButtons(0);
    }
  }

  private class WirelessAction implements Command {
    @Override
    public void execute() {
      if (Ode.getInstance().okToConnect()) {
        startRepl(true, false, false); // false means we are
                                       // *not* the emulator
      }
    }
  }

  private class EmulatorAction implements Command {
    @Override
    public void execute() {
      if (Ode.getInstance().okToConnect()) {
        startRepl(true, true, false); // true means we are the
                                      // emulator
      }
    }
  }

  private class UsbAction implements Command {
    @Override
    public void execute() {
      if (Ode.getInstance().okToConnect()) {
        startRepl(true, false, true);
      }
    }
  }

  private class ResetAction implements Command {
    @Override
    public void execute() {
      if (Ode.getInstance().okToConnect()) {
        startRepl(false, false, false); // We are really stopping the repl here
      }
    }
  }

  private class HardResetAction implements Command {
    @Override
    public void execute() {
      if (Ode.getInstance().okToConnect()) {
        replHardReset();
      }
    }
  }

  private class BarcodeAction implements Command {

    private boolean secondBuildserver = false;

    public BarcodeAction(boolean secondBuildserver) {
      this.secondBuildserver = secondBuildserver;
    }

    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new GenerateYailCommand(
                new BuildCommand(target, secondBuildserver,
                  new ShowProgressBarCommand(target,
                    new WaitForBuildResultCommand(target,
                      new ShowBarcodeCommand(target)), "BarcodeAction"))));
        if (!Ode.getInstance().getWarnBuild(secondBuildserver)) {
          cmd = new WarningDialogCommand(target, secondBuildserver, cmd);
          Ode.getInstance().setWarnBuild(secondBuildserver, true);
        }
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_BARCODE_YA, projectRootNode,
            new Command() {
              @Override
              public void execute() {
              }
            });
      }
    }
  }

  private class DownloadAction implements Command {

    private boolean secondBuildserver = false;

    DownloadAction(boolean secondBuildserver) {
      this.secondBuildserver = secondBuildserver;
    }

    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new GenerateYailCommand(
                new BuildCommand(target, secondBuildserver,
                  new ShowProgressBarCommand(target,
                    new WaitForBuildResultCommand(target,
                      new DownloadProjectOutputCommand(target)), "DownloadAction"))));
        if (!Ode.getInstance().getWarnBuild(secondBuildserver)) {
          cmd = new WarningDialogCommand(target, secondBuildserver, cmd);
          Ode.getInstance().setWarnBuild(secondBuildserver, true);
        }
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_DOWNLOAD_YA, projectRootNode,
            new Command() {
              @Override
              public void execute() {
              }
            });
      }
    }
  }
  private static class ExportProjectAction implements Command {
    @Override
    public void execute() {
      List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      if (Ode.getInstance().getCurrentView() == Ode.PROJECTS) {
        //If we are in the projects view
        if (selectedProjects.size() == 1) {
          exportProject(selectedProjects.get(0));
        } else {
          // The user needs to select only one project.
          ErrorReporter.reportInfo(MESSAGES.wrongNumberProjectsSelected());
        }
      } else {
        //If we are in the designer view.
        Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE + ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + Ode.getInstance().getCurrentYoungAndroidProjectId());
      }
    }

    private void exportProject(Project project) {
      Tracking.trackEvent(Tracking.PROJECT_EVENT,
          Tracking.PROJECT_ACTION_DOWNLOAD_PROJECT_SOURCE_YA, project.getProjectName());

      Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
          ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + project.getProjectId());
    }
  }

  private static class ExportAllProjectsAction implements Command {
    @Override
    public void execute() {
      Tracking.trackEvent(Tracking.PROJECT_EVENT,
          Tracking.PROJECT_ACTION_DOWNLOAD_ALL_PROJECTS_SOURCE_YA);

      // Is there a way to disable the Download All button until this completes?
      if (Window.confirm(MESSAGES.downloadAllAlert())) {

        Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
            ServerLayout.DOWNLOAD_ALL_PROJECTS_SOURCE);
      }
    }
  }

  private static class ImportProjectAction implements Command {
    @Override
    public void execute() {
      new ProjectUploadWizard().center();
    }
  }

  private static class ImportTemplateAction implements Command {
    @Override
    public void execute() {
      new TemplateUploadWizard().center();
    }
  }

  private static class DeleteAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
        @Override
        public void execute() {
          if (Ode.getInstance().getCurrentView() == Ode.PROJECTS) {
            List<Project> selectedProjects =
                ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
            if (selectedProjects.size() > 0) {
              // Show one confirmation window for selected projects.
              if (deleteConfirmation(selectedProjects)) {
                for (Project project : selectedProjects) {
                  deleteProject(project);
                }
              }
            } else {
              // The user can select a project to resolve the
              // error.
              ErrorReporter.reportInfo(MESSAGES.noProjectSelectedForDelete());
            }
          } else { //We are deleting a project in the designer view
            List<Project> selectedProjects = new ArrayList<Project>();
            Project currentProject = Ode.getInstance().getProjectManager().getProject(Ode.getInstance().getCurrentYoungAndroidProjectId());
            selectedProjects.add(currentProject);
            if (deleteConfirmation(selectedProjects)) {
              deleteProject(currentProject);
              //Add the command to stop this current project from saving
              Ode.getInstance().switchToProjectsView();
            }
          }
        }
      });
    }


    private boolean deleteConfirmation(List<Project> projects) {
      String message;
      GallerySettings gallerySettings = GalleryClient.getInstance().getGallerySettings();
      if (projects.size() == 1) {
        if (projects.get(0).isPublished())
          message = MESSAGES.confirmDeleteSinglePublishedProject(projects.get(0).getProjectName());
        else
          message = MESSAGES.confirmDeleteSingleProject(projects.get(0).getProjectName());
      } else {
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (Project project : projects) {
          sb.append(separator).append(project.getProjectName());
          separator = ", ";
        }
        String projectNames = sb.toString();
        if(!gallerySettings.galleryEnabled()){
          message = MESSAGES.confirmDeleteManyProjects(projectNames);
        } else {
          message = MESSAGES.confirmDeleteManyProjectsWithGalleryOn(projectNames);
        }
      }
      return Window.confirm(message);
    }

    private void deleteProject(Project project) {
      Tracking.trackEvent(Tracking.PROJECT_EVENT,
          Tracking.PROJECT_ACTION_DELETE_PROJECT_YA, project.getProjectName());

      final long projectId = project.getProjectId();

      Ode ode = Ode.getInstance();
      boolean isCurrentProject = (projectId == ode.getCurrentYoungAndroidProjectId());
      ode.getEditorManager().closeProjectEditor(projectId);
      if (isCurrentProject) {
        // If we're deleting the project that is currently open in the Designer we
        // need to clear the ViewerBox first.
        ViewerBox.getViewerBox().clear();
      }
      if (project.isPublished()) {
        doDeleteGalleryApp(project.getGalleryId());
      }
      // Make sure that we delete projects even if they are not open.
      doDeleteProject(projectId);
    }

    private void doDeleteProject(final long projectId) {
      Ode.getInstance().getProjectService().deleteProject(projectId,
          new OdeAsyncCallback<Void>(
              // failure message
              MESSAGES.deleteProjectError()) {
            @Override
            public void onSuccess(Void result) {
              Ode.getInstance().getProjectManager().removeProject(projectId);
              // Show a welcome dialog in case there are no
              // projects saved.
              if (Ode.getInstance().getProjectManager().getProjects().size() == 0) {
                Ode.getInstance().createNoProjectsDialog(true);
              }
            }
          });
    }
    private void doDeleteGalleryApp(final long galleryId) {
      Ode.getInstance().getGalleryService().deleteApp(galleryId,
          new OdeAsyncCallback<Void>(
              // failure message
              MESSAGES.galleryDeleteError()) {
            @Override
            public void onSuccess(Void result) {
              // need to update gallery list
              GalleryClient gallery = GalleryClient.getInstance();
              gallery.appWasChanged();
            }
          });
    }
  }

  private static class DownloadKeystoreAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
          new OdeAsyncCallback<Boolean>(MESSAGES.downloadKeystoreError()) {
            @Override
            public void onSuccess(Boolean keystoreFileExists) {
              if (keystoreFileExists) {
                Tracking.trackEvent(Tracking.USER_EVENT, Tracking.USER_ACTION_DOWNLOAD_KEYSTORE);
                Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
                    ServerLayout.DOWNLOAD_USERFILE + "/" + StorageUtil.ANDROID_KEYSTORE_FILENAME);
              } else {
                Window.alert(MESSAGES.noKeystoreToDownload());
              }
            }
          });
    }
  }

  private class UploadKeystoreAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
          new OdeAsyncCallback<Boolean>(MESSAGES.uploadKeystoreError()) {
            @Override
            public void onSuccess(Boolean keystoreFileExists) {
              if (!keystoreFileExists || Window.confirm(MESSAGES.confirmOverwriteKeystore())) {
                KeystoreUploadWizard wizard = new KeystoreUploadWizard(new Command() {
                  @Override
                  public void execute() {
                    Tracking.trackEvent(Tracking.USER_EVENT, Tracking.USER_ACTION_UPLOAD_KEYSTORE);
                    updateKeystoreFileMenuButtons();
                  }
                });
                wizard.center();
              }
            }
          });
    }
  }

  private class DeleteKeystoreAction implements Command {
    @Override
    public void execute() {
      final String errorMessage = MESSAGES.deleteKeystoreError();
      Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
          new OdeAsyncCallback<Boolean>(errorMessage) {
            @Override
            public void onSuccess(Boolean keystoreFileExists) {
              if (keystoreFileExists && Window.confirm(MESSAGES.confirmDeleteKeystore())) {
                Tracking.trackEvent(Tracking.USER_EVENT, Tracking.USER_ACTION_DELETE_KEYSTORE);
                Ode.getInstance().getUserInfoService().deleteUserFile(
                    StorageUtil.ANDROID_KEYSTORE_FILENAME,
                    new OdeAsyncCallback<Void>(errorMessage) {
                      @Override
                      public void onSuccess(Void result) {
                        // The android.keystore shouldn't exist at this point, so reset cached values.
                        isKeystoreCached = true;
                        isKeystorePresent = false;
                        isKeystoreCheckPending = false;
                        fileDropDown.setItemEnabled(MESSAGES.deleteKeystoreMenuItem(), false);
                        fileDropDown.setItemEnabled(MESSAGES.downloadKeystoreMenuItem(), false);
                      }
                    });
              }
            }
          });
    }
  }

  /**
   *  Made changes to the now Projects menu made name changes to the menu items
   * Implements the action to generate the ".yail" file for each screen in the current project.
   * It does not build the entire project. The intention is that this will be helpful for
   * debugging during development, and will most likely be disabled in the production system.
   */
  private class GenerateYailAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        ChainableCommand cmd = new SaveAllEditorsCommand(new GenerateYailCommand(null));
        //updateBuildButton(true);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_YAIL_YA, projectRootNode,
            new Command() {
              @Override
              public void execute() {
                //updateBuildButton(false);
              }
            });
      }
    }
  }

  private static class AboutAction implements Command {
    @Override
    public void execute() {
      final DialogBox db = new DialogBox(false, true);
      db.setText("About MIT App Inventor");
      db.setStyleName("ode-DialogBox");
      db.setHeight("200px");
      db.setWidth("400px");
      db.setGlassEnabled(true);
      db.setAnimationEnabled(true);
      db.center();

      VerticalPanel DialogBoxContents = new VerticalPanel();
      String html = MESSAGES.gitBuildId(GitBuildId.getDate(), GitBuildId.getVersion()) +
          "<BR/>Use Companion: " + BlocklyPanel.getCompVersion();
      Config config = Ode.getInstance().getSystemConfig();
      String releaseNotesUrl = config.getReleaseNotesUrl();
      if (!Strings.isNullOrEmpty(releaseNotesUrl)) {
        html += "<BR/><BR/>Please see <a href=\"" + releaseNotesUrl +
            "\" target=\"_blank\">release notes</a>";
      }
      String tosUrl = config.getTosUrl();
      if (!Strings.isNullOrEmpty(tosUrl)) {
        html += "<BR/><BR/><a href=\"" + tosUrl +
            "\" target=\"_blank\">" + MESSAGES.privacyTermsLink() + "</a>";
      }
      HTML message = new HTML(html);

      SimplePanel holder = new SimplePanel();
      Button ok = new Button("Close");
      ok.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          db.hide();
        }
      });
      holder.add(ok);
      DialogBoxContents.add(message);
      DialogBoxContents.add(holder);
      db.setWidget(DialogBoxContents);
      db.show();
    }
  }

  private static class AboutCompanionAction implements Command {
    @Override
    public void execute() {
      final DialogBox db = new DialogBox(false, true);
      db.setText("About The Companion");
      db.setStyleName("ode-DialogBox");
      db.setHeight("200px");
      db.setWidth("400px");
      db.setGlassEnabled(true);
      db.setAnimationEnabled(true);
      db.center();

      String downloadinfo = "";
      if (!YaVersion.COMPANION_UPDATE_URL1.equals("")) {
        String url = "http://" + Window.Location.getHost() + YaVersion.COMPANION_UPDATE_URL1;
        downloadinfo = "<br/>\n<a href=" + url + ">Download URL: " + url + "</a><br/>\n";
        downloadinfo += BlocklyPanel.getQRCode(url);
      }

      VerticalPanel DialogBoxContents = new VerticalPanel();
      HTML message = new HTML(
          "Companion Version " + BlocklyPanel.getCompVersion() + downloadinfo
      );

      SimplePanel holder = new SimplePanel();
      Button ok = new Button("Close");
      ok.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          db.hide();
        }
      });
      holder.add(ok);
      DialogBoxContents.add(message);
      DialogBoxContents.add(holder);
      db.setWidget(DialogBoxContents);
      db.show();
    }
  }

  private static class CompanionUpdateAction implements Command {
    @Override
    public void execute() {
      DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
      if (currentProject == null) {
        Window.alert(MESSAGES.companionUpdateMustHaveProject());
        return;
      }
      DesignToolbar.Screen screen = currentProject.screens.get(currentProject.currentScreen);
      screen.blocksEditor.updateCompanion();
    }
  }

  private static class ShowSplashAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().showWelcomeDialog();
    }
  }

  private static class WindowOpenAction implements Command {
    private final String url;

    WindowOpenAction(String url) {
      this.url = url;
    }

    @Override
    public void execute() {
      Window.open(url, WINDOW_OPEN_LOCATION, WINDOW_OPEN_FEATURES);
    }
  }

  private static class ImportComponentAction implements Command {
    @Override
    public void execute() {
      new ComponentImportWizard().center();
    }
  }

  private static class BuildComponentAction implements Command {
    @Override
    public void execute() {
      // to be added
    }
  }

  private static class UploadComponentAction implements Command {
    @Override
    public void execute() {
      new ComponentUploadWizard().show();
    }
  }

  private void updateConnectToDropDownButton(boolean isEmulatorRunning, boolean isCompanionRunning, boolean isUsbRunning){
    if (!isEmulatorRunning && !isCompanionRunning && !isUsbRunning) {
      connectDropDown.setItemEnabled(MESSAGES.AICompanionMenuItem(), true);
      connectDropDown.setItemEnabled(MESSAGES.emulatorMenuItem(), true);
      connectDropDown.setItemEnabled(MESSAGES.usbMenuItem(), true);
    } else {
      connectDropDown.setItemEnabled(MESSAGES.AICompanionMenuItem(), false);
      connectDropDown.setItemEnabled(MESSAGES.emulatorMenuItem(), false);
      connectDropDown.setItemEnabled(MESSAGES.usbMenuItem(), false);
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
   *
   * @param start -- true to start the repl, false to stop it.
   * @param forEmulator -- true if we are connecting to the emulator.
   * @param forUsb -- true if this is a USB connection.
   *
   * If both forEmulator and forUsb are false, then we are connecting
   * via Wireless.
   */

  private void startRepl(boolean start, boolean forEmulator, boolean forUsb) {
    DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    if (currentProject == null) {
      OdeLog.wlog("DesignToolbar.currentProject is null. "
            + "Ignoring attempt to start the repl.");
      return;
    }
    DesignToolbar.Screen screen = currentProject.screens.get(currentProject.currentScreen);
    screen.blocksEditor.startRepl(!start, forEmulator, forUsb);
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

  private void replHardReset() {
    DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    if (currentProject == null) {
      OdeLog.wlog("DesignToolbar.currentProject is null. "
            + "Ignoring attempt to do hard reset.");
      return;
    }
    DesignToolbar.Screen screen = currentProject.screens.get(currentProject.currentScreen);
    ((YaBlocksEditor)screen.blocksEditor).hardReset();
    updateConnectToDropDownButton(false, false, false);
  }

  /**
   * Enables and/or disables buttons based on how many projects exist
   * (in the case of "Download All Projects") or are selected (in the case
   * of "Delete" and "Download Source").
   */
  public void updateFileMenuButtons(int view) {
    if (isReadOnly) {
      // This may be too simple
      return;
    }
    if (view == 0) {  // We are in the Projects view
      fileDropDown.setItemEnabled(MESSAGES.deleteProjectButton(), false);
      fileDropDown.setItemEnabled(MESSAGES.deleteProjectMenuItem(),
          Ode.getInstance().getProjectManager().getProjects() == null);
      fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(),
          Ode.getInstance().getProjectManager().getProjects().size() > 0);
      fileDropDown.setItemEnabled(MESSAGES.exportProjectMenuItem(), false);
      fileDropDown.setItemEnabled(MESSAGES.saveMenuItem(), false);
      fileDropDown.setItemEnabled(MESSAGES.saveAsMenuItem(), false);
      fileDropDown.setItemEnabled(MESSAGES.checkpointMenuItem(), false);
      buildDropDown.setItemEnabled(MESSAGES.showBarcodeMenuItem(), false);
      buildDropDown.setItemEnabled(MESSAGES.downloadToComputerMenuItem(), false);
    } else { // We have to be in the Designer/Blocks view
      fileDropDown.setItemEnabled(MESSAGES.deleteProjectButton(), true);
      fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(),
          Ode.getInstance().getProjectManager().getProjects().size() > 0);
      fileDropDown.setItemEnabled(MESSAGES.exportProjectMenuItem(), true);
      fileDropDown.setItemEnabled(MESSAGES.saveMenuItem(), true);
      fileDropDown.setItemEnabled(MESSAGES.saveAsMenuItem(), true);
      fileDropDown.setItemEnabled(MESSAGES.checkpointMenuItem(), true);
      buildDropDown.setItemEnabled(MESSAGES.showBarcodeMenuItem(), true);
      buildDropDown.setItemEnabled(MESSAGES.downloadToComputerMenuItem(), true);
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

  //Admin commands
  private static class DownloadUserSourceAction implements Command {
    @Override
    public void execute() {
      new DownloadUserSourceWizard().center();
    }
  }

  private static class SwitchToDebugAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().switchToDebuggingView();
    }
  }

  private static class SwitchToUserAdminAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().switchToUserAdminPanel();
    }
  }

}
