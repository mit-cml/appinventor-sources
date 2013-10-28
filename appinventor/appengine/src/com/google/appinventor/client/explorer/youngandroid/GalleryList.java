// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.GalleryRequestListener;
import com.google.appinventor.client.OdeAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.google.gwt.user.client.ui.Image;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.shared.rpc.project.UserProject;

import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.user.client.Window;
/**
 * The gallery list shows apps from the gallery in a table.
 *
 * <p> The project name and date created will be shown in the table.
 *
 * @author wolberd@google.com (Dave Wolber)
 */
public class GalleryList extends Composite implements GalleryRequestListener {
  private enum SortField {
    NAME,
    DATE,
  }
  private enum SortOrder {
    ASCENDING,
    DESCENDING,
  }
  private  List<GalleryApp> apps;
  private final List<GalleryApp> selectedApps;
  private final Map<GalleryApp, GalleryAppWidget> projectWidgets;
  private SortField sortField;
  private SortOrder sortOrder;

  // UI elements
  private final FlowPanel galleryGUI;
  private final TabPanel appTabs;
  private final FlowPanel appNewest;
  private final FlowPanel appFeatured;
  private final FlowPanel appPopular;
  private final FlowPanel searchApp;
  private final Grid table;
  private final Label nameSortIndicator;
  private final Label dateSortIndicator;
  
  GalleryClient gallery = null;

  /**
   * Creates a new GalleryList
   */
  public GalleryList() {
    //apps = new ArrayList<GalleryApp>();
	gallery = new GalleryClient(this);

    selectedApps = new ArrayList<GalleryApp>();
    projectWidgets = new HashMap<GalleryApp, GalleryAppWidget>();
    sortField = SortField.NAME;
    sortOrder = SortOrder.ASCENDING;
    
    // Initialize UI
    galleryGUI = new FlowPanel();
    galleryGUI.addStyleName("gallery");
    HTML headerExtra = new HTML(
    		"<link href='http://fonts.googleapis.com/css?family=Roboto:400,300,100' rel='stylesheet' type='text/css'>");
    galleryGUI.add(headerExtra);
    
    appTabs = new TabPanel();
    appNewest = new FlowPanel();
    appNewest.addStyleName("gallery-app-collection");
    appFeatured = new FlowPanel();
    appFeatured.addStyleName("gallery-app-collection");
    Label featured = new Label("Hi you are in featured apps");
    appFeatured.add(featured);
    appPopular = new FlowPanel();
    appPopular.addStyleName("gallery-app-collection");
    Label popular = new Label("Hi you are in the popular apps");
    appPopular.add(popular);
    searchApp = new FlowPanel();
    appPopular.addStyleName("gallery-app-collection");
    
    
    // Initialize UI (Old stuff ready to delete)
    table = new Grid(1, 4); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);
    nameSortIndicator = new Label("");
    dateSortIndicator = new Label("");
//    refreshSortIndicators();
//    setHeaderRow();

    appTabs.add(appNewest, "Recent");
    appTabs.add(appFeatured, "Featured");
    appTabs.add(appPopular, "Popular");
    appTabs.add(searchApp, "Search");
    appTabs.selectTab(0);
    appTabs.addStyleName("gallery-app-tabs");
    galleryGUI.add(appTabs);
    
    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");
    panel.add(galleryGUI);
//    panel.add(table);
    
    initWidget(panel);
    
    // calls to gallery get methods will eventually trigger call back to methods
    // at bottom of this file

    //gallery.GetMostRecent(0,2);
    gallery.GetAppsByDeveloper(0,5,"David Wolber");  // this works
    gallery.GetComments("111004",0,5);  //111004 is gagnam style
    // gallery.GetMostRecent(0,7); // BEFORE THE MERGE
  }

  private class GalleryAppWidget {
    final Label nameLabel;
    final Label authorLabel;
    final Label numDownloadsLabel;
    final Label numCommentsLabel;
    final Label numViewsLabel;
    final Label numLikesLabel;
    final Image image;

    private GalleryAppWidget(final GalleryApp app) {
      nameLabel = new Label(app.getTitle());
      nameLabel.addStyleName("ode-ProjectNameLabel");

      authorLabel = new Label(app.getDeveloperName());
      numDownloadsLabel = new Label(Integer.toString(app.getDownloads()));
      numLikesLabel = new Label(Integer.toString(app.getLikes()));
      numViewsLabel = new Label(Integer.toString(app.getViews()));
      numCommentsLabel = new Label(Integer.toString(app.getComments()));
      image = new Image();
      image.setUrl(app.getImageURL());
      
      
      image.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          OdeLog.log("######## I clicked on Image, open app - ");
          Ode.getInstance().switchToGalleryAppView(app); 
        }
      });

      authorLabel.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          OdeLog.log("######## I clicked on authorLabel - ");
          gallery.loadSourceFile(app.getProjectName(),app.getSourceURL());
        }
      });
      
    }
  }

  private void refreshTable(List<GalleryApp> apps) {
	  
    for (GalleryApp app: apps) {
      projectWidgets.put(app, new GalleryAppWidget(app));
    }
    // Add apps to the container
    for (GalleryApp app : apps) {
      GalleryAppWidget gaw = projectWidgets.get(app);
//      OdeLog.log("Reaching gallery app = " + app.toString());
      
      // Add GUI components
      FlowPanel appCard = new FlowPanel();
      FlowPanel appCardContent = new FlowPanel();
      
      appCard.add(gaw.image);
      appCard.add(appCardContent);
      
      // Special processing for the app title
      HTML appTitle = new HTML("" +
    		"<div class='gallery-title'>" + gaw.nameLabel.getText() +
    		"<span class='paragraph-end-block'></span></div>");
      appCardContent.add(appTitle);
      appCardContent.add(gaw.authorLabel);

      Image numViews = new Image();
      numViews.setUrl("http://i.imgur.com/jyTeyCJ.png");
      Image numDownloads = new Image();
      numDownloads.setUrl("http://i.imgur.com/j6IPJX0.png");
      Image numLikes = new Image();
      numLikes.setUrl("http://i.imgur.com/N6Lpeo2.png");
//      Image numComments = new Image();
//      numComments.setUrl("http://i.imgur.com/GGt7H4c.png");

      appCardContent.add(numViews);
      appCardContent.add(gaw.numViewsLabel);
      appCardContent.add(numDownloads);
      appCardContent.add(gaw.numDownloadsLabel);
      appCardContent.add(numLikes);
      appCardContent.add(gaw.numLikesLabel);
//      appCardContent.add(numComments);
//      appCardContent.add(gaw.numCommentsLabel);
      
      appNewest.add(appCard);

      // Add styling
      appCard.addStyleName("gallery-card");
      gaw.image.addStyleName("gallery-card-cover");
//      gaw.nameLabel.addStyleName("gallery-title");
      gaw.authorLabel.addStyleName("gallery-subtitle");
      appCardContent.addStyleName("gallery-card-content");
      gaw.numViewsLabel.addStyleName("gallery-meta");
      gaw.numDownloadsLabel.addStyleName("gallery-meta");
      gaw.numLikesLabel.addStyleName("gallery-meta");
//      gaw.numCommentsLabel.addStyleName("gallery-meta");
    
    
    }
  }

  /**
   * Gets the number of projects
   *
   * @return the number of projects
   */
  public int getNumProjects() {
    return apps.size();
  }

  /**
   * Gets the number of selected projects
   *
   * @return the number of selected projects
   */
  public int getNumSelectedApps() {
    return selectedApps.size();
  }

  /**
   * Returns the list of selected projects
   *
   * @return the selected projects
   */
  public List<GalleryApp> getSelectedApps() {
    return selectedApps;
  }
  /*
  public void loadGalleryZip(final String projectName, String sourceURL) {
	final NewProjectCommand onSuccessCommand = new NewProjectCommand() {
       @Override
       public void execute(Project project) {
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
       }
    };

    // Callback for updating the project explorer after the project is created on the back-end
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(
		      // failure message
      MESSAGES.createProjectError()) {
      @Override
      public void onSuccess(UserProject projectInfo) {
      // Update project explorer -- i.e., display in project view
      if (projectInfo == null) {

        Window.alert("This template has no zip file. Creating a new project with name = " + projectName);
        ode.getProjectService().newProject(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
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

    ode.getProjectService().newProjectFromExternalTemplate(projectName, sourceURL,callback);
  }
  */

  public void onAppListRequestCompleted(List<GalleryApp> apps, int requestId)
  {
    if (apps != null)
      refreshTable(apps);
    else
      Window.alert("apps was null");
  }

  public void onCommentsRequestCompleted(List<GalleryComment> comments)
  {
      // Window.alert("comments returned:"+comments.size());
  }
  
  public void onSourceLoadCompleted(UserProject projectInfo) {
    final NewProjectCommand onSuccessCommand = new NewProjectCommand() {
       @Override
       public void execute(Project project) {
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
       }
    };
    // Update project explorer -- i.e., display in project view
    final Ode ode = Ode.getInstance();
    if (projectInfo == null) {
      Window.alert("Unable to create project from Gallery source"); 
    }
    else {
      Project project = ode.getProjectManager().addProject(projectInfo);
      if (onSuccessCommand != null) {
        onSuccessCommand.execute(project);
      }
    }
  }
}	  
  
  

 
