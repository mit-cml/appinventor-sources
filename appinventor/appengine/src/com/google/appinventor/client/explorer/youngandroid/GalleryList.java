// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.ArrayList;
import java.util.List;

import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.GalleryGuiFactory;
import com.google.appinventor.client.GalleryRequestListener;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
/**
 * The gallery list shows apps from the gallery in a table.
 *
 * <p> The project name and date created will be shown in the table.
 *
 * @author wolberd@google.com (Dave Wolber)
 */
public class GalleryList extends Composite implements GalleryRequestListener {

  final Ode ode = Ode.getInstance();
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
  private final FlowPanel appTutorial;
  private final FlowPanel appRecentContent;
  private final FlowPanel appFeaturedContent;
  private final FlowPanel appPopularContent;
  private final FlowPanel appSearchContent;
  private final FlowPanel appTutorialContent;
  private final TextBox searchText;

  private GalleryAppTab appRecentTab;
  private GalleryAppTab appFeaturedTab;
  private GalleryAppTab appPopularTab;
  private GalleryAppTab appSearchTab;
  private GalleryAppTab appTutorialTab;


  public static final int REQUEST_FEATURED = 1;
  public static final int REQUEST_RECENT = 2;
  public static final int REQUEST_SEARCH = 3;
  public static final int REQUEST_MOSTLIKED = 4;
  public static final int REQUEST_MOSTDOWNLOADED = 5;
  public static final int REQUEST_MOSTVIEWED = 6;
  public static final int REQUEST_BYDEVELOPER = 7;
  public static final int REQUEST_BYTAG = 8;
  public static final int REQUEST_ALL = 9;
  public static final int REQUEST_REMIXED_TO = 10;
  public static final int REQUEST_TUTORIAL = 11;

  private int appRecentCounter = 0;
  private int appFeaturedCounter = 0;
  private int appPopularCounter = 0;
  private int appSearchCounter = 0;
  private int appTutorialCounter = 0;
  private boolean appRecentExhausted = false;
  private boolean appFeaturedExhausted = false;
  private boolean appPopularExhausted = false;
  private boolean appSearchExhausted = false;
  private boolean appTutorialExhausted = false;
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
    appTutorial = new FlowPanel();
    appRecentContent = new FlowPanel();
    appFeaturedContent = new FlowPanel();
    appPopularContent = new FlowPanel();
    appSearchContent = new FlowPanel();
    appTutorialContent = new FlowPanel();
    searchText = new TextBox();

    // HTML segment for gallery typeface
    HTML headerExtra = new HTML(
        "<link href='http://fonts.googleapis.com/css?" +
        "family=Roboto:400,300,100' rel='stylesheet' type='text/css'>");
    galleryGUI.add(headerExtra);

    // Add content to panels
    appFeaturedTab = new GalleryAppTab(appFeatured, appFeaturedContent, REQUEST_FEATURED);
    appRecentTab = new GalleryAppTab(appRecent, appRecentContent, REQUEST_RECENT);
    appSearchTab = new GalleryAppTab(appSearch, appSearchContent, REQUEST_SEARCH);
    appPopularTab = new GalleryAppTab(appPopular, appPopularContent, REQUEST_MOSTLIKED);
    appTutorialTab = new GalleryAppTab(appTutorial, appTutorialContent, REQUEST_TUTORIAL);
    // don't think we need because in regular addgallerytab below

    // Add panels to main tabPanel
    appTabs.add(appRecent, "Recent");
    appTabs.add(appTutorial,"Tutorials");
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
  }

  /**
   * A wrapper class of tab, which provides help method to get/set UI components
   */
  private class GalleryAppTab{
    Label buttonNext;
    Label noResultsFound;
    Label keywordTotalResultsLabel;
    Label generalTotalResultsLabel;
    /**
     * @param container: the FlowPanel that this app tab will reside.
     * @param content: the sub-panel that contains the actual app content.
     * @param request: type of app request, for pagination.
     */
    GalleryAppTab(FlowPanel container, FlowPanel content, final int request){
      addGalleryAppTab(container, content, request);
    }

    /**
     * @return Label buttonNext
     */
    public Label getButtonNext(){
      return buttonNext;
    }

    /**
     * @return Label noResultsFound
     */
    public Label getNoResultsFound(){
      return noResultsFound;
    }

    /**
     * @return Label keywordTotalResultsLabel
     */
    public Label getKeywordTotalResultsLabel(){
      return keywordTotalResultsLabel;
    }

    /**
     * Set keywordTotalResultsLabel's text to new text
     * @param keyword the search keyword
     * @param num number of results
     */
    public void setKeywordTotalResultsLabel(String keyword, int num){
       keywordTotalResultsLabel.setText(MESSAGES.gallerySearchResultsPrefix() + keyword + MESSAGES.gallerySearchResultsInfix() + num + MESSAGES.gallerySearchResultsSuffix());
    }

    /**
     * @return Label generalTotalResultsLabel
     */
    public Label getGeneralTotalResultsLabel(){
      return generalTotalResultsLabel;
    }

    /**
     * set generalTotalResultsLabel to new text
     * @param num number of results
     */
    public void setGeneralTotalResultsLabel(int num){
      generalTotalResultsLabel.setText(num + MESSAGES.gallerySearchResultsSuffix());
    }

    /**
     * Creates the GUI components for a regular app tab.
     * This method resides here because it needs access to global variables.
     * @param container: the FlowPanel that this app tab will reside.
     * @param content: the sub-panel that contains the actual app content.
     * @param request: type of app request, for pagination.
     */
    private void addGalleryAppTab(FlowPanel container, FlowPanel content, final int request) {
      // Search specific
      if (request == REQUEST_SEARCH) {
        FlowPanel searchPanel = new FlowPanel();
        searchText.addStyleName("gallery-search-textarea");
        Button sb = new Button("Search for apps");
        searchPanel.add(searchText);
        searchPanel.add(sb);
        searchPanel.addStyleName("gallery-search-panel");
//        container.add(searchPanel);
        appSearchContent.addStyleName("gallery-search-results");
        container.add(appSearchContent);
        keywordTotalResultsLabel = new Label();
        container.add(keywordTotalResultsLabel);
        noResultsFound = new Label(MESSAGES.noResultsFound());
        noResultsFound.setVisible(false);
        container.add(noResultsFound);
        container.addStyleName("gallery-search");
        sb.addClickHandler(new ClickHandler() {
          //  @Override
          public void onClick(ClickEvent event) {
            gallery.FindApps(searchText.getText(), 0, NUMAPPSTOSHOW, 0, true);
            searchText.setFocus(true);
          }
        });
        searchText.addKeyDownHandler(new KeyDownHandler() {
          //  @Override
          public void onKeyDown(KeyDownEvent e) {
            if(e.getNativeKeyCode() == KeyCodes.KEY_ENTER){
              gallery.FindApps(searchText.getText(), 0, NUMAPPSTOSHOW, 0, true);
              searchText.setFocus(true);
            }
          }
        });
      } else if (request == REQUEST_TUTORIAL) {
        generalTotalResultsLabel = new Label();
        container.add(generalTotalResultsLabel);
        gallery.GetTutorial(appTutorialCounter, NUMAPPSTOSHOW, 0, false);
      }
        else if (request == REQUEST_RECENT) {
        generalTotalResultsLabel = new Label();
        container.add(generalTotalResultsLabel);
        gallery.GetMostRecent(appRecentCounter, NUMAPPSTOSHOW, false);
      } else if (request == REQUEST_MOSTLIKED) {
        generalTotalResultsLabel = new Label();
        container.add(generalTotalResultsLabel);
        gallery.GetMostLiked(appPopularCounter, NUMAPPSTOSHOW, false);
      } else if (request == REQUEST_FEATURED){
        generalTotalResultsLabel = new Label();
        container.add(generalTotalResultsLabel);
        gallery.GetFeatured(appFeaturedCounter, NUMAPPSTOSHOW, 0, false);
      }
      container.add(content);

      buttonNext = new Label();
      buttonNext.setText(MESSAGES.galleryMoreApps());
      buttonNext.addStyleName("active");
      if(request == REQUEST_SEARCH){
        buttonNext.setVisible(false);
      }

      FlowPanel next = new FlowPanel();
      next.add(buttonNext);
      next.addStyleName("gallery-nav-next");
      container.add(next);

      buttonNext.addClickHandler(new ClickHandler() {
        //  @Override
        public void onClick(ClickEvent event) {
          switch (request) {
            case REQUEST_FEATURED:
              if (!appFeaturedExhausted) {
                // If the next page still has apps to retrieve, do it
                appFeaturedCounter += NUMAPPSTOSHOW;
                gallery.GetFeatured(appFeaturedCounter, NUMAPPSTOSHOW, 0, false);
              }
              break;
            case REQUEST_TUTORIAL:
              if (!appTutorialExhausted) {
                // If the next page still has apps to retrieve, do it
                appTutorialCounter += NUMAPPSTOSHOW;
                gallery.GetTutorial(appTutorialCounter, NUMAPPSTOSHOW,0, false);
              }
              break;
            case REQUEST_RECENT:
              if (!appRecentExhausted) {
                // If the next page still has apps to retrieve, do it
                appRecentCounter += NUMAPPSTOSHOW;
                gallery.GetMostRecent(appRecentCounter, NUMAPPSTOSHOW, false);
              }
              break;
            case REQUEST_SEARCH:
              if (!appSearchExhausted) {
                // If the next page still has apps to retrieve, do it
                appSearchCounter += NUMAPPSTOSHOW;
                gallery.FindApps(searchText.getText(), appSearchCounter, NUMAPPSTOSHOW, 0, false);
              }
              break;
            case REQUEST_MOSTLIKED:
              if (!appPopularExhausted) {
                // If the next page still has apps to retrieve, do it
                appPopularCounter += NUMAPPSTOSHOW;
                gallery.GetMostLiked(appPopularCounter, NUMAPPSTOSHOW, false);
              }
              break;
          }
        }
      });
    }
  }

  /**
   * @return TextBox searchText
   */
  public TextBox getSearchText(){
    return searchText;
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
    Button sb = new Button(MESSAGES.gallerySearchForAppsButton());
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
        gallery.FindApps(searchText.getText(), 0, NUMAPPSTOSHOW, 0, true);
      }
    });
  }

  /**
   * Loads the proper tab GUI with gallery's app data.
   * @param apps: list of returned gallery apps from callback.
   * @param requestId: determines the specific type of app data.
   */
  private void refreshApps(GalleryAppListResult appsResult, int requestId, boolean refreshable) {
    switch (requestId) {
      case REQUEST_FEATURED:
        appFeaturedTab.setGeneralTotalResultsLabel(appsResult.getTotalCount());
        if (appsResult.getTotalCount() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appFeaturedExhausted = true;
        } else {
          appFeaturedExhausted = false;
        }
        galleryGF.generateHorizontalAppList(appsResult.getApps(), appFeaturedContent, refreshable);
        if(appsResult.getTotalCount() < NUMAPPSTOSHOW || appFeaturedCounter + NUMAPPSTOSHOW >= appsResult.getTotalCount()){
          appFeaturedTab.getButtonNext().setVisible(false);
        }else{
          appFeaturedTab.getButtonNext().setVisible(true);
        }
        break;
      case REQUEST_TUTORIAL:
        appTutorialTab.setGeneralTotalResultsLabel(appsResult.getTotalCount());
        if (appsResult.getTotalCount() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appTutorialExhausted = true;
        } else {
          appTutorialExhausted = false;
        }
        galleryGF.generateHorizontalAppList(appsResult.getApps(), appTutorialContent, refreshable);
        if(appsResult.getTotalCount() < NUMAPPSTOSHOW || appTutorialCounter + NUMAPPSTOSHOW >= appsResult.getTotalCount()){
          appTutorialTab.getButtonNext().setVisible(false);
        }else{
          appTutorialTab.getButtonNext().setVisible(true);
        }
        break;
      case REQUEST_RECENT:
        appRecentTab.setGeneralTotalResultsLabel(appsResult.getTotalCount());
        if(appsResult.getTotalCount() < NUMAPPSTOSHOW  || appRecentCounter + NUMAPPSTOSHOW >= appsResult.getTotalCount()){
          appRecentTab.getButtonNext().setVisible(false);
        }else{
          appRecentTab.getButtonNext().setVisible(true);
        }

        if (appsResult.getApps().size() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appRecentExhausted = true;
        } else {
          appRecentExhausted = false;
        }
        galleryGF.generateHorizontalAppList(appsResult.getApps(), appRecentContent, refreshable);
        break;
      case REQUEST_SEARCH:
        appSearchTab.setKeywordTotalResultsLabel(appsResult.getKeyword(), appsResult.getTotalCount());
        if(appsResult.getTotalCount() == 0){
          appSearchTab.getNoResultsFound().setVisible(true);
        }else{
          appSearchTab.getNoResultsFound().setVisible(false);
        }
        if(appSearchCounter + NUMAPPSTOSHOW >= appsResult.getTotalCount()){
          appSearchTab.getButtonNext().setVisible(false);
        }else{
          appSearchTab.getButtonNext().setVisible(true);
        }
        if (appsResult.getApps().size() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appSearchExhausted = true;
        } else {
          appSearchExhausted = false;
        }
        galleryGF.generateHorizontalAppList(appsResult.getApps(), appSearchContent, refreshable);
        break;
      case REQUEST_MOSTLIKED:
        appPopularTab.setGeneralTotalResultsLabel(appsResult.getTotalCount());
        if(appsResult.getTotalCount() < NUMAPPSTOSHOW || appPopularCounter + NUMAPPSTOSHOW >= appsResult.getTotalCount()){
          appPopularTab.getButtonNext().setVisible(false);
        }else{
          appPopularTab.getButtonNext().setVisible(true);
        }
        if (appsResult.getApps().size() < NUMAPPSTOSHOW) {
          // That means there's not enough apps to show (reaches the end)
          appPopularExhausted = true;
        } else {
          appPopularExhausted = false;
        }
        galleryGF.generateHorizontalAppList(appsResult.getApps(), appPopularContent, refreshable);
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
  /**
   * select specific tab index based on given index
   * @param index
   */
  public void setSelectTabIndex(int index){
    appTabs.selectTab(index);
  }
  /**
   * Process the results after retrieving GalleryAppListResult
   * @param appsResult GalleryAppList Result
   * @param requestId request id
   * @param refreshable whether or not clear container
   * @see GalleryRequestListener
   */
  public void onAppListRequestCompleted(GalleryAppListResult appsResult, int requestId, boolean refreshable)
  {
    List<GalleryApp> apps = appsResult.getApps();
    if (apps != null)
      refreshApps(appsResult, requestId, refreshable);
    else
      OdeLog.log("apps was null");
  }
  /**
   * Process the results after retrieving list of GalleryComment
   * @see GalleryRequestListener
   */
  public void onCommentsRequestCompleted(List<GalleryComment> comments){

  }

  /**
   * Process the results after retrieving list of UserProject
   * @see GalleryRequestListener
   */
  public void onSourceLoadCompleted(UserProject projectInfo) {

  }

}

