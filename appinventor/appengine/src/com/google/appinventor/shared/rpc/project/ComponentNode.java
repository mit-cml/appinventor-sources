// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;

/**
 * Component node in project tree.
 * A subclass of YoungAndroidAssetNode so that
 * assets panel gets reloaded when it is added to the tree
 */
public class ComponentNode extends YoungAndroidAssetNode {

  // For serialization
  private static final long serialVersionUID = 8824805241429504801L;

  /**
   * Default constructor (for serialization only).
   */
  public ComponentNode() {
  }

  /**
   * Creates a new component node
   *
   * @param name display name shown in assets panel
   * @param fileId path to the directory storing all component files
   */
  public ComponentNode(String name, String fileId) {
    super(name, fileId);
  }
}
