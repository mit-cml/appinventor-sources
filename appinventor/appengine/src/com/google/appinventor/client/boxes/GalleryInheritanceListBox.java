
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.output.OdeLog;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.appinventor.client.explorer.youngandroid.GalleryInheritanceList;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.gwt.user.client.ui.FlowPanel;


/**
 * Box implementation for Gallery list.
 *
 */
public final class GalleryInheritanceListBox extends FlowPanel {
  private static final Logger LOG = Logger.getLogger(GalleryInheritanceListBox.class.getName());
  // Singleton Gallery explorer box instance (only one Gallery explorer allowed)
  private static final GalleryInheritanceListBox INSTANCE = new GalleryInheritanceListBox();

  // Gallery list for young android
  private GalleryInheritanceList plist;

  /**
   * Returns the singleton Gallerys list box.
   *
   * @return  Gallery list box
   */
  public static GalleryInheritanceListBox getGalleryInheritanceListBox() {
    return INSTANCE;
  }

  /**
   * Creates new Gallery list box.
   */
  private GalleryInheritanceListBox() {
    /*
    super(MESSAGES.galleryListBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable
     */
    plist = new GalleryInheritanceList();
    //plist = null;
    FlowPanel pContainer = new FlowPanel();
    pContainer.add(plist);
    this.add(pContainer);
  }

  /**
   * Returns Gallery list associated with Gallerys explorer box.
   *
   * @return  Gallery list
   */
  public GalleryInheritanceList getGalleryInheritanceList() {
     return plist;
  }
  public void setAppAttributionList(List<GalleryApp> apps)
  {
	plist.showRemixedToList(apps);
  }
}
