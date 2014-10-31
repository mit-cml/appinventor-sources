// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.utils;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.project.youngandroid.YoungAndroidWebStartSupport;


/**
 * Tests for {@link WebStartSupportDispatcher}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class WebStartSupportDispatcherTest extends LocalDatastoreTestCase {
  private WebStartSupportDispatcher dispatcher;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dispatcher = new WebStartSupportDispatcher();
  }

  public void testGetWebStartSupport() throws Exception {
    assertTrue(dispatcher.getWebStartSupport()
        instanceof YoungAndroidWebStartSupport);
  }
}
