// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.admin;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;
import java.util.Date;

/**
 * Data Transfer Object representing user data.
 *
 */
public class AdminUser implements IsSerializable, Serializable {
  // Unique identifier for the user
  private String id;

  // user email address
  private String email;

  // user display name
  private String name;

  // whether user has accepted terms of service
  private boolean tosAccepted;

  // whether the user has admin priviledges
  private boolean isAdmin;

  // which type the user has
  private int type;

  private Date visited;         // When they last logged in

  private String password;

  /**
   * Creates a new user data transfer object.
   *
   * @param id unique user id (from {@link com.google.appengine.api.users.User#getUserId()}
   * @param name -- Their full name, if we have it (we often don't)
   * @param email user email address
   * @param tosAccepted TOS accepted?
   * @param isAdmin -- are they an admin
   * @param visited -- when they last logged in.
   */
  public AdminUser(String id, String name, String email, boolean tosAccepted, boolean isAdmin, Date visited) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.tosAccepted = tosAccepted;
    this.isAdmin = isAdmin;
    this.type = type;
    this.visited = visited;
  }

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private AdminUser() {
  }

  /**
   * Returns the user's unique id.
   *
   * @return user id
   */

  public String getId() {
    return id;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return this.name;
  }

  public boolean getTosaccepted() {
    return this.tosAccepted;
  }

  public boolean getIsAdmin() {
    return this.isAdmin;
  }

  public void setIsAdmin(boolean value) {
    this.isAdmin = value;
  }

  public int getType() {
    return this.type;
  }

  public Date getVisited() {
    return this.visited;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
