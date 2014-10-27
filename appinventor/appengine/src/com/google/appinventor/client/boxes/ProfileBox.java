// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.explorer.youngandroid.ProfilePage;
import com.google.gwt.user.client.ui.FlowPanel;



/**
 * Box implementation for user profile.
 *
 * @author vincentaths@gmail.com (Vincent Zhang)
 *
 */
public final class ProfileBox extends FlowPanel {

  // Singleton Profile explorer box instance (only one Profile explorer allowed)
  private static final ProfileBox INSTANCE = new ProfileBox();

  // Profile page for young android
  private static FlowPanel pContainer;
  private static ProfilePage pPage;

  /**
   * Returns the singleton ProfileBox.
   *
   * @return  ProfileBox box
   */
  public static ProfileBox getUserProfileBox() {
    return INSTANCE;
  }

  public static void setProfile(String userId, int editStatus)
  {
    pContainer.clear();
    pPage = new ProfilePage(userId, editStatus);
    if(editStatus == ProfilePage.PRIVATE){
      pPage.loadImage();
    }
    pContainer.add(pPage);
  }
  /**
   * Creates new user profile box.
   */
  private ProfileBox() {
//    super(MESSAGES.userProfileBoxCaption(),
//        300,    // height
//        false,  // minimizable
//        false); // removable
    pContainer = new FlowPanel();
    this.add(pContainer);
  }

//  /**
//   * Returns user profile page.
//   *
//   * @return  User profile page.
//   */
//  public ProfilePage getUserProfilePage() {
//     return pPage;
//  }
}
