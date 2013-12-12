
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.GalleryPage;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.boxes.Box;



/**
 * Box implementation for Gallery list.
 *
 */
public final class GalleryAppBox extends Box {

  // Singleton Gallery explorer box instance (only one Gallery explorer allowed)
  private static final GalleryAppBox INSTANCE = new GalleryAppBox();

  // Gallery list for young android
  private static GalleryPage pPage;

  /**
   * Returns the singleton GalleryAppBox.
   *
   * @return  Gallery list box
   */
  public static GalleryAppBox getGalleryAppBox() {
    return INSTANCE;
  }
  
  public static void setApp(GalleryApp app,  Boolean editable)
  {
//	OdeLog.log("######### I got in setApp");
    pPage = new GalleryPage(app,editable);
    INSTANCE.setContent(pPage);
  }
  /**
   * Creates new Gallery list box.
   */
  private GalleryAppBox() {
    super(MESSAGES.galleryAppBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    //pPage = new GalleryPage(app);
    //setContent(pPage);
  }

  /**
   * Returns Gallery page associated with Gallerys explorer box.
   *
   * @return  Gallery list
   */
  public GalleryPage getGalleryPage() {
     return pPage;
  }
}
