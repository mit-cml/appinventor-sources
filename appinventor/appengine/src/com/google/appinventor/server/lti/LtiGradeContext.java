// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.lti;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * Remembers, per App Inventor user, the Assignment and Grade Services line item
 * from the most recent LTI launch, so a later grade passback can target it.
 *
 * <p>Persisted as a small per user file through {@link StorageIo}, the same
 * mechanism that stores the Android keystore, so a submission still works after
 * a server restart and nothing is added to the StorageIo interface. The file
 * holds only the gradebook line item reference and the platform user id, no
 * secret, so it is stored in plain form.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiGradeContext {

  private static final Logger LOG = Logger.getLogger(LtiGradeContext.class.getName());
  private static final String FILENAME = "lti_grade_context.json";

  /** What is needed to post a score back to the platform. */
  static final class Context {
    final String lineItemUrl;
    final String ltiUserSub;

    Context(String lineItemUrl, String ltiUserSub) {
      this.lineItemUrl = lineItemUrl;
      this.ltiUserSub = ltiUserSub;
    }
  }

  private LtiGradeContext() {}

  /** Saves the grade passback target for a user, replacing any earlier one. */
  static void put(String appInventorUserId, String lineItemUrl, String ltiUserSub) {
    if (lineItemUrl == null || lineItemUrl.isEmpty()) {
      return;
    }
    try {
      JSONObject json = new JSONObject()
          .put("lineItemUrl", lineItemUrl)
          .put("ltiUserSub", ltiUserSub == null ? "" : ltiUserSub);
      StorageIoInstanceHolder.getInstance().uploadRawUserFile(
          appInventorUserId, FILENAME, json.toString().getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Could not save the LTI grade context", e);
    }
  }

  /** Loads the grade passback target for a user, or null if none is stored. */
  static Context get(String appInventorUserId) {
    try {
      StorageIo storageIo = StorageIoInstanceHolder.getInstance();
      if (!storageIo.getUserFiles(appInventorUserId).contains(FILENAME)) {
        return null;
      }
      byte[] raw = storageIo.downloadRawUserFile(appInventorUserId, FILENAME);
      JSONObject json = new JSONObject(new String(raw, StandardCharsets.UTF_8));
      String lineItemUrl = json.optString("lineItemUrl", "");
      if (lineItemUrl.isEmpty()) {
        return null;
      }
      return new Context(lineItemUrl, json.optString("ltiUserSub", ""));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Could not load the LTI grade context", e);
      return null;
    }
  }
}
