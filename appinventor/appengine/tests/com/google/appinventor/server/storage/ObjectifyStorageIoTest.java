// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server.storage;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileStat;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.GSFileOptions;
import com.google.appengine.api.files.LockException;
import com.google.appengine.api.files.RecordReadChannel;
import com.google.appengine.api.files.RecordWriteChannel;
import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.StoredData.ProjectData;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

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
  private static final String FILE_NAME_OUTPUT = "File.apk";
  private static final String FILE_CONTENT1 = "The quick onyx goblin jumps over the lazy dwarf";
  private static final String FILE_CONTENT2 = "This Pangram contains four a's, one b, two c's, "
      + "one d, thirty e's, six f's, five g's, seven h's, eleven i's, one j, one k, two l's, "
      + "two m's, eighteen n's, fifteen o's, two p's, one q, five r's, twenty-seven s's, "
      + "eighteen t's, two u's, seven v's, eight w's, two x's, three y's, & one z.";
  private static final byte[] RAW_FILE_CONTENT1 = { (byte) 0, (byte) 1, (byte) 32, (byte) 255};
  private static final byte[] RAW_FILE_CONTENT2 = { (byte) 0, (byte) 1, (byte) 32, (byte) 255};
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

  private void createUserFiles(String userId, ObjectifyStorageIo storage)
    throws UnsupportedEncodingException {
    // remove files in case they were already created
    storage.getUser(userId);  // ensure userId exists in the DB
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
    ObjectifyStorageIo.requireTos.setForTest(true);
    User user = storage.getUser(USER_ID);
    assertEquals(false, user.getUserTosAccepted());
    storage.setTosAccepted(USER_ID);
    assertEquals(true, storage.getUser(USER_ID).getUserTosAccepted());
  }

  public void testLoadSettingsNewUser() {
    final String USER_ID = "200";
    assertEquals("", storage.loadSettings(USER_ID));
  }

  public void testStoreLoadSettings() {
    final String USER_ID = "300";
    storage.getUser(USER_ID);
    storage.storeSettings(USER_ID, SETTINGS);
    assertEquals(SETTINGS, storage.loadSettings(USER_ID));
  }

  public void testCreateProjectSuccessful() {
    final String USER_ID = "400";
    storage.getUser(USER_ID);
    storage.createProject(USER_ID, project, SETTINGS);
    assertEquals(1, storage.getProjects(USER_ID).size());
  }

  public void testCreateProjectFailFirst() {
    final String USER_ID = "600";
    // fail on first job in createProject (2nd job overall)
    StorageIo throwingStorage = new FailingJobObjectifyStorageIo(2);

    try {
      throwingStorage.getUser(USER_ID);
      throwingStorage.createProject(USER_ID, project, SETTINGS);
    } catch (RuntimeException e) {
      assertEquals(0, throwingStorage.getProjects(USER_ID).size());
      return;
    }

    fail();
  }

  public void testCreateProjectFailSecond() {
    final String USER_ID = "700";
    // fail on second job in createProject (3rd job overall)
    StorageIo throwingStorage = new FailingJobObjectifyStorageIo(3);

    try {
      throwingStorage.getUser(USER_ID);
      throwingStorage.createProject(USER_ID, project, SETTINGS);
    } catch (RuntimeException e) {
      assertEquals(0, throwingStorage.getProjects(USER_ID).size());
      return;
    }

    fail();
  }
  
  public void testCreateProjectDeletesBlobsOnFileFailure() {
    final String USER_ID = "710";
    // fail on 3rd blob creation.
    FailingBlobFileService failingFileService = new FailingBlobFileService(3);
    // storage doesn't really fail but we want to count blob deletions.
    FailingJobObjectifyStorageIo storageIo = 
        new FailingJobObjectifyStorageIo(0, failingFileService);
    try {
      storageIo.getUser(USER_ID);
      storageIo.createProject(USER_ID, project, SETTINGS);
    } catch (RuntimeException e) {
      assertEquals(3, failingFileService.numBlobsCreated());
      assertEquals(2, storageIo.numBlobsDeleted());
    }
  }
  
  public void testCreateProjectDeletesBlobsOnJobFailure() {
    final String USER_ID = "720";
    // we don't actually want blob creation to fail in this case, but we do
    // want to be able to count created blobs
    FailingBlobFileService failingFileService = new FailingBlobFileService(0);
    // arrange that the second job in createProject fails. make sure
    // blobs created in the first job were deleted.
    FailingJobObjectifyStorageIo storageIo = 
        new FailingJobObjectifyStorageIo(3, failingFileService);
    try {
      storageIo.getUser(USER_ID);
      storageIo.createProject(USER_ID, project, SETTINGS);
    } catch (RuntimeException e) {
      assertEquals(2, failingFileService.numBlobsCreated());
      assertEquals(2, storageIo.numBlobsDeleted());
    }
  }

  public void testCreateProjectDeletesBlobsOnRetry() {
    final String USER_ID = "730";
    // 2nd blob creation fails with ConcurrentModificationException, which
    // should cause the job to be retried. It should ultimately succeed
    // and we should have 1 blob that got deleted.
    FailingBlobFileService failingFileService = new FailingBlobFileService(2,
        new ConcurrentModificationException("this is intentional"));
    // storage doesn't really fail but we want to count deleted blobs
    FailingJobObjectifyStorageIo storageIo = 
        new FailingJobObjectifyStorageIo(0, failingFileService);
    try {
      storageIo.getUser(USER_ID);
      storageIo.createProject(USER_ID, project, SETTINGS);
    } catch (RuntimeException e) {
      fail();
    }
    assertEquals(4, failingFileService.numBlobsCreated());
    assertEquals(1, storageIo.numBlobsDeleted());
  }

  public void testUploadBeforeAdd() throws BlocksTruncatedException {
    final String USER_ID = "800";
    storage.getUser(USER_ID);
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

  public void testUploadUserFileBeforeAdd() {
    final String USER_ID = "900";
    storage.getUser(USER_ID);
    try {
      storage.uploadUserFile(USER_ID, FILE_NAME1, "does not matter",
          StorageUtil.DEFAULT_CHARSET);
      fail("Allowed upload before add");
    } catch (IllegalStateException ignored) {
      // File upload should be preceded by add
    }
    try {
      storage.uploadRawUserFile(USER_ID, FILE_NAME2, "does not matter".getBytes());
      fail("Allowed upload before add");
    } catch (IllegalStateException ignored) {
      // File upload should be preceded by add
    }
  }

  public void testMuliRoleFile() {
    final String USER_ID = "1000";
    storage.getUser(USER_ID);
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
    storage.getUser(USER_ID);
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
    assertTrue(oldModificationDate < modificationDate);
    oldModificationDate = modificationDate;

    storage.removeSourceFilesFromProject(USER_ID, projectId, true, FILE_NAME1);
    assertFalse(storage.getProjectSourceFiles(USER_ID, projectId).contains(FILE_NAME1));
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertTrue(oldModificationDate < modificationDate);
    oldModificationDate = modificationDate;

    storage.addSourceFilesToProject(USER_ID, projectId, false, FILE_NAME1);
    modificationDate = storage.uploadFile(projectId, FILE_NAME1, USER_ID, FILE_CONTENT1,
        StorageUtil.DEFAULT_CHARSET);
    assertTrue(oldModificationDate < modificationDate);
    oldModificationDate = modificationDate;
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertEquals(oldModificationDate, modificationDate);
    oldModificationDate = modificationDate;

    storage.addOutputFilesToProject(USER_ID, projectId, FILE_NAME_OUTPUT);
    modificationDate = storage.uploadRawFile(projectId, FILE_NAME_OUTPUT, USER_ID,
        true, FILE_CONTENT_OUTPUT);
    assertTrue(oldModificationDate < modificationDate);
    oldModificationDate = modificationDate;
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertEquals(oldModificationDate, modificationDate);
    oldModificationDate = modificationDate;


    modificationDate = storage.deleteFile(USER_ID, projectId, FILE_NAME1);
    assertTrue(oldModificationDate < modificationDate);
    oldModificationDate = modificationDate;
    modificationDate = storage.getProjectDateModified(USER_ID, projectId);
    assertEquals(oldModificationDate, modificationDate);
    oldModificationDate = modificationDate;
  }

  public void testAddRemoveFile() throws BlocksTruncatedException {
    final String USER_ID = "1200";
    storage.getUser(USER_ID);
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
    storage.getUser(USER_ID);
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
    storage.getUser(USER_ID);
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
    storage.getUser(USER_ID);
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
    storage.getUser(USER_ID);
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
    assertTrue(storage.isBlobFile(projectId, ASSET_FILE_NAME1));
    assertTrue(storage.isBlobFile(projectId, APK_FILE_NAME1));
    assertTrue(!storage.isBlobFile(projectId, BLOCK_FILE_NAME)); // small block files now in datastore

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
      @Override
      boolean useBlobstoreForFile(String fileName, int length) {
        return false;
      }
    };

    final String USER_ID = "1310";
    oldStyleStorage.getUser(USER_ID);
    long projectId = createProject(
        USER_ID, PROJECT_NAME, YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
        FORM_QUALIFIED_NAME);
    oldStyleStorage.addSourceFilesToProject(USER_ID, projectId, false, BLOCK_FILE_NAME);
    oldStyleStorage.uploadRawFile(projectId, BLOCK_FILE_NAME, USER_ID, true, BLOCK_FILE_CONTENT);
    assertTrue(Arrays.equals(BLOCK_FILE_CONTENT,
                                       oldStyleStorage.downloadRawFile(
                                           USER_ID, projectId, BLOCK_FILE_NAME)));
    assertFalse(oldStyleStorage.isBlobFile(projectId, BLOCK_FILE_NAME));

    // Test that we can still get the content with an ordinary storage object
    assertTrue(Arrays.equals(BLOCK_FILE_CONTENT,
                                       storage.downloadRawFile(
                                           USER_ID, projectId, BLOCK_FILE_NAME)));
    // Test that ordinary storage objects will still store the data in blobstore
//    storage.uploadRawFile(projectId, BLOCK_FILE_NAME, USER_ID, BLOCK_FILE_CONTENT);
//    assertTrue(storage.isBlobFile(projectId, BLOCK_FILE_NAME)); // small block files go to datastore now
 }

  public void testGetProject() {
    final String USER_ID = "1400";
    storage.getUser(USER_ID);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    ProjectData result = storage.getProject(projectId);
    assertEquals(projectId, result.id.longValue());
    assertEquals(PROJECT_NAME, result.name);
    assertEquals(FAKE_PROJECT_TYPE, result.type);
  }

  public void testGetProject_withNonexistentProject() {
    final String USER_ID = "1500";
    storage.getUser(USER_ID);
    long projectId = createProject(USER_ID, PROJECT_NAME, FAKE_PROJECT_TYPE, FORM_QUALIFIED_NAME);
    long nonExistentProjectId = (projectId + 10);
    ProjectData result = storage.getProject(nonExistentProjectId);
    assertNull(result);
  }

  public void testWrongUserThrowsException() throws Exception {
    final String USER_ID = "1600";
    final String USER_ID2 = "1700";
    createUserFiles(USER_ID, storage);

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

    FailingJobObjectifyStorageIo(int failingRun, FileService fileService) {
      super(fileService);
      this.failingRun = failingRun;
      run = 0;
    }

    @Override
    void runJobWithRetries(JobRetryHelper job) throws ObjectifyException {
      ++run;
      if (run != failingRun) {
        super.runJobWithRetries(job);
      } else {
        throw new ObjectifyException("job failed (on purpose)");
      }
    }
    
    @Override
    protected void deleteBlobstoreFile(String blobstorePath) {
      super.deleteBlobstoreFile(blobstorePath);
      numDeletedBlobs++;
    }
    
    int numBlobsDeleted() {
      return numDeletedBlobs;
    }
  }
  
  /* 
   * Fail on Nth blob create, where N is the value of the failingBlobNum
   * argument to the constructor. Also allows counting attempted blob creations.
   */
  private class FailingBlobFileService implements FileService {
    private final int failingBlobNum;
    private FileService fileService = FileServiceFactory.getFileService();
    private List<AppEngineFile> blobs = new ArrayList<AppEngineFile>();
    private RuntimeException runtimeException;
    private int numBlobsCreated;
    
    // fail on creation of blob number failingBlobNum with an IOException
    FailingBlobFileService(int failingBlobNum) {
      super();
      this.failingBlobNum = failingBlobNum;
      numBlobsCreated = 0;
      runtimeException = null;
    }

    // fail on creation of blob number failingBlobNum with runtime exception e
    FailingBlobFileService(int failingBlobNum, RuntimeException e) {
      super();
      this.failingBlobNum = failingBlobNum;
      numBlobsCreated = 0;
      runtimeException = e;
    }
    
    @Override
    public AppEngineFile createNewBlobFile(String arg0) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AppEngineFile createNewBlobFile(String arg0, String arg1) throws IOException {
      numBlobsCreated++;
      if (numBlobsCreated == failingBlobNum) {
        if (runtimeException != null) {
          throw runtimeException;
        } else {
          throw new IOException("This one is supposed to fail");
        }
      } else {
        AppEngineFile newBlob = fileService.createNewBlobFile(arg0, arg1);
        blobs.add(newBlob);
        return newBlob;
      }
    }

    @Override
    public AppEngineFile getBlobFile(BlobKey arg0) {
      throw new UnsupportedOperationException();
    }

    @Override
    public BlobKey getBlobKey(AppEngineFile arg0) {
      return fileService.getBlobKey(arg0);
    }

    @Override
    public FileReadChannel openReadChannel(AppEngineFile arg0, boolean arg1)
        throws FileNotFoundException, LockException, IOException {
      return fileService.openReadChannel(arg0, arg1);
    }

    @Override
    public FileWriteChannel openWriteChannel(AppEngineFile arg0, boolean arg1)
        throws FileNotFoundException, FinalizationException, LockException, IOException {
      return fileService.openWriteChannel(arg0, arg1);
    }
    
    // note: returns number of attempted creates, not just successful ones
    public int numBlobsCreated() {
      return numBlobsCreated;
    }

    @Override
    public AppEngineFile createNewGSFile(GSFileOptions arg0) throws IOException {
      return null;
    }

    @Override
    public RecordReadChannel openRecordReadChannel(AppEngineFile arg0, boolean arg1)
        throws FileNotFoundException, LockException, IOException {
      return null;
    }

    @Override
    public RecordWriteChannel openRecordWriteChannel(AppEngineFile arg0, boolean arg1)
        throws FileNotFoundException, FinalizationException, LockException, IOException {
      return null;
    }

    @Override
    public void delete(AppEngineFile... arg0) throws IOException {
    }

    @Override
    public String getDefaultGsBucketName() throws IOException {
      return null;
    }

    @Override
    public FileStat stat(AppEngineFile arg0) throws IOException {
      return null;
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
