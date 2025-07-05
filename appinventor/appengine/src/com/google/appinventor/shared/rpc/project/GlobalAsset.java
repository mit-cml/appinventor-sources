// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data for a global asset.
 *
 */
public class GlobalAsset implements IsSerializable {
  private String userId;
  private String fileName;
  private String folder;
  private long timestamp;

  // For GWT RPC
  @SuppressWarnings("unused")
  private GlobalAsset() {}

  public GlobalAsset(String userId, String fileName, String folder, long timestamp) {
    this.userId = userId;
    this.fileName = fileName;
    this.folder = folder;
    this.timestamp = timestamp;
  }

  public String getUserId() {
    return userId;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFolder() {
    return folder;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
