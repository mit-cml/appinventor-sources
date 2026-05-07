// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.Images;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Tree.Resources;

/**
 * Image bundle containing all client images.
 *
 * Note: Images extends Tree.Resources rather than ClientBundle so that
 * the Images can be used with the com.google.gwt.user.client.ui.Tree class.
 *
 */
public interface DarkModeImages extends Images {
  /**
  * Designer palette item: barometer component
  * <p>
  * Source: Ellen Spertus, released into public domain
  */
  @Source("com/google/appinventor/darkmode-images/dark-modebarometer.png")
  ImageResource barometer();

  /**
   * Designer palette item: camcorder declaration
   */
  @Source("com/google/appinventor/darkmode-images/dark-modecamcorder.png")
  ImageResource camcorder();

  /**
  * Designer palette item: hygrometer component
  * <p>
  * Source: Ellen Spertus, released into public domain
  */
  @Source("com/google/appinventor/darkmode-images/dark-modehygrometer.png")
  ImageResource hygrometer();


  /**
   * Designer palette item: LineString
   */
  @Source("com/google/appinventor/darkmode-images/dark-modelinestring.png")
  ImageResource linestring();


  /**
   * Designer palette item: progressbar circular component
   */
  @Source("com/google/appinventor/darkmode-images/circularProgress.png")
  ImageResource circularProgress();


  /**
   * Designer palette item: progressbar linear component
   */
  @Source("com/google/appinventor/darkmode-images/linearProgress.png")
  ImageResource linearProgress();

    /**
   * Designer palette item: ListView component
   */
  @Source("com/google/appinventor/darkmode-images/dark-modelistView.png")
  ImageResource listview();

    /**
   * Designer palette item: Sharing Component
   */
  @Source("com/google/appinventor/darkmode-images/dark-modesharing.png")
  ImageResource sharingComponent();


  /**
   * Designer palette item: Translator Component
   */
  @Source("com/google/appinventor/darkmode-images/dark-modetranslator.png")
  ImageResource translator();
}
