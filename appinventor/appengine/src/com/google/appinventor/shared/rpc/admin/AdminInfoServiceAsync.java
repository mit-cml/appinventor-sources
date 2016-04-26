// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.admin;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface for the service providing user admin related information.
 */
public interface AdminInfoServiceAsync {

  /**
   * @see AdminInfoService#searchUsers()
   */
  void searchUsers(String startingPoint, AsyncCallback<List<AdminUser>> callback);
  void storeUser(AdminUser user, AsyncCallback<Void> callback);
  void switchUser(AdminUser user, AsyncCallback<Void> callback);

}
