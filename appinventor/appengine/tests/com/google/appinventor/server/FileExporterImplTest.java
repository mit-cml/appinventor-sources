// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.UnauthorizedAccessException;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Tests for {@link FileExporterImpl}.
 *
 */
public class FileExporterImplTest extends LocalDatastoreTestCase {
  private static final String USER_ID = "1";

  // The following represent a fake project, containing both source and
  // output files, for the purpose of testing.
  private static final String FAKE_PROJECT_TYPE = "FakeProjectType";
  private static final String PROJECT_NAME = "Project1";
  private static final String FORM1_NAME = "Screen1";
  private static final String FORM1_QUALIFIED_NAME = "com.yourdomain." + FORM1_NAME;
  private static final String FORM1_CONTENT = "Form A\nEnd Form";
  private static final String IMAGE1_NAME = "Image.jpg";
  private static final byte[] IMAGE_CONTENT = { (byte) 0, (byte) 1, (byte) 32, (byte) 255};
  private static final String TARGET1_NAME = "Project1.apk";
  private static final String TARGET1_QUALIFIED_NAME = "build/target1/" + TARGET1_NAME;
  private static final byte[] TARGET1_CONTENT = "pk1".getBytes();
  private static final String TARGET2_NAME = "Project2.pak";
  private static final String TARGET2_QUALIFIED_NAME = "build/target2/" + TARGET2_NAME;
  private static final byte[] TARGET2_CONTENT = "pk2".getBytes();
  private static final String SETTINGS = "";
  private static final String HISTORY = "1:History";

  private StorageIo storageIo;
  private FileExporterImpl exporter;
  private long projectId;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storageIo = StorageIoInstanceHolder.INSTANCE;
    exporter = new FileExporterImpl();
    Project project = new Project(PROJECT_NAME);
    project.setProjectType(FAKE_PROJECT_TYPE);
    project.setProjectHistory(HISTORY);
    project.addTextFile(new TextFile(FORM1_QUALIFIED_NAME, ""));
    projectId = storageIo.createProject(USER_ID, project, SETTINGS);
    storageIo.uploadFile(projectId, FORM1_QUALIFIED_NAME, USER_ID, FORM1_CONTENT,
        StorageUtil.DEFAULT_CHARSET);
    storageIo.addSourceFilesToProject(USER_ID, projectId, false, IMAGE1_NAME);
    storageIo.uploadRawFile(projectId, IMAGE1_NAME, USER_ID, true, IMAGE_CONTENT);
    storageIo.addOutputFilesToProject(USER_ID, projectId, TARGET1_QUALIFIED_NAME);
    storageIo.uploadRawFile(projectId, TARGET1_QUALIFIED_NAME, USER_ID,
        true, TARGET1_CONTENT);
    storageIo.addOutputFilesToProject(USER_ID, projectId, TARGET2_QUALIFIED_NAME);
    storageIo.uploadRawFile(projectId, TARGET2_QUALIFIED_NAME, USER_ID,
        true, TARGET2_CONTENT);
  }

  private Map<String, byte[]> testExportProjectSourceZipHelper(ProjectSourceZip project)
      throws IOException {
    ZipInputStream zis =
        new ZipInputStream(new ByteArrayInputStream(project.getContent()));
    Map<String, byte[]> content = new HashMap<String, byte[]>();
    ZipEntry zipEntry;

    while ((zipEntry = zis.getNextEntry()) != null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ByteStreams.copy(zis, baos);
      content.put(zipEntry.getName(), baos.toByteArray());
    }

    assertEquals(content.size(), project.getFileCount());

    assertTrue(content.containsKey(FORM1_QUALIFIED_NAME));
    assertTrue(content.containsKey(IMAGE1_NAME));
    assertFalse(content.containsKey(TARGET1_NAME));

    assertEquals(FORM1_CONTENT, new String(content.get(FORM1_QUALIFIED_NAME),
        StorageUtil.DEFAULT_CHARSET));
    assertTrue(Arrays.equals(IMAGE_CONTENT, content.get(IMAGE1_NAME)));

    return content;
  }

  public void testExportProjectSourceZipWithoutHistory() throws IOException {
    ProjectSourceZip project = exporter.exportProjectSourceZip(USER_ID, projectId,
      false, false, null, false, false, false, false);
    Map<String, byte[]> content = testExportProjectSourceZipHelper(project);
    assertEquals(2, content.size());
    /* Do not expect remix history when includeProjectHistory parameter is false
     * as in the publish case. */
    assertFalse(content.containsKey(FileExporter.REMIX_INFORMATION_FILE_PATH));
  }

  // TODO(user): Add test with properly formatted history
  public void testExportProjectSourceZipWithHistory() throws IOException {
    ProjectSourceZip project = exporter.exportProjectSourceZip(USER_ID, projectId,
      true, false, null, false, false, false, false);
    Map<String, byte[]> content = testExportProjectSourceZipHelper(project);
    assertEquals(3, content.size());
    // Expect the remix file to be in
    assertTrue(content.containsKey(FileExporter.REMIX_INFORMATION_FILE_PATH));
    assertEquals(HISTORY, new String(content.get(FileExporter.REMIX_INFORMATION_FILE_PATH),
        StorageUtil.DEFAULT_CHARSET));
  }

  public void testExportProjectSourceZipWithNonExistingProject() throws IOException {
    try {
      exporter.exportProjectSourceZip(USER_ID, projectId + 1, false, false, null, false, false, false, false);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof IllegalArgumentException ||
                 e.getCause() instanceof IllegalArgumentException);
    }
  }

  public void testExportProjectOutputFileWithTarget() throws IOException {
    RawFile file = exporter.exportProjectOutputFile(USER_ID, projectId, "target1");
    assertEquals(TARGET1_NAME, file.getFileName());
    assertTrue(Arrays.equals(TARGET1_CONTENT, file.getContent()));
  }

  public void testExportProjectOutputFileWithNonExistingTraget() throws IOException {
    try {
      exporter.exportProjectOutputFile(USER_ID, projectId, "target3");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  public void testExportFile() throws IOException {
    RawFile file = exporter.exportFile(USER_ID, projectId, FORM1_QUALIFIED_NAME);
    assertEquals(FORM1_QUALIFIED_NAME, file.getFileName());
    assertEquals(FORM1_CONTENT, new String(file.getContent(), StorageUtil.DEFAULT_CHARSET));
  }

  public void testExportFileWithNonExistingFile() throws IOException {
    final String nonExistingFileName = FORM1_QUALIFIED_NAME + "1";
    try {
      exporter.exportFile(USER_ID, projectId, nonExistingFileName);
      fail();
    } catch (RuntimeException e) {
      // expected
      // note that FileExporter throws an explicit RuntimeException
    }
  }

  // TODO(user): Add test of exportAllProjectsSourceZip().
}
