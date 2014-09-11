// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.ProfilePage;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TabPanel;


/**
 * TabPanel implementation for project list.
 *
 */
public final class ProjectListBox extends TabPanel {

  // Singleton project explorer box instance (only one project explorer allowed)
  private static final ProjectListBox INSTANCE = new ProjectListBox();
  // Project list for young android
  private final ProjectList projects;
  private ProfilePage profile;
  // Tab container
  private FlowPanel projectsContainer = new FlowPanel();
  private FlowPanel profileContainer = new FlowPanel();

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
  private ProjectListBox() {
    /*
    super(MESSAGES.projectListBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable
    */
    projects = new ProjectList();
    projectsContainer.add(projects);
    profile = new ProfilePage("-1",  0);
    profileContainer.add(profile);

    this.add(projectsContainer, MESSAGES.projectListBoxCaption());
    this.add(profileContainer, MESSAGES.profilePageBoxCaption());
    profileContainer.setVisible(false);
    this.selectTab(0);

    // Styling options
    this.addStyleName("gallery");
//    this.addStyleName("ode-MyTabs");
  }

  public void loadProfileImage(){
    profile.loadImage();
  }

  public void showProfileTab(){
    profileContainer.setVisible(true);
  }

  /**
   * Returns project list associated with projects explorer box.
   *
   * @return  project list
   */
  public ProjectList getProjectList() {
     return projects;
  }
}
