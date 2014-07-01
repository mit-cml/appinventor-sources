// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.GalleryListBox;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.GalleryClient;

/**
 * The project toolbar houses command buttons in the Young Android Project tab.
 *
 */
public class GalleryToolbar extends Composite {
  public static List<GalleryToolbar> allSearchToolbars = new ArrayList<GalleryToolbar>();
  final TextBox searchText;
  final Button searchButton;

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public GalleryToolbar() {
    allSearchToolbars.add(this);
    HorizontalPanel toolbar = new HorizontalPanel();
    toolbar.setWidth("100%");
    toolbar.setStylePrimaryName("ya-GalleryToolbar");

    FlowPanel searchPanel = new FlowPanel();
    searchText = new TextBox();
    searchText.addStyleName("gallery-search-textarea");
    searchButton = new Button("Search for apps");
    searchButton.addStyleName("search-compontent");
    searchPanel.add(searchText);
    searchPanel.add(searchButton);
    searchPanel.addStyleName("gallery");
    toolbar.add(searchPanel);
    searchButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        GalleryClient.getInstance().FindApps(searchText.getText(), 0, GalleryList.NUMAPPSTOSHOW, 0, true);
        searchText.setFocus(true);
        Ode.getInstance().switchToGalleryView();
        GalleryListBox.getGalleryListBox().getGalleryList().setSelectTabIndex(2);
        for(GalleryToolbar toolbar : allSearchToolbars){
          toolbar.getSearchText().setText(searchText.getText());
        }
        //TODO in gallerylist.java --> findapps: create a way to grab keyword from this toolbar
        //this is just a temp solution.
        GalleryListBox.getGalleryListBox().getGalleryList().getSearchText().setText(searchText.getText());
      }
    });
    searchText.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent e) {
        if(e.getNativeKeyCode() == KeyCodes.KEY_ENTER){
          GalleryClient.getInstance().FindApps(searchText.getText(), 0, GalleryList.NUMAPPSTOSHOW, 0, true);
          searchText.setFocus(true);
          Ode.getInstance().switchToGalleryView();
          GalleryListBox.getGalleryListBox().getGalleryList().setSelectTabIndex(2);
          for(GalleryToolbar toolbar : allSearchToolbars){
            toolbar.getSearchText().setText(searchText.getText());
          }
          //TODO in gallerylist.java --> findapps: create a way to grab keyword from this toolbar
          //this is just a temp solution.
          GalleryListBox.getGalleryListBox().getGalleryList().getSearchText().setText(searchText.getText());
        }
      }
    });
    initWidget(toolbar);
  }

  public TextBox getSearchText(){
    return searchText;
  }

  public Button getSearchButton(){
    return searchButton;
  }
}
