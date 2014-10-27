// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.explorer.youngandroid.GalleryPage;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.gwt.user.client.ui.FlowPanel;



/**
 * Box implementation for Gallery app.
 *
 * @author vincentaths@gmail.com (Vincent Zhang)
 * @author wolberd@gmail.com (Dave Wolber)
 *
 */
public final class GalleryAppBox extends FlowPanel {

  // Singleton Gallery explorer box instance (only one Gallery explorer allowed)
  private static final GalleryAppBox INSTANCE = new GalleryAppBox();

  // Gallery app page for young android
  private static FlowPanel gContainer;
  private static GalleryPage gPage;

  /**
   * Returns the singleton GalleryAppBox.
   *
   * @return  Gallery app box
   */
  public static GalleryAppBox getGalleryAppBox() {
    return INSTANCE;
  }


  public static void setApp(GalleryApp app, int editStatus) {
    gContainer.clear();
    gPage = new GalleryPage(app, editStatus);
    gContainer.add(gPage);
  }


  /**
   * Creates new Gallery app box.
   */
  private GalleryAppBox() {
    gContainer = new FlowPanel();
    this.add(gContainer);
  }
}
