// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.client.explorer.folder.ProjectFolder;

import java.util.Comparator;


/**
 * Comparators for {@link Project}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ProjectComparators {
  private ProjectComparators() {
  }

  public static final Comparator<Project> COMPARE_BY_NAME_ASCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      String proj1Name = proj1.getProjectName();
      String proj2Name = proj2.getProjectName();
      return proj1Name.compareToIgnoreCase(proj2Name); // ascending
    }
  };

  public static final Comparator<Project> COMPARE_BY_NAME_DESCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      String proj1Name = proj1.getProjectName();
      String proj2Name = proj2.getProjectName();
      return proj2Name.compareToIgnoreCase(proj1Name); // descending
    }
  };

  public static final Comparator<Project> COMPARE_BY_DATE_CREATED_ASCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      long date1 = proj1.getDateCreated();
      long date2 = proj2.getDateCreated();
      return Long.signum(date1 - date2); // ascending
    }
  };

  public static final Comparator<Project> COMPARE_BY_DATE_CREATED_DESCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      long date1 = proj1.getDateCreated();
      long date2 = proj2.getDateCreated();
      return Long.signum(date2 - date1); // descending
    }
  };

  public static final Comparator<Project> COMPARE_BY_DATE_MODIFIED_ASCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      long date1 = proj1.getDateModified();
      long date2 = proj2.getDateModified();
      return Long.signum(date1 - date2); // ascending
    }
  };

  public static final Comparator<Project> COMPARE_BY_DATE_MODIFIED_DESCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      long date1 = proj1.getDateModified();
      long date2 = proj2.getDateModified();
      return Long.signum(date2 - date1); // descending
    }
  };

  public static final Comparator<ProjectFolder> COMPARE_BY_FOLDER_NAME_ASCENDING = new Comparator<ProjectFolder>() {
    @Override
    public int compare(ProjectFolder folder1, ProjectFolder folder2) {
      String folder1Name = folder1.getName();
      String folder2Name = folder2.getName();
      return folder1Name.compareToIgnoreCase(folder2Name); // ascending
    }
  };

  public static final Comparator<ProjectFolder> COMPARE_BY_FOLDER_NAME_DESCENDING = new Comparator<ProjectFolder>() {
    @Override
    public int compare(ProjectFolder folder1, ProjectFolder folder2) {
      String folder1Name = folder1.getName();
      String folder2Name = folder2.getName();
      return folder2Name.compareToIgnoreCase(folder1Name); // descending
    }
  };

  public static final Comparator<ProjectFolder> COMPARE_BY_FOLDER_DATE_CREATED_ASCENDING = new Comparator<ProjectFolder>() {
    @Override
    public int compare(ProjectFolder folder1, ProjectFolder folder2) {
      long date1 = folder1.getDateCreated();
      long date2 = folder2.getDateCreated();
      return Long.signum(date1 - date2); // ascending
    }
  };

  public static final Comparator<ProjectFolder> COMPARE_BY_FOLDER_DATE_CREATED_DESCENDING = new Comparator<ProjectFolder>() {
    @Override
    public int compare(ProjectFolder folder1, ProjectFolder folder2) {
      long date1 = folder1.getDateCreated();
      long date2 = folder2.getDateCreated();
      return Long.signum(date2 - date1); // descending
    }
  };

  public static final Comparator<ProjectFolder> COMPARE_BY_FOLDER_DATE_MODIFIED_ASCENDING = new Comparator<ProjectFolder>() {
    @Override
    public int compare(ProjectFolder folder1, ProjectFolder folder2) {
      long date1 = folder1.getDateModified();
      long date2 = folder2.getDateModified();
      return Long.signum(date1 - date2); // ascending
    }
  };

  public static final Comparator<ProjectFolder> COMPARE_BY_FOLDER_DATE_MODIFIED_DESCENDING = new Comparator<ProjectFolder>() {
    @Override
    public int compare(ProjectFolder folder1, ProjectFolder folder2) {
      long date1 = folder1.getDateModified();
      long date2 = folder2.getDateModified();
      return Long.signum(date2 - date1); // descending
    }
  };
}
