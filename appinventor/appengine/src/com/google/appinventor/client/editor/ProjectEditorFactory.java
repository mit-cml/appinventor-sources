// Copyright 2008 Google Inc. All Rights Reserved.

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
