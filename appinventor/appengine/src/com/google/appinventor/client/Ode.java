// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import java.util.Random;

import java.util.List;
import java.util.logging.Logger;

import com.google.appinventor.client.boxes.AdminUserListBox;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.PrivateUserProfileTabPanel;
import com.google.appinventor.client.boxes.MessagesOutputBox;
import com.google.appinventor.client.boxes.OdeLogBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.boxes.ModerationPageBox;
import com.google.appinventor.client.boxes.GalleryListBox;
import com.google.appinventor.client.boxes.GalleryAppBox;
import com.google.appinventor.client.boxes.ProfileBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.EditorManager;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.appinventor.client.editor.youngandroid.TutorialPanel;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.CommandRegistry;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeAdapter;
import com.google.appinventor.client.explorer.project.ProjectManager;
import com.google.appinventor.client.explorer.project.ProjectManagerEventAdapter;
import com.google.appinventor.client.explorer.youngandroid.GalleryList;
import com.google.appinventor.client.explorer.youngandroid.GalleryPage;
import com.google.appinventor.client.explorer.youngandroid.GalleryToolbar;
import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
import com.google.appinventor.client.explorer.youngandroid.ReportList;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.user.UserSettings;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.PZAwarePositionCallback;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.client.widgets.boxes.ColumnLayout;
import com.google.appinventor.client.widgets.boxes.ColumnLayout.Column;
import com.google.appinventor.client.widgets.boxes.WorkAreaPanel;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.rpc.cloudDB.CloudDBAuthService;
import com.google.appinventor.shared.rpc.cloudDB.CloudDBAuthServiceAsync;
import com.google.appinventor.shared.rpc.component.ComponentService;
import com.google.appinventor.shared.rpc.component.ComponentServiceAsync;
import com.google.appinventor.shared.rpc.GetMotdService;
import com.google.appinventor.shared.rpc.GetMotdServiceAsync;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.admin.AdminInfoService;
import com.google.appinventor.shared.rpc.admin.AdminInfoServiceAsync;
import com.google.appinventor.shared.rpc.help.HelpService;
import com.google.appinventor.shared.rpc.help.HelpServiceAsync;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.GallerySettings;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectService;
import com.google.appinventor.shared.rpc.project.ProjectServiceAsync;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.GalleryServiceAsync;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.UserInfoService;
import com.google.appinventor.shared.rpc.user.UserInfoServiceAsync;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.appinventor.shared.rpc.project.GalleryApp;

/**
 * Main entry point for Ode. Defines the startup UI elements in
 * {@link #onModuleLoad()}.
 *
 */
public class Ode implements EntryPoint {
  private static final Logger LOG = Logger.getLogger(Ode.class.getName());
  // I18n messages
  public static final OdeMessages MESSAGES = GWT.create(OdeMessages.class);

  // Global instance of the Ode object
  private static Ode instance;

  // Application level image bundle
  private static final Images IMAGES = GWT.create(Images.class);

  // ProjectEditor registry
  private static final ProjectEditorRegistry EDITORS = new ProjectEditorRegistry();

  // Command registry
  private static final CommandRegistry COMMANDS = new CommandRegistry();

  // System config
  private static Config config;

  // User settings
  private static UserSettings userSettings;

  // Gallery settings
  private static GallerySettings gallerySettings;

  private MotdFetcher motdFetcher;

  // User information
  private User user;

  // Template path if set by /?repo=
  private String templatePath;
  private boolean templateLoadingFlag = false;

  // Gallery id if set by /?galleryId=
  private String galleryId;
  private boolean galleryIdLoadingFlag = false;

  // Nonce Information
  private String nonce;

  // Read Only Flag: If true, the UI will not permit operations which permit
  // write requests

  private boolean isReadOnly;

  private String sessionId = generateUuid(); // Create new session id
  private Random random = new Random(); // For generating random nonce


  // Collection of projects
  private ProjectManager projectManager;

  // Collection of editors
  private EditorManager editorManager;

  // Currently active file editor, could be a YaFormEditor or a YaBlocksEditor or null.
  private FileEditor currentFileEditor;

  private AssetManager assetManager;

  // Remembers the current View
  static final int DESIGNER = 0;
  static final int PROJECTS = 1;
  private static final int GALLERY = 2;
  private static final int GALLERYAPP = 3;
  private static final int USERPROFILE = 4;
  private static final int PRIVATEUSERPROFILE = 5;
  private static final int MODERATIONPAGE = 6;
  private static final int USERADMIN = 7;
  private static int currentView = DESIGNER;

  /*
   * The following fields define the general layout of the UI as seen in the following diagram:
   *
   *  +-- mainPanel --------------------------------+
   *  |+-- topPanel -------------------------------+|
   *  ||                                           ||
   *  |+-------------------------------------------+|
   *  |+-- overDeckPanel --+-----------------------+|
   *  || tutorialPanel     |  deckPanel            ||
   *  |+-------------------+-----------------------+|
   *  |+-- statusPanel ----------------------------+|
   *  ||                                           ||
   *  |+-------------------------------------------+|
   *  +---------------------------------------------+
   */
  private DeckPanel deckPanel;
  private HorizontalPanel overDeckPanel;
  private Frame tutorialPanel;
  private int projectsTabIndex;
  private int designTabIndex;
  private int debuggingTabIndex;
  private int galleryTabIndex;
  private int galleryAppTabIndex;
  private int userAdminTabIndex;
  private int userProfileTabIndex;
  private int privateUserProfileIndex;
  private int moderationPageTabIndex;
  private TopPanel topPanel;
  private StatusPanel statusPanel;
  private HorizontalPanel workColumns;
  private VerticalPanel structureAndAssets;
  private ProjectToolbar projectToolbar;
  private GalleryToolbar galleryListToolbar;
  private GalleryToolbar galleryPageToolbar;
  private AdminUserListBox uaListBox;
  private DesignToolbar designToolbar;
  private TopToolbar topToolbar;

  // Is the tutorial toolbar currently displayed?
  private boolean tutorialVisible = false;

  // Popup that indicates that an asynchronous request is pending. It is visible
  // initially, and will be hidden automatically after the first RPC completes.
  private static RpcStatusPopup rpcStatusPopup;

  // Web service for help information
  private final HelpServiceAsync helpService = GWT.create(HelpService.class);

  // Web service for project related information
  private final ProjectServiceAsync projectService = GWT.create(ProjectService.class);

  // Web service for gallery related information
  private final GalleryServiceAsync galleryService = GWT.create(GalleryService.class);

  // Web service for user related information
  private final UserInfoServiceAsync userInfoService = GWT.create(UserInfoService.class);

  // Web service for get motd information
  private final GetMotdServiceAsync getMotdService = GWT.create(GetMotdService.class);

  // Web service for component related operations
  private final ComponentServiceAsync componentService = GWT.create(ComponentService.class);
  private final AdminInfoServiceAsync adminInfoService = GWT.create(AdminInfoService.class);

  //Web service for CloudDB authentication operations
  private final CloudDBAuthServiceAsync cloudDBAuthService = GWT.create(CloudDBAuthService.class);

  private boolean windowClosing;

  private boolean screensLocked;

  private boolean galleryInitialized = false;

  /**
   * Flag set if we may need to show the splash screen based on
   * current user settings.
   */
  private boolean mayNeedSplash = false;

  /**
   * Flag to inidcate that we have already transitioned away from the
   * project list to a project (auto-open feature).
   */
  private boolean didTransitionFromProjectList = false;

  /**
   * Flag indicating that we have shown the splash screens.
   */
  private boolean didShowSplash = false;

  private SplashConfig splashConfig; // Splash Screen Configuration

  private boolean secondBuildserver = false; // True if we have a second
                                             // buildserver.

  // The flags below are used by the Build menus. Because we have two
  // different buildservers, we have two sets of build menu items, one
  // for each buildserver.  The first time one is selected, we put up
  // a warning/notice dialog box explaining its purpose. We don't show
  // it again during the same session, and keeping track of that is
  // the purpose of these two flags.

  private boolean warnedBuild1 = false;
  private boolean warnedBuild2 = false;

  /**
   * Returns global instance of Ode.
   *
   * @return  global Ode instance
   */
  public static Ode getInstance() {
    return instance;
  }

  /**
   * Returns instance of the aggregate image bundle for the application.
   *
   * @return  image bundle
   */
  public static Images getImageBundle() {
    return IMAGES;
  }

  /**
   * Returns the editor registry.
   *
   * @return the editor registry
   */
  public static ProjectEditorRegistry getProjectEditorRegistry() {
    return EDITORS;
  }

  /**
   * Returns the command registry.
   *
   * @return the command registry
   */
  public static CommandRegistry getCommandRegistry() {
    return COMMANDS;
  }

  /**
   * Returns the system config.
   *
   * @return  system config
   */
  public static Config getSystemConfig() {
    return config;
  }

  /**
   * Returns the user settings.
   *
   * @return  user settings
   */
  public static UserSettings getUserSettings() {
    return userSettings;
  }

  /**
   * Returns the gallery settings.
   *
   * @return  gallery settings
   */
  public static GallerySettings getGallerySettings() {
    return gallerySettings;
  }

  /**
   * loads the gallery settings from server
   *
   */
  public void  loadGallerySettings() {
     // Callback for when the server returns us the apps
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<GallerySettings> callback = new OdeAsyncCallback<GallerySettings>(
    // failure message
    MESSAGES.gallerySettingsError()) {
      @Override
      public void onSuccess(GallerySettings settings) {
        gallerySettings = settings;
        if(gallerySettings.galleryEnabled() == true){
          ProjectListBox.getProjectListBox().getProjectList().setPublishedHeaderVisible(true);
          projectToolbar.setPublishOrUpdateButtonVisible(true);
          GalleryClient.getInstance().setSystemEnvironment(settings.getEnvironment());
          topPanel.showGalleryLink(true);
          if(user.isModerator()){
            topPanel.showModerationLink(true);
          }
          topPanel.updateAccountMessageButton();
        }else{
          topPanel.showModerationLink(false);
          topPanel.showGalleryLink(false);
          projectToolbar.setPublishOrUpdateButtonVisible(false);
          ProjectListBox.getProjectListBox().getProjectList().setPublishedHeaderVisible(false);
        }
      }
    };
    //this is below the call back, but of course it is done first
    ode.getGalleryService().loadGallerySettings(callback);
  }

  /**
   * Returns the asset manager.
   *
   * @return  asset manager
   */
  public AssetManager getAssetManager() {
    return assetManager;
  }

  /**
   * Returns true if we have received the window closing event.
   */
  public static boolean isWindowClosing() {
    return getInstance().windowClosing;
  }

  /**
   * Get the current view
   */
  public int getCurrentView() {
    return currentView;
  }

  /**
   * Switch to the Projects tab
   */
  public void switchToProjectsView() {
    // We may need to pass the code below as a runnable to
    // screenShotMaybe() so build the runnable now
    hideTutorials();
    Runnable next = new Runnable() {
        @Override
        public void run() {
          if(currentView != PROJECTS) { //If we are switching to projects view from somewhere else, clear all of the previously selected projects.
            ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects().clear();
            ProjectListBox.getProjectListBox().getProjectList().refreshTable(false);
          }
          currentView = PROJECTS;
          getTopToolbar().updateFileMenuButtons(currentView);
          deckPanel.showWidget(projectsTabIndex);
          // If we started a project, then the start button was disabled (to avoid
          // a second press while the new project wizard was starting (aka we "debounce"
          // the button). When the person switches to the projects list view again (here)
          // we re-enable it.
          projectToolbar.enableStartButton();
        }
      };
    if (designToolbar.getCurrentView() != DesignToolbar.View.BLOCKS) {
      next.run();
    } else {
      // maybe take a screenshot, second argument is true so we wait for i/o to complete
      screenShotMaybe(next, true);
    }
  }

  /**
   * Switch to the User Admin Panel
   */

  public void switchToUserAdminPanel() {
    hideTutorials();
    currentView = USERADMIN;
    deckPanel.showWidget(userAdminTabIndex);
  }

  /**
   * Switch to the Gallery tab
   */
  public void switchToGalleryView() {
    hideTutorials();
    if (!galleryInitialized) {
      // Gallery initialization is deferred until now.
      initializeGallery();
    }
    currentView = GALLERY;
    deckPanel.showWidget(galleryTabIndex);
  }

  /**
   * Switch to the Gallery App
   */
  public void switchToGalleryAppView(GalleryApp app, int editStatus) {
    hideTutorials();
    if (!galleryInitialized) {
      // Gallery initialization is deferred until now.
      initializeGallery();
    }
    currentView = GALLERYAPP;
    GalleryAppBox.setApp(app, editStatus);
    deckPanel.showWidget(galleryAppTabIndex);
  }

  /**
   * Switch to the user profile
   * TODO: change string parameter
   */
  public void switchToUserProfileView(String userId, int editStatus) {
    hideTutorials();
    currentView = USERPROFILE;
    OdeLog.log("###########" + userId + "||||||" + editStatus);
    ProfileBox.setProfile(userId, editStatus);
    deckPanel.showWidget(userProfileTabIndex);
  }

  /**
   * Switch to the Designer tab. Shows an error message if there is no currentFileEditor.
   */
  public void switchToDesignView() {
    // Only show designer if there is a current editor.
    // ***** THE DESIGNER TAB DOES NOT DISPLAY CORRECTLY IF THERE IS NO CURRENT EDITOR. *****
    showTutorials();
    currentView = DESIGNER;
    getTopToolbar().updateFileMenuButtons(currentView);
    if (currentFileEditor != null) {
      deckPanel.showWidget(designTabIndex);
    } else if (!editorManager.hasOpenEditor()) {  // is there a project editor pending visibility?
      OdeLog.wlog("No current file editor to show in designer");
      ErrorReporter.reportInfo(MESSAGES.chooseProject());
    }
  }

  /**
   * Switch to Gallery TabPanel
   */
  public void switchToPrivateUserProfileView() {
    currentView = privateUserProfileIndex;
    deckPanel.showWidget(privateUserProfileIndex);
  }

  /**
   * Switch to the Moderation Page tab
   */
  public void switchToModerationPageView() {
    hideTutorials();
    if (!galleryInitialized) {
      initializeGallery();
    }
    currentView = MODERATIONPAGE;
    deckPanel.showWidget(moderationPageTabIndex);
  }
  /**
   * Switch to the Debugging tab
   */
  public void switchToDebuggingView() {
    hideTutorials();
    deckPanel.showWidget(debuggingTabIndex);

    // NOTE(lizlooney) - Calling resizeWorkArea for debuggingTab prevents the
    // boxes from overlapping each other.
    resizeWorkArea((WorkAreaPanel) deckPanel.getWidget(debuggingTabIndex));
  }

  public void openPreviousProject() {
    if (userSettings == null) {
      OdeLog.wlog("Ignoring openPreviousProject() since userSettings is null");
      return;
    }
    OdeLog.log("Ode.openPreviousProject called");
    final String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      getPropertyValue(SettingsConstants.GENERAL_SETTINGS_CURRENT_PROJECT_ID);

    // Retrieve the userTemplates
    String userTemplates = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      getPropertyValue(SettingsConstants.USER_TEMPLATE_URLS);
    TemplateUploadWizard.setStoredTemplateUrls(userTemplates);

    if (templateLoadingFlag) {  // We are loading a template, open it instead
                                // of the last project
      NewProjectCommand callbackCommand = new NewProjectCommand() {
          @Override
          public void execute(Project project) {
            templateLoadingFlag = false;
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
          }
        };
      TemplateUploadWizard.openProjectFromTemplate(templatePath, callbackCommand);
    } else if(galleryIdLoadingFlag){
      try {
        long galleryId_Long = Long.valueOf(galleryId);
        final OdeAsyncCallback<GalleryApp> callback = new OdeAsyncCallback<GalleryApp>(
            // failure message
            MESSAGES.galleryError()) {
              @Override
              public void onSuccess(GalleryApp app) {
                if(app == null){
                  openProject(value);
                  Window.alert(MESSAGES.galleryIdNotExist());
                }else{
                  Ode.getInstance().switchToGalleryAppView(app, GalleryPage.VIEWAPP);
                }
              }
            };
        Ode.getInstance().getGalleryService().getApp(galleryId_Long, callback);
      } catch (NumberFormatException e) {
        openProject(value);
        Window.alert(MESSAGES.galleryIdNotExist());
      }
    } else {
      openProject(value);
    }
  }

  private void openProject(String projectIdString) {
    OdeLog.log("Ode.openProject called for " + projectIdString);
    if (projectIdString.equals("")) {
      openPreviousProject();
    } else if (!projectIdString.equals("0")) {
      final long projectId = Long.parseLong(projectIdString);
      Project project = projectManager.getProject(projectId);
      if (project != null) {
        openYoungAndroidProjectInDesigner(project);
      } else {
        // The project hasn't been added to the ProjectManager yet.
        // Add a ProjectManagerEventListener so we'll be notified when it has been added.
        // Alternatively, it is an invalid projectId. In which case,
        // nothing happens since if the listener eventually fires
        // it will not match the projectId.
        projectManager.addProjectManagerEventListener(new ProjectManagerEventAdapter() {
          @Override
          public void onProjectAdded(Project project) {
            if (project.getProjectId() == projectId) {
              projectManager.removeProjectManagerEventListener(this);
              openYoungAndroidProjectInDesigner(project);
            }
          }
          @Override
          public void onProjectsLoaded() {
            // we only get here iff onProjectAdded is never called with the target project id
            projectManager.removeProjectManagerEventListener(this);
            switchToProjectsView();  // the user will need to select a project...
            ErrorReporter.reportInfo(MESSAGES.chooseProject());
          }
        });
      }
    }
    // else projectIdString == 0; do nothing
  }

  public void openYoungAndroidProjectInDesigner(final Project project) {
    ProjectRootNode projectRootNode = project.getRootNode();
    if (projectRootNode == null) {
      // The project nodes haven't been loaded yet.
      // Add a ProjectChangeListener so we'll be notified when they have been loaded.
      project.addProjectChangeListener(new ProjectChangeAdapter() {
        @Override
        public void onProjectLoaded(Project projectLoaded) {
          project.removeProjectChangeListener(this);
          openYoungAndroidProjectInDesigner(project);
        }
      });
      project.loadProjectNodes();

    } else {
      // The project nodes have been loaded. Tell the viewer to open
      // the project. This will cause the projects source files to be fetched
      // asynchronously, and loaded into file editors.
      ViewerBox.getViewerBox().show(projectRootNode);
      // Note: we can't call switchToDesignView until the Screen1 file editor
      // finishes loading. We leave that to setCurrentFileEditor(), which
      // will get called at the appropriate time.
      String projectIdString = Long.toString(project.getProjectId());
      if (!History.getToken().equals(projectIdString)) {
        // insert token into history but do not trigger listener event
        History.newItem(projectIdString, false);
      }
      if (assetManager == null) {
        assetManager = AssetManager.getInstance();
      }
      assetManager.loadAssets(project.getProjectId());
    }
    getTopToolbar().updateFileMenuButtons(1);
  }

  /**
   * Returns i18n compatible messages
   * @return messages
   */
  public static OdeMessages getMessages() {
    return MESSAGES;
  }

  /**
   * Returns the rpcStatusPopup object.
   * @return RpcStatusPopup
   */
  public static RpcStatusPopup getRpcStatusPopup() {
    return rpcStatusPopup;
  }

  /**
   * Main entry point for Ode. Setting up the UI and the web service
   * connections.
   */
  @Override
  public void onModuleLoad() {
    Tracking.trackPageview();

    // Handler for any otherwise unhandled exceptions
    GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
      @Override
      public void onUncaughtException(Throwable e) {
        OdeLog.xlog(e);

        if (AppInventorFeatures.sendBugReports()) {
          if (Window.confirm(MESSAGES.internalErrorReportBug())) {
            Window.open(BugReport.getBugReportLink(e), "_blank", "");
          }
        } else {
          // Display a confirm dialog with error msg and if 'ok' open the debugging view
          if (Window.confirm(MESSAGES.internalErrorClickOkDebuggingView())) {
            Ode.getInstance().switchToDebuggingView();
          }
        }
      }
    });

    // Initialize global Ode instance
    instance = this;

    // Let's see if we were started with a repo= parameter which points to a template
    templatePath = Window.Location.getParameter("repo");
    if (templatePath != null) {
      OdeLog.wlog("Got a template path of " + templatePath);
      templateLoadingFlag = true;
    }

    // Let's see if we were started with a galleryId= parameter which points to a template
    galleryId = Window.Location.getParameter("galleryId");
    if(galleryId != null){
      OdeLog.wlog("Got a galleryId of " + galleryId);
      galleryIdLoadingFlag = true;
    }

    // We call this below to initialize the ConnectProgressBar
    ConnectProgressBar.getInstance();

    // Get user information.
    OdeAsyncCallback<Config> callback = new OdeAsyncCallback<Config>(
        // failure message
        MESSAGES.serverUnavailable()) {

      @Override
      public void onSuccess(Config result) {
        config = result;
        user = result.getUser();
        isReadOnly = user.isReadOnly();

        // load the user's backpack if we are not using a shared
        // backpack

        String backPackId = user.getBackpackId();
        if (backPackId == null || backPackId.isEmpty()) {
          loadBackpack();
          OdeLog.log("backpack: No shared backpack");
        } else {
          BlocklyPanel.setSharedBackpackId(backPackId);
          OdeLog.log("Have a shared backpack backPackId = " + backPackId);
        }

        // Setup noop timer (if enabled)
        int noop = config.getNoop();
        if (noop > 0) {
          // If we have a noop time, setup a timer to do the noop
          Timer t = new Timer() {
              @Override
              public void run() {
                userInfoService.noop(new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void e) {
                    }
                    @Override
                    public void onFailure(Throwable e) {
                    }
                  });
              }
            };
            t.scheduleRepeating(1000*60*noop);
        }

        // If user hasn't accepted terms of service, ask them to.
        if (!user.getUserTosAccepted() && !isReadOnly) {
          // We expect that the redirect to the TOS page should be handled
          // by the onFailure method below. The server should return a
          // "forbidden" error if the TOS wasn't accepted.
          ErrorReporter.reportError(MESSAGES.serverUnavailable());
          return;
        }

        splashConfig = result.getSplashConfig();
        secondBuildserver = result.getSecondBuildserver();
        // The code below is invoked if we do not have a second buildserver
        // configured. It sets the warnedBuild1 flag to true which inhibits
        // the display of the dialog box used when building. This means that
        // if no second buildserver is configured, there is no dialog box
        // displayed when the build menu items are invoked.
        if (!secondBuildserver) {
          warnedBuild1 = true;
        }

        if (result.getRendezvousServer() != null) {
          setRendezvousServer(result.getRendezvousServer());
        } else {
          setRendezvousServer(YaVersion.RENDEZVOUS_SERVER);
        }

        userSettings = new UserSettings(user);
        userSettings.loadSettings(new Command() {
          @Override
          public void execute() {

            // Gallery settings
            gallerySettings = new GallerySettings();
            //gallerySettings.loadGallerySettings();
            loadGallerySettings();

            // Initialize project and editor managers
            // The project manager loads the user's projects asynchronously
            projectManager = new ProjectManager();
            projectManager.addProjectManagerEventListener(new ProjectManagerEventAdapter() {
              @Override
              public void onProjectsLoaded() {
                projectManager.removeProjectManagerEventListener(this);
                openPreviousProject();

                // This handles any built-in templates stored in /war
                // Retrieve template data stored in war/templates folder and
                // and save it for later use in TemplateUploadWizard
                OdeAsyncCallback<String> templateCallback =
                    new OdeAsyncCallback<String>(
                        // failure message
                        MESSAGES.createProjectError()) {
                      @Override
                      public void onSuccess(String json) {
                        // Save the templateData
                        TemplateUploadWizard.initializeBuiltInTemplates(json);
                      }
                    };
                Ode.getInstance().getProjectService().retrieveTemplateData(TemplateUploadWizard.TEMPLATES_ROOT_DIRECTORY, templateCallback);
              }
            });
            editorManager = new EditorManager();

            // Initialize UI
            initializeUi();

            topPanel.showUserEmail(user.getUserEmail());
          }
        });
      }

      private boolean isSet(String str) {
        return str != null && !str.equals("");
      }

      private String makeUri(String base) {
        String[] params = new String[] { "locale", "repo", "galleryId" };
        String separator = "?";
        StringBuilder sb = new StringBuilder(base);
        for (String param : params) {
          String value = Window.Location.getParameter(param);
          if (isSet(value)) {
            sb.append(separator);
            sb.append(param);
            sb.append("=");
            sb.append(value);
            separator = "&";
          }
        }
        return sb.toString();
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof StatusCodeException) {
          StatusCodeException e = (StatusCodeException) caught;
          int statusCode = e.getStatusCode();
          switch (statusCode) {
            case Response.SC_UNAUTHORIZED:
              // unauthorized => not on whitelist
              // getEncodedResponse() gives us the message that we wrote in
              // OdeAuthFilter.writeWhitelistErrorMessage().
              Window.alert(e.getEncodedResponse());
              return;
            case Response.SC_FORBIDDEN:
              // forbidden => need tos accept
              Window.open(makeUri("/" + ServerLayout.YA_TOS_FORM), "_self", null);
              return;
            case Response.SC_PRECONDITION_FAILED:
              Window.Location.replace(makeUri("/login/"));
              return;           // likely not reached
          }
        }
        super.onFailure(caught);
      }
    };

    // The call below begins an asynchronous read of the user's settings
    // When the settings are finished reading, various settings parsers
    // will be called on the returned JSON object. They will call various
    // other functions in this module, including openPreviousProject (the
    // previous project ID is stored in the settings) as well as the splash
    // screen displaying functions below.
    //
    // TODO(user): ODE makes too many RPC requests at startup time. Currently
    // we do 3 RPCs + 1 per project + 1 per open file. We should bundle some of
    // those with each other or with the initial HTML transfer.
    //
    // This call also stores our sessionId in the backend. This will be checked
    // when we go to save a file and if different file saving will be disabled
    // Newer sessions invalidate older sessions.

    userInfoService.getSystemConfig(sessionId, callback);

    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        openProject(event.getValue());
      }
    });

    // load project based on current url
    // TODO(sharon): Seems like a possible race condition here if the onValueChange
    // handler defined above gets called before the getSystemConfig call sets
    // userSettings.
    // The following line causes problems with GWT debugging, and commenting
    // it out doesn't seem to break things.
    //History.fireCurrentHistoryState();
  }

  /*
   * Initializes all UI elements.
   */
  private void initializeUi() {
    rpcStatusPopup = new RpcStatusPopup();

    // Register services with RPC status popup
    rpcStatusPopup.register((ExtendedServiceProxy<?>) helpService);
    rpcStatusPopup.register((ExtendedServiceProxy<?>) projectService);
    rpcStatusPopup.register((ExtendedServiceProxy<?>) galleryService);
    rpcStatusPopup.register((ExtendedServiceProxy<?>) userInfoService);

    Window.setTitle(MESSAGES.titleYoungAndroid());
    Window.enableScrolling(true);

    topPanel = new TopPanel();
    statusPanel = new StatusPanel();

    DockPanel mainPanel = new DockPanel();
    mainPanel.add(topPanel, DockPanel.NORTH);

    // Create the Tutorial Panel
    tutorialPanel = new TutorialPanel();
    tutorialPanel.setWidth("100%");
    tutorialPanel.setHeight("100%");
    // Initially we do not display it. If the project we load has
    // a tutorial URL, then we will set this visible when we load
    // the project
    tutorialPanel.setVisible(false);

    // Create tab panel for subsequent tabs
    deckPanel = new DeckPanel() {
      @Override
      public final void onBrowserEvent(Event event) {
        switch (event.getTypeInt()) {
          case Event.ONCONTEXTMENU:
            event.preventDefault();
            break;
        }
      }
    };

    deckPanel.setAnimationEnabled(true);
    deckPanel.sinkEvents(Event.ONCONTEXTMENU);
    deckPanel.setStyleName("ode-DeckPanel");

    // Projects tab
    VerticalPanel pVertPanel = new VerticalPanel() {
        /**
         * Flag to indicate the project list has been rendered at least once.
         */
        private boolean rendered = false;

        // Override to add splash screen behavior after leaving the project list
        @Override
        public void setVisible(boolean visible) {
          super.setVisible(visible);
          if (visible && !rendered) {
            // setVisible(false) will be called during UI initialization.
            // this flag indicates we are now being shown (possibly again...)
            rendered = true;
            maybeShowSplash2();  // in case of new user; they have no projects!
          } else if (rendered && !visible && (mayNeedSplash || shouldShowWelcomeDialog())
                     && !didShowSplash) {
            showSplashScreens();
          }
        }
      };
    pVertPanel.setWidth("100%");
    pVertPanel.setSpacing(0);
    HorizontalPanel projectListPanel = new HorizontalPanel();
    projectListPanel.setWidth("100%");
    projectToolbar = new ProjectToolbar();
    projectListPanel.add(ProjectListBox.getProjectListBox());
    pVertPanel.add(projectToolbar);
    pVertPanel.add(projectListPanel);
    projectsTabIndex = deckPanel.getWidgetCount();
    deckPanel.add(pVertPanel);

    // Design tab
    VerticalPanel dVertPanel = new VerticalPanel();
    dVertPanel.setWidth("100%");
    dVertPanel.setHeight("100%");

    // Add the Code Navigation arrow
//    switchToBlocksButton = new VerticalPanel();
//    switchToBlocksButton.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
//    switchToBlocksButton.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
//    switchToBlocksButton.setStyleName("ode-NavArrow");
//    switchToBlocksButton.add(new Image(RIGHT_ARROW_IMAGE_URL));
//    switchToBlocksButton.setWidth("25px");
//    switchToBlocksButton.setHeight("100%");

    // Add the Code Navigation arrow
//    switchToDesignerButton = new VerticalPanel();
//    switchToDesignerButton.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
//    switchToDesignerButton.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
//    switchToDesignerButton.setStyleName("ode-NavArrow");
//    switchToDesignerButton.add(new Image(LEFT_ARROW_IMAGE_URL));
//    switchToDesignerButton.setWidth("25px");
//    switchToDesignerButton.setHeight("100%");

    designToolbar = new DesignToolbar();
    dVertPanel.add(designToolbar);

    workColumns = new HorizontalPanel();
    workColumns.setWidth("100%");

    //workColumns.add(switchToDesignerButton);

    Box palletebox = PaletteBox.getPaletteBox();
    palletebox.setWidth("240px");
    workColumns.add(palletebox);

    Box viewerbox = ViewerBox.getViewerBox();
    workColumns.add(viewerbox);
    workColumns.setCellWidth(viewerbox, "97%");
    workColumns.setCellHeight(viewerbox, "97%");

    structureAndAssets = new VerticalPanel();
    structureAndAssets.setVerticalAlignment(VerticalPanel.ALIGN_TOP);
    // Only one of the SourceStructureBox and the BlockSelectorBox is visible
    // at any given time, according to whether we are showing the form editor
    // or the blocks editor. They share the same screen real estate.
    structureAndAssets.add(SourceStructureBox.getSourceStructureBox());
    structureAndAssets.add(BlockSelectorBox.getBlockSelectorBox());  // initially not visible
    structureAndAssets.add(AssetListBox.getAssetListBox());
    workColumns.add(structureAndAssets);

    Box propertiesbox = PropertiesBox.getPropertiesBox();
    propertiesbox.setWidth("222px");
    workColumns.add(propertiesbox);
    //switchToBlocksButton.setHeight("650px");
    //workColumns.add(switchToBlocksButton);
    dVertPanel.add(workColumns);
    designTabIndex = deckPanel.getWidgetCount();
    deckPanel.add(dVertPanel);

    // Gallery list tab
    VerticalPanel gVertPanel = new VerticalPanel();
    gVertPanel.add(createLoadingWidget(GalleryList.INITIAL_RPCS));
    galleryTabIndex = deckPanel.getWidgetCount();
    deckPanel.add(gVertPanel);

     // Gallery app tab
    VerticalPanel aVertPanel = new VerticalPanel();
    galleryAppTabIndex = deckPanel.getWidgetCount();
    deckPanel.add(aVertPanel);

    // User Admin Panel
    VerticalPanel uaVertPanel = new VerticalPanel();
    uaVertPanel.setWidth("100%");
    uaVertPanel.setSpacing(0);
    HorizontalPanel adminUserListPanel = new HorizontalPanel();
    adminUserListPanel.setWidth("100%");
    adminUserListPanel.add(AdminUserListBox.getAdminUserListBox());
    uaVertPanel.add(adminUserListPanel);
    userAdminTabIndex = deckPanel.getWidgetCount();
    deckPanel.add(uaVertPanel);

    // KM: DEBUGGING BEGIN
    // User profile tab
    VerticalPanel uVertPanel = new VerticalPanel();
    uVertPanel.setWidth("100%");
    uVertPanel.setSpacing(0);
    HorizontalPanel userProfilePanel = new HorizontalPanel();
    userProfilePanel.setWidth("100%");
    userProfilePanel.add(ProfileBox.getUserProfileBox());

    uVertPanel.add(userProfilePanel);
    userProfileTabIndex = deckPanel.getWidgetCount();
    deckPanel.add(uVertPanel);
    // KM: DEBUGGING END

    // Private User Profile TabPanel
    VerticalPanel ppVertPanel = new VerticalPanel();
    ppVertPanel.setWidth("100%");
    ppVertPanel.setSpacing(0);
    HorizontalPanel privateUserProfileTabPanel = new HorizontalPanel();
    privateUserProfileTabPanel.setWidth("100%");
    privateUserProfileTabPanel.add(PrivateUserProfileTabPanel.getPrivateUserProfileTabPanel());
    ppVertPanel.add(privateUserProfileTabPanel);
    privateUserProfileIndex = deckPanel.getWidgetCount();
    deckPanel.add(ppVertPanel);

    // Moderation Page tab
    VerticalPanel mPVertPanel = new VerticalPanel();
    mPVertPanel.add(createLoadingWidget(ReportList.INITIAL_RPCS));
    moderationPageTabIndex = deckPanel.getWidgetCount();
    deckPanel.add(mPVertPanel);

    // Debugging tab
    if (AppInventorFeatures.hasDebuggingView()) {

      Button dismissButton = new Button(MESSAGES.dismissButton());
      dismissButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (currentView == DESIGNER)
            switchToDesignView();
          else
            switchToProjectsView();
        }
      });

      ColumnLayout defaultLayout = new ColumnLayout("Default");
      Column column = defaultLayout.addColumn(100);
      column.add(MessagesOutputBox.class, 300, false);
      column.add(OdeLogBox.class, 300, false);
      final WorkAreaPanel debuggingTab = new WorkAreaPanel(new OdeBoxRegistry(), defaultLayout);

      debuggingTab.add(dismissButton);

      debuggingTabIndex = deckPanel.getWidgetCount();
      deckPanel.add(debuggingTab);

      // Hook the window resize event, so that we can adjust the UI.
      Window.addResizeHandler(new ResizeHandler() {
        @Override
        public void onResize(ResizeEvent event) {
          resizeWorkArea(debuggingTab);
        }
      });

      // Call the window resized handler to get the initial sizes setup. Doing this in a deferred
      // command causes it to occur after all widgets' sizes have been computed by the browser.
      DeferredCommand.addCommand(new Command() {
        @Override
        public void execute() {
          resizeWorkArea(debuggingTab);
        }
      });

      resizeWorkArea(debuggingTab);
    }

    // We do not select the designer tab here because at this point there is no current project.
    // Instead, we select the projects tab. If the user has a previously opened project, we will
    // open it and switch to the designer after the user settings are loaded.
    // Remember, the user may not have any projects at all yet.
    // Or, the user may have deleted their previously opened project.
    // ***** THE DESIGNER TAB DOES NOT DISPLAY CORRECTLY IF THERE IS NO CURRENT PROJECT. *****
    deckPanel.showWidget(projectsTabIndex);

    overDeckPanel = new HorizontalPanel();
    overDeckPanel.setHeight("100%");
    overDeckPanel.setWidth("100%");
    overDeckPanel.add(tutorialPanel);
    overDeckPanel.setCellWidth(tutorialPanel, "0%");
    overDeckPanel.setCellHeight(tutorialPanel, "100%");
    overDeckPanel.add(deckPanel);
    mainPanel.add(overDeckPanel, DockPanel.CENTER);
    mainPanel.setCellHeight(overDeckPanel, "100%");
    mainPanel.setCellWidth(overDeckPanel, "100%");

//    mainPanel.add(switchToDesignerButton, DockPanel.WEST);
//    mainPanel.add(switchToBlocksButton, DockPanel.EAST);

    //Commenting out for now to gain more space for the blocks editor
    mainPanel.add(statusPanel, DockPanel.SOUTH);
    mainPanel.setSize("100%", "100%");
    RootPanel.get().add(mainPanel);

    // Add a handler to the RootPanel to keep track of Google Chrome Pinch Zooming and
    // handle relevant bugs. Chrome maps a Pinch Zoom to a MouseWheelEvent with the
    // control key pressed.
    RootPanel.get().addDomHandler(new MouseWheelHandler() {
      @Override
      public void onMouseWheel(MouseWheelEvent event) {
        if(event.isControlKeyDown()) {
          // Trip the appropriate flag in PZAwarePositionCallback when the page
          // is Pinch Zoomed. Note that this flag does not need to be removed when
          // the browser is un-zoomed because the patched function for determining
          // absolute position works in all circumstances.
          PZAwarePositionCallback.setPinchZoomed(true);
        }
      }
    }, MouseWheelEvent.getType());

    // There is no sure-fire way of preventing people from accidentally navigating away from ODE
    // (e.g. by hitting the Backspace key). What we do need though is to make sure that people will
    // not lose any work because of this. We hook into the window closing  event to detect the
    // situation.
    Window.addWindowClosingHandler(new Window.ClosingHandler() {
      @Override
      public void onWindowClosing(Window.ClosingEvent event) {
        onClosing();
      }
    });

    setupMotd();
  }

  private void setupMotd() {
    AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {
      @Override
      public void onFailure(Throwable caught) {
        OdeLog.log(MESSAGES.getMotdFailed());
      }

      @Override
      public void onSuccess(Integer intervalSecs) {
        if (intervalSecs > 0) {
          topPanel.showMotd();
          motdFetcher = new MotdFetcher(intervalSecs);
          motdFetcher.register((ExtendedServiceProxy<?>) projectService);
          motdFetcher.register((ExtendedServiceProxy<?>) userInfoService);
        }
      }
    };

    getGetMotdService().getCheckInterval(callback);
  }

  /**
   * Returns the editor manager.
   *
   * @return  {@link EditorManager}
   */
  public EditorManager getEditorManager() {
    return editorManager;
  }

  /**
   * Returns the project manager.
   *
   * @return  {@link ProjectManager}
   */
  public ProjectManager getProjectManager() {
    return projectManager;
  }

  /**
   * Returns the project tool bar.
   *
   * @return  {@link ProjectToolbar}
   */
  public ProjectToolbar getProjectToolbar() {
    return projectToolbar;
  }

  /**
   * Returns the structureAndAssets panel.
   *
   * @return  {@link VerticalPanel}
   */
  public VerticalPanel getStructureAndAssets() {
    return structureAndAssets;
  }

  /**
   * Returns the workColumns panel.
   *
   * @return  {@link HorizontalPanel}
   */
  public HorizontalPanel getWorkColumns() {
    return workColumns;
  }

  /**
   * Returns the design tool bar.
   *
   * @return  {@link DesignToolbar}
   */
  public DesignToolbar getDesignToolbar() {
    return designToolbar;
  }

  /**
   * Returns the design tool bar.
   *
   * @return  {@link DesignToolbar}
   */
  public TopToolbar getTopToolbar() {
    return topToolbar;
  }

  /**
   * Set the location of the topToolBar. Called from
   * TopPanel(). We need a way to find it because the
   * blockly code needs to interact with the Connect-To
   * dropdown when a connection to a companion terminates.
   */

  public void setTopToolbar(TopToolbar toolbar) {
    topToolbar = toolbar;
  }

  /**
   * Get an instance of the project information web service.
   *
   * @return project web service instance
   */
  public ProjectServiceAsync getProjectService() {
    return projectService;
  }

  /**
   * Get an instance of the gallery information web service.
   *
   * @return gallery web service instance
   */
  public GalleryServiceAsync getGalleryService() {
    return galleryService;
  }


  /**
   * Get an instance of the user information web service.
   *
   * @return user information web service instance
   */
  public UserInfoServiceAsync getUserInfoService() {
    return userInfoService;
  }

  /**
   * Get an instance of the motd web service.
   *
   * @return motd web service instance
   */
  public GetMotdServiceAsync getGetMotdService() {
    return getMotdService;
  }

  /**
   * Get an instance of the Admin Info service
   *
   * @return admin info service instance
   */
  public AdminInfoServiceAsync getAdminInfoService() {
    return adminInfoService;
  }

  /**
   * Get an instance of the help web service.
   *
   * @return help service instance
   */
  public HelpServiceAsync getHelpService() {
    return helpService;
  }

  /**
   * Get an instance of the component web service.
   *
   * @return component web service instance
   */
  public ComponentServiceAsync getComponentService() {
    return componentService;
  }

  /**
   * Get an instance of the CloudDBAuth web service.
   *
   * @return CloudDBAuth web service instance
   */
  public CloudDBAuthServiceAsync getCloudDBAuthService(){
    return cloudDBAuthService;
  }

  /**
   * Set the current file editor.
   *
   * @param fileEditor  the file editor, can be null.
   */
  public void setCurrentFileEditor(FileEditor fileEditor) {
    currentFileEditor = fileEditor;
    if (currentFileEditor == null) {
      // nothing more we can do
      OdeLog.log("Setting current file editor to null");
      return;
    }
    OdeLog.log("Ode: Setting current file editor to " + currentFileEditor.getFileId());
    switchToDesignView();
    if (!windowClosing) {
      userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      changePropertyValue(SettingsConstants.GENERAL_SETTINGS_CURRENT_PROJECT_ID,
          "" + getCurrentYoungAndroidProjectId());
      userSettings.saveSettings(null);
    }
  }

  /**
   * @return  currently open FileEditor, or null if none
   */
  public FileEditor getCurrentFileEditor() {
    return currentFileEditor;
  }

  /**
   * Returns the project root node for the current project, or null if there is no current project.
   *
   * @return  project root node corresponding to current project
   */
  public ProjectRootNode getCurrentYoungAndroidProjectRootNode() {
    if (currentFileEditor != null) {
      return currentFileEditor.getProjectRootNode();
    }
    return null;
  }

  /**
   * Updates the modification date for the requested projected in the local
   * cached data structure based on the date received from the server.
   * @param date  the date to update it to
   */
  public void updateModificationDate(long projectId, long date) {
    Project project = getProjectManager().getProject(projectId);
    if (project != null) {
      project.setDateModified(date);
    }
  }

  /**
   * Returns the current project id, or 0 if there is no current project.
   *
   * @return  the current project id
   */
  public long getCurrentYoungAndroidProjectId() {
    if (currentFileEditor != null) {
      return currentFileEditor.getProjectId();
    }
    return 0;
  }

  /**
   * Returns the current source node, or null if there is no current source node.
   *
   * @return  the current source node
   */
  public YoungAndroidSourceNode getCurrentYoungAndroidSourceNode() {
    if (currentFileEditor != null) {
      FileNode fileNode = currentFileEditor.getFileNode();
      if (fileNode instanceof YoungAndroidSourceNode) {
        return (YoungAndroidSourceNode) fileNode;
      }
    }
    return null;
  }

  /**
   * Returns user account information.
   *
   * @return user account information
   */
  public User getUser() {
    return user;
  }

  /**
   * Helper method to create push buttons.
   *
   * @param img  image to shown on face of push button
   * @param tip  text to show in tooltip
   * @return  newly created push button
   */
  public static PushButton createPushButton(ImageResource img, String tip,
                                            ClickHandler handler) {
    PushButton pb = new PushButton(new Image(img));
    pb.addClickHandler(handler);
    pb.setTitle(tip);
    return pb;
  }

  /**
   * Compares two locales and determines if they are equal. We consider oldLocale value
   * of null to be equal to the empty string to handle default values.
   * @param oldLocale one locale
   * @param newLocale another locale
   * @param defaultValue the default locale
   * @return  true if the locale ISO strings are equal modulo case or if both
   *          are empty, otherwise false
   */
  @VisibleForTesting
  static boolean compareLocales(String oldLocale, String newLocale, String defaultValue) {
    if ((oldLocale == null || oldLocale.isEmpty()) && (newLocale == null || newLocale.isEmpty())) {
      return true;
    } else if (oldLocale == null || oldLocale.isEmpty()) {
      return defaultValue.equalsIgnoreCase(newLocale);
    } else {
      return oldLocale.equalsIgnoreCase(newLocale);
    }
  }

  /**
   * Check the user's locale against the currently requested locale. No locale
   * is specified in the query string, then we redirect to the user's previous
   * locale. English, the default locale, won't redirect in this scenario to
   * prevent double requests for most of our users. If locale parameter is
   * specified and the locales don't match, then we set the user's last locale
   * to the current locale.
   */
  public static boolean handleUserLocale() {
    String locale = Window.Location.getParameter("locale");
    String lastUserLocale = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).getPropertyValue(SettingsConstants.USER_LAST_LOCALE);
    if (!compareLocales(locale, lastUserLocale, "en")) {
      if (locale == null) {
        Window.Location.assign(Window.Location.createUrlBuilder().setParameter("locale", lastUserLocale).buildString());
        return false;
      } else {
        userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).changePropertyValue(SettingsConstants.USER_LAST_LOCALE, locale);
        userSettings.saveSettings(null);
      }
    }
    return true;
  }

  private void resizeWorkArea(WorkAreaPanel workArea) {
    // Subtract 16px from width to account for vertical scrollbar FF3 likes to add
    workArea.onResize(Window.getClientWidth() - 16, Window.getClientHeight());
  }

  private void onClosing() {
    // At this point, we aren't allowed to do any UI.
    windowClosing = true;

    if (motdFetcher != null) {
      motdFetcher.unregister((ExtendedServiceProxy<?>) projectService);
      motdFetcher.unregister((ExtendedServiceProxy<?>) userInfoService);
    }

    // Unregister services with RPC status popup.
    rpcStatusPopup.unregister((ExtendedServiceProxy<?>) helpService);
    rpcStatusPopup.unregister((ExtendedServiceProxy<?>) projectService);
    rpcStatusPopup.unregister((ExtendedServiceProxy<?>) userInfoService);

    // Save the user settings.
    userSettings.saveSettings(null);

    // Save all unsaved editors.
    editorManager.saveDirtyEditors(null);

    // Not sure if this will get to do its work...
    // We purposely do this after saving dirty
    // editors because saving work is more important then
    // getting this screenshot!
    screenShotMaybe(new Runnable() {
        @Override
        public void run() {
        }
      }, true);                 // Wait for i/o!!!
  }

  /**
   * Creates, visually centers, and optionally displays the dialog box
   * that informs the user how to start learning about using App Inventor
   * or create a new project.
   * @param showDialog Convenience variable to show the created DialogBox.
   * @return The created and optionally displayed Dialog box.
   */
  public DialogBox createNoProjectsDialog(boolean showDialog) {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(true, false); //DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.createNoProjectsDialogText());

    Grid mainGrid = new Grid(2, 2);
    mainGrid.getCellFormatter().setAlignment(0,
        0,
        HasHorizontalAlignment.ALIGN_CENTER,
        HasVerticalAlignment.ALIGN_MIDDLE);
    mainGrid.getCellFormatter().setAlignment(0,
        1,
        HasHorizontalAlignment.ALIGN_CENTER,
        HasVerticalAlignment.ALIGN_MIDDLE);
    mainGrid.getCellFormatter().setAlignment(1,
        1,
        HasHorizontalAlignment.ALIGN_RIGHT,
        HasVerticalAlignment.ALIGN_MIDDLE);

    Image dialogImage = new Image(Ode.getImageBundle().codiVert());

    Grid messageGrid = new Grid(2, 1);
    messageGrid.getCellFormatter().setAlignment(0,
        0,
        HasHorizontalAlignment.ALIGN_JUSTIFY,
        HasVerticalAlignment.ALIGN_MIDDLE);
    messageGrid.getCellFormatter().setAlignment(1,
        0,
        HasHorizontalAlignment.ALIGN_LEFT,
        HasVerticalAlignment.ALIGN_MIDDLE);

    Label messageChunk1 = new HTML(MESSAGES.createNoProjectsDialogMessage1());

    messageChunk1.setWidth("23em");
    Label messageChunk2 = new Label(MESSAGES.createNoprojectsDialogMessage2());

    // Add the elements to the grids and DialogBox.
    messageGrid.setWidget(0, 0, messageChunk1);
    messageGrid.setWidget(1, 0, messageChunk2);
    mainGrid.setWidget(0, 0, dialogImage);
    mainGrid.setWidget(0, 1, messageGrid);

    dialogBox.setWidget(mainGrid);
    dialogBox.center();

    if (showDialog) {
      dialogBox.show();
    }

    return dialogBox;
  }

  /**
   * public entry for (re)displaying the welcome dialog box.
   * Bypass the "Do Not Show Again" feature. This is used by the
   * menu choice to explicitly show the dialog box. This lets
   * people who have dismissed the dialog to manually decide to
   * see it again.
   *
   */
  public void showWelcomeDialog() {
    createWelcomeDialog(true);
  }

  /**
   * Possibly display the MIT App Inventor "Splash Screen"
   *
   * @param force Bypass the check to see if they have dimissed this version
   */
  private void createWelcomeDialog(boolean force) {
    if (!shouldShowWelcomeDialog() && !force) {
      maybeShowNoProjectsDialog();
      return;
    }
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.createWelcomeDialogText());
    dialogBox.setHeight(splashConfig.height + "px");
    dialogBox.setWidth(splashConfig.width + "px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(splashConfig.content);
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button ok = new Button(MESSAGES.createWelcomeDialogButton());
    final CheckBox noshow = new CheckBox(MESSAGES.doNotShow());
    ok.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          if (noshow.getValue()) { // User checked the box
            userSettings.getSettings(SettingsConstants.SPLASH_SETTINGS).
              changePropertyValue(SettingsConstants.SPLASH_SETTINGS_VERSION,
                "" + splashConfig.version);
            userSettings.saveSettings(null);
          }
          maybeShowNoProjectsDialog();
        }
      });
    holder.add(ok);
    holder.add(noshow);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  /**
   * Check the number of projects for the user and show the "no projects" dialog if no projects
   * are present.
   */
  private void maybeShowNoProjectsDialog() {
    projectManager.addProjectManagerEventListener(new ProjectManagerEventAdapter() {
      @Override
      public void onProjectsLoaded() {
        if (projectManager.projectCount() == 0 && !templateLoadingFlag && !galleryIdLoadingFlag) {
          ErrorReporter.hide();  // hide the "Please choose a project" message
          createNoProjectsDialog(true);
        }
      }
    });
  }

  /*
   * Check the user's setting to get the version of the Splash
   * Screen that they have seen. If they have seen this version (or greater)
   * then return false so they do not see it again. Return true to show it
   */
  private boolean shouldShowWelcomeDialog() {
    if (splashConfig.version == 0) {   // Never show splash if version is 0
      return false;             // Check first to avoid others unnecessary calls
    }
    String value = userSettings.getSettings(SettingsConstants.SPLASH_SETTINGS).
      getPropertyValue(SettingsConstants.SPLASH_SETTINGS_VERSION);
    int uversion;
    if (value == null) {        // Nothing stored
      uversion = 0;
    } else {
      uversion = Integer.parseInt(value);
    }
    if (uversion >= splashConfig.version) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Show a Survey Splash Screen to the user if they have not previously
   * acknowledged it.
   */
  private void showSurveySplash() {
    // Create the UI elements of the DialogBox
    if (isReadOnly) {           // Bypass the survey if we are read-only
      maybeShowSplash();
      return;
    }
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.createWelcomeDialogText());
    dialogBox.setHeight("200px");
    dialogBox.setWidth("600px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(MESSAGES.showSurveySplashMessage());
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button takesurvey = new Button(MESSAGES.showSurveySplashButtonNow());
    takesurvey.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          // Update Splash Settings here
          userSettings.getSettings(SettingsConstants.SPLASH_SETTINGS).
            changePropertyValue(SettingsConstants.SPLASH_SETTINGS_SHOWSURVEY,
              "" + YaVersion.SPLASH_SURVEY);
          userSettings.saveSettings(null);
          takeSurvey();         // Open survey in a new window
          maybeShowSplash();
        }
      });
    holder.add(takesurvey);
    Button latersurvey = new Button(MESSAGES.showSurveySplashButtonLater());
    latersurvey.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          maybeShowSplash();
        }
      });
    holder.add(latersurvey);
    Button neversurvey = new Button(MESSAGES.showSurveySplashButtonNever());
    neversurvey.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          // Update Splash Settings here
          Settings settings =
            userSettings.getSettings(SettingsConstants.SPLASH_SETTINGS);
          settings.changePropertyValue(SettingsConstants.SPLASH_SETTINGS_SHOWSURVEY,
            "" + YaVersion.SPLASH_SURVEY);
          String declined = settings.getPropertyValue(SettingsConstants.SPLASH_SETTINGS_DECLINED);
          if (declined == null) declined = ""; // Shouldn't happen
          if (declined != "") declined += ",";
          declined += "" + YaVersion.SPLASH_SURVEY; // Record that we declined this survey
          settings.changePropertyValue(SettingsConstants.SPLASH_SETTINGS_DECLINED, declined);
          userSettings.saveSettings(null);
          maybeShowSplash();
        }
      });
    holder.add(neversurvey);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  private void maybeShowSplash() {
    if (AppInventorFeatures.showSplashScreen() && !isReadOnly) {
      createWelcomeDialog(false);
    } else {
      maybeShowNoProjectsDialog();
    }
  }

  private void maybeShowSplash2() {
    projectManager.addProjectManagerEventListener(new ProjectManagerEventAdapter() {
      @Override
      public void onProjectsLoaded() {
        if (projectManager.projectCount() == 0 && !templateLoadingFlag) {
          ErrorReporter.hide();  // hide the "Please choose a project" message
          showSplashScreens();
        }
      }
    });
  }

  public void requestShowSplashScreens() {
    mayNeedSplash = true;
    if (didTransitionFromProjectList) {  // do immediately
      showSplashScreens();
    }
  }

  // Display the Survey and/or Normal Splash Screens
  // (if enabled). This function is called out of SplashSettings.java
  // after the userSettings object is loaded (above) and parsed.
  private void showSplashScreens() {
    boolean showSplash = false;
    if (AppInventorFeatures.showSurveySplashScreen()) {
      int nvalue = 0;
      String value = userSettings.getSettings(SettingsConstants.SPLASH_SETTINGS).
        getPropertyValue(SettingsConstants.SPLASH_SETTINGS_SHOWSURVEY);
      if (value != null) {
        nvalue = Integer.parseInt(value);
      }
      if (nvalue < YaVersion.SPLASH_SURVEY) {
        showSurveySplash();
      } else {
        showSplash = true;
      }
    } else {
      showSplash = true;
    }
    if (showSplash) {
      maybeShowSplash();
    }
    didShowSplash = true;
  }

  /**
   * Show a Dialog Box when we receive an SC_PRECONDITION_FAILED
   * response code to any Async RPC call. This is a signal that
   * either our session has expired, or our login cookie has otherwise
   * become invalid. This is a fatal error and the user should not
   * be permitted to continue (many ignore the red error bar and keep
   * working, in vain). So now when this happens, we put up this
   * modal dialog box which cannot be dismissed. Instead it presents
   * just one option, a "Reload" button which reloads the browser.
   * This should trigger a re-authentication (or in the case of an
   * App Inventor upgrade trigging the problem, the loading of newer
   * code).
   */

  public void sessionDead() {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.invalidSessionDialogText());
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(MESSAGES.sessionDead());
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button reloadSession = new Button(MESSAGES.reloadWindow());
    reloadSession.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          reloadWindow(true);
        }
      });
    holder.add(reloadSession);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  /**
   * Show a Warning Dialog box when another login session has been
   * created. The user is then given two choices. They can either
   * close this session of App Inventor, which will close the current
   * window, or they can click "Take Over" which will reload this
   * window effectively making it the latest login and invalidating
   * all other sessions.
   *
   * We are called from OdeAsyncCallback when we detect that our
   * session has been invalidated.
   */
  public void invalidSessionDialog() {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.invalidSessionDialogText());
    dialogBox.setHeight("200px");
    dialogBox.setWidth("800px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(MESSAGES.invalidSessionDialogMessage());
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button closeSession = new Button(MESSAGES.invalidSessionDialogButtonEnd());
    closeSession.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          finalDialog();
        }
      });
    holder.add(closeSession);
    Button reloadSession = new Button(MESSAGES.invalidSessionDialogButtonCurrent());
    reloadSession.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          reloadWindow(false);
        }
      });
    holder.add(reloadSession);
    Button continueSession = new Button(MESSAGES.invalidSessionDialogButtonContinue());
    continueSession.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          bashWarningDialog();
        }
      });
    holder.add(continueSession);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  /**
   * The user has chosen to continue a session even though
   * others are still active. This risks damaging (bashing) projects.
   * So before we proceed, we provide a stern warning. If they press
   * "Continue" we set their sessionId to "force" which is recognized
   * by the backend as a sessionId that should always match. This is
   * safe because normal sessionIds are UUIDs which are always longer
   * then the word "force." I know this is a bit kludgey, but by doing
   * it this way we don't have to change the RPC interface which makes
   * releasing this code non-disruptive to people using App Inventor
   * during the release.
   *
   * If the user selects "Cancel" we take them back to the
   * invalidSessionDialog.
   */

  private void bashWarningDialog() {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.bashWarningDialogText());
    dialogBox.setHeight("200px");
    dialogBox.setWidth("800px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(MESSAGES.bashWarningDialogMessage());
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button continueSession = new Button(MESSAGES.bashWarningDialogButtonContinue());
    continueSession.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          sessionId = "force";  // OK, over-ride in place!
          // Because we ultimately got here from a failure in the save function...
          ChainableCommand cmd = new SaveAllEditorsCommand(null);
          cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_YA, getCurrentYoungAndroidProjectRootNode());
          // Will now go back to our regularly scheduled main loop
        }
      });
    holder.add(continueSession);
    Button cancelSession = new Button(MESSAGES.bashWarningDialogButtonNo());
    cancelSession.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          invalidSessionDialog();
        }
      });
    holder.add(cancelSession);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  /**
   * The "Final" Dialog box. When a user chooses to end their session
   * due to a conflicting login, we should show this dialog which is modal
   * and has no exit! My preference would have been to close the window
   * altogether, but the browsers won't let javascript code close windows
   * that it didn't open itself (like the main window). I also tried to
   * use document.write() to write replacement HTML but that caused errors
   * in Firefox and strange behavior in Chrome. So we do this...
   *
   * We are called from invalidSessionDialog() (above).
   */
  private void finalDialog() {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.finalDialogText());
    dialogBox.setHeight("100px");
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(MESSAGES.finalDialogMessage());
    message.setStyleName("DialogBox-message");
    DialogBoxContents.add(message);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  /**
   * corruptionDialog -- Put up a dialog box explaining that we detected corruption
   * while reading in a project file. There is no continuing once this happens.
   *
   */
  void corruptionDialog() {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.corruptionDialogText());
    dialogBox.setHeight("100px");
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(MESSAGES.corruptionDialogMessage());
    message.setStyleName("DialogBox-message");
    DialogBoxContents.add(message);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  public void blocksTruncatedDialog(final long projectId, final String fileId, final String content, final OdeAsyncCallback callback) {
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.blocksTruncatedDialogText());
    dialogBox.setHeight("150px");
    dialogBox.setWidth("600px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    String [] fileParts = fileId.split("/");
    String screenNameParts = fileParts[fileParts.length - 1];
    final String screenName = screenNameParts.split("\\.")[0]; // Get rid of the .bky part
    final String userEmail = user.getUserEmail();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(MESSAGES.blocksTruncatedDialogMessage().replace("%1", screenName));
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    final Button continueSession = new Button(MESSAGES.blocksTruncatedDialogButtonSave());
    continueSession.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          // call save2 again, this time with force = true so the empty workspace will be written
          getProjectService().save2(getSessionId(), projectId, fileId, true, content, callback);
        }
      });
    holder.add(continueSession);
    final Button cancelSession = new Button(MESSAGES.blocksTruncatedDialogButtonNoSave());
    final OdeAsyncCallback<Void> logReturn = new OdeAsyncCallback<Void> () {
      @Override
      public void onSuccess(Void result) {
        reloadWindow(false);
      }
    };
    cancelSession.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          // Note: We do *not* remove the dialog, this locks the UI up (our intent)
          // Wait for a few seconds for other I/O to complete
          cancelSession.setEnabled(false); // Disable button to prevent further clicking
          continueSession.setEnabled(false); // This one as well
          Timer t = new Timer() {
              int count = 5;
              @Override
              public void run() {
                if (count > 0) {
                  HTML html = (HTML) ((VerticalPanel)dialogBox.getWidget()).getWidget(0);
                  html.setHTML(MESSAGES.blocksTruncatedDialogButtonHTML().replace("%1", "" + count));
                  count -= 1;
                } else {
                  this.cancel();
                  getProjectService().log("Disappearing Blocks: ProjectId = " + projectId +
                      " fileId = " + fileId + " User = " + userEmail, logReturn);
                }
              }
            };
          t.scheduleRepeating(1000);     // Run every second
        }
      });
    holder.add(cancelSession);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  /**
   * Display a Dialog box that explains that you cannot connect a
   * device or the emulator to App Inventor until you have a project
   * selected.
   */

  private void wontConnectDialog() {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.noprojectDialogTitle());
    dialogBox.setHeight("100px");
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML("<p>" + MESSAGES.noprojectDuringConnect() + "</p>");
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button okButton = new Button("OK");
    okButton.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
        }
      });
    holder.add(okButton);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  /**
   * This dialog is showned if an account is disabled. It is
   * completely modal with no escape. The provided URL is displayed in
   * an iframe, so it can be tailored to each person whose account is
   * disabled.
   *
   * @param Url the Url to display in the dialog box.
   */

  public void disabledAccountDialog(String Url) {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.accountDisabledMessage());
    dialogBox.setHeight("700px");
    dialogBox.setWidth("700px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML("<iframe src=\"" + Url + "\" style=\"border: 0; width: 680px; height: 660px;\"></iframe>");
    message.setStyleName("DialogBox-message");
    DialogBoxContents.add(message);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
  }

  /**
   * Is it OK to connect a device/emulator. Returns true if so false
   * otherwise.
   *
   * Determination is made based on whether or not a project is
   * selected.
   *
   * @return boolean
   */

  public boolean okToConnect() {
    if (getCurrentYoungAndroidProjectId() == 0) {
      wontConnectDialog();
      return false;
    } else {
      return true;
    }
  }

  /**
   * recordCorruptProject -- Record that we received a corrupt read. This
   * may or may not work depending on the reason why we received a corrupt
   * file. If the network just went down, then obviously we won't be able
   * to use the network to report this problem. However if the corruption
   * was due to a proxy mangling data (perhaps in the name of censorship)
   * then this will likely work. We'll see.... (JIS)
   *
   */

  public void recordCorruptProject(long projectId, String fileId, String message) {
    getProjectService().recordCorruption(projectId, fileId, message,
        new OdeAsyncCallback<Void>(
          "") {                   // No failure message
          @Override
            public void onSuccess(Void result) {
            // do nothing
          }
        });
  }

  /**
   * generateNonce() -- Generate a unique String value
   * this value is used to reference a built APK without
   * requiring explicit authentication.
   *
   * @return nonce
   */
  public String generateNonce() {
    int v = random.nextInt(1000000);
    nonce = Integer.toString(v, 36); // Base 36 string
    return nonce;
  }

  public String getSessionId() {
    return sessionId;
  }

  /*
   * getNonce() -- return a previously generated nonce.
   *
   * @return nonce
   */
  public String getNonce() {
    return nonce;
  }

  public boolean isReadOnly() {
    return isReadOnly;
  }

  // This is called from AdminUserList when we are switching users
  // See the comment there...
  public void setReadOnly() {
    isReadOnly = true;
  }

  // Code to lock out certain screen and project switching code
  // These are locked out while files are being saved
  // lockScreens(true) is called from EditorManager when it
  // is about to call saveDirtyEditors() and then cleared
  // in the afterSaving command called when saveDirtyEditors
  // is finished.

  public boolean screensLocked() {
    return screensLocked;
  }

  public void lockScreens(boolean value) {
    if (value) {
      OdeLog.log("Locking Screens");
    } else {
      OdeLog.log("Unlocking Screens");
    }
    screensLocked = value;
  }

  /**
   * Take a screenshot when the user leaves a blocks editor
   *
   * Take note of the "deferred" flag. If set, we run the runnable
   * after i/o is finished. Otherwise we run it immediately while i/o
   * may still be happening. We wait in the case of logout or window
   * closing, where we want to hold things up until i/o is done.
   *
   * @param next a runnable to run when we are finished
   * @param deferred whether to run the runnable immediately or after i/o is finished
   */

  public void screenShotMaybe(final Runnable next, final boolean deferred) {
    // Only take screenshots if we are an enabled feature
    if (!AppInventorFeatures.takeScreenShots()) {
      next.run();
      return;
    }
    // If we are not in the blocks editor, we do nothing
    // but we do run our callback
    if (designToolbar.getCurrentView() != DesignToolbar.View.BLOCKS) {
      next.run();
      return;
    }
    String image = "";
    FileEditor editor = Ode.getInstance().getCurrentFileEditor();
    final long projectId = editor.getProjectId();
    final FileNode fileNode = editor.getFileNode();
    currentFileEditor.getBlocksImage(new Callback<String,String>() {
        @Override
        public void onSuccess(String result) {
          int comma = result.indexOf(",");
          if (comma < 0) {
            OdeLog.log("screenshot invalid");
            next.run();
            return;
          }
          result = result.substring(comma+1); // Strip off url header
          String screenShotName = fileNode.getName();
          int period = screenShotName.lastIndexOf(".");
          screenShotName = "screenshots/" + screenShotName.substring(0, period) + ".png";
          OdeLog.log("ScreenShotName = " + screenShotName);
          projectService.screenshot(sessionId, projectId, screenShotName, result,
            new OdeAsyncCallback<RpcResult>() {
              @Override
              public void onSuccess(RpcResult result) {
                if (deferred) {
                  next.run();
                }
              }
              public void OnFailure(Throwable caught) {
                super.onFailure(caught);
                if (deferred) {
                  next.run();
                }
              }
            });
          if (!deferred) {
            next.run();
          }
        }
        @Override
        public void onFailure(String error) {
          OdeLog.log("Screenshot failed: " + error);
          next.run();
        }
      });
  }

  private void initializeGallery() {
    VerticalPanel gVertPanel = (VerticalPanel)deckPanel.getWidget(galleryTabIndex);
    gVertPanel.setWidth("100%");
    gVertPanel.setSpacing(0);
    galleryListToolbar = new GalleryToolbar();
    gVertPanel.add(galleryListToolbar);
    HorizontalPanel appListPanel = new HorizontalPanel();
    appListPanel.setWidth("100%");
    appListPanel.add(GalleryListBox.getGalleryListBox());
    gVertPanel.add(appListPanel);

    VerticalPanel aVertPanel = (VerticalPanel)deckPanel.getWidget(galleryAppTabIndex);
    aVertPanel.setWidth("100%");
    aVertPanel.setSpacing(0);
    galleryPageToolbar = new GalleryToolbar();
    aVertPanel.add(galleryPageToolbar);
    HorizontalPanel appPanel = new HorizontalPanel();
    appPanel.setWidth("100%");
    appPanel.add(GalleryAppBox.getGalleryAppBox());
    aVertPanel.add(appPanel);

    VerticalPanel mPVertPanel = (VerticalPanel)deckPanel.getWidget(moderationPageTabIndex);
    mPVertPanel.setWidth("100%");
    mPVertPanel.setSpacing(0);
    HorizontalPanel moderationPagePanel = new HorizontalPanel();
    moderationPagePanel.setWidth("100%");
    moderationPagePanel.add(ModerationPageBox.getModerationPageBox());
    mPVertPanel.add(moderationPagePanel);

    GalleryListBox.loadGalleryList();
    if (user.isModerator()) {
      ModerationPageBox.loadModerationPage();
    }
    PrivateUserProfileTabPanel.getPrivateUserProfileTabPanel().loadProfileImage();

    galleryInitialized = true;
  }

  private Widget createLoadingWidget(final int pending) {
    final HorizontalPanel container = new HorizontalPanel();
    container.setWidth("100%");
    container.setSpacing(0);
    container.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    HorizontalPanel panel = new HorizontalPanel();
    Image image = new Image();
    image.setResource(IMAGES.waitingIcon());
    panel.add(image);
    Label label = new Label();
    label.setText(MESSAGES.defaultRpcMessage());
    panel.add(label);
    container.add(panel);
    GalleryClient.getInstance().addListener(new GalleryRequestListener() {
      volatile int count = pending;
      private void hideLoadingWidget() {
        if (container.getParent() != null) {
          container.clear();
          container.removeFromParent();
        }
      }
      @Override
      public boolean onAppListRequestCompleted(GalleryAppListResult appsResult, int requestID, boolean refreshable) {
        if ((--count) <= 0) {
          hideLoadingWidget();
          return true;
        }
        return false;
      }
      @Override
      public boolean onCommentsRequestCompleted(List<GalleryComment> comments) {
        if ((--count) <= 0) {
          hideLoadingWidget();
          return true;
        }
        return false;
      }
      @Override
      public boolean onSourceLoadCompleted(UserProject projectInfo) {
        if ((--count) <= 0) {
          hideLoadingWidget();
          return true;
        }
        return false;
      }
    });
    return container;
  }

  // Used internally here so that the tutorial panel is only shown on
  // the blocks or designer view, not the gallery or projects (or
  // other) views. unlike setTutorialVisible, we do not side effect
  // the instance variable tutorialVisible, so we can use it in showTutorials()
  // (below) to put back the tutorial frame when we revisit the project
  private void hideTutorials() {
    tutorialPanel.setVisible(false);
    overDeckPanel.setCellWidth(tutorialPanel, "0%");
  }

  private void showTutorials() {
    if (tutorialVisible) {
      tutorialPanel.setVisible(true);
    }
  }

  public void setTutorialVisible(boolean visible) {
    tutorialVisible = visible;
    if (visible) {
      tutorialPanel.setVisible(true);
      tutorialPanel.setWidth("300px");
    } else {
      tutorialPanel.setVisible(false);
      overDeckPanel.setCellWidth(tutorialPanel, "0%");
    }
  }

  /**
   * Indicate if the tutorial panel is currently visible.
   * @return true if the tutorial panel is visible.
   *
   * Note: This value is only valid if in the blocks editor or the designer.
   * As of this note this routine is called when the "Toogle Tutorial" button
   * is clicked, and it is only displayed when in the Designer of the Blocks
   * Editor.
   */

  public boolean isTutorialVisible() {
    return tutorialVisible;
  }

  public void setTutorialURL(String newURL) {
    if (newURL.isEmpty() || (!newURL.startsWith("http://appinventor.mit.edu/")
        && !newURL.startsWith("http://appinv.us/"))) {
      designToolbar.setTutorialToggleVisible(false);
      setTutorialVisible(false);
    } else {
      tutorialPanel.setUrl(newURL);
      designToolbar.setTutorialToggleVisible(true);
      setTutorialVisible(true);
    }
  }

  // Load the user's backpack. This is not called if we are using
  // a shared backpack
  private void loadBackpack() {
    userInfoService.getUserBackpack(new AsyncCallback<String>() {
        @Override
        public void onSuccess(String backpack) {
          BlocklyPanel.setInitialBackpack(backpack);
        }
        @Override
        public void onFailure(Throwable caught) {
          OdeLog.log("Fetching backpack failed");
        }
      });
  }

  public boolean hasSecondBuildserver() {
    return secondBuildserver;
  }

  public boolean getWarnBuild(boolean secondBuildserver) {
    return secondBuildserver ? warnedBuild2 : warnedBuild1;
  }

  public void setWarnBuild(boolean secondBuildserver, boolean value) {
    if (secondBuildserver) {
      warnedBuild2 = value;
    } else {
      warnedBuild1 = value;
    }
  }

  // Native code to set the top level rendezvousServer variable
  // where blockly code can easily find it.
  private native void setRendezvousServer(String server) /*-{
    top.rendezvousServer = server;
  }-*/;

  // Native code to open a new window (or tab) to display the
  // desired survey. The value below "http://web.mit.edu" is just
  // a plug value. You should insert your own as appropriate.
  private native void takeSurvey() /*-{
    $wnd.open("http://web.mit.edu");
  }-*/;

  // Making this public in case we need something like this elsewhere
  public static native String generateUuid() /*-{
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
      return v.toString(16);
     });
  }-*/;

  public static native void reloadWindow(boolean full) /*-{
    if (full) {
      top.location.replace(top.location.origin);
    } else {
      top.location.reload();
    }
  }-*/;

  private static native boolean finish(String userId) /*-{
    var delete_cookie = function(name) {
       document.cookie = name + '=;Path=/;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    };
    var retval = {
       "type": "closeApp",
       "uuid" : userId }
    if (top.opener) {
      delete_cookie("AppInventor"); // This ends our authentication
      top.opener.postMessage(retval, "*");
      return true;
    } else {
      return false;
    }
  }-*/;

  public static native void CLog(String message) /*-{
    console.log(message);
  }-*/;

}
