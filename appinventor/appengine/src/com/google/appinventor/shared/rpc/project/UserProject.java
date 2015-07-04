// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bundles user specific information about a project to send it over an RPC.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class UserProject implements IsSerializable {
  /**
   * The project's ID.
   */
  private long projectId;

  /**
   * The project's name.
   */
  private String projectName;

  /**
   * The project's type.
   */
  private String projectType;

  /**
   * The attribution ID.
   */
  private long attributionId;

  /**
   * The date the project was created expressed in milliseconds since
   * January 1, 1970 UTC
   */
  private long creationDate;

  /**
   * The last date the project was modified expressed in milliseconds since
   * January 1, 1970 UTC
   */
  private long modificationDate;

  private long galleryId;

  private static final String DELIM = "#DELIM#";

  public static final long NOTPUBLISHED = 0;
  public static final long FROMSCRATCH = 0;

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private UserProject() {
  }

  /**
   * Creates a new project info object.
   *
   * @param projectId the project id
   * @param projectName the project name
   * @param projectType the project type
   * @param creationDate the creation date
   * @param long galleryId the gallery id
   */
  public UserProject(long projectId, String projectName, String projectType, long creationDate, long galleryId, long attributionId) {
    this.projectId = projectId;
    this.projectName = projectName;
    this.projectType = projectType;
    this.creationDate = creationDate;
    this.modificationDate = creationDate;
    this.galleryId = galleryId;
    this.attributionId = attributionId;
  }

  /**
   * Creates a new project info object.
   *
   * @param projectId the project id
   * @param projectName the project name
   * @param projectType the project type
   * @param creationData creation date
   * @param modificationData modification data
   * @param galleryId gallery id
   * @param attributionId attribution id
   */
  public UserProject(long projectId, String projectName, String projectType, long creationDate,
      long modificationDate, long galleryId, long attributionId) {
    this.projectId = projectId;
    this.projectName = projectName;
    this.projectType = projectType;
    this.creationDate = creationDate;
    this.modificationDate = modificationDate;
    this.galleryId = galleryId;
    this.attributionId = attributionId;
  }

  /**
   * Returns the attribution ID.
   *
   * @return the attributionId
   */
  public long getAttributionId() {
    return attributionId;
  }

  public void setAttributionId(long attributionId) {
    this.attributionId = attributionId;
  }

  /**
   * Returns the project ID.
   *
   * @return the projectId
   */
  public long getProjectId() {
    return projectId;
  }

  /**
   * Returns the project name.
   *
   * @return the projectName
   */
  public String getProjectName() {
    return projectName;
  }

  /**
   * Returns the project type.
   *
   * @return the projectType
   */
  public String getProjectType() {
    return projectType;
  }

  public long getDateCreated() {
    return creationDate;
  }

  public long getDateModified() {
    return modificationDate;
  }
  public long getGalleryId() {
    return galleryId;
  }
  public void setDateModified(long modificationDate) {
    if (modificationDate != 0) {
      this.modificationDate = modificationDate;
    }
  }
  public void setGalleryId(long galleryId) {
    this.galleryId = galleryId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof UserProject)) {
      return false;
    }
    UserProject otherUserProject = (UserProject) other;
    return projectId == otherUserProject.projectId &&
        projectName.equals(otherUserProject.projectName) &&
        projectType.equals(otherUserProject.projectType) &&
        creationDate == otherUserProject.creationDate &&
        modificationDate == otherUserProject.modificationDate;
  }

  @Override
  public int hashCode() {
    return (int) (projectId ^ (projectId >>> 32));
  }

  @Override
  public String toString() {
    return projectId + DELIM + projectName + DELIM + projectType + DELIM + creationDate +
        DELIM + modificationDate;
  }

  public static UserProject valueOf(String text) {
    String[] parts = text.split(DELIM);
    if (parts.length != 5) {
      throw new IllegalArgumentException();
    }
    UserProject userProject = new UserProject();
    userProject.projectId = Long.parseLong(parts[0]);
    userProject.projectName = parts[1];
    userProject.projectType = parts[2];
    userProject.creationDate = Long.parseLong(parts[3]);
    userProject.modificationDate = Long.parseLong(parts[4]);
    userProject.galleryId= UserProject.NOTPUBLISHED;
    return userProject;
  }
}
