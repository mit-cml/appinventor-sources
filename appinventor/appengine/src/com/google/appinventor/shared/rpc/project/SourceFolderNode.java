// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

/**
 * General purpose source folder node in project tree.
 *
 */
public class SourceFolderNode extends FolderNode {

  // For serialization
  private static final long serialVersionUID = 585658933308877875L;

  /**
   * Default constructor (for serialization only).
   */
  public SourceFolderNode() {
  }

  /**
   * Creates a new source folder node.
   *
   * @param name  folder name
   * @param fileId  folder ID
   */
  public SourceFolderNode(String name, String fileId) {
    super(name, fileId);
  }
}
