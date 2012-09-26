// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.SourceNode;

/**
 * Superclass for all Young Android source file nodes in the project tree.
 *
 */
public abstract class YoungAndroidSourceNode extends SourceNode {

  /**
   * Default constructor (for serialization only).
   */
  YoungAndroidSourceNode() {
  }

  /**
   * Creates a new Young Android source file project node.
   *
   * @param name  file name
   * @param fileId  file id
   */
  public YoungAndroidSourceNode(String name, String fileId) {
    super(name, fileId);
  }
}
