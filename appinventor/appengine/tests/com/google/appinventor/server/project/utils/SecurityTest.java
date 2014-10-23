// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.utils;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.encryption.KeyczarEncryptor;
import com.google.appinventor.server.storage.StorageIo;

import junit.framework.TestCase;

/**
 * Tests for {@link Security}.
 *
 */
public class SecurityTest extends TestCase {

  public static final String KEYSTORE_ROOT_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/build/war/";  // must end with a slash

  @Override
  public void setUp() {
    KeyczarEncryptor.rootPath.setForTest(KEYSTORE_ROOT_PATH);
  }

  /**
   * Tests {@link Security#encryptUserAndProjectId(long, long)},
   * {@link Security#decryptUserId(String)} and
   * {@link Security#decryptProjectId(String)}.
   */
  public void testUserAndProjectIdEncryption() throws EncryptionException {
    // Test same IDs for user and project
    String userId = "1";
    long projectId = 1;
    String encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
    assertEquals(userId, Security.decryptUserId(encryptedIds));
    assertEquals(projectId, Security.decryptProjectId(encryptedIds));

    // Test different IDs for user and project
    userId = "3";
    projectId = 4;
    encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
    assertEquals(userId, Security.decryptUserId(encryptedIds));
    assertEquals(projectId, Security.decryptProjectId(encryptedIds));

    // Test one invalid user ID (expect it to throw EncryptionException)
    userId = "";
    projectId = 5;
    try {
      encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
      fail();
    } catch (EncryptionException e) {
      // expected
    }

    // Test another invalid user ID (expect it to throw EncryptionException)
    userId = null;
    projectId = 55;
    try {
      encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
      fail();
    } catch (EncryptionException e) {
      // expected
    }

    // Test one invalid project ID (expect the result to be invalid ID as well)
    userId = "6";
    projectId = StorageIo.INVALID_PROJECTID;
    encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
    assertEquals(userId, Security.decryptUserId(encryptedIds));
    assertEquals(projectId, Security.decryptProjectId(encryptedIds));

    // Test both invalid IDs together (expect the result to be invalid IDs as well)
    userId = "";
    projectId = StorageIo.INVALID_PROJECTID;
    try {
      encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
      fail();
    } catch (EncryptionException e) {
      // expected
    }

    // Test special long values

    userId = "7";
    projectId = Long.MAX_VALUE;
    encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
    assertEquals(userId, Security.decryptUserId(encryptedIds));
    assertEquals(projectId, Security.decryptProjectId(encryptedIds));

    userId = "";
    projectId = Long.MIN_VALUE;
    try {
      encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
      fail();
    } catch (EncryptionException e) {
      // expected
    }

    userId = "";
    projectId = Long.MAX_VALUE;
    try {
      encryptedIds = Security.encryptUserAndProjectId(userId, projectId);
      fail();
    } catch (EncryptionException e) {
      // expected
    }

    // Test putting in some bogus value for the encrypted IDs
    try {
      Security.decryptUserId("");
      fail();
    } catch (EncryptionException e) {
      // expected
    }
    try {
      Security.decryptProjectId("");
      fail();
    } catch (EncryptionException e) {
      // expected
    }
  }
}
