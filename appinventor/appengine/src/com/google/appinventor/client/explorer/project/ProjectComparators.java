// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.project;

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
      return proj1Name.compareTo(proj2Name); // ascending
    }
  };

  public static final Comparator<Project> COMPARE_BY_NAME_DESCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      String proj1Name = proj1.getProjectName();
      String proj2Name = proj2.getProjectName();
      return proj2Name.compareTo(proj1Name); // descending
    }
  };

  public static final Comparator<Project> COMPARE_BY_DATE_ASCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      long date1 = proj1.getDateCreated();
      long date2 = proj2.getDateCreated();
      return Long.signum(date1 - date2); // ascending
    }
  };

  public static final Comparator<Project> COMPARE_BY_DATE_DESCENDING = new Comparator<Project>() {
    @Override
    public int compare(Project proj1, Project proj2) {
      long date1 = proj1.getDateCreated();
      long date2 = proj2.getDateCreated();
      return Long.signum(date2 - date1); // descending
    }
  };
}
