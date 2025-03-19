// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.utils.Promise.RejectCallback;
import static com.google.appinventor.client.utils.Promise.ResolveCallback;
import static com.google.appinventor.client.utils.Promise.reject;
import static com.google.appinventor.client.utils.Promise.rejectWithReason;
import static com.google.appinventor.client.utils.Promise.resolve;
import static com.google.appinventor.client.wizards.TemplateUploadWizard.TEMPLATES_ROOT_DIRECTORY;

import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.EditorManager;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.youngandroid.ConsolePanel;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.TutorialPanel;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.editor.youngandroid.YaVisibleComponentsPanel;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.CommandRegistry;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.dialogs.NoProjectDialogBox;
import com.google.appinventor.client.explorer.folder.FolderManager;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeAdapter;
import com.google.appinventor.client.explorer.project.ProjectManager;
import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
import com.google.appinventor.client.local.LocalProjectService;
import com.google.appinventor.client.local.LocalTokenAuthService;
import com.google.appinventor.client.local.LocalUserInfoService;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.user.UserSettings;
import com.google.appinventor.client.style.neo.ImagesNeo;
import com.google.appinventor.client.style.neo.DarkModeImagesNeo;
import com.google.appinventor.client.style.neo.UiFactoryNeo;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.HTML5DragDrop;
import com.google.appinventor.client.utils.PZAwarePositionCallback;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.Urls;
import com.google.appinventor.client.widgets.ExpiredServiceOverlay;
import com.google.appinventor.client.widgets.TutorialPopup;
import com.google.appinventor.client.widgets.boxes.WorkAreaPanel;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.admin.AdminInfoService;
import com.google.appinventor.shared.rpc.admin.AdminInfoServiceAsync;
import com.google.appinventor.shared.rpc.component.ComponentService;
import com.google.appinventor.shared.rpc.component.ComponentServiceAsync;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectService;
import com.google.appinventor.shared.rpc.project.ProjectServiceAsync;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.tokenauth.TokenAuthService;
import com.google.appinventor.shared.rpc.tokenauth.TokenAuthServiceAsync;
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
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
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

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for Ode. Defines the startup UI elements in
 * {@link #onModuleLoad()}.
 *
 */
public class Ode implements EntryPoint {
  // I18n messages
  public static final OdeMessages MESSAGES = GWT.create(OdeMessages.class);

  private static final Logger LOG = Logger.getLogger(Ode.class.getName());

  // Global instance of the Ode object
  private static Ode instance;

  // Application level image bundle
  private static Images IMAGES;
  private static boolean useNeoStyle = false;
  private static boolean useDarkMode = false;

  // ProjectEditor registry
  private static final ProjectEditorRegistry EDITORS = new ProjectEditorRegistry();

  // Command registry
  private static final CommandRegistry COMMANDS = new CommandRegistry();

  // System config
  private static Config config;

  // User settings
  private static UserSettings userSettings;

  // User information
  private User user;

  // Template path if set by /?repo=
  private String templatePath;
  private boolean templateLoadingFlag = false;

  // New Gallery path if set by /?ng=
  // Set to true if we are loading from the new Gallery
  private boolean newGalleryLoadingFlag = false;
  private String newGalleryId;

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

  // Collection of folders
  private FolderManager folderManager;

  // Currently active file editor, could be a YaFormEditor or a YaBlocksEditor or null.
  private FileEditor currentFileEditor;

  private AssetManager assetManager = AssetManager.getInstance();

  private DropTargetProvider dragDropTargets;

  // Remembers the current View
  public static final int DESIGNER = 0;
  public static final int PROJECTS = 1;
  public static final int USERADMIN = 2;
  public static final int TRASHCAN = 3;
  public static int currentView = PROJECTS;

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
  @UiField(provided = true) protected DeckPanel deckPanel;
  @UiField(provided = true) protected FlowPanel overDeckPanel;
  @UiField protected TutorialPanel tutorialPanel;
  @UiField protected ConsolePanel consolePanel;
  private int projectsTabIndex;
  private int designTabIndex;
  private int debuggingTabIndex;
  private int userAdminTabIndex;
  @UiField protected TopPanel topPanel;
  @UiField protected StatusPanel statusPanel;
  @UiField protected FlowPanel workColumns;
  @UiField protected FlowPanel structureAndAssets;
  @UiField protected ProjectToolbar projectToolbar;
  @UiField (provided = true) protected ProjectListBox projectListbox;
  @UiField protected DesignToolbar designToolbar;
  @UiField (provided = true) protected PaletteBox paletteBox = PaletteBox.getPaletteBox();
  @UiField (provided = true) protected ViewerBox viewerBox = ViewerBox.getViewerBox();
  @UiField (provided = true) protected AssetListBox assetListBox = AssetListBox.getAssetListBox();
  @UiField (provided = true) protected SourceStructureBox sourceStructureBox;
  @UiField (provided = true) protected PropertiesBox propertiesBox = PropertiesBox.getPropertiesBox();

  // mode
  @UiField(provided = true) static Resources.Style style;

  // Is the tutorial toolbar currently displayed?
  private boolean tutorialVisible = false;

  private boolean consoleVisible = false;

  // Popup that indicates that an asynchronous request is pending. It is visible
  // initially, and will be hidden automatically after the first RPC completes.
  private static RpcStatusPopup rpcStatusPopup;

  // Web service for project related information
  private final ProjectServiceAsync projectService = GWT.create(ProjectService.class);

  // Web service for user related information
  private final UserInfoServiceAsync userInfoService = GWT.create(UserInfoService.class);

  // Web service for component related operations
  private final ComponentServiceAsync componentService = GWT.create(ComponentService.class);
  private final AdminInfoServiceAsync adminInfoService = GWT.create(AdminInfoService.class);

  //Web service for Token authentication operations
  private final TokenAuthServiceAsync tokenAuthService = GWT.create(TokenAuthService.class);

  private boolean windowClosing;

  private boolean screensLocked;

  // Licensing related variables
  private String licenseCode;
  private String systemId;
  private static UiStyleFactory uiFactory = null;

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

  public static Project getCurrentProject() {
    return instance.projectManager.getProject(instance.getCurrentYoungAndroidProjectId());
  }

  public static long getCurrentProjectID() {
    return instance.getCurrentYoungAndroidProjectId();
  }

  public static ProjectEditor getCurrentProjectEditor() {
    return instance.editorManager.getOpenProjectEditor(getCurrentProjectID());
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

  public DeckPanel getDeckPanel() {
    return deckPanel;
  }

  public FlowPanel getOverDeckPanel() {
    return overDeckPanel;
  }

  /**
   * Switch to the Projects tab
   */
  public void switchToProjectsView() {
    // We may need to pass the code below as a runnable to
    // screenShotMaybe() so build the runnable now
    hideChaff();
    hideTutorials();
    Runnable next = new Runnable() {
        @Override
        public void run() {
          ProjectListBox.getProjectListBox().loadProjectList();
          currentView = PROJECTS;
          getTopToolbar().updateFileMenuButtons(currentView);
          deckPanel.showWidget(projectsTabIndex);
          // If we started a project, then the start button was disabled (to avoid
          // a second press while the new project wizard was starting (aka we "debounce"
          // the button). When the person switches to the projects list view again (here)
          // we re-enable it.
          projectToolbar.enableStartButton();
          projectToolbar.setProjectTabButtonsVisible(true);
          projectToolbar.setTrashTabButtonsVisible(false);
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
   * Switch to the Trash tab
   */

  public void switchToTrash() {
    getTopToolbar().updateMoveToTrash(false);
    hideChaff();
    hideTutorials();
    currentView = TRASHCAN;
    ProjectListBox.getProjectListBox().loadTrashList();
    projectToolbar.enableStartButton();
    projectToolbar.setProjectTabButtonsVisible(false);
    projectToolbar.setTrashTabButtonsVisible(true);
    deckPanel.showWidget(projectsTabIndex);
  }

  /**
   * Switch to the User Admin Panel
   */

  public void switchToUserAdminPanel() {
    hideChaff();
    hideTutorials();
    currentView = USERADMIN;
    deckPanel.showWidget(userAdminTabIndex);
  }

  public void showComponentDesigner() {
    paletteBox.setVisible(true);
    sourceStructureBox.setVisible(true);
    propertiesBox.setVisible(true);
    if (currentFileEditor instanceof YaFormEditor) {
      YaFormEditor formEditor = (YaFormEditor) currentFileEditor;
      YaVisibleComponentsPanel panel = formEditor.getVisibleComponentsPanel();
      if (panel != null) {
        panel.showHiddenComponentsCheckbox();
      } else {
        LOG.warning("visibleComponentsPanel is null in showComponentDesigner");
      }
    }
  }

  public void hideComponentDesigner() {
    paletteBox.setVisible(false);
    sourceStructureBox.setVisible(false);
    propertiesBox.setVisible(false);
    if (currentFileEditor instanceof YaFormEditor) {
      YaFormEditor formEditor = (YaFormEditor) currentFileEditor;
      YaVisibleComponentsPanel panel = formEditor.getVisibleComponentsPanel();
      if (panel != null) {
        panel.hideHiddenComponentsCheckbox();
      } else {
        LOG.warning("visibleComponentsPanel is null in hideComponentDesigner");
      }
    }
  }

  /**
   * Switch to the Designer tab. Shows an error message if there is no currentFileEditor.
   */
  public void switchToDesignView() {
    hideChaff();
    // Only show designer if there is a current editor.
    // ***** THE DESIGNER TAB DOES NOT DISPLAY CORRECTLY IF THERE IS NO CURRENT EDITOR. *****
    showTutorials();
    currentView = DESIGNER;
    getTopToolbar().updateFileMenuButtons(currentView);
    if (currentFileEditor != null) {
      deckPanel.showWidget(designTabIndex);
    } else if (!editorManager.hasOpenEditor()) {  // is there a project editor pending visibility?
      LOG.warning("No current file editor to show in designer");
      ErrorReporter.reportInfo(MESSAGES.chooseProject());
    }
  }

  /**
   * Switch to the Debugging tab
   */
  public void switchToDebuggingView() {
    hideChaff();
    hideTutorials();
    deckPanel.showWidget(debuggingTabIndex);

    // NOTE(lizlooney) - Calling resizeWorkArea for debuggingTab prevents the
    // boxes from overlapping each other.
    resizeWorkArea((WorkAreaPanel) deckPanel.getWidget(debuggingTabIndex));
  }

  /**
   * Processes the template and galleryId flags.
   *
   * @return true if a template or gallery id is present and being handled, otherwise false.
   */
  private boolean handleQueryString() {
    if (userSettings == null) {
      return false;
    }
    // Retrieve the userTemplates
    String userTemplates = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
        .getPropertyValue(SettingsConstants.USER_TEMPLATE_URLS);
    TemplateUploadWizard.setStoredTemplateUrls(userTemplates);

    if (templateLoadingFlag) {  // We are loading a template, open it instead
      //check to see what kind of file is in url, binary (*.aia) or base64(*.apk)
      if (templatePath.endsWith(".aia")){
        HTML5DragDrop.importProjectFromUrl(templatePath);
        return true;
      }

      NewProjectCommand callbackCommand = new NewProjectCommand() {
        @Override
        public void execute(Project project) {
          templateLoadingFlag = false;
          Ode.getInstance().openYoungAndroidProjectInDesigner(project);
        }
      };
      TemplateUploadWizard.openProjectFromTemplate(templatePath, callbackCommand);
      return true;
    } else if (newGalleryLoadingFlag) {
      final DialogBox dialog = galleryLoadingDialog();
      NewProjectCommand callback = new NewProjectCommand() {
          @Override
          public void execute(Project project) {
            newGalleryLoadingFlag = false;
            dialog.hide();      // Get rid of the project loading dialog
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
          }
        };
      LoadGalleryProject.openProjectFromGallery(newGalleryId, callback);
      return true;
    }
    return false;
  }

  /**
   * Opens the user's last project, if the information is known.
   */
  private void openPreviousProject() {
    if (userSettings == null) {
      LOG.warning("Ignoring openPreviousProject() since userSettings is null");
      return;
    }
    final String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
            .getPropertyValue(SettingsConstants.GENERAL_SETTINGS_CURRENT_PROJECT_ID);
    if (value.isEmpty()) {
      switchToProjectsView();
      return;
    }
    long projectId = Long.parseLong(value);
    if (projectManager.isProjectInTrash(projectId)) {
      switchToProjectsView();
    } else {
      openProject(value);
    }
  }

  private void openProject(String projectIdString) {
    if (projectIdString.equals("")) {
      openPreviousProject();
    } else if (!projectIdString.equals("0")) {
      final long projectId = Long.parseLong(projectIdString);
      Project project = projectManager.getProject(projectId);
      if (project != null && !project.isInTrash()) {   // If last opened project is now in the trash, don't open it.
        openYoungAndroidProjectInDesigner(project);
      } else {
        // The project hasn't been added to the ProjectManager yet.
        // Add a ProjectManagerEventListener so we'll be notified when it has been added.
        // Alternatively, it is an invalid projectId. In which case,
        // nothing happens since if the listener eventually fires
        // it will not match the projectId.
        projectManager.ensureProjectsLoadedFromServer(projectService).then(projects -> {
          Project loadedProject = projectManager.getProject(projectId);
          if (loadedProject != null) {
            openYoungAndroidProjectInDesigner(loadedProject);
          } else {
            switchToProjectsView();  // the user will need to select a project...
            ErrorReporter.reportInfo(MESSAGES.chooseProject());
          }
          return null;
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
        public void onProjectLoaded(Project glass) {
          project.removeProjectChangeListener(this);
          openYoungAndroidProjectInDesigner(project);
        }
      });
      project.loadProjectNodes();
    } else {
      // The project nodes have been loaded. Tell the viewer to open
      // the project. This will cause the projects source files to be fetched
      // asynchronously, and loaded into file editors.

      viewerBox.show(projectRootNode);
      // Note: we can't call switchToDesignView until the Screen1 file editor
      // finishes loading. We leave that to setCurrentFileEditor(), which
      // will get called at the appropriate time.
      String projectIdString = Long.toString(project.getProjectId());
      if (!History.getToken().equals(projectIdString)) {
        // insert token into history but do not trigger listener event
        History.newItem(projectIdString, false);
      }
      assetManager.loadAssets(project.getProjectId());
      assetListBox.getAssetList().refreshAssetList(project.getProjectId());
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
    // Handler for any otherwise unhandled exceptions
    GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
      @Override
      public void onUncaughtException(Throwable e) {
        LOG.log(Level.SEVERE, "Unexpected exception", e);

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
      LOG.warning("Got a template or project path of " + templatePath);
      templateLoadingFlag = true;
    }
    // OK, let's see if we are loading from the new gallery Note: If
    // we are loading from a template (see above) then we ignore the
    // "ng" parameter. It doesn't make sense to have both, but if we
    // do, template loading wins.
    if (!templateLoadingFlag) {
      newGalleryId = Window.Location.getParameter("ng");
      if (newGalleryId != null) {
        LOG.warning("Got a new Gallery ID of " + newGalleryId);
        newGalleryLoadingFlag = true;
      }
    }

    // We call this below to initialize the ConnectProgressBar
    ConnectProgressBar.getInstance();

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

    setupOrigin(projectService);
    setupOrigin(userInfoService);
    setupOrigin(componentService);
    setupOrigin(adminInfoService);
    setupOrigin(tokenAuthService);

    Promise.<Config>call(MESSAGES.serverUnavailable(),
        c -> userInfoService.getSystemConfig(sessionId, c))
        .then(result -> {
          config = result;
          // Before we get too far into it, let's see if we have to do a survey!
          String surveyUrl = config.getSurveyUrl();
          if (surveyUrl != null && !surveyUrl.isEmpty()) {
            Window.Location.replace(Urls.makeUri(surveyUrl, true));
            // off we go, no returning
          }
          user = result.getUser();
          isReadOnly = user.isReadOnly();
          registerIosExtensions(config.getIosExtensions());
          return resolve(null);
        })
        .then0(this::handleGalleryId)
        .then0(this::checkTos)
        .then0(this::loadUserSettings)
        .then0(() -> Promise.allOf(
            Promise.wrap(this::processSettings),
            Promise.wrap(this::loadUserBackpack)
        ))
        .then0(this::handleUiPreference)
        .then(this::initializeUi)
        .then0(() -> projectManager.ensureProjectsLoadedFromServer(projectService))
        .then(projects -> {
          folderManager.loadFolders();
          ProjectListBox.getProjectListBox().getProjectList().onProjectsLoaded();
          return resolve(projects);
        })
        .then0(this::retrieveTemplateData)
        .then0(this::maybeOpenLastProject)
        .error(caught -> {
          if (caught == null) {
            // previous step rejected without an actual error
            return null;
          }
          Throwable original = caught.getOriginal();
          if (original instanceof StatusCodeException) {
            StatusCodeException e = (StatusCodeException) original;
            int statusCode = e.getStatusCode();
            switch (statusCode) {
              case Response.SC_UNAUTHORIZED:
                // unauthorized => not on whitelist
                // getEncodedResponse() gives us the message that we wrote in
                // OdeAuthFilter.writeWhitelistErrorMessage().
                Window.alert(e.getEncodedResponse());
                break;
              case Response.SC_FORBIDDEN:
                // forbidden => need tos accept
                Window.open(Urls.makeUri("/" + ServerLayout.YA_TOS_FORM), "_self", null);
                break;
              case Response.SC_PRECONDITION_FAILED:
                Window.Location.replace(Urls.makeUri("/login/"));
                break;           // likely not reached
              default:
                break;
            }
          }
          return null;
        });

    // load project based on current url
    // TODO(sharon): Seems like a possible race condition here if the onValueChange
    // handler defined above gets called before the getSystemConfig call sets
    // userSettings.
    // The following line causes problems with GWT debugging, and commenting
    // it out doesn't seem to break things.
    //History.fireCurrentHistoryState();
  }

  private Promise<Object> handleGalleryId() {
    // Arrange to redirect to the new gallery, which is run as a
    // separate server when we are started with a galleryId flag
    // We process this as soon as we have the system config
    // because we need the system config to tell us where the
    // gallery is located!

    String galleryId = Window.Location.getParameter("galleryId");
    if (galleryId != null) {
      // This will replace us with the gallery server, displaying the app in question
      Window.open(config.getGalleryLocation() + "?galleryid=" + galleryId, "_self", null);
      // Never get here...(?)
      return reject(null);
    }
    return resolve(null);
  }

  private Promise<Object> checkTos() {
    // If user hasn't accepted terms of service, ask them to.
    if (!user.getUserTosAccepted() && !isReadOnly) {
      // We expect that the redirect to the TOS page should be handled
      // by the onFailure method below. The server should return a
      // "forbidden" error if the TOS wasn't accepted.
      ErrorReporter.reportError(MESSAGES.serverUnavailable());
      return rejectWithReason(MESSAGES.serverUnavailable());
    }

    return resolve(null);
  }

  private Promise<UserSettings> loadUserSettings() {
    // This is called before processSettings so the work can be interleaved.
    userSettings = new UserSettings(user);
    return userSettings.loadSettings()
        .then(Ode::handleUserLocale)
        .then(result -> resolve(userSettings));
  }

  private Promise<Object> retrieveTemplateData() {
    return Promise.<String>call(MESSAGES.createProjectError(),
        c -> projectService.retrieveTemplateData(TEMPLATES_ROOT_DIRECTORY, c))
        .then(json -> {
          TemplateUploadWizard.initializeBuiltInTemplates(json);
          return resolve(null);
        });
  }

  private Promise<Object> maybeOpenLastProject() {
    if (!handleQueryString() && shouldAutoloadLastProject()) {
      openPreviousProject();
    }

    return null;
  }

  private Promise<String> loadUserBackpack() {
    String backPackId = user.getBackpackId();
    if (backPackId == null || backPackId.isEmpty()) {
      LOG.info("backpack: No shared backpack");
      return this.loadBackpack();
    } else {
      LOG.info("Have a shared backpack backPackId = " + backPackId);
      BlocklyPanel.setSharedBackpackId(backPackId);
      return resolve("");
    }
  }

  private Promise<Boolean> processSettings() {
    // load the user's backpack if we are not using a shared
    // backpack

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
      t.scheduleRepeating(1000 * 60 * noop);
    }

    splashConfig = config.getSplashConfig();
    secondBuildserver = config.getSecondBuildserver();
    // The code below is invoked if we do not have a second buildserver
    // configured. It sets the warnedBuild1 flag to true which inhibits
    // the display of the dialog box used when building. This means that
    // if no second buildserver is configured, there is no dialog box
    // displayed when the build menu items are invoked.
    if (!secondBuildserver) {
      warnedBuild1 = true;
    }

    if (config.getRendezvousServer() != null) {
      setRendezvousServer(config.getRendezvousServer(), true);
    } else {
      setRendezvousServer(YaVersion.RENDEZVOUS_SERVER, false);
    }
    return resolve(true);
  }

  private Promise<Void> handleUiPreference() {
    return new Promise<>((ResolveCallback<Void> res, RejectCallback rej) -> {
      useNeoStyle = Ode.getUserNewLayout();
      useDarkMode = Ode.getUserDarkThemeEnabled();
      if (useNeoStyle) {
        GWT.runAsync(new RunAsyncCallback() {
          @Override
          public void onFailure(Throwable reason) {
            rej.apply(new Promise.WrappedException(reason));
          }

          @Override
          public void onSuccess() {
            if (useDarkMode) {
              IMAGES = GWT.create(DarkModeImagesNeo.class);
            } else {
              IMAGES = GWT.create(ImagesNeo.class);
            }
            RootPanel.get().addStyleName("neo");
            uiFactory = new UiFactoryNeo();
            res.apply(null);
          }
        });
      } else {
        GWT.runAsync(new RunAsyncCallback() {
          @Override
          public void onFailure(Throwable reason) {
            rej.apply(new Promise.WrappedException(reason));
          }

          @Override
          public void onSuccess() {
            if (useDarkMode) {
              IMAGES = GWT.create(DarkModeImages.class);
            } else {
              IMAGES = GWT.create(Images.class);
            }
            RootPanel.get().addStyleName("classic");
            uiFactory = new UiStyleFactory();
            res.apply(null);
          }
        });
      }
    });
  }

  /*
   * Initializes all UI elements.
   */
  private Promise<Object> initializeUi(Object result) {
    EDITORS.register(YoungAndroidProjectNode.class, node -> new YaProjectEditor(node, uiFactory));
    sourceStructureBox = SourceStructureBox.getSourceStructureBox();
    folderManager = new FolderManager(uiFactory);
    projectManager = new ProjectManager();
    editorManager = new EditorManager();

    rpcStatusPopup = new RpcStatusPopup();

    // Register services with RPC status popup
    rpcStatusPopup.register(projectService);
    rpcStatusPopup.register(userInfoService);
    rpcStatusPopup.register(componentService);

    overDeckPanel = new FlowPanel("main");
    Window.setTitle(MESSAGES.titleYoungAndroid());
    Window.enableScrolling(true);

    if (config.getServerExpired()) {
      RootPanel.get().add(new ExpiredServiceOverlay());
    }
    LOG.info("Declare DeckPanel");

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
    deckPanel.sinkEvents(Event.ONCONTEXTMENU);

    // TODO: Tidy up user preference variable
    projectListbox = ProjectListBox.create(uiFactory);
    String layout;
    if (Ode.getUserNewLayout()) {
      layout = "modern";
      if (Ode.getUserDarkThemeEnabled()) {
        style = Resources.INSTANCE.stylemodernDark();
      } else {
        style = Resources.INSTANCE.stylemodernLight();
      }
    } else {
      layout = "classic";
      if (Ode.getUserDarkThemeEnabled()) {
        style = Resources.INSTANCE.styleclassicDark();
      } else {
        style = Resources.INSTANCE.styleclassicLight();
      }
    }

    style.ensureInjected();
    FlowPanel mainPanel = uiFactory.createOde(this, layout);

    deckPanel.showWidget(0);

    // Projects tab
    projectsTabIndex = 0;

    // Design tab
    designTabIndex = 1;

    // User Admin Panel
    userAdminTabIndex = 2;

    // Debugging Panel
    debuggingTabIndex = 3;

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

    if (getUserDyslexicFont()) {
      RootPanel.get().addStyleName("dyslexic");
    }

    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        openProject(event.getValue());
      }
    });

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

    HTML5DragDrop.init();
    topPanel.showUserEmail(user.getUserEmail());
    if ((mayNeedSplash || shouldShowWelcomeDialog()) && !didShowSplash) {
      showSplashScreens();
    }
    return resolve(result);
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
   * Returns the folder manager.
   *
   * @return  {@link FolderManager}
   */
  public FolderManager getFolderManager() {
    return folderManager;
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
  public FlowPanel getStructureAndAssets() {
    return structureAndAssets;
  }

  /**
   * Returns the workColumns panel.
   *
   * @return  {@link HorizontalPanel}
   */
  public FlowPanel getWorkColumns() {
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
    return topPanel.getTopToolbar();
  }

  /**
   * Returns the top panel.
   *
   * @return  {@link TopPanel}
   */
  public TopPanel getTopPanel() {
    return topPanel;
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
   * Get an instance of the user information web service.
   *
   * @return user information web service instance
   */
  public UserInfoServiceAsync getUserInfoService() {
    return userInfoService;
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
   * Get an instance of the component web service.
   *
   * @return component web service instance
   */
  public ComponentServiceAsync getComponentService() {
    return componentService;
  }

  /**
   * Get an instance of the TokenAuth web service.
   *
   * @return TokenAuth web service instance
   */
  public TokenAuthServiceAsync getTokenAuthService(){
    return tokenAuthService;
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
      LOG.info("Setting current file editor to null");
      return;
    }
    LOG.info("Ode: Setting current file editor to " + currentFileEditor.getFileId());
    if (currentFileEditor instanceof YaFormEditor) {
      sourceStructureBox.show(((YaFormEditor) currentFileEditor).getForm());
      switchToDesignView();
    }
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
   * Checks whether autoloading of the user's previous project should be
   * performed.
   *
   * @return true if autoloading should be performed, otherwise false.
   */
  public boolean shouldAutoloadLastProject() {
    String autoloadParam = Window.Location.getParameter("autoload");
    if ("false".equalsIgnoreCase(autoloadParam)) {
      return false;
    } else if ("true".equalsIgnoreCase(autoloadParam)) {
      return true;
    }
    return getUserAutoloadProject();
  }

  /**
   * HideChaff when switching view from block to others
   */
  private void hideChaff() {
    if (designToolbar.getCurrentView() == DesignToolbar.View.BLOCKS
        // currentFileEditor may be null when switching projects
        && currentFileEditor != null) {
      currentFileEditor.hideChaff();
    }
  }
  /**
   * Returns user dyslexic font setting.
   *
   * @return user default font
   */
  public static boolean getUserDyslexicFont() {
    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
            getPropertyValue(SettingsConstants.USER_DYSLEXIC_FONT);
    return Boolean.parseBoolean(value);
  }

  /**
   * Set user dyslexic font setting.
   *
   * @param dyslexicFont new value for the user default font
   */
  public static void setUserDyslexicFont(boolean dyslexicFont) {
    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
            changePropertyValue(SettingsConstants.USER_DYSLEXIC_FONT,
                    "" + dyslexicFont);
    userSettings.saveSettings(new Command() {
        @Override
        public void execute() {
          // Reload for the new font to take effect. We
          // do this here because we need to make sure that
          // the user settings were saved before we terminate
          // this browsing session. This is particularly important
          // for Firefox
          Window.Location.reload();
        }
      });
  }

  /**
   * Returns the dark theme setting.
   *
   * @return true if the user has opted to use a dark theme, false otherwise
   */
  public static boolean getUserDarkThemeEnabled() {
    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
            .getPropertyValue(SettingsConstants.DARK_THEME_ENABLED);
    if (value == null) {
      return false;
    }
    return Boolean.parseBoolean(value);
  }

  /**
   * Set user dark theme setting.
   *
   * @param enabled new value for the user's UI preference
   */
  public static void setUserDarkThemeEnabled(boolean enabled) {
    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
            .changePropertyValue(SettingsConstants.DARK_THEME_ENABLED,
                    "" + enabled);
    // userSettings.saveSettings(new Command() {
    //     @Override
    //     public void execute() {
    //       // Reload for the UI preferences to take effect. We
    //       // do this here because we need to make sure that
    //       // the user settings were saved before we terminate
    //       // this browsing session. This is particularly important
    //       // for Firefox
    //       Window.Location.reload();
    //     }
    //   });
  }

  /**
   * Returns user new layout usage setting.
   *
   * @return true if the user has opted to use the new UI, false otherwise
   */
  public static boolean getUserNewLayout() {
    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
            .getPropertyValue(SettingsConstants.USER_NEW_LAYOUT);
    return Boolean.parseBoolean(value);
    // return true;
  }

  /**
   * Set user new layout usage setting.
   *
   * @param newLayout new value for the user's UI preference
   */
  public static void setUserNewLayout(boolean newLayout) {
    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
            .changePropertyValue(SettingsConstants.USER_NEW_LAYOUT,
                    "" + newLayout);
  }

  public static void setShowUIPicker(boolean value) {
    userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
        .changePropertyValue(SettingsConstants.SHOW_UIPICKER,
            "" + value);
  }

  public static boolean getShowUIPicker() {
    return userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
            .getPropertyValue(SettingsConstants.SHOW_UIPICKER).equalsIgnoreCase("True");
  }

  public static void saveUserDesignSettings() {
    userSettings.saveSettings(new Command() {
      @Override
      public void execute() {
        // Reload for the UI preferences to take effect. We
        // do this here because we need to make sure that
        // the user settings were saved before we terminate
        // this browsing session. This is particularly important
        // for Firefox
        Window.Location.reload();
      }
    });
  }

  /**
   * Checks whether the user has autoloading enabled in their settings.
   *
   * @return true if autoloading is enabled, otherwise false.
   */
  public static boolean getUserAutoloadProject() {
    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
        .getPropertyValue(SettingsConstants.USER_AUTOLOAD_PROJECT);
    return Boolean.parseBoolean(value);
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
  public static Promise<Boolean> handleUserLocale(UserSettings userSettings) {
    String locale = Window.Location.getParameter("locale");
    String lastUserLocale = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).getPropertyValue(SettingsConstants.USER_LAST_LOCALE);
    if (!compareLocales(locale, lastUserLocale, "en")) {
      if (locale == null) {
        Window.Location.assign(Window.Location.createUrlBuilder().setParameter("locale", lastUserLocale).buildString());
        return rejectWithReason("Reloading to apply user locale");
      } else {
        userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).changePropertyValue(SettingsConstants.USER_LAST_LOCALE, locale);
        userSettings.saveSettings(null);
      }
    }
    return resolve(true);
  }

  private void resizeWorkArea(WorkAreaPanel workArea) {
    // Subtract 16px from width to account for vertical scrollbar FF3 likes to add
    workArea.onResize(Window.getClientWidth() - 16, Window.getClientHeight());
  }

  private void onClosing() {
    // At this point, we aren't allowed to do any UI.
    windowClosing = true;

    // Unregister services with RPC status popup.
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

    doCloseProxy();

  }

  /**
   * Creates, visually centers, and optionally displays the dialog box
   * that informs the user how to start learning about using App Inventor
   * or create a new project.
   * @param showDialog Convenience variable to show the created DialogBox.
   * @return The created and optionally displayed Dialog box.
   */
  public DialogBox createNoProjectsDialog(boolean showDialog) {
    final NoProjectDialogBox dialogBox = new NoProjectDialogBox();

    if (showDialog) {
      dialogBox.show();
    }

    return dialogBox;
  }

  /**
   * Creates a dialog box to show empty trash list message.
   * @param showDialog Convenience variable to show the created DialogBox.
   * @return The created and optionally displayed Dialog box.
   */

  public DialogBox createEmptyTrashDialog(boolean showDialog) {
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


    Label messageChunk2 = new Label(MESSAGES.showEmptyTrashMessage());
    messageGrid.setWidget(1, 0, messageChunk2);
    mainGrid.setWidget(0, 0, dialogImage);
    mainGrid.setWidget(0, 1, messageGrid);

    dialogBox.setWidget(mainGrid);
    dialogBox.center();

    if (showDialog) {
      dialogBox.show();
    }

    Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        if (event.getTypeInt() == Event.ONKEYDOWN && dialogBox.isShowing()) {
          dialogBox.hide();
        }
      }
    });

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
  private void createWelcomeDialog(final boolean force) {
    if (!shouldShowWelcomeDialog() && !force) {
      maybeShowNoProjectsDialog();
      return;
    }
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, false); // DialogBox(autohide, modal)
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
    ok.setFocus(true);
    dialogBox.show();

    Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      public void onPreviewNativeEvent(NativePreviewEvent event) {
        if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE && dialogBox.isShowing()) {
          dialogBox.hide();
        }
      }
    });
  }

  /**
   * Check the number of projects for the user and show the "no projects" dialog if no projects
   * are present.
   */
  private void maybeShowNoProjectsDialog() {
    projectManager.ensureProjectsLoadedFromServer(projectService).then0(() -> {
      for (Project p : projectManager.getProjects()) {
        if (!p.isInTrash()) {
          return null;  // We have at least one valid project so exit early
        }
      }
      if (!templateLoadingFlag && !newGalleryLoadingFlag) {
        ErrorReporter.hide();  // hide the "Please choose a project" message
        createNoProjectsDialog(true);
      }
      return null;
    });
    if (getShowUIPicker()) {
      TutorialPopup popup = new TutorialPopup(MESSAGES.neoWelcomeMessage(), () -> {
        setUserNewLayout(false);
        saveUserDesignSettings();
      });
      setShowUIPicker(false);
      userSettings.saveSettings(null);
      Scheduler.get().scheduleFixedDelay(() -> {
        popup.show(getTopToolbar().getSettingsDropDown().getElement());
        return false;
      }, 375);
    }
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
    projectManager.ensureProjectsLoadedFromServer(projectService).then0(() -> {
      if (ProjectListBox.getProjectListBox().getProjectList().getMyProjectsCount() == 0
          && !templateLoadingFlag && !newGalleryLoadingFlag) {
        ErrorReporter.hide();  // hide the "Please choose a project" message
        showSplashScreens();
      }
      return null;
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
   * galleryLoadingDialog -- Put up a dialog box while a Gallery
   * project is loading.
   *
   */

  private DialogBox galleryLoadingDialog() {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    // dialogBox.setText(MESSAGES.galleryLoadingDialogText());
    dialogBox.setHeight("100px");
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML(MESSAGES.galleryLoadingDialogText());
    message.setStyleName("DialogBox-message");
    DialogBoxContents.add(message);
    dialogBox.setWidget(DialogBoxContents);
    dialogBox.show();
    return dialogBox;
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
   * Display a dialog box with a provided warning message.
   *
   * @param inputMessage The message to display
   */

  public void genericWarning(String inputMessage) {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText(MESSAGES.warningDialogTitle());
    dialogBox.setHeight("100px");
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML("<p>" + inputMessage + "</p>");
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
    int v = random.nextInt(10000000);
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
      LOG.info("Locking Screens");
    } else {
      LOG.info("Unlocking Screens");
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
            LOG.info("screenshot invalid");
            next.run();
            return;
          }
          result = result.substring(comma+1); // Strip off url header
          String screenShotName = fileNode.getName();
          int period = screenShotName.lastIndexOf(".");
          screenShotName = "screenshots/" + screenShotName.substring(0, period) + ".png";
          LOG.info("ScreenShotName = " + screenShotName);
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
          LOG.info("Screenshot failed: " + error);
          next.run();
        }
      });
  }

  public static Promise<?> exportProject() {
    return ((LocalProjectService) instance.projectService).exportProject(
        instance.getCurrentYoungAndroidProjectId());
  }

  // Used internally here so that the tutorial panel is only shown on
  // the blocks or designer view, not the gallery or projects (or
  // other) views. unlike setTutorialVisible, we do not side effect
  // the instance variable tutorialVisible, so we can use it in showTutorials()
  // (below) to put back the tutorial frame when we revisit the project
  private void hideTutorials() {
    tutorialPanel.setVisible(false);
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
    }
    if (currentFileEditor != null) {
      currentFileEditor.resize();
    }
  }

  public void setConsoleVisible(boolean visible) {
    consoleVisible = visible;
    if (visible) {
      consolePanel.setVisible(true);
      consolePanel.setWidth("300px");
    } else {;
      consolePanel.setVisible(false);
    }
    if (currentFileEditor != null) {
      currentFileEditor.resize();
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

  public boolean isConsoleVisible() {
    return consoleVisible;
  }

  public void setTutorialURL(String newURL) {
    if (newURL.isEmpty()) {
      designToolbar.setTutorialToggleVisible(false);
      setTutorialVisible(false);
      return;
    }

    boolean isUrlAllowed = false;
    for (String candidate : config.getTutorialsUrlAllowed()) {
      if (newURL.startsWith(candidate)) {
        isUrlAllowed = true;
        break;
      }
    }

    if (!isUrlAllowed) {
      designToolbar.setTutorialToggleVisible(false);
      setTutorialVisible(false);
    } else {
      String locale = Window.Location.getParameter("locale");
      if (locale != null) {
        newURL += (newURL.contains("?") ? "&" : "?") + "locale=" + locale;
      }
      String[] urlSplits = newURL.split("//"); // [protocol, rest]
      boolean isHttps = Window.Location.getProtocol() == "https:" || urlSplits[0] == "https:";
      String effectiveUrl = (isHttps ? "https://" : "http://") + urlSplits[1];
      tutorialPanel.setUrl(effectiveUrl);
      designToolbar.setTutorialToggleVisible(true);
      setTutorialVisible(true);
    }
  }


  // Load the user's backpack. This is not called if we are using
  // a shared backpack
  private Promise<String> loadBackpack() {
    return Promise.call("Fetching backpack failed",
        userInfoService::getUserBackpack
    ).then(result -> {
      BlocklyPanel.setInitialBackpack(result);
      return resolve(result);
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

  public boolean getGalleryReadOnly() {
    return config.getGalleryReadOnly();
  }

  public boolean getDeleteAccountAllowed() {
    return config.getDeleteAccountAllowed();
  }

  public static void setupOrigin(Object service) {
    if (service instanceof ServiceDefTarget) {
      String host = Window.Location.getProtocol() + "//" + Window.Location.getHost();
      String oldUrl = ((ServiceDefTarget)service).getServiceEntryPoint();
      if (oldUrl.startsWith(GWT.getModuleBaseURL())) {
        String newUrl = host + "/" + GWT.getModuleName() + "/" + oldUrl.substring(GWT.getModuleBaseURL().length());
        ((ServiceDefTarget)service).setServiceEntryPoint(newUrl);
      }
    }
  }

  /**
   * setRendezvousServer
   *
   * Setup the Rendezvous server location.
   *
   * There are two places where the rendezvous servers is setup. The
   * "compiled in" version is in YaVersion.RENDEZVOUS_SERVER.  The
   * runtime version is in appengine-web.xml. If they differ, we set
   * "top.includeQRcode" to true so that when we display the
   * QRCode, we include the name of the Rendezvous server. Note: What
   * is important here is what version of the rendezvous server is
   * compiled into the Companion itself. The Companion does *not* get
   * the default version from YaVersion, but from the blocks that are
   * used to build the Companion.
   *
   * @param server the domain name of the Rendezvous servers
   * @param inclQRcode true to indicate that the rendezvous server domain name should be included in the QR Code
   *
   */
  private native void setRendezvousServer(String server, boolean inclQRcode) /*-{
    top.rendezvousServer = server;
    top.includeQRcode = inclQRcode;
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

  public static native boolean isMobile() /*-{
    var check = false;
    (function (a) {
        if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4))) check = true;
    })($wnd.navigator.userAgent || $wnd.navigator.vendor || $wnd.opera);
    return check;
  }-*/;

  private static native void doCloseProxy() /*-{
    if (top.proxy) {
      top.proxy.close();
    }
  }-*/;

  public interface Resources extends ClientBundle {

    public static final Resources INSTANCE =  GWT.create(Resources.class);
    
    @Source({
      "com/google/appinventor/client/style/classic/light.css",
      "com/google/appinventor/client/style/classic/variableColors.css"
    })
    Style styleclassicLight();

    @Source({
      "com/google/appinventor/client/style/classic/dark.css",
      "com/google/appinventor/client/style/classic/variableColors.css"
    })
    Style styleclassicDark();

    @Source({
      "com/google/appinventor/client/style/neo/lightNeo.css",
      "com/google/appinventor/client/style/neo/neo.css"
    })
    Style stylemodernLight();

    @Source({
      "com/google/appinventor/client/style/neo/darkNeo.css",
      "com/google/appinventor/client/style/neo/neo.css"
    })
    Style stylemodernDark();

    public interface Style extends CssResource {
    }
  }

  private static native void registerIosExtensions(String extensionJson)/*-{
    $wnd.ALLOWED_IOS_EXTENSIONS = JSON.parse(extensionJson);
  }-*/;

}
