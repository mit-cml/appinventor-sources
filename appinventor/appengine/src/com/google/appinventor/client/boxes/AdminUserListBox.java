// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.AdminUserList;
import com.google.appinventor.client.widgets.boxes.Box;


/**
 * Box implementation for admin user list.
 *
 */
public final class AdminUserListBox extends Box {

  // Singleton project explorer box instance (only one project explorer allowed)
  private static final AdminUserListBox INSTANCE = new AdminUserListBox();

  // Project list for young android
  private final AdminUserList plist;

  /**
   * Returns the singleton admin user list box.
   *
   * @return  admin user list box
   */
  public static AdminUserListBox getAdminUserListBox() {
    return INSTANCE;
  }

  /**
   * Creates new admin user list box.
   */
  private AdminUserListBox() {
    super("User List",//MESSAGES.projectListBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    plist = new AdminUserList();
    setContent(plist);
  }

  /**
   * Returns user admin list associated with projects explorer box.
   *
   * @return  user admin list
   */
  public AdminUserList getAdminUserList() {
     return plist;
  }
}
