// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.riq.MockHttpServletRequest;
import com.riq.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.easymock.EasyMock.expect;

/**
 * Tests for {@link DownloadServlet}. Mocks out FileExporter. Mainly tests
 * request url parsing logic.
 *
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileExporterImpl.class, LocalUser.class, DownloadServlet.class })
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

  private FileExporterImpl exporterMock;
  private LocalUser localUserMock;

  @Before
  public void setUp() throws Exception {
    PowerMock.mockStatic(LocalUser.class);
    localUserMock = PowerMock.createNiceMock(LocalUser.class);
    expect(LocalUser.getInstance()).andReturn(localUserMock).anyTimes();
    expect(localUserMock.getUserId()).andReturn(USER_ID).anyTimes();
    exporterMock = PowerMock.createNiceMock(FileExporterImpl.class);
    PowerMock.expectNew(FileExporterImpl.class).andReturn(exporterMock).anyTimes();

    dummyZip = new ProjectSourceZip(DUMMY_ZIP_FILENAME, new byte[] {}, 2);
    dummyZipWithTitle = new ProjectSourceZip(DUMMY_ZIP_FILENAME_WITH_TITLE, new byte[] {}, 2);
    dummyApk = new RawFile(DUMMY_APK_FILENAME, new byte[] {});
    dummyFile = new RawFile(DUMMY_FILENAME, new byte[] {});
  }

  private void checkResponseHeader(MockHttpServletResponse response, String header) {
    List cd = (List) response.getHeader("content-disposition");
    assertEquals(header, cd.get(0));
    assertEquals(1, cd.size());
  }

  @Test
  public void testDownloadProjectSourceZipWithoutTitle() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "project-source/1234");
    expect(exporterMock.exportProjectSourceZip(USER_ID, PROJECT_ID, true, false, null, false, false))
        .andReturn(dummyZip);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    MockHttpServletResponse response = new MockHttpServletResponse();
    download.doGet(request, response);
    checkResponseHeader(response, "attachment; filename=\"filename123.aia\"");
    assertEquals("application/zip; charset=utf-8", response.getContentType());
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadProjectSourceZipWithTitle() throws IOException {
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "project-source/1234/My Project Title 123");
    expect(exporterMock.exportProjectSourceZip(USER_ID, PROJECT_ID, true, false,
        "MyProjectTitle123.aia", false, false))
        .andReturn(dummyZipWithTitle);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    MockHttpServletResponse response = new MockHttpServletResponse();
    download.doGet(request, response);
    checkResponseHeader(response, "attachment; filename=\"MyProjectTitle123.aia\"");
    assertEquals("application/zip; charset=utf-8", response.getContentType());
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadProjectSourceZipWithNonExistingProject() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "project-source/12345");
    expect(exporterMock.exportProjectSourceZip(USER_ID, 12345L, true, false, null, false, false))
        .andThrow(expectedException);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    try {
      download.doGet(request, new MockHttpServletResponse());
      fail();
    } catch (IllegalArgumentException ex) {
      assertEquals(expectedException, ex);
    }
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadProjectOutputFileWithoutTarget() throws IOException {
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "project-output/1234");
    expect(exporterMock.exportProjectOutputFile(USER_ID, PROJECT_ID, null))
        .andReturn(dummyApk);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    MockHttpServletResponse response = new MockHttpServletResponse();
    download.doGet(request, response);
    checkResponseHeader(response, "attachment; filename=\"filename123.apk\"");
    assertEquals("application/vnd.android.package-archive; charset=utf-8",
        response.getContentType());
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadProjectOutputFileWithTarget() throws IOException {
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "project-output/1234/target1");
    expect(exporterMock.exportProjectOutputFile(USER_ID, PROJECT_ID, "target1"))
        .andReturn(dummyApk);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    MockHttpServletResponse response = new MockHttpServletResponse();
    download.doGet(request, response);
    checkResponseHeader(response, "attachment; filename=\"filename123.apk\"");
    assertEquals("application/vnd.android.package-archive; charset=utf-8",
        response.getContentType());
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadProjectOutputFileWithNonExistingProject() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "project-output/12345");
    expect(exporterMock.exportProjectOutputFile(USER_ID, 12345L, null))
        .andThrow(expectedException);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    try {
      download.doGet(request, new MockHttpServletResponse());
      fail();
    } catch (IllegalArgumentException ex) {
      assertEquals(expectedException, ex);
    }
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadProjectOutputFileWithNonExistingTarget() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "project-output/1234/target3");
    expect(exporterMock.exportProjectOutputFile(USER_ID, PROJECT_ID, "target3"))
        .andThrow(expectedException);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    try {
      download.doGet(request, new MockHttpServletResponse());
      fail();
    } catch (IllegalArgumentException ex) {
      assertEquals(expectedException, ex);
    }
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadFile() throws IOException {
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "file/1234/" + FORM1_QUALIFIED_NAME);
    expect(exporterMock.exportFile(USER_ID, PROJECT_ID, FORM1_QUALIFIED_NAME))
        .andReturn(dummyFile);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    MockHttpServletResponse response = new MockHttpServletResponse();
    download.doGet(request, response);
    checkResponseHeader(response, "attachment; filename=\"filename123\"");
    assertEquals("text/plain; charset=utf-8", response.getContentType());
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadFileWithNonExistingProject() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "file/12345/" + FORM1_QUALIFIED_NAME);
    expect(exporterMock.exportFile(USER_ID, 12345L, FORM1_QUALIFIED_NAME))
        .andThrow(expectedException);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    try {
      download.doGet(request, new MockHttpServletResponse());
      fail();
    } catch (IllegalArgumentException ex) {
      assertEquals(expectedException, ex);
    }
    PowerMock.verifyAll();
  }

  @Test
  public void testDownloadFileWithNonExistingFile() throws IOException {
    IllegalArgumentException expectedException = new IllegalArgumentException();
    MockHttpServletRequest request = new MockHttpServletRequest(DOWNLOAD_URL +
        "file/1234/" + FORM1_QUALIFIED_NAME + "1");
    String nonExistentFile = FORM1_QUALIFIED_NAME + "1";
    expect(exporterMock.exportFile(USER_ID, PROJECT_ID, nonExistentFile))
        .andThrow(expectedException);
    PowerMock.replayAll();
    DownloadServlet download = new DownloadServlet();
    try {
      download.doGet(request, new MockHttpServletResponse());
      fail();
    } catch (IllegalArgumentException ex) {
      assertEquals(expectedException, ex);
    }
    PowerMock.verifyAll();
    }

  // TODO(user): Add testDownloadAllProjectsSource* to test
  // downloading all projects.
}
