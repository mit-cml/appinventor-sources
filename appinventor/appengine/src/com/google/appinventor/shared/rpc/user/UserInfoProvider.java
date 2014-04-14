// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.user;

/**
 * Provides user information.
 *
 */
public interface UserInfoProvider {
  /**
   * Returns the unique user id.
   *
   * @return the user id
   */
  String getUserId();

  /**
   * Returns the user's email address.
   *
   * @return user email address
   */
  String getUserEmail();

  /**
   * Returns the user object.
   *
   * @return user object
   */
  User getUser();

  /**
   * Returns whether the user has accepted the terms of service.
   *
   * @return {@code true} if the user has accepted the terms of service,
   *         {@code false} otherwise
   */
  boolean getUserTosAccepted();
  
  /**
   * Returns whether the user has admin priviledges
   * 
   * @return {@code true} if the user has admin priviledges, 
   *         {@code false} otherwise
   */
  boolean getIsAdmin();

  String getSessionId();

  void setSessionId(String SessionId);

}
