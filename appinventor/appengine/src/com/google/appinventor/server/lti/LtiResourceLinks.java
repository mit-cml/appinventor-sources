// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIoInstanceHolder;

/**
 * Remembers, per App Inventor user, which project belongs to which LMS
 * assignment (platform issuer, deployment, and resource link), so a relaunch
 * always finds the same project again, even after the teacher renames the
 * activity, the server restarts, or the launch lands on another server instance.
 *
 * <p>Stored in the datastore through the storage layer, keyed by the user and
 * the assignment identity.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiResourceLinks {

  private LtiResourceLinks() {}

  /** Returns the project linked to the assignment, or 0 if there is none. */
  static long get(String appInventorUserId, String issuer, String deploymentId,
      String resourceLinkId) {
    return StorageIoInstanceHolder.getInstance().getLtiForkProject(
        appInventorUserId, issuer, deploymentId, resourceLinkId);
  }

  /** Links the assignment to a project, replacing any earlier link. */
  static void put(String appInventorUserId, String issuer, String deploymentId,
      String resourceLinkId, long projectId) {
    StorageIoInstanceHolder.getInstance().storeLtiForkProject(
        appInventorUserId, issuer, deploymentId, resourceLinkId, projectId);
  }
}
