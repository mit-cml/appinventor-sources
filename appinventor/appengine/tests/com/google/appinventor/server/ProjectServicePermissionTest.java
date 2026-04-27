// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.ComponentPermission;
import com.google.appinventor.shared.rpc.project.PermissionMetadata;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;

import com.google.common.collect.Sets;

import static junit.framework.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

/**
 * Tests for {@link ProjectServiceImpl#getProjectPermissionMetadata(long)}.
 */
@PowerMockIgnore({"javax.crypto.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ LocalUser.class })
public class ProjectServicePermissionTest {
  private final LocalDatastoreTestCase helper = LocalDatastoreTestCase.createHelper();

  private static final String USER_ID = "id1";
  private static final String USER_EMAIL = "user@domain.com";
  private static final String PROJECT_NAME = "TestProject";
  private static final String PACKAGE_BASE = "com.domain.";

  private StorageIo storageIo;
  private ProjectServiceImpl projectServiceImpl;
  private LocalUser localUserMock;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    storageIo = StorageIoInstanceHolder.getInstance();

    PowerMock.mockStatic(LocalUser.class);
    localUserMock = PowerMock.createMock(LocalUser.class);
    expect(localUserMock.getSessionId()).andReturn("test-session").anyTimes();
    expect(LocalUser.getInstance()).andReturn(localUserMock).anyTimes();
    localUserMock.set(new User(USER_ID, USER_EMAIL, false, false, null));
    expectLastCall().anyTimes();
    
    storageIo.getUser(USER_ID, USER_EMAIL);
    projectServiceImpl = new ProjectServiceImpl();
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
    PowerMock.resetAll();
  }

  @Test
  public void testGetProjectPermissionMetadata() throws Exception {
    expect(localUserMock.getUserId()).andReturn(USER_ID).anyTimes();
    PowerMock.replayAll();

    // Create a new project
    NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(
        PACKAGE_BASE + PROJECT_NAME);
    long projectId = projectServiceImpl.newProject(
        YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, PROJECT_NAME, params).getProjectId();

    // Create a mock SCM file with some components
    String scmContent = 
        "#|\n" +
        "$JSON\n" +
        "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
        "\"$Components\":[" +
        "{\"$Name\":\"Bluetooth1\",\"$Type\":\"BluetoothClient\"}," +
        "{\"$Name\":\"Location1\",\"$Type\":\"LocationSensor\"}" +
        "]}}\n" +
        "|#";
    
    String scmFileId = "src/com/domain/TestProject/Screen1.scm";
    storageIo.uploadFile(projectId, scmFileId, USER_ID, scmContent, StorageUtil.DEFAULT_CHARSET);

    // Execute
    PermissionMetadata result = projectServiceImpl.getProjectPermissionMetadata(projectId);

    // Assert
    assertNotNull(result);
    assertEquals(projectId, result.getProjectId());
    
    List<ComponentPermission> components = result.getComponents();
    // We expect BluetoothClient and LocationSensor to have permissions in our mock implementation
    assertEquals(2, components.size());

    assertTrue(result.getComponentsRequiringPermission("android.permission.BLUETOOTH").contains("BluetoothClient"));
    assertTrue(result.getComponentsRequiringPermission("android.permission.ACCESS_FINE_LOCATION").contains("LocationSensor"));

    PowerMock.verifyAll();
  }
}
