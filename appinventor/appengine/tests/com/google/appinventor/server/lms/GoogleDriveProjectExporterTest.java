// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.FileExporterImpl;
import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.encryption.KeyczarEncryptor;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

/**
 * Tests for {@link GoogleDriveProjectExporter}. Its happy path drives live Google
 * token-refresh and Drive-upload calls, so that path is covered by the
 * integration tests with mocked Google responses rather than here. This unit test
 * pins the one branch that needs no network: the guard for a user who has not
 * connected a Google credential.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class GoogleDriveProjectExporterTest extends LocalDatastoreTestCase {

  private static final String KEYSTORE_ROOT_PATH =
      TestUtils.APP_INVENTOR_ROOT_DIR + "/appengine/build/war/";  // must end with a slash

  private static final String USER_ID = "1";
  private static final String USER_EMAIL = "user@example.com";

  private GoogleDriveProjectExporter exporter;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    KeyczarEncryptor.rootPath.setForTest(KEYSTORE_ROOT_PATH);
    StorageIoInstanceHolder.getInstance().getUser(USER_ID, USER_EMAIL);
    exporter = new GoogleDriveProjectExporter(new FileExporterImpl(), new LmsCredentialStore());
  }

  public void testExportWithoutStoredCredentialThrows() throws Exception {
    // The user has not connected to Google, so there is no refresh token to mint
    // an access token from, and the export must not proceed.
    try {
      exporter.exportProjectToDrive(USER_ID, 1L);
      fail("expected an IllegalStateException when no Google credential is stored");
    } catch (IllegalStateException expected) {
      // expected
    }
  }
}
