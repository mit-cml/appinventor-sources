// Copyright 2009 Google Inc. All Rights Reserved.

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
