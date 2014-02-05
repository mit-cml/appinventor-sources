// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.ProfilePage;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
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
  
//  private ProjectToolbar projectToolbar;
  
  private static ProfilePage profile;
  

  /**
   * Returns the singleton projects list box.
   *
   * @return  project list box
   */
  public static ProjectListBox getProjectListBox() {
    //INSTANCE.setToolbar(pt);
    return INSTANCE;
  }
  
  private void setToolbar(ProjectToolbar pt) {
    
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
    profile = new ProfilePage("-1",  0);
    
    FlowPanel projectsContainer = new FlowPanel();
    FlowPanel studiosContainer = new FlowPanel();
    FlowPanel profileContainer = new FlowPanel();

//  pContainer.add(projectToolbar);
    projectsContainer.add(projects);
    profileContainer.add(profile);
    
  
    
    this.add(projectsContainer, MESSAGES.projectListBoxCaption());
    this.add(studiosContainer, MESSAGES.studioListBoxCaption());
    this.add(profileContainer, MESSAGES.profilePageBoxCaption());
    this.selectTab(0);
    
    // Styling options
    this.addStyleName("gallery");
//    this.addStyleName("ode-MyTabs");
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
