// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.shared.rpc.project.ProjectNode;

/**
 * Adapter class for {@link ProjectChangeListener}.
 *
 */
public class ProjectChangeAdapter implements ProjectChangeListener {

  @Override
  public void onProjectLoaded(Project project) {
  }

  @Override
  public void onProjectNodeAdded(Project project, ProjectNode node) {
  }

  @Override
  public void onProjectNodeRemoved(Project project, ProjectNode node) {
  }
}
