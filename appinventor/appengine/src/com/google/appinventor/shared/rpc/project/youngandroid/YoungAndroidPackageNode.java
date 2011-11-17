// Copyright 2008 Google Inc. All Rights Reserved.

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
