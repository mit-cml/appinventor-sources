// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.youngandroid;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;

/**
 * Tests that {@link YoungAndroidProjectService#build} only accepts unguessable build nonces and
 * never lets one user take over another user's nonce. The nonce is the only credential for the
 * unauthenticated /b/&lt;nonce&gt; download link.
 */
public class YoungAndroidProjectServiceNonceTest extends LocalDatastoreTestCase {

  private static final String OWNER_ID = "owner-user";
  private static final String OTHER_ID = "other-user";
  private static final String VALID_NONCE = "0123456789abcdef0123456789abcdef";

  private StorageIo storage;
  private YoungAndroidProjectService service;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storage = StorageIoInstanceHolder.getInstance();
    service = new YoungAndroidProjectService(storage);
  }

  public void testBuildRejectsMalformedNonce() {
    long projectId = createProject(OWNER_ID, "OwnerProject");
    User owner = new User(OWNER_ID, "owner@example.com", true, false, null);
    String[] badNonces = {
        null,
        "",
        "5yc1r",                               // old 10^7 base-36 format
        "0123456789ABCDEF0123456789ABCDEF",    // uppercase
        "0123456789abcdef0123456789abcde",     // 31 chars
        "0123456789abcdef0123456789abcdef0",   // 33 chars
        "0123456789abcdef0123456789abcdeg"     // non-hex character
    };
    for (String nonce : badNonces) {
      RpcResult result = service.build(owner, projectId, nonce, "Android", false, false, false,
          false);
      assertTrue("Build accepted malformed nonce: " + nonce, result.failed());
      if (nonce != null) {
        assertNull("Malformed nonce was stored: " + nonce, storage.getNoncebyValue(nonce));
      }
    }
  }

  public void testBuildCannotTakeOverAnotherUsersNonce() {
    long ownerProjectId = createProject(OWNER_ID, "OwnerProject");
    long otherProjectId = createProject(OTHER_ID, "OtherProject");
    storage.storeNonce(VALID_NONCE, OWNER_ID, ownerProjectId);

    User other = new User(OTHER_ID, "other@example.com", true, false, null);
    RpcResult result = service.build(other, otherProjectId, VALID_NONCE, "Android", false, false,
        false, false);

    assertTrue(result.failed());
    Nonce stored = storage.getNoncebyValue(VALID_NONCE);
    assertEquals(OWNER_ID, stored.getUserId());
    assertEquals(ownerProjectId, stored.getProjectId());
  }

  private long createProject(String userId, String name) {
    Project project = new Project(name);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    project.addTextFile(new TextFile("src/Screen1.scm", ""));
    return storage.createProject(userId, project, "");
  }
}
