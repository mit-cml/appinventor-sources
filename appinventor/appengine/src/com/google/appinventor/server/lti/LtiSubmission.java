// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData;

import java.util.Date;

/**
 * Remembers the current immutable copy of an LTI assignment project. A new
 * submission replaces the state for the source project, so review can find the
 * latest submitted artifact, or learn that its copy failed, with one datastore
 * lookup.
 */
final class LtiSubmission {

  /** The recorded artifact for one source assignment project. */
  static final class Submission {
    final String userId;
    final long sourceProjectId;
    final long snapshotProjectId;
    final String snapshotOwnerId;
    final Date submittedAt;

    Submission(String userId, long sourceProjectId, long snapshotProjectId,
        String snapshotOwnerId, Date submittedAt) {
      this.userId = userId;
      this.sourceProjectId = sourceProjectId;
      this.snapshotProjectId = snapshotProjectId;
      this.snapshotOwnerId = snapshotOwnerId;
      this.submittedAt = submittedAt;
    }
  }

  private LtiSubmission() {}

  /** Records a completed copy as the current submission for its source project. */
  static void put(long sourceProjectId, String userId, long snapshotProjectId,
      String snapshotOwnerId, Date submittedAt) {
    StorageIoInstanceHolder.getInstance().storeLtiSubmission(sourceProjectId, userId,
        snapshotProjectId, snapshotOwnerId, submittedAt);
  }

  /** Makes a failed newer attempt supersede any older submitted copy. */
  static void markUnavailable(long sourceProjectId, String userId, Date submittedAt) {
    StorageIoInstanceHolder.getInstance().storeLtiSubmission(sourceProjectId, userId,
        0, "", submittedAt);
  }

  /** Loads the current submitted copy for a source project, or null if none exists. */
  static Submission get(long sourceProjectId) {
    StoredData.LtiSubmissionData data =
        StorageIoInstanceHolder.getInstance().getLtiSubmission(sourceProjectId);
    if (data == null || data.snapshotProjectId <= 0) {
      return null;
    }
    Date submittedAt = data.submittedAt == null
        ? null : new Date(data.submittedAt.getTime());
    return new Submission(data.userId == null ? "" : data.userId,
        data.sourceProjectId, data.snapshotProjectId,
        data.snapshotOwnerId == null ? "" : data.snapshotOwnerId, submittedAt);
  }
}
