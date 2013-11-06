// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
