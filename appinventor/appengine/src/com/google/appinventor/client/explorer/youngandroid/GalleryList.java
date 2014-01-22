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
//  private enum SortField {
//    NAME,
//    DATE,
//  }
//  private enum SortOrder {
//    ASCENDING,
//    DESCENDING,
//  }
  private  List<GalleryApp> apps;
  private final List<GalleryApp> selectedApps;
  GalleryClient gallery = null;
  GalleryGuiFactory galleryGF = null;
  
  // UI elements
  private final FlowPanel galleryGUI;
  private final TabPanel appTabs;
  private final FlowPanel appRecent;
  private final FlowPanel appFeatured;
  private final FlowPanel appPopular;
  private final FlowPanel appSearch;
  private final FlowPanel appRecentContent;
  private final FlowPanel appFeaturedContent;
  private final FlowPanel appPopularContent;
  private final FlowPanel appSearchContent;

  private int appRecentCounter = 0;
  private int appFeaturedCounter = 0;
  private int appPopularCounter = 0;
  private int appSearchCounter = 0;
  private boolean appRecentExhausted = false;
  private boolean appFeaturedExhausted = false;
  private boolean appPopularExhausted = false;
  private boolean appSearchExhausted = false;
  private final int offset = 5;
  private final String activeNext = "http://i.imgur.com/PC2RTC5.png";
  private final String activePrev = "http://i.imgur.com/K8aiGBZ.png";
  private final String disabledNext = "http://i.imgur.com/UYeELMN.png";
  private final String disabledPrev = "http://i.imgur.com/FGDpu47.png";

  /**
   * Creates a new GalleryList
   */
  public GalleryList() {
	  gallery = GalleryClient.getInstance();
      gallery.addListener(this);
	  galleryGF = new GalleryGuiFactory();

    selectedApps = new ArrayList<GalleryApp>();
    
    // Initialize UI
    galleryGUI = new FlowPanel();
    galleryGUI.addStyleName("gallery");
    appTabs = new TabPanel();
    appRecent = new FlowPanel();
    appFeatured = new FlowPanel();
    appPopular = new FlowPanel();
    appSearch = new FlowPanel();
    appRecentContent = new FlowPanel();
    appFeaturedContent = new FlowPanel();
    appPopularContent = new FlowPanel();
    appSearchContent = new FlowPanel();

    // HTML segment for gallery typeface
    HTML headerExtra = new HTML(
    		"<link href='http://fonts.googleapis.com/css?family=Roboto:400,300,100' rel='stylesheet' type='text/css'>");
    galleryGUI.add(headerExtra);

    // Add content to panels
    addGalleryAppTab(appFeatured, appFeaturedContent, 1);
    addGalleryAppTab(appRecent, appRecentContent, 2);
    addGalleryAppTab(appSearch, appSearchContent, 3);
    addGalleryAppTab(appPopular, appPopularContent, 5);

    // addGallerySearchTab(appSearch);  don't think we need because in regular addgallerytab below

    // Add panels to main tabPanel
    appTabs.add(appRecent, "Recent");
    appTabs.add(appFeatured, "Featured");
    appTabs.add(appPopular, "Popular");
    appTabs.add(appSearch, "Search");
    appTabs.selectTab(0);
    appTabs.addStyleName("gallery-app-tabs");
    galleryGUI.add(appTabs);
    
    // Initialize top-level GUI
    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");
    panel.add(galleryGUI);
    
    initWidget(panel);
    
    // Calls to gallery get methods will eventually trigger call back to methods
    // at bottom of this file
    gallery.GetFeatured(0, offset, 0);
    gallery.GetMostDownloaded(0, offset);
  }


  /**
   * Creates the GUI components for a regular app tab.
   * This method resides here because it needs access to global variables.
   *
   * @param container: the FlowPanel that this app tab will reside.
   *
   * @param content: the sub-panel that contains the actual app content.
   *
   * @param request: type of app request, for pagination.
   */
  private void addGalleryAppTab(FlowPanel container, FlowPanel content, final int request) {

    final TextBox searchText = new TextBox();
    // Search specific
    if (request == 3) {
      FlowPanel searchPanel = new FlowPanel();
      searchText.addStyleName("gallery-search-textarea");
      Button sb = new Button("Search for apps");
      searchPanel.add(searchText);
      searchPanel.add(sb);
      searchPanel.addStyleName("gallery-search-panel");
      container.add(searchPanel);
      appSearchContent.addStyleName("gallery-search-results");
      container.add(appSearchContent);
      container.addStyleName("gallery-search");
      sb.addClickHandler(new ClickHandler() {
        //  @Override
        public void onClick(ClickEvent event) {
          gallery.FindApps(searchText.getText(), 0, offset, 0);
        }
      }); 
    }
    
    // Add regular GUI components
    final Image buttonPrev = new Image();
//    buttonPrev.setUrl(activePrev);
    buttonPrev.setUrl(disabledPrev);
    FlowPanel prev = new FlowPanel();
    prev.add(buttonPrev);
    prev.addStyleName("gallery-nav-prev");
    container.add(prev);
    
    gallery.GetMostRecent(appRecentCounter, 5);
    container.add(content);

    final Image buttonNext = new Image();
    buttonNext.setUrl(activeNext);
    FlowPanel next = new FlowPanel();
    next.add(buttonNext);
    next.addStyleName("gallery-nav-next");
    container.add(next);
    
    final Label counter = new Label("Counting...");
    counter.addStyleName("gallery-nav-counter");
    if (request != 1) {
      container.add(counter);
      counter.setText("");      
    }
    
    buttonPrev.addClickHandler(new ClickHandler() {
      //  @Override
      public void onClick(ClickEvent event) {
        switch (request) {
        case 1: 
          if (appFeaturedCounter - offset >= 0) {
            // If the previous page still has apps to retrieve, do it
            appFeaturedCounter -= offset;
            gallery.GetFeatured(appFeaturedCounter, offset, 0);
            buttonPrev.setUrl(activePrev);
          } else {
            buttonPrev.setUrl(disabledPrev);
            OdeLog.log("prev appFeaturedCounter = " + appFeaturedCounter);
          }
          break;   
        case 2: 
          if (appRecentCounter - offset >= 0) {
            // If the previous page still has apps to retrieve, do it
            appRecentCounter -= offset;
            gallery.GetMostRecent(appRecentCounter, offset);
            buttonPrev.setUrl(activePrev);
          } else {
            buttonPrev.setUrl(disabledPrev);
            OdeLog.log("prev appRecentCounter = " + appRecentCounter);
          }
          break;  
        case 3: 
          if (appSearchCounter - offset >= 0) {
            // If the previous page still has apps to retrieve, do it
            appSearchCounter -= offset;
            gallery.FindApps(searchText.getText(), appSearchCounter, offset, 0);
            buttonPrev.setUrl(activePrev);
          } else {
            buttonPrev.setUrl(disabledPrev);
            OdeLog.log("prev appSearchCounter = " + appSearchCounter);
          }
          break;  
        case 5:
          if (appPopularCounter - offset >= 0) {
            // If the previous page still has apps to retrieve, do it
            appPopularCounter -= offset;
            gallery.GetMostDownloaded(appPopularCounter, offset);
            buttonPrev.setUrl(activePrev);
          } else {
            buttonPrev.setUrl(disabledPrev);
            OdeLog.log("prev appPopularCounter = " + appPopularCounter);
          }
          break;  
        }
      }
    });    
    buttonNext.addClickHandler(new ClickHandler() {
      //  @Override
      public void onClick(ClickEvent event) {
        switch (request) {
          case 1: 
            if (!appFeaturedExhausted) {
              // If the next page still has apps to retrieve, do it
              appFeaturedCounter += offset;
              gallery.GetFeatured(appFeaturedCounter, offset, 0);
              buttonNext.setUrl(activeNext);
            } else {
              buttonNext.setUrl(disabledNext);
              OdeLog.log("next appFeaturedCounter = " + appFeaturedCounter);
            }
            break;    
          case 2: 
            if (!appRecentExhausted) {
              // If the next page still has apps to retrieve, do it
              appRecentCounter += offset;
              gallery.GetMostRecent(appRecentCounter, offset);
              buttonNext.setUrl(activeNext);
            } else {
              buttonNext.setUrl(disabledNext);
              OdeLog.log("next appRecentCounter = " + appRecentCounter);
            }
            break;   
          case 3: 
            if (!appSearchExhausted) {
              // If the next page still has apps to retrieve, do it
              appSearchCounter += offset;
              gallery.FindApps(searchText.getText(), appSearchCounter, offset, 0);
              buttonNext.setUrl(activeNext);
            } else {
              buttonNext.setUrl(disabledNext);
              OdeLog.log("next appSearchCounter = " + appSearchCounter);
            }
            break;   
          case 5:
            if (!appPopularExhausted) {
              // If the next page still has apps to retrieve, do it
              appPopularCounter += offset;
              gallery.GetMostDownloaded(appPopularCounter, offset);
              buttonNext.setUrl(activeNext);
            } else {
              buttonNext.setUrl(disabledNext);
              OdeLog.log("next appPopularCounter = " + appPopularCounter);
            }
            break;   
        }
      }
    });    
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
    appSearchContent.addStyleName("gallery-search-results");
    searchApp.add(appSearchContent);
    searchApp.addStyleName("gallery-search");
    
    sb.addClickHandler(new ClickHandler() {
      //  @Override
      public void onClick(ClickEvent event) {
        gallery.FindApps(searchText.getText(), 0, offset, 0);
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
    OdeLog.log("#### we are now refreshing app list"+apps.size());
    switch (requestId) {
      case 1: 
        if (apps.size() < offset) {
          // That means there's not enough apps to show (reaches the end)
          appFeaturedExhausted = true;
        } else {
          appFeaturedExhausted = false;
        }
        galleryGF.generateHorizontalAppList(apps, appFeaturedContent, true); 
        break;   
      case 2: 
        if (apps.size() < offset) {
          // That means there's not enough apps to show (reaches the end)
          appRecentExhausted = true;
        } else {
          appRecentExhausted = false;
        }
        OdeLog.log("#### we are now calling generateHoriz for recent");
        galleryGF.generateHorizontalAppList(apps, appRecentContent, true); 
        break;    
      case 3: 
        if (apps.size() < offset) {
          // That means there's not enough apps to show (reaches the end)
          appSearchExhausted = true;
        } else {
          appSearchExhausted = false;
        }
        galleryGF.generateHorizontalAppList(apps, appSearchContent, true); 
        break;   
      case 5: 
        if (apps.size() < offset) {
          // That means there's not enough apps to show (reaches the end)
          appPopularExhausted = true;
        } else {
          appPopularExhausted = false;
        }
        galleryGF.generateHorizontalAppList(apps, appPopularContent, true); 
        break;
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
      
  }
  
  // the gallery page is the listener that should deal with this
  //    really, projectlist should be a listener
  public void onSourceLoadCompleted(UserProject projectInfo) {
    
  }
}	  
  
  

 
