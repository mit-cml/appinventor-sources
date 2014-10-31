// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.testutils.TestUtils;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.apphosting.utils.config.GenerationDirectory;

import junit.framework.TestCase;

/**
 * Abstract base class for tests that need local datastore.
 *
 * @author lizlooney@google.com
 */
public abstract class LocalDatastoreTestCase extends TestCase {
  private static final String APPENGINE_GENERATED_DIR = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/build/tests/appengine-generated";
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(GenerationDirectory.GENERATED_DIR_PROPERTY, APPENGINE_GENERATED_DIR);
    helper.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    helper.tearDown();
  }

  /**
   * Tests that create multiple threads should call <code>setUpThread</code> for each thread.
   */
  public void setUpThread() {
    helper.setUp();
  }

  /**
   * Tests that cannot extend <code>LocalDatastoreTestCase</code> can call
   * <code>createHelper</code> to create a <code>LocalDatastoreTestCase</code>.
   */
  public static LocalDatastoreTestCase createHelper() {
    return new LocalDatastoreTestCase() {};
  }
}
