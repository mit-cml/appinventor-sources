// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.encryption.KeyczarEncryptor;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

/**
 * Tests for {@link ProjectServiceImpl#exportProjectToDrive(long)}, the Route C
 * submit-to-Google-Classroom endpoint. These cover the two branches that need no
 * live Google call: the entry-point ownership check, and the graceful failure
 * when the user has not connected a Google account. The successful upload path
 * drives live Google calls and is left to the integration tests.
 *
 * <p>This test sets the current user directly through {@link LocalUser} rather
 * than mocking it, so it runs without PowerMock.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class ProjectServiceExportToDriveTest extends LocalDatastoreTestCase {

  private static final String KEYSTORE_ROOT_PATH =
      TestUtils.APP_INVENTOR_ROOT_DIR + "/appengine/build/war/";  // must end with a slash

  private static final String USER_ID = "1";
  private static final String USER_EMAIL = "user@example.com";
  private static final String PACKAGE_NAME = "com.example.App";
  private static final String PROJECT_NAME = "App";

  private StorageIo storageIo;
  private ProjectServiceImpl projectService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    KeyczarEncryptor.rootPath.setForTest(KEYSTORE_ROOT_PATH);
    storageIo = StorageIoInstanceHolder.getInstance();
    LocalUser.getInstance().set(storageIo.getUser(USER_ID, USER_EMAIL));
    projectService = new ProjectServiceImpl();
  }

  public void testExportProjectToDriveRejectsProjectNotOwned() {
    // A project id the signed-in user does not own must be rejected by the
    // entry-point ownership check, before any export work happens.
    try {
      projectService.exportProjectToDrive(999999L);
      fail("expected a SecurityException for a project the user does not own");
    } catch (SecurityException expected) {
      // expected
    }
  }

  public void testExportProjectToDriveWithoutCredentialFails() {
    long projectId = projectService.newProject(
        YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, PROJECT_NAME,
        new NewYoungAndroidProjectParameters(PACKAGE_NAME)).getProjectId();
    // The user owns the project but has not connected a Google account, so the
    // export must fail gracefully with a failing RpcResult rather than throw.
    RpcResult result = projectService.exportProjectToDrive(projectId);
    assertFalse(result.succeeded());
    assertEquals("NOT_CONNECTED", result.getError());
  }
}
