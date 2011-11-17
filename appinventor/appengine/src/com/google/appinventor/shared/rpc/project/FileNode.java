// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.project;


/**
 * General purpose file node in project tree.
 *
 */
public class FileNode extends ProjectNode {
  // For serialization
  private static final long serialVersionUID = 5856589308877963875L;

  /**
   * Default constructor (for serialization only).
   */
  public FileNode() {
  }

  /**
   * Creates a new file project node.
   *
   * @param name  file name
   * @param fileId  file ID
   */
  public FileNode(String name, String fileId) {
    super(name, fileId);
  }
}
