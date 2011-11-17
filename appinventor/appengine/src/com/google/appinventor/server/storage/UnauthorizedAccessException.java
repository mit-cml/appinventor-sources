// Copyright 2010 Google. All Rights Reserved.

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
