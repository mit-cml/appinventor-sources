// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.storage.StorageUtil;

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

  /**
   * Returns the entity name associated with this source node.
   */
  public String getEntityName() {
    return getEntityName(getFileId());
  }

  /**
   * Returns the entity name, potentially with a namespace, for this source node.
   *
   * @return the prefixed entity name
   */
  public String getPrefixedEntityName() {
    return getEntityName();
  }

  /**
   * Returns the form name associated with the given fileId.
   * Note that the extension of the fileId is ignored, so this works for both form (.scm) files and
   * blocks (.blk) files.
   */
  public static String getEntityName(String fileId) {
    return StorageUtil.trimOffExtension(StorageUtil.basename(fileId));
  }
}
