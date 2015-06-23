// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

/**
 * Source file node in the project tree.
 *
 */
public abstract class SourceNode extends FileNode {

  // For serialization
  private static final long serialVersionUID = 5856588435743875L;

  /**
   * Default constructor (for serialization only).
   */
  public SourceNode() {
  }

  /**
   * Creates a new source file project node.
   *
   * @param name  file name
   * @param fileId  file ID
   */
  public SourceNode(String name, String fileId) {
    super(name, fileId);
  }

  @Override
  protected boolean isSourceNode() {
    return true;
  }
}
