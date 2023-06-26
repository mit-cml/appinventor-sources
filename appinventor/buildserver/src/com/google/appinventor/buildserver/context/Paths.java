// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

import java.io.File;

public class Paths {
  private final String outputFileName;

  private File buildDir;
  private File deployDir;

  private File resDir;
  private File drawableDir;
  private File tmpDir;
  private File libsDir;
  private File assetsDir;
  private File classesDir;

  private File manifest;

  private File mergedResDir;
  private File tmpPackageName;

  public Paths(String outputFileName) {
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

  public File getDrawableDir() {
    return drawableDir;
  }

  public void setDrawableDir(File drawableDir) {
    this.drawableDir = drawableDir;
  }

  public File getTmpDir() {
    return tmpDir;
  }

  public void setTmpDir(File tmpDir) {
    this.tmpDir = tmpDir;
  }

  public File getLibsDir() {
    return libsDir;
  }

  public void setLibsDir(File libsDir) {
    this.libsDir = libsDir;
  }

  public File getAssetsDir() {
    return assetsDir;
  }

  public void setAssetsDir(File assetsDir) {
    this.assetsDir = assetsDir;
  }

  public File getClassesDir() {
    return classesDir;
  }

  public void setClassesDir(File classesDir) {
    this.classesDir = classesDir;
  }

  public File getManifest() {
    return manifest;
  }

  public void setManifest(File manifest) {
    this.manifest = manifest;
  }

  public File getMergedResDir() {
    return mergedResDir;
  }

  public void setMergedResDir(File mergedResDir) {
    this.mergedResDir = mergedResDir;
  }

  public File getTmpPackageName() {
    return tmpPackageName;
  }

  public void setTmpPackageName(File tmpPackageName) {
    this.tmpPackageName = tmpPackageName;
  }

  @Override
  public String toString() {
    return "Paths{"
        + "outputFileName='" + outputFileName + '\''
        + ", buildDir=" + buildDir
        + ", deployDir=" + deployDir
        + ", resDir=" + resDir
        + ", drawableDir=" + drawableDir
        + ", tmpDir=" + tmpDir
        + ", libsDir=" + libsDir
        + ", assetsDir=" + assetsDir
        + ", manifest=" + manifest
        + ", mergedResDir=" + mergedResDir
        + ", tmpPackageName=" + tmpPackageName
        + '}';
  }
}
