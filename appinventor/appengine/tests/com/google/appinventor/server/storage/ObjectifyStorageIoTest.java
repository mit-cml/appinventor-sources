// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.StoredData.ProjectData;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.component.Component;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;

import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.json.JSONObject;

/**
 * Tests for {@link ObjectifyStorageIo}.
 *
 * @author sharon@google.com (Sharon Perl)
 */
public class ObjectifyStorageIoTest extends LocalDatastoreTestCase {

  private static final String SETTINGS = "{settings: \"none\"}";
  private static final String FAKE_PROJECT_TYPE = "FakeProjectType";
  private static final String PROJECT_NAME = "Project1";
  private static final String FILE_NAME1 = "File1.src";
  private static final String FILE_NAME2 = "src/File2.blk";
  private static final String RAW_FILE_NAME1 = "assets/File1.jpg";
  private static final String RAW_FILE_NAME2 = "assets/File2.wav";
  private static final String COMPONENT_FILE_NAME1 = "com.package.Twitter.aix";
  private static final String COMPONENT_FILE_NAME2 = "com.package.Facebook.aix";
  private static final String COMPONENT_EXTENSION_NAME = ".aix";
  private static final String FILE_NAME_OUTPUT = "File.apk";
  private static final String FILE_CONTENT1 = "The quick onyx goblin jumps over the lazy dwarf";
  private static final String FILE_CONTENT2 = "This Pangram contains four a's, one b, two c's, "
      + "one d, thirty e's, six f's, five g's, seven h's, eleven i's, one j, one k, two l's, "
      + "two m's, eighteen n's, fifteen o's, two p's, one q, five r's, twenty-seven s's, "
      + "eighteen t's, two u's, seven v's, eight w's, two x's, three y's, & one z.";
  private static final byte[] RAW_FILE_CONTENT1 = { (byte) 0, (byte) 1, (byte) 32, (byte) 255};
  private static final byte[] RAW_FILE_CONTENT2 = { (byte) 0, (byte) 1, (byte) 32, (byte) 255};
  private static final byte[] RAW_FILE_CONTENT3 = { (byte) 0, (byte) 1, (byte) 2, (byte) 3};
  private static final byte[] FILE_CONTENT_OUTPUT = { (byte) 0, (byte) 1, (byte) 32, (byte) 255};
  private static final String FORM_NAME = "Form1";
  private static final String FORM_QUALIFIED_NAME = "com.yourdomain." + FORM_NAME;
  private static final String ASSET_FILE_NAME1 = "assets/kitty.jpg";
  private static final byte[] ASSET_FILE_CONTENT1 = { (byte) 0, (byte) 1, (byte) 32, (byte) 255};
  private static final String APK_FILE_NAME1 = "/ode/build/Android/HelloPurr.apk";
  private static final byte[] APK_FILE_CONTENT = { (byte) 0, (byte) 1, (byte) 32, (byte) 255};
  private static final String BLOCK_FILE_NAME = "src/blocks.blk";
  private static final byte[] BLOCK_FILE_CONTENT = {(byte) 0, (byte) 1, (byte) 32, (byte) 255};

  private ObjectifyStorageIo storage;
  private Project project;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storage = new ObjectifyStorageIo();

    project = new Project(PROJECT_NAME);
    project.setProjectType(FAKE_PROJECT_TYPE);
    project.addTextFile(new TextFile(FILE_NAME1, FILE_CONTENT1));
    project.addTextFile(new TextFile(FILE_NAME2, FILE_CONTENT2));
    project.addRawFile(new RawFile(RAW_FILE_NAME1, RAW_FILE_CONTENT1));
    project.addRawFile(new RawFile(RAW_FILE_NAME2, RAW_FILE_CONTENT2));
  }

  private void createUserFiles(String userId, String userEmail, ObjectifyStorageIo storage)
    throws UnsupportedEncodingException {
    // remove files in case they were already created
    storage.getUser(userId, userEmail);  // ensure userId exists in the DB
    storage.deleteUserFile(userId, FILE_NAME1);
    storage.deleteUserFile(userId, FILE_NAME2);
    storage.deleteUserFile(userId, RAW_FILE_NAME1);
    storage.deleteUserFile(userId, RAW_FILE_NAME2);
    storage.createRawUserFile(userId, FILE_NAME1,
        FILE_CONTENT1.getBytes(StorageUtil.DEFAULT_CHARSET));
    // note: the following 3 files should be stored in blobstore
    storage.createRawUserFile(userId, FILE_NAME2,
        FILE_CONTENT2.getBytes(StorageUtil.DEFAULT_CHARSET));
    storage.createRawUserFile(userId, RAW_FILE_NAME1, RAW_FILE_CONTENT1);
    storage.createRawUserFile(userId, RAW_FILE_NAME2, RAW_FILE_CONTENT2);
  }

  public void testGetUser() {
    final String USER_ID = "500";
    final String USER_EMAIL = "user500@test.com";
    final String USER_EMAIL_NEW = "newuser500@test.com";

    User user1 = storage.getUser(USER_ID, USER_EMAIL);
    assertEquals(USER_ID, user1.getUserId());
    assertEquals(USER_EMAIL, user1.getUserEmail());

    User user2 = storage.getUser(USER_ID);
    assertEquals(USER_ID, user2.getUserId());
    assertEquals(USER_EMAIL, user2.getUserEmail());

    User user3 = storage.getUser(USER_ID, USER_EMAIL_NEW);
    assertEquals(USER_ID, user3.getUserId());
    assertEquals(USER_EMAIL_NEW, user3.getUserEmail());

    User user4 = storage.getUser(USER_ID);
    assertEquals(USER_ID, user4.getUserId());
    assertEquals(USER_EMAIL_NEW, user4.getUserEmail());
  }

  public void testSetTosAccepted() {
    final String USER_ID = "100";
    final String USER_EMAIL = "newuser100@test.com";
    ObjectifyStorageIo.requireTos.setForTest(true);
    User user = storage.getUser(USER_ID, USER_EMAIL);
    assertEquals(false, user.getUserTosAccepted());
    storage.setTosAccepted(USER_ID);
    assertEquals(true, storage.getUser(USER_ID, USER_EMAIL).getUserTosAccepted());
  }

  public void testLoadSettingsNewUser() {
    final String USER_ID = "200";
    final String USER_EMAIL = "newuser200@test.com";
    assertEquals("", storage.loadSettings(USER_ID));
  }

  public void testStoreLoadSettings() {
    final String USER_ID = "300";
    final String USER_EMAIL = "newuser300@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    storage.storeSettings(USER_ID, SETTINGS);
    assertEquals(SETTINGS, storage.loadSettings(USER_ID));
  }

  public void testCreateProjectSuccessful() {
    final String USER_ID = "400";
    final String USER_EMAIL = "newuser400@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    storage.createProject(USER_ID, project, SETTINGS);
    assertEquals(1, storage.getProjects(USER_ID).size());
  }

  public void testCreateProjectFailFirst() {
    final String USER_ID = "600";
    final String USER_EMAIL = "newuser600@test.com";
    // fail on first job in createProject (2nd job overall)
    StorageIo throwingStorage = new FailingJobObjectifyStorageIo(2);

    try {
      throwingStorage.getUser(USER_ID, USER_EMAIL);
      throwingStorage.createProject(USER_ID, project, SETTINGS);
    } catch (RuntimeException e) {
      assertEquals(0, throwingStorage.getProjects(USER_ID).size());
      return;
    }

    fail();
  }

  public void testCreateProjectFailSecond() {
    final String USER_ID = "700";
    final String USER_EMAIL = "newuser700@test.com";
    // fail on second job in createProject (3rd job overall)
    StorageIo throwingStorage = new FailingJobObjectifyStorageIo(3);

    try {
      throwingStorage.getUser(USER_ID, USER_EMAIL);
      throwingStorage.createProject(USER_ID, project, SETTINGS);
    } catch (RuntimeException e) {
      assertEquals(0, throwingStorage.getProjects(USER_ID).size());
      return;
    }

    fail();
  }

  public void testUploadBeforeAdd() throws BlocksTruncatedException {
    final String USER_ID = "800";
    final String USER_EMAIL = "newuser800@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    try {
      storage.uploadFile(projectId, FILE_NAME1, USER_ID, "does not matter",
          StorageUtil.DEFAULT_CHARSET);
      fail("Allowed upload before add");
    } catch (IllegalStateException ignored) {
      // File upload should be preceded by add
    }
    try {
      storage.uploadRawFile(projectId, FILE_NAME1, USER_ID, true, "does not matter".getBytes());
      fail("Allowed upload before add");
    } catch (IllegalStateException ignored) {
      // File upload should be preceded by add
    }
  }

  public void testMuliRoleFile() {
    final String USER_ID = "1000";
    final String USER_EMAIL = "newuser1000@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    storage.addSourceFilesToProject(USER_ID, projectId, false, FILE_NAME1);
    try {
      storage.addOutputFilesToProject(USER_ID, projectId, FILE_NAME1);
      fail("File role changed");
    } catch (IllegalStateException ignored) {
      // File role change is not allowed
    }
    try {
      storage.removeOutputFilesFromProject(USER_ID, projectId, FILE_NAME1);
      fail("File role changed");
    } catch (IllegalStateException ignored) {
      // File role change is not allowed
    } catch (RuntimeException ignored) {
      // File role change is not allowed
    }
  }

  public void testUpdateModificationTime() throws BlocksTruncatedException {
    final String USER_ID = "1100";
    final String USER_EMAIL = "newuser1100@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    UserProject uproject = storage.getUserProject(USER_ID, projectId);
    long creationDate = uproject.getDateCreated();
    long modificationDate = uproject.getDateModified();
    assertEquals(creationDate, modificationDate);
    long oldModificationDate = modificationDate;

    storage.addSourceFilesToProject(USER_ID, projectId, false, FILE_NAME1);
    assertTrue(storage.getProjectSourceFiles(USER_ID, projectId).contains(FILE_NAME1));
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertEquals(oldModificationDate, modificationDate);
    oldModificationDate = modificationDate;

    storage.removeSourceFilesFromProject(USER_ID, projectId, false, FILE_NAME1);
    assertFalse(storage.getProjectSourceFiles(USER_ID, projectId).contains(FILE_NAME1));
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertEquals(oldModificationDate, modificationDate);
    oldModificationDate = modificationDate;

    storage.addSourceFilesToProject(USER_ID, projectId, true, FILE_NAME1);
    assertTrue(storage.getProjectSourceFiles(USER_ID, projectId).contains(FILE_NAME1));
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    // Note: Modification date will not change due to restrictions where we only
    // update project modification date if it is more then a minute since the last
    // update.
    assertTrue(oldModificationDate <= modificationDate);
    oldModificationDate = modificationDate;

    storage.removeSourceFilesFromProject(USER_ID, projectId, true, FILE_NAME1);
    assertFalse(storage.getProjectSourceFiles(USER_ID, projectId).contains(FILE_NAME1));
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertTrue(oldModificationDate <= modificationDate);
    oldModificationDate = modificationDate;

    storage.addSourceFilesToProject(USER_ID, projectId, false, FILE_NAME1);
    modificationDate = storage.uploadFile(projectId, FILE_NAME1, USER_ID, FILE_CONTENT1,
        StorageUtil.DEFAULT_CHARSET);
    assertTrue(oldModificationDate <= modificationDate);
    oldModificationDate = modificationDate;
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertEquals(oldModificationDate, modificationDate);
    oldModificationDate = modificationDate;

    storage.addOutputFilesToProject(USER_ID, projectId, FILE_NAME_OUTPUT);
    modificationDate = storage.uploadRawFile(projectId, FILE_NAME_OUTPUT, USER_ID,
        true, FILE_CONTENT_OUTPUT);
    assertTrue(oldModificationDate <= modificationDate);
    oldModificationDate = modificationDate;
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertEquals(oldModificationDate, modificationDate);
    oldModificationDate = modificationDate;


    modificationDate = storage.deleteFile(USER_ID, projectId, FILE_NAME1);
    assertTrue(oldModificationDate <= modificationDate);
    oldModificationDate = modificationDate;
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertEquals(oldModificationDate, modificationDate);
    oldModificationDate = modificationDate;
  }

  public void testAddRemoveFile() throws BlocksTruncatedException {
    final String USER_ID = "1200";
    final String USER_EMAIL = "newuser1200@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    storage.addSourceFilesToProject(USER_ID, projectId, false, FILE_NAME1);
    storage.uploadFile(projectId, FILE_NAME1, USER_ID, FILE_CONTENT1, StorageUtil.DEFAULT_CHARSET);
    storage.addOutputFilesToProject(USER_ID, projectId, FILE_NAME_OUTPUT);
    storage.uploadRawFile(projectId, FILE_NAME_OUTPUT, USER_ID, true, FILE_CONTENT_OUTPUT);

    assertTrue(storage.getProjectSourceFiles(USER_ID, projectId).contains(FILE_NAME1));
    assertTrue(storage.getProjectOutputFiles(USER_ID, projectId).contains(FILE_NAME_OUTPUT));
    assertEquals(FILE_CONTENT1, storage.downloadFile(USER_ID, projectId, FILE_NAME1,
        StorageUtil.DEFAULT_CHARSET));
    assertTrue(
        java.util.Arrays.equals(FILE_CONTENT_OUTPUT,
                                storage.downloadRawFile(USER_ID, projectId, FILE_NAME_OUTPUT)));

    storage.removeSourceFilesFromProject(USER_ID, projectId, false, FILE_NAME1);
    assertFalse(storage.getProjectSourceFiles(USER_ID, projectId).contains(FILE_NAME1));
    assertTrue(storage.getProjectOutputFiles(USER_ID, projectId).contains(FILE_NAME_OUTPUT));

    storage.removeOutputFilesFromProject(USER_ID, projectId, FILE_NAME_OUTPUT);
    assertFalse(storage.getProjectSourceFiles(USER_ID, projectId).contains(FILE_NAME1));
    assertFalse(storage.getProjectOutputFiles(USER_ID, projectId).contains(FILE_NAME_OUTPUT));
  }

  public void testAddRemoveUserFile() {
    // Note that neither FILE_NAME1 nor FILE_NAME_OUTPUT should exist
    // at the start of this test
    final String USER_ID = "1100";
    final String USER_EMAIL = "newuser1100@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    storage.addFilesToUser(USER_ID, FILE_NAME1);
    storage.uploadUserFile(USER_ID, FILE_NAME1, FILE_CONTENT1,
        StorageUtil.DEFAULT_CHARSET);
    storage.addFilesToUser(USER_ID, FILE_NAME_OUTPUT);
    storage.uploadRawUserFile(USER_ID, FILE_NAME_OUTPUT, FILE_CONTENT_OUTPUT);

    assertTrue(storage.getUserFiles(USER_ID).contains(FILE_NAME1));
    assertTrue(storage.getUserFiles(USER_ID).contains(FILE_NAME_OUTPUT));
    assertEquals(FILE_CONTENT1, storage.downloadUserFile(USER_ID, FILE_NAME1,
        StorageUtil.DEFAULT_CHARSET));
    assertEquals(new String(FILE_CONTENT_OUTPUT),
        new String(storage.downloadRawUserFile(USER_ID, FILE_NAME_OUTPUT)));

    storage.deleteUserFile(USER_ID, FILE_NAME1);
    assertFalse(storage.getUserFiles(USER_ID).contains(FILE_NAME1));
    assertTrue(storage.getUserFiles(USER_ID).contains(FILE_NAME_OUTPUT));

    storage.deleteUserFile(USER_ID, FILE_NAME_OUTPUT);
    assertFalse(storage.getUserFiles(USER_ID).contains(FILE_NAME1));
    assertFalse(storage.getUserFiles(USER_ID).contains(FILE_NAME_OUTPUT));
  }

  public void testUnsupportedEncoding() throws BlocksTruncatedException {
    final String USER_ID = "1100";
    final String USER_EMAIL = "newuser1100@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    storage.addSourceFilesToProject(USER_ID, projectId, false, FILE_NAME1);
    try {
      storage.uploadFile(projectId, FILE_NAME1, USER_ID, FILE_CONTENT1, "No such encoding");
      fail("Unsupported encoding accepted");
    } catch (RuntimeException e) {
      // This encoding is not supported
      assertTrue(e.getCause() instanceof UnsupportedEncodingException);
    }
    storage.uploadFile(projectId, FILE_NAME1, USER_ID, FILE_CONTENT1, StorageUtil.DEFAULT_CHARSET);
    try {
      storage.downloadFile(USER_ID, projectId, FILE_NAME1, "No such encoding");
      fail("Unsupported encoding accepted");
    } catch (RuntimeException e) {
      // This encoding is not supported
      assertTrue(e.getCause() instanceof UnsupportedEncodingException);
    }
  }

  public void testUnsupportedEncodingUserFIle() {
    // Note that neither FILE_NAME1 nor FILE_NAME_OUTPUT should exist
    // at the start of this test
    final String USER_ID = "1100";
    final String USER_EMAIL = "newuser1100@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    storage.addFilesToUser(USER_ID, FILE_NAME1);
    try {
      storage.uploadUserFile(USER_ID, FILE_NAME1, FILE_CONTENT1, "No such encoding");
      fail("Unsupported encoding accepted");
    } catch (RuntimeException e) {
      // This encoding is not supported
      assertTrue(e.getCause() instanceof UnsupportedEncodingException);
    }
    storage.uploadUserFile(USER_ID, FILE_NAME1, FILE_CONTENT1,
        StorageUtil.DEFAULT_CHARSET);
    try {
      storage.downloadUserFile(USER_ID, FILE_NAME1, "No such encoding");
      fail("Unsupported encoding accepted");
    } catch (RuntimeException e) {
      // This encoding is not supported
      assertTrue(e.getCause() instanceof UnsupportedEncodingException);
    }
  }

  public void testBlobFiles() throws BlocksTruncatedException {
    final String USER_ID = "1300";
    final String USER_EMAIL = "newuser1300@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(
        USER_ID, PROJECT_NAME, YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        FORM_QUALIFIED_NAME);
    storage.addSourceFilesToProject(USER_ID, projectId, false, ASSET_FILE_NAME1);
    storage.uploadRawFile(projectId, ASSET_FILE_NAME1, USER_ID, true, ASSET_FILE_CONTENT1);
    storage.addSourceFilesToProject(USER_ID, projectId, false, BLOCK_FILE_NAME);
    storage.uploadRawFile(projectId, BLOCK_FILE_NAME, USER_ID, true, BLOCK_FILE_CONTENT);
    storage.addOutputFilesToProject(USER_ID, projectId, APK_FILE_NAME1);
    storage.uploadRawFile(projectId, APK_FILE_NAME1, USER_ID, true, APK_FILE_CONTENT);

    assertTrue(storage.getProjectSourceFiles(USER_ID, projectId).contains(ASSET_FILE_NAME1));
    assertTrue(storage.getProjectOutputFiles(USER_ID, projectId).contains(APK_FILE_NAME1));
    assertTrue(Arrays.equals(ASSET_FILE_CONTENT1,
        storage.downloadRawFile(USER_ID, projectId, ASSET_FILE_NAME1)));
    assertTrue(Arrays.equals(APK_FILE_CONTENT,
        storage.downloadRawFile(USER_ID, projectId, APK_FILE_NAME1)));
    assertTrue(Arrays.equals(BLOCK_FILE_CONTENT,
        storage.downloadRawFile(USER_ID, projectId, BLOCK_FILE_NAME)));
    assertTrue(storage.isGcsFile(projectId, ASSET_FILE_NAME1));
    assertTrue(storage.isGcsFile(projectId, APK_FILE_NAME1));
    assertTrue(!storage.isGcsFile(projectId, BLOCK_FILE_NAME)); // small block files now in datastore

    storage.removeSourceFilesFromProject(USER_ID, projectId, false, ASSET_FILE_NAME1);
    storage.removeOutputFilesFromProject(USER_ID, projectId, APK_FILE_NAME1);
    storage.removeSourceFilesFromProject(USER_ID, projectId, false, BLOCK_FILE_NAME);
    assertFalse(storage.getProjectSourceFiles(USER_ID, projectId).contains(ASSET_FILE_NAME1));
    assertFalse(storage.getProjectOutputFiles(USER_ID, projectId).contains(APK_FILE_NAME1));
    assertFalse(storage.getProjectOutputFiles(USER_ID, projectId).contains(BLOCK_FILE_NAME));
    // TODO(sharon): would be good to check that blobrefs are deleted from blobstore too

    // TODO(sharon): should test large blob files (e.g., >2MB (chunk size), >4MB (row size));
  }

  public void testOldBlockFilesInDatastoreStillWork() throws BlocksTruncatedException {
    // Create new storage object that forces storage in the datastore
    ObjectifyStorageIo oldStyleStorage = new ObjectifyStorageIo() {

      boolean useBlobstoreForFile(String fileName, int length) {
        return false;
      }
    };

    final String USER_ID = "1310";
    final String USER_EMAIL = "newuser1310@test.com";
    oldStyleStorage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(
        USER_ID, PROJECT_NAME, YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        FORM_QUALIFIED_NAME);
    oldStyleStorage.addSourceFilesToProject(USER_ID, projectId, false, BLOCK_FILE_NAME);
    oldStyleStorage.uploadRawFile(projectId, BLOCK_FILE_NAME, USER_ID, true, BLOCK_FILE_CONTENT);
    assertTrue(Arrays.equals(BLOCK_FILE_CONTENT,
                                       oldStyleStorage.downloadRawFile(
                                           USER_ID, projectId, BLOCK_FILE_NAME)));
    assertFalse(oldStyleStorage.isGcsFile(projectId, BLOCK_FILE_NAME));

    // Test that we can still get the content with an ordinary storage object
    assertTrue(Arrays.equals(BLOCK_FILE_CONTENT,
        storage.downloadRawFile(
          USER_ID, projectId, BLOCK_FILE_NAME)));
 }

  public void testGetProject() {
    final String USER_ID = "1400";
    final String USER_EMAIL = "newuser1400@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    ProjectData result = storage.getProject(projectId);
    assertEquals(projectId, result.id.longValue());
    assertEquals(PROJECT_NAME, result.name);
    assertEquals(FAKE_PROJECT_TYPE, result.type);
  }

  public void testGetProject_withNonexistentProject() {
    final String USER_ID = "1500";
    final String USER_EMAIL = "newuser1500@test.com";
    storage.getUser(USER_ID, USER_EMAIL);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    long nonExistentProjectId = (projectId + 10);
    ProjectData result = storage.getProject(nonExistentProjectId);
    assertNull(result);
  }

  public void testWrongUserThrowsException() throws Exception {
    final String USER_ID = "1600";
    final String USER_EMAIL = "newuser1600@test.com";
    final String USER_ID2 = "1700";
    createUserFiles(USER_ID, USER_EMAIL, storage);

    long projectId = storage.createProject(USER_ID, project, SETTINGS);
    assertTrue(Arrays.equals(RAW_FILE_CONTENT1,
        storage.downloadRawFile(USER_ID, projectId, RAW_FILE_NAME1)));
    try {
      storage.downloadRawFile(USER_ID2, projectId, RAW_FILE_NAME1);
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof UnauthorizedAccessException
                 || e.getCause() instanceof UnauthorizedAccessException);
    }
  }

  public void testTempFiles() throws Exception {
    String fileName = storage.uploadTempFile("test\n".getBytes(Charsets.UTF_8));
    BufferedReader reader = new BufferedReader(new InputStreamReader(storage.openTempFile(fileName),
        Charsets.UTF_8));
    assertTrue(reader.readLine().equals("test"));
    storage.deleteTempFile(fileName);
    try {
      storage.deleteTempFile("frob"); // Should fail because doesn't start with __TEMP__
      fail();
    } catch (Exception e) {
      assertTrue(e instanceof RuntimeException);
    }
  }

  /*
   * Fail on the Nth call to runJobWithRetries, where N is the value of the
   * failingRun argument to the constructor. Also allows counting
   * blob deletions.
   */
  private static class FailingJobObjectifyStorageIo extends ObjectifyStorageIo {
    private final int failingRun;
    private int run;
    private int numDeletedBlobs = 0;

    FailingJobObjectifyStorageIo(int failingRun) {
      super();
      this.failingRun = failingRun;
      run = 0;
    }

    @Override
    void runJobWithRetries(JobRetryHelper job, boolean useTransaction) throws ObjectifyException {
      ++run;
      if (run != failingRun) {
        super.runJobWithRetries(job, useTransaction);
      } else {
        throw new ObjectifyException("job failed (on purpose)");
      }
    }

    @Override
    protected void deleteBlobstoreFile(String blobstoreKey) {
      super.deleteBlobstoreFile(blobstoreKey);
      numDeletedBlobs++;
    }

    int numBlobsDeleted() {
      return numDeletedBlobs;
    }
  }

  private long createProject(String userId, String name, String type, String fileName) {
    return createProject(userId, name, type, fileName, storage);
  }

  private long createProject(String userId, String name, String type, String fileName,
                             ObjectifyStorageIo storageIo) {
    Project project = new Project(name);
    project.setProjectType(type);
    project.addTextFile(new TextFile(fileName, ""));
    return storageIo.createProject(userId, project, SETTINGS);
  }
}
