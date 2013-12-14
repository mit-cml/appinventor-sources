// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.BlockSelectorBox;
import com.google.appinventor.client.boxes.MessagesOutputBox;
import com.google.appinventor.client.boxes.OdeLogBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.boxes.PropertiesBox;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.editor.EditorManager;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.appinventor.client.explorer.commands.CommandRegistry;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeAdapter;
import com.google.appinventor.client.explorer.project.ProjectManager;
import com.google.appinventor.client.explorer.project.ProjectManagerEventAdapter;
import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
import com.google.appinventor.client.jsonp.JsonpConnection;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.user.UserSettings;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.client.widgets.boxes.ColumnLayout;
import com.google.appinventor.client.widgets.boxes.ColumnLayout.Column;
import com.google.appinventor.client.widgets.boxes.WorkAreaPanel;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.rpc.GetMotdService;
import com.google.appinventor.shared.rpc.GetMotdServiceAsync;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.help.HelpService;
import com.google.appinventor.shared.rpc.help.HelpServiceAsync;
import com.google.appinventor.shared.rpc.launch.LaunchService;
import com.google.appinventor.shared.rpc.launch.LaunchServiceAsync;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectService;
import com.google.appinventor.shared.rpc.project.ProjectServiceAsync;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.user.UserInfoService;
import com.google.appinventor.shared.rpc.user.UserInfoServiceAsync;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Main entry point for Ode. Defines the startup UI elements in
 * {@link #onModuleLoad()}.
 *
 */
public class Ode implements EntryPoint {
  // I18n messages
  public static final OdeMessages MESSAGES = GWT.create(OdeMessages.class);

  /**
   * The base URL for App Inventor documentation.
   */
  public static final String APP_INVENTOR_DOCS_URL = "";

  // Global instance of the Ode object
  private static Ode instance;

  // Application level image bundle
  private static final Images IMAGES = GWT.create(Images.class);

  // ProjectEditor registry
  private static final ProjectEditorRegistry EDITORS = new ProjectEditorRegistry();

  // Command registry
  private static final CommandRegistry COMMANDS = new CommandRegistry();

  // User settings
  private static UserSettings userSettings;

  private MotdFetcher motdFetcher;

  // User information
  private User user;

  // Collection of projects
  private ProjectManager projectManager;

  // Collection of editors
  private EditorManager editorManager;

  // Currently active file editor, could be a YaFormEditor or a YaBlocksEditor or null.
  private FileEditor currentFileEditor;

  private AssetManager assetManager;

  // Remembers the current View
  private static final int DESIGNER = 0;
  private static final int PROJECTS = 1;
  private static int currentView = DESIGNER;

  /*
   * The following fields define the general layout of the UI as seen in the following diagram:
   *
   *  +-- mainPanel --------------------------------+
   *  |+-- topPanel -------------------------------+|
   *  ||                                           ||
   *  |+-------------------------------------------+|
   *  |+-- deckPanel -------------------------------+|
   *  ||                                           ||
   *  |+-------------------------------------------+|
   *  |+-- statusPanel ----------------------------+|
   *  ||                                           ||
   *  |+-------------------------------------------+|
   *  +---------------------------------------------+
   */
  private DeckPanel deckPanel;
  private int projectsTabIndex;
  private int designTabIndex;
  private int debuggingTabIndex;
  private TopPanel topPanel;
  private StatusPanel statusPanel;
  private HorizontalPanel workColumns;
  private VerticalPanel structureAndAssets;
  private ProjectToolbar projectToolbar;
  private DesignToolbar designToolbar;
  private TopToolbar topToolbar;
  // Popup that indicates that an asynchronous request is pending. It is visible
  // initially, and will be hidden automatically after the first RPC completes.
  private static RpcStatusPopup rpcStatusPopup;

  // Web service for help information
  private final HelpServiceAsync helpService = GWT.create(HelpService.class);

  // Web service for project related information
  private final ProjectServiceAsync projectService = GWT.create(ProjectService.class);

  // Web service for user related information
  private final UserInfoServiceAsync userInfoService = GWT.create(UserInfoService.class);

  // Web service for launch related services
  private final LaunchServiceAsync launchService = GWT.create(LaunchService.class);

  // Web service for get motd information
  private final GetMotdServiceAsync getMotdService = GWT.create(GetMotdService.class);

  private boolean windowClosing;

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
   * Switch to the Projects tab
   */
  public void switchToProjectsView() {
    currentView = PROJECTS;
    getTopToolbar().updateFileMenuButtons(currentView);
    deckPanel.showWidget(projectsTabIndex);
  }

  /**
   * Switch to the Designer tab. Shows an error message if there is no currentFileEditor.
   */
  public void switchToDesignView() {
    // Only show designer if there is a current editor.
    // ***** THE DESIGNER TAB DOES NOT DISPLAY CORRECTLY IF THERE IS NO CURRENT EDITOR. *****
    currentView = DESIGNER;
    getTopToolbar().updateFileMenuButtons(currentView);
    if (currentFileEditor != null) {
      deckPanel.showWidget(designTabIndex);
    } else {
      OdeLog.wlog("No current file editor to show in designer");
      ErrorReporter.reportInfo(MESSAGES.chooseProject());
    }
  }

  /**
   * Switch to the Debugging tab
   */
  public void switchToDebuggingView() {
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
    String value = userSettings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
    getPropertyValue(SettingsConstants.GENERAL_SETTINGS_CURRENT_PROJECT_ID);
    openProject(value);
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

    // Define bridge methods to Javascript
    JsonpConnection.defineBridgeMethod();

    // Initialize global Ode instance
    instance = this;

    // Get user information.
    OdeAsyncCallback<User> callback = new OdeAsyncCallback<User>(
        // failure message
        MESSAGES.serverUnavailable()) {

      @Override
      public void onSuccess(User result) {
        // If user hasn't accepted terms of service, ask them to.
        if (!result.getUserTosAccepted()) {
          // We expect that the redirect to the TOS page should be handled
          // by the onFailure method below. The server should return a
          // "forbidden" error if the TOS wasn't accepted.
          ErrorReporter.reportError(MESSAGES.serverUnavailable());
          return;
        }
        user = result;
        userSettings = new UserSettings(user);
        // Here we call userSettings.loadSettings, but the settings are actually loaded
        // asynchronously, so this loadSettings call will return before they are loaded.
        // After the user settings have been loaded, openPreviousProject will be called.
        userSettings.loadSettings();

        // Initialize project and editor managers
        projectManager = new ProjectManager();
        editorManager = new EditorManager();

        // Initialize UI
        initializeUi();

        topPanel.showUserEmail(user.getUserEmail());
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
              Window.open("/" + ServerLayout.YA_TOS_FORM, "_self", null);
              return;
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
    userInfoService.getUserInformation(callback);

    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        openProject(event.getValue());
      }
    });

    // load project based on current url
    // TODO(sharon): Seems like a possible race condition here if the onValueChange
    // handler defined above gets called before the getUserInformation call sets
    // userSettings.
    // The following line causes problems with GWT debugging, and commenting
    // it out doesn't seem to break things.
    //History.fireCurrentHistoryState();
  }

  /*
   * Initializes all UI elements.
   */
  private void initializeUi() {
    BlocklyPanel.initUi();

    rpcStatusPopup = new RpcStatusPopup();

    // Register services with RPC status popup
    rpcStatusPopup.register((ExtendedServiceProxy<?>) helpService);
    rpcStatusPopup.register((ExtendedServiceProxy<?>) projectService);
    rpcStatusPopup.register((ExtendedServiceProxy<?>) userInfoService);

    Window.setTitle(MESSAGES.titleYoungAndroid());
    Window.enableScrolling(true);

    topPanel = new TopPanel();
    statusPanel = new StatusPanel();

    DockPanel mainPanel = new DockPanel();
    mainPanel.add(topPanel, DockPanel.NORTH);

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
    VerticalPanel pVertPanel = new VerticalPanel();
    pVertPanel.setWidth("100%");
    pVertPanel.setSpacing(0);
    HorizontalPanel projectListPanel = new HorizontalPanel();
    projectListPanel.setWidth("100%");
    projectListPanel.add(ProjectListBox.getProjectListBox());
    projectToolbar = new ProjectToolbar();
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
    palletebox.setWidth("222px");
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

    mainPanel.add(deckPanel, DockPanel.CENTER);
    mainPanel.setCellHeight(deckPanel, "100%");
    mainPanel.setCellWidth(deckPanel, "100%");

//    mainPanel.add(switchToDesignerButton, DockPanel.WEST);
//    mainPanel.add(switchToBlocksButton, DockPanel.EAST);

    //Commenting out for now to gain more space for the blocks editor
    mainPanel.add(statusPanel, DockPanel.SOUTH);
    mainPanel.setSize("100%", "100%");
    RootPanel.get().add(mainPanel);

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
   * Get an instance of the help web service.
   *
   * @return help service instance
   */
  public HelpServiceAsync getHelpService() {
    return helpService;
  }

  /**
   * Get an instance of the launch RPC service.
   *
   * @return launch service instance
   */
  public LaunchServiceAsync getLaunchService() {
    return launchService;
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
    final DialogBox dialogBox = new DialogBox(true);
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText("Welcome to App Inventor 2!");

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

    Image dialogImage = new Image(Ode.getImageBundle().androidGreenSmall());

    Grid messageGrid = new Grid(2, 1);
    messageGrid.getCellFormatter().setAlignment(0,
        0,
        HasHorizontalAlignment.ALIGN_JUSTIFY,
        HasVerticalAlignment.ALIGN_MIDDLE);
    messageGrid.getCellFormatter().setAlignment(1,
        0,
        HasHorizontalAlignment.ALIGN_LEFT,
        HasVerticalAlignment.ALIGN_MIDDLE);

    Label messageChunk1 = new HTML("<p>You don't have any projects in App Inventor 2 yet. " +
      "To learn how to use App Inventor, click the \"Guide\" " +
      "link at the upper right of the window; or to start your first project, " +
      "click the \"New\" button at the upper left of the window.</p>\n<p>" +
      "<strong>Where did my projects go?</strong> " +
      "If you had projects but now they're missing, " +
      "you are probably looking for App Inventor version 1. " +
      "It's still available here: " +
      "<a href=\"http://beta.appinventor.mit.edu\" target=\"_blank\">beta.appinventor.mit.edu</a></p>\n");


    messageChunk1.setWidth("23em");
    Label messageChunk2 = new Label("Happy Inventing!");

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
   * Creates, visually centers, and optionally displays the dialog box
   * that informs the user how to start learning about using App Inventor
   * or create a new project.
   * @param showDialog Convenience variable to show the created DialogBox.
   * @return The created and optionally displayed Dialog box.
   */
  public DialogBox createWelcomeDialog(boolean showDialog) {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText("Welcome to App Inventor!");
    dialogBox.setHeight("400px");
    dialogBox.setWidth("400px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML("<h2>This is the Splash Screen. Make this an iframe to your splash screen.</h2>");
    message.setStyleName("DialogBox-message");
    SimplePanel holder = new SimplePanel();
    Button ok = new Button("Continue");
    ok.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          getProjectService().getProjects(new AsyncCallback<long[]>() {
              @Override
              public void onSuccess(long [] projectIds) {
                if (projectIds.length == 0) {
                  createNoProjectsDialog(true);
                }
              }

              @Override
              public void onFailure(Throwable projectIds) {
                OdeLog.elog("Could not get project list");
              }
            });
        }
      });
    holder.add(ok);
    DialogBoxContents.add(message);
    DialogBoxContents.add(holder);
    dialogBox.setWidget(DialogBoxContents);
    if (showDialog) {
      dialogBox.show();
    }
    return dialogBox;
  }

  /**
   * Show a Survey Splash Screen to the user if they have not previously
   * acknowledged it.
   */
  private void showSurveySplash() {
    // Create the UI elements of the DialogBox
    final DialogBox dialogBox = new DialogBox(false, true); // DialogBox(autohide, modal)
    dialogBox.setStylePrimaryName("ode-DialogBox");
    dialogBox.setText("Welcome to App Inventor!");
    dialogBox.setHeight("200px");
    dialogBox.setWidth("600px");
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();
    VerticalPanel DialogBoxContents = new VerticalPanel();
    HTML message = new HTML("<h2>Please fill out a short voluntary survey so that we can learn more about our users and improve MIT App Inventor.</h2>");
    message.setStyleName("DialogBox-message");
    FlowPanel holder = new FlowPanel();
    Button takesurvey = new Button("Take Survey Now");
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
    Button latersurvey = new Button("Take Survey Later");
    latersurvey.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          dialogBox.hide();
          maybeShowSplash();
        }
      });
    holder.add(latersurvey);
    Button neversurvey = new Button("Never Take Survey");
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
    if (AppInventorFeatures.showSplashScreen()) {
      createWelcomeDialog(true);
    } else {
      getProjectService().getProjects(new AsyncCallback<long[]>() {
          @Override
            public void onSuccess(long [] projectIds) {
            if (projectIds.length == 0) {
              createNoProjectsDialog(true);
            }
          }

          @Override
            public void onFailure(Throwable projectIds) {
            OdeLog.elog("Could not get project list");
          }
        });
    }
  }

  // Display the Survey and/or Normal Splash Screens
  // (if enabled). This function is called out of SplashSettings.java
  // after the userSettings object is loaded (above) and parsed.
  public void showSplashScreens() {
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
  }

  // Native code to open a new window (or tab) to display the
  // desired survey. The value below "http://web.mit.edu" is just
  // a plug value. You should insert your own as appropriate.
  private native void takeSurvey() /*-{
    $wnd.open("http://web.mit.edu");
  }-*/;

}
