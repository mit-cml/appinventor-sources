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
 * Remembers, per App Inventor user, which project belongs to which LMS
 * assignment (platform issuer, deployment, and resource link), so a relaunch
 * always finds the same project again, even after the teacher renames the
 * activity or the server restarts.
 *
 * <p>Persisted as a small per user JSON file through {@link StorageIo}, the
 * same mechanism that stores the Android keystore, so nothing is added to the
 * StorageIo interface. Launches are rare and per user, so the read modify
 * write of the file needs no further coordination.
 *
 * @author zikun@stanford.edu (Zikun Zhu)
 */
final class LtiResourceLinks {

  private static final Logger LOG = Logger.getLogger(LtiResourceLinks.class.getName());
  private static final String FILENAME = "lti_resource_links.json";

  private LtiResourceLinks() {}

  /** Builds the map key for one assignment on one platform. */
  static String key(String issuer, String deploymentId, String resourceLinkId) {
    return issuer + "|" + deploymentId + "|" + resourceLinkId;
  }

  /** Returns the project linked to the assignment key, or -1 if none. */
  static long get(String appInventorUserId, String linkKey) {
    JSONObject links = load(appInventorUserId);
    return links.optLong(linkKey, -1);
  }

  /** Links the assignment key to a project, replacing any earlier link. */
  static void put(String appInventorUserId, String linkKey, long projectId) {
    try {
      JSONObject links = load(appInventorUserId);
      links.put(linkKey, projectId);
      StorageIoInstanceHolder.getInstance().uploadRawUserFile(
          appInventorUserId, FILENAME, links.toString().getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Could not save the LTI resource link", e);
    }
  }

  private static JSONObject load(String appInventorUserId) {
    try {
      StorageIo storageIo = StorageIoInstanceHolder.getInstance();
      if (!storageIo.getUserFiles(appInventorUserId).contains(FILENAME)) {
        return new JSONObject();
      }
      byte[] raw = storageIo.downloadRawUserFile(appInventorUserId, FILENAME);
      return new JSONObject(new String(raw, StandardCharsets.UTF_8));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Could not load the LTI resource links", e);
      return new JSONObject();
    }
  }
}
