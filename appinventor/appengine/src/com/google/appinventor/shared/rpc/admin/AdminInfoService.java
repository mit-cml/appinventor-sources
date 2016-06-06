// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.admin;

import java.util.List;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.appinventor.shared.rpc.AdminInterfaceException;

/**
 * Interface for the service providing user related information.
 *
 */
@RemoteServiceRelativePath(ServerLayout.ADMIN_INFO_SERVICE)
public interface AdminInfoService extends RemoteService {

  /*
   * Retrieves up to 20 user objects based on a
   * search starting from the provided argument
   */

  List<AdminUser> searchUsers(String startingPoint);

  /*
   * Update or Add a user.
   */

  void storeUser(AdminUser user) throws AdminInterfaceException;

  /*
   * Switch to a different account, but readonly
   */

  void switchUser(AdminUser user) throws AdminInterfaceException;

}
