// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.component.ComponentInfo;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import com.google.common.io.ByteStreams;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.File;
import java.io.FileInputStream;
import java.lang.Exception;
import java.util.List;

/**
 * Tests for {@link ComponentServiceImpl}.
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ LocalUser.class })
public class ComponentServiceTest {
  private LocalDatastoreTestCase helper = LocalDatastoreTestCase.createHelper();
  private StorageIo storageIo;
  private ComponentServiceImpl compServiceImpl;
  private LocalUser localUserMock;

  private static final String USER_ID = "id";
  private static final String USER_EMAIL = "noname@domain.com";
  private static final String KEYSTORE_ROOT_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/build/war/";

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    storageIo = StorageIoInstanceHolder.INSTANCE;

    PowerMock.mockStatic(LocalUser.class);
    localUserMock = PowerMock.createMock(LocalUser.class);
    expect(localUserMock.getSessionId()).andReturn("test-session").anyTimes();
    localUserMock.setSessionId("test-session");
    expectLastCall().times(1);
    expect(LocalUser.getInstance()).andReturn(localUserMock).anyTimes();
  }

  private static final String PACKAGE_BASE = "com.domain.noname.";
  private static final String PROJECT1_NAME = "Project1";
  private ProjectServiceImpl projectServiceImpl;

  void do_init() {
    storageIo.getUser(USER_ID, USER_EMAIL);
    projectServiceImpl = new ProjectServiceImpl();
    compServiceImpl = new ComponentServiceImpl();
    localUserMock.setSessionId("test-session");

  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
    PowerMock.resetAll();
  }

  @Test
  public void testImportComponentToProject() throws Exception {
    expect(localUserMock.getUserId()).andReturn(USER_ID).anyTimes();
    PowerMock.replayAll();
    do_init();

    NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(
        PACKAGE_BASE + PROJECT1_NAME);
    long user1Project1 = projectServiceImpl.newProject(
        YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, PROJECT1_NAME, params).getProjectId();

    String testingSourcePath = TestUtils.APP_INVENTOR_ROOT_DIR +
        "/appengine/tests/com/google/appinventor/server/";
    String fileName = "Component.aix";
    byte[] content = ByteStreams.toByteArray(
        new FileInputStream(new File(testingSourcePath + fileName)));

    storageIo.uploadComponentFile(USER_ID, fileName, content);

    ComponentInfo info = storageIo.getComponentInfos(USER_ID).get(0);
    long projectId = user1Project1;
    String folderPath = "awesomeFolder";
    List<ProjectNode> nodes = compServiceImpl.importComponentToProject(info, projectId, folderPath);

    assertFalse(nodes.isEmpty());
    for (ProjectNode node : nodes) {
      assertTrue(node.getFileId().startsWith(folderPath));
    }

    info = new ComponentInfo("fakeAuthorId", "fakeFullName", "fakeName", 0);
    nodes = compServiceImpl.importComponentToProject(info, projectId, folderPath);
    assertTrue(nodes.isEmpty());

    PowerMock.verifyAll();
  }

}
