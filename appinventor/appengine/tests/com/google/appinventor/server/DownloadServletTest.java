// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.riq.MockHttpServletRequest;
import com.riq.MockHttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Tests for {@link DownloadServlet}. Mocks out FileExporter. Mainly tests
 * request url parsing logic.
 *
 */
public class DownloadServletTest {
  private static final String FORM1_NAME = "Form1";
  private static final String FORM1_QUALIFIED_NAME = "com/yourdomain/" + FORM1_NAME;
  private static final String USER_ID = "1";
  private static final long PROJECT_ID = 1234L;
  private static final String DUMMY_FILENAME = "filename123";
  private static final String DUMMY_APK_FILENAME = "filename123.apk";
  private static final String DUMMY_ZIP_FILENAME = "filename123.aia";
  private static final String DUMMY_ZIP_FILENAME_WITH_TITLE = "MyProjectTitle123.aia";
  private static final String DOWNLOAD_URL = "http://localhost/baseUrl/download/";

  private ProjectSourceZip dummyZip;
  private ProjectSourceZip dummyZipWithTitle;
  private RawFile dummyApk;
  private RawFile dummyFile;

  private LocalUser localUserMock;
  private MockedStatic<LocalUser> localUserStatic;
  private StorageIo storageIoMock;

  @Before
  public void setUp() throws Exception {
    localUserMock = Mockito.mock(LocalUser.class);
    localUserStatic = Mockito.mockStatic(LocalUser.class);
    localUserStatic.when(LocalUser::getInstance).thenReturn(localUserMock);
    Mockito.when(localUserMock.getUserId()).thenReturn(USER_ID);

    storageIoMock = Mockito.mock(StorageIo.class);
    StorageIoInstanceHolder.setInstance(storageIoMock);

    dummyZip = new ProjectSourceZip(DUMMY_ZIP_FILENAME, new byte[] {}, 2);
    dummyZipWithTitle = new ProjectSourceZip(DUMMY_ZIP_FILENAME_WITH_TITLE, new byte[] {}, 2);
    dummyApk = new RawFile(DUMMY_APK_FILENAME, new byte[] {});
    dummyFile = new RawFile(DUMMY_FILENAME, new byte[] {});
  }

  @After
  public void tearDown() {
    localUserStatic.close();
    StorageIoInstanceHolder.setInstance(null);
  }

  private void checkResponseHeader(MockHttpServletResponse response, String header) {
    List cd = (List) response.getHeader("content-disposition");
    assertEquals(header, cd.get(0));
    assertEquals(1, cd.size());
  }

  @Test
  public void testDownloadProjectSourceZipWithoutTitle() throws Exception {
    final ProjectSourceZip zip = dummyZip;
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportProjectSourceZip(
            USER_ID, PROJECT_ID, true, false, null, false, false, false, false, false, false))
            .thenReturn(zip))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL + "project-source/1234");
      DownloadServlet download = new DownloadServlet();
      MockHttpServletResponse response = new MockHttpServletResponse();
      download.doGet(request, response);
      checkResponseHeader(response, "attachment; filename=\"filename123.aia\"");
      assertEquals("application/zip; charset=utf-8", response.getContentType());
    }
  }

  @Test
  public void testDownloadProjectSourceZipWithTitle() throws IOException {
    final ProjectSourceZip zip = dummyZipWithTitle;
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportProjectSourceZip(
            USER_ID, PROJECT_ID, true, false, "MyProjectTitle123.aia", false, false, false, false, false, false))
            .thenReturn(zip))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
          "project-source/1234/My Project Title 123");
      DownloadServlet download = new DownloadServlet();
      MockHttpServletResponse response = new MockHttpServletResponse();
      download.doGet(request, response);
      checkResponseHeader(response, "attachment; filename=\"MyProjectTitle123.aia\"");
      assertEquals("application/zip; charset=utf-8", response.getContentType());
    }
  }

  @Test
  public void testDownloadProjectSourceZipWithNonExistingProject() throws IOException {
    Mockito.doThrow(new SecurityException()).when(storageIoMock).assertUserHasProject(USER_ID, 12345L);
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class)) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL + "project-source/12345");
      DownloadServlet download = new DownloadServlet();
      MockHttpServletResponse response = new MockHttpServletResponse();
      download.doGet(request, response);
      assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
  }

  @Test
  public void testDownloadProjectOutputFileWithoutTarget() throws IOException {
    final RawFile apk = dummyApk;
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportProjectOutputFile(USER_ID, PROJECT_ID, null))
            .thenReturn(apk))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL + "project-output/1234");
      DownloadServlet download = new DownloadServlet();
      MockHttpServletResponse response = new MockHttpServletResponse();
      download.doGet(request, response);
      checkResponseHeader(response, "attachment; filename=\"filename123.apk\"");
      assertEquals("application/vnd.android.package-archive", response.getContentType());
    }
  }

  @Test
  public void testDownloadProjectOutputFileWithTarget() throws IOException {
    final RawFile apk = dummyApk;
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportProjectOutputFile(USER_ID, PROJECT_ID, "target1"))
            .thenReturn(apk))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL + "project-output/1234/target1");
      DownloadServlet download = new DownloadServlet();
      MockHttpServletResponse response = new MockHttpServletResponse();
      download.doGet(request, response);
      checkResponseHeader(response, "attachment; filename=\"filename123.apk\"");
      assertEquals("application/vnd.android.package-archive", response.getContentType());
    }
  }

  @Test
  public void testDownloadProjectOutputFileWithNonExistingProject() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportProjectOutputFile(USER_ID, 12345L, null))
            .thenThrow(expectedException))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL + "project-output/12345");
      DownloadServlet download = new DownloadServlet();
      try {
        download.doGet(request, new MockHttpServletResponse());
        fail();
      } catch (IllegalArgumentException ex) {
        assertEquals(expectedException, ex);
      }
    }
  }

  @Test
  public void testDownloadProjectOutputFileWithNonExistingTarget() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportProjectOutputFile(USER_ID, PROJECT_ID, "target3"))
            .thenThrow(expectedException))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL + "project-output/1234/target3");
      DownloadServlet download = new DownloadServlet();
      try {
        download.doGet(request, new MockHttpServletResponse());
        fail();
      } catch (IllegalArgumentException ex) {
        assertEquals(expectedException, ex);
      }
    }
  }

  @Test
  public void testDownloadFile() throws IOException {
    final RawFile file = dummyFile;
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportFile(USER_ID, PROJECT_ID, FORM1_QUALIFIED_NAME))
            .thenReturn(file))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
          "file/1234/" + FORM1_QUALIFIED_NAME);
      DownloadServlet download = new DownloadServlet();
      MockHttpServletResponse response = new MockHttpServletResponse();
      download.doGet(request, response);
      checkResponseHeader(response, "attachment; filename=\"filename123\"");
      assertEquals("text/plain; charset=utf-8", response.getContentType());
    }
  }

  @Test
  public void testDownloadFileWithNonExistingProject() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportFile(USER_ID, 12345L, FORM1_QUALIFIED_NAME))
            .thenThrow(expectedException))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
          "file/12345/" + FORM1_QUALIFIED_NAME);
      DownloadServlet download = new DownloadServlet();
      try {
        download.doGet(request, new MockHttpServletResponse());
        fail();
      } catch (IllegalArgumentException ex) {
        assertEquals(expectedException, ex);
      }
    }
  }

  @Test
  public void testDownloadFileWithNonExistingFile() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    String nonExistentFile = FORM1_QUALIFIED_NAME + "1";
    try (MockedConstruction<FileExporterImpl> ignored = Mockito.mockConstruction(FileExporterImpl.class,
        (mock, ctx) -> Mockito.when(mock.exportFile(USER_ID, PROJECT_ID, nonExistentFile))
            .thenThrow(expectedException))) {
      MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
          "file/1234/" + nonExistentFile);
      DownloadServlet download = new DownloadServlet();
      try {
        download.doGet(request, new MockHttpServletResponse());
        fail();
      } catch (IllegalArgumentException ex) {
        assertEquals(expectedException, ex);
      }
    }
  }

  // TODO(user): Add testDownloadAllProjectsSource* to test
  // downloading all projects.
}
