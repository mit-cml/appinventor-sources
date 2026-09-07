// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lms;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.encryption.KeyczarEncryptor;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.storage.StorageUtil;

/**
 * Tests for {@link LmsCredentialStore}.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LmsCredentialStoreTest extends LocalDatastoreTestCase {

  private static final String KEYSTORE_ROOT_PATH =
      TestUtils.APP_INVENTOR_ROOT_DIR + "/appengine/build/war/";  // must end with a slash

  private static final String USER_ID = "1";
  private static final String USER_EMAIL = "user@example.com";
  private static final String REFRESH_TOKEN = "1//0gRefreshTokenValueExample";

  private LmsCredentialStore store;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    KeyczarEncryptor.rootPath.setForTest(KEYSTORE_ROOT_PATH);
    StorageIoInstanceHolder.getInstance().getUser(USER_ID, USER_EMAIL);
    store = new LmsCredentialStore();
  }

  public void testNoCredentialInitially() throws Exception {
    assertFalse(store.hasGoogleCredential(USER_ID));
    assertNull(store.getGoogleRefreshToken(USER_ID));
  }

  public void testSaveThenGetReturnsSameToken() throws Exception {
    store.saveGoogleRefreshToken(USER_ID, REFRESH_TOKEN);
    assertTrue(store.hasGoogleCredential(USER_ID));
    assertEquals(REFRESH_TOKEN, store.getGoogleRefreshToken(USER_ID));
  }

  public void testSaveReplacesExistingToken() throws Exception {
    store.saveGoogleRefreshToken(USER_ID, REFRESH_TOKEN);
    String second = "1//0gSecondTokenValue";
    store.saveGoogleRefreshToken(USER_ID, second);
    assertEquals(second, store.getGoogleRefreshToken(USER_ID));
  }

  public void testStoredBytesDoNotContainPlaintext() throws Exception {
    store.saveGoogleRefreshToken(USER_ID, REFRESH_TOKEN);
    StorageIo storageIo = StorageIoInstanceHolder.getInstance();
    byte[] stored =
        storageIo.downloadRawUserFile(USER_ID, StorageUtil.LMS_GOOGLE_REFRESH_TOKEN_FILENAME);
    String storedAsString = new String(stored, StorageUtil.DEFAULT_CHARSET);
    assertFalse(storedAsString.contains(REFRESH_TOKEN));
  }

  public void testEmptyArgumentsRejected() {
    try {
      store.saveGoogleRefreshToken("", REFRESH_TOKEN);
      fail();
    } catch (Exception e) {
      // expected
    }
    try {
      store.saveGoogleRefreshToken(USER_ID, "");
      fail();
    } catch (Exception e) {
      // expected
    }
  }
}
