// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.PackageNode;

/**
 * Young Android package node in project tree.
 *
 */
public final class YoungAndroidPackageNode extends PackageNode {

  // For serialization
  private static final long serialVersionUID = -2978004305264661517L;

  /**
   * Default constructor (for serialization only).
   */
  public YoungAndroidPackageNode() {
  }

  /**
   * Creates a new package project node.
   *
   * @param name  package name
   * @param packageId  package ID
   */
  public YoungAndroidPackageNode(String name, String packageId) {
    super(name, packageId);
  }

  /**
   * Returns package name.
   *
   * @return  package name
   */
  public String getPackageName() {
    return getName();
  }
}
