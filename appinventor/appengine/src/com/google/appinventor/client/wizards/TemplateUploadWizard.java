// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.settings.user.UserSettings;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.properties.json.JSONUtil;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Wizard for importing AI2 project from a server.  A 'template' is
 * a partially built project that is designed to provide 'scaffolding' for  a
 * lesson or tutorial.  It could take various forms -- e.g., just the media that
 * makes up an app, or just the components but no blocks, or just a "library" of
 * certain blocks, etc.
 *
 * The repositories can be either 'built-in' (stored on the appengine server) or
 * 'external', stored on any other Web server -- provided it is stored in the
 *  format described below.
 *
 * Built-in template repositories are hosted in appengine/war/templates/
 * That entire directory will be copied during build to: appengine/build/war/templates/
 *
 * External templates must be placed at a reachable URL on a web server.
 *
 * If templates are stored on a static website. Your webserver must
 * implement "Cross-origin Resource Sharing (CORS). With Apache you
 * can do this by using mod_headers placing a .htaccess file in the
 * templates directory that contains the one line:
 *
 * 'Header set Access-Control-Allow-Origin "*"'.
 *
 * External repositories can be added/removed by the user at runtime.
 *
 * To add a new repository, fill in the URL to the directory that
 * contains the templates direcory, for example
 * http://appinventor.cs.trincoll.edu/csp/week1/ where templates is
 * contained in week1.
 *
 * The Wizard assumes that the templates/ repository is organized as follows and uses
 * the naming conventions shown here, where 'HelloPurr' is a typical project.  If a
 * project is stored in /templates/Project it's .json and .asc and .aia files are
 * expected to be named Project.json, Project.aia, Project.asc:
 *
 *  /templates/HelloPurr/
 *  /templates/HelloPurr/HelloPurr.json  -- JSON description of the project
 *  /templates/HelloPurr/HelloPurr.aia -- the zip archive
 *  /templates/HelloPurr/HelloPurr.asc -- a base64 encoded version of the aia file
 *  /templates/HelloPurr/screenshot.png -- optional screenshot, specified in JSON file
 *  /templates/HelloPurr/thumbnail.png -- optional thumbnail, specified in JSON file
 *  /templates/SomeOtherProject/
 *  ...
 *
 * The .json file is used to construct the description of the project in the Wizard's
 * dialog:
 *
 *  {"name": "HelloPurr", "subtitle": "A purring kitty app", "description":
      "<p>This is App Inventor's version of the HelloWorld app. ...", "screenshot":
      "screenshot.png",  "thumbnail":"thumbnail.png" }
 *
 * The images are used in the templates summary that is displayed in  dialog when
 * the use selects a repository.
 *
 * The base64 encoded file is the one that the Wizard imports.
 */

public class TemplateUploadWizard extends Wizard implements NewUrlDialogCallback {
  // Project archive extension
  private static final String PROJECT_ARCHIVE_EXTENSION = ".zip";
  private static final String PROJECT_ARCHIVE_ENCODED_EXTENSION = ".asc";
  public static final String TEMPLATES_ROOT_DIRECTORY =  "templates/";
  public static final String URL_HOST = "";  // Default uses server as host, i.e., relative addr
  public static final String EXTERNAL_JSON_FILE = "templates.json";
  public static final String MIT_TEMPLATES = "Built-in Templates";
  public static final int MIT_TEMPLATES_INDEX = 1;
  public static final int TIMEOUT = 3000;  // 3 seconds

  /**
   * Reference to the instantiated Wizard. Reset to null when dialog
   * 'Ok' or 'Cancel' buttons are clicked.
   */
  private static TemplateUploadWizard instance;

  /**
   * Needed to retrieve existing templates from user settings
   *
   */
  private static UserSettings userSettings;

  /**
   * The current template host Url.
   */
  private String templateHostUrl = "";

  public void setTemplateUrlHost(String host) {
    templateHostUrl = host;
  }

  public String getTemplateUrlHost() {
    return templateHostUrl;
  }

  /**
   * Map of dynamic (i.e., not built-in) templates.
   */
  private static Map<String, ArrayList<TemplateInfo>> templatesMap =
    new HashMap<String, ArrayList<TemplateInfo>>();

  /**
   * Json representation of a template repository consisting of
   * one or more App Inventor projects.
   * Set from ProjectService.retrieveTemplateData
   */
  private static String templateDataString;

  /**
   * Keeps track of user-created (not built-in) repositories.
   */
  private static ArrayList<String> dynamicTemplateUrls = new ArrayList<String>();

  /**
   * Sets the dynamic template Urls from a jsonStr.  This method is
   * called during start up where jsonStr is retrieved from User
   * settings.
   *
   * @param jsonStr
   */
  public static void setStoredTemplateUrls(String jsonStr) {
    if (jsonStr == null || jsonStr.length() == 0)
      return;
    JSONValue jsonVal = JSONParser.parseLenient(jsonStr);
    JSONArray jsonArr = jsonVal.isArray();
    for (int i = 0; i < jsonArr.size(); i++) {
      JSONValue value = jsonArr.get(i);
      JSONString str = value.isString();
      dynamicTemplateUrls.add(str.stringValue());
    }
  }

  /**
   * Retrieves a Json string representing the Urls of dynamic
   * template repositories.  Called when saving UserSettings
   * during shutdown or otherwise.
   *
   * @return a Json string of repository Urls
   */
  public static String getStoredTemplateUrls() {
    String[] arr = new String[dynamicTemplateUrls.size()];
    for (int k = 0; k < arr.length; k++) {
      arr[k] = dynamicTemplateUrls.get(k);
    }
    return JSONUtil.toJson(arr);
  }

  /**
   * Returns true if hostUrl is already part of the template library.
   *
   */
  public static boolean hasUrl(String hostUrl) {
   return templatesMap.get(hostUrl) != null;
  }

  /**
   * A list of built-in templates -- typically the MIT repository.
   */
  private static ArrayList<TemplateInfo> builtInTemplates;

  /**
   * Initializes the built-in template repositories.
   *
   * Called by ProjectService.retrieveTemplateData, which passes
   *  a Json string describing the template. This variable is read
   *  when the user selects 'Upload Template' from the Projects Toolbar
   * @param json takes the form of a string:
   *
   * {"name": "HelloPurr",   "subtitle": "A purring kitty app", "description":
       "<p>This is App Inventor's version of the HelloWorld app. For more information
        see the <a href='http://appinventor.mit.edu/explore/content/hellopurr.html'
        target='_blank'>HelloPurr tutorial</a>.", "screenshot": "screenshot.png",
       "thumbnail":"thumbnail.png" }
  */
  public static void initializeBuiltInTemplates(String json) {
    templateDataString = json;
    builtInTemplates = getTemplates();
    templatesMap.put(MIT_TEMPLATES, builtInTemplates);
  }

  /**
   * UI Panel holding the template list.
   */
  private HorizontalPanel templatePanel;

  /**
   * UI Listbox of template Urls.
   */
  private ListBox templatesMenu;

  /**
   * UI Button for removing a template Url.
   */
  private Button removeButton;

  /**
   * Remembers the last template library selected.
   */
  private int lastSelectedIndex = MIT_TEMPLATES_INDEX;

  /**
   * Set to true when the user has selected an external template.
   */
  private boolean usingExternalTemplate = false;

  /**
   * Set to true when the user is inputting a new Url.
   */
  private boolean newUrlTestIsPending = false;

  /**
   * Stores the Url the user has input, which is not
   * added to the list of repositories until it is validated.
   */
  private String pendingUrl = "";

  /**
   * Stores the name of the template project selected by user.
   */
  private String selectedTemplateNAME = null;

  /**
   * Returns a list of Template objects containing data needed
   *  to load a template from a zip file. Each non-null template object
   *  is created from its Json string
   *
   * @return ArrayList of TemplateInfo objects
   */
  protected static ArrayList<TemplateInfo> getTemplates() {
    JSONValue jsonVal = JSONParser.parseLenient(templateDataString);
    JSONArray jsonArr = jsonVal.isArray();
    ArrayList<TemplateInfo> templates = new ArrayList<TemplateInfo>();
    for (int i = 0; i < jsonArr.size(); i++) {
      JSONValue value = jsonArr.get(i);
      JSONObject obj = value.isObject();
      if (obj != null)
        templates.add(new TemplateInfo(obj)); // Create TemplateInfo from Json
    }
    return templates;
  }

  /**
   * Creates a new project upload wizard. This is invoked either from ProjectToolbar,
   *  when the user chooses 'Import from repo' from the toolbar menu or from the
   *  retrieveExternalTemplateData's callback method, if an external template library
   *  is selected from the drop-down menu.
   *
   */
  public TemplateUploadWizard() {
    super(MESSAGES.templateUploadWizardCaption(), true, false); // modal, adaptive sizing
    instance = this;

    // Initialize the UI
    this.setStylePrimaryName("ode-DialogBox");
    setUpUiAndFinishCommand();
  }

  /**
   * Callback from the InputTemplateUrlWizard.  Part of NewUrlDialogCallback.
   * When newUrl is received an attempt is made to retrieve template data
   * from that address.
   *
   * @param newUrl the possibly invalid Url entered by the user.
   */
  @Override
  public void updateTemplateOptions(String newUrl) {
    if (newUrl.length() != 0) {
      newUrlTestIsPending = true;
      this.pendingUrl = newUrl;
      retrieveSelectedTemplates(newUrl);
    }
  }

  /**
   * Sets up the UI and calls the initFinish() method passing it the
   *  Command that's done when 'Ok' is clicked on the Wizard dialog.
   */
  private void setUpUiAndFinishCommand() {
    this.addPage(createUI(builtInTemplates));
    populateTemplateDialog(builtInTemplates);

    initCancelCommand(new Command() {
        @Override
        public void execute() {
          instance = null;
        }
      });

    // Create finish command (upload a project archive)
    initFinishCommand(new Command() {
        @Override
        public void execute() {
          String filename = selectedTemplateNAME + PROJECT_ARCHIVE_EXTENSION;
          // Make sure the project name is legal and unique.
          if (!TextValidators.checkNewProjectName(selectedTemplateNAME)) {
            center();
            return;
          }
          NewProjectCommand callbackCommand = new NewProjectCommand() {
              @Override
              public void execute(Project project) {
                Ode.getInstance().openYoungAndroidProjectInDesigner(project);
              }
            };

          createProjectFromExistingZip(selectedTemplateNAME, callbackCommand);

          Tracking.trackEvent(Tracking.PROJECT_EVENT, Tracking.PROJECT_ACTION_NEW_YA, filename);
          instance = null;
        }
      });
  }

  /**
   * The UI consists of a vertical panel that holds a drop-down list box,
   *   a Horizontal panel that holds the templates list (cell list) plus
   *   the selected template. This is inserted in the Wizard dialog.
   *
   * @param templates should never be null
   * @return the main panel for Wizard dialog.
   */
  VerticalPanel createUI(final ArrayList<TemplateInfo> templates) {
    VerticalPanel panel = new VerticalPanel();
    panel.setStylePrimaryName("gwt-SimplePanel");
    panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

    templatePanel = new HorizontalPanel();
    templatePanel.add(makeTemplateSelector(templates));
    if (templates.size() > 0)
      templatePanel.add(new TemplateWidget(templates.get(0), templateHostUrl));

    templatesMenu = makeTemplatesMenu();

    HorizontalPanel hPanel = new HorizontalPanel();
    hPanel.add(templatesMenu);
    removeButton = new Button("Remove this repository", new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          removeCurrentlySelectedRepository();
        }
      });
    removeButton.setVisible(false);
    hPanel.add(removeButton);
    panel.add(hPanel);
    panel.add(templatePanel);
    return panel;
  }

  /**
   * Adds a new templates host to the list of available repositories.
   *
   * @param hostUrl
   * @param newTemplates
   */
  private static void addNewTemplateHost(String hostUrl, ArrayList<TemplateInfo> newTemplates) {
    templatesMap.put(hostUrl, newTemplates);

    // Display the templates dialog
    if (instance == null) {
      if (dynamicTemplateUrls.contains(hostUrl)) {
// Don't alert because we may be invoked via repo=... approach which
// can happen multiple times.
//        Window.alert("We already have that host " + hostUrl) ;
        instance = new TemplateUploadWizard();
        instance.setTemplateUrlHost(hostUrl);
        instance.populateTemplateDialog(newTemplates);
        instance.center();
        return;
      }
      instance = new TemplateUploadWizard();
      instance.setTemplateUrlHost(hostUrl);
      instance.updateTemplateOptions(hostUrl);
      instance.center();
    } else {
      instance.populateTemplateDialog(newTemplates);
    }

    // Update the user settings
    UserSettings settings = Ode.getUserSettings();
    settings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
      changePropertyValue(SettingsConstants.USER_TEMPLATE_URLS,
        TemplateUploadWizard.getStoredTemplateUrls());
    settings.saveSettings(null);
  }

  /**
   * Removes a template repository.
   */
  private void removeCurrentlySelectedRepository() {
    boolean ok = Window.confirm("Are you sure you want to remove this repository? " +
      "Click cancel to abort.");
    if (ok) {
      dynamicTemplateUrls.remove(templateHostUrl);
      templatesMap.remove(templateHostUrl);
      templatesMenu.removeItem(lastSelectedIndex);
      templatesMenu.setSelectedIndex(1);
      templatesMenu.setItemSelected(1, true);
      removeButton.setVisible(false);
      retrieveSelectedTemplates(templatesMenu.getValue(1));

      // Update the user settings
      UserSettings settings = Ode.getUserSettings();
      settings.getSettings(SettingsConstants.USER_GENERAL_SETTINGS).
        changePropertyValue(SettingsConstants.USER_TEMPLATE_URLS,
          TemplateUploadWizard.getStoredTemplateUrls());
      settings.saveSettings(null);
    }
  }

  /**
   * Creates a drop down menu for selecting Template repositories.
   * @return the drop down menu of repository Urls.
   */
  private ListBox makeTemplatesMenu() {
    final ListBox templatesMenu = new ListBox();
    templatesMenu.addItem(MESSAGES.templateUploadNewUrlCaption());
    templatesMenu.addItem(MIT_TEMPLATES);

    for (int k = 0; k < dynamicTemplateUrls.size(); k++) { // Dynamically added Urls
      templatesMenu.addItem(dynamicTemplateUrls.get(k));
    }
    templatesMenu.setSelectedIndex(MIT_TEMPLATES_INDEX);
    templatesMenu.addChangeHandler(new ChangeHandler() {
        public void onChange(ChangeEvent event) {
          int selectedIndex = templatesMenu.getSelectedIndex();

          if (selectedIndex == 0) {
            templatesMenu.setSelectedIndex(lastSelectedIndex);
            usingExternalTemplate = true; // MIT templates at index 1
            removeButton.setVisible(false);
            new InputTemplateUrlWizard(instance).center();     // This will do a callback
          } else if (selectedIndex == 1) {
            removeButton.setVisible(false);          lastSelectedIndex = selectedIndex;
            usingExternalTemplate = false; // MIT templates at index 1
            templateHostUrl = "";
            retrieveSelectedTemplates(templatesMenu.getValue(selectedIndex));  // may do callback
          } else {
            removeButton.setVisible(true);         lastSelectedIndex = selectedIndex;
            usingExternalTemplate = true; // MIT templates at index 1
            templateHostUrl = templatesMenu.getValue(selectedIndex);
            retrieveSelectedTemplates(templatesMenu.getValue(selectedIndex));  // may do callback
          }
        }
      });
    templatesMenu.setVisibleItemCount(1);  // Turns menu into a drop-down list).
    return templatesMenu;
  }

  /**
   * Retrieves the templates associated with a particular template repository.
   * Called when the user selects a template library from the drop-down box.
   * If the templates are already stored in the templates map, we just
   * refresh the Wizard's dialog. Otherwise we retrieve the templates
   * from the external hostUrl.
   *
   * @param hostUrl url of an external templates host -- e.g., 'http://localhost:85/'
   */
  void retrieveSelectedTemplates(String hostUrl) {
    ArrayList<TemplateInfo> templates = templatesMap.get(hostUrl);
    if (templates == null) {
      TemplateUploadWizard.retrieveExternalTemplateData(hostUrl);
    } else {
      populateTemplateDialog(templates);
    }
  }

  /**
   * Loads templates into the templatePanel, which consists of a
   *  clickable list widget of templates and a widget that displays
   *  a summary of the list's current selection.
   *
   * @param templates
   */
  void populateTemplateDialog(ArrayList<TemplateInfo> templates) {
    String hostUrl = "";

    // Validity check for user-entered Url.
    if (this.newUrlTestIsPending) {
      newUrlTestIsPending = false;
      hostUrl = pendingUrl;
      pendingUrl = "";
      if (templates != null) {
        dynamicTemplateUrls.add(hostUrl);
        templatesMenu.addItem(hostUrl);
        templatesMenu.setSelectedIndex(templatesMenu.getItemCount()-1);  // Last item
        lastSelectedIndex = templatesMenu.getSelectedIndex();
        usingExternalTemplate = true;
        templateHostUrl = templatesMenu.getValue(lastSelectedIndex);
      } else {
        return;
      }
    }

    if (templates == null)
      return;

    // Display the templates for the the selected Url.
    for (int k = 0; k < templatePanel.getWidgetCount(); k++) {
      templatePanel.getWidget(k).removeFromParent();
    }
    VerticalPanel parent = (VerticalPanel) templatePanel.getParent();
    templatePanel.removeFromParent();
    templatePanel = new HorizontalPanel();
    // Add the new templates
    templatePanel.add(makeTemplateSelector(templates));
    if (templates.size() > 0)
      templatePanel.add(new TemplateWidget(templates.get(0), templateHostUrl));
    parent.add(templatePanel);
  }

  /**
   * Creates a new project from a Zip file and lists it in the ProjectView.
   *
   * @param projectName project name
   * @param onSuccessCommand command to be executed after process creation
   *   succeeds (can be {@code null})
   */
  public void createProjectFromExistingZip(final String projectName,
    final NewProjectCommand onSuccessCommand) {

    // Callback for updating the project explorer after the project is created on the back-end
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(
      // failure message
      MESSAGES.createProjectError()) {
      @Override
      public void onSuccess(UserProject projectInfo) {
        // Update project explorer -- i.e., display in project view
        if (projectInfo == null) {

          Window.alert("This template has no aia file. Creating a new project with name = " + projectName);
          ode.getProjectService().newProject(
            YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
            projectName,
            new NewYoungAndroidProjectParameters(projectName),
            this);
          return;
        }
        Project project = ode.getProjectManager().addProject(projectInfo);
        if (onSuccessCommand != null) {
          onSuccessCommand.execute(project);
        }
      }
    };

    // Use project RPC service to create the project on back end using
    String pathToZip = "";
    if (usingExternalTemplate) {
      String zipUrl = templateHostUrl + TEMPLATES_ROOT_DIRECTORY + projectName + "/" +
        projectName + PROJECT_ARCHIVE_ENCODED_EXTENSION;
      RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, zipUrl);
     try {
        Request response = builder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              Window.alert("Unable to load Project Template Data");
            }
            @Override
            public void onResponseReceived(Request request, Response response) {
              ode.getProjectService().newProjectFromExternalTemplate(projectName,response.getText(),callback);
            }

          });
      } catch (RequestException e) {
        Window.alert("Error fetching project zip file template.");
      }
    } else {
      pathToZip = TEMPLATES_ROOT_DIRECTORY + projectName + "/" + projectName +
        PROJECT_ARCHIVE_EXTENSION;
      ode.getProjectService().newProjectFromTemplate(projectName, pathToZip, callback);
    }
  }

  /**
   * Called from Ode when a template Url is passed as GET parameter.
   *  The Url could take two forms:
   *  1. appinventor.cs.trincoll.edu/templates/Project/Project.asc
   *     This is a Base64 encoded AI project. In this case the project should be opened.
   *  2. appinventor.cs.trincoll.edu/templates/  or .../templates
   *     This is a repository with 0 or more templates. They should be
   *     loaded into the client and displaye in the Templates Dialog.
   * @param url the template's Url
   * @param onSuccessCommand command to open the project
   */
  public static void openProjectFromTemplate(String url, final NewProjectCommand onSuccessCommand) {
    if (url.endsWith(".asc")) {
      openTemplateProject("http://" + url, onSuccessCommand);
    } else  {
      retrieveExternalTemplateData("http://" + url);
    }
  }

  /**
   * Helper method for opening a project given its Url
   * @param url A string of the form "http://... .asc
   * @param onSuccessCommand
   */
  private static void openTemplateProject(String url, final NewProjectCommand onSuccessCommand) {
    final Ode ode = Ode.getInstance();

    // This Async callback is called after the project is input and created
    final OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(
        // failure message
        MESSAGES.createProjectError()) {
      @Override
      public void onSuccess(UserProject projectInfo) {
        // This just adds the new project to the project manager, not to AppEngine
        Project project = ode.getProjectManager().addProject(projectInfo);
        // And this opens the project
        if (onSuccessCommand != null) {
          onSuccessCommand.execute(project);
        }
      }
    };

    final String projectName;
    if (url.endsWith(".asc")) {
      projectName = url.substring(1 + url.lastIndexOf("/"), url.lastIndexOf("."));
    } else {
      return;
    }

    // If project of the same name already exists, just open it
    if (!TextValidators.checkNewProjectName(projectName)) {
      Project project = ode.getProjectManager().getProject(projectName);
      if (onSuccessCommand != null) {
        onSuccessCommand.execute(project);
      }
     return;   // Don't retrieve the template if the project is a duplicate
    }

    // Here's where we retrieve the template data
    // Do a GET to retrieve data at url
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      Request response = builder.sendRequest(null, new RequestCallback() {
        @Override
        public void onError(Request request, Throwable exception) {
          Window.alert("Unable to load Project Template Data");
        }

        // Response received from the GET
        @Override
        public void onResponseReceived(Request request, Response response) {
         // The response.getText is the zip data used to create a new project.
         // The callback opens the project
         ode.getProjectService().newProjectFromExternalTemplate(projectName,response.getText(),callback);
        }
      });
    } catch (RequestException e) {
      Window.alert("Error fetching template file.");
    }
  }

  /**
   * A class to stores template details.
   *
   */
  public static class TemplateInfo { // implements Comparable<TemplateInfo> {
    public String name;
    public String subtitle;
    public String description;
    public String thumbStr;        // thumbStr and/or screenshotStr can be ""
    public String screenshotStr;
    public ImageResource thumbnail;
    public ImageResource screenshot;

    /**
     * The key provider that provides the unique ID of a template, its name.
     */
    public static final ProvidesKey<TemplateInfo> KEY_PROVIDER = new ProvidesKey<TemplateInfo>() {
      @Override
      public Object getKey(TemplateInfo item) {
        return item == null ? null : item.name;
      }
    };

    /**
     * Default constructor
     */
    public TemplateInfo() {
    }

    public TemplateInfo(String name, String subtitle, String description, String screenshot, String thumbnail) {
      this.name = name;
      this.subtitle = subtitle;
      this.description = description;
      this.screenshotStr = screenshot;
      this.thumbStr = thumbnail;
    }

    /**
     * Builds the TemplateInfo object from JSON
     * @param value
     */
    public TemplateInfo(JSONObject value) {
      this.name = value.get("name").toString();
      this.name = this.name.substring(1, this.name.length() -1);
      this.subtitle = value.get("subtitle").toString();
      this.subtitle = this.subtitle.substring(1, this.subtitle.length() -1);
      this.description = value.get("description").toString();
      this.description = this.description.substring(1, this.description.length() -1);
      this.thumbStr = value.get("thumbnail").toString();
      this.thumbStr = this.thumbStr.substring(1, this.thumbStr.length() -1);
      this.screenshotStr = value.get("screenshot").toString();
      this.screenshotStr = this.screenshotStr.substring(1, this.screenshotStr.length() -1);
    };
  }

  /**
   * A composite widget for displaying a template.
   */
  public static class TemplateWidget extends Composite {
    private static Label title = new Label();
    private static Label subtitle = new Label();
    private static Image image = new Image();
    private static HTML descriptionHtml = new HTML();
    private VerticalPanel panel;

    public TemplateWidget(TemplateInfo info, String hostUrl) {
      setTemplate(info, hostUrl);

      panel = new VerticalPanel();
      panel.add(title);
      panel.add(subtitle);
      descriptionHtml.setHTML(info.description);
      panel.add(descriptionHtml);
      panel.add(image);
      initWidget(panel);
      setStylePrimaryName("ode-ContextMenu");
    }

    public static void setTemplate(TemplateInfo info, String hostUrl) {
      title.setText(info.name);
      subtitle.setText(info.subtitle);
      descriptionHtml.setHTML(info.description);

      if (! info.screenshotStr.equals("")) {
        String url = hostUrl + TEMPLATES_ROOT_DIRECTORY + info.name + "/" + info.screenshotStr;
        image.setUrl(url);
      } else {
        TemplateWidget.image.setResource(Ode.getImageBundle().appInventorLogo());
      }
      image.setWidth("240px");
      image.setHeight("400px");

      // Display the screenshot if available
      if (! info.screenshotStr.equals("")) {
        String url = hostUrl + TEMPLATES_ROOT_DIRECTORY + info.name + "/" + info.screenshotStr;
        image.setUrl(url);
      }
    }
  }

  /**
   * A Cell widget for displaying a summary of a template.
   *
   */
  public static class TemplateCell extends AbstractCell<TemplateInfo> {

    public TemplateInfo info;
    private String hostUrl;

    public TemplateCell(TemplateInfo info, String hostUrl) {
      this.info = info;
      this.hostUrl = hostUrl;
    }

    @Override
      public void render(Context context, TemplateInfo template, SafeHtmlBuilder sb) {
      if (template == null)
        return;
      sb.appendHtmlConstant("<table>");

      // Add the thumbnail image, if available, or a default image.
      sb.appendHtmlConstant("<tr><td rowspan='3'>");
      if ( !template.thumbStr.equals("") )   {
        String src = hostUrl + TEMPLATES_ROOT_DIRECTORY +   template.name + "/" + template.thumbStr;
        sb.appendHtmlConstant("<img style='width:32px' src='" + src + "'>");
      } else {
        ImageResource imgResource = Ode.getImageBundle().appInventorLogo();
        Image img = new Image(imgResource);
        String url = img.getUrl();
        sb.appendHtmlConstant("<img style='width:32px' src='" + url + "'>");
      }
      sb.appendHtmlConstant("</td>");

      // Add the name and description.
      sb.appendHtmlConstant("<td style='font-size:95%;'>");
      sb.appendEscaped(template.name);
      sb.appendHtmlConstant("</td></tr><tr><td>");
      sb.appendEscaped(template.subtitle);
      sb.appendHtmlConstant("</td></tr></table>");
    }
  }

  /**
   * Creates the scrollable list of cells each of which serves as a link to a template.
   *
   * @param list an ArrayList of TemplateInfo
   * @return A CellList widget
   */
  public CellList<TemplateInfo> makeTemplateSelector(ArrayList<TemplateInfo> list) {
    TemplateCell templateCell = new TemplateCell(list.get(0), templateHostUrl);

    CellList<TemplateInfo> templateCellList = new CellList<TemplateInfo>(templateCell,TemplateInfo.KEY_PROVIDER);
    templateCellList.setPageSize(list.size() + 10);
    templateCellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
    templateCellList.setWidth("250px");
    templateCellList.setHeight("400px");
    templateCellList.setVisible(true);

    // Add a selection model to handle user selection.
    final SingleSelectionModel<TemplateInfo> selectionModel =
      new SingleSelectionModel<TemplateInfo>(TemplateInfo.KEY_PROVIDER);
    templateCellList.setSelectionModel(selectionModel);
    selectionModel.setSelected(list.get(0), true);
    final TemplateUploadWizard wizard = this;
    selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
        public void onSelectionChange(SelectionChangeEvent event) {
          TemplateInfo selected = selectionModel.getSelectedObject();
          if (selected != null) {
            selectedTemplateNAME = selected.name;
            TemplateWidget.setTemplate(selected, wizard.getTemplateUrlHost());
          }
        }
      });

    // Set the total row count. This isn't strictly necessary, but it affects
    // paging calculations, so its good habit to keep the row count up to date.
    templateCellList.setRowCount(list.size(), true);

    // Push the data into the widget.
    templateCellList.setRowData(0, list);
    return templateCellList;
  }

  /**
   * Display the UploadTemplate dialog.
   */
  @Override
  public void show() {
    super.show();
    // Wizard size (having it resize between page changes is quite annoying)
    int width = 640;
    int height = 600;
    this.center();

    setPixelSize(width, height);
    super.setPagePanelHeight(580);
  }

  /**
   * Called from ProjectToolbar when user selects a set of external templates. It uses
   *  JsonP to retrieve a json file from an external server.
   *
   * @param hostUrl, Url of the host -- e.g., http://localhost:85/
   */
  public static void retrieveExternalTemplateData(final String hostUrl) {
    String url = hostUrl +  TEMPLATES_ROOT_DIRECTORY + EXTERNAL_JSON_FILE;

    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      Request response = builder.sendRequest(null, new RequestCallback() {
          @Override
          public void onError(Request request, Throwable exception) {
            Window.alert("Unable to load Project Template Data.");
            if (instance != null) {
              instance.populateTemplateDialog(null);
            }
          }
          @Override
          public void onResponseReceived(Request request, Response response) {
            if (response.getStatusCode() != Response.SC_OK) {
              Window.alert("Unable to load Project Template Data.");
              return;
            }

            ArrayList<TemplateInfo> externalTemplates = new ArrayList<TemplateInfo>();

            JSONValue jsonVal = JSONParser.parseLenient(response.getText());
            JSONArray jsonArr = jsonVal.isArray();

            for(int i = 0; i < jsonArr.size(); i++) {
              JSONValue entry1 = jsonArr.get(i);
              JSONObject entry = entry1.isObject();
              externalTemplates.add(
                new TemplateInfo(entry.get("name").isString().stringValue(),
                  entry.get("subtitle").isString().stringValue(),
                  entry.get("description").isString().stringValue(),
                  entry.get("screenshot").isString().stringValue(),
                  entry.get("thumbnail").isString().stringValue()));
            }
            if (externalTemplates.size() == 0) {
              Window.alert("Unable to retrieve templates for host = " + hostUrl + ".");
              return;
            }
            addNewTemplateHost(hostUrl, externalTemplates);
          }
        });
    } catch (RequestException e) {
      Window.alert("Error fetching external template.");
    }
  }

}
