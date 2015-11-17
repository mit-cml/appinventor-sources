// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import java.util.ArrayList;
import java.util.List;

import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.GalleryListBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The Gallery Toolbar
 * @author Bin Lu blu2@dons.usfca.edu
 *
 */
public class GalleryToolbar extends Composite {
  private static final int SEARCHTAB = 4;
  public static List<GalleryToolbar> allSearchToolbars = new ArrayList<GalleryToolbar>();  //store the reference of all creating toolbar
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
        GalleryListBox.getGalleryListBox().getGalleryList().setSelectTabIndex(SEARCHTAB);
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
          GalleryListBox.getGalleryListBox().getGalleryList().setSelectTabIndex(SEARCHTAB);
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

  /**
   * get searchText label
   * @return Label searchText
   */
  public TextBox getSearchText(){
    return searchText;
  }

  /**
   * get getSearchButton button
   * @return Button searchButton
   */
  public Button getSearchButton(){
    return searchButton;
  }
}
