// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData;

/**
 * Remembers, per forked assignment project, the platform issuer and the
 * Assignment and Grade Services line item from its launch, so that submitting
 * that project posts to that assignment's own line item rather than whichever
 * assignment the student launched last.
 *
 * <p>Stored in the datastore through {@link StorageIo}, so a submission still
 * works after a server restart and on another server instance. The record holds
 * only the owner, the issuer, the gradebook line item reference, and the
 * platform user id, no secret.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiGradeContext {

  /** What is needed to post a score back to the platform for one project. */
  static final class Context {
    final String userId;
    final String issuer;
    final String lineItemUrl;
    final String ltiUserSub;

    Context(String userId, String issuer, String lineItemUrl, String ltiUserSub) {
      this.userId = userId;
      this.issuer = issuer;
      this.lineItemUrl = lineItemUrl;
      this.ltiUserSub = ltiUserSub;
    }
  }

  private LtiGradeContext() {}

  /** Saves the grade passback target for a project, replacing any earlier one. */
  static void put(long projectId, String userId, String issuer, String lineItemUrl,
      String ltiUserSub) {
    if (lineItemUrl == null || lineItemUrl.isEmpty()) {
      return;
    }
    StorageIoInstanceHolder.getInstance().storeLtiGradeContext(projectId, userId,
        issuer == null ? "" : issuer, lineItemUrl, ltiUserSub == null ? "" : ltiUserSub);
  }

  /**
   * Takes the grade passback target away from a project.
   *
   * <p>An assignment points at one project at a time. When a launch has to give a learner a
   * new project, the one it replaces must stop being submittable, or that older project could
   * still be handed in while a review of the assignment opens the new one and finds nothing.
   * An empty line item is what {@link #get} treats as nothing stored, so submitting from the
   * replaced project is refused the same way a plain project is.
   */
  static void revoke(long projectId, String userId) {
    StorageIoInstanceHolder.getInstance().storeLtiGradeContext(projectId,
        userId == null ? "" : userId, "", "", "");
  }

  /** Loads the grade passback target for a project, or null if none is stored. */
  static Context get(long projectId) {
    StoredData.LtiGradeContextData data =
        StorageIoInstanceHolder.getInstance().getLtiGradeContext(projectId);
    if (data == null || data.lineItemUrl == null || data.lineItemUrl.isEmpty()) {
      return null;
    }
    return new Context(data.userId == null ? "" : data.userId,
        data.issuer == null ? "" : data.issuer, data.lineItemUrl,
        data.ltiUserSub == null ? "" : data.ltiUserSub);
  }
}
