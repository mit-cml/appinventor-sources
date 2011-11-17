// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.project;

/**
 * Package node in project tree.
 *
 * <p>Packages are a series of nested folders (as defined in Java). It is the
 * job of the project recognizer in the backend (server) to identify them.
 *
 */
public class PackageNode extends FolderNode {
  // For serialization
  private static final long serialVersionUID = 5856589308877963875L;

  /**
   * Default constructor (for serialization only).
   */
  public PackageNode() {
  }

  /**
   * Creates a new package project node.
   *
   * @param name  package name
   * @param packageId  package ID
   */
  public PackageNode(String name, String packageId) {
    super(name, packageId);
  }
}
