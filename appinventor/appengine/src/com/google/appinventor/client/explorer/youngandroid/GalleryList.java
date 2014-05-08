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

  public static final int REQUEST_FEATURED=1;
  public static final int REQUEST_RECENT=2;
  public static final int REQUEST_SEARCH=3;
  public static final int REQUEST_MOSTLIKED=4;
  public static final int REQUEST_MOSTDOWNLOADED=5;
  public static final int REQUEST_MOSTVIEWED=6;
  public static final int REQUEST_BYDEVELOPER=7;
  public static final int REQUEST_BYTAG=8;
  public static final int REQUEST_ALL=9;
  public static final int REQUEST_REMIXED_TO=10;

  private int appRecentCounter = 0;
  private int appFeaturedCounter = 0;
  private int appPopularCounter = 0;
  private int appSearchCounter = 0;
  private boolean appRecentExhausted = false;
  private boolean appFeaturedExhausted = false;
  private boolean appPopularExhausted = false;
  private boolean appSearchExhausted = false;
  
  private final String activeNext = "More Apps";
//  private final String activePrev = "Previous Apps";

  public static final int NUMAPPSTOSHOW = 10;

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
    		"<link href='http://fonts.googleapis.com/css?" +
    		"family=Roboto:400,300,100' rel='stylesheet' type='text/css'>");
    galleryGUI.add(headerExtra);

    // Add content to panels
//    addGalleryAppTab(appFeatured, appFeaturedContent, REQUEST_FEATURED);
    addGalleryAppTab(appRecent, appRecentContent, REQUEST_RECENT);
    addGalleryAppTab(appSearch, appSearchContent, REQUEST_SEARCH);
    addGalleryAppTab(appPopular, appPopularContent, REQUEST_MOSTDOWNLOADED);

    // addGallerySearchTab(appSearch);  
    // don't think we need because in regular addgallerytab below

    // Add panels to main tabPanel
    appTabs.add(appRecent, "Recent");
//    appTabs.add(appFeatured, "Featured"); // 2014/05/15: don't need now
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
//    gallery.GetFeatured(0, offset, 0); // 2014/05/15: don't need now
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
    if (request == REQUEST_SEARCH) {
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
          gallery.FindApps(searchText.getText(), 0, NUMAPPSTOSHOW, 0);
        }
      });
    } else if (request == REQUEST_RECENT) {
      gallery.GetMostRecent(appRecentCounter, NUMAPPSTOSHOW);
    } else if (request == REQUEST_MOSTDOWNLOADED) {
      gallery.GetMostDownloaded(appPopularCounter, NUMAPPSTOSHOW);
    }
    container.add(content);

    // Add regular GUI components
//    final Label buttonPrev = new Label();
//    buttonPrev.setText(activePrev);
    FlowPanel prev = new FlowPanel();
//    prev.add(buttonPrev);
    prev.addStyleName("gallery-nav-prev");
    container.add(prev);

    final Label buttonNext = new Label();
    buttonNext.setText(activeNext);
    buttonNext.removeStyleName("disabled");
    buttonNext.addStyleName("active");
    FlowPanel next = new FlowPanel();
    prev.add(buttonNext);
    next.addStyleName("gallery-nav-next");
    container.add(next);
    
    final Label counter = new Label("Counting...");
    counter.addStyleName("gallery-nav-counter");
    if (request != REQUEST_FEATURED) {
      container.add(counter);
      counter.setText("");      
    }

    /**
    buttonPrev.addClickHandler(new ClickHandler() {
      //  @Override
      public void onClick(ClickEvent event) {
        switch (request) {
<<<<<<< HEAD
        case 1: 
          if (appFeaturedCounter - NUMAPPSTOSHOW >= 0) {
=======
        case REQUEST_FEATURED:
          if (appFeaturedCounter - offset >= 0) {
>>>>>>> Fixed message, profile and app page error
            // If the previous page still has apps to retrieve, do it
            appFeaturedCounter -= NUMAPPSTOSHOW;
            gallery.GetFeatured(appFeaturedCounter, NUMAPPSTOSHOW, 0);
            buttonPrev.setText(activePrev);
          } else {
            buttonPrev.setText(disabledPrev);
            OdeLog.log("prev appFeaturedCounter = " + appFeaturedCounter);
          }
          break;   
<<<<<<< HEAD
        case 2: 
          if (appRecentCounter - NUMAPPSTOSHOW >= 0) {
=======
        case REQUEST_RECENT:
          if (appRecentCounter - offset >= 0) {
>>>>>>> Fixed message, profile and app page error
            // If the previous page still has apps to retrieve, do it
            appRecentCounter -= NUMAPPSTOSHOW;
            gallery.GetMostRecent(appRecentCounter, NUMAPPSTOSHOW);
            buttonPrev.setText(activePrev);
          } else {
            buttonPrev.setText(disabledPrev);
            OdeLog.log("prev appRecentCounter = " + appRecentCounter);
          }
          break;  
<<<<<<< HEAD
        case 3: 
          if (appSearchCounter - NUMAPPSTOSHOW >= 0) {
=======
        case REQUEST_SEARCH:
          if (appSearchCounter - offset >= 0) {
>>>>>>> Fixed message, profile and app page error
            // If the previous page still has apps to retrieve, do it
            appSearchCounter -= NUMAPPSTOSHOW;
            gallery.FindApps(searchText.getText(), appSearchCounter, NUMAPPSTOSHOW, 0);
            buttonPrev.setText(activePrev);
          } else {
            buttonPrev.setText(disabledPrev);
            OdeLog.log("prev appSearchCounter = " + appSearchCounter);
          }
          break;  
<<<<<<< HEAD
        case 5:
          if (appPopularCounter - NUMAPPSTOSHOW >= 0) {
=======
        case REQUEST_MOSTDOWNLOADED:
          if (appPopularCounter - offset >= 0) {
>>>>>>> Fixed message, profile and app page error
            // If the previous page still has apps to retrieve, do it
            appPopularCounter -= NUMAPPSTOSHOW;
            gallery.GetMostDownloaded(appPopularCounter, NUMAPPSTOSHOW);
            buttonPrev.setText(activePrev);
          } else {
            buttonPrev.setText(disabledPrev);
            OdeLog.log("prev appPopularCounter = " + appPopularCounter);
          }
          break;  
        }
      }
    });    
    */

    buttonNext.addClickHandler(new ClickHandler() {
      //  @Override
      public void onClick(ClickEvent event) {
        switch (request) {
          case REQUEST_FEATURED:
            if (!appFeaturedExhausted) {
              // If the next page still has apps to retrieve, do it
              appFeaturedCounter += NUMAPPSTOSHOW;
              gallery.GetFeatured(appFeaturedCounter, NUMAPPSTOSHOW, 0);
              buttonNext.removeStyleName("disabled");
              buttonNext.addStyleName("active");
            } else {
              buttonNext.removeStyleName("active");
              buttonNext.setStyleName("disabled");
              OdeLog.log("next appFeaturedCounter = " + appFeaturedCounter);
            }
            break;    
          case REQUEST_RECENT:
            if (!appRecentExhausted) {
              // If the next page still has apps to retrieve, do it
              appRecentCounter += NUMAPPSTOSHOW;
              gallery.GetMostRecent(appRecentCounter, NUMAPPSTOSHOW);
              buttonNext.removeStyleName("disabled");
              buttonNext.addStyleName("active");
            } else {
              buttonNext.removeStyleName("active");
              buttonNext.setStyleName("disabled");
              OdeLog.log("next appRecentCounter = " + appRecentCounter);
            }
            break;   
          case REQUEST_SEARCH:
            if (!appSearchExhausted) {
              // If the next page still has apps to retrieve, do it
              appSearchCounter += NUMAPPSTOSHOW;
              gallery.FindApps(searchText.getText(), appSearchCounter, NUMAPPSTOSHOW, 0);
              buttonNext.removeStyleName("disabled");
              buttonNext.addStyleName("active");
            } else {
              buttonNext.removeStyleName("active");
              buttonNext.setStyleName("disabled");
              OdeLog.log("next appSearchCounter = " + appSearchCounter);
            }
            break;   
          case REQUEST_MOSTDOWNLOADED:
            if (!appPopularExhausted) {
              // If the next page still has apps to retrieve, do it
              appPopularCounter += NUMAPPSTOSHOW;
              gallery.GetMostDownloaded(appPopularCounter, NUMAPPSTOSHOW);
              buttonNext.removeStyleName("disabled");
              buttonNext.addStyleName("active");
            } else {
              buttonNext.removeStyleName("active");
              buttonNext.setStyleName("disabled");
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
        gallery.FindApps(searchText.getText(), 0, NUMAPPSTOSHOW, 0);
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
      case REQUEST_FEATURED:
        if (apps.size() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appFeaturedExhausted = true;
        } else {
          appFeaturedExhausted = false;
        }
        galleryGF.generateHorizontalAppList(apps, appFeaturedContent, false);
        break;
      case REQUEST_RECENT:
        if (apps.size() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appRecentExhausted = true;
        } else {
          appRecentExhausted = false;
        }
        galleryGF.generateHorizontalAppList(apps, appRecentContent, false);
        break;
      case REQUEST_SEARCH:
        if (apps.size() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appSearchExhausted = true;
        } else {
          appSearchExhausted = false;
        }
        galleryGF.generateHorizontalAppList(apps, appSearchContent, true);
        break;
      case REQUEST_MOSTDOWNLOADED: 
        if (apps.size() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appPopularExhausted = true;
        } else {
          appPopularExhausted = false;
        }
        galleryGF.generateHorizontalAppList(apps, appPopularContent, false);
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
  
  

 
