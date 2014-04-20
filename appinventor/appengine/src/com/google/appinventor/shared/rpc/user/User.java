// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.user;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Data Transfer Object representing user data.
 *
 */
public class User implements IsSerializable, UserInfoProvider, Serializable {
  // Unique identifier for the user
  private String id;

  // user email address
  private String email;

  // whether user has accepted terms of service
  private boolean tosAccepted;
  
  // whether the user has admin priviledges
  private boolean isAdmin;

  private String sessionId;        // Used to ensure only one account active at a time

  public final static String usercachekey = "f682688a-1065-4cda-8515-a8bd70200ac9"; // UUID
  // This UUID is prepended to any key lookup for User objects. Memcache is a common
  // key/value store for the entire application. By prepending a unique value, we ensure
  // that other modules that use Memcache will not collide with us. By using a UUID,
  // properly generated, we don't have to worry about allocating specific prefixes and
  // ensuring that they are unique.

  /**
   * Creates a new user data transfer object.
   *
   * @param id unique user id (from {@link com.google.appengine.api.users.User#getUserId()}
   * @param email user email address
   * @param tosAccepted TOS accepted?
   */
  public User(String id, String email, boolean tosAccepted, boolean isAdmin, String sessionId) {
    this.id = id;
    this.email = email;
    this.tosAccepted = tosAccepted;
    this.isAdmin = isAdmin;
    this.sessionId = sessionId;
  }

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private User() {
  }

  /**
   * Returns the user's unique id.
   *
   * @return user id
   */
  @Override
  public String getUserId() {
    return id;
  }

  /**
   * Returns the user's email address.
   *
   * @return user email address
   */
  @Override
  public String getUserEmail() {
    return email;
  }

  /**
   * Sets the user's email address.
   */
  public void setUserEmail(String email) {
    this.email = email;
  }

  /**
   * Sets whether the user has accepted the terms of service.
   *
   * @param tos {@code true} if the user has accepted the terms of service,
   *            {@code false} otherwise
   */
  public void setUserTosAccepted(boolean tos) {
    tosAccepted = tos;
  }

  /**
   * Returns whether the user has accepted the terms of service.
   *
   * @return {@code true} if the user has accepted the terms of service,
   *         {@code false} otherwise
   */
  @Override
  public boolean getUserTosAccepted() {
    return tosAccepted;
  }
  
  @Override
  public boolean getIsAdmin() {
    return isAdmin;
  }
  
  /**
   * Sets whether the user has admin priviledges.
   *
   * @param admin {@code true} if the user has admin priviledges,
   *              {@code false} otherwise
   */
  public void setIsAdmin(boolean admin) {
    isAdmin = admin;
  }


  /**
   * Returns this object.
   *
   * @return user
   */
  @Override
  public User getUser() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof User && ((User) obj).id == id;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Get the unique session id associated with this user
   * This is used to ensure that only one session is opened
   * per uers. Old sessions are invalidated.
   *
   * @return sessionId
   */
  @Override
  public String getSessionId() {
    return sessionId;
  }

  @Override
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public User copy() {
    return new User(id, email, tosAccepted, isAdmin, sessionId);
  }
}
