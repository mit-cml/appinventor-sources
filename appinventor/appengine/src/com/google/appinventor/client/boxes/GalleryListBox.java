
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.GalleryList;
import com.google.appinventor.client.widgets.boxes.Box;


/**
 * Box implementation for Gallery list.
 *
 */
public final class GalleryListBox extends Box {

  // Singleton Gallery explorer box instance (only one Gallery explorer allowed)
  private static final GalleryListBox INSTANCE = new GalleryListBox();

  // Gallery list for young android
  private final GalleryList plist;

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
    super(MESSAGES.galleryListBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    plist = new GalleryList();
    setContent(plist);
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
