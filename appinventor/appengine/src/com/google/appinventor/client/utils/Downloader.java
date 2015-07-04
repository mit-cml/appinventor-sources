// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.appinventor.client.ErrorReporter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Utility class to download files from the server.
 *
 * <p>Implementation note (by herbertc): I would have loved to give this action
 * a callback parameter, but it is impossible to detect a successful download.
 * After a browser saves the downloaded file, it will not generate any further
 * events.
 *
 */
public class Downloader extends Frame {
  // Singleton Downloader instance
  private static final Downloader INSTANCE = new Downloader();

  /**
   * Creates a new downloader.
   */
  private Downloader() {
    // We need to trick the browser into downloading a file by adding an
    // invisible frame to our application. That's the only way to open the
    // browser download dialog box.
    setSize("0px", "0px");
    setVisible(false);
    sinkEvents(Event.ONLOAD);
    RootPanel.get().add(this);
  }

  /**
   * Downloads content for the given path.
   *
   * @param path a relative path within the GWT module
   */
  public final void download(String path) {
    ErrorReporter.hide();
    setUrl(GWT.getModuleBaseURL() + path);
  }

  /**
   * Returns the downloader.
   *
   * @return the downloader.
   */
  public static Downloader getInstance() {
    return INSTANCE;
  }
}
