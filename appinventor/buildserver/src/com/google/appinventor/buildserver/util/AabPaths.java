// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.util;

import java.io.File;

public class AabPaths {
  private File root = null;
  private File base = null;
  private File protoApk = null;

  private File assetsDir = null;
  private File dexDir = null;
  private File libDir = null;
  private File manifestDir = null;
  private File resDir = null;

  public File getRoot() {
    return root;
  }

  public void setRoot(File root) {
    this.root = root;
  }

  public File getBase() {
    return base;
  }

  public void setBase(File base) {
    this.base = base;
  }

  public File getProtoApk() {
    return protoApk;
  }

  public void setProtoApk(File protoApk) {
    this.protoApk = protoApk;
  }

  public File getAssetsDir() {
    return assetsDir;
  }

  public void setAssetsDir(File assetsDir) {
    this.assetsDir = assetsDir;
  }

  public File getDexDir() {
    return dexDir;
  }

  public void setDexDir(File dexDir) {
    this.dexDir = dexDir;
  }

  public File getLibDir() {
    return libDir;
  }

  public void setLibDir(File libDir) {
    this.libDir = libDir;
  }

  public File getManifestDir() {
    return manifestDir;
  }

  public void setManifestDir(File manifestDir) {
    this.manifestDir = manifestDir;
  }

  public File getResDir() {
    return resDir;
  }

  public void setResDir(File resDir) {
    this.resDir = resDir;
  }
}
