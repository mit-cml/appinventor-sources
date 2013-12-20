
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.GalleryPage;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.gwt.user.client.ui.FlowPanel;



/**
 * Box implementation for Gallery list.
 *
 */
public final class GalleryAppBox extends FlowPanel {

  // Singleton Gallery explorer box instance (only one Gallery explorer allowed)
  private static final GalleryAppBox INSTANCE = new GalleryAppBox();

  // Gallery list for young android
  private static GalleryPage pPage;
  
  private static FlowPanel appContainer = new FlowPanel();

  /**
   * Returns the singleton GalleryAppBox.
   *
   * @return  Gallery list box
   */
  public static GalleryAppBox getGalleryAppBox() {
    return INSTANCE;
  }
  
  public static void setApp(GalleryApp app, int editStatus)
  {
//	OdeLog.log("######### I got in setApp");
    pPage = new GalleryPage(app, editStatus);
    appContainer.add(pPage);
  }
  /**
   * Creates new Gallery list box.
   */
  private GalleryAppBox() {
    /*
    super(MESSAGES.galleryAppBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable
    */
    this.add(appContainer);
    // Styling options
    this.addStyleName("ode-galleryapp-wrapper");
    
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
