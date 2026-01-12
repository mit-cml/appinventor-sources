// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IosPaths extends Paths {

  private File appIconDir;
  private File payloadDir;
  private File appDir;
  private File frameworkDir;
  private final List<File> plistParts = new ArrayList<>();

  public IosPaths() {
  }

  @Override
  public void setProjectRootDir(File projectRootDir) {
    super.setProjectRootDir(projectRootDir);
    setAssetsDir(new File(projectRootDir, "assets"));
  }

  public void setBuildDir(File buildDir) {
    super.setBuildDir(buildDir);
  }

  public void setAppIconDir(File appIconDir) {
    this.appIconDir = appIconDir;
  }

  public File getAppIconDir() {
    return appIconDir;
  }

  public void setPayloadDir(File payloadDir) {
    this.payloadDir = payloadDir;
  }

  public File getPayloadDir() {
    return payloadDir;
  }

  public void setAppDir(File appDir) {
    this.appDir = appDir;
  }

  public File getAppDir() {
    return appDir;
  }

  public void setFrameworkDir(File frameworkDir) {
    this.frameworkDir = frameworkDir;
  }

  public File getFrameworkDir() {
    return frameworkDir;
  }

  public void addPartialPlist(File plist) {
    plistParts.add(plist);
  }

  public List<File> getPartialPlistFiles() {
    return Collections.unmodifiableList(plistParts);
  }
}
