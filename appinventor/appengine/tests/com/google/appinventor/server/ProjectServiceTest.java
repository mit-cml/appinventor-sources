// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import static com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters.YOUNG_ANDROID_FORM_NAME;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.server.encryption.KeyczarEncryptor;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.project.FileDescriptor;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static junit.framework.Assert.*;
import junitx.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for {@link ProjectServiceImpl}.
 *
 */
@PowerMockIgnore({"javax.crypto.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ LocalUser.class })
public class ProjectServiceTest {
  // If ProjectServiceTest (which uses PowerMock.mockStatic) extends LocalDatastoreTestCase, then
  // it will fail with Ant version 1.8.2.
  private final LocalDatastoreTestCase helper = LocalDatastoreTestCase.createHelper();

  private static final String USER_ID_ONE = "id1";
  private static final String USER_ID_TWO = "id2";

  private static final String USER_EMAIL_ONE = "noname1@domain.com";
  private static final String USER_EMAIL_TWO = "noname2@domain.com";

  private static final String PROJECT1_NAME = "Project1";
  private static final String PROJECT2_NAME = "Project2";
  private static final String PROJECT3_NAME = "Project3";

  private static final String PACKAGE_BASE = "com.domain.noname.";

  private static final String YOUNG_ANDROID_COMMENT = ";;; Cat In The Hat by Dr. Seuss\n";
  private static final String YOUNG_ANDROID_COMMENT1 = ";;; Don't Hop on Pop by Dr. Seuss\n";
  private static final String YOUNG_ANDROID_COMMENT2 = ";;; Green Eggs and Ham by Dr. Seuss\n";

  private static final int NUMBER_OF_SIMULTANEOUS_NEW_PROJECTS = 50;

  private static final String YOUNG_ANDROID_PROJECT_SCM_SOURCE = "\n" +
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
      "\"Uuid\":\"0\",\"Title\":\"Screen1\",\"AppName\":\"noname\",\"$Components\":[" +
      "{\"$Name\":\"Button1\",\"$Type\":\"Button\",\"Uuid\":\"123\",\"Text\":\"Button1\"," +
      "\"Width\":\"80\"}," +
      "{\"$Name\":\"Label1\",\"$Type\":\"Label\",\"Uuid\":\"-456\",\"Text\":\"Liz\"}" +
      "]}}\n" +
      "|#";

  private StorageIo storageIo;
  private ProjectServiceImpl projectServiceImpl;   // for USER_ID_ONE
  private ProjectServiceImpl projectServiceImpl2;  // for USER_ID_TWO
  private Map<String, ProjectServiceImpl> projectServiceImpls;

  private LocalUser localUserMock;

  public static final String KEYSTORE_ROOT_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/build/war/";  // must end with a slash

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
    KeyczarEncryptor.rootPath.setForTest(KEYSTORE_ROOT_PATH);
  }

  void do_init() {
    storageIo.getUser(USER_ID_ONE, USER_EMAIL_ONE);
    projectServiceImpl = new ProjectServiceImpl();
    storageIo.getUser(USER_ID_TWO, USER_EMAIL_TWO);
    projectServiceImpl2 = new ProjectServiceImpl();
    projectServiceImpls = Maps.newHashMap();
    projectServiceImpls.put(USER_ID_ONE, projectServiceImpl);
    projectServiceImpls.put(USER_ID_TWO, projectServiceImpl2);
    localUserMock.setSessionId("test-session");
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
    PowerMock.resetAll();
  }

  private void checkModificationDateMatchesStored(long oldModificationDate, String userId,
      long projectId) throws Exception {
    long modificationDate = storageIo.getProjectDateModified(userId, projectId);
    assertEquals(oldModificationDate, modificationDate);
  }

  @Test
  public void testProjectService() throws Exception {
    expect(localUserMock.getUserId()).andReturn(USER_ID_ONE).times(2);
    expect(localUserMock.getUserId()).andReturn(USER_ID_TWO).times(2);
    expect(localUserMock.getUserId()).andReturn(USER_ID_ONE).times(4);
    expect(localUserMock.getUserId()).andReturn(USER_ID_TWO).times(2);
    expect(localUserMock.getUserId()).andReturn(USER_ID_ONE).times(11);
    PowerMock.replayAll();
    do_init();

    // Create a new young android project
    NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(
        PACKAGE_BASE + PROJECT1_NAME);
    long user1Project1 = projectServiceImpl.newProject(
        YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, PROJECT1_NAME, params).getProjectId();
    long[] user1Projects = projectServiceImpl.getProjects();
    assertTrue(user1Projects.length == 1 && user1Projects[0] == user1Project1);

    // Create another young android project with the same name for another user
    long user2Project1 = projectServiceImpl2.newProject(
        YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, PROJECT1_NAME, params).getProjectId();
    long[] user2Projects = projectServiceImpl2.getProjects();
    assertEquals(1, user2Projects.length);
    assertEquals(user2Project1, user2Projects[0]);

    // Change the source file in the second users project and make sure only that file
    // got changed (and not the one of the same name owned by the first user)
    ProjectRootNode user1Project1Root = projectServiceImpl.getProject(user1Project1);
    String user1Project1Source1FileId = findFileIdByName(user1Project1Root,
        YOUNG_ANDROID_FORM_NAME + YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION);
    assertNotNull(user1Project1Source1FileId);

    String user1Project1Source1 = projectServiceImpl.load(user1Project1,
        user1Project1Source1FileId);

    long oldModificationDate = storageIo.getProjectDateModified(USER_ID_ONE, user1Project1);
    long modificationDate = projectServiceImpl.save("test-session", user1Project1, user1Project1Source1FileId,
        YOUNG_ANDROID_COMMENT + user1Project1Source1);
    assertEquals(YOUNG_ANDROID_COMMENT + user1Project1Source1,
        projectServiceImpl.load(user1Project1, user1Project1Source1FileId));
    assertTrue(oldModificationDate <= modificationDate);
    checkModificationDateMatchesStored(modificationDate, USER_ID_ONE, user1Project1);
    oldModificationDate = modificationDate;

    ProjectRootNode user2Project1Root = projectServiceImpl2.getProject(user2Project1);
    String user2Project1Source1FileId = findFileIdByName(user2Project1Root,
        YOUNG_ANDROID_FORM_NAME + YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION);
    assertNotNull(user2Project1Source1FileId);

    Assert.assertNotEquals(YOUNG_ANDROID_COMMENT, projectServiceImpl2.load(user2Project1,
        user2Project1Source1FileId));

    // Create another new project for user1

    params = new NewYoungAndroidProjectParameters(
        PACKAGE_BASE + PROJECT2_NAME);
    long user1Project2 = projectServiceImpl.newProject(
        YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, PROJECT2_NAME, params).getProjectId();
    user1Projects = projectServiceImpl.getProjects();
    assertTrue(user1Projects.length == 2);
    ProjectRootNode user1Project2Root = projectServiceImpl.getProject(user1Project2);
    String user1Project2Source1FileId = findFileIdByName(user1Project2Root,
        YOUNG_ANDROID_FORM_NAME + YoungAndroidSourceAnalyzer.FORM_PROPERTIES_EXTENSION);
    assertNotNull(user1Project2Source1FileId);

    // Load and save multiple files

    String u1p1s1 =
        projectServiceImpl.load(user1Project1, user1Project1Source1FileId) + YOUNG_ANDROID_COMMENT1;
    String u1p2s1 =
        projectServiceImpl.load(user1Project2, user1Project2Source1FileId) + YOUNG_ANDROID_COMMENT2;

    List<FileDescriptorWithContent> filesWithContent = Lists.newArrayList();
    filesWithContent.add(new FileDescriptorWithContent(user1Project1, user1Project1Source1FileId,
        u1p1s1));
    filesWithContent.add(new FileDescriptorWithContent(user1Project2, user1Project2Source1FileId,
        u1p2s1));
    checkModificationDateMatchesStored(oldModificationDate, USER_ID_ONE, user1Project1);
    modificationDate = projectServiceImpl.save("test-session", filesWithContent);
    assertTrue(oldModificationDate <= modificationDate);
    checkModificationDateMatchesStored(modificationDate, USER_ID_ONE, user1Project2);
    oldModificationDate = modificationDate;

    List<FileDescriptor> files = Lists.newArrayList();
    files.add(new FileDescriptor(user1Project1, user1Project1Source1FileId));
    files.add(new FileDescriptor(user1Project2, user1Project2Source1FileId));

    filesWithContent = projectServiceImpl.load(files);
    assertEquals(files.size(), filesWithContent.size());
    FileDescriptorWithContent fileWithContent = findFileDescriptorWithContent(filesWithContent,
        user1Project1, user1Project1Source1FileId);
    assertNotNull(fileWithContent);
    assertEquals(u1p1s1, fileWithContent.getContent());
    fileWithContent = findFileDescriptorWithContent(filesWithContent, user1Project2,
        user1Project2Source1FileId);
    assertNotNull(fileWithContent);
    assertEquals(u1p2s1, fileWithContent.getContent());

    oldModificationDate = storageIo.getProjectDateModified(USER_ID_ONE, user1Project1);
    modificationDate = projectServiceImpl.deleteFile("test-session", user1Project1, user1Project1Source1FileId);
    assertTrue(oldModificationDate <= modificationDate);
    checkModificationDateMatchesStored(modificationDate, USER_ID_ONE, user1Project1);
    oldModificationDate = modificationDate;

    // Delete the projects of the first user
    projectServiceImpl.deleteProject(user1Project1);
    projectServiceImpl.deleteProject(user1Project2);
    user1Projects = projectServiceImpl.getProjects();
    assertTrue(user1Projects.length == 0);
    PowerMock.verifyAll();
  }

  @Test
  public void testNewYoungAndroidProject() throws Exception {
    // Since only USER_ID_ONE is used, we don't care how many times
    // getUserId is called
    expect(localUserMock.getUserId()).andReturn(USER_ID_ONE).anyTimes();
    PowerMock.replayAll();
    do_init();

    // First create a Young Android project.
    NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(
        PACKAGE_BASE + PROJECT1_NAME);
    long yaProject =
        projectServiceImpl.newProject(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
                                      PROJECT1_NAME, params).getProjectId();

    // Check the contents of each file in the new project.
    Map<String, String> expectedYaFiles = new HashMap<String, String>();
    expectedYaFiles.put("src/com/domain/noname/Project1/Screen1.bky", "");
    expectedYaFiles.put("src/com/domain/noname/Project1/Screen1.yail", "");
    expectedYaFiles.put("youngandroidproject/project.properties",
        "main=com.domain.noname.Project1.Screen1\n" +
        "name=Project1\n" +
        "assets=../assets\n" +
        "source=../src\n" +
        "build=../build\n");
    expectedYaFiles.put("src/com/domain/noname/Project1/Screen1.scm",
        "#|\n$JSON\n" +
        "{\"YaVersion\":\"" + YaVersion.YOUNG_ANDROID_VERSION + "\",\"Source\":\"Form\"," +
        "\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
        "\"$Version\":\"" + YaVersion.FORM_COMPONENT_VERSION + "\",\"Uuid\":\"0\"," +
        "\"Title\":\"Screen1\","+"\"AppName\":\"noname\"}}\n|#");
    assertEquals(expectedYaFiles, getTextFiles(USER_ID_ONE, yaProject));
    assertTrue(getNonTextFiles(USER_ID_ONE, yaProject).isEmpty());

    checkUserProjects(projectServiceImpl.getProjectInfos(),
        new UserProject(yaProject, PROJECT1_NAME,
            YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, System.currentTimeMillis(), System.currentTimeMillis(), 0L, 0L));
    PowerMock.verifyAll();
  }

  @Test
  public void testCopyProject() throws Exception {
    // Since only USER_ID_ONE is used in this test, we don't care how
    // many times getUser or getUserId are called; they'll always
    // return the same result
    expect(localUserMock.getUserId()).andReturn(USER_ID_ONE).anyTimes();
    expect(localUserMock.getUser()).andReturn(storageIo.getUser(USER_ID_ONE)).anyTimes();
    PowerMock.replayAll();
    do_init();

    // First create a Young Android project.
    long yaProject1 = getBuildableYoungAndroidProjectId(USER_ID_ONE, PROJECT1_NAME);
    // Check the contents of each file in the new project.
    Map<String, String> expectedYaFiles1 = new HashMap<String, String>();
    expectedYaFiles1.put("src/com/domain/noname/Project1/Screen1.bky", "");
    expectedYaFiles1.put("src/com/domain/noname/Project1/Screen1.yail", "");
    expectedYaFiles1.put("youngandroidproject/project.properties",
        "main=com.domain.noname.Project1.Screen1\n" +
        "name=Project1\n" +
        "assets=../assets\n" +
        "source=../src\n" +
        "build=../build\n");
    expectedYaFiles1.put("src/com/domain/noname/Project1/Screen1.scm",
        YOUNG_ANDROID_PROJECT_SCM_SOURCE);
    assertEquals(expectedYaFiles1, getTextFiles(USER_ID_ONE, yaProject1));
    assertTrue(getNonTextFiles(USER_ID_ONE, yaProject1).isEmpty());
    // No user files yet (e.g. the keystore)
    assertTrue(getUserFiles(USER_ID_ONE).isEmpty());
    UserProject uproject = storageIo.getUserProject(USER_ID_ONE, yaProject1);
    long project1CreationDate = uproject.getDateCreated();
    long project1ModificationDate = uproject.getDateModified();
    assertTrue(project1ModificationDate >= project1CreationDate);

    // Make a copy of project 1.
    long yaProject2 = projectServiceImpl.copyProject(yaProject1, PROJECT2_NAME).
        getProjectId();
    // Check the contents of each file in the new project.
    Map<String, String> expectedYaFiles2 = new HashMap<String, String>();
    expectedYaFiles2.put("src/com/domain/noname/Project2/Screen1.bky", "");
    expectedYaFiles2.put("src/com/domain/noname/Project2/Screen1.yail", "");
    expectedYaFiles2.put("youngandroidproject/project.properties",
        "main=appinventor.ai_noname1.Project2.Screen1\n" +
        "name=Project2\n" +
        "assets=../assets\n" +
        "source=../src\n" +
        "build=../build\n" +
        "versioncode=1\n" +
        "versionname=1.0\n" +
        "useslocation=false\n" +
        "aname=Project1\n" +
        "sizing=Fixed\n");
    expectedYaFiles2.put("src/com/domain/noname/Project2/Screen1.scm",
        YOUNG_ANDROID_PROJECT_SCM_SOURCE);
    assertEquals(expectedYaFiles2, getTextFiles(USER_ID_ONE, yaProject2));
    assertTrue(getNonTextFiles(USER_ID_ONE, yaProject2).isEmpty());
    assertTrue(getUserFiles(USER_ID_ONE).isEmpty());
    UserProject uproject1 = storageIo.getUserProject(USER_ID_ONE, yaProject2);
    long project1CopyCreationDate = uproject1.getDateCreated();
    long project1CopyModificationDate = uproject1.getDateModified();
    assertTrue(project1CopyCreationDate > project1CreationDate);
    assertTrue(project1CopyCreationDate > project1ModificationDate);
    assertTrue(project1CopyModificationDate >= project1CopyCreationDate);
    PowerMock.verifyAll();
  }

  /**
   * Creates a new Young Android project with non-empty source
   * in the test storage instance and returns its project id.
   *
   * @param projectName name of the Young Android project.
   * @return the project id of the Young Android project in the test storage
   * instance.
   */
  private long getBuildableYoungAndroidProjectId(String userId, String projectName) {
    NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(
        PACKAGE_BASE + projectName);
    ProjectServiceImpl impl = projectServiceImpls.get(userId);

    long projectId =
        impl.newProject(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
                        projectName, params).getProjectId();

    String scmFileId = "src/com/domain/noname/" + projectName + "/"
        + YOUNG_ANDROID_FORM_NAME + ".scm";
    try {
      storageIo.uploadFile(projectId, scmFileId,
          userId, YOUNG_ANDROID_PROJECT_SCM_SOURCE, StorageUtil.DEFAULT_CHARSET);
    } catch (BlocksTruncatedException e) {
      // Won't happen
    }

    return projectId;
  }

  @Test
  public void testCreateManyYoungAndroidProjects() throws Exception {
    // Since only USER_ID_ONE is used in this test, we don't care how
    // many times getUser or getUserId are called; they'll always
    // return the same result
    expect(localUserMock.getUserId()).andReturn(USER_ID_ONE).anyTimes();
    expect(localUserMock.getUser()).andReturn(storageIo.getUser(USER_ID_ONE)).anyTimes();
    PowerMock.replayAll();
    do_init();

    List<Thread> threads = Lists.newArrayList();
    final AtomicInteger ready = new AtomicInteger();
    final Object start = new Object();
    final AtomicInteger successes = new AtomicInteger();
    final AtomicInteger failures = new AtomicInteger();
    int numThreads = NUMBER_OF_SIMULTANEOUS_NEW_PROJECTS;

    for (int i = 0; i < numThreads; i++) {
      final String projectName = "Project" + i;
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          // We need to set up the LocalServiceTestHelper here because
          // otherwise each thread tries to access the real storage.
          // TODO(user): Does this test still test the right
          // thing, given that?
          helper.setUpThread();

          // The first thing each thread does is signal that it is ready and then wait for the
          // start notification.
          synchronized (start) {
            ready.incrementAndGet();
            try {
              start.wait();
            } catch (InterruptedException e) {
              // do nothing
            }
          }

          NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(
              PACKAGE_BASE + projectName);
          long projectId =
              projectServiceImpl.newProject(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
                  projectName, params).getProjectId();
          if (projectId < 0) {
            failures.incrementAndGet();
          } else {
            successes.incrementAndGet();
          }
        }
      });
      threads.add(t);
      t.start();
    }

    // Tell all threads to start.
    // We can't enter the synchronized (start) block until all the threads have told us they are
    // ready. Otherwise, a thread could miss the notification and wait forever.
    while (ready.get() < numThreads) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        // do nothing
      }

    }
    synchronized (start) {
      start.notifyAll();
    }

    // Wait for all threads to finish.
    for (Thread t : threads) {
      t.join();
    }

    assertEquals(numThreads, successes.get());
    assertEquals(0, failures.get());
    PowerMock.verifyAll();
  }

  @Test
  public void testLoadAndStoreProjectSettings() throws Exception {
    // Since only USER_ID_ONE is used in this test, we don't care how
    // many times getUser or getUserId are called; they'll always
    // return the same result
    expect(localUserMock.getUserId()).andReturn(USER_ID_ONE).anyTimes();
    expect(localUserMock.getUser()).andReturn(storageIo.getUser(USER_ID_ONE)).anyTimes();
    PowerMock.replayAll();
    do_init();

    NewYoungAndroidProjectParameters params = new NewYoungAndroidProjectParameters(
        PACKAGE_BASE + PROJECT1_NAME);
    long projectId = projectServiceImpl.newProject(
        YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, PROJECT1_NAME, params).getProjectId();

    String loadedSettings = projectServiceImpl.loadProjectSettings(projectId);
    assertEquals(
        "{\"" + SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS + "\":" +
        "{\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON + "\":\"\",\"" +
        SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE + "\":\"1\",\"" +
        SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME + "\":\"1.0\",\"" +
        SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION + "\":\"false\",\"" +
        SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME + "\":\"Project1\",\"" +
        SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING + "\":\"Fixed\"}}",
        loadedSettings);

    String storedSettings =
        "{\"" + SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS + "\":" +
        "{\"" + SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON + "\":\"KittyIcon.png\"}}";
    projectServiceImpl.storeProjectSettings("test-session", projectId, storedSettings);
    loadedSettings = projectServiceImpl.loadProjectSettings(projectId);
    assertEquals(storedSettings, loadedSettings);
    PowerMock.verifyAll();
  }

  private Map<String, String> getTextFiles(String userId, long projectId) {
    Map<String, String> textFiles = new HashMap<String, String>();
    for (String fileId : storageIo.getProjectSourceFiles(userId, projectId)) {
      if (StorageUtil.isTextFile(fileId)) {
        // TODO(user): We should get rid of DEFAULT_CHARSET and use UTF-8 everywhere.
        textFiles.put(fileId,
                      storageIo.downloadFile(userId, projectId, fileId,
                                             StorageUtil.DEFAULT_CHARSET));
      }
    }
    return textFiles;
  }

  private Map<String, byte[]> getNonTextFiles(String userId, long projectId) {
    Map<String, byte[]> nonTextFiles = new HashMap<String, byte[]>();
    for (String fileId : storageIo.getProjectSourceFiles(userId, projectId)) {
      if (!StorageUtil.isTextFile(fileId)) {
        nonTextFiles.put(fileId,
                         storageIo.downloadRawFile(userId, projectId, fileId));
      }
    }
    return nonTextFiles;
  }

  private Map<String, byte[]> getUserFiles(String userId) {
    Map<String, byte[]> userFiles = new HashMap<String, byte[]>();
    for (String fileId : storageIo.getUserFiles(userId)) {
      userFiles.put(fileId,
                    storageIo.downloadRawUserFile(userId, fileId));
    }
    return userFiles;
  }

  private static String findFileIdByName(ProjectNode parent, String name) {
    if (parent.getName().equals(name)) {
      return parent.getFileId();
    }

    for (ProjectNode node : parent.getChildren()) {
      String fileId = findFileIdByName(node, name);
      if (fileId != null) {
        return fileId;
      }
    }

    return null;
  }

  private static FileDescriptorWithContent findFileDescriptorWithContent(
      List<FileDescriptorWithContent> filesWithContent, long projectId, String fileId) {
    for (FileDescriptorWithContent fileWithContent : filesWithContent) {
      if (fileWithContent.getProjectId() == projectId &&
          fileWithContent.getFileId().equals(fileId)) {
        return fileWithContent;
      }
    }
    return null;
  }

  private void checkUserProjects(List<UserProject> actual, UserProject... expected)
      throws Exception {
    if (actual.size() != expected.length) {
      fail("expected <" + expected.length + "> UserProjects but was:<" + actual.size() + '>');
    }
    // Build a map from project id to UserProject for the actual projects so we won't nested
    // for loops below.
    // Also, build a String to describe the actual UserProjects that we can use in failure messages.
    Map<Long, UserProject> actualByProjectId = Maps.newHashMap();
    StringBuilder actualDesc = new StringBuilder();
    String delimiter = "";
    for (UserProject actualUserProject : actual) {
      actualByProjectId.put(actualUserProject.getProjectId(), actualUserProject);
      actualDesc.append(delimiter).append(getUserProjectDesc(actualUserProject));
      delimiter = ",";
    }
    for (UserProject expectedUserProject : expected) {
      long projectId = expectedUserProject.getProjectId();
      UserProject actualUserProject = actualByProjectId.get(projectId);
      if (actualUserProject == null) {
        fail("expected to contain:<" + getUserProjectDesc(expectedUserProject) +
            "> but was:<" + actualDesc + '>');
      }
      if (!Objects.equal(expectedUserProject.getProjectName(),
          actualUserProject.getProjectName())) {
        fail("expected project name:<" + expectedUserProject.getProjectName() + "> but was:<" +
            actualUserProject.getProjectName() + '>');
      }
      if (!Objects.equal(expectedUserProject.getProjectType(),
          actualUserProject.getProjectType())) {
        fail("expected project type:<" + expectedUserProject.getProjectType() + "> but was:<" +
            actualUserProject.getProjectType() + '>');
      }

      // Expected user project date was created after actual user project
      if (expectedUserProject.getDateCreated() < actualUserProject.getDateCreated()) {
        fail("expected date:<" + expectedUserProject.getDateCreated() + "> but was:<" +
            actualUserProject.getDateCreated() + '>');
      }
    }
  }

  private static String getUserProjectDesc(UserProject userProject) {
    return "UserProject(" + userProject.getProjectId() + ",\"" +
        userProject.getProjectName() + "\")";
  }
}
