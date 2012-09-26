// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.shared.rpc.project.UserProject;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lizlooney@google.com (Liz Looney)
 */
public class ProjectComparatorsTest extends TestCase {
  private List<Project> projects;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    projects = new ArrayList<Project>();
    projects.add(new Project(
        new UserProject(1, "WheresSpeedo", "YoungAndroid", 1279563121389L)));
    projects.add(new Project(
        new UserProject(2, "Test", "YoungAndroid", 1283385318767L)));
    projects.add(new Project(
        new UserProject(3, "Calculator", "YoungAndroid", 1285093039591L)));
    projects.add(new Project(
        new UserProject(4, "CenteredButton", "YoungAndroid", 1285093334176L)));
    projects.add(new Project(
        new UserProject(5, "ActivityStarter", "YoungAndroid", 1286912595603L)));
    projects.add(new Project(
        new UserProject(6, "Xylophone", "YoungAndroid", 1287376452708L)));
    projects.add(new Project(
        new UserProject(7, "TestErrors", "YoungAndroid", 1289524529700L)));
    projects.add(new Project(
        new UserProject(8, "FastMath", "YoungAndroid", 1289586160944L)));
    projects.add(new Project(
        new UserProject(9, "CustomIcon", "YoungAndroid", 1289954486145L)));
    projects.add(new Project(
        new UserProject(10, "BluetoothLegoNxt", "YoungAndroid", 1290020039719L)));
    projects.add(new Project(
        new UserProject(11, "HideArrangement", "YoungAndroid", 1291068117943L)));
    projects.add(new Project(
        new UserProject(12, "Issue331", "YoungAndroid", 1291073134051L)));
    projects.add(new Project(
        new UserProject(13, "TextAlignment", "YoungAndroid", 1291318515390L)));
    projects.add(new Project(
        new UserProject(14, "StopWatch", "YoungAndroid", 1291415211435L)));
    projects.add(new Project(
        new UserProject(15, "ProjectCreatedDec7At4_47pm", "YoungAndroid", 1291769259588L)));
  }

  public void testCompareByNameAscending() {
    Collections.sort(projects, ProjectComparators.COMPARE_BY_NAME_ASCENDING);
    String previousName = null;
    for (Project project : projects) {
      String name = project.getProjectName();
      if (previousName != null) {
        assertTrue(name.compareTo(previousName) >= 0);
      }
      previousName = name;
    }
  }

  public void testCompareByNameDescending() {
    Collections.sort(projects, ProjectComparators.COMPARE_BY_NAME_DESCENDING);
    String previousName = null;
    for (Project project : projects) {
      String name = project.getProjectName();
      if (previousName != null) {
        assertTrue(name.compareTo(previousName) <= 0);
      }
      previousName = name;
    }
  }

  public void testCompareByDateAscending() {
    Collections.sort(projects, ProjectComparators.COMPARE_BY_DATE_ASCENDING);
    long previousDate = 0;
    for (Project project : projects) {
      long date = project.getDateCreated();
      if (previousDate != 0) {
        assertTrue(date >= previousDate);
      }
      previousDate = date;
    }
  }

  public void testCompareByDateDescending() {
    Collections.sort(projects, ProjectComparators.COMPARE_BY_DATE_DESCENDING);
    long previousDate = 0;
    for (Project project : projects) {
      long date = project.getDateCreated();
      if (previousDate != 0) {
        assertTrue(date <= previousDate);
      }
      previousDate = date;
    }
  }
}
