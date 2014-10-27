// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.explorer.youngandroid.GalleryList;
import com.google.gwt.user.client.ui.FlowPanel;


/**
 * Box implementation for Gallery list.
 *
 */
public final class GalleryListBox extends FlowPanel {

  // Singleton Gallery explorer box instance (only one Gallery explorer allowed)
  private static final GalleryListBox INSTANCE = new GalleryListBox();

  // Gallery list for young android
  private GalleryList plist;

  /**
   * Returns the singleton Gallerys list box.
   *
   * @return  Gallery list box
   */
  public static GalleryListBox getGalleryListBox() {
    return INSTANCE;
  }

  /**
   * Creates new Gallery list box.
   */
  private GalleryListBox() {
    /*
    super(MESSAGES.galleryListBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable
     */
    plist = null;
  }

  /**
   * Load GalleryList
   */
  public static void loadGalleryList(){
    INSTANCE.plist = new GalleryList();
    FlowPanel pContainer = new FlowPanel();
    pContainer.add(INSTANCE.plist);
    INSTANCE.add(pContainer);
  }

  /**
   * Returns Gallery list associated with Gallerys explorer box.
   *
   * @return  Gallery list
   */
  public GalleryList getGalleryList() {
     return plist;
  }
}
