// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

import com.google.appinventor.buildserver.util.ExecutorUtils;
import java.io.File;

/**
 * The AndroidPaths class extends the Paths class to include Android-specific
 * file locations, such as where to place merged resources and the R class
 * files needed by Android apps.
 */
public class AndroidPaths extends Paths {
  private File drawableDir;
  private File libsDir;
  private File classesDir;
  private File manifest;
  private File mergedResDir;
  private File tmpPackageName;

  @Override
  public void mkdirs(File buildDir) {
    super.mkdirs(buildDir);
    setDeployDir(ExecutorUtils.createDir(buildDir, "deploy"));
    setResDir(ExecutorUtils.createDir(buildDir, "res"));
    setDrawableDir(ExecutorUtils.createDir(buildDir, "drawable"));
    setLibsDir(ExecutorUtils.createDir(buildDir, "libs"));
    setClassesDir(ExecutorUtils.createDir(buildDir, "classes"));
  }

  public File getDrawableDir() {
    return drawableDir;
  }

  public void setDrawableDir(File drawableDir) {
    this.drawableDir = drawableDir;
  }

  public File getLibsDir() {
    return libsDir;
  }

  public void setLibsDir(File libsDir) {
    this.libsDir = libsDir;
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
