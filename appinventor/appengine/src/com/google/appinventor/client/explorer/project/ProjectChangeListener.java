// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.shared.rpc.project.ProjectNode;

/**
 * Listener interface for project change events.
 *
 * <p>Classes interested in processing project changes must implement this
 * interface, and instances of that class are registered with the
 * {@link Project} instances using their
 * {@link Project#addProjectChangeListener(ProjectChangeListener)} method.
 * When a project node is added, the listeners'
 * {@link #onProjectNodeAdded(Project, ProjectNode)} methods will be invoked.
 * When a project node is removed, the listeners'
 * {@link #onProjectNodeRemoved(Project, ProjectNode)} methods will be invoked.
 *
 */
public interface ProjectChangeListener {

  /**
   * Invoked after all project information loaded successfully.
   */
  void onProjectLoaded(Project project);

  /**
   * Invoked when a project node is added.
   *
   * @param node  project node added
   */
  void onProjectNodeAdded(Project project, ProjectNode node);

  /**
   * Invoked when a project node is removed.
   *
   * @param node  project node removed
   */
  void onProjectNodeRemoved(Project project, ProjectNode node);
}
