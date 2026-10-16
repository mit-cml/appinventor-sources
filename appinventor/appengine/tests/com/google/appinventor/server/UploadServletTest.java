// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.riq.MockHttpServletRequest;
import com.riq.MockHttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link UploadServlet}. FileImporter and StorageIo are mocked out, so these tests
 * focus on the servlet itself: in particular, that asset uploads are only accepted into
 * projects owned by the signed-in user.
 */
public class UploadServletTest {
  private static final String USER_ID = "1";
  private static final long OWN_PROJECT_ID = 1234L;
  private static final long OTHER_USERS_PROJECT_ID = 12345L;
  private static final String UPLOAD_URL = "http://localhost/ode/upload/";

  private MockedStatic<LocalUser> localUserStatic;
  private StorageIo storageIoMock;

  @Before
  public void setUp() {
    LocalUser localUserMock = Mockito.mock(LocalUser.class);
    localUserStatic = Mockito.mockStatic(LocalUser.class);
    localUserStatic.when(LocalUser::getInstance).thenReturn(localUserMock);
    Mockito.when(localUserMock.getUserId()).thenReturn(USER_ID);

    storageIoMock = Mockito.mock(StorageIo.class);
    StorageIoInstanceHolder.setInstance(storageIoMock);
  }

  @After
  public void tearDown() {
    localUserStatic.close();
    StorageIoInstanceHolder.setInstance(null);
  }

  /**
   * The owner of a project can still upload assets to it, and the file is handed to the
   * importer under the requested name.
   */
  @Test
  public void testUploadFileToOwnProject() throws Exception {
    // A minimal multipart/form-data body, shaped like the one the browser sends.
    final String boundary = "AppInventorTestBoundary";
    String body = "--" + boundary + "\r\n"
        + "Content-Disposition: form-data; name=\"" + ServerLayout.UPLOAD_FILE_FORM_ELEMENT
        + "\"; filename=\"kitty.png\"\r\n"
        + "Content-Type: application/octet-stream\r\n\r\n"
        + "meow\r\n"
        + "--" + boundary + "--\r\n";
    try (MockedConstruction<FileImporterImpl> importers =
        Mockito.mockConstruction(FileImporterImpl.class, (mock, ctx) ->
            Mockito.when(mock.importFile(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(InputStream.class)))
                .thenReturn(42L))) {
      MockHttpServletRequest request = new MockHttpServletRequest(UPLOAD_URL + "file/"
          + OWN_PROJECT_ID + "/assets/kitty.png");
      request.setMethod("POST");
      request.setContentType("multipart/form-data; boundary=" + boundary);
      request.setPostData(body.getBytes(StandardCharsets.UTF_8));
      UploadServlet upload = new UploadServlet();
      MockHttpServletResponse response = new MockHttpServletResponse();
      upload.doPost(request, response);
      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      Mockito.verify(storageIoMock).assertUserHasProject(USER_ID, OWN_PROJECT_ID);
      assertEquals(1, importers.constructed().size());
      Mockito.verify(importers.constructed().get(0)).importFile(ArgumentMatchers.eq(USER_ID),
          ArgumentMatchers.eq(OWN_PROJECT_ID), ArgumentMatchers.eq("assets/kitty.png"),
          ArgumentMatchers.any(InputStream.class));
    }
  }

  /**
   * Uploading into a project the user doesn't own is rejected with a 404, and nothing is
   * passed on to the importer.
   */
  @Test
  public void testUploadFileToProjectOwnedByAnotherUser() throws Exception {
    Mockito.doThrow(new SecurityException()).when(storageIoMock)
        .assertUserHasProject(USER_ID, OTHER_USERS_PROJECT_ID);
    try (MockedConstruction<FileImporterImpl> importers =
        Mockito.mockConstruction(FileImporterImpl.class)) {
      MockHttpServletRequest request = new MockHttpServletRequest(UPLOAD_URL + "file/"
          + OTHER_USERS_PROJECT_ID + "/assets/kitty.png");
      request.setMethod("POST");
      UploadServlet upload = new UploadServlet();
      MockHttpServletResponse response = new MockHttpServletResponse();
      upload.doPost(request, response);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
      assertEquals(1, importers.constructed().size());
      Mockito.verify(importers.constructed().get(0), Mockito.never()).importFile(
          ArgumentMatchers.anyString(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(),
          ArgumentMatchers.any(InputStream.class));
    }
  }
}
