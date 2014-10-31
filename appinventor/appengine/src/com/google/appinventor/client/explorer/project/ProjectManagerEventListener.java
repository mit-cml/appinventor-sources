// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

/**
 * Listener interface for receiving project manager events.
 *
 * <p>Classes interested in processing project manager events must implement
 * this interface, and instances of that class must be registered with the
 * {@link ProjectManager} instance using its
 * {@link ProjectManager#addProjectManagerEventListener(ProjectManagerEventListener)}
 * method. When a project is added to the project manager, the listeners'
 * {@link #onProjectAdded(Project)} methods will be invoked. When a project is
 * removed (either closed or deleted) the listeners'
 * {@link #onProjectRemoved(Project)} methods will be invoked.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface ProjectManagerEventListener {

  /**
   * Invoked after a project was added to the ProjectManager
   *
   * @param project  project added
   */
  void onProjectAdded(Project project);

  /**
   * Invoked after a project was removed (either closed or deleted).
   *
   * @param project  project removed
   */
  void onProjectRemoved(Project project);

  /**
   * Invoked after all projects have been loaded by ProjectManager
   *
   */
  void onProjectsLoaded();

  /**
   * Invoked after a project is published so we can change its button to "Update"
   *
   */
  void onProjectPublishedOrUnpublished();
}
