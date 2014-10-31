// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

/**
 * Exception for unauthorized access to a file.
 *
 * @author markf@google.com (Mark Friedman)
 */
public class UnauthorizedAccessException extends Exception {

  private final String userId;
  private final long projectId;
  private final String fileName;

  public UnauthorizedAccessException(String userId, long projectId, String fileName) {
    this.userId = userId;
    this.projectId = projectId;
    this.fileName = fileName;
  }
  
  public String getUserId() {
    return userId;
  }

  public long getProjectId() {
    return projectId;
  }

  public String getFileName() {
    return fileName;
  }
}
