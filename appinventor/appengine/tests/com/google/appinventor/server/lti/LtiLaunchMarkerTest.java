// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;

import org.json.JSONObject;

/**
 * Tests what the launch leaves on the project it gives a learner.
 *
 * <p>The marker decides whether Submit to LMS appears in the Project menu. The launch writes
 * it on the project it forks, and submit strips it from the frozen copy so a teacher
 * reviewing that copy is not offered a submit action. The client reads the same two settings
 * keys, so the shape written here is the contract between the server and the menu.
 *
 * <p>The fork is also a copy into another account, so what it must not carry across is
 * covered here too, on the launch path itself rather than on the tidy in isolation.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiLaunchMarkerTest extends LocalDatastoreTestCase {

  private static final String OWNER = "learner-1";

  private StorageIo storageIo;
  private LtiLaunchServlet launchServlet;
  private LtiSubmitServlet submitServlet;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storageIo = StorageIoInstanceHolder.getInstance();
    storageIo.getUser(OWNER, "learner1@example.com");
    launchServlet = new LtiLaunchServlet();
    submitServlet = new LtiSubmitServlet();
  }

  private long createProject(String name) {
    return createProject(OWNER, name, false);
  }

  /** Optionally with the Yail a build leaves behind, which a copy must not carry across. */
  private long createProject(String ownerId, String name, boolean withYail) {
    Project project = new Project(name);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    project.addTextFile(new TextFile(YoungAndroidProjectService.PROJECT_PROPERTIES_FILE_NAME,
        "main=appinventor.ai_test." + name + ".Screen1\nname=" + name + "\n"));
    project.addTextFile(new TextFile("src/appinventor/ai_test/" + name + "/Screen1.scm", "{}"));
    if (withYail) {
      project.addTextFile(new TextFile("src/appinventor/ai_test/" + name + "/Screen1.yail",
          "(set-and-coerce-property! 'FirebaseDB1 'FirebaseToken \"secret\")"));
    }
    return storageIo.createProject(ownerId, project, "{}");
  }

  private static boolean hasYail(java.util.List<String> fileNames) {
    for (String fileName : fileNames) {
      if (fileName.endsWith(".yail")) {
        return true;
      }
    }
    return false;
  }

  /**
   * The project a learner is given is a copy of the teacher's template into the learner's own
   * account, so it has to arrive without the Yail. This drives the fork rather than the tidy
   * on its own, since the tidy only protects anything if the fork actually runs it.
   */
  public void testTheForkedProjectLeavesTheTemplateYailBehind() throws Exception {
    String teacher = "teacher-1";
    storageIo.getUser(teacher, "teacher@example.com");
    long templateId = createProject(teacher, "Exercise_1", true);
    assertTrue("the template has Yail to begin with",
        hasYail(storageIo.getProjectSourceFiles(teacher, templateId)));

    long forked = launchServlet.forkNewProject(
        storageIo.getUser(OWNER), templateId, "Exercise_1");

    assertFalse("the learner copy must not carry the template Yail",
        hasYail(storageIo.getProjectSourceFiles(OWNER, forked)));
    assertTrue("the template keeps its own",
        hasYail(storageIo.getProjectSourceFiles(teacher, templateId)));
  }

  /** Reads back the Young Android settings group the client menu looks in. */
  private JSONObject youngAndroidSettings(long projectId) {
    String raw = storageIo.loadProjectSettings(OWNER, projectId);
    assertNotNull("the project should carry settings once the launch has marked it", raw);
    JSONObject group =
        new JSONObject(raw).optJSONObject(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);
    assertNotNull("the marker belongs in the Young Android settings group", group);
    return group;
  }

  /**
   * A project an assignment no longer points at cannot be handed in.
   *
   * <p>A learner who trashes their project and launches again is given a new one, and the
   * assignment points at that. If they then take the old one back out of the trash, handing it
   * in would tell the platform they had submitted while a review of the assignment opened the
   * new project and found nothing there.
   */
  public void testARetiredProjectCanNoLongerBeSubmitted() {
    long replaced = createProject("Exercise_1");
    launchServlet.markLtiLaunched(OWNER, replaced);
    LtiGradeContext.put(replaced, OWNER, "http://localhost:8080",
        "http://localhost:8080/lineitems/1/lineitem", "platform-sub-1");
    assertNotNull("it is submittable to begin with", LtiGradeContext.get(replaced));

    launchServlet.clearLtiLaunched(OWNER, replaced);
    LtiGradeContext.revoke(replaced, OWNER);

    assertNull("the server must refuse a submission from it", LtiGradeContext.get(replaced));
    assertFalse("and the client must stop offering the menu item",
        youngAndroidSettings(replaced).has(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED));
  }

  /**
   * An assignment whose template cannot be copied fails the launch and leaves the learner with
   * nothing, rather than quietly giving them a blank project. A blank project would put this
   * learner somewhere different to the rest of the class with nothing to say so, while a
   * failure shows the retry page and reaches the log.
   */
  public void testAnUncopyableTemplateLeavesTheLearnerWithNoProject() throws Exception {
    long missingTemplateId = 987654321L;
    assertNull("the template really is unreadable", storageIo.getProjectUserId(missingTemplateId));
    int before = storageIo.getProjects(OWNER).size();

    try {
      launchServlet.forkNewProject(storageIo.getUser(OWNER), missingTemplateId, "Exercise_1");
      fail("expected the launch to fail rather than hand over a project");
    } catch (Exception expected) {
      // expected
    }

    assertEquals("no project may be created when the template cannot be copied",
        before, storageIo.getProjects(OWNER).size());
  }

  /**
   * The launch writes the exact string the client compares against. The client accepts
   * only "true", so anything else here would silently hide the menu item.
   */
  public void testLaunchWritesTheMarkerTheMenuReads() {
    long projectId = createProject("Exercise_1");

    launchServlet.markLtiLaunched(OWNER, projectId);

    assertEquals("true", youngAndroidSettings(projectId)
        .optString(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED));
  }

  /**
   * Submit copies the project into a reserved account and strips the marker from that
   * copy, so the teacher opening it read only is not offered a submit action.
   */
  public void testSubmitStripsTheMarkerFromTheFrozenCopy() {
    long projectId = createProject("Exercise_1");
    launchServlet.markLtiLaunched(OWNER, projectId);

    submitServlet.removeLaunchMarker(OWNER, projectId);

    assertFalse("the frozen copy must not look like an assignment project",
        youngAndroidSettings(projectId)
            .has(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED));
  }

  /** Marking a project must not discard the settings it already carries. */
  public void testMarkingKeepsTheSettingsAlreadyOnTheProject() {
    long projectId = createProject("Exercise_1");
    storageIo.storeProjectSettings(OWNER, projectId,
        new JSONObject().put(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            new JSONObject().put(SettingsConstants.YOUNG_ANDROID_SETTINGS_LAST_OPENED, "Screen2"))
            .toString());

    launchServlet.markLtiLaunched(OWNER, projectId);

    JSONObject group = youngAndroidSettings(projectId);
    assertEquals("true",
        group.optString(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED));
    assertEquals("Screen2",
        group.optString(SettingsConstants.YOUNG_ANDROID_SETTINGS_LAST_OPENED));
  }

  /**
   * Stripping runs on every submission snapshot, including a copy of a project that
   * never carried the marker, so it has to leave such a project alone rather than fail.
   */
  public void testStrippingAProjectWithoutTheMarkerLeavesItAlone() {
    long projectId = createProject("Plain_project");
    storageIo.storeProjectSettings(OWNER, projectId,
        new JSONObject().put(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            new JSONObject().put(SettingsConstants.YOUNG_ANDROID_SETTINGS_LAST_OPENED, "Screen1"))
            .toString());

    submitServlet.removeLaunchMarker(OWNER, projectId);

    JSONObject group = youngAndroidSettings(projectId);
    assertFalse(group.has(SettingsConstants.YOUNG_ANDROID_SETTINGS_LTI_LAUNCHED));
    assertEquals("Screen1",
        group.optString(SettingsConstants.YOUNG_ANDROID_SETTINGS_LAST_OPENED));
  }
}
