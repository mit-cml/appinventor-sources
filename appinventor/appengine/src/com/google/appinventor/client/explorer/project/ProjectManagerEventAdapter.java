// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

/**
 * Adapter class for {@link ProjectManagerEventListener}.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ProjectManagerEventAdapter implements ProjectManagerEventListener {

  @Override
  public void onProjectAdded(Project project) {
  }

  @Override
  public void onProjectRemoved(Project project) {
  }

  /**
   * Invoked after all projects have been loaded by ProjectManager. If the ProjectManager has
   * already finished loading projects, this will be called immediately upon adding the listener.
   */
  @Override
  public void onProjectsLoaded() { }

  @Override
  public void onProjectPublishedOrUnpublished() {
  }
}
