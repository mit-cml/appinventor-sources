// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.riq.MockHttpServletRequest;
import com.riq.MockHttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link UploadServlet} backed by the real storage layer, so that the ownership check
 * on asset uploads is exercised end to end rather than against a mocked {@link StorageIo}.
 */
public class UploadServletStorageTest {
  private static final String OWNER_ID = "1";
  private static final String OWNER_EMAIL = "owner@example.com";
  private static final String OTHER_ID = "2";
  private static final String OTHER_EMAIL = "other@example.com";
  private static final String UPLOAD_URL = "http://localhost/ode/upload/file/";
  private static final String EXISTING_ASSET = "assets/existing.png";
  private static final byte[] EXISTING_CONTENT = "original".getBytes(StandardCharsets.UTF_8);
  private static final String BOUNDARY = "AppInventorTestBoundary";

  private final LocalDatastoreTestCase helper = LocalDatastoreTestCase.createHelper();
  private MockedStatic<LocalUser> localUserStatic;
  private LocalUser localUserMock;
  private StorageIo storageIo;
  private long projectId;

  @Before
  public void setUp() throws Exception {
    helper.setUp();
    storageIo = StorageIoInstanceHolder.getInstance();

    localUserMock = Mockito.mock(LocalUser.class);
    localUserStatic = Mockito.mockStatic(LocalUser.class);
    localUserStatic.when(LocalUser::getInstance).thenReturn(localUserMock);

    storageIo.getUser(OWNER_ID, OWNER_EMAIL);
    storageIo.getUser(OTHER_ID, OTHER_EMAIL);
    Project project = new Project("OwnersProject");
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    project.addTextFile(new TextFile("src/com/example/OwnersProject/Screen1.scm", "{}"));
    projectId = storageIo.createProject(OWNER_ID, project, "{}");
    storageIo.addSourceFilesToProject(OWNER_ID, projectId, false, EXISTING_ASSET);
    storageIo.uploadRawFileForce(projectId, EXISTING_ASSET, OWNER_ID, EXISTING_CONTENT);
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
    localUserStatic.close();
  }

  /**
   * The owner of a project can upload a new asset to it.
   */
  @Test
  public void testOwnerCanUploadAssetToOwnProject() throws Exception {
    MockHttpServletResponse response = uploadAs(OWNER_ID, "assets/kitty.png", "meow");
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(storageIo.getProjectSourceFiles(OWNER_ID, projectId).contains("assets/kitty.png"));
    assertTrue(Arrays.equals("meow".getBytes(StandardCharsets.UTF_8),
        storageIo.downloadRawFile(OWNER_ID, projectId, "assets/kitty.png")));
  }

  /**
   * A signed-in user cannot add a new asset to a project owned by someone else.
   */
  @Test
  public void testUploadNewAssetToAnotherUsersProjectIsRejected() throws Exception {
    List<String> filesBefore = storageIo.getProjectSourceFiles(OWNER_ID, projectId);
    MockHttpServletResponse response = uploadAs(OTHER_ID, "assets/kitty.png", "meow");
    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    List<String> filesAfter = storageIo.getProjectSourceFiles(OWNER_ID, projectId);
    assertFalse(filesAfter.contains("assets/kitty.png"));
    assertEquals(filesBefore.size(), filesAfter.size());
  }

  /**
   * A signed-in user cannot replace an existing asset in a project owned by someone else.
   */
  @Test
  public void testUploadOverAnotherUsersAssetIsRejected() throws Exception {
    MockHttpServletResponse response = uploadAs(OTHER_ID, EXISTING_ASSET, "replaced");
    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    assertTrue(Arrays.equals(EXISTING_CONTENT,
        storageIo.downloadRawFile(OWNER_ID, projectId, EXISTING_ASSET)));
  }

  private MockHttpServletResponse uploadAs(String userId, String path, String content)
      throws Exception {
    Mockito.when(localUserMock.getUserId()).thenReturn(userId);
    // A minimal multipart/form-data body, shaped like the one the browser sends.
    String body = "--" + BOUNDARY + "\r\n"
        + "Content-Disposition: form-data; name=\"" + ServerLayout.UPLOAD_FILE_FORM_ELEMENT
        + "\"; filename=\"upload.png\"\r\n"
        + "Content-Type: application/octet-stream\r\n\r\n"
        + content + "\r\n"
        + "--" + BOUNDARY + "--\r\n";
    MockHttpServletRequest request =
        new MockHttpServletRequest(UPLOAD_URL + projectId + "/" + path);
    request.setMethod("POST");
    request.setContentType("multipart/form-data; boundary=" + BOUNDARY);
    request.setPostData(body.getBytes(StandardCharsets.UTF_8));
    MockHttpServletResponse response = new MockHttpServletResponse();
    new UploadServlet().doPost(request, response);
    return response;
  }
}
