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

import java.util.Date;

/**
 * Tests which project a read-only review launch is allowed to open. The review
 * arrives on the teacher's own launch but opens a learner's work, so the link
 * between the assignment, the learner, and the frozen copy is checked before
 * anything is exposed. These drive the servlet against the real storage layer
 * rather than its pure helpers, so a broken owner check fails here.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
public class LtiReviewAccessTest extends LocalDatastoreTestCase {

  private static final String ISSUER = "http://localhost:8080";
  private static final String DEPLOYMENT = "1";
  private static final String RESOURCE_LINK = "42";
  private static final String LEARNER = "learner-1";
  private static final String OTHER_LEARNER = "learner-2";
  private static final String SNAPSHOT_OWNER = "lti-snapshot-account";

  private StorageIo storageIo;
  private LtiLaunchServlet servlet;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storageIo = StorageIoInstanceHolder.getInstance();
    storageIo.getUser(LEARNER, "learner1@example.com");
    storageIo.getUser(OTHER_LEARNER, "learner2@example.com");
    storageIo.getUser(SNAPSHOT_OWNER, "snapshot@example.com");
    servlet = new LtiLaunchServlet();
  }

  /**
   * The owner of a project is read back from its properties file, the same way a
   * real Young Android project records it, so the project has to carry one for
   * the review checks to see an owner at all.
   */
  private long createProject(String ownerId, String name) {
    Project project = new Project(name);
    project.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
    project.addTextFile(new TextFile(YoungAndroidProjectService.PROJECT_PROPERTIES_FILE_NAME,
        "main=appinventor.ai_test." + name + ".Screen1\nname=" + name + "\n"));
    project.addTextFile(new TextFile("src/Screen1.scm", ""));
    return storageIo.createProject(ownerId, project, "{}");
  }

  private long review(String learnerAccountId) throws Exception {
    return servlet.reviewProjectId(learnerAccountId, ISSUER, DEPLOYMENT, RESOURCE_LINK);
  }

  /** A learner who never opened the activity has nothing for the teacher to see. */
  public void testLearnerWhoNeverStartedHasNothingToReview() throws Exception {
    assertEquals(0, review(LEARNER));
  }

  /** Before a submission the review reaches the learner's own assignment project. */
  public void testBeforeSubmitTheLearnerProjectIsOpened() throws Exception {
    long projectId = createProject(LEARNER, "Exercise_1");
    LtiResourceLinks.put(LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, projectId);
    assertEquals(projectId, review(LEARNER));
  }

  /** After a submission the review reaches the frozen copy, never the live project. */
  public void testAfterSubmitTheFrozenCopyIsOpened() throws Exception {
    long projectId = createProject(LEARNER, "Exercise_1");
    long snapshotId = createProject(SNAPSHOT_OWNER, "Snapshot_1");
    LtiResourceLinks.put(LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, projectId);
    LtiSubmission.put(projectId, LEARNER, snapshotId, SNAPSHOT_OWNER, new Date(1000L));
    assertEquals(snapshotId, review(LEARNER));
  }

  /** A later attempt whose copy failed supersedes the older one and shows the live project. */
  public void testFailedLaterCopySupersedesTheOlderOne() throws Exception {
    long projectId = createProject(LEARNER, "Exercise_1");
    long snapshotId = createProject(SNAPSHOT_OWNER, "Snapshot_1");
    LtiResourceLinks.put(LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, projectId);
    LtiSubmission.put(projectId, LEARNER, snapshotId, SNAPSHOT_OWNER, new Date(1000L));
    LtiSubmission.markUnavailable(projectId, LEARNER, new Date(2000L));
    assertEquals(projectId, review(LEARNER));
  }

  /** An assignment link that resolves to another learner's project is refused. */
  public void testLinkToAnotherLearnerProjectIsRefused() throws Exception {
    long foreign = createProject(OTHER_LEARNER, "Exercise_1");
    LtiResourceLinks.put(LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, foreign);
    try {
      review(LEARNER);
      fail("a project owned by another learner must not be exposed");
    } catch (SecurityException expected) {
      // The owner check refuses before any session is built.
    }
  }

  /** A submission recorded against another learner is refused. */
  public void testSubmissionRecordedForAnotherLearnerIsRefused() throws Exception {
    long projectId = createProject(LEARNER, "Exercise_1");
    long snapshotId = createProject(SNAPSHOT_OWNER, "Snapshot_1");
    LtiResourceLinks.put(LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, projectId);
    LtiSubmission.put(projectId, OTHER_LEARNER, snapshotId, SNAPSHOT_OWNER, new Date(1000L));
    try {
      review(LEARNER);
      fail("a submission belonging to another learner must not be exposed");
    } catch (SecurityException expected) {
      // The submission has to name the learner the review asked for.
    }
  }

  /** A frozen copy whose recorded owner is not the account holding it is refused. */
  public void testSnapshotOwnerMismatchIsRefused() throws Exception {
    long projectId = createProject(LEARNER, "Exercise_1");
    long snapshotId = createProject(SNAPSHOT_OWNER, "Snapshot_1");
    LtiResourceLinks.put(LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, projectId);
    LtiSubmission.put(projectId, LEARNER, snapshotId, OTHER_LEARNER, new Date(1000L));
    try {
      review(LEARNER);
      fail("a frozen copy whose owner does not match must not be exposed");
    } catch (SecurityException expected) {
      // The recorded owner has to still be the owner of the copy.
    }
  }

  /** Two learners on the same assignment reach only their own work. */
  public void testEachLearnerReachesOnlyTheirOwnProject() throws Exception {
    long mine = createProject(LEARNER, "Exercise_1");
    long theirs = createProject(OTHER_LEARNER, "Exercise_1_other");
    LtiResourceLinks.put(LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, mine);
    LtiResourceLinks.put(OTHER_LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, theirs);
    assertEquals(mine, review(LEARNER));
    assertEquals(theirs, review(OTHER_LEARNER));
  }

  /** An assignment the learner never joined stays out of reach even after they submit elsewhere. */
  public void testAnotherAssignmentIsNotReachable() throws Exception {
    long projectId = createProject(LEARNER, "Exercise_1");
    LtiResourceLinks.put(LEARNER, ISSUER, DEPLOYMENT, RESOURCE_LINK, projectId);
    assertEquals(0, servlet.reviewProjectId(LEARNER, ISSUER, DEPLOYMENT, "999"));
  }
}
