
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.ProfilePage;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.boxes.Box;



/**
 * Box implementation for user profile.
 *
 */
public final class ProfileBox extends Box {

  // Singleton Gallery explorer box instance (only one Gallery explorer allowed)
  private static final ProfileBox INSTANCE = new ProfileBox();

  // Gallery list for young android
  private static ProfilePage pPage;

  /**
   * Returns the singleton GalleryAppBox.
   *
   * @return  Gallery list box
   */
  public static ProfileBox getUserProfileBox() {
    return INSTANCE;
  }

  public static void setProfile(String userId, int editStatus)
  {
//    OdeLog.log("######### I got in setApp");
//    OdeLog.log("######### I got in setApp user " + userId);
//    OdeLog.log("######### I got in setApp edit " + editStatus);
    pPage = new ProfilePage(userId, editStatus);
    INSTANCE.setContent(pPage);
  }
  /**
   * Creates new user profile box.
   */
  private ProfileBox() {
    super(MESSAGES.userProfileBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    //pPage = new GalleryPage(app);
    //setContent(pPage);
  }

  /**
   * Returns user profile page.
   *
   * @return  User profile page.
   */
  public ProfilePage getUserProfilePage() {
     return pPage;
  }
}
