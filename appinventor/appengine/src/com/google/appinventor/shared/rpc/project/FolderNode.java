// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.project;

/**
 * General purpose folder node in project tree.
 *
 */
public class FolderNode extends ProjectNode {

  // For serialization
  private static final long serialVersionUID = 5856589308877963875L;

  /**
   * Default constructor (for serialization only).
   */
  public FolderNode() {
  }

  /**
   * Creates a new folder project node.
   *
   * @param name  folder name
   * @param fileId  folder ID
   */
  public FolderNode(String name, String fileId) {
    super(name, fileId);
  }
}
