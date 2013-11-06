// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor;

import com.google.appinventor.shared.rpc.project.ProjectRootNode;

/**
 * Interface for classes that can create project editors.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface ProjectEditorFactory {
  /**
   * Creates a new project editor for the given project.
   *
   * @param projectRootNode  the project root node
   * @return a new project editor
   */
  ProjectEditor createProjectEditor(ProjectRootNode projectRootNode);
}
