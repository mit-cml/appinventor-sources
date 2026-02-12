// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import com.google.appinventor.server.storage.StorageIo;

/**
 * Value class carrying per-request parameters shared across context modules.
 */
public class ContextParams {

  private final String userId;
  private final long projectId;
  private final String screenName;
  private final String mode;
  private final String blocksYail;
  private final String currentView;
  private final StorageIo storageIo;

  public ContextParams(String userId, long projectId, String screenName,
      String mode, String blocksYail, String currentView, StorageIo storageIo) {
    this.userId = userId;
    this.projectId = projectId;
    this.screenName = screenName;
    this.mode = mode;
    this.blocksYail = blocksYail;
    this.currentView = currentView;
    this.storageIo = storageIo;
  }

  public String getUserId() {
    return userId;
  }

  public long getProjectId() {
    return projectId;
  }

  public String getScreenName() {
    return screenName;
  }

  public String getMode() {
    return mode;
  }

  public String getBlocksYail() {
    return blocksYail;
  }

  public String getCurrentView() {
    return currentView;
  }

  public StorageIo getStorageIo() {
    return storageIo;
  }
}
