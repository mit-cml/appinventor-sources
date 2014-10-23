// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.utils;

import com.google.appinventor.server.LocalUser;
import com.google.appinventor.server.project.WebStartSupport;
import com.google.appinventor.server.project.youngandroid.YoungAndroidWebStartSupport;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

/**
 * Finds the appropriate {@link WebStartSupport} for a project type.
 *
 */
public class WebStartSupportDispatcher {

  // WebStartSupport for Young Android projects
  private final transient YoungAndroidWebStartSupport youngAndroidWebStartSupport;

  /**
   * Creates a new WebStartSupportDispatcher.
   */
  public WebStartSupportDispatcher() {
    youngAndroidWebStartSupport =
      new YoungAndroidWebStartSupport(StorageIoInstanceHolder.INSTANCE,
        LocalUser.getInstance());
  }

  /**
   * Always returns youngAndroidWebStartSupport. That's the only kind we know
   * about now.
   */
  public WebStartSupport getWebStartSupport() {
    return youngAndroidWebStartSupport;
  }
}
