// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
   * Returns the user's name.
   *
   * @return user email name
   */
  String getUserName();

  /**
   * Returns the user's link.
   *
   * @return user link
   */
  String getUserLink();

  /**
   * Returns the email notification frequency set by user
   * @return emailFrequency email frequency
   */
  int getUserEmailFrequency();

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

  /**
   * Returns which type the user has
   *
   * @return user type
   */
  int getType();

  String getSessionId();

  void setSessionId(String SessionId);

}
