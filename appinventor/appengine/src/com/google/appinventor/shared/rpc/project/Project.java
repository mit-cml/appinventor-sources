// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates project along with source file names and content.
 *
 */
public final class Project {
  private String projectName;
  private String projectType;
  private String projectHistory;
  private final List<TextFile> sourceFiles;
  private final List<RawFile> rawSourceFiles;

  /**
   * Creates project.
   *
   * @param projectName project name
   */
  public Project(String projectName) {
    this.projectName = projectName;
    sourceFiles = new ArrayList<TextFile>();
    rawSourceFiles = new ArrayList<RawFile>();
  }

  /**
   * Returns project name.
   *
   * @return project name
   */
  public String getProjectName() {
    return projectName;
  }

  /**
   * Sets project type.
   *
   * @param projectType project type
   */
  public void setProjectType(String projectType) {
    this.projectType = projectType;
  }

  /**
   * Returns project type.
   *
   * @return project type
   */
  public String getProjectType() {
    return projectType;
  }

  /**
   * Adds file name and content to project.
   *
   * @param textFile text file
   */
  public void addTextFile(TextFile textFile) {
    sourceFiles.add(textFile);
  }

  /**
   * Adds file name and content to project.
   *
   * @param rawFile raw file
   */
  public void addRawFile(RawFile rawFile) {
    rawSourceFiles.add(rawFile);
  }

  /**
   * Returns source files.
   *
   * @return source files
   */
  public Iterable<TextFile> getSourceFiles() {
    return sourceFiles;
  }

  /**
   * Returns raw source files.
   *
   * @return raw source files
   */
  public Iterable<RawFile> getRawSourceFiles() {
    return rawSourceFiles;
  }

  /**
   * @return the projectHistory specially formatted string
   */
  public String getProjectHistory() {
    return projectHistory;
  }

  /**
   * @param projectHistory the projectHistory to set
   */
  public void setProjectHistory(String projectHistory) {
    this.projectHistory = projectHistory;
  }
}
