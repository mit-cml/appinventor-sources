// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.widgets.boxes.Box;

/**
 * Box implementation for project list.
 *
 */
public final class ProjectListBox extends Box {
  // Singleton project explorer box instance (only one project explorer allowed)
  private static ProjectListBox INSTANCE;

  // Project list for young android
  private final ProjectList plist;

  /**
   * Creates a new ProjectListBox if one doesn't exist using the provided
   * {@code factory}. If this method has previously been called, the existing
   * ProjectListBox is returned.
   *
   * @param factory factory to use for creating UI components
   * @return a project list box instance
   */
  public static ProjectListBox create(UiStyleFactory factory) {
    if (INSTANCE == null) {
      INSTANCE = new ProjectListBox(factory.createProjectList());
    }
    return INSTANCE;
  }

  /**
   * Returns the singleton projects list box.
   *
   * @return  project list box
   */
  public static ProjectListBox getProjectListBox() {
    return INSTANCE;
  }

  /**
   * Creates new project list box.
   */
  private ProjectListBox(ProjectList plist) {
    super(MESSAGES.projectListBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    this.plist = plist;
    setContent(plist);
  }

  /**
   * Returns project list associated with projects explorer box.
   *
   * @return  project list
   */
  public ProjectList getProjectList() {
    return plist;
  }

  public void loadProjectList() {
    plist.setIsTrash(false);
    plist.refresh(false);
    this.setCaption(MESSAGES.projectListBoxCaption());
  }

  public void loadTrashList() {
    plist.setIsTrash(true);
    plist.refresh(false);
    this.setCaption(MESSAGES.trashprojectlistbox());
  }
}
