// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import junitx.framework.ListAssert;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * Test FileImporterImpl.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class FileImporterImplTest extends LocalDatastoreTestCase {
  private static final String USER_ID = "12345678";
  private static final String USER_EMAIL_ADDRESS = "joeuser@gmail.com";

  // This project name matches what is in Project1.zip and ProjectWithAssets.zip
  private static final String PROJECT_NAME_1 = "project1";

  // This project name does not match what is in Project1.zip.
  private static final String PROJECT_NAME_2 = "project2";

  private StorageIo storageIo;
  private FileImporter fileImporter;

  public static final String TESTING_SOURCE_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/tests/com/google/appinventor/server/";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storageIo = StorageIoInstanceHolder.INSTANCE;
    // Create user with given parameters
    storageIo.getUser(USER_ID, USER_EMAIL_ADDRESS);
    fileImporter = new FileImporterImpl();
  }

  private UserProject importProjectArchive(String zipFileName, String projectName)
      throws Exception {
    String projectZip = TESTING_SOURCE_PATH + zipFileName;
    File zip = new File(projectZip);
    assertTrue(zip.exists());
    return fileImporter.importProject(USER_ID, projectName, new FileInputStream(zip));
  }

  public void testImportProject() throws Exception {
    UserProject userProject = importProjectArchive("Project1.zip", PROJECT_NAME_1);
    assertNotNull(userProject);
    assertEquals(PROJECT_NAME_1, userProject.getProjectName());
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(PROJECT_NAME_1, storageIo.getProjectName(USER_ID, projectId));
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    List<String> projectSourceFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSourceFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.blk");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.scm");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.yail");
  }

  public void testImportProject_withAssets() throws Exception {
    UserProject userProject = importProjectArchive("ProjectWithAssets.zip", PROJECT_NAME_1);
    assertNotNull(userProject);
    assertEquals(PROJECT_NAME_1, userProject.getProjectName());
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(PROJECT_NAME_1, storageIo.getProjectName(USER_ID, projectId));
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    List<String> projectSourceFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSourceFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.blk");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.scm");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project1/Screen1.yail");
    ListAssert.assertContains(projectSourceFiles,
        "assets/kitty.png");
    ListAssert.assertContains(projectSourceFiles,
        "assets/meow.mp3");
  }

  public void testProjectNameUsed() throws Exception {
    UserProject userProject = importProjectArchive("Project1.zip", PROJECT_NAME_2);
    assertNotNull(userProject);
    assertEquals(PROJECT_NAME_2, userProject.getProjectName());
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(PROJECT_NAME_2, storageIo.getProjectName(USER_ID, projectId));
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    List<String> projectSourceFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSourceFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project2/Screen1.blk");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project2/Screen1.scm");
    ListAssert.assertContains(projectSourceFiles,
        "src/appinventor/ai_joeuser/project2/Screen1.yail");
  }

  public void testImportProject_withoutProjectHistory() throws Exception {
    String zipFileName = "Project1.zip";
    String projectZip = TESTING_SOURCE_PATH + zipFileName;
    File zip = new File(projectZip);
    assertTrue(zip.exists());
    String projectName = "MyProject";
    UserProject userProject = fileImporter.importProject(USER_ID, projectName,
        new FileInputStream(zip), null);
    assertNotNull(userProject);
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    List<String> projectSrcFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSrcFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.blk");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.scm");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.yail");
  }

  public void testImportProject_withProjectHistory() throws Exception {
    String zipFileName = "Project1.zip";
    String projectZip = TESTING_SOURCE_PATH + zipFileName;
    File zip = new File(projectZip);
    assertTrue(zip.exists());
    String projectName = "MyProject";
    UserProject userProject =  fileImporter.importProject(USER_ID, projectName,
        new FileInputStream(zip), "HISTORY");
    assertNotNull(userProject);
    assertEquals("YoungAndroid", userProject.getProjectType());
    long projectId = userProject.getProjectId();
    ListAssert.assertContains(storageIo.getProjects(USER_ID), projectId);
    assertEquals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        storageIo.getProjectType(USER_ID, projectId));
    assertEquals("HISTORY", storageIo.getProjectHistory(USER_ID, projectId));
    List<String> projectSrcFiles = storageIo.getProjectSourceFiles(USER_ID, projectId);
    ListAssert.assertContains(projectSrcFiles, "youngandroidproject/project.properties");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.blk");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.scm");
    ListAssert.assertContains(projectSrcFiles,
        "src/appinventor/ai_joeuser/" + projectName + "/Screen1.yail");
  }

  public void testEmptyZip() throws Exception {
    try {
      UserProject userProject = importProjectArchive("EmptyZip.zip", PROJECT_NAME_1);
      fail();
    } catch (FileImporterException e) {
      assertEquals(UploadResponse.Status.NOT_PROJECT_ARCHIVE, e.uploadResponse.getStatus());
    }
  }

  public void testBuggyZip() throws Exception {
    try {
      UserProject userProject = importProjectArchive("Buggy.zip", PROJECT_NAME_1);
      fail();
    } catch (FileImporterException e) {
      assertEquals(UploadResponse.Status.NOT_PROJECT_ARCHIVE, e.uploadResponse.getStatus());
    }
  }

  public void testNotAValidProjectArchive() throws Exception {
    try {
      UserProject userProject =
          importProjectArchive("NotAValidProjectArchive.zip", PROJECT_NAME_1);
      fail();
    } catch (FileImporterException e) {
      assertEquals(UploadResponse.Status.NOT_PROJECT_ARCHIVE, e.uploadResponse.getStatus());
    }
  }
}
