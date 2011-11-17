// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.user;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data Transfer Object representing user data.
 *
 */
public class User implements IsSerializable, UserInfoProvider {
  // Unique identifier for the user
  private String id;

  // user email address
  private String email;

  // whether user has accepted terms of service
  private boolean tosAccepted;

  /**
   * Creates a new user data transfer object.
   *
   * @param id unique user id (from {@link com.google.appengine.api.users.User#getUserId()}
   * @param email user email address
   * @param tosAccepted TOS accepted?
   */
  public User(String id, String email, boolean tosAccepted) {
    this.id = id;
    this.email = email;
    this.tosAccepted = tosAccepted;
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

  public User copy() {
    return new User(id, email, tosAccepted);
  }
}
