// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIoInstanceHolder;

/**
 * Remembers which template project one LMS assignment copies from, so that every
 * learner on that assignment starts from the same project.
 *
 * <p>The template a teacher picks travels to the platform inside the Deep Linking
 * response and comes back on each launch, which means a teacher who picks a
 * different template later would otherwise change the starting point for learners
 * who join an assignment already under way. The specification does not let the
 * platform tell a tool which existing assignment a selection is replacing, so the
 * choice is fixed here instead, on the first launch that uses it. The classroom
 * portal reaches the same result by blocking template changes once a learner has
 * started.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiAssignmentTemplates {

  private LtiAssignmentTemplates() {}

  /** Returns the template the assignment is fixed to, or 0 if no learner has opened it yet. */
  static long get(String issuer, String deploymentId, String resourceLinkId) {
    return StorageIoInstanceHolder.getInstance().getLtiAssignmentTemplate(
        issuer, deploymentId, resourceLinkId);
  }

  /**
   * Fixes the assignment to a template on the first call and returns whatever it is fixed to on
   * every call after that, so the returned value is what the learner should be given.
   */
  static long pin(String issuer, String deploymentId, String resourceLinkId,
      long templateProjectId) {
    return StorageIoInstanceHolder.getInstance().pinLtiAssignmentTemplate(
        issuer, deploymentId, resourceLinkId, templateProjectId);
  }
}
