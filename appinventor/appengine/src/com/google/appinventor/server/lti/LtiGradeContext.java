// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers, per App Inventor user, the Assignment and Grade Services line item
 * from the most recent LTI launch, so a later grade passback can target it.
 * In memory, single instance dev spike only.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiGradeContext {

  /** What is needed to post a score back to the platform. */
  static final class Context {
    final String lineItemUrl;
    final String ltiUserSub;

    Context(String lineItemUrl, String ltiUserSub) {
      this.lineItemUrl = lineItemUrl;
      this.ltiUserSub = ltiUserSub;
    }
  }

  private static final Map<String, Context> STORE = new ConcurrentHashMap<>();

  private LtiGradeContext() {}

  static void put(String appInventorUserId, String lineItemUrl, String ltiUserSub) {
    if (lineItemUrl != null && !lineItemUrl.isEmpty()) {
      STORE.put(appInventorUserId, new Context(lineItemUrl, ltiUserSub));
    }
  }

  static Context get(String appInventorUserId) {
    return STORE.get(appInventorUserId);
  }
}
