// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

import com.google.appinventor.buildserver.util.ExecutorUtils;
import java.io.File;

public class Paths {
  protected String outputFileName;

  protected File projectRootDir;
  protected File buildDir;
  protected File deployDir;

  protected File resDir;
  protected File tmpDir;
  protected File assetsDir;

  protected Paths() {
  }

  public void setProjectRootDir(File projectRootDir) {
    this.projectRootDir = projectRootDir;
  }

  public File getProjectRootDir() {
    return projectRootDir;
  }

  public void setOutputFileName(String outputFileName) {
    this.outputFileName = outputFileName;
  }

  public File getBuildDir() {
    return buildDir;
  }

  public void setBuildDir(File buildDir) {
    this.buildDir = buildDir;
  }

  public File getDeployDir() {
    return deployDir;
  }

  public File getDeployFile() {
    return new File(this.deployDir, this.outputFileName);
  }

  public void setDeployDir(File deployDir) {
    this.deployDir = deployDir;
  }

  public File getResDir() {
    return resDir;
  }

  public void setResDir(File resDir) {
    this.resDir = resDir;
  }

  public File getTmpDir() {
    return tmpDir;
  }

  public void setTmpDir(File tmpDir) {
    this.tmpDir = tmpDir;
  }

  public File getAssetsDir() {
    return assetsDir;
  }

  public void setAssetsDir(File assetsDir) {
    this.assetsDir = assetsDir;
  }

  public void mkdirs(File buildDir) {
    setBuildDir(ExecutorUtils.createDir(buildDir));
    setTmpDir(ExecutorUtils.createDir(buildDir, "tmp"));
  }

  @Override
  public String toString() {
    return "Paths{"
        + "outputFileName='" + outputFileName + '\''
        + ", buildDir=" + buildDir
        + ", deployDir=" + deployDir
        + ", resDir=" + resDir
        + ", tmpDir=" + tmpDir
        + ", assetsDir=" + assetsDir
        + '}';
  }
}
