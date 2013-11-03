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
import com.google.appinventor.client.GalleryGuiFactory;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
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

  // UI elements
  private final FlowPanel galleryGUI;
  private final TabPanel appTabs;
  private final FlowPanel appNewest;
  private final FlowPanel appFeatured;
  private final FlowPanel appPopular;
  private final FlowPanel searchApp;
  private final FlowPanel searchResults;
  
  GalleryClient gallery = null;
  GalleryGuiFactory galleryGF = null;

  /**
   * Creates a new GalleryList
   */
  public GalleryList() {
	  gallery = new GalleryClient(this);
	  galleryGF = new GalleryGuiFactory();

    selectedApps = new ArrayList<GalleryApp>();
    
    // Initialize UI
    galleryGUI = new FlowPanel();
    galleryGUI.addStyleName("gallery");
    appTabs = new TabPanel();
    appNewest = new FlowPanel();
    appFeatured = new FlowPanel();
    appPopular = new FlowPanel();
    searchApp = new FlowPanel();
    searchResults = new FlowPanel();

    // HTML segment for gallery typeface
    HTML headerExtra = new HTML(
    		"<link href='http://fonts.googleapis.com/css?family=Roboto:400,300,100' rel='stylesheet' type='text/css'>");
    galleryGUI.add(headerExtra);

    // Add panels to main tabPanel
    appTabs.add(appNewest, "Recent");
    appTabs.add(appFeatured, "Featured");
    appTabs.add(appPopular, "Popular");
    appTabs.add(searchApp, "Search");
    addGallerySearchTab(searchApp);
    appTabs.selectTab(0);
    appTabs.addStyleName("gallery-app-tabs");
    galleryGUI.add(appTabs);
    
    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");
    panel.add(galleryGUI);
    
    initWidget(panel);
    
    // calls to gallery get methods will eventually trigger call back to methods
    // at bottom of this file
    gallery.GetMostRecent(0, 5);
    gallery.GetFeatured(0, 5, 0);
    gallery.GetMostDownloaded(0, 5);
  }


  /**
   * Creates the GUI components for search tab.
   *
   * @param searchApp: the FlowPanel that search tab will reside.
   */
  private void addGallerySearchTab(FlowPanel searchApp) {
    // Add search GUI
    FlowPanel searchPanel = new FlowPanel();
    final TextBox searchText = new TextBox();
    searchText.addStyleName("gallery-search-textarea");
    Button sb = new Button("Search for apps");
    searchPanel.add(searchText);
    searchPanel.add(sb);
    searchPanel.addStyleName("gallery-search-panel");
    searchApp.add(searchPanel);
    searchResults.addStyleName("gallery-search-results");
    searchApp.add(searchResults);
    searchApp.addStyleName("gallery-search");
    
    sb.addClickHandler(new ClickHandler() {
      //  @Override
      public void onClick(ClickEvent event) {
        gallery.FindApps(searchText.getText(), 0, 5, 0);
      }
    });    
  }


  /**
   * Loads the proper tab GUI with gallery's app data.
   *
   * @param apps: list of returned gallery apps from callback.
   * 
   * @param requestId: determines the specific type of app data.
   */
  private void refreshApps(List<GalleryApp> apps, int requestId) {
    switch (requestId) {
      case 1: galleryGF.generateHorizontalAppList(apps, 1, appFeatured, false); break;   
      case 2: galleryGF.generateHorizontalAppList(apps, 2, appNewest, false); break;    
      case 3: galleryGF.generateHorizontalAppList(apps, 3, searchResults, true); break;   
      case 5: galleryGF.generateHorizontalAppList(apps, 5, appPopular, false); break;
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
  
  public void onAppListRequestCompleted(List<GalleryApp> apps, int requestId)
  {
    if (apps != null)
      refreshApps(apps, requestId);
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
  
  

 
