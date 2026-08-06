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
 * Tests the marker that decides whether Submit to LMS appears in the Project menu.
 * The launch writes it on the project it forks for a learner, and submit strips it
 * from the frozen copy so a teacher reviewing that copy is not offered a submit
 * action. The client reads the same two settings keys, so the shape written here
 * is the contract between the server and the menu.
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
    Project project = new Project(name);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    project.addTextFile(new TextFile(YoungAndroidProjectService.PROJECT_PROPERTIES_FILE_NAME,
        "main=appinventor.ai_test." + name + ".Screen1\nname=" + name + "\n"));
    project.addTextFile(new TextFile("src/Screen1.scm", ""));
    return storageIo.createProject(OWNER, project, "{}");
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
