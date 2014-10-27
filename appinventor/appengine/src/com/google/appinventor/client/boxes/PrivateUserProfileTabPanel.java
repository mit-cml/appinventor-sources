// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.ProfilePage;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TabPanel;


/**
 * TabPanel implementation for private user profile.
 *
 */
public final class PrivateUserProfileTabPanel extends TabPanel {

  // Singleton project explorer box instance (only one project explorer allowed)
  private static final PrivateUserProfileTabPanel INSTANCE = new PrivateUserProfileTabPanel();
  // Project list for young android
  private ProfilePage profile;
  // Tab container
  private FlowPanel profileContainer = new FlowPanel();

  /**
   * Returns the singleton projects list box.
   *
   * @return  project list box
   */
  public static PrivateUserProfileTabPanel getPrivateUserProfileTabPanel() {
    return INSTANCE;
  }

  /**
   * Creates new Gallery Tab Panel
   */
  private PrivateUserProfileTabPanel() {
    profile = new ProfilePage("-1",  0);
    profileContainer.add(profile);
    this.add(profileContainer, MESSAGES.profilePageBoxCaption());
    this.selectTab(0);

    // Styling options
    this.addStyleName("gallery");
  }

  public void loadProfileImage(){
    profile.loadImage();
  }

}
