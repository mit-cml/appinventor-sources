// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project.youngandroid;

import com.google.appinventor.shared.rpc.project.HasAssetsFolder;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;


/**
 * Project root node for Young Android projects.
 *
 */
public final class YoungAndroidProjectNode extends ProjectRootNode
    implements HasAssetsFolder<YoungAndroidAssetsFolder> {
  /**
   * Project type for Young Android projects.
   */
  public static final String YOUNG_ANDROID_PROJECT_TYPE = "YoungAndroid";

  /**
   * Project target types for YoungAndroid projects.
   */
  public static final String YOUNG_ANDROID_TARGET_ANDROID = "Android";

  /**
   * New file types.
   */
  public static final String YOUNG_ANDROID_NEW_FORM_FILE = "form";

  // For serialization
  private static final long serialVersionUID = -3993102178645391456L;

  /**
   * Default constructor (for serialization only).
   */
  public YoungAndroidProjectNode() {
  }

  /**
   * Creates a new Young Android project node.
   *
   * @param name  project name
   * @param projectId  project ID
   */
  public YoungAndroidProjectNode(String name, long projectId) {
    super(name, projectId, YOUNG_ANDROID_PROJECT_TYPE);
  }

  /**
   * Returns the asset folder node of the project.
   *
   * @return asset folder node
   */
  @Override
  public YoungAndroidAssetsFolder getAssetsFolder() {
    for (ProjectNode child : getChildren()) {
      if (child instanceof YoungAndroidAssetsFolder) {
        return (YoungAndroidAssetsFolder) child;
      }
    }

    // Should never happen!
    throw new IllegalStateException("Couldn't find asset folder");
  }

  /**
   * Returns the package node of the project.
   *
   * @return package node
   */
  public YoungAndroidPackageNode getPackageNode() {
    for (ProjectNode child : getChildren()) {
      // A package node is a child of a source folder node.
      if (child instanceof YoungAndroidSourceFolderNode) {
        for (ProjectNode child2 : child.getChildren()) {
          if (child2 instanceof YoungAndroidPackageNode) {
            return (YoungAndroidPackageNode) child2;
          }
        }
      }
    }

    // Should never happen!
    throw new IllegalStateException("Couldn't find package node");
  }
}
